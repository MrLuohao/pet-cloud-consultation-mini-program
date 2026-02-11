const app = getApp()
const { AuthAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    userInfo: {},
    apiHost: ''
  },

  onShow() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadData()
  },

  async loadData() {
    this.setData({ apiHost: wx.getStorageSync('apiHost') || '' })
    try {
      const userInfo = await AuthAPI.getUserInfo()
      this.setData({ userInfo: userInfo || {} })
    } catch (error) {
      wx.showToast({ title: '用户信息加载失败', icon: 'none' })
    }
  },

  goProfile() {
    wx.navigateTo({ url: '/pages/user/profile' })
  },

  onApiHostInput(e) {
    this.setData({ apiHost: e.detail.value.trim() })
  },

  saveApiHost() {
    const host = this.data.apiHost
    if (!host) {
      wx.removeStorageSync('apiHost')
      wx.showToast({ title: '已恢复默认地址', icon: 'none' })
      return
    }

    const valid = /^(\d{1,3}\.){3}\d{1,3}$/.test(host) || /^[a-zA-Z0-9.-]+$/.test(host)
    if (!valid) {
      wx.showToast({ title: '地址格式不正确', icon: 'none' })
      return
    }

    wx.setStorageSync('apiHost', host)
    wx.showToast({ title: '已保存，重进页面生效', icon: 'none' })
  },

  resetApiHost() {
    wx.removeStorageSync('apiHost')
    this.setData({ apiHost: '' })
    wx.showToast({ title: '已恢复默认地址', icon: 'none' })
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确认退出当前账号？',
      success: async (res) => {
        if (!res.confirm) return

        try {
          await AuthAPI.logout()
        } catch (error) {
          // 忽略后端错误，继续本地退出
        }

        wx.removeStorageSync('token')
        wx.removeStorageSync('userId')
        wx.removeStorageSync('userInfo')
        app.globalData.token = null
        app.globalData.userId = null
        app.globalData.userInfo = null

        wx.switchTab({ url: '/pages/user/user' })
      }
    })
  }
})
