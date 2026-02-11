/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  购物车页面逻辑 | Cart Page Logic
 * ║  Feature-Rich Cart with Industry Best Practices
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const { CartAPI, ProductAPI, RecommendationAPI, CouponAPI, PromotionAPI, CollectionAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    // 购物车数据
    cartList: [],
    groupedCartList: [],
    invalidList: [],

    // 选择状态
    allSelected: false,
    selectedCount: 0,

    // 价格信息
    totalPrice: '0.00',
    totalOriginalPrice: 0,
    totalDiscount: 0,
    totalSaved: '0.00',       // 总节省金额（折扣+优惠券）
    estimatedShipping: 0,
    estimatedDelivery: '预计明天送达',

    // 促销信息
    promoInfo: {
      hasPromo: false,
      promotions: [],      // 所有满减活动
      currentPromo: null,  // 当前最优满减
      threshold: 0,
      discount: 0,
      gap: 0,
      progress: 0,
      achieved: false
    },

    // 限时优惠
    flashSaleCount: 0,
    flashCountdown: '',

    // 优惠券
    availableCouponCount: 0,
    availableCoupons: [],
    couponInfoText: '',
    selectedCoupon: null,      // 选中的优惠券
    couponDiscount: 0,         // 优惠券优惠金额

    // 推荐商品
    recommendList: [],

    // UI状态
    isEditMode: false,
    showSpecPopup: false,
    currentProduct: {}
  },

  onLoad() {
    // 购物车页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    this.loadRecommendList()
    // 先加载满减活动和优惠券，再加载购物车
    Promise.all([
      this.loadPromotions(),
      this.loadAvailableCoupons()
    ]).then(() => {
      this.loadCartList()
    })
  },

  onShow() {
    // 未登录时不执行任何操作（避免返回时死循环）
    if (!isLoggedIn()) {
      return
    }

    // 检查是否有从优惠券页面选中的优惠券
    this.checkSelectedCoupon()

    Promise.all([
      this.loadPromotions(),
      this.loadAvailableCoupons()
    ]).then(() => {
      this.loadCartList()
    })
  },

  /**
   * 检查全局存储的选中优惠券
   */
  checkSelectedCoupon() {
    const app = getApp()
    if (app.globalData && app.globalData.selectedCoupon) {
      this.setData({
        selectedCoupon: app.globalData.selectedCoupon
      })
      // 清除全局存储，避免重复使用
      app.globalData.selectedCoupon = null
    }
  },

  /**
   * 加载购物车列表
   */
  async loadCartList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const list = await CartAPI.getList()

      // 分离有效和失效商品
      const validList = []
      const invalidList = []

      ;(list || []).forEach(item => {
        const enrichedItem = this.enrichProductData(item)
        if (item.status === 'invalid' || item.stock === 0) {
          invalidList.push({
            ...enrichedItem,
            invalidReason: item.stock === 0 ? '已售罄' : '已下架'
          })
        } else {
          validList.push(enrichedItem)
        }
      })

      // 统计限时优惠商品
      const flashSaleCount = validList.filter(item => item.isFlashSale).length

      this.setData({
        cartList: validList,
        invalidList,
        flashSaleCount
      })

      this.groupByShop()
      this.calculateTotal()
      this.updatePromoInfo()

      // 启动限时优惠倒计时
      if (flashSaleCount > 0) {
        this.startFlashCountdown()
      }
    } catch (error) {
      console.error('加载购物车失败:', error)
      this.setData({
        cartList: [],
        groupedCartList: [],
        invalidList: []
      })
    } finally {
      wx.hideLoading()
    }
  },

  /**
   * 丰富商品数据（添加展示所需的额外字段）
   */
  enrichProductData(item) {
    const price = parseFloat(item.price) || 0
    const priceStr = price.toFixed(2)
    const [priceInt, priceDecimal] = priceStr.split('.')

    return {
      ...item,
      selected: false,
      // 商品名称（兼容后端字段）
      name: item.name || item.productName || '',
      // 分类名称
      categoryName: item.categoryName || '',
      // 店铺信息
      shopName: item.shopName || '伴宠云诊官方商城',
      shopId: item.shopId || 'official',
      // 价格处理
      priceInt,
      priceDecimal,
      originalPrice: item.originalPrice || price,
      priceDrop: item.priceDrop || 0,
      // 规格
      spec: item.spec || '',
      specGroups: item.specGroups || [],
      // 库存
      stock: item.stock || 999,
      stockStatus: item.stock && item.stock < 10 ? 'low' : 'normal',
      // 标签
      badge: item.badge || '',
      badgeType: item.badgeType || 'hot',
      isHot: item.isHot || false,
      isNew: item.isNew || false,
      isOnSale: item.isOnSale || false,
      isFlashSale: item.isFlashSale || false,
      // 服务
      freeShipping: item.freeShipping !== false,
      guarantee: item.guarantee !== false,
      returnPolicy: item.returnPolicy !== false,
      // 评分销量
      rating: item.rating || '4.9',
      salesCount: item.salesCount || '',
      deliveryTime: item.deliveryTime || '预计明天送达',
      // 优惠标签
      promoTags: item.promoTags || this.generatePromoTags(item)
    }
  },

  /**
   * 生成优惠标签
   */
  generatePromoTags(item) {
    const tags = []
    if (item.isFlashSale) tags.push('限时特价')
    if (item.originalPrice > item.price) tags.push('直降' + (item.originalPrice - item.price).toFixed(0))
    return tags.slice(0, 2)
  },

  /**
   * 格式化销量
   */
  formatSales(num) {
    if (num >= 10000) {
      return (num / 10000).toFixed(1) + '万'
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'k'
    }
    return num + ''
  },

  /**
   * 按店铺分组
   */
  groupByShop() {
    const shopMap = {}
    const { availableCouponCount, couponInfoText } = this.data

    this.data.cartList.forEach(item => {
      if (!shopMap[item.shopId]) {
        shopMap[item.shopId] = {
          shopId: item.shopId,
          shopName: item.shopName,
          products: [],
          allSelected: true,
          selectedCount: 0,
          subtotal: '0.00',
          couponCount: availableCouponCount,
          couponInfo: couponInfoText
        }
      }
      shopMap[item.shopId].products.push(item)
      if (!item.selected) {
        shopMap[item.shopId].allSelected = false
      } else {
        shopMap[item.shopId].selectedCount++
        const currentSubtotal = parseFloat(shopMap[item.shopId].subtotal) || 0
        shopMap[item.shopId].subtotal = (currentSubtotal + parseFloat(item.price) * item.quantity).toFixed(2)
      }
    })
    const groupedCartList = Object.values(shopMap)
    this.setData({ groupedCartList })
  },

  /**
   * 加载满减活动
   */
  async loadPromotions() {
    try {
      const list = await PromotionAPI.getActivePromotions()
      const promotions = (list || []).map(p => ({
        id: p.id,
        name: p.name,
        threshold: parseFloat(p.threshold) || 0,
        discount: parseFloat(p.discount) || 0,
        ruleDesc: p.ruleDesc || `满${p.threshold}减${p.discount}`
      }))
      // 按门槛金额升序排序
      promotions.sort((a, b) => a.threshold - b.threshold)

      this.setData({
        'promoInfo.promotions': promotions,
        'promoInfo.hasPromo': promotions.length > 0
      })
    } catch (error) {
      console.error('加载满减活动失败:', error)
      this.setData({
        'promoInfo.promotions': [],
        'promoInfo.hasPromo': false
      })
    }
  },

  /**
   * 更新满减促销信息
   * 根据当前购物车金额，计算最优满减活动
   */
  updatePromoInfo() {
    const total = parseFloat(this.data.totalPrice) || 0
    const promotions = this.data.promoInfo.promotions || []

    if (promotions.length === 0) {
      this.setData({
        promoInfo: {
          ...this.data.promoInfo,
          hasPromo: false,
          currentPromo: null,
          threshold: 0,
          discount: 0,
          gap: 0,
          progress: 0,
          achieved: false
        }
      })
      return
    }

    // 找到下一个可达到的满减门槛（或者已达到的最高门槛）
    let targetPromo = null
    let achieved = false

    // 先找已达到的最高档位
    const achievedPromos = promotions.filter(p => total >= p.threshold)
    if (achievedPromos.length > 0) {
      // 已达到某个门槛，显示下一个更高的门槛（如果有）
      const highestAchieved = achievedPromos[achievedPromos.length - 1]
      const nextPromo = promotions.find(p => p.threshold > highestAchieved.threshold)
      if (nextPromo) {
        targetPromo = nextPromo
        achieved = false
      } else {
        // 已达到最高档位
        targetPromo = highestAchieved
        achieved = true
      }
    } else {
      // 还没达到任何门槛，显示最低门槛
      targetPromo = promotions[0]
      achieved = false
    }

    const threshold = targetPromo.threshold
    const discount = targetPromo.discount
    const gap = achieved ? 0 : Math.max(0, threshold - total)
    const progress = Math.min(100, (total / threshold) * 100)

    this.setData({
      promoInfo: {
        ...this.data.promoInfo,
        currentPromo: targetPromo,
        threshold,
        discount,
        gap: gap.toFixed(2),
        progress: progress.toFixed(0),
        achieved
      }
    })
  },

  /**
   * 启动限时优惠倒计时
   */
  startFlashCountdown() {
    // 模拟2小时倒计时
    let totalSeconds = 2 * 60 * 60

    const updateCountdown = () => {
      if (totalSeconds <= 0) {
        this.setData({ flashCountdown: '' })
        return
      }

      const hours = Math.floor(totalSeconds / 3600)
      const minutes = Math.floor((totalSeconds % 3600) / 60)
      const seconds = totalSeconds % 60

      const countdown = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
      this.setData({ flashCountdown: countdown })

      totalSeconds--
    }

    updateCountdown()
    this.countdownTimer = setInterval(updateCountdown, 1000)
  },

  /**
   * 格式化销量文本，保留整数部分
   * 例如：月销4.5万+ -> 月销4万+，月销3.2千+ -> 月销3千+
   */
  formatSalesText(text) {
    if (!text) return ''

    // 匹配 X.X万 格式，保留整数部分
    const wanMatch = text.match(/(\d+\.?\d*)万/)
    if (wanMatch) {
      const num = Math.floor(parseFloat(wanMatch[1]))
      return text.replace(/\d+\.?\d*万/, num + '万')
    }

    // 匹配 X.X千 格式，保留整数部分
    const qianMatch = text.match(/(\d+\.?\d*)千/)
    if (qianMatch) {
      const num = Math.floor(parseFloat(qianMatch[1]))
      return text.replace(/\d+\.?\d*千/, num + '千')
    }

    return text
  },

  /**
   * 加载推荐商品
   */
  async loadRecommendList() {
    try {
      // 调用后端推荐API获取推荐商品
      const list = await RecommendationAPI.getByCart(5)

      // 处理推荐商品数据
      const recommendList = (list || []).map(item => ({
        id: item.id,
        name: item.name,
        price: item.price ? parseFloat(item.price).toFixed(2) : '0.00',
        coverUrl: item.coverUrl || '/image/product-placeholder.png',
        badge: item.badge || '',
        salesText: this.formatSalesText(item.salesText || '')
      }))

      this.setData({ recommendList })
    } catch (error) {
      console.error('加载推荐失败:', error)
      // 失败时使用空列表
      this.setData({ recommendList: [] })
    }
  },

  /**
   * 加载可用优惠券
   */
  async loadAvailableCoupons() {
    try {
      // 获取可用优惠券列表（status=0表示未使用）
      const list = await CouponAPI.getMyCoupons(0)
      const coupons = list || []

      // 生成优惠券描述文字
      const couponInfoText = this.generateCouponInfoText(coupons)

      this.setData({
        availableCouponCount: coupons.length,
        availableCoupons: coupons,
        couponInfoText: couponInfoText
      })
    } catch (error) {
      console.error('加载可用优惠券失败:', error)
      this.setData({
        availableCouponCount: 0,
        availableCoupons: [],
        couponInfoText: ''
      })
    }
  },

  /**
   * 生成优惠券描述文字
   * 例如：满50减10，满100减20
   */
  generateCouponInfoText(coupons) {
    if (!coupons || coupons.length === 0) return ''

    // 筛选满减券并按最低金额排序
    const fullReductionCoupons = coupons
      .filter(c => c.couponType === 1 && c.discountAmount)
      .sort((a, b) => (a.minAmount || 0) - (b.minAmount || 0))

    // 取前2张满减券生成描述
    const descriptions = fullReductionCoupons.slice(0, 2).map(c => {
      const minAmount = c.minAmount ? Math.floor(c.minAmount) : 0
      const discount = c.discountAmount ? Math.floor(c.discountAmount) : 0
      return `满${minAmount}减${discount}`
    })

    // 如果有折扣券，也加上
    const discountCoupons = coupons.filter(c => c.couponType === 2 && c.discountRate)
    if (discountCoupons.length > 0 && descriptions.length < 2) {
      const dc = discountCoupons[0]
      descriptions.push(`${dc.discountRate}折券`)
    }

    return descriptions.join('，')
  },

  /**
   * 切换编辑模式
   */
  toggleEditMode() {
    this.setData({
      isEditMode: !this.data.isEditMode
    })
  },

  /**
   * 切换商品选中状态
   */
  toggleSelect(e) {
    const { id } = e.currentTarget.dataset
    const cartList = this.data.cartList.map(item => {
      if (item.id === id) {
        return { ...item, selected: !item.selected }
      }
      return item
    })
    this.setData({ cartList })
    this.groupByShop()
    this.calculateTotal()
    this.updatePromoInfo()
  },

  /**
   * 切换店铺全选
   */
  toggleShopSelect(e) {
    const { shopid } = e.currentTarget.dataset
    const shop = this.data.groupedCartList.find(s => s.shopId === shopid)
    if (!shop) return

    const newAllSelected = !shop.allSelected
    const cartList = this.data.cartList.map(item => {
      if (item.shopId === shopid) {
        return { ...item, selected: newAllSelected }
      }
      return item
    })
    this.setData({ cartList })
    this.groupByShop()
    this.calculateTotal()
    this.updatePromoInfo()
  },

  /**
   * 全选/取消全选
   */
  toggleSelectAll() {
    const allSelected = !this.data.allSelected
    const cartList = this.data.cartList.map(item => ({
      ...item,
      selected: allSelected
    }))
    this.setData({ cartList, allSelected })
    this.groupByShop()
    this.calculateTotal()
    this.updatePromoInfo()
  },

  /**
   * 增加数量
   */
  async increaseQuantity(e) {
    const { id } = e.currentTarget.dataset
    const item = this.data.cartList.find(i => i.id === id)
    if (item && item.quantity < item.stock) {
      try {
        await CartAPI.updateQuantity(id, item.quantity + 1)
        const cartList = this.data.cartList.map(i => {
          if (i.id === id) {
            return { ...i, quantity: i.quantity + 1 }
          }
          return i
        })
        this.setData({ cartList })
        this.groupByShop()
        this.calculateTotal()
        this.updatePromoInfo()
      } catch (error) {
        wx.showToast({ title: '更新失败', icon: 'none' })
      }
    } else if (item && item.quantity >= item.stock) {
      wx.showToast({ title: '已达库存上限', icon: 'none' })
    }
  },

  /**
   * 减少数量
   */
  async decreaseQuantity(e) {
    const { id } = e.currentTarget.dataset
    const item = this.data.cartList.find(i => i.id === id)
    if (item && item.quantity > 1) {
      try {
        await CartAPI.updateQuantity(id, item.quantity - 1)
        const cartList = this.data.cartList.map(i => {
          if (i.id === id) {
            return { ...i, quantity: i.quantity - 1 }
          }
          return i
        })
        this.setData({ cartList })
        this.groupByShop()
        this.calculateTotal()
        this.updatePromoInfo()
      } catch (error) {
        wx.showToast({ title: '更新失败', icon: 'none' })
      }
    }
  },

  /**
   * 数量输入
   */
  onQuantityInput(e) {
    // 实时输入不做处理，等blur时处理
  },

  /**
   * 数量输入完成
   */
  async onQuantityBlur(e) {
    const { id } = e.currentTarget.dataset
    let value = parseInt(e.detail.value) || 1
    const item = this.data.cartList.find(i => i.id === id)

    if (!item) return

    // 限制范围
    value = Math.max(1, Math.min(value, item.stock))

    if (value !== item.quantity) {
      try {
        await CartAPI.updateQuantity(id, value)
        const cartList = this.data.cartList.map(i => {
          if (i.id === id) {
            return { ...i, quantity: value }
          }
          return i
        })
        this.setData({ cartList })
        this.groupByShop()
        this.calculateTotal()
        this.updatePromoInfo()
      } catch (error) {
        wx.showToast({ title: '更新失败', icon: 'none' })
      }
    }
  },

  /**
   * 删除商品
   */
  deleteItem(e) {
    const { id } = e.currentTarget.dataset
    wx.showModal({
      title: '提示',
      content: '确定要删除这个商品吗？',
      confirmText: '删除',
      confirmColor: '#FF6B35',
      success: async (res) => {
        if (res.confirm) {
          try {
            await CartAPI.delete(id)
            const cartList = this.data.cartList.filter(i => i.id !== id)
            this.setData({ cartList })
            this.groupByShop()
            this.calculateTotal()
            this.updatePromoInfo()
            wx.showToast({ title: '已删除', icon: 'success' })
          } catch (error) {
            wx.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      }
    })
  },

  /**
   * 加入收藏
   */
  moveToCollect(e) {
    const { id } = e.currentTarget.dataset
    const item = this.data.cartList.find(i => i.id === id)
    if (!item) return

    const productId = item.productId || item.id

    wx.showModal({
      title: '加入收藏',
      content: '确定要将该商品加入收藏吗？',
      confirmText: '确定',
      confirmColor: '#667eea',
      success: async (res) => {
        if (res.confirm) {
          try {
            await CollectionAPI.add(productId)
            wx.showToast({ title: '已加入收藏', icon: 'success' })
          } catch (error) {
            console.error('加入收藏失败:', error)
            // 如果已经收藏过，也提示成功
            if (error && error.includes && error.includes('已收藏')) {
              wx.showToast({ title: '该商品已在收藏中', icon: 'none' })
            } else {
              wx.showToast({ title: '操作失败', icon: 'none' })
            }
          }
        }
      }
    })
  },

  /**
   * 找相似
   */
  findSimilar(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/search/result?similar=${id}`
    })
  },

  /**
   * 清理失效商品
   */
  clearInvalid() {
    wx.showModal({
      title: '提示',
      content: '确定要清理所有失效商品吗？',
      confirmText: '清理',
      confirmColor: '#FF6B35',
      success: (res) => {
        if (res.confirm) {
          this.setData({ invalidList: [] })
          wx.showToast({ title: '已清理', icon: 'success' })
        }
      }
    })
  },

  /**
   * 显示规格选择器
   */
  showSpecPicker(e) {
    const { product } = e.currentTarget.dataset
    // 模拟规格数据
    const specGroups = product.specGroups && product.specGroups.length > 0 ? product.specGroups : [
      {
        name: '规格',
        options: [
          { value: '500g', selected: true, disabled: false },
          { value: '1kg', selected: false, disabled: false },
          { value: '2.5kg', selected: false, disabled: false }
        ]
      },
      {
        name: '口味',
        options: [
          { value: '鸡肉味', selected: true, disabled: false },
          { value: '牛肉味', selected: false, disabled: false },
          { value: '三文鱼味', selected: false, disabled: true }
        ]
      }
    ]

    this.setData({
      showSpecPopup: true,
      currentProduct: {
        ...product,
        specGroups
      }
    })
  },

  /**
   * 隐藏规格选择器
   */
  hideSpecPopup() {
    this.setData({ showSpecPopup: false })
  },

  /**
   * 选择规格
   */
  selectSpec(e) {
    const { group, value } = e.currentTarget.dataset
    const specGroups = this.data.currentProduct.specGroups.map(g => {
      if (g.name === group) {
        return {
          ...g,
          options: g.options.map(opt => ({
            ...opt,
            selected: opt.value === value && !opt.disabled
          }))
        }
      }
      return g
    })

    // 更新已选规格文本
    const selectedSpecs = specGroups
      .map(g => g.options.find(o => o.selected)?.value)
      .filter(Boolean)
      .join(' / ')

    this.setData({
      currentProduct: {
        ...this.data.currentProduct,
        specGroups,
        spec: selectedSpecs
      }
    })
  },

  /**
   * 确认规格
   */
  async confirmSpec() {
    const { currentProduct } = this.data
    try {
      await CartAPI.updateSpec(currentProduct.id, currentProduct.spec)
      const cartList = this.data.cartList.map(item => {
        if (item.id === currentProduct.id) {
          return { ...item, spec: currentProduct.spec }
        }
        return item
      })
      this.setData({
        cartList,
        showSpecPopup: false
      })
      this.groupByShop()
      wx.showToast({ title: '规格已更新', icon: 'success' })
    } catch (error) {
      wx.showToast({ title: '更新失败', icon: 'none' })
    }
  },

  /**
   * 阻止冒泡
   */
  preventBubble() {
    // 阻止事件冒泡
  },

  /**
   * 显示店铺优惠券
   */
  showShopCoupon(e) {
    const { shopid } = e.currentTarget.dataset
    wx.showToast({
      title: '领取优惠券',
      icon: 'none'
    })
  },

  /**
   * 显示优惠券选择器
   */
  showCouponPicker() {
    wx.navigateTo({
      url: '/pages/coupon/my?select=1'
    })
  },

  /**
   * 清除选中的优惠券
   */
  clearCoupon() {
    this.setData({
      selectedCoupon: null,
      couponDiscount: 0
    })
    this.calculateTotal()
    wx.showToast({
      title: '已取消优惠券',
      icon: 'none'
    })
  },

  /**
   * 去凑单
   */
  gotoPromoProducts() {
    wx.switchTab({
      url: '/pages/shop/shop'
    })
  },

  /**
   * 去逛逛
   */
  gotoShop() {
    wx.switchTab({
      url: '/pages/shop/shop'
    })
  },

  /**
   * 去收藏
   */
  gotoCollect() {
    wx.navigateTo({
      url: '/pages/collection/list'
    })
  },

  /**
   * 跳转商品详情
   */
  gotoProduct(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/product/detail?id=${id}`
    })
  },

  /**
   * 跳转店铺详情
   */
  gotoShopDetail(e) {
    const { shopid } = e.currentTarget.dataset
    wx.showToast({
      title: '店铺详情',
      icon: 'none'
    })
  },

  /**
   * 查看更多推荐
   */
  gotoRecommend() {
    wx.switchTab({
      url: '/pages/shop/shop'
    })
  },

  /**
   * 快速加入购物车
   */
  async quickAddCart(e) {
    const { product } = e.currentTarget.dataset
    try {
      await CartAPI.add(product.id, 1)
      wx.showToast({ title: '已加入购物车', icon: 'success' })
      this.loadCartList()
    } catch (error) {
      wx.showToast({ title: '添加失败', icon: 'none' })
    }
  },

  /**
   * 结算
   */
  checkout() {
    const selectedItems = this.data.cartList.filter(item => item.selected)
    if (selectedItems.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' })
      return
    }

    const productIds = selectedItems.map(item => item.productId)
    const quantities = selectedItems.map(item => item.quantity)

    wx.navigateTo({
      url: `/pages/order/confirm?productIds=${JSON.stringify(productIds)}&quantities=${JSON.stringify(quantities)}`
    })
  },

  /**
   * 计算总价和优惠
   */
  calculateTotal() {
    const selectedItems = this.data.cartList.filter(item => item.selected)
    let totalPrice = 0
    let totalOriginalPrice = 0

    selectedItems.forEach(item => {
      const original = parseFloat(item.originalPrice) || parseFloat(item.price)
      const current = parseFloat(item.price)
      totalOriginalPrice += original * item.quantity
      totalPrice += current * item.quantity
    })

    const totalDiscount = totalOriginalPrice - totalPrice
    const selectedCount = selectedItems.length
    const allSelected = selectedCount === this.data.cartList.length && this.data.cartList.length > 0

    // 计算运费
    const estimatedShipping = totalPrice >= 99 ? 0 : 8

    // 计算优惠券优惠金额
    let couponDiscount = 0
    let selectedCoupon = this.data.selectedCoupon

    if (selectedCoupon) {
      const minAmount = parseFloat(selectedCoupon.minAmount) || 0
      if (totalPrice >= minAmount) {
        // 满足使用条件
        if (selectedCoupon.couponType === 1) {
          // 满减券
          couponDiscount = parseFloat(selectedCoupon.discountAmount) || 0
        } else if (selectedCoupon.couponType === 2) {
          // 折扣券
          const rate = parseFloat(selectedCoupon.discountRate) || 10
          couponDiscount = totalPrice * (1 - rate / 10)
          // 检查最大优惠金额
          const maxDiscount = parseFloat(selectedCoupon.maxDiscount) || 0
          if (maxDiscount > 0 && couponDiscount > maxDiscount) {
            couponDiscount = maxDiscount
          }
        }
      } else {
        // 不满足使用条件，清除选中的优惠券
        selectedCoupon = null
      }
    }

    // 计算最终价格
    const finalPrice = Math.max(0, totalPrice - couponDiscount)

    // 计算总节省金额
    const totalSaved = totalDiscount + couponDiscount

    this.setData({
      totalPrice: finalPrice.toFixed(2),
      totalOriginalPrice: totalOriginalPrice.toFixed(2),
      totalDiscount: totalDiscount.toFixed(2),
      totalSaved: totalSaved.toFixed(2),
      selectedCount,
      allSelected,
      estimatedShipping,
      couponDiscount: couponDiscount.toFixed(2),
      selectedCoupon
    })
  },

  /**
   * 页面卸载
   */
  onUnload() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
    }
  }
})
