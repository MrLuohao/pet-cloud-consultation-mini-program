// pages/login/login.js - Apple 风格登录页逻辑
const app = getApp();
const { AuthAPI, AIAPI } = require('../../utils/api');

Page({
  data: {
    isLoading: false,
    avatarUrl: '',
    nickname: ''
  },

  onShow() {
    // 如果已登录，直接返回上一页
    if (app.globalData?.token) {
      wx.navigateBack({
        fail: () => {
          wx.switchTab({ url: '/pages/index/index' })
        }
      })
    }
  },

  // 选择头像
  onChooseAvatar(e) {
    const tempAvatarUrl = e.detail.avatarUrl
    if (tempAvatarUrl) {
      // 先显示临时头像
      this.setData({ avatarUrl: tempAvatarUrl })
    }
  },

  // 昵称输入
  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  // 昵称输入完成
  onNicknameBlur(e) {
    this.setData({ nickname: e.detail.value })
  },

  // 微信登录
  handleLogin() {
    if (this.data.isLoading) return;

    this.setData({ isLoading: true });

    // 获取微信登录code
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          console.log('登录成功，code:', loginRes.code);
          // 调用后端登录接口
          this.callBackendLogin(loginRes.code);
        } else {
          this.setData({ isLoading: false });
          wx.showToast({
            title: '获取登录凭证失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        console.error('wx.login 失败', err);
        this.setData({ isLoading: false });
        wx.showToast({
          title: '登录失败，请重试',
          icon: 'none'
        });
      }
    });
  },

  // 调用后端登录接口
  async callBackendLogin(code) {
    try {
      const { avatarUrl, nickname } = this.data

      // 如果用户选择了头像，先上传到服务器
      let uploadedAvatarUrl = ''
      if (avatarUrl && (avatarUrl.startsWith('http://tmp') || avatarUrl.startsWith('wxfile://'))) {
        try {
          uploadedAvatarUrl = await AIAPI.uploadImage(avatarUrl)
        } catch (e) {
          console.error('头像上传失败:', e)
          // 上传失败不影响登录
        }
      } else {
        uploadedAvatarUrl = avatarUrl
      }

      const result = await AuthAPI.login(
        code,
        nickname || '宠物主人',
        uploadedAvatarUrl || '',
        0
      );
      console.log('后端登录成功:', result);
      this.handleLoginSuccess(result, uploadedAvatarUrl, nickname);
    } catch (error) {
      console.error('后端登录失败:', error);
      this.setData({ isLoading: false });
      wx.showToast({
        title: error.message || '登录失败，请检查服务器是否启动',
        icon: 'none',
        duration: 3000
      });
    }
  },

  // 登录成功处理
  handleLoginSuccess(data, avatarUrl, nickname) {
    const app = getApp();

    // 构建用户信息对象（优先使用用户选择的头像和昵称）
    const userInfo = {
      id: data.userId,
      nickname: nickname || data.nickname || '宠物主人',
      avatarUrl: avatarUrl || data.avatarUrl || '',
      isVip: data.isVip || false,
      vipLevel: data.vipLevel || 'VIP',
      vipExpireDate: data.vipExpireDate || '',
      savingAmount: data.savingAmount || '0.00'
    };

    // 1. 存储登录凭证
    wx.setStorageSync('token', data.token);
    wx.setStorageSync('userId', data.userId);
    wx.setStorageSync('userInfo', userInfo);

    // 2. 更新全局状态
    app.globalData.token = data.token;
    app.globalData.userId = data.userId;
    app.globalData.userInfo = userInfo;

    // 3. 显示成功提示
    wx.showToast({
      title: data.isNewUser ? '注册成功' : '登录成功',
      icon: 'success',
      duration: 1500
    });

    // 4. 延迟后返回上一页
    setTimeout(() => {
      wx.navigateBack({
        fail: () => {
          // 如果返回失败，跳转首页
          wx.switchTab({ url: '/pages/index/index' })
        }
      })
    }, 1500);
  }
});
