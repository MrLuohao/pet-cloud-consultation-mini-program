// pages/beauty/beauty.js - 美容服务逻辑
const { BeautyAPI } = require('../../utils/api');

Page({
  data: {
    stores: [],
    loading: false
  },

  onLoad() {
    this.loadStores();
  },

  // 加载门店列表
  async loadStores() {
    this.setData({ loading: true });
    try {
      const stores = await BeautyAPI.getStoreList();
      const formattedStores = (stores || []).map((store, index) => ({
        id: store.id,
        name: store.name,
        rating: store.rating || 0,
        distance: store.distance || '未知',
        address: store.address,
        tags: store.tags ? store.tags.split(',') : [],
        emoji: this.getStoreEmoji(index),
        color: this.getStoreColor(index)
      }));
      this.setData({ stores: formattedStores, loading: false });
    } catch (error) {
      console.error('加载门店失败:', error);
      this.setData({ stores: [], loading: false });
      wx.showToast({ title: '暂无门店，请稍后再来', icon: 'none' });
    }
  },

  // 获取门店emoji
  getStoreEmoji(index) {
    const emojis = ['🐶', '🐱', '✨', '🛁', '💅'];
    return emojis[index % emojis.length];
  },

  // 获取门店颜色
  getStoreColor(index) {
    const colors = ['pink', 'blue', 'green', 'yellow'];
    return colors[index % colors.length];
  },

  // 返回
  onBack() {
    wx.navigateBack();
  },

  // 门店点击 → 跳转详情页
  onStoreTap(e) {
    const store = e.currentTarget.dataset.store;
    wx.navigateTo({
      url: `/pages/beauty/detail/index?id=${store.id}`
    });
  },

  // 跳转预约页面
  goToBooking(store) {
    wx.navigateTo({
      url: `/pages/beauty/booking/index?storeId=${store.id}`
    });
  }
});
