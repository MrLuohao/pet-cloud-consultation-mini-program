const { AuthAPI, MessageAPI, ConversationAPI, AIAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    categories: ['功能建议', '体验问题', 'Bug反馈', '其他'],
    categoryIndex: 0,
    contact: '',
    content: '',
    screenshots: [],
    recentMessages: [],
    submitting: false
  },

  onShow() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadInitialData()
  },

  async loadInitialData() {
    try {
      const [userRes, msgRes] = await Promise.allSettled([
        AuthAPI.getUserInfo(),
        MessageAPI.getList()
      ])

      if (userRes.status === 'fulfilled' && userRes.value) {
        const user = userRes.value
        this.setData({ contact: user.nickname || '' })
      }

      if (msgRes.status === 'fulfilled' && Array.isArray(msgRes.value)) {
        this.setData({ recentMessages: msgRes.value.slice(0, 3) })
      }
    } catch (error) {
      // 静默
    }
  },

  onCategoryChange(e) {
    this.setData({ categoryIndex: Number(e.detail.value) || 0 })
  },

  onContactInput(e) {
    this.setData({ contact: e.detail.value.trim() })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  chooseImage() {
    wx.chooseImage({
      count: 3 - this.data.screenshots.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const merged = this.data.screenshots.concat(res.tempFilePaths).slice(0, 3)
        this.setData({ screenshots: merged })
      }
    })
  },

  removeImage(e) {
    const index = Number(e.currentTarget.dataset.index)
    const screenshots = this.data.screenshots.filter((_, i) => i !== index)
    this.setData({ screenshots })
  },

  async submitFeedback() {
    if (this.data.submitting) return

    const content = this.data.content.trim()
    if (!content || content.length < 5) {
      wx.showToast({ title: '请至少输入5个字', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    wx.showLoading({ title: '提交中...' })

    try {
      const uploadedImages = []
      for (const localPath of this.data.screenshots) {
        const url = await AIAPI.uploadImage(localPath)
        if (url) uploadedImages.push(url)
      }

      const category = this.data.categories[this.data.categoryIndex]
      const payload = [
        '[用户反馈提交]',
        `类型: ${category}`,
        `联系方式: ${this.data.contact || '未填写'}`,
        `内容: ${content}`,
        `截图: ${uploadedImages.length > 0 ? uploadedImages.join(', ') : '无'}`
      ].join('\n')

      const conversation = await ConversationAPI.getOrCreateAi()
      const conversationId = conversation && conversation.id
      if (!conversationId) {
        throw new Error('创建反馈会话失败')
      }

      await AIAPI.sendWithConversation(conversationId, payload, 'qwen')

      wx.hideLoading()
      wx.showToast({ title: '提交成功', icon: 'success' })

      this.setData({ content: '', screenshots: [] })

      setTimeout(() => {
        wx.navigateTo({ url: `/pages/chat/chat?conversationId=${conversationId}` })
      }, 800)
    } catch (error) {
      wx.hideLoading()
      wx.showToast({ title: error.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
