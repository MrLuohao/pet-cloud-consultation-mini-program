const { CartAPI, RecommendationAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

function enrichCartItem(item) {
  const price = Number(item.price) || 0
  const [priceInt, priceDecimal] = price.toFixed(2).split('.')

  return {
    ...item,
    selected: Boolean(item.selected),
    name: item.name || item.productName || '',
    shopName: item.shopName || '伴宠云诊自营',
    shopId: item.shopId || 'official',
    spec: item.spec || '',
    stock: item.stock || 999,
    priceInt,
    priceDecimal
  }
}

function mapStructuredGroup(group) {
  const shopId = group.shopId || group.merchantId || 'official'
  const products = (group.items || group.products || []).map(enrichCartItem)

  return {
    shopId,
    shopName: group.shopName || group.merchantName || '伴宠云诊自营',
    serviceText: group.serviceText || (shopId === 'official' ? '包邮 · 正品保障' : '极速达'),
    allSelected: Boolean(group.allSelected),
    products
  }
}

function buildShopGroups(cartList) {
  const groups = {}

  cartList.forEach(item => {
    if (!groups[item.shopId]) {
      groups[item.shopId] = {
        shopId: item.shopId,
        shopName: item.shopName,
        serviceText: item.shopId === 'official' ? '包邮 · 正品保障' : '极速达',
        allSelected: true,
        products: []
      }
    }

    groups[item.shopId].products.push(item)
    if (!item.selected) {
      groups[item.shopId].allSelected = false
    }
  })

  return Object.values(groups)
}

function buildSummary(cartList) {
  const selectedItems = cartList.filter(item => item.selected)
  const originalTotal = selectedItems.reduce((sum, item) => {
    const original = Number(item.originalPrice || item.price) || 0
    return sum + original * item.quantity
  }, 0)
  const currentTotal = selectedItems.reduce((sum, item) => sum + (Number(item.price) || 0) * item.quantity, 0)
  const totalDiscount = Math.max(0, originalTotal - currentTotal)

  return {
    totalPrice: currentTotal.toFixed(2),
    totalDiscountDisplay: totalDiscount.toFixed(0),
    allSelected: cartList.length > 0 && selectedItems.length === cartList.length
  }
}

function buildSummaryFromPayload(summary, cartList) {
  if (!summary || typeof summary !== 'object') {
    return buildSummary(cartList)
  }

  return {
    totalPrice: (Number(summary.totalAmount) || 0).toFixed(2),
    totalDiscountDisplay: String(Math.round(Number(summary.totalDiscount) || 0)),
    allSelected: Boolean(summary.allSelected)
  }
}

Page({
  data: {
    cartList: [],
    groupedCartList: [],
    invalidList: [],
    allSelected: false,
    totalPrice: '0.00',
    totalDiscountDisplay: '0',
    recommendCards: []
  },

  onLoad() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    this.loadCartPage()
  },

  onShow() {
    if (!isLoggedIn()) {
      return
    }

    this.loadCartPage()
  },

  async loadCartPage() {
    await Promise.all([
      this.loadCartList(),
      this.loadRecommendList()
    ])
  },

  async loadCartList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const payload = await CartAPI.getList()
      let validList = []
      let invalidList = []
      let groupedCartList = []
      let summary = null

      if (Array.isArray(payload)) {
        payload.forEach(item => {
          const enriched = enrichCartItem(item)
          if (item.status === 'invalid' || item.stock === 0) {
            invalidList.push(enriched)
          } else {
            validList.push(enriched)
          }
        })
        groupedCartList = buildShopGroups(validList)
        summary = buildSummary(validList)
      } else {
        groupedCartList = (payload && Array.isArray(payload.cartGroups))
          ? payload.cartGroups.map(mapStructuredGroup)
          : []
        validList = groupedCartList.flatMap(group => group.products)
        invalidList = (payload && Array.isArray(payload.invalidItems))
          ? payload.invalidItems.map(enrichCartItem)
          : []
        summary = buildSummaryFromPayload(payload && payload.summary, validList)
      }

      this.setData({
        cartList: validList,
        invalidList,
        groupedCartList,
        ...summary
      })
    } catch (error) {
      console.error('加载购物车失败:', error)
      this.setData({
        cartList: [],
        groupedCartList: [],
        invalidList: [],
        allSelected: false,
        totalPrice: '0.00',
        totalDiscountDisplay: '0'
      })
    } finally {
      wx.hideLoading()
    }
  },

  async loadRecommendList() {
    try {
      const list = await RecommendationAPI.getByCart(5)
      const recommendCards = (list || []).slice(0, 2).map(item => ({
        id: item.id,
        name: item.name,
        price: item.price ? Number(item.price).toFixed(2) : '0.00',
        coverUrl: item.coverUrl || ''
      }))

      this.setData({ recommendCards })
    } catch (error) {
      console.error('加载推荐失败:', error)
      this.setData({ recommendCards: [] })
    }
  },

  syncCartView(cartList) {
    this.setData({
      cartList,
      groupedCartList: buildShopGroups(cartList),
      ...buildSummary(cartList)
    })
  },

  toggleSelect(e) {
    const { id } = e.currentTarget.dataset
    const cartList = this.data.cartList.map(item => (
      item.id === id ? { ...item, selected: !item.selected } : item
    ))
    this.syncCartView(cartList)
  },

  toggleSelectAll() {
    const nextSelected = !this.data.allSelected
    const cartList = this.data.cartList.map(item => ({
      ...item,
      selected: nextSelected
    }))
    this.syncCartView(cartList)
  },

  async increaseQuantity(e) {
    const { id } = e.currentTarget.dataset
    const item = this.data.cartList.find(entry => entry.id === id)
    if (!item) {
      return
    }

    const nextQuantity = Math.min(item.quantity + 1, item.stock)
    if (nextQuantity === item.quantity) {
      wx.showToast({ title: '已达库存上限', icon: 'none' })
      return
    }

    try {
      await CartAPI.updateQuantity(id, nextQuantity)
      const cartList = this.data.cartList.map(entry => (
        entry.id === id ? { ...entry, quantity: nextQuantity } : entry
      ))
      this.syncCartView(cartList)
    } catch (error) {
      wx.showToast({ title: '更新失败', icon: 'none' })
    }
  },

  async decreaseQuantity(e) {
    const { id } = e.currentTarget.dataset
    const item = this.data.cartList.find(entry => entry.id === id)
    if (!item || item.quantity <= 1) {
      return
    }

    try {
      await CartAPI.updateQuantity(id, item.quantity - 1)
      const cartList = this.data.cartList.map(entry => (
        entry.id === id ? { ...entry, quantity: entry.quantity - 1 } : entry
      ))
      this.syncCartView(cartList)
    } catch (error) {
      wx.showToast({ title: '更新失败', icon: 'none' })
    }
  },

  gotoShop() {
    wx.switchTab({
      url: '/pages/shop/shop'
    })
  },

  gotoProduct(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/product/detail?id=${id}`
    })
  },

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

  checkout() {
    const selectedItems = this.data.cartList.filter(item => item.selected)
    if (!selectedItems.length) {
      wx.showToast({ title: '请选择商品', icon: 'none' })
      return
    }

    const productIds = selectedItems.map(item => item.productId)
    const quantities = selectedItems.map(item => item.quantity)
    const cartIds = selectedItems.map(item => item.id)
    const query = [
      `productIds=${encodeURIComponent(JSON.stringify(productIds))}`,
      `quantities=${encodeURIComponent(JSON.stringify(quantities))}`,
      `cartIds=${encodeURIComponent(JSON.stringify(cartIds))}`
    ].join('&')

    wx.navigateTo({
      url: `/pages/order/confirm?${query}`
    })
  }
})
