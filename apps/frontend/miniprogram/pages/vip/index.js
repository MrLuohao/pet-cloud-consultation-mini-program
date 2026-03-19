const { AuthAPI, VipAPI } = require('../../utils/api')
const app = getApp()

Page({
  data: {
    loading: false,
    subscribing: false,
    showPayPanel: false,
    userInfo: {},
    isVip: false,
    savingAmount: '0.00',
    vipExpireDate: '',
    vipLevelName: '',
    selectedPlan: 1,

    plans: [
      {
        id: 'monthly',
        name: '月卡',
        price: '19.9',
        originalPrice: '29.9',
        unit: '30天',
        saveText: '',
        recommend: false
      },
      {
        id: 'quarterly',
        name: '季卡',
        price: '49.9',
        originalPrice: '89.7',
        unit: '90天',
        saveText: '省39.8',
        recommend: true
      },
      {
        id: 'yearly',
        name: '年卡',
        price: '149.9',
        originalPrice: '358.8',
        unit: '365天',
        saveText: '省208.9',
        recommend: false
      }
    ],

    benefits: [
      { id: 1, icon: '🏷️', name: '专属折扣', desc: '商品享专属折扣', color: 'purple' },
      { id: 2, icon: '🩺', name: '问诊优先', desc: '在线问诊优先响应', color: 'blue' },
      { id: 3, icon: '💬', name: '专属客服', desc: '会员专属服务通道', color: 'pink' },
      { id: 4, icon: '📚', name: '训练课程', desc: '精选课程解锁', color: 'violet' },
      { id: 5, icon: '🧼', name: '护理折扣', desc: '美容护理专属价', color: 'sky' },
      { id: 6, icon: '🎁', name: '会员活动', desc: '会员日专享福利', color: 'indigo' }
    ],

    faqs: [
      { q: '会员有效期如何计算？', a: '从开通当日起计算，续费时间会在原有效期基础上叠加。' },
      { q: '会员到期后权益还能用吗？', a: '到期后会员权益将暂停，重新开通后立即恢复。' },
      { q: '可以退款吗？', a: '开通后7天内未使用任何权益可申请退款。' }
    ]
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const userRes = await AuthAPI.getUserInfo()
      const userInfo = userRes || {}

      this.setData({
        userInfo,
        isVip: !!userInfo.isVip,
        vipExpireDate: userInfo.vipExpireDate || '',
        vipLevelName: userInfo.vipLevel || 'VIP',
        savingAmount: userInfo.savingAmount || '0.00'
      })
    } catch (error) {
      console.error('加载VIP数据失败:', error)
    } finally {
      this.setData({ loading: false })
    }
  },

  onPlanSelect(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ selectedPlan: index })
  },

  // 点击开通按钮 - 直接发起微信支付（Story 4.3）
  async onSubscribe() {
    if (this.data.subscribing) return
    const plan = this.data.plans[this.data.selectedPlan]
    this.setData({ subscribing: true })
    wx.showLoading({ title: '获取支付信息...' })
    try {
      // 1. 获取微信支付参数（planType 对应 monthly/quarterly/yearly）
      const planTypeMap = { 'monthly': 'monthly', 'quarterly': 'quarterly', 'yearly': 'yearly' }
      const planType = planTypeMap[plan.id] || 'monthly'
      const payParams = await VipAPI.pay(planType)
      wx.hideLoading()

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

      // 3. 支付成功后通知后端激活会员
      wx.showLoading({ title: '开通中...' })
      const userInfo = await VipAPI.subscribe(plan.id)
      this.setData({
        userInfo,
        isVip: !!userInfo.isVip,
        vipExpireDate: userInfo.vipExpireDate || '',
        vipLevelName: userInfo.vipLevel || 'VIP',
        savingAmount: userInfo.savingAmount || '0.00'
      })

      const mergedUserInfo = {
        ...(app.globalData.userInfo || {}),
        id: userInfo.id,
        nickname: userInfo.nickname,
        avatarUrl: userInfo.avatarUrl,
        isVip: userInfo.isVip,
        vipLevel: userInfo.vipLevel,
        vipExpireDate: userInfo.vipExpireDate,
        savingAmount: userInfo.savingAmount
      }
      app.globalData.userInfo = mergedUserInfo
      wx.setStorageSync('userInfo', mergedUserInfo)

      wx.showToast({ title: '开通成功', icon: 'success' })
      app.globalData._vipUpdated = true
      setTimeout(() => { wx.navigateBack({ delta: 1 }) }, 1500)
    } catch (error) {
      wx.hideLoading()
      if (error && error.errMsg && error.errMsg.includes('cancel')) {
        wx.showToast({ title: '已取消支付', icon: 'none' })
      } else {
        wx.showToast({ title: error.message || '开通失败，请重试', icon: 'none' })
      }
    } finally {
      wx.hideLoading()
      this.setData({ subscribing: false })
    }
  },

  // 隐藏支付面板（保留兼容）
  hidePayPanel() {
    this.setData({ showPayPanel: false })
  }
})
