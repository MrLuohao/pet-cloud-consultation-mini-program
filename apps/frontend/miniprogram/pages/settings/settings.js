const app = getApp()
const { AuthAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')
const config = require('../../utils/config')

Page({
  data: {
    userInfo: {},
    apiHost: '',
    defaultHost: '',
    recommendedHost: '',
    platformLabel: '',
    currentUserApi: '',
    currentShopApi: '',
    connectionStatus: '',  // '', 'testing', 'success', 'fail'
    statusMessage: ''
  },

  onShow() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadData()
  },

  async loadData() {
    const defaultConfig = config.getDefaultConfig()
    const isDevtools = config.isDevtoolsPlatform()
    this.setData({
      apiHost: config.getApiHost(),
      defaultHost: defaultConfig.API_HOST,
      recommendedHost: config.getRecommendedApiHost(),
      platformLabel: isDevtools ? '开发者工具' : '真机',
      currentUserApi: config.getApiBaseUrl(),
      currentShopApi: config.getShopApiBaseUrl()
    })
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

  async saveApiHost() {
    const host = this.data.apiHost.trim()

    if (!host) {
      config.setApiHost(null)
      this.setData({
        apiHost: config.getApiHost(),
        currentUserApi: config.getApiBaseUrl(),
        currentShopApi: config.getShopApiBaseUrl(),
        connectionStatus: '',
        statusMessage: ''
      })
      wx.showToast({ title: '已恢复默认地址', icon: 'success' })
      return
    }

    // 验证IP或域名格式
    const valid = /^(\d{1,3}\.){3}\d{1,3}$/.test(host) || /^[a-zA-Z0-9.-]+$/.test(host)
    if (!valid) {
      wx.showToast({ title: '地址格式不正确', icon: 'none' })
      return
    }

    config.setApiHost(host)
    this.setData({
      currentUserApi: config.getApiBaseUrl(),
      currentShopApi: config.getShopApiBaseUrl()
    })
    wx.showToast({ title: '已保存', icon: 'success' })

    // 自动测试连接
    await this.testConnection()
  },

  async useRecommendedHost() {
    const host = this.data.recommendedHost
    config.setApiHost(host)
    this.setData({
      apiHost: host,
      currentUserApi: config.getApiBaseUrl(),
      currentShopApi: config.getShopApiBaseUrl()
    })
    wx.showToast({ title: '已应用推荐地址', icon: 'success' })
    await this.testConnection()
  },

  resetApiHost() {
    config.setApiHost(null)
    this.setData({
      apiHost: config.getApiHost(),
      currentUserApi: config.getApiBaseUrl(),
      currentShopApi: config.getShopApiBaseUrl(),
      connectionStatus: '',
      statusMessage: ''
    })
    wx.showToast({ title: '已恢复默认地址', icon: 'success' })
  },

  async testConnection() {
    this.setData({ connectionStatus: 'testing', statusMessage: '测试中...' })

    const result = await config.testConnection()

    this.setData({
      connectionStatus: result.success ? 'success' : 'fail',
      statusMessage: result.message
    })

    if (result.success) {
      wx.showToast({ title: '连接成功', icon: 'success' })
    }
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
