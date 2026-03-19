const { AuthAPI } = require('./utils/api')

// app.js
App({
  globalData: {
    token: null,
    userId: null,
    userInfo: null,
    needRefreshTasks: false
  },

  onLaunch() {
    // 检查登录状态
    const token = wx.getStorageSync('token');
    const userId = wx.getStorageSync('userId');
    if (token) {
      this.globalData.token = token;
      this.globalData.userId = userId;
      this.globalData.userInfo = null;
      this.refreshUserInfo();
    }
  },

  async refreshUserInfo() {
    if (!this.globalData.token) return
    try {
      const userInfo = await AuthAPI.getUserInfo()
      if (!userInfo) return
      this.globalData.userInfo = userInfo
      wx.setStorageSync('userInfo', userInfo || {})
    } catch (error) {
      console.error('刷新用户信息失败:', error)
    }
  },

  // 统一登录方法
  checkLogin(callback) {
    if (this.globalData.token) {
      callback && callback();
      return true;
    }

    // 存储回调函数
    wx.setStorageSync('loginCallback', callback.toString());

    wx.navigateTo({
      url: '/pages/login/login'
    });
    return false;
  }
});
