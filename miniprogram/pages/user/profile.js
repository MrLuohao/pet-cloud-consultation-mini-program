/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  个人资料编辑逻辑 | Profile Edit Page Logic                                        ║
 * ║  Design: Purple • Blue • Pink • White                                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const app = getApp();
const { AuthAPI, AIAPI } = require('../../utils/api');

Page({
  data: {
    userInfo: {
      avatarUrl: '',
      nickname: '',
      gender: 0
    },
    originalInfo: {
      avatarUrl: '',
      nickname: '',
      gender: 0
    },
    genderText: '保密',
    showGenderPicker: false,
    canSave: false,
    saving: false
  },

  onLoad() {
    this.loadUserInfo();
  },

  // 加载用户信息
  loadUserInfo() {
    // 从全局数据或缓存获取
    const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo') || {};
    const gender = userInfo.gender || 0;

    this.setData({
      'userInfo.avatarUrl': userInfo.avatarUrl || '',
      'userInfo.nickname': userInfo.nickname || '',
      'userInfo.gender': gender,
      'originalInfo.avatarUrl': userInfo.avatarUrl || '',
      'originalInfo.nickname': userInfo.nickname || '',
      'originalInfo.gender': gender,
      genderText: this.getGenderText(gender)
    });

    // 从 API 刷新用户信息
    this.refreshUserInfo();
  },

  // 从 API 刷新用户信息
  async refreshUserInfo() {
    try {
      const userData = await AuthAPI.getUserInfo();
      if (userData) {
        const gender = userData.gender || 0;
        this.setData({
          'userInfo.avatarUrl': userData.avatarUrl || '',
          'userInfo.nickname': userData.nickname || '',
          'userInfo.gender': gender,
          'originalInfo.avatarUrl': userData.avatarUrl || '',
          'originalInfo.nickname': userData.nickname || '',
          'originalInfo.gender': gender,
          genderText: this.getGenderText(gender)
        });
      }
    } catch (error) {
      console.error('刷新用户信息失败:', error);
    }
  },

  // 获取性别文本
  getGenderText(gender) {
    const genderMap = {
      0: '保密',
      1: '男',
      2: '女'
    };
    return genderMap[gender] || '保密';
  },

  // ==================== 头像相关 ====================
  onChooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        wx.showLoading({ title: '上传中...' });

        try {
          // 上传头像到服务器
          const uploadedUrl = await AIAPI.uploadImage(tempFilePath);
          this.setData({
            'userInfo.avatarUrl': uploadedUrl
          });
          this.checkCanSave();
          wx.hideLoading();
        } catch (error) {
          wx.hideLoading();
          wx.showToast({
            title: '上传失败',
            icon: 'none'
          });
          console.error('头像上传失败:', error);
        }
      }
    });
  },

  // ==================== 昵称相关 ====================
  onNicknameInput(e) {
    const nickname = e.detail.value;
    this.setData({
      'userInfo.nickname': nickname
    });
    this.checkCanSave();
  },

  // ==================== 性别相关 ====================
  onGenderTap() {
    this.setData({
      showGenderPicker: true
    });
  },

  onCloseGenderPicker() {
    this.setData({
      showGenderPicker: false
    });
  },

  onSelectGender(e) {
    const gender = parseInt(e.currentTarget.dataset.gender);
    this.setData({
      'userInfo.gender': gender,
      genderText: this.getGenderText(gender),
      showGenderPicker: false
    });
    this.checkCanSave();
  },

  // ==================== 保存相关 ====================
  checkCanSave() {
    const { userInfo, originalInfo } = this.data;

    // 检查是否有修改
    const hasChange =
      userInfo.avatarUrl !== originalInfo.avatarUrl ||
      userInfo.nickname !== originalInfo.nickname ||
      userInfo.gender !== originalInfo.gender;

    // 检查昵称是否有效
    const nicknameValid = userInfo.nickname && userInfo.nickname.trim().length > 0;

    this.setData({
      canSave: hasChange && nicknameValid
    });
  },

  async onSave() {
    if (!this.data.canSave || this.data.saving) {
      return;
    }

    this.setData({ saving: true });

    try {
      const { userInfo } = this.data;

      // 调用 API 更新用户信息
      await AuthAPI.updateUserInfo({
        nickname: userInfo.nickname,
        avatarUrl: userInfo.avatarUrl,
        gender: userInfo.gender
      });

      // 更新全局数据和缓存
      const existingInfo = app.globalData.userInfo || wx.getStorageSync('userInfo') || {};
      const newUserInfo = {
        ...existingInfo,
        avatarUrl: userInfo.avatarUrl,
        nickname: userInfo.nickname,
        gender: userInfo.gender
      };
      app.globalData.userInfo = newUserInfo;
      wx.setStorageSync('userInfo', newUserInfo);

      wx.showToast({
        title: '保存成功',
        icon: 'success'
      });

      // 延迟返回上一页
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);

    } catch (error) {
      console.error('保存用户信息失败:', error);
      wx.showToast({
        title: error.message || '保存失败',
        icon: 'none'
      });
      this.setData({ saving: false });
    }
  }
});
