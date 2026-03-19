// pages/consultation/urgent/index.js - 紧急问诊快速响应通道（Story 5.1）
const { ConsultationAPI, PetAPI, AIAPI, isLoggedIn, navigateToLogin } = require('../../../utils/api');

Page({
  data: {
    pets: [],
    selectedPet: null,
    description: '',
    images: [],
    submitting: false,
    countdownText: '5分钟内响应',
    showResponseBadge: true
  },

  onLoad() {
    if (!isLoggedIn()) {
      navigateToLogin();
      return;
    }
    this.loadPets();
    this.startCountdown();
  },

  onUnload() {
    if (this._timer) clearInterval(this._timer);
  },

  async loadPets() {
    try {
      const pets = await PetAPI.getList();
      this.setData({
        pets: pets || [],
        selectedPet: pets && pets.length > 0 ? pets[0] : null
      });
    } catch (error) {
      console.error('加载宠物失败:', error);
    }
  },

  // 倒计时动画效果
  startCountdown() {
    const texts = ['5分钟内响应', '医生等待中...', '优先派单中...'];
    let i = 0;
    this._timer = setInterval(() => {
      i = (i + 1) % texts.length;
      this.setData({ countdownText: texts[i] });
    }, 2000);
  },

  onPetSelect(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ selectedPet: this.data.pets[index] });
  },

  onDescriptionInput(e) {
    this.setData({ description: e.detail.value });
  },

  // 快捷症状词填充
  onHintTap(e) {
    const text = e.currentTarget.dataset.text;
    const current = this.data.description;
    const appended = current ? current + '，' + text : text;
    this.setData({ description: appended });
  },

  // 预览图片
  onPreviewImage(e) {
    const idx = e.currentTarget.dataset.index;
    wx.previewImage({ urls: this.data.images, current: this.data.images[idx] });
  },

  // 上传图片
  async onChooseImage() {
    if (this.data.images.length >= 3) {
      wx.showToast({ title: '最多上传3张', icon: 'none' });
      return;
    }
    const res = await new Promise((resolve, reject) =>
      wx.chooseMedia({
        count: 3 - this.data.images.length,
        mediaType: ['image'],
        success: resolve, fail: reject
      })
    );
    const files = res.tempFiles.map(f => f.tempFilePath);
    this.setData({ images: [...this.data.images, ...files] });
  },

  onRemoveImage(e) {
    const idx = e.currentTarget.dataset.index;
    const images = [...this.data.images];
    images.splice(idx, 1);
    this.setData({ images });
  },

  // 提交紧急问诊
  async onSubmitUrgent() {
    const { selectedPet, description, submitting } = this.data;
    if (submitting) return;
    if (!selectedPet) {
      wx.showToast({ title: '请选择宠物', icon: 'none' });
      return;
    }
    if (!description.trim()) {
      wx.showToast({ title: '请描述紧急症状', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '正在为您匹配医生...' });
    try {
      // 先上传所有图片到服务器
      const uploadedUrls = [];
      for (const tempPath of this.data.images) {
        const url = await AIAPI.uploadImage(tempPath);
        uploadedUrls.push(url);
      }
      const consultationId = await ConsultationAPI.create(
        selectedPet.id,
        null,          // 紧急问诊由后端自动分配在线医生
        1,             // 图文类型
        description,
        JSON.stringify(uploadedUrls),
        'urgent'       // 紧急标识
      );
      wx.hideLoading();
      wx.showToast({ title: '医生已接单！', icon: 'success' });
      setTimeout(() => {
        wx.redirectTo({
          url: `/pages/consultation/chat?id=${consultationId}`
        });
      }, 1500);
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '提交失败，请重试', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
