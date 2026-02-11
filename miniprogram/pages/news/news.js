/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  消息中心逻辑 | Message Center Logic                                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const app = getApp();
const { MessageAPI, ConversationAPI } = require('../../utils/api');

// 轮询间隔（5分钟）
const POLLING_INTERVAL = 300000;

Page({
  data: {
    // 消息中心数据
    totalUnreadCount: 0,
    conversations: [],
    recentNotifications: [],
    aiUnreadCount: 0,
    consultationUnreadCount: 0,
    notificationCounts: {
      orderCount: 0,
      activityCount: 0,
      systemCount: 0,
      interactionCount: 0,
      totalCount: 0
    },

    // UI状态
    isRefreshing: false,
    showActionSheet: false,
    selectedConversation: null,

    // 颜色映射
    colorMap: {
      system: 'purple',
      consult: 'blue',
      order: 'green',
      activity: 'orange',
      interaction: 'blue',
      article: 'pink'
    },

    // 图标映射
    iconMap: {
      system: '🔔',
      consult: '💬',
      order: '📦',
      activity: '🎁',
      article: '📰',
      interaction: '💬'
    }
  },

  // 轮询定时器
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

  // ==================== 数据加载 ====================

  async loadMessageCenter() {
    // 未登录时显示空状态
    if (!app.globalData.token) {
      this.setData({
        conversations: [],
        recentNotifications: [],
        totalUnreadCount: 0,
        aiUnreadCount: 0,
        consultationUnreadCount: 0
      });
      return;
    }

    try {
      const centerData = await ConversationAPI.getCenter();

      // 处理会话列表
      const conversations = (centerData.recentConversations || []).map(conv => ({
        ...conv,
        lastMessage: this.truncateMessage(conv.lastMessage)
      }));

      // 处理通知列表
      const recentNotifications = (centerData.recentNotifications || []).map(msg => ({
        ...msg,
        icon: this.data.iconMap[msg.type] || '📄',
        color: this.data.colorMap[msg.type] || 'purple'
      }));

      this.setData({
        totalUnreadCount: centerData.totalUnreadCount || 0,
        conversations,
        recentNotifications,
        aiUnreadCount: centerData.aiUnreadCount || 0,
        consultationUnreadCount: centerData.consultationUnreadCount || 0,
        notificationCounts: centerData.notificationCounts || this.data.notificationCounts
      });

    } catch (error) {
      console.error('加载消息中心失败:', error);
      this.setData({
        conversations: [],
        recentNotifications: [],
        totalUnreadCount: 0
      });
    }
  },

  truncateMessage(message) {
    if (!message) return '';
    return message.length > 30 ? message.substring(0, 30) + '...' : message;
  },

  // ==================== 轮询机制 ====================

  startPolling() {
    // 先清除可能存在的旧定时器
    this.stopPolling();

    // 只在登录状态下轮询
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

  // ==================== 下拉刷新 ====================

  async onRefresh() {
    this.setData({ isRefreshing: true });
    await this.loadMessageCenter();
    this.setData({ isRefreshing: false });
  },

  // ==================== 快捷入口跳转 ====================

  async goToAiChat() {
    try {
      // 获取或创建AI会话
      const conversation = await ConversationAPI.getOrCreateAi();
      wx.navigateTo({
        url: `/pages/chat/chat?conversationId=${conversation.id}`
      });
    } catch (error) {
      console.error('获取AI会话失败:', error);
      // 降级处理：直接跳转到聊天页面
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

  // ==================== 会话操作 ====================

  async onConversationTap(e) {
    const conversation = e.currentTarget.dataset.conversation;

    // 根据会话类型跳转
    if (conversation.type === 'ai_chat') {
      // AI会话：先获取或创建真实会话，再跳转
      try {
        const realConversation = await ConversationAPI.getOrCreateAi();
        wx.navigateTo({
          url: `/pages/chat/chat?conversationId=${realConversation.id}`
        });
      } catch (error) {
        console.error('获取AI会话失败:', error);
        // 降级处理：直接跳转到聊天页面（无会话ID）
        wx.navigateTo({
          url: '/pages/chat/chat'
        });
      }
      return;
    }

    // 标记已读
    if (conversation.unreadCount > 0 && app.globalData.token) {
      try {
        await ConversationAPI.markAsRead(conversation.id);
        // 更新本地状态
        const conversations = this.data.conversations.map(c => {
          if (c.id === conversation.id) {
            return { ...c, unreadCount: 0 };
          }
          return c;
        });
        this.setData({ conversations });
      } catch (error) {
        console.error('标记已读失败:', error);
      }
    }

    // 其他会话类型跳转
    if (conversation.type === 'doctor_consultation') {
      wx.navigateTo({
        url: `/pages/consultation/chat?id=${conversation.targetId}`
      });
    }
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
      // 更新本地状态
      const conversations = this.data.conversations.map(c => {
        if (c.id === conversation.id) {
          return { ...c, unreadCount: 0 };
        }
        return c;
      });
      this.setData({ conversations });
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
      const newPinned = !conversation.isPinned;
      await ConversationAPI.togglePin(conversation.id, newPinned);

      // 更新本地状态并重新排序
      let conversations = this.data.conversations.map(c => {
        if (c.id === conversation.id) {
          return { ...c, isPinned: newPinned };
        }
        return c;
      });

      // 按置顶和时间排序
      conversations.sort((a, b) => {
        if (a.isPinned !== b.isPinned) {
          return b.isPinned ? 1 : -1;
        }
        return 0;
      });

      this.setData({ conversations });
      wx.showToast({
        title: newPinned ? '已置顶' : '已取消置顶',
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

      // 从列表中移除
      const conversations = this.data.conversations.filter(c => c.id !== conversation.id);
      this.setData({ conversations });
      wx.showToast({ title: '已删除', icon: 'success' });
    } catch (error) {
      console.error('删除会话失败:', error);
      wx.showToast({ title: '删除失败', icon: 'none' });
    }

    this.hideActionSheet();
  },

  // ==================== 通知操作 ====================

  async onNotificationTap(e) {
    const notification = e.currentTarget.dataset.notification;

    // 标记已读
    if (notification.isRead === 0 && app.globalData.token) {
      try {
        await MessageAPI.markAsRead(notification.id);
        // 更新本地状态
        const recentNotifications = this.data.recentNotifications.map(n => {
          if (n.id === notification.id) {
            return { ...n, isRead: 1 };
          }
          return n;
        });
        this.setData({ recentNotifications });
      } catch (error) {
        console.error('标记已读失败:', error);
      }
    }

    // 根据通知类型跳转
    if (notification.type === 'order') {
      wx.switchTab({
        url: '/pages/user/user'
      });
    } else if (notification.type === 'activity') {
      wx.switchTab({
        url: '/pages/index/index'
      });
    } else {
      wx.showToast({
        title: notification.title,
        icon: 'none'
      });
    }
  },

  // ==================== 全部已读 ====================

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

      // 更新本地状态
      const conversations = this.data.conversations.map(c => ({ ...c, unreadCount: 0 }));
      const recentNotifications = this.data.recentNotifications.map(n => ({ ...n, isRead: 1 }));

      this.setData({
        conversations,
        recentNotifications,
        totalUnreadCount: 0,
        aiUnreadCount: 0,
        consultationUnreadCount: 0,
        notificationCounts: {
          orderCount: 0,
          activityCount: 0,
          systemCount: 0,
          interactionCount: 0,
          totalCount: 0
        }
      });

      wx.showToast({
        title: '已全部标记为已读',
        icon: 'success'
      });
    } catch (error) {
      console.error('标记已读失败:', error);
      wx.showToast({
        title: '操作失败',
        icon: 'none'
      });
    }
  }
});
