// pages/community/topic.js
const { CommunityAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')
const { formatCommunityPostCard } = require('./post-presenter')

Page({
  data: {
    topicId: null,
    topic: null,
    posts: [],
    page: 1,
    hasMore: true,
    isLoading: false
  },

  onLoad(options) {
    this.setData({ topicId: options.id })
    this.loadTopicDetail()
    this.loadPosts()
  },

  async loadTopicDetail() {
    try {
      const topic = await CommunityAPI.getTopicDetail(this.data.topicId)
      this.setData({ topic })
      wx.setNavigationBarTitle({ title: topic.name || '话题' })
    } catch (error) {
      console.error('加载话题失败:', error)
    }
  },

  async loadPosts() {
    if (this.data.isLoading || !this.data.hasMore) return

    this.setData({ isLoading: true })

    try {
      const { page } = this.data
      const result = await CommunityAPI.getTopicPosts(this.data.topicId, page, 20)
      const newPosts = Array.isArray(result.list) ? result.list.map(post => formatCommunityPostCard(post)) : []

      this.setData({
        posts: page === 1 ? newPosts : [...this.data.posts, ...newPosts],
        hasMore: newPosts.length >= 20,
        page: page + 1,
        isLoading: false
      })
    } catch (error) {
      console.error('加载帖子失败:', error)
      this.setData({ isLoading: false })
    }
  },

  async toggleLike(e) {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { id, index } = e.currentTarget.dataset
    const post = this.data.posts[index]
    const isLiked = post.isLiked

    try {
      if (isLiked) {
        await CommunityAPI.unlikePost(id)
      } else {
        await CommunityAPI.likePost(id)
      }

      this.setData({
        [`posts[${index}].isLiked`]: !isLiked,
        [`posts[${index}].likeCount`]: post.likeCount + (isLiked ? -1 : 1)
      })
    } catch (error) {
      console.error('操作失败:', error)
    }
  },

  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/community/detail?id=${id}` })
  },

  goToPublish() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    wx.navigateTo({ url: `/pages/community/publish?topicId=${this.data.topicId}` })
  },

  onReachBottom() {
    this.loadPosts()
  },

  onShareAppMessage() {
    const { topic } = this.data
    return {
      title: `#${topic.name} - 伴宠云诊社区`,
      path: `/pages/community/topic?id=${this.data.topicId}`
    }
  }
})
