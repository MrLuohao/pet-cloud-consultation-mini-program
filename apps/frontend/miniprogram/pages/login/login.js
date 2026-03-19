const app = getApp();
const { AuthAPI, AIAPI } = require('../../utils/api');

Page({
  data: {
    isLoading: false,
    isUploadingAvatar: false,
    avatarUrl: '',
    nickname: ''
  },

  onLoad() {
    this.hydrateDraftProfile()
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

  hydrateDraftProfile() {
    const globalUserInfo = app.globalData?.userInfo || {}
    let cachedUserInfo = {}

    try {
      cachedUserInfo = wx.getStorageSync('userInfo') || {}
    } catch (error) {
      cachedUserInfo = {}
    }

    const nickname = this.getDraftText(globalUserInfo.nickname || cachedUserInfo.nickname)
    const avatarUrl = this.getDraftAvatarUrl(globalUserInfo.avatarUrl || cachedUserInfo.avatarUrl)

    this.setData({
      nickname,
      avatarUrl
    })
  },

  getDraftText(value) {
    return typeof value === 'string' ? value.trim() : ''
  },

  getDraftAvatarUrl(url) {
    return typeof url === 'string' ? url : ''
  },

  isTempAvatarUrl(url) {
    if (!url || typeof url !== 'string') return false
    return url.startsWith('wxfile://') ||
      url.startsWith('http://tmp') ||
      /https?:\/\/(127\.0\.0\.1|localhost):\d+\/__tmp__\//.test(url)
  },

  // 选择头像 —— 尝试立即上传，将临时URL转为服务器URL后再渲染
  // 如果上传失败（如首次登录无token），用 base64 预览，避免临时URL触发渲染层500报错
  async onChooseAvatar(e) {
    const tempAvatarUrl = e.detail.avatarUrl
    if (!tempAvatarUrl) return

    if (this.isTempAvatarUrl(tempAvatarUrl)) {
      this.setData({ isUploadingAvatar: true })
      try {
        const uploadedAvatarUrl = await AIAPI.uploadImage(tempAvatarUrl)
        this.setData({ avatarUrl: uploadedAvatarUrl || '' })
      } catch (error) {
        console.error('头像上传失败:', error)
        // 上传失败（通常是首次登录无token）：
        // 将临时文件转为 base64 用于预览，避免 <image> 请求临时URL触发500
        this._pendingAvatarPath = tempAvatarUrl
        try {
          const base64Data = wx.getFileSystemManager().readFileSync(tempAvatarUrl, 'base64')
          this.setData({ avatarUrl: 'data:image/jpeg;base64,' + base64Data })
        } catch (fsErr) {
          this.setData({ avatarUrl: '' })
        }
      } finally {
        this.setData({ isUploadingAvatar: false })
      }
      return
    }

    this.setData({ avatarUrl: tempAvatarUrl })
  },

  // 头像预览加载失败时的兜底（临时文件过期等场景）
  onAvatarError() {
    this.setData({ avatarUrl: '' })
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
    if (this.data.isLoading || this.data.isUploadingAvatar) return;

    this.setData({ isLoading: true });

    // 获取微信登录code
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          console.log('登录成功，code:', loginRes.code);
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

  // 调用后端登录接口 —— 先登录拿 token，头像上传在 handleLoginSuccess 中完成
  async callBackendLogin(code) {
    try {
      const nickname = (this.data.nickname || '').trim()

      const result = await AuthAPI.login(
        code,
        nickname,
        '',  // 头像在拿到 token 后才能上传，此处先留空
        0
      );
      console.log('后端登录成功:', result);
      this.handleLoginSuccess(result);
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
  async handleLoginSuccess(data) {
    const app = getApp();

    // 1. 先存储登录凭证（必须在任何 authRequest 之前完成）
    wx.setStorageSync('token', data.token);
    wx.setStorageSync('userId', data.userId);
    app.globalData.token = data.token;
    app.globalData.userId = data.userId;

    // 2. 现在有 token 了，上传头像（优先用已上传的服务器URL，否则重试之前失败的临时路径）
    let uploadedAvatarUrl = ''
    const { avatarUrl } = this.data
    const pendingPath = this._pendingAvatarPath || ''
    const avatarToUpload = avatarUrl || pendingPath

    if (avatarToUpload) {
      try {
        if (this.isTempAvatarUrl(avatarToUpload)) {
          uploadedAvatarUrl = await AIAPI.uploadImage(avatarToUpload)
        } else {
          uploadedAvatarUrl = avatarToUpload
        }
        if (uploadedAvatarUrl) {
          await AuthAPI.updateUserInfo({ avatarUrl: uploadedAvatarUrl })
        }
      } catch (e) {
        console.error('头像上传失败（不影响登录）:', e)
      }
      this._pendingAvatarPath = ''
    }

    // 3. 拉取最新用户信息（包含刚上传的头像）
    try {
      const latestUserInfo = await AuthAPI.getUserInfo()
      const userInfo = {
        ...(latestUserInfo || {}),
        id: (latestUserInfo && latestUserInfo.id) || data.userId
      }

      wx.setStorageSync('userInfo', userInfo);
      app.globalData.userInfo = userInfo;
    } catch (error) {
      console.error('拉取最新用户信息失败:', error)
      // 兜底：使用 login 返回的基础信息 + 已上传的头像 URL
      const fallbackUserInfo = {
        id: data.userId,
        nickname: data.nickname || '',
        avatarUrl: uploadedAvatarUrl || ''
      }
      wx.setStorageSync('userInfo', fallbackUserInfo);
      app.globalData.userInfo = fallbackUserInfo;
    }

    // 4. 提示并跳转
    wx.showToast({
      title: data.isNewUser ? '注册成功' : '登录成功',
      icon: 'success',
      duration: 1500
    });

    setTimeout(() => {
      wx.navigateBack({
        fail: () => {
          wx.switchTab({ url: '/pages/index/index' })
        }
      })
    }, 1500);

    this.setData({ isLoading: false });
  }
});
