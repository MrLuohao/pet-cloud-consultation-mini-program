// pages/order/pay-result/index.js - 支付结果页（Story 4.2）
const { OrderAPI } = require('../../../utils/api')

Page({
  data: {
    status: 'success',  // success | fail | pending
    orderId: null,
    amount: '0.00',
    order: null,
    loading: false
  },

  onLoad(options) {
    const status = options.status || 'success'
    const orderId = options.orderId ? Number(options.orderId) : null
    const amount = options.amount || '0.00'
    this.setData({ status, orderId, amount })

    // 支付成功时拉取订单摘要
    if (status === 'success' && orderId) {
      this.loadOrderSummary(orderId)
    }
  },

  async loadOrderSummary(orderId) {
    this.setData({ loading: true })
    try {
      const order = await OrderAPI.getDetail(orderId)
      this.setData({ order, loading: false })
    } catch (error) {
      console.error('加载订单摘要失败:', error)
      this.setData({ loading: false })
    }
  },

  goToOrderDetail() {
    if (this.data.orderId) {
      wx.redirectTo({
        url: `/pages/order/detail?id=${this.data.orderId}`
      })
    }
  },

  goToHome() {
    wx.switchTab({ url: '/pages/index/index' })
  },

  goToShop() {
    wx.switchTab({ url: '/pages/shop/shop' })
  },

  // 重试支付（失败时）
  retryPay() {
    if (this.data.orderId) {
      wx.redirectTo({
        url: `/pages/order/detail?id=${this.data.orderId}`
      })
    }
  }
})
