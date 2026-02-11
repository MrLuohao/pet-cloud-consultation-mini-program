// pages/order/detail.js
const { OrderAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    orderId: null,
    order: {
      status: 0,
      statusDesc: '',
      receiverName: '',
      receiverPhone: '',
      receiverAddress: '',
      items: [],
      totalAmount: '0.00',
      couponDiscount: '0.00',
      payAmount: '0.00',
      orderNo: '',
      createTime: '',
      payTime: '',
      remark: ''
    },
    statusIcon: '⏱',
    hasUnreviewedItems: false,
    showPayPanel: false,
    paying: false
  },

  onLoad(options) {
    // 订单详情页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    if (options.id) {
      this.setData({ orderId: options.id })
      this.loadOrderDetail()
    }
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
  },

  // 加载订单详情
  async loadOrderDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      const order = await OrderAPI.getDetail(this.data.orderId)
      const statusIcon = this.getStatusIcon(order.status)
      // 检查是否有未评价商品
      const hasUnreviewedItems = order.status === 3 && order.items.some(item => !item.reviewed)
      this.setData({ order, statusIcon, hasUnreviewedItems })
    } catch (error) {
      console.error('加载订单详情失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  // 获取状态图标
  getStatusIcon(status) {
    const icons = {
      0: '⏱',
      1: '📦',
      2: '🚚',
      3: '✅',
      4: '❌'
    }
    return icons[status] || '⏱'
  },

  // 取消订单
  cancelOrder() {
    wx.showModal({
      title: '提示',
      content: '确定要取消订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '处理中...' })
            await OrderAPI.cancel(this.data.orderId)
            wx.showToast({ title: '订单已取消', icon: 'success' })
            this.loadOrderDetail()
          } catch (error) {
            wx.hideLoading()
            wx.showToast({ title: '取消失败', icon: 'none' })
          }
        }
      }
    })
  },

  // 显示支付面板
  showPayPanel() {
    this.setData({ showPayPanel: true })
  },

  // 隐藏支付面板
  hidePayPanel() {
    if (this.data.paying) return
    this.setData({ showPayPanel: false })
  },

  // 确认支付 - 集成 wx.requestPayment（Story 4.1）
  async confirmPay() {
    if (this.data.paying) return
    this.setData({ paying: true })
    wx.showLoading({ title: '获取支付信息...' })
    try {
      // 1. 向后端请求微信支付参数（金额由后端从数据库读取，防篡改）
      const payParams = await OrderAPI.pay(this.data.orderId)
      wx.hideLoading()
      this.setData({ showPayPanel: false })

      // 2. 调起微信支付
      await new Promise((resolve, reject) => {
        wx.requestPayment({
          timeStamp: payParams.timeStamp,
          nonceStr: payParams.nonceStr,
          package: payParams.packageStr,
          signType: payParams.signType || 'RSA',
          paySign: payParams.paySign,
          success: resolve,
          fail: reject
        })
      })

      // 3. 支付成功 - 跳转结果页
      wx.redirectTo({
        url: `/pages/order/pay-result/index?orderId=${this.data.orderId}&amount=${this.data.order.payAmount}&status=success`
      })
    } catch (error) {
      wx.hideLoading()
      this.setData({ showPayPanel: false })
      // 用户主动取消支付不显示错误提示
      if (error && error.errMsg && error.errMsg.includes('cancel')) {
        wx.showToast({ title: '已取消支付', icon: 'none' })
      } else {
        wx.showToast({ title: '支付失败，请重试', icon: 'none' })
        wx.redirectTo({
          url: `/pages/order/pay-result/index?orderId=${this.data.orderId}&amount=${this.data.order.payAmount}&status=fail`
        })
      }
    } finally {
      this.setData({ paying: false })
    }
  },

  // 确认收货
  confirmReceive() {
    wx.showModal({
      title: '提示',
      content: '确认已收到商品？',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '处理中...' })
            await OrderAPI.confirmReceive(this.data.orderId)
            wx.showToast({ title: '确认成功', icon: 'success' })
            this.loadOrderDetail()
          } catch (error) {
            wx.hideLoading()
            wx.showToast({ title: '操作失败', icon: 'none' })
          }
        }
      }
    })
  },

  // 去评价
  goToReview() {
    wx.navigateTo({
      url: '/pages/order/pending-review'
    })
  }
})
