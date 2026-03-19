// pages/message/conversations.js
const { PrivateMsgAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    conversations: [],
    isLoading: false
  },

  onLoad() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadConversations()
  },

  onShow() {
    if (isLoggedIn()) {
      this.loadConversations()
    }
  },

  async loadConversations() {
    this.setData({ isLoading: true })

    try {
      const result = await PrivateMsgAPI.getConversations(1, 50)
      this.setData({
        conversations: result.list || [],
        isLoading: false
      })
    } catch (error) {
      console.error('加载会话列表失败:', error)
      this.setData({ isLoading: false })
    }
  },

  goToChat(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/message/chat?targetId=${id}` })
  }
})
