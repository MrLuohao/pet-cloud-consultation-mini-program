// pages/pet/timeline/index.js - 宠物时间轴
const app = getApp();
const { PetAPI } = require('../../../utils/api');

Page({
  data: {
    pet: null,
    timeline: [],
    report: null,
    loading: true,
    currentYear: new Date().getFullYear(),
    currentMonth: new Date().getMonth() + 1
  },

  onLoad(options) {
    this.petId = options.petId;
    this.petName = decodeURIComponent(options.petName || '');
    wx.setNavigationBarTitle({ title: this.petName + '的成长档案' });
    this.loadData();
  },

  async loadData() {
    const { currentYear, currentMonth } = this.data;
    try {
      const [timeline, report] = await Promise.allSettled([
        PetAPI.getTimeline(this.petId),
        PetAPI.getMonthlyReport(this.petId, currentYear, currentMonth)
      ]);
      this.setData({
        timeline: timeline.status === 'fulfilled' ? (timeline.value || []) : [],
        report: report.status === 'fulfilled' ? report.value : null,
        loading: false
      });
    } catch (error) {
      console.error('加载时间轴失败:', error);
      this.setData({ loading: false });
    }
  },

  onPrevMonth() {
    let { currentYear, currentMonth } = this.data;
    currentMonth--;
    if (currentMonth < 1) {
      currentMonth = 12;
      currentYear--;
    }
    this.setData({ currentYear, currentMonth });
    this.loadReport(currentYear, currentMonth);
  },

  onNextMonth() {
    let { currentYear, currentMonth } = this.data;
    const now = new Date();
    if (currentYear === now.getFullYear() && currentMonth >= now.getMonth() + 1) return;
    currentMonth++;
    if (currentMonth > 12) {
      currentMonth = 1;
      currentYear++;
    }
    this.setData({ currentYear, currentMonth });
    this.loadReport(currentYear, currentMonth);
  },

  async loadReport(year, month) {
    try {
      const report = await PetAPI.getMonthlyReport(this.petId, year, month);
      this.setData({ report });
    } catch (error) {
      this.setData({ report: null });
    }
  }
});
