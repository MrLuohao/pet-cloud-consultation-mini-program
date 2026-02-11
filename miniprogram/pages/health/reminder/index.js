// pages/health/reminder/index.js - 健康提醒页
const app = getApp();
const { HealthAPI, PetAPI, parseDate } = require('../../../utils/api');

Page({
  data: {
    reminders: [],
    pets: [],
    loading: true,
    showAddModal: false,
    form: {
      petId: '',
      petName: '',
      reminderType: 'vaccine',
      title: '',
      remindDate: '',
      note: ''
    },
    reminderTypes: [
      { value: 'vaccine', label: '疫苗接种', icon: '💉' },
      { value: 'checkup', label: '定期检查', icon: '🏥' },
      { value: 'medicine', label: '喂药', icon: '💊' },
      { value: 'deworming', label: '驱虫', icon: '🐛' },
      { value: 'other', label: '其他', icon: '📋' }
    ]
  },

  onLoad() {
    if (!app.globalData.token) {
      wx.redirectTo({ url: '/pages/login/login' });
      return;
    }
    this.loadData();
  },

  onShow() {
    if (app.globalData.token) {
      this.loadReminders();
    }
  },

  async loadData() {
    await Promise.all([this.loadReminders(), this.loadPets()]);
    this.setData({ loading: false });
  },

  async loadReminders() {
    try {
      const reminders = await HealthAPI.getReminders();
      // 计算是否已过期
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const enriched = (reminders || []).map(r => ({
        ...r,
        typeInfo: this.getTypeInfo(r.reminderType),
        isOverdue: !r.isDone && parseDate(r.remindDate) < today,
        isToday: !r.isDone && this.isToday(r.remindDate)
      }));
      this.setData({ reminders: enriched });
    } catch (error) {
      console.error('加载提醒失败:', error);
      this.setData({ reminders: [] });
    }
  },

  async loadPets() {
    try {
      const pets = await PetAPI.getList();
      this.setData({ pets: pets || [] });
      if (pets && pets.length > 0) {
        this.setData({
          'form.petId': pets[0].id,
          'form.petName': pets[0].name
        });
      }
    } catch (error) {
      this.setData({ pets: [] });
    }
  },

  isToday(dateStr) {
    if (!dateStr) return false;
    const today = new Date();
    const d = parseDate(dateStr);
    return d.getFullYear() === today.getFullYear() &&
           d.getMonth() === today.getMonth() &&
           d.getDate() === today.getDate();
  },

  getTypeInfo(type) {
    const map = {
      vaccine: { label: '疫苗接种', icon: '💉', color: 'blue' },
      checkup: { label: '定期检查', icon: '🏥', color: 'green' },
      medicine: { label: '喂药', icon: '💊', color: 'orange' },
      deworming: { label: '驱虫', icon: '🐛', color: 'purple' },
      other: { label: '其他', icon: '📋', color: 'gray' }
    };
    return map[type] || map.other;
  },

  onShowAdd() {
    const today = new Date();
    const dateStr = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`;
    this.setData({ showAddModal: true, 'form.remindDate': dateStr });
  },

  onHideAdd() {
    this.setData({ showAddModal: false });
  },

  onPetChange(e) {
    const pet = this.data.pets[e.detail.value];
    if (pet) {
      this.setData({ 'form.petId': pet.id, 'form.petName': pet.name });
    }
  },

  onTypeChange(e) {
    const type = this.data.reminderTypes[e.detail.value];
    if (type) {
      this.setData({ 'form.reminderType': type.value });
    }
  },

  onTitleInput(e) { this.setData({ 'form.title': e.detail.value }); },
  onDateChange(e) { this.setData({ 'form.remindDate': e.detail.value }); },
  onNoteInput(e) { this.setData({ 'form.note': e.detail.value }); },

  async onAddReminder() {
    const { form } = this.data;
    if (!form.petId) {
      wx.showToast({ title: '请选择宠物', icon: 'none' });
      return;
    }
    if (!form.title.trim()) {
      wx.showToast({ title: '请输入提醒标题', icon: 'none' });
      return;
    }
    if (!form.remindDate) {
      wx.showToast({ title: '请选择提醒日期', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '添加中...' });
    try {
      await HealthAPI.createReminder({
        petId: form.petId,
        petName: form.petName,
        reminderType: form.reminderType,
        title: form.title,
        remindDate: form.remindDate,
        note: form.note
      });
      wx.hideLoading();
      wx.showToast({ title: '添加成功', icon: 'success' });
      this.setData({ showAddModal: false, form: { ...form, title: '', note: '' } });
      this.loadReminders();
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: '添加失败', icon: 'none' });
    }
  },

  async onMarkDone(e) {
    const { id } = e.currentTarget.dataset;
    try {
      await HealthAPI.markReminderDone(id);
      wx.showToast({ title: '已完成', icon: 'success' });
      this.loadReminders();
    } catch (error) {
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  async onDelete(e) {
    const { id } = e.currentTarget.dataset;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条提醒吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await HealthAPI.deleteReminder(id);
            wx.showToast({ title: '已删除', icon: 'success' });
            this.loadReminders();
          } catch (error) {
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  }
});
