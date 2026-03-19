// pages/coupon/my.js
const { CouponAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    activeTab: 0,
    couponList: []
  },

  onLoad() {
    // 我的优惠券页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadCouponList()
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
  },

  // 切换Tab
  switchTab(e) {
    const tab = parseInt(e.currentTarget.dataset.tab)
    this.setData({ activeTab: tab })
    this.loadCouponList()
  },

  // 加载优惠券列表
  async loadCouponList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const status = this.data.activeTab
      const list = await CouponAPI.getMyCoupons(status)
      this.setData({ couponList: list || [] })
    } catch (error) {
      console.error('加载优惠券失败:', error)
      this.setData({ couponList: [] })
    } finally {
      wx.hideLoading()
    }
  },

  // 使用优惠券 - 跳转到购物车
  useCoupon(e) {
    const coupon = e.currentTarget.dataset.item
    console.log('选中优惠券:', coupon)

    // 将选中的优惠券信息存储到全局
    const app = getApp()
    app.globalData = app.globalData || {}
    app.globalData.selectedCoupon = coupon

    // 跳转到购物车页面
    wx.navigateTo({
      url: '/pages/cart/cart'
    })
  },

  // 跳转到领券中心
  gotoCouponCenter() {
    wx.navigateTo({
      url: '/pages/coupon/center'
    })
  }
})
