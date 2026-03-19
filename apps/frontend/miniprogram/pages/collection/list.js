// pages/collection/list.js
const { CollectionAPI, CartAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    collectionList: [],
    isEditMode: false,
    selectedIds: [],
    allSelected: false
  },

  onLoad() {
    // 收藏列表页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadCollectionList()
  },

  onShow() {
    // 未登录时不执行任何操作
    if (!isLoggedIn()) {
      return
    }
    this.loadCollectionList()
  },

  // 加载收藏列表
  async loadCollectionList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const list = await CollectionAPI.getList()

      // 处理数据
      const collectionList = (list || []).map(item => ({
        ...item,
        selected: false,
        priceInt: item.productPrice ? Math.floor(item.productPrice) : '0',
        priceDecimal: item.productPrice ? (item.productPrice.toFixed(2).split('.')[1]) : '00'
      }))

      this.setData({
        collectionList,
        selectedIds: [],
        allSelected: false
      })
    } catch (error) {
      console.error('加载收藏列表失败:', error)
      this.setData({ collectionList: [] })
    } finally {
      wx.hideLoading()
    }
  },

  // 切换编辑模式
  toggleEditMode() {
    this.setData({
      isEditMode: !this.data.isEditMode,
      selectedIds: [],
      allSelected: false,
      collectionList: this.data.collectionList.map(item => ({
        ...item,
        selected: false
      }))
    })
  },

  // 切换商品选中状态
  toggleSelect(e) {
    const { id } = e.currentTarget.dataset
    const collectionList = this.data.collectionList.map(item => {
      if (item.id === id) {
        return { ...item, selected: !item.selected }
      }
      return item
    })

    const selectedIds = collectionList.filter(item => item.selected).map(item => item.id)
    const allSelected = selectedIds.length === collectionList.length && collectionList.length > 0

    this.setData({
      collectionList,
      selectedIds,
      allSelected
    })
  },

  // 全选/取消全选
  toggleSelectAll() {
    const allSelected = !this.data.allSelected
    const collectionList = this.data.collectionList.map(item => ({
      ...item,
      selected: allSelected
    }))
    const selectedIds = allSelected ? collectionList.map(item => item.id) : []

    this.setData({
      collectionList,
      allSelected,
      selectedIds
    })
  },

  // 删除单个收藏
  async deleteItem(e) {
    const { id, productid } = e.currentTarget.dataset

    wx.showModal({
      title: '提示',
      content: '确定要取消收藏该商品吗？',
      confirmText: '确定',
      confirmColor: '#ff6b6b',
      success: async (res) => {
        if (res.confirm) {
          try {
            await CollectionAPI.remove(productid)
            const collectionList = this.data.collectionList.filter(item => item.id !== id)
            this.setData({ collectionList })
            wx.showToast({ title: '已取消收藏', icon: 'success' })
          } catch (error) {
            console.error('删除收藏失败:', error)
            wx.showToast({ title: '操作失败', icon: 'none' })
          }
        }
      }
    })
  },

  // 批量删除
  async batchDelete() {
    const selectedItems = this.data.collectionList.filter(item => item.selected)
    if (selectedItems.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' })
      return
    }

    wx.showModal({
      title: '提示',
      content: `确定要取消收藏这${selectedItems.length}件商品吗？`,
      confirmText: '确定',
      confirmColor: '#ff6b6b',
      success: async (res) => {
        if (res.confirm) {
          try {
            // 批量删除
            for (const item of selectedItems) {
              await CollectionAPI.remove(item.productId)
            }

            const collectionList = this.data.collectionList.filter(item => !item.selected)
            this.setData({
              collectionList,
              selectedIds: [],
              allSelected: false
            })
            wx.showToast({ title: '已取消收藏', icon: 'success' })
          } catch (error) {
            console.error('批量删除失败:', error)
            wx.showToast({ title: '操作失败', icon: 'none' })
          }
        }
      }
    })
  },

  // 加入购物车
  async addToCart(e) {
    const { productid } = e.currentTarget.dataset

    try {
      await CartAPI.add(productid, 1)
      wx.showToast({ title: '已加入购物车', icon: 'success' })
    } catch (error) {
      console.error('加入购物车失败:', error)
      wx.showToast({ title: '加入失败', icon: 'none' })
    }
  },

  // 批量加入购物车
  async batchAddToCart() {
    const selectedItems = this.data.collectionList.filter(item => item.selected)
    if (selectedItems.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '加入中...' })
      for (const item of selectedItems) {
        await CartAPI.add(item.productId, 1)
      }
      wx.hideLoading()
      wx.showToast({ title: '已加入购物车', icon: 'success' })
    } catch (error) {
      wx.hideLoading()
      console.error('批量加入购物车失败:', error)
      wx.showToast({ title: '部分商品加入失败', icon: 'none' })
    }
  },

  // 跳转商品详情
  gotoProduct(e) {
    const { productid } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/product/detail?id=${productid}`
    })
  },

  // 去商城逛逛
  gotoShop() {
    wx.switchTab({
      url: '/pages/shop/shop'
    })
  }
})
