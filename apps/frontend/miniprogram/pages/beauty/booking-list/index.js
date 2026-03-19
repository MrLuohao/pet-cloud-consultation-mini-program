// pages/beauty/booking-list/index.js - 我的预约列表
const { BeautyAPI, isLoggedIn, navigateToLogin } = require('../../../utils/api');

Page({
  data: {
    activeStatus: null,
    statusTabs: [
      { key: null, label: '全部' },
      { key: 0, label: '待确认' },
      { key: 1, label: '已确认' },
      { key: 2, label: '已完成' },
      { key: 3, label: '已取消' }
    ],
    bookings: [],
    loading: false,
    statusColorMap: {
      0: 'orange',
      1: 'blue',
      2: 'green',
      3: 'gray'
    }
  },

  onLoad() {
    if (!isLoggedIn()) {
      navigateToLogin();
      return;
    }
    this.loadBookings();
  },

  onShow() {
    if (isLoggedIn()) {
      this.loadBookings();
    }
  },

  async loadBookings() {
    this.setData({ loading: true });
    try {
      const bookings = await BeautyAPI.getBookingList(this.data.activeStatus);
      this.setData({
        bookings: (bookings || []).map(b => ({
          ...b,
          statusColor: this.data.statusColorMap[b.status] || 'gray'
        })),
        loading: false
      });
    } catch (error) {
      console.error('加载预约列表失败:', error);
      this.setData({ bookings: [], loading: false });
    }
  },

  onStatusChange(e) {
    const key = e.currentTarget.dataset.key;
    this.setData({ activeStatus: key === 'null' ? null : Number(key) });
    this.loadBookings();
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/beauty/booking-detail/index?id=${id}`
    });
  },

  async cancelBooking(e) {
    const id = e.currentTarget.dataset.id;
    const res = await new Promise(resolve => wx.showModal({
      title: '确认取消',
      content: '确定要取消这个预约吗？',
      success: resolve
    }));
    if (!res.confirm) return;

    try {
      await BeautyAPI.cancelBooking(id);
      wx.showToast({ title: '已取消预约', icon: 'success' });
      this.loadBookings();
    } catch (error) {
      console.error('取消预约失败:', error);
    }
  },

  onBack() {
    wx.navigateBack();
  }
});
