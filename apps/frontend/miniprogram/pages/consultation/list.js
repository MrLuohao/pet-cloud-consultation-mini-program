// pages/consultation/list.js
const { ConsultationAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    activeTab: '',
    consultationList: []
  },

  onLoad() {
    // 咨询记录页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadConsultationList()
  },

  onShow() {
    // 未登录时不执行任何操作
    if (!isLoggedIn()) {
      return
    }
    this.loadConsultationList()
  },

  // 切换Tab
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    this.loadConsultationList()
  },

  // 加载咨询列表
  async loadConsultationList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const list = await ConsultationAPI.getList()
      let filteredList = list
      if (this.data.activeTab !== '') {
        const status = parseInt(this.data.activeTab)
        filteredList = list.filter(item => item.status === status)
      }
      this.setData({ consultationList: filteredList })
    } catch (error) {
      console.error('加载咨询列表失败:', error)
    } finally {
      wx.hideLoading()
    }
  },

  // 进入聊天
  gotoChat(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/consultation/chat?id=${id}`
    })
  }
})
