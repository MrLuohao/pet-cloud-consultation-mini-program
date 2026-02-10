// pages/login/login.js
Page({
  data: {
    code: null
  },

  // 微信登录
  handleLogin() {
    // 1. 获取临时code
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          this.setData({ code: loginRes.code });
          
          // 2. 获取用户信息
          wx.getUserProfile({
            desc: '获取你的昵称、头像信息',
            success: (userRes) => {
              // 3. 发送登录请求
              this.sendLoginRequest(loginRes.code, userRes);
            },
            fail: (err) => {
              console.error('获取用户信息失败', err);
              wx.showToast({
                title: '获取信息失败',
                icon: 'none'
              });
            }
          });
        }
      },
      fail: (err) => {
        console.error('登录失败', err);
        wx.showToast({
          title: '登录失败',
          icon: 'none'
        });
      }
    });
  },

  // 发送登录请求
  sendLoginRequest(code, userProfile) {
    const app = getApp();
    
    wx.request({
      url: 'https://your-domain.com/api/login', // 替换为你的后端API地址
      method: 'POST',
      data: {
        code: code,
        rawData: userProfile.rawData,
        signature: userProfile.signature,
        encryptedData: userProfile.encryptedData,
        iv: userProfile.iv
      },
      success: (res) => {
        if (res.data && res.data.success) {
          // 登录成功处理
          this.handleLoginSuccess(res.data);
        } else {
          wx.showToast({
            title: res.data.message || '登录失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        console.error('登录请求失败', err);
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      }
    });
  },

  // 登录成功处理
  handleLoginSuccess(data) {
    const app = getApp();
    
    // 1. 存储登录凭证
    wx.setStorageSync('token', data.token);
    wx.setStorageSync('userInfo', data.userInfo);
    
    // 2. 更新全局状态
    app.globalData.token = data.token;
    app.globalData.userInfo = data.userInfo;
    
    // 3. 获取并执行回调
    const callback = wx.getStorageSync('loginCallback');
    
    if (callback) {
      try {
        const callbackFn = new Function(`return ${callback}`)();
        wx.navigateBack({
          success: () => {
            // 延迟执行确保页面已渲染
            setTimeout(callbackFn, 300);
          }
        });
      } catch (e) {
        console.error('执行回调失败', e);
        wx.switchTab({ url: '/pages/index/index' });
      }
    } else {
      wx.switchTab({ url: '/pages/index/index' });
    }
  }
});