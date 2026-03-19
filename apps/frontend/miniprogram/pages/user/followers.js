// pages/user/followers.js
const { FollowAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    userId: null,
    users: [],
    page: 1,
    hasMore: true,
    isLoading: false
  },

  onLoad(options) {
    this.setData({ userId: options.userId || options.id })
    this.loadFollowers()
  },

  async loadFollowers() {
    if (this.data.isLoading || !this.data.hasMore) return

    this.setData({ isLoading: true })

    try {
      const { userId, page } = this.data
      const result = await FollowAPI.getFollowers(userId, page, 20)
      const newUsers = result.list || []

      this.setData({
        users: page === 1 ? newUsers : [...this.data.users, ...newUsers],
        hasMore: newUsers.length >= 20,
        page: page + 1,
        isLoading: false
      })
    } catch (error) {
      console.error('加载粉丝列表失败:', error)
      this.setData({ isLoading: false })
    }
  },

  async toggleFollow(e) {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { id, index } = e.currentTarget.dataset
    const user = this.data.users[index]
    const isFollowed = user.isFollowed

    try {
      if (isFollowed) {
        await FollowAPI.unfollow(id)
      } else {
        await FollowAPI.follow(id)
      }

      this.setData({
        [`users[${index}].isFollowed`]: !isFollowed
      })
    } catch (error) {
      console.error('操作失败:', error)
      wx.showToast({ title: '操作失败', icon: 'none' })
    }
  },

  goToProfile(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/user/profile?id=${id}` })
  },

  onReachBottom() {
    this.loadFollowers()
  }
})
