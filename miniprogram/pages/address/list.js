// pages/address/list.js
const { AddressAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    addressList: [],
    from: '' // 来自哪个页面
  },

  onLoad(options) {
    // 地址列表页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    if (options.from) {
      this.setData({ from: options.from })
    }
    this.loadAddressList()
  },

  onShow() {
    // 未登录时不执行任何操作
    if (!isLoggedIn()) {
      return
    }
    this.loadAddressList()
  },

  // 加载地址列表
  async loadAddressList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const list = await AddressAPI.getList()
      this.setData({ addressList: list })
    } catch (error) {
      console.error('加载地址列表失败:', error)
    } finally {
      wx.hideLoading()
    }
  },

  // 返回上一页
  goBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      wx.navigateBack()
    } else {
      wx.switchTab({
        url: '/pages/user/user'
      })
    }
  },

  // 阻止冒泡
  stopPropagation() {},

  // 选择地址（从订单页进入时）
  selectAddress(e) {
    const { id } = e.currentTarget.dataset
    if (this.data.from === 'order') {
      const pages = getCurrentPages()
      const prevPage = pages[pages.length - 2]
      if (prevPage) {
        const address = this.data.addressList.find(a => a.id === id)
        prevPage.setData({ address })
        wx.navigateBack()
      }
    }
  },

  // 添加地址
  addAddress() {
    wx.navigateTo({
      url: '/pages/address/edit'
    })
  },

  // 编辑地址
  editAddress(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/address/edit?id=${id}`
    })
  },

  // 删除地址
  deleteAddress(e) {
    const { id } = e.currentTarget.dataset
    wx.showModal({
      title: '提示',
      content: '确定要删除这个地址吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await AddressAPI.delete(id)
            wx.showToast({ title: '删除成功', icon: 'success' })
            this.loadAddressList()
          } catch (error) {
            wx.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      }
    })
  }
})
