// pages/coupon/center.js
const { CouponAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    activeTab: 0,
    allCouponList: [],
    couponList: []
  },

  onLoad() {
    this.loadCouponList()
  },

  // Tab切换
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    this.filterCouponList(tab)
  },

  // 根据Tab筛选优惠券
  filterCouponList(tab) {
    const { allCouponList } = this.data
    let filteredList = allCouponList

    // tab: 0=全部, 1=满减券(type=1), 2=折扣券(type=2)
    if (tab === 1) {
      filteredList = allCouponList.filter(item => item.type === 1)
    } else if (tab === 2) {
      filteredList = allCouponList.filter(item => item.type === 2)
    }

    this.setData({ couponList: filteredList })
  },

  // 加载优惠券列表
  async loadCouponList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const list = await CouponAPI.getList()
      this.setData({
        allCouponList: list || [],
        couponList: list || []
      })
    } catch (error) {
      console.error('加载优惠券失败:', error)
    } finally {
      wx.hideLoading()
    }
  },

  // 领取优惠券（需要登录）
  async receiveCoupon(e) {
    // 领取操作需要登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { id, canReceive } = e.currentTarget.dataset
    if (!canReceive) return

    try {
      wx.showLoading({ title: '领取中...' })
      await CouponAPI.receive(id)
      wx.showToast({ title: '领取成功', icon: 'success' })
      this.loadCouponList()
    } catch (error) {
      wx.hideLoading()
      wx.showToast({ title: error.message || '领取失败', icon: 'none' })
    }
  }
})
