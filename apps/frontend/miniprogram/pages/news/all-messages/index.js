// pages/news/all-messages/index.js - 全部消息列表
const { MessageAPI } = require('../../../utils/api');

Page({
  data: {
    activeType: null,
    types: [
      { key: null, label: '全部' },
      { key: 'system', label: '系统', icon: '🔔', color: 'purple' },
      { key: 'order', label: '订单', icon: '📦', color: 'green' },
      { key: 'activity', label: '活动', icon: '🎁', color: 'orange' },
      { key: 'interaction', label: '互动', icon: '💬', color: 'blue' }
    ],
    messages: [],
    loading: false,
    iconMap: {
      system: '🔔',
      order: '📦',
      activity: '🎁',
      interaction: '💬',
      article: '📰',
      consult: '💬'
    },
    colorMap: {
      system: 'purple',
      order: 'green',
      activity: 'orange',
      interaction: 'blue',
      article: 'pink',
      consult: 'blue'
    }
  },

  onLoad(options) {
    if (options.type) {
      this.setData({ activeType: options.type });
    }
    this.loadMessages();
  },

  onShow() {
    this.loadMessages();
  },

  async loadMessages() {
    this.setData({ loading: true });
    try {
      const list = await MessageAPI.getList(this.data.activeType);
      const messages = (list || []).map(msg => ({
        ...msg,
        icon: this.data.iconMap[msg.type] || '📄',
        color: this.data.colorMap[msg.type] || 'purple'
      }));
      this.setData({ messages, loading: false });
    } catch (error) {
      console.error('加载消息失败:', error);
      this.setData({ messages: [], loading: false });
    }
  },

  onTypeChange(e) {
    const key = e.currentTarget.dataset.key;
    this.setData({ activeType: key === 'null' ? null : key });
    this.loadMessages();
  },

  async onMessageTap(e) {
    const msg = e.currentTarget.dataset.msg;
    if (msg.isRead === 0) {
      try {
        await MessageAPI.markAsRead(msg.id);
        const messages = this.data.messages.map(m =>
          m.id === msg.id ? { ...m, isRead: 1 } : m
        );
        this.setData({ messages });
      } catch (err) {
        console.error('标记已读失败:', err);
      }
    }
  },

  onBack() {
    wx.navigateBack();
  }
});
