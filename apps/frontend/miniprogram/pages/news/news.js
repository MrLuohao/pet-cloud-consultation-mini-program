const app = getApp();
const { MessageAPI, ConversationAPI } = require('../../utils/api');

const POLLING_INTERVAL = 300000;

const EMPTY_NOTIFICATION_COUNTS = {
  orderCount: 0,
  activityCount: 0,
  systemCount: 0,
  interactionCount: 0,
  totalCount: 0
};

Page({
  data: {
    totalUnreadCount: 0,
    unreadSummary: {
      totalUnreadCount: 0,
      conversationUnreadCount: 0,
      notificationUnreadCount: 0,
      aiUnreadCount: 0,
      consultationUnreadCount: 0,
      customerServiceUnreadCount: 0
    },
    quickEntries: [],
    conversations: [],
    topConversations: [],
    systemNotifications: [],
    eventSlots: [],
    notificationCounts: EMPTY_NOTIFICATION_COUNTS,
    isRefreshing: false,
    showActionSheet: false,
    selectedConversation: null
  },

  _pollingTimer: null,

  onLoad() {
    this.loadMessageCenter();
  },

  onShow() {
    this.loadMessageCenter();
    this.startPolling();
  },

  onHide() {
    this.stopPolling();
  },

  onUnload() {
    this.stopPolling();
  },

  async loadMessageCenter() {
    if (!app.globalData.token) {
      this.resetPageData();
      return;
    }

    try {
      const centerData = await ConversationAPI.getCenter();
      const conversations = (centerData.recentConversations || []).map(item => ({
        ...item,
        lastMessage: this.truncateText(item.lastMessage, 34)
      }));
      const quickEntries = (centerData.quickEntries || []).map(item => ({
        ...item,
        iconSrc: this.getQuickEntryIcon(item.iconKey),
        badgeText: item.unreadCount > 99 ? '99+' : item.unreadCount,
        subtitle: item.subtitle || '查看最新消息'
      }));
      const systemNotifications = (centerData.systemNotifications || centerData.recentNotifications || []).map(item => ({
        ...item,
        preview: this.truncateText(item.content || item.title, 38)
      }));
      const eventSlots = (centerData.eventSlots || []).map(item => ({
        ...item,
        badgeText: item.unreadCount > 99 ? '99+' : item.unreadCount
      }));

      this.setData({
        totalUnreadCount: centerData.totalUnreadCount || 0,
        unreadSummary: centerData.unreadSummary || this.data.unreadSummary,
        quickEntries,
        conversations,
        topConversations: conversations.slice(0, 2),
        systemNotifications,
        eventSlots,
        notificationCounts: centerData.notificationCounts || EMPTY_NOTIFICATION_COUNTS
      });
    } catch (error) {
      console.error('加载消息中心失败:', error);
      this.resetPageData();
    }
  },

  resetPageData() {
    this.setData({
      totalUnreadCount: 0,
      unreadSummary: {
        totalUnreadCount: 0,
        conversationUnreadCount: 0,
        notificationUnreadCount: 0,
        aiUnreadCount: 0,
        consultationUnreadCount: 0,
        customerServiceUnreadCount: 0
      },
      quickEntries: [],
      conversations: [],
      topConversations: [],
      systemNotifications: [],
      eventSlots: [],
      notificationCounts: EMPTY_NOTIFICATION_COUNTS
    });
  },

  truncateText(text, limit) {
    if (!text) return '';
    return text.length > limit ? `${text.slice(0, limit)}...` : text;
  },

  getQuickEntryIcon(iconKey) {
    const iconMap = {
      'msg-ai': '/image/icons/msg-ai.svg',
      'msg-consult': '/image/icons/msg-consult.svg',
      'msg-service': '/image/icons/msg-service.svg'
    };
    return iconMap[iconKey] || '/image/icons/msg-ai.svg';
  },

  startPolling() {
    this.stopPolling();
    if (app.globalData.token) {
      this._pollingTimer = setInterval(() => {
        this.loadMessageCenter();
      }, POLLING_INTERVAL);
    }
  },

  stopPolling() {
    if (this._pollingTimer) {
      clearInterval(this._pollingTimer);
      this._pollingTimer = null;
    }
  },

  async onRefresh() {
    this.setData({ isRefreshing: true });
    await this.loadMessageCenter();
    this.setData({ isRefreshing: false });
  },

  onQuickEntryTap(e) {
    const entry = e.currentTarget.dataset.entry || {};
    if (entry.key === 'ai_assistant') {
      this.goToAiChat();
      return;
    }
    if (entry.key === 'consultation') {
      this.goToConsultation();
      return;
    }
    if (entry.key === 'customer_service') {
      this.goToCustomerService();
      return;
    }
    if (entry.navigateUrl) {
      wx.navigateTo({ url: entry.navigateUrl });
    }
  },

  async goToAiChat() {
    try {
      const conversation = await ConversationAPI.getOrCreateAi();
      wx.navigateTo({
        url: `/pages/chat/chat?conversationId=${conversation.id}`
      });
    } catch (error) {
      console.error('获取AI会话失败:', error);
      wx.navigateTo({
        url: '/pages/chat/chat'
      });
    }
  },

  goToCustomerService() {
    wx.navigateTo({
      url: '/pages/consultation/doctor-list'
    });
  },

  goToConsultation() {
    wx.navigateTo({
      url: '/pages/consultation/list'
    });
  },

  goToConversationList() {
    wx.navigateTo({
      url: '/pages/message/conversations'
    });
  },

  goToNotifications() {
    wx.navigateTo({
      url: '/pages/news/all-messages/index'
    });
  },

  goToNotificationType(e) {
    const type = e.currentTarget.dataset.type;
    wx.navigateTo({
      url: `/pages/news/all-messages/index?type=${type}`
    });
  },

  goToIndex() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  async onConversationTap(e) {
    const conversation = e.currentTarget.dataset.conversation;
    if (!conversation) {
      return;
    }

    if (conversation.type === 'ai_chat') {
      await this.goToAiChat();
      return;
    }

    if (conversation.unreadCount > 0 && app.globalData.token) {
      try {
        await ConversationAPI.markAsRead(conversation.id);
      } catch (error) {
        console.error('标记会话已读失败:', error);
      }
    }

    if (conversation.type === 'doctor_consultation') {
      wx.navigateTo({
        url: `/pages/consultation/chat?id=${conversation.targetId}`
      });
      return;
    }

    wx.navigateTo({
      url: conversation.navigateUrl || `/pages/chat/chat?conversationId=${conversation.id}`
    });
  },

  onConversationLongPress(e) {
    const conversation = e.currentTarget.dataset.conversation;
    this.setData({
      showActionSheet: true,
      selectedConversation: conversation
    });
  },

  hideActionSheet() {
    this.setData({
      showActionSheet: false,
      selectedConversation: null
    });
  },

  async onActionMarkRead() {
    const conversation = this.data.selectedConversation;
    if (!conversation || !app.globalData.token) {
      this.hideActionSheet();
      return;
    }

    try {
      await ConversationAPI.markAsRead(conversation.id);
      await this.loadMessageCenter();
      wx.showToast({ title: '已标为已读', icon: 'success' });
    } catch (error) {
      console.error('标记已读失败:', error);
      wx.showToast({ title: '操作失败', icon: 'none' });
    }

    this.hideActionSheet();
  },

  async onActionTogglePin() {
    const conversation = this.data.selectedConversation;
    if (!conversation || !app.globalData.token) {
      this.hideActionSheet();
      return;
    }

    try {
      await ConversationAPI.togglePin(conversation.id, !conversation.isPinned);
      await this.loadMessageCenter();
      wx.showToast({
        title: conversation.isPinned ? '已取消置顶' : '已置顶',
        icon: 'success'
      });
    } catch (error) {
      console.error('置顶操作失败:', error);
      wx.showToast({ title: '操作失败', icon: 'none' });
    }

    this.hideActionSheet();
  },

  async onActionDelete() {
    const conversation = this.data.selectedConversation;
    if (!conversation || !app.globalData.token) {
      this.hideActionSheet();
      return;
    }

    try {
      await ConversationAPI.delete(conversation.id);
      await this.loadMessageCenter();
      wx.showToast({ title: '已删除', icon: 'success' });
    } catch (error) {
      console.error('删除会话失败:', error);
      wx.showToast({ title: '删除失败', icon: 'none' });
    }

    this.hideActionSheet();
  },

  async onNotificationTap(e) {
    const notification = e.currentTarget.dataset.notification;
    if (!notification) {
      return;
    }

    if (notification.isRead === 0 && app.globalData.token) {
      try {
        await MessageAPI.markAsRead(notification.id);
        await this.loadMessageCenter();
      } catch (error) {
        console.error('标记通知已读失败:', error);
      }
    }

    if (notification.type === 'order') {
      wx.switchTab({ url: '/pages/user/user' });
      return;
    }
    if (notification.type === 'activity') {
      wx.switchTab({ url: '/pages/index/index' });
      return;
    }
    wx.showToast({
      title: notification.title || '系统通知',
      icon: 'none'
    });
  },

  async onReadAll() {
    if (!app.globalData.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }

    try {
      await MessageAPI.markAllAsRead();
      const unreadConversations = this.data.conversations.filter(item => item.unreadCount > 0);
      await Promise.all(unreadConversations.map(item => ConversationAPI.markAsRead(item.id)));
      await this.loadMessageCenter();
      wx.showToast({
        title: '已全部标记为已读',
        icon: 'success'
      });
    } catch (error) {
      console.error('全部已读失败:', error);
      wx.showToast({
        title: '操作失败',
        icon: 'none'
      });
    }
  }
});
