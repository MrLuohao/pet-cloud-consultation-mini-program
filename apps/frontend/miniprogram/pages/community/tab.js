const { CommunityAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')
const { formatCommunityPostCard } = require('./post-presenter')

const TAB_OPTIONS = [
  { key: 'recommend', label: '推荐' },
  { key: 'following', label: '关注' },
  { key: 'hot', label: '热门' },
  { key: 'latest', label: '最新' }
]

function mapTabToType(tabKey) {
  if (tabKey === 'hot') return 'hot'
  if (tabKey === 'following') return 'following'
  return 'latest'
}

function formatCount(value) {
  const count = Number(value || 0)
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`
  }
  return `${count}`
}

function formatPostCard(post) {
  if (!post || typeof post !== 'object') return post

  const formattedPost = formatCommunityPostCard(post)

  return {
    ...formattedPost,
    likeCountText: formatCount(post.likeCount),
    commentCountText: formatCount(post.commentCount),
    collectCountText: formatCount(post.collectCount)
  }
}

Page({
  data: {
    tabs: TAB_OPTIONS,
    currentTab: 'recommend',
    hotTopics: [],
    posts: [],
    page: 1,
    pageSize: 10,
    isLoading: false,
    isRefreshing: false,
    hasMore: true
  },

  onLoad() {
    this.loadInitialData()
  },

  onShow() {
    if (this.data.posts.length > 0) {
      this.refreshPosts()
    }
  },

  async loadInitialData() {
    await Promise.all([
      this.loadHotTopics(),
      this.refreshPosts()
    ])
  },

  async loadHotTopics() {
    try {
      const topics = await CommunityAPI.getHotTopics()
      this.setData({
        hotTopics: Array.isArray(topics) ? topics.slice(0, 6) : []
      })
    } catch (error) {
      console.error('加载热门话题失败:', error)
    }
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (!tab || tab === this.data.currentTab) return

    if (tab === 'following' && !isLoggedIn()) {
      navigateToLogin()
      return
    }

    this.setData({
      currentTab: tab
    })
    this.refreshPosts()
  },

  async refreshPosts() {
    this.setData({
      page: 1,
      hasMore: true,
      posts: []
    })
    await this.loadPosts(true)
  },

  async loadPosts(isRefresh = false) {
    if (this.data.isLoading || (!this.data.hasMore && !isRefresh)) {
      return
    }

    this.setData({ isLoading: true })

    try {
      const { currentTab, page, pageSize } = this.data
      const result = await CommunityAPI.getFeed(page, pageSize, mapTabToType(currentTab))
      const list = Array.isArray(result && result.list) ? result.list.map(formatPostCard) : []
      this.setData({
        posts: page === 1 ? list : this.data.posts.concat(list),
        page: page + 1,
        hasMore: list.length >= pageSize
      })
    } catch (error) {
      console.error('加载社区帖子失败:', error)
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    } finally {
      this.setData({
        isLoading: false,
        isRefreshing: false
      })
    }
  },

  async onRefresh() {
    this.setData({ isRefreshing: true })
    await this.refreshPosts()
  },

  loadMore() {
    this.loadPosts()
  },

  previewImage(e) {
    const { urls, current } = e.currentTarget.dataset
    wx.previewImage({
      urls,
      current
    })
  },

  goToSearch() {
    wx.navigateTo({
      url: '/pages/community/search'
    })
  },

  goToTopic(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/community/topic?id=${id}`
    })
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/community/detail?id=${id}`
    })
  },

  goToPublish() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    wx.navigateTo({
      url: '/pages/community/publish'
    })
  },

  async toggleLike(e) {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { id, index } = e.currentTarget.dataset
    const post = this.data.posts[index]
    if (!post) return

    try {
      if (post.isLiked) {
        await CommunityAPI.unlikePost(id)
      } else {
        await CommunityAPI.likePost(id)
      }

      const nextLikeCount = Number(post.likeCount || 0) + (post.isLiked ? -1 : 1)
      this.setData({
        [`posts[${index}].isLiked`]: !post.isLiked,
        [`posts[${index}].likeCount`]: nextLikeCount,
        [`posts[${index}].likeCountText`]: formatCount(nextLikeCount)
      })
    } catch (error) {
      console.error('点赞失败:', error)
    }
  },

  async toggleCollect(e) {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { id, index } = e.currentTarget.dataset
    const post = this.data.posts[index]
    if (!post) return

    try {
      if (post.isCollected) {
        await CommunityAPI.uncollectPost(id)
      } else {
        await CommunityAPI.collectPost(id)
      }

      const nextCollectCount = Number(post.collectCount || 0) + (post.isCollected ? -1 : 1)
      this.setData({
        [`posts[${index}].isCollected`]: !post.isCollected,
        [`posts[${index}].collectCount`]: nextCollectCount,
        [`posts[${index}].collectCountText`]: formatCount(nextCollectCount)
      })
    } catch (error) {
      console.error('收藏失败:', error)
    }
  },

  async handleShare(e) {
    const { id } = e.currentTarget.dataset
    try {
      await CommunityAPI.sharePost(id, 'wechat')
    } catch (error) {
      console.error('分享记录失败:', error)
    }
  },

  onShareAppMessage() {
    return {
      title: '伴宠云诊社区',
      path: '/pages/community/tab'
    }
  }
})
