// pages/shop/subscription/index.js - 宠物订阅套餐购买（Story 5.2）
const { SubscriptionAPI, PetAPI, isLoggedIn, navigateToLogin } = require('../../../utils/api');

Page({
  data: {
    loading: false,
    subscribing: false,
    pets: [],
    selectedPetId: null,
    mySubs: [],         // 我的订阅列表
    activeTab: 0,       // 0=购买套餐 1=我的订阅

    plans: [
      {
        id: 'basic',
        name: '基础护理包',
        tag: '入门',
        tagColor: 'green',
        price: '99',
        originalPrice: '129',
        period: '月',
        desc: '每月精选护理套装配送上门',
        features: ['每月1次配送', '基础驱虫用品', '营养零食礼包', '护理小工具'],
        icon: '🎁'
      },
      {
        id: 'standard',
        name: '标准健康包',
        tag: '推荐',
        tagColor: 'purple',
        price: '199',
        originalPrice: '269',
        period: '月',
        desc: '全面健康管理，专业兽医推荐配方',
        features: ['每月2次配送', '专业营养品', '健康检测套件', '线上问诊1次', '个性化方案'],
        icon: '🏥'
      },
      {
        id: 'premium',
        name: '高端宠物订阅',
        tag: '尊享',
        tagColor: 'gold',
        price: '399',
        originalPrice: '599',
        period: '月',
        desc: '顶级宠物生活方式，专属定制服务',
        features: ['每月4次配送', '进口高端零食', '上门美容1次', '线上问诊3次', '专属客服', '生日惊喜礼'],
        icon: '👑'
      }
    ],
    selectedPlan: 1,

    frequencies: ['每月', '每季度', '每半年'],
    selectedFreq: 0,

    statusLabels: { active: '生效中', paused: '已暂停', cancelled: '已取消' },
    statusColors: { active: 'green', paused: 'orange', cancelled: 'gray' }
  },

  onLoad() {
    if (!isLoggedIn()) {
      navigateToLogin();
      return;
    }
    this.loadPets();
    this.loadMySubs();
  },

  onShow() {
    if (isLoggedIn()) this.loadMySubs();
  },

  async loadPets() {
    try {
      const pets = await PetAPI.getList();
      this.setData({
        pets: pets || [],
        selectedPetId: pets && pets.length > 0 ? pets[0].id : null
      });
    } catch (e) {
      console.error('加载宠物失败:', e);
    }
  },

  async loadMySubs() {
    try {
      const list = await SubscriptionAPI.getList();
      this.setData({ mySubs: list || [] });
    } catch (e) {
      console.error('加载订阅失败:', e);
    }
  },

  onTabSwitch(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab });
  },

  onPlanSelect(e) {
    this.setData({ selectedPlan: e.currentTarget.dataset.index });
  },

  onPetSelect(e) {
    this.setData({ selectedPetId: e.currentTarget.dataset.id });
  },

  onFreqSelect(e) {
    this.setData({ selectedFreq: e.currentTarget.dataset.index });
  },

  // 立即订阅
  async onSubscribe() {
    const { selectedPetId, plans, selectedPlan, frequencies, selectedFreq, subscribing } = this.data;
    if (subscribing) return;
    if (!selectedPetId) {
      wx.showToast({ title: '请选择宠物', icon: 'none' });
      return;
    }
    const plan = plans[selectedPlan];
    this.setData({ subscribing: true });
    wx.showLoading({ title: '创建订阅中...' });
    try {
      await SubscriptionAPI.create({
        petId: selectedPetId,
        planType: plan.id,
        frequency: frequencies[selectedFreq]
      });
      wx.hideLoading();
      wx.showToast({ title: '订阅成功！', icon: 'success' });
      this.loadMySubs();
      setTimeout(() => this.setData({ activeTab: 1 }), 1200);
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '订阅失败，请重试', icon: 'none' });
    } finally {
      this.setData({ subscribing: false });
    }
  },

  // 暂停订阅
  async onPause(e) {
    const id = e.currentTarget.dataset.id;
    wx.showLoading({ title: '处理中...' });
    try {
      await SubscriptionAPI.pause(id);
      wx.hideLoading();
      wx.showToast({ title: '已暂停', icon: 'success' });
      this.loadMySubs();
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '操作失败', icon: 'none' });
    }
  },

  // 恢复订阅
  async onResume(e) {
    const id = e.currentTarget.dataset.id;
    wx.showLoading({ title: '处理中...' });
    try {
      await SubscriptionAPI.resume(id);
      wx.hideLoading();
      wx.showToast({ title: '已恢复', icon: 'success' });
      this.loadMySubs();
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '操作失败', icon: 'none' });
    }
  },

  // 取消订阅
  onCancel(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '取消订阅',
      content: '确定要取消此订阅吗？下次配送日期前均可享受当期权益。',
      confirmColor: '#ff3b30',
      success: async (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '处理中...' });
        try {
          await SubscriptionAPI.cancel(id);
          wx.hideLoading();
          wx.showToast({ title: '已取消', icon: 'success' });
          this.loadMySubs();
        } catch (error) {
          wx.hideLoading();
          wx.showToast({ title: error.message || '操作失败', icon: 'none' });
        }
      }
    });
  }
});
