/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  评价页面 | Review Page                                                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const { ProductAPI, AIAPI } = require('../../utils/api')

function normalizeReviewItems(items) {
  const list = Array.isArray(items) ? items : []
  return list.map(item => ({
    orderItemId: item.orderItemId,
    productId: item.productId,
    productName: item.productName,
    coverUrl: item.coverUrl,
    metaText: `${item.specLabel || item.spec || ''}${item.specLabel || item.spec ? '' : ''}${item.price ? ` · ¥${item.price}` : ''}${item.quantity ? ` · x${item.quantity}` : ''}`.replace(/^ · /, ''),
    rating: 5,
    content: '',
    images: [],
    maxImages: 3,
    placeholder: '分享你在适口性、耐受性、实际使用感受。',
    starOptions: [1, 2, 3, 4, 5].map(value => ({ value }))
  }))
}

Page({
  data: {
    orderId: null,
    reviewItems: [],
    submitting: false
  },

  onLoad(options) {
    if (options.orderId && (options.itemsData || options.itemData)) {
      try {
        const parsedItems = options.itemsData
          ? JSON.parse(decodeURIComponent(options.itemsData))
          : [JSON.parse(decodeURIComponent(options.itemData))]
        this.setData({
          orderId: options.orderId,
          reviewItems: normalizeReviewItems(parsedItems)
        })
      } catch (e) {
        console.error('解析商品数据失败:', e)
        wx.showToast({ title: '数据错误', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1500)
      }
    }
  },

  setRating(e) {
    const { index, rating } = e.currentTarget.dataset
    const reviewItems = this.data.reviewItems.slice()
    reviewItems[index].rating = Number(rating)
    this.setData({ reviewItems })
  },

  onContentInput(e) {
    const { index } = e.currentTarget.dataset
    const reviewItems = this.data.reviewItems.slice()
    reviewItems[index].content = e.detail.value
    this.setData({ reviewItems })
  },

  chooseImage(e) {
    const { index } = e.currentTarget.dataset
    const reviewItems = this.data.reviewItems.slice()
    const reviewItem = reviewItems[index]
    const remaining = reviewItem.maxImages - reviewItem.images.length

    if (remaining <= 0) {
      wx.showToast({ title: `最多上传${reviewItem.maxImages}张图片`, icon: 'none' })
      return
    }

    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = res.tempFiles.map(file => file.tempFilePath)
        reviewItem.images = [...reviewItem.images, ...newImages]
        reviewItems[index] = reviewItem
        this.setData({ reviewItems })
      }
    })
  },

  previewImage(e) {
    const url = e.currentTarget.dataset.url
    const { index } = e.currentTarget.dataset
    wx.previewImage({
      current: url,
      urls: this.data.reviewItems[index].images
    })
  },

  deleteImage(e) {
    const { index, imageIndex } = e.currentTarget.dataset
    const reviewItems = this.data.reviewItems.slice()
    reviewItems[index].images = reviewItems[index].images.filter((_, i) => i !== imageIndex)
    this.setData({ reviewItems })
  },

  async submitReview() {
    const { reviewItems, submitting } = this.data

    if (submitting) return

    const invalidItem = reviewItems.find(item => !item.content.trim() || item.content.trim().length < 10)
    if (invalidItem) {
      wx.showToast({ title: '每件商品评价至少10个字', icon: 'none' })
      return
    }

    try {
      this.setData({ submitting: true })
      wx.showLoading({ title: '提交中...' })

      for (const item of reviewItems) {
        const uploadedImages = []
        for (const imagePath of item.images) {
          try {
            const result = await this.uploadImage(imagePath)
            if (result) {
              uploadedImages.push(result)
            }
          } catch (e) {
            console.error('上传图片失败:', e)
          }
        }

        await ProductAPI.createReview(
          item.orderItemId,
          item.productId,
          item.rating,
          item.content,
          uploadedImages.join(',')
        )
      }

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
    return AIAPI.uploadMedia(filePath, 'shop_review')
      .then(result => result.url)
  }
})
