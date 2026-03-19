// pages/message/chat.js
const { PrivateMsgAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    targetId: null,
    messages: [],
    inputContent: '',
    scrollToView: '',
    isLoading: false
  },

  onLoad(options) {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    this.setData({ targetId: options.targetId })
    this.loadMessages()
  },

  onShow() {
    // 标记已读
    if (this.data.targetId && isLoggedIn()) {
      this.markAsRead()
    }
  },

  async loadMessages() {
    this.setData({ isLoading: true })

    try {
      const result = await PrivateMsgAPI.getMessagesWithUser(this.data.targetId, 1, 50)
      const messages = result.list || []

      this.setData({
        messages,
        isLoading: false
      })

      // 滚动到底部
      if (messages.length > 0) {
        this.scrollToBottom()
      }

      // 标记已读
      this.markAsRead()
    } catch (error) {
      console.error('加载消息失败:', error)
      this.setData({ isLoading: false })
    }
  },

  async markAsRead() {
    try {
      // 获取会话ID需要从消息列表中获取
      if (this.data.messages.length > 0) {
        const conversationId = this.data.messages[0].conversationId
        if (conversationId) {
          await PrivateMsgAPI.markAsRead(conversationId)
        }
      }
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  },

  onInput(e) {
    this.setData({ inputContent: e.detail.value })
  },

  async sendMessage() {
    const { inputContent, targetId, messages } = this.data
    if (!inputContent.trim()) return

    const content = inputContent.trim()
    this.setData({ inputContent: '' })

    // 添加临时消息
    const tempMsg = {
      id: 'temp-' + Date.now(),
      senderId: wx.getStorageSync('userId'),
      content,
      isSelf: true,
      createTime: this.formatTime(new Date())
    }
    this.setData({
      messages: [...messages, tempMsg]
    })
    this.scrollToBottom()

    try {
      await PrivateMsgAPI.sendMessage(targetId, content)
      // 发送成功后刷新消息列表
      this.loadMessages()
    } catch (error) {
      console.error('发送消息失败:', error)
      wx.showToast({ title: '发送失败', icon: 'none' })
      // 移除临时消息
      const newMessages = this.data.messages.filter(m => m.id !== tempMsg.id)
      this.setData({ messages: newMessages, inputContent: content })
    }
  },

  scrollToBottom() {
    this.setData({
      scrollToView: `msg-${this.data.messages.length - 1}`
    })
  },

  formatTime(date) {
    const h = date.getHours().toString().padStart(2, '0')
    const m = date.getMinutes().toString().padStart(2, '0')
    return `${h}:${m}`
  }
})
