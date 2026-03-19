// pages/order/detail.js
const { OrderAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')
const { formatOrderDetail } = require('./order-detail-presenter')

Page({
  data: {
    orderId: null,
    viewModel: {
      statusTitle: '',
      statusHint: '',
      statusChip: '',
      receiverLine: '',
      receiverAddress: '',
      products: [],
      amountRows: [],
      infoRows: [],
      primaryAction: null,
      secondaryAction: null
    },
    timeline: [],
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
      remark: '',
      freight: '0.00'
    },
    showPayPanel: false,
    paying: false,
    showCancelSheet: false,
    selectedCancelReason: 'no_need',
    cancelNote: '',
    cancelReasonOptions: [
      { key: 'no_need', label: '不想买了' },
      { key: 'change_address', label: '收货信息需要调整' },
      { key: 'change_product', label: '想重新选择商品' }
    ]
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
      const timeline = await OrderAPI.getTimeline(this.data.orderId).catch(() => [])
      this.setData({
        order,
        viewModel: formatOrderDetail(order),
        timeline
      })
    } catch (error) {
      console.error('加载订单详情失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  openCancelSheet() {
    this.setData({
      showCancelSheet: true,
      selectedCancelReason: this.data.selectedCancelReason || 'no_need'
    })
  },

  closeCancelSheet() {
    this.setData({ showCancelSheet: false })
  },

  selectCancelReason(e) {
    const { reason } = e.currentTarget.dataset
    if (!reason) return
    this.setData({ selectedCancelReason: reason })
  },

  onCancelNoteInput(e) {
    this.setData({ cancelNote: e.detail.value || '' })
  },

  async confirmCancelOrder() {
    try {
      wx.showLoading({ title: '处理中...' })
      await OrderAPI.cancel(this.data.orderId)
      wx.hideLoading()
      this.setData({
        showCancelSheet: false,
        cancelNote: ''
      })
      wx.showToast({ title: '订单已取消', icon: 'success' })
      this.loadOrderDetail()
    } catch (error) {
      wx.hideLoading()
      wx.showToast({ title: '取消失败', icon: 'none' })
    }
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
    const reviewItems = (this.data.order.items || [])
      .filter(item => !item.reviewed)
      .map(item => ({
        orderItemId: item.orderItemId || item.id,
        productId: item.productId,
        productName: item.productName || item.name,
        coverUrl: item.coverUrl,
        specLabel: item.specLabel || item.spec,
        price: item.price,
        quantity: item.quantity
      }))

    if (reviewItems.length === 0) {
      wx.showToast({ title: '暂无可评价商品', icon: 'none' })
      return
    }

    const itemsData = encodeURIComponent(JSON.stringify(reviewItems))
    wx.navigateTo({
      url: `/pages/order/review?orderId=${this.data.orderId}&itemsData=${itemsData}`
    })
  },

  requestAfterSale() {
    wx.showToast({
      title: '售后功能整理中',
      icon: 'none'
    })
  },

  handleSecondaryAction() {
    const action = this.data.viewModel.secondaryAction
    if (!action) return

    if (action.key === 'cancel') {
      this.openCancelSheet()
      return
    }

    if (action.key === 'after_sale') {
      this.requestAfterSale()
    }
  },

  handlePrimaryAction() {
    const action = this.data.viewModel.primaryAction
    if (!action) return

    if (action.key === 'pay') {
      this.showPayPanel()
      return
    }

    if (action.key === 'receive') {
      this.confirmReceive()
      return
    }

    if (action.key === 'review') {
      this.goToReview()
    }
  }
})
