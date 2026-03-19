// pages/beauty/booking/index.js - 预约页面
const { BeautyAPI, PetAPI, isLoggedIn, navigateToLogin } = require('../../../utils/api');

Page({
  data: {
    storeId: null,
    store: null,
    services: [],
    pets: [],
    availableSlots: [],

    selectedServiceIds: [],
    selectedPetId: null,
    selectedDate: '',
    selectedSlot: '',
    remark: '',

    submitting: false,
    minDate: '',
    maxDate: ''
  },

  onLoad(options) {
    if (!isLoggedIn()) {
      navigateToLogin();
      return;
    }
    if (options.storeId) {
      this.setData({ storeId: options.storeId });
      this.initDate();
      this.loadStore(options.storeId);
      this.loadServices(options.storeId);
      this.loadPets();
    }
  },

  initDate() {
    const today = new Date();
    const maxDay = new Date();
    maxDay.setDate(maxDay.getDate() + 30);
    const fmt = d => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
    this.setData({
      selectedDate: fmt(today),
      minDate: fmt(today),
      maxDate: fmt(maxDay)
    });
    this.loadSlots(this.data.storeId, fmt(today));
  },

  async loadStore(id) {
    try {
      const store = await BeautyAPI.getStoreDetail(id);
      this.setData({ store });
    } catch (e) {
      console.error('加载门店信息失败:', e);
    }
  },

  async loadServices(storeId) {
    try {
      const services = await BeautyAPI.getStoreServices(storeId);
      this.setData({ services: services || [] });
    } catch (e) {
      this.setData({ services: [] });
    }
  },

  async loadPets() {
    try {
      const pets = await PetAPI.getList();
      this.setData({ pets: pets || [] });
      if (pets && pets.length > 0) {
        this.setData({ selectedPetId: pets[0].id });
      }
    } catch (e) {
      this.setData({ pets: [] });
    }
  },

  async loadSlots(storeId, date) {
    try {
      const slots = await BeautyAPI.getAvailableSlots(storeId, date);
      this.setData({ availableSlots: slots || [], selectedSlot: '' });
    } catch (e) {
      this.setData({ availableSlots: [] });
    }
  },

  onDateChange(e) {
    const date = e.detail.value;
    this.setData({ selectedDate: date });
    this.loadSlots(this.data.storeId, date);
  },

  onSlotTap(e) {
    const slot = e.currentTarget.dataset.slot;
    if (!slot.available) return;
    this.setData({ selectedSlot: slot.slot });
  },

  toggleService(e) {
    const id = e.currentTarget.dataset.id;
    let ids = [...this.data.selectedServiceIds];
    const idx = ids.indexOf(id);
    if (idx >= 0) {
      ids.splice(idx, 1);
    } else {
      ids.push(id);
    }
    this.setData({ selectedServiceIds: ids });
  },

  onPetChange(e) {
    const idx = e.detail.value;
    this.setData({ selectedPetId: this.data.pets[idx].id });
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  async submitBooking() {
    const { storeId, selectedPetId, selectedDate, selectedSlot, selectedServiceIds, remark, submitting } = this.data;
    if (submitting) return;

    if (!selectedPetId) {
      wx.showToast({ title: '请选择宠物', icon: 'none' });
      return;
    }
    if (!selectedSlot) {
      wx.showToast({ title: '请选择预约时间段', icon: 'none' });
      return;
    }
    if (selectedServiceIds.length === 0) {
      wx.showToast({ title: '请选择至少一个服务项目', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });
    try {
      await BeautyAPI.createBooking({
        storeId: Number(storeId),
        petId: selectedPetId,
        bookingDate: selectedDate,
        bookingTime: selectedSlot,
        services: selectedServiceIds.join(','),
        remark
      });
      wx.hideLoading();
      wx.showToast({ title: '预约成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (error) {
      wx.hideLoading();
      console.error('预约失败:', error);
      this.setData({ submitting: false });
    }
  },

  onBack() {
    wx.navigateBack();
  }
});
