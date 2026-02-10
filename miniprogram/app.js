// app.js
App({
  globalData: {
    token: null,
    userInfo: null
  },

  onLaunch() {
    // 检查登录状态
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      this.globalData.userInfo = wx.getStorageSync('userInfo');
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