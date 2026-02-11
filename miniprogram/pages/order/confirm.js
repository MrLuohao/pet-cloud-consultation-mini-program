// pages/order/confirm.js
const { OrderAPI, AddressAPI, CouponAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    productIds: [],
    quantities: [],
    cartIds: [],
    orderItems: [],
    address: null,
    availableCoupons: [],
    selectedCoupon: null,
    selectedCouponId: null,
    remark: '',
    totalAmount: '0.00',
    freight: '0.00',
    couponDiscount: '0.00',
    payAmount: '0.00',
    showCouponPopup: false
  },

  onLoad(options) {
    // 订单确认页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    if (options.productIds) {
      try {
        this.setData({
          productIds: JSON.parse(options.productIds),
          quantities: JSON.parse(options.quantities)
        })
      } catch (e) {
        console.error('解析参数失败:', e)
      }
    }
    this.loadOrderConfirm()
    this.loadDefaultAddress()
  },

  // 加载订单确认信息
  async loadOrderConfirm() {
    try {
      wx.showLoading({ title: '加载中...' })
      const result = await OrderAPI.getConfirm(
        this.data.productIds,
        this.data.quantities,
        this.data.cartIds
      )
      this.setData({
        orderItems: result.items || [],
        totalAmount: result.totalAmount || '0.00',
        freight: result.freight || '0.00',
        availableCoupons: result.availableCoupons || []
      })
      this.calculatePayAmount()
    } catch (error) {
      console.error('加载订单确认信息失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  // 加载默认地址
  async loadDefaultAddress() {
    try {
      const address = await AddressAPI.getDefault()
      this.setData({ address })
    } catch (error) {
      console.error('加载地址失败:', error)
    }
  },

  // 选择地址
  selectAddress() {
    wx.navigateTo({
      url: '/pages/address/list?from=order'
    })
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
  },

  // 备注输入
  onRemarkInput(e) {
    this.setData({ remark: e.detail.value })
  },

  // 选择优惠券
  selectCoupon() {
    this.setData({ showCouponPopup: true })
  },

  // 关闭优惠券弹窗
  closeCouponPopup() {
    this.setData({ showCouponPopup: false })
  },

  // 选择优惠券
  selectCouponItem(e) {
    const { id } = e.currentTarget.dataset
    const coupon = this.data.availableCoupons.find(c => c.id === id)
    this.setData({
      selectedCoupon: coupon,
      selectedCouponId: id,
      showCouponPopup: false
    })
    this.calculateCouponDiscount(coupon)
  },

  // 不使用优惠券
  notUseCoupon() {
    this.setData({
      selectedCoupon: null,
      selectedCouponId: null,
      showCouponPopup: false,
      couponDiscount: '0.00'
    })
    this.calculatePayAmount()
  },

  // 计算优惠券折扣
  calculateCouponDiscount(coupon) {
    let discount = 0
    const totalAmount = parseFloat(this.data.totalAmount)
    if (coupon && totalAmount >= (coupon.minAmount || 0)) {
      if (coupon.couponType === 1) {
        discount = coupon.discountAmount || 0
      } else if (coupon.couponType === 2) {
        discount = totalAmount * (1 - coupon.discountRate / 10)
        if (coupon.maxDiscount && discount > coupon.maxDiscount) {
          discount = coupon.maxDiscount
        }
      }
    }
    this.setData({ couponDiscount: discount.toFixed(2) })
    this.calculatePayAmount()
  },

  // 计算应付金额
  calculatePayAmount() {
    const totalAmount = parseFloat(this.data.totalAmount) || 0
    const freight = parseFloat(this.data.freight) || 0
    const couponDiscount = parseFloat(this.data.couponDiscount) || 0
    const payAmount = totalAmount + freight - couponDiscount
    this.setData({ payAmount: Math.max(0, payAmount).toFixed(2) })
  },

  // 提交订单
  async submitOrder() {
    if (!this.data.address) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '提交中...' })
      const orderId = await OrderAPI.submit(
        this.data.productIds,
        this.data.quantities,
        this.data.address.id,
        this.data.selectedCouponId,
        this.data.remark
      )
      wx.hideLoading()
      wx.navigateTo({
        url: `/pages/order/detail?id=${orderId}`
      })
    } catch (error) {
      console.error('提交订单失败:', error)
      wx.hideLoading()
      wx.showToast({ title: '提交失败', icon: 'none' })
    }
  }
})
