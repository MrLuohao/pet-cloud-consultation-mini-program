// pages/beauty/detail/index.js - 门店详情页
const { BeautyAPI } = require('../../../utils/api');

Page({
  data: {
    storeId: null,
    store: null,
    services: [],
    loading: true
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ storeId: options.id });
      this.loadStore(options.id);
      this.loadServices(options.id);
    }
  },

  async loadStore(id) {
    try {
      const store = await BeautyAPI.getStoreDetail(id);
      this.setData({
        store: {
          ...store,
          tagsArr: store.tags ? store.tags.split(',') : []
        },
        loading: false
      });
    } catch (error) {
      console.error('加载门店详情失败:', error);
      this.setData({ loading: false });
    }
  },

  async loadServices(id) {
    try {
      const services = await BeautyAPI.getStoreServices(id);
      this.setData({ services: services || [] });
    } catch (error) {
      console.error('加载服务项目失败:', error);
      this.setData({ services: [] });
    }
  },

  onBack() {
    wx.navigateBack();
  },

  goToBooking() {
    const { storeId } = this.data;
    wx.navigateTo({
      url: `/pages/beauty/booking/index?storeId=${storeId}`
    });
  },

  callPhone() {
    const { store } = this.data;
    if (store && store.phone) {
      wx.makePhoneCall({ phoneNumber: store.phone });
    }
  }
});
