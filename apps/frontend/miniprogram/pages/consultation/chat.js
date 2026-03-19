// pages/consultation/chat.js
const { ConsultationAPI, AIAPI } = require('../../utils/api')

Page({
  data: {
    consultationId: null,
    consultation: null,
    messages: [],
    inputValue: '',
    toView: ''
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ consultationId: options.id })
      this.loadConsultationDetail()
    }
  },

  onShow() {
    if (this.data.consultationId) {
      this.loadMessages()
    }
  },

  // 加载咨询详情
  async loadConsultationDetail() {
    try {
      const consultation = await ConsultationAPI.getDetail(this.data.consultationId)
      this.setData({ consultation })
    } catch (error) {
      console.error('加载咨询详情失败:', error)
    }
  },

  // 加载消息
  async loadMessages() {
    try {
      const messages = await ConsultationAPI.getMessages(this.data.consultationId)
      this.setData({ messages })
      this.scrollToBottom()
    } catch (error) {
      console.error('加载消息失败:', error)
    }
  },

  // 输入变化
  onInput(e) {
    this.setData({ inputValue: e.detail.value })
  },

  // 发送消息
  async sendMessage() {
    const content = this.data.inputValue.trim()
    if (!content) {
      wx.showToast({ title: '请输入消息', icon: 'none' })
      return
    }

    try {
      await ConsultationAPI.sendMessage(
        this.data.consultationId,
        1,
        content,
        null
      )
      this.setData({ inputValue: '' })
      this.loadMessages()
    } catch (error) {
      wx.showToast({ title: '发送失败', icon: 'none' })
    }
  },

  // 选择图片
  chooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        try {
          wx.showLoading({ title: '上传中...' })
          const uploadedUrl = await AIAPI.uploadImage(res.tempFilePaths[0])
          await ConsultationAPI.sendMessage(
            this.data.consultationId,
            2,
            null,
            uploadedUrl
          )
          wx.hideLoading()
          this.loadMessages()
        } catch (error) {
          wx.hideLoading()
          wx.showToast({ title: '发送失败', icon: 'none' })
        }
      }
    })
  },

  // 滚动到底部
  scrollToBottom() {
    if (this.data.messages.length > 0) {
      const lastMsg = this.data.messages[this.data.messages.length - 1]
      this.setData({ toView: `msg-${lastMsg.id}` })
    }
  },

  // 取消咨询
  cancelConsultation() {
    wx.showModal({
      title: '提示',
      content: '确定要取消咨询吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await ConsultationAPI.cancel(this.data.consultationId)
            wx.showToast({ title: '已取消', icon: 'success' })
            setTimeout(() => {
              wx.navigateBack()
            }, 1500)
          } catch (error) {
            wx.showToast({ title: '操作失败', icon: 'none' })
          }
        }
      }
    })
  },

  // 结束咨询
  finishConsultation() {
    wx.showModal({
      title: '提示',
      content: '确定要结束咨询吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await ConsultationAPI.finish(this.data.consultationId)
            wx.showToast({ title: '已结束', icon: 'success' })
            this.loadConsultationDetail()
          } catch (error) {
            wx.showToast({ title: '操作失败', icon: 'none' })
          }
        }
      }
    })
  }
})
