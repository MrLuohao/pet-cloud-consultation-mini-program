// pages/product/reviews.js
const { ProductAPI, parseDate } = require('../../utils/api')

Page({
  data: {
    productId: null,
    currentUserId: null,
    reviews: [],
    filterTabs: [
      { key: 'all', name: '全部' },
      { key: 'good', name: '好评' },
      { key: 'bad', name: '差评' },
      { key: 'withImages', name: '有图' }
    ],
    currentFilter: 'all',
    page: 1,
    size: 10,
    hasMore: true,
    loading: false,
    summary: {
      total: 0,
      goodCount: 0,
      badCount: 0,
      withImagesCount: 0,
      avgRating: 5.0
    },
    // 编辑评价相关
    showEditPopup: false,
    editingReview: null,
    editForm: {
      rating: 5,
      content: '',
      images: []
    },
    // 追评相关
    showFollowUpPopup: false,
    followUpReview: null,
    followUpContent: ''
  },

  onLoad(options) {
    if (options.productId) {
      this.setData({ productId: options.productId })
      // 获取当前用户ID（从全局或存储中）
      const app = getApp()
      let userId = app.globalData.userId
      if (!userId) {
        userId = wx.getStorageSync('userId')
      }
      if (userId) {
        this.setData({ currentUserId: userId })
      }
      this.loadReviews()
    }
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMore()
    }
  },

  onPullDownRefresh() {
    this.setData({ page: 1, reviews: [], hasMore: true })
    this.loadReviews().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  // 切换筛选Tab
  switchFilter(e) {
    const filter = e.currentTarget.dataset.filter
    if (filter !== this.data.currentFilter) {
      this.setData({
        currentFilter: filter,
        page: 1,
        reviews: [],
        hasMore: true
      })
      this.loadReviews()
    }
  },

  // 加载评价列表
  async loadReviews() {
    if (this.data.loading) return

    this.setData({ loading: true })
    wx.showLoading({ title: '加载中...' })

    try {
      const { productId, currentFilter, page, size } = this.data

      // 首次加载时获取统计数据
      if (page === 1) {
        try {
          const summary = await ProductAPI.getReviewSummary(productId)
          this.setData({ summary })
        } catch (e) {
          console.error('获取评价统计失败:', e)
        }
      }

      const reviews = await ProductAPI.getReviewsWithFilter(
        productId,
        currentFilter,
        page,
        size
      )

      // 处理评价数据
      const processedReviews = reviews.map(review => ({
        ...review,
        isVerified: !!review.orderItemId,
        isEdited: !!review.updatedAt,
        images: review.images || [],
        imageList: review.images || [],
        createTimeText: this.formatTime(review.createTime)
      }))

      // 计算统计数据（首次加载时）
      if (page === 1 && processedReviews.length > 0) {
        // 统计数据由后端返回或前端计算
      }

      this.setData({
        reviews: page === 1 ? processedReviews : [...this.data.reviews, ...processedReviews],
        hasMore: processedReviews.length >= size,
        loading: false
      })
    } catch (error) {
      console.error('加载评价失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({ loading: false })
    } finally {
      wx.hideLoading()
    }
  },

  // 加载更多
  async loadMore() {
    this.setData({ page: this.data.page + 1 })
    await this.loadReviews()
  },

  // 格式化时间
  formatTime(timeStr) {
    if (!timeStr) return ''
    const date = parseDate(timeStr)
    const now = new Date()
    const diff = now - date
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))

    if (days === 0) return '今天'
    if (days === 1) return '昨天'
    if (days < 7) return `${days}天前`
    if (days < 30) return `${Math.floor(days / 7)}周前`
    if (days < 365) return `${Math.floor(days / 30)}个月前`
    return `${Math.floor(days / 365)}年前`
  },

  // 预览图片
  previewImage(e) {
    const { url, urls } = e.currentTarget.dataset
    wx.previewImage({
      current: url,
      urls: urls || [url]
    })
  },

  // 点赞
  async toggleLike(e) {
    const { id, index } = e.currentTarget.dataset
    const review = this.data.reviews[index]

    try {
      const result = await ProductAPI.toggleReviewLike(id)

      // 更新本地状态
      const updateKey = `reviews[${index}].isLiked`
      const countKey = `reviews[${index}].likeCount`
      this.setData({
        [updateKey]: result.isLiked,
        [countKey]: result.likeCount
      })

      // 点赞动画
      if (result.isLiked) {
        wx.showToast({ title: '点赞成功', icon: 'success' })
      }
    } catch (error) {
      console.error('点赞失败:', error)
      wx.showToast({ title: '操作失败', icon: 'none' })
    }
  },

  // ==================== 编辑评价相关 ====================

  // 打开编辑弹窗
  openEditPopup(e) {
    const { index } = e.currentTarget.dataset
    const review = this.data.reviews[index]

    // 解析已有图片
    let images = []
    if (review.images && review.images.length > 0) {
      images = review.images
    }

    this.setData({
      showEditPopup: true,
      editingReview: review,
      editForm: {
        rating: review.rating,
        content: review.content,
        images: images
      }
    })
  },

  // 关闭编辑弹窗
  closeEditPopup() {
    this.setData({ showEditPopup: false, editingReview: null })
  },

  // 阻止冒泡
  stopPropagation() {},

  // 设置编辑评分
  setEditRating(e) {
    const rating = e.currentTarget.dataset.rating
    this.setData({ 'editForm.rating': rating })
  },

  // 输入编辑内容
  onEditInput(e) {
    this.setData({ 'editForm.content': e.detail.value })
  },

  // 选择编辑图片
  chooseEditImage() {
    const currentImages = this.data.editForm.images
    if (currentImages.length >= 9) {
      wx.showToast({ title: '最多上传9张图片', icon: 'none' })
      return
    }

    wx.chooseMedia({
      count: 9 - currentImages.length,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        wx.showLoading({ title: '上传中...' })
        try {
          const { AIAPI } = require('../../utils/api')
          for (const file of res.tempFiles) {
            const url = await AIAPI.uploadImage(file.tempFilePath)
            this.setData({
              'editForm.images': [...this.data.editForm.images, url]
            })
          }
          wx.hideLoading()
        } catch (error) {
          wx.hideLoading()
          wx.showToast({ title: '图片上传失败', icon: 'none' })
        }
      }
    })
  },

  // 删除编辑图片
  deleteEditImage(e) {
    const index = e.currentTarget.dataset.index
    const images = this.data.editForm.images
    images.splice(index, 1)
    this.setData({ 'editForm.images': images })
  },

  // 预览编辑图片
  previewEditImage(e) {
    const url = e.currentTarget.dataset.url
    wx.previewImage({
      current: url,
      urls: this.data.editForm.images
    })
  },

  // 提交编辑
  async submitEdit() {
    const { editingReview, editForm } = this.data

    if (!editForm.content.trim()) {
      wx.showToast({ title: '请输入评价内容', icon: 'none' })
      return
    }

    if (editForm.content.length > 500) {
      wx.showToast({ title: '评价内容不能超过500字', icon: 'none' })
      return
    }

    wx.showLoading({ title: '提交中...' })
    try {
      await ProductAPI.updateReview(
        editingReview.id,
        editForm.rating,
        editForm.content,
        JSON.stringify(editForm.images)
      )
      wx.hideLoading()
      wx.showToast({ title: '修改成功', icon: 'success' })
      this.setData({ showEditPopup: false })
      // 刷新列表
      this.setData({ page: 1, reviews: [], hasMore: true })
      this.loadReviews()
    } catch (error) {
      wx.hideLoading()
      console.error('编辑评价失败:', error)
      wx.showToast({ title: error.message || '修改失败', icon: 'none' })
    }
  },

  // ==================== 追评相关 ====================

  // 打开追评弹窗
  openFollowUpPopup(e) {
    const { index } = e.currentTarget.dataset
    const review = this.data.reviews[index]

    this.setData({
      showFollowUpPopup: true,
      followUpReview: review,
      followUpContent: ''
    })
  },

  // 关闭追评弹窗
  closeFollowUpPopup() {
    this.setData({ showFollowUpPopup: false, followUpReview: null })
  },

  // 输入追评内容
  onFollowUpInput(e) {
    this.setData({ followUpContent: e.detail.value })
  },

  // 提交追评
  async submitFollowUp() {
    const { followUpReview, followUpContent } = this.data

    if (!followUpContent.trim()) {
      wx.showToast({ title: '请输入追评内容', icon: 'none' })
      return
    }

    if (followUpContent.length > 300) {
      wx.showToast({ title: '追评内容不能超过300字', icon: 'none' })
      return
    }

    wx.showLoading({ title: '提交中...' })
    try {
      await ProductAPI.addFollowUp(followUpReview.id, followUpContent)
      wx.hideLoading()
      wx.showToast({ title: '追评成功', icon: 'success' })
      this.setData({ showFollowUpPopup: false })
      // 刷新列表
      this.setData({ page: 1, reviews: [], hasMore: true })
      this.loadReviews()
    } catch (error) {
      wx.hideLoading()
      console.error('追评失败:', error)
      wx.showToast({ title: error.message || '追评失败', icon: 'none' })
    }
  },

  // 返回
  goBack() {
    wx.navigateBack()
  }
})
