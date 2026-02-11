/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  评价页面 | Review Page                                                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const { ProductAPI, API_BASE_URL } = require('../../utils/api')

Page({
  data: {
    orderId: null,
    orderItem: null,
    rating: 5,
    content: '',
    images: [],
    maxImages: 9,
    submitting: false
  },

  onLoad(options) {
    if (options.orderId && options.itemData) {
      try {
        const orderItem = JSON.parse(decodeURIComponent(options.itemData))
        this.setData({
          orderId: options.orderId,
          orderItem
        })
      } catch (e) {
        console.error('解析商品数据失败:', e)
        wx.showToast({ title: '数据错误', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
      }
    }
  },

  setRating(e) {
    const rating = e.currentTarget.dataset.rating
    this.setData({ rating })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  chooseImage() {
    const { images, maxImages } = this.data
    const remaining = maxImages - images.length

    if (remaining <= 0) {
      wx.showToast({ title: `最多上传${maxImages}张图片`, icon: 'none' })
      return
    }

    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = res.tempFiles.map(file => file.tempFilePath)
        this.setData({
          images: [...images, ...newImages]
        })
      }
    })
  },

  previewImage(e) {
    const url = e.currentTarget.dataset.url
    wx.previewImage({
      current: url,
      urls: this.data.images
    })
  },

  deleteImage(e) {
    const index = e.currentTarget.dataset.index
    const images = this.data.images.filter((_, i) => i !== index)
    this.setData({ images })
  },

  async submitReview() {
    const { orderId, orderItem, rating, content, images, submitting } = this.data

    if (submitting) return

    if (!content.trim()) {
      wx.showToast({ title: '请填写评价内容', icon: 'none' })
      return
    }

    if (content.length < 10) {
      wx.showToast({ title: '评价内容至少10个字', icon: 'none' })
      return
    }

    try {
      this.setData({ submitting: true })
      wx.showLoading({ title: '提交中...' })

      // 上传图片
      let uploadedImages = []
      for (const imagePath of images) {
        try {
          const result = await this.uploadImage(imagePath)
          if (result) {
            uploadedImages.push(result)
          }
        } catch (e) {
          console.error('上传图片失败:', e)
        }
      }

      // 提交评价
      await ProductAPI.createReview(
        orderItem.orderItemId,
        orderItem.productId,
        rating,
        content,
        uploadedImages.join(',')
      )

      wx.hideLoading()
      wx.showToast({ title: '评价成功', icon: 'success' })

      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (error) {
      wx.hideLoading()
      console.error('提交评价失败:', error)
      wx.showToast({ title: '提交失败，请重试', icon: 'none' })
      this.setData({ submitting: false })
    }
  },

  uploadImage(filePath) {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token')
      const userInfo = wx.getStorageSync('userInfo') || {}
      wx.uploadFile({
        url: `${API_BASE_URL}/api/upload/image`,
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : '',
          'X-User-Id': userInfo.id ? String(userInfo.id) : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data)
              if (data.status === true) {
                resolve(data.data)
              } else {
                reject(new Error(data.msg || '上传失败'))
              }
            } catch (e) {
              reject(e)
            }
          } else {
            reject(new Error('上传失败'))
          }
        },
        fail: reject
      })
    })
  }
})
