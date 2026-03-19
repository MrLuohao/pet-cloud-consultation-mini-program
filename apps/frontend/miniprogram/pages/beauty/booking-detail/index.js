// pages/beauty/booking-detail/index.js - 美容预约详情与状态追踪页
const { BeautyAPI, isLoggedIn, navigateToLogin, parseDate } = require('../../../utils/api');

// 状态时间轴节点定义（全链路）
const STATUS_STEPS = [
  { key: 'booked',    label: '已预约',  icon: '📋', status: -1 },
  { key: 'confirmed', label: '已确认',  icon: '✅', status: 1 },
  { key: 'arrived',   label: '已到店',  icon: '🏪', status: 1 },
  { key: 'inService', label: '服务中',  icon: '✂️', status: 2 },
  { key: 'completed', label: '已完成',  icon: '🎉', status: 2 }
];

Page({
  data: {
    bookingId: null,
    booking: null,
    loading: true,

    // 状态时间轴
    timelineSteps: [],

    // 服务照片预览
    previewPhotos: [],
    showPreview: false,
    previewCurrent: 0,

    statusColorMap: {
      0: 'orange',
      1: 'blue',
      2: 'green',
      3: 'gray'
    }
  },

  onLoad(options) {
    if (!isLoggedIn()) {
      navigateToLogin();
      return;
    }
    if (options.id) {
      this.setData({ bookingId: options.id });
      this.loadBookingDetail(options.id);
    }
  },

  onShow() {
    if (this.data.bookingId && isLoggedIn()) {
      this.loadBookingDetail(this.data.bookingId);
    }
  },

  async loadBookingDetail(id) {
    this.setData({ loading: true });
    try {
      const booking = await BeautyAPI.getBookingDetail(id);
      const timeline = this.buildTimeline(booking);
      const servicePhotos = this.parseServicePhotos(booking.servicePhotos);

      this.setData({
        booking: {
          ...booking,
          statusColor: this.data.statusColorMap[booking.status] || 'gray',
          servicePhotoList: servicePhotos
        },
        timelineSteps: timeline,
        loading: false
      });
    } catch (error) {
      console.error('加载预约详情失败:', error);
      this.setData({ loading: false });
      wx.showToast({ title: '加载失败，请重试', icon: 'none' });
    }
  },

  // 构建状态时间轴
  buildTimeline(booking) {
    const now = new Date().toLocaleString('zh-CN', { hour12: false });
    const bookingTime = `${booking.bookingDate} ${booking.bookingTime}`;
    const createTimeStr = booking.createTime
      ? parseDate(booking.createTime).toLocaleString('zh-CN', { hour12: false })
      : now;

    const steps = [
      {
        key: 'booked',
        label: '预约成功',
        icon: '📋',
        done: true,
        time: createTimeStr,
        desc: `预约 ${booking.bookingDate} ${booking.bookingTime}`
      },
      {
        key: 'confirmed',
        label: '门店确认',
        icon: '✅',
        done: booking.status >= 1,
        time: booking.status >= 1 ? bookingTime : '',
        desc: booking.status >= 1 ? '门店已确认您的预约' : '等待门店确认'
      },
      {
        key: 'arrived',
        label: '宠物到店',
        icon: '🏪',
        done: booking.status >= 1,
        time: booking.status >= 1 ? bookingTime : '',
        desc: booking.status >= 1 ? '已到店，服务即将开始' : '待到店'
      },
      {
        key: 'inService',
        label: '美容进行中',
        icon: '✂️',
        done: booking.status === 2,
        active: booking.status === 1,
        time: booking.status >= 2 ? '服务中' : '',
        desc: booking.status === 2 ? '美容服务已完成' :
              booking.status === 1 ? '正在为您的宠物精心打理...' : '待服务'
      },
      {
        key: 'completed',
        label: '美容完成',
        icon: '🎉',
        done: booking.status === 2,
        time: booking.status === 2 ? '已完成' : '',
        desc: booking.status === 2 ? '美容服务圆满完成！' : '待完成'
      }
    ];

    // 取消状态特殊处理
    if (booking.status === 3) {
      return [
        { ...steps[0], done: true },
        { key: 'cancelled', label: '预约已取消', icon: '❌', done: true, time: '', desc: '该预约已被取消' }
      ];
    }

    return steps;
  },

  // 解析服务照片 JSON
  parseServicePhotos(servicePhotosStr) {
    if (!servicePhotosStr) return [];
    try {
      const parsed = JSON.parse(servicePhotosStr);
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      return [];
    }
  },

  // 点击预览服务过程照片
  onPreviewPhoto(e) {
    const { index } = e.currentTarget.dataset;
    const { booking } = this.data;
    const photos = booking.servicePhotoList || [];
    wx.previewImage({
      current: photos[index],
      urls: photos
    });
  },

  // 预览前后对比照片
  onPreviewContrast(e) {
    const { type } = e.currentTarget.dataset;
    const { booking } = this.data;
    const urls = [];
    if (booking.beforePhoto) urls.push(booking.beforePhoto);
    if (booking.afterPhoto) urls.push(booking.afterPhoto);
    wx.previewImage({
      current: type === 'before' ? booking.beforePhoto : booking.afterPhoto,
      urls
    });
  },

  // 分享美容成果
  onShareResult() {
    wx.showShareMenu({ withShareTicket: true });
    wx.showToast({ title: '长按图片可保存分享', icon: 'none' });
  },

  // 取消预约
  async onCancelBooking() {
    const res = await new Promise(resolve => wx.showModal({
      title: '确认取消',
      content: '确定要取消这个预约吗？取消后不可恢复。',
      confirmColor: '#ff3b30',
      success: resolve
    }));
    if (!res.confirm) return;

    wx.showLoading({ title: '取消中...' });
    try {
      await BeautyAPI.cancelBooking(this.data.bookingId);
      wx.hideLoading();
      wx.showToast({ title: '预约已取消', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: '取消失败，请重试', icon: 'none' });
    }
  },

  // 去评价
  onGoReview() {
    wx.showToast({ title: '评价功能即将上线', icon: 'none' });
  },

  onBack() {
    wx.navigateBack();
  },

  onShareAppMessage() {
    const { booking } = this.data;
    return {
      title: `${booking?.storeName || ''}的美容服务`,
      path: `/pages/beauty/booking-detail/index?id=${this.data.bookingId}`
    };
  }
});
