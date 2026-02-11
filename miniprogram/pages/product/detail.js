// pages/product/detail.js
const { ProductAPI, CartAPI, OrderAPI, CollectionAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    productId: null,
    product: {
      imageUrls: [],
      name: '',
      summary: '',
      price: 0,
      originalPrice: 0,
      sales: 0,
      stock: 0,
      rating: 5,
      reviewCount: 0
    },
    // 价格拆分显示
    priceInteger: '0',
    priceDecimal: '00',
    discountPercent: 0,
    // 轮播索引
    currentImageIndex: 0,
    quantity: 1,
    reviews: [],
    cartCount: 0,
    isCollected: false,
    // 写评价相关
    showReviewPopup: false,
    reviewableOrderItem: null,
    reviewForm: {
      rating: 5,
      content: '',
      images: []
    }
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ productId: options.id })
      this.loadProductDetail()
      this.loadCartCount()
      this.checkCollectionStatus()
    }
  },

  onShow() {
    this.loadCartCount()
  },

  // 加载商品详情
  async loadProductDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      const product = await ProductAPI.getDetail(this.data.productId)

      // 处理价格显示
      const price = parseFloat(product.price) || 0
      const priceStr = price.toFixed(2)
      const [priceInteger, priceDecimal] = priceStr.split('.')

      // 计算折扣百分比
      let discountPercent = 0
      if (product.originalPrice && product.originalPrice > price) {
        discountPercent = Math.round((1 - price / product.originalPrice) * 100)
      }

      this.setData({
        product: {
          ...product,
          imageUrls: (product.imageUrls && product.imageUrls.length > 0) ? product.imageUrls : [product.coverUrl]
        },
        priceInteger,
        priceDecimal,
        discountPercent
      })
      this.loadReviews()
    } catch (error) {
      console.error('加载商品详情失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  // 加载评价
  async loadReviews() {
    try {
      const reviews = await ProductAPI.getReviews(this.data.productId, 1, 3)
      this.setData({ reviews })
    } catch (error) {
      console.error('加载评价失败:', error)
    }
  },

  // 加载购物车数量（未登录时显示0）
  async loadCartCount() {
    // 未登录时直接显示0，不调用接口
    if (!isLoggedIn()) {
      this.setData({ cartCount: 0 })
      return
    }
    try {
      const count = await CartAPI.getCount()
      this.setData({ cartCount: count })
    } catch (error) {
      console.error('加载购物车数量失败:', error)
      this.setData({ cartCount: 0 })
    }
  },

  // 检查收藏状态（未登录时显示未收藏）
  async checkCollectionStatus() {
    // 未登录时直接显示未收藏，不调用接口
    if (!isLoggedIn()) {
      this.setData({ isCollected: false })
      return
    }
    try {
      const result = await CollectionAPI.check(this.data.productId)
      this.setData({ isCollected: result && result.collected })
    } catch (error) {
      console.error('检查收藏状态失败:', error)
      this.setData({ isCollected: false })
    }
  },

  // 切换收藏状态（需要登录）
  async toggleCollect() {
    // 未登录时跳转登录页
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { isCollected, productId } = this.data

    try {
      if (isCollected) {
        // 当前已收藏，执行取消收藏
        await CollectionAPI.remove(productId)
        this.setData({ isCollected: false })
        wx.showToast({
          title: '已取消收藏',
          icon: 'none'
        })
      } else {
        // 当前未收藏，执行添加收藏
        await CollectionAPI.add(productId)
        this.setData({ isCollected: true })
        wx.showToast({
          title: '收藏成功',
          icon: 'success'
        })
      }
    } catch (error) {
      console.error('切换收藏状态失败:', error)
      wx.showToast({ title: '操作失败', icon: 'none' })
    }
  },

  // 获取库存状态文本
  getStockStatus() {
    const stock = this.data.product.stock
    if (stock <= 0) return '暂无库存'
    if (stock <= 10) return '库存紧张'
    return '库存充足'
  },

  // 增加数量
  increaseQuantity() {
    const { quantity, product } = this.data
    if (product.stock <= 0) {
      wx.showToast({ title: '商品暂无库存', icon: 'none' })
      return
    }
    if (quantity < product.stock) {
      this.setData({ quantity: quantity + 1 })
    } else {
      wx.showToast({ title: '库存不足', icon: 'none' })
    }
  },

  // 减少数量
  decreaseQuantity() {
    const { quantity } = this.data
    if (quantity > 1) {
      this.setData({ quantity: quantity - 1 })
    }
  },

  // 手动输入数量
  onQuantityInput(e) {
    let value = e.detail.value
    // 移除非数字字符
    value = value.replace(/[^\d]/g, '')
    // 限制输入长度
    if (value.length > 4) value = value.substring(0, 4)
    // 实时更新显示（暂不限制库存，在失焦时校验）
    this.setData({ quantity: value === '' ? 0 : parseInt(value) || 0 })
  },

  // 输入框失焦时校验数量
  onQuantityBlur() {
    const { product } = this.data
    let quantity = this.data.quantity
    // 校验最小值
    if (quantity < 1) quantity = 1
    // 校验最大值（库存）
    if (product.stock > 0 && quantity > product.stock) {
      quantity = product.stock
      wx.showToast({ title: `库存仅剩${product.stock}件`, icon: 'none' })
    }
    this.setData({ quantity })
  },

  // 加入购物车（需要登录）
  async addToCart() {
    // 未登录时跳转登录页
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { product, quantity } = this.data
    if (product.stock <= 0) {
      wx.showToast({ title: '商品暂无库存', icon: 'none' })
      return
    }
    wx.showLoading({ title: '添加中...' })
    try {
      await CartAPI.add(this.data.productId, quantity)
      wx.hideLoading()
      wx.showToast({ title: '已加入购物车', icon: 'success' })
      this.loadCartCount()
    } catch (error) {
      wx.hideLoading()
      console.error('加入购物车失败:', error)
      // 处理商品已在购物车中的情况（唯一键冲突）
      if (error.message && error.message.includes('uk_user_product')) {
        wx.showToast({ title: '商品已在购物车中', icon: 'none' })
        this.loadCartCount()
      }
    }
  },

  // 立即购买（需要登录）
  buyNow() {
    // 未登录时跳转登录页
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { product, productId, quantity } = this.data
    if (product.stock <= 0) {
      wx.showToast({ title: '商品暂无库存', icon: 'none' })
      return
    }
    wx.navigateTo({
      url: `/pages/order/confirm?productIds=${JSON.stringify([productId])}&quantities=${JSON.stringify([quantity])}`
    })
  },

  // 跳转购物车
  gotoCart() {
    wx.navigateTo({
      url: '/pages/cart/cart'
    })
  },

  // 联系客服
  contactService() {
    wx.showActionSheet({
      itemList: ['在线客服', '电话咨询 400-888-8888'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 在线客服 - 显示客服信息或跳转客服页面
          wx.showModal({
            title: '在线客服',
            content: '客服工作时间：9:00-21:00\n\n您也可以添加客服微信：pet_service\n或拨打客服热线获得帮助',
            showCancel: true,
            cancelText: '知道了',
            confirmText: '拨打电话',
            success: (result) => {
              if (result.confirm) {
                wx.makePhoneCall({
                  phoneNumber: '400-888-8888'
                })
              }
            }
          })
        } else if (res.tapIndex === 1) {
          // 电话客服
          wx.makePhoneCall({
            phoneNumber: '400-888-8888',
            fail: () => {
              wx.showToast({ title: '拨打失败', icon: 'none' })
            }
          })
        }
      }
    })
  },

  // 图片加载失败时使用默认图片
  onImageError(e) {
    const index = e.currentTarget.dataset.index
    // 使用在线占位图
    const defaultImage = 'https://via.placeholder.com/400x400?text=No+Image'
    const imageUrls = [...this.data.product.imageUrls]
    imageUrls[index] = defaultImage
    this.setData({
      'product.imageUrls': imageUrls
    })
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
  },

  // 预览评价图片（详情页预览区）
  previewReviewImages(e) {
    const url = e.currentTarget.dataset.url
    const urls = e.currentTarget.dataset.urls
    wx.previewImage({
      current: url,
      urls: urls
    })
  },

  // 查看评价
  gotoReviews() {
    wx.navigateTo({
      url: `/pages/product/reviews?productId=${this.data.productId}`
    })
  },

  // 轮播切换
  onSwiperChange(e) {
    this.setData({
      currentImageIndex: e.detail.current
    })
  },

  // ==================== 写评价相关 ====================

  // 打开写评价弹窗（需要登录）
  async openReviewPopup() {
    // 未登录时跳转登录页
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    wx.showLoading({ title: '检查购买记录...' })
    try {
      const result = await ProductAPI.getReviewableOrderItem(this.data.productId)
      wx.hideLoading()

      if (result && result.orderItemId) {
        // 有可评价的订单项
        this.setData({
          showReviewPopup: true,
          reviewableOrderItem: result,
          reviewForm: {
            rating: 5,
            content: '',
            images: []
          }
        })
      } else if (result && result.hasPurchased && result.allReviewed) {
        // 已购买但全部已评价
        wx.showModal({
          title: '提示',
          content: '您已评价过此商品的所有订单，感谢您的支持！',
          showCancel: false,
          confirmText: '知道了'
        })
      } else {
        // 未购买过此商品
        wx.showModal({
          title: '提示',
          content: '您还没有购买此商品，购买后才能评价哦~',
          showCancel: true,
          cancelText: '取消',
          confirmText: '立即购买',
          success: (res) => {
            if (res.confirm) {
              this.buyNow()
            }
          }
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('检查购买记录失败:', error)
      wx.showToast({ title: '操作失败，请稍后重试', icon: 'none' })
    }
  },

  // 关闭评价弹窗
  closeReviewPopup() {
    this.setData({ showReviewPopup: false })
  },

  // 阻止弹窗内点击事件冒泡
  stopPropagation() {},

  // 设置评分
  setRating(e) {
    const rating = e.currentTarget.dataset.rating
    this.setData({ 'reviewForm.rating': rating })
  },

  // 输入评价内容
  onReviewInput(e) {
    this.setData({ 'reviewForm.content': e.detail.value })
  },

  // 选择图片
  chooseReviewImage() {
    const currentImages = this.data.reviewForm.images
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
              'reviewForm.images': [...this.data.reviewForm.images, url]
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

  // 删除图片
  deleteReviewImage(e) {
    const index = e.currentTarget.dataset.index
    const images = this.data.reviewForm.images
    images.splice(index, 1)
    this.setData({ 'reviewForm.images': images })
  },

  // 预览评价图片
  previewReviewImage(e) {
    const url = e.currentTarget.dataset.url
    wx.previewImage({
      current: url,
      urls: this.data.reviewForm.images
    })
  },

  // 提交评价
  async submitReview() {
    const { reviewableOrderItem, reviewForm, productId } = this.data

    if (!reviewForm.content.trim()) {
      wx.showToast({ title: '请输入评价内容', icon: 'none' })
      return
    }

    if (reviewForm.content.length > 500) {
      wx.showToast({ title: '评价内容不能超过500字', icon: 'none' })
      return
    }

    wx.showLoading({ title: '提交中...' })
    try {
      await ProductAPI.createReview(
        reviewableOrderItem.orderItemId,
        productId,
        reviewForm.rating,
        reviewForm.content,
        JSON.stringify(reviewForm.images)
      )
      wx.hideLoading()
      wx.showToast({ title: '评价成功', icon: 'success' })
      this.setData({ showReviewPopup: false })
      // 刷新商品详情（更新评价数量）和评价列表
      this.loadProductDetail()
    } catch (error) {
      wx.hideLoading()
      console.error('提交评价失败:', error)
      wx.showToast({ title: error.message || '提交失败', icon: 'none' })
    }
  }
})
