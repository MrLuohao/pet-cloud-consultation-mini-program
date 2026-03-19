/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  待评价订单页面 | Pending Review Orders Page                                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const { OrderAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    orders: [],
    loading: true,
    page: 1,
    size: 10,
    hasMore: true,
    loaded: false
  },

  onLoad() {
    // 待评价页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
  },

  onShow() {
    // 未登录时不执行任何操作
    if (!isLoggedIn()) {
      return
    }
    // 每次显示页面时刷新数据，确保评价返回后列表更新
    this.setData({ page: 1, orders: [], hasMore: true })
    this.loadPendingReviewOrders()
  },

  onPullDownRefresh() {
    this.setData({ page: 1, orders: [], hasMore: true })
    this.loadPendingReviewOrders().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadPendingReviewOrders()
    }
  },

  async loadPendingReviewOrders() {
    if (!this.data.hasMore) return

    try {
      this.setData({ loading: true })
      const orders = await OrderAPI.getPendingReviewList(this.data.page, this.data.size)

      this.setData({
        orders: this.data.page === 1 ? orders : [...this.data.orders, ...orders],
        page: this.data.page + 1,
        hasMore: orders.length === this.data.size,
        loading: false
      })
    } catch (error) {
      console.error('加载待评价订单失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({ loading: false })
    }
  },

  goToReview(e) {
    const { orderId, orderItemId, productId, productName, coverUrl, price, quantity } = e.currentTarget.dataset
    const itemsData = encodeURIComponent(JSON.stringify([{
      orderItemId,
      productId,
      productName,
      coverUrl,
      price,
      quantity
    }]))
    wx.navigateTo({
      url: `/pages/order/review?orderId=${orderId}&itemsData=${itemsData}`
    })
  },

  goToOrderDetail(e) {
    const { orderId } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/order/detail?id=${orderId}`
    })
  }
})
