Page({
  data: {
    status: 'success',
    orderId: null,
    amount: '0.00',
    feedbackTitle: '支付成功'
  },

  onLoad(options) {
    const status = options.status || 'success'
    const orderId = options.orderId ? Number(options.orderId) : null
    const amount = options.amount || '0.00'

    this.setData({
      status,
      orderId,
      amount,
      feedbackTitle: status === 'success' ? '支付成功' : status === 'fail' ? '支付未完成' : '支付处理中'
    })

    if (status === 'success') {
      this.redirectTimer = setTimeout(() => {
        this.closeSuccess()
      }, 1200)
    }
  },

  onUnload() {
    if (this.redirectTimer) {
      clearTimeout(this.redirectTimer)
      this.redirectTimer = null
    }
  },

  closeSuccess() {
    if (this.redirectTimer) {
      clearTimeout(this.redirectTimer)
      this.redirectTimer = null
    }

    if (this.data.orderId) {
      wx.redirectTo({
        url: `/pages/order/detail?id=${this.data.orderId}`
      })
      return
    }

    wx.switchTab({ url: '/pages/shop/shop' })
  }
})
