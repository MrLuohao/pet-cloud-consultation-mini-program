// pages/order/confirm.js
const { OrderAPI, AddressAPI, CouponAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')
const {
  PAYMENT_PASSWORD_KEYS,
  applyPasswordKey,
  getPaymentMethodLabel,
  buildVerificationMethodText
} = require('./payment-presenter')

const PAYMENT_BADGE_MAP = {
  wechat: { badge: '微', badgeClass: 'wechat' },
  alipay: { badge: '支', badgeClass: 'alipay' },
  bank: { badge: '银', badgeClass: 'bank' },
  credit: { badge: '信', badgeClass: 'credit' }
}

function decoratePaymentMethods(paymentMethods) {
  return (paymentMethods || []).map(method => {
    const badgeConfig = PAYMENT_BADGE_MAP[method.key] || { badge: '付', badgeClass: 'bank' }
    return {
      ...method,
      ...badgeConfig
    }
  })
}

function resolveVerificationMode(methods, selectedKey, fallbackMode = 'face') {
  const selected = (methods || []).find(item => item.key === selectedKey)
  return selected && selected.verifyType ? selected.verifyType : fallbackMode
}

function parseArrayOption(value) {
  if (!value) return []
  return JSON.parse(decodeURIComponent(value))
}

function formatPhoneDisplay(phone) {
  const digits = String(phone || '').replace(/\D/g, '')
  if (digits.length !== 11) return phone || ''
  return `${digits.slice(0, 3)} ${digits.slice(3, 7)} ${digits.slice(7)}`
}

function decorateOrderItems(items) {
  return (items || []).map(item => {
    const title = item.productName || ''
    let fallbackIcon = '/image/icons/shop-all.svg'

    if (/主粮|猫粮|狗粮|冻干|罐头/.test(title)) {
      fallbackIcon = '/image/icons/shop-food.svg'
    } else if (/喷雾|护理|洗护|清洁/.test(title)) {
      fallbackIcon = '/image/icons/shop-care.svg'
    } else if (/保健|营养|维生素/.test(title)) {
      fallbackIcon = '/image/icons/shop-health.svg'
    } else if (/玩具|逗猫|球|飞盘/.test(title)) {
      fallbackIcon = '/image/icons/shop-toy.svg'
    }

    return {
      ...item,
      fallbackIcon
    }
  })
}

Page({
  data: {
    productIds: [],
    quantities: [],
    cartIds: [],
    specLabels: [],
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
    deliveryText: '顺丰冷链',
    orderHint: '订单将于提交后创建，请在 30 分钟内完成支付。',
    showPaymentSheet: false,
    showVerification: false,
    verificationMode: 'face',
    verificationStage: 'face',
    selectedPaymentMethod: 'wechat',
    selectedPaymentLabel: '微信支付',
    verificationMethodText: '微信支付 · Face ID 验证',
    paymentPassword: '',
    passwordSlots: [0, 1, 2, 3, 4, 5],
    passwordKeypadRows: PAYMENT_PASSWORD_KEYS,
    paymentMethods: [
      {
        key: 'wechat',
        badge: '微',
        badgeClass: 'wechat',
        title: '微信支付',
        subtitle: '默认推荐 · 快速完成支付'
      },
      {
        key: 'alipay',
        badge: '支',
        badgeClass: 'alipay',
        title: '支付宝',
        subtitle: '切换支付方式 · 支持余额与花呗'
      },
      {
        key: 'bank',
        badge: '银',
        badgeClass: 'bank',
        title: '银行卡',
        subtitle: '储蓄卡与借记卡账户'
      },
      {
        key: 'credit',
        badge: '信',
        badgeClass: 'credit',
        title: '信用卡',
        subtitle: '支持分期与国际信用卡'
      }
    ]
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
          productIds: parseArrayOption(options.productIds),
          quantities: parseArrayOption(options.quantities),
          specLabels: parseArrayOption(options.specLabels)
        })
      } catch (e) {
        console.error('解析参数失败:', e)
      }
    }
    if (options.cartIds) {
      try {
        this.setData({
          cartIds: parseArrayOption(options.cartIds)
        })
      } catch (e) {
        console.error('解析购物车参数失败:', e)
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
        this.data.cartIds,
        this.data.specLabels
      )
      const paymentMethods = Array.isArray(result.paymentMethods) && result.paymentMethods.length
        ? decoratePaymentMethods(result.paymentMethods)
        : this.data.paymentMethods
      const selectedPaymentMethod = result.selectedPaymentMethod || paymentMethods[0]?.key || this.data.selectedPaymentMethod
      const verificationMode = resolveVerificationMode(
        paymentMethods,
        selectedPaymentMethod,
        this.data.verificationMode
      )

      this.setData({
        orderItems: decorateOrderItems(result.items || []),
        totalAmount: result.goodsAmount ?? result.totalAmount ?? '0.00',
        freight: result.freight ?? '0.00',
        couponDiscount: result.couponDiscount ?? '0.00',
        payAmount: result.payAmount ?? '0.00',
        availableCoupons: result.availableCoupons || [],
        deliveryText: result.deliveryText || this.data.deliveryText,
        orderHint: result.orderHint || this.data.orderHint,
        paymentMethods,
        selectedPaymentMethod,
        selectedPaymentLabel: getPaymentMethodLabel(selectedPaymentMethod),
        verificationMode,
        verificationMethodText: buildVerificationMethodText(selectedPaymentMethod, 'face')
      })
      if (result.payAmount == null) {
        this.calculatePayAmount()
      }
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
      this.setData({
        address: address
          ? {
              ...address,
              contactPhoneDisplay: formatPhoneDisplay(address.contactPhone || address.receiverPhone)
            }
          : address
      })
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
    const items = (this.data.availableCoupons || []).map(coupon => {
      const amount = coupon.couponType === 2
        ? `${coupon.discountRate}折`
        : `减${coupon.discountAmount || 0}元`
      return `${coupon.couponName || '优惠券'} · ${amount}`
    })

    if (!items.length) {
      wx.showToast({ title: '暂无可用优惠券', icon: 'none' })
      return
    }

    wx.showActionSheet({
      itemList: [...items, '不使用优惠券'],
      success: res => {
        if (res.tapIndex === items.length) {
          this.notUseCoupon()
          return
        }

        const coupon = this.data.availableCoupons[res.tapIndex]
        if (!coupon) {
          return
        }

        this.setData({
          selectedCoupon: coupon,
          selectedCouponId: coupon.id
        })
        this.calculateCouponDiscount(coupon)
      }
    })
  },

  // 不使用优惠券
  notUseCoupon() {
    this.setData({
      selectedCoupon: null,
      selectedCouponId: null,
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

  submitOrder() {
    if (!this.data.address) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' })
      return
    }

    this.setData({
      showPaymentSheet: true,
      showVerification: false,
      verificationStage: 'face',
      paymentPassword: '',
      verificationMode: 'face',
      verificationMethodText: buildVerificationMethodText(this.data.selectedPaymentMethod, 'face')
    })
  },

  closePaymentLayers() {
    this.setData({
      showPaymentSheet: false,
      showVerification: false,
      verificationStage: 'face',
      paymentPassword: '',
      verificationMethodText: buildVerificationMethodText(this.data.selectedPaymentMethod, 'face')
    })
  },

  selectPaymentMethod(e) {
    const { method } = e.currentTarget.dataset
    this.setData({
      selectedPaymentMethod: method,
      selectedPaymentLabel: getPaymentMethodLabel(method),
      verificationMode: resolveVerificationMode(this.data.paymentMethods, method, this.data.verificationMode),
      verificationMethodText: buildVerificationMethodText(method, this.data.verificationStage)
    })
  },

  confirmPaymentMethod() {
    this.setData({
      showPaymentSheet: false,
      showVerification: true,
      verificationStage: 'face',
      paymentPassword: '',
      verificationMode: 'face',
      verificationMethodText: buildVerificationMethodText(this.data.selectedPaymentMethod, 'face')
    })
  },

  completeFaceVerification() {
    this.setData({
      verificationStage: 'password',
      verificationMode: 'password',
      paymentPassword: '',
      verificationMethodText: buildVerificationMethodText(this.data.selectedPaymentMethod, 'password')
    })
  },

  async handlePasswordKey(e) {
    const key = e.currentTarget.dataset.key
    if (!key) return

    if (key === 'face') {
      this.setData({
        verificationStage: 'face',
        verificationMode: 'face',
        paymentPassword: '',
        verificationMethodText: buildVerificationMethodText(this.data.selectedPaymentMethod, 'face')
      })
      return
    }

    const nextPassword = applyPasswordKey(this.data.paymentPassword, key)
    if (nextPassword === this.data.paymentPassword) {
      return
    }

    this.setData({
      paymentPassword: nextPassword
    })

    if (nextPassword.length === 6) {
      await this.submitVerifiedOrder(nextPassword)
    }
  },

  async submitVerifiedOrder(paymentPassword) {
    if (!this.data.address) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '提交中...' })
      const couponId = this.data.selectedCoupon
        ? (this.data.selectedCouponId == null ? null : Number(this.data.selectedCouponId))
        : null
      const normalizedCouponId = Number.isNaN(couponId) ? null : couponId
      const orderId = await OrderAPI.submit({
        productIds: this.data.productIds,
        quantities: this.data.quantities,
        cartIds: this.data.cartIds,
        specLabels: this.data.specLabels,
        addressId: this.data.address.id,
        couponId: normalizedCouponId,
        remark: this.data.remark,
        paymentMethod: this.data.selectedPaymentMethod,
        verificationType: 'password'
      })
      wx.hideLoading()
      this.setData({
        showPaymentSheet: false,
        showVerification: false,
        verificationStage: 'face',
        verificationMode: 'face',
        paymentPassword: '',
        verificationMethodText: buildVerificationMethodText(this.data.selectedPaymentMethod, 'face')
      })
      wx.redirectTo({
        url: `/pages/order/pay-result/index?status=success&orderId=${orderId}&amount=${this.data.payAmount}`
      })
    } catch (error) {
      console.error('提交订单失败:', error)
      wx.hideLoading()
      wx.showToast({ title: '提交失败', icon: 'none' })
    }
  }
})
