const { AddressAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')
const { buildAddressListState } = require('./address-presenter')

function buildConfirmCopy(from) {
  if (from === 'order') {
    return {
      buttonText: '确认并使用该地址',
      subText: '已切换到当前选中的地址'
    }
  }

  return {
    buttonText: '设为默认地址',
    subText: '可将当前选中的地址设为默认地址'
  }
}

Page({
  data: {
    from: '',
    addressList: [],
    defaultAddress: null,
    otherAddresses: [],
    activeAddressId: null,
    activeAddress: null,
    helperTitle: '默认地址会优先用于下单',
    helperSub: '从订单进入时，也可以直接切换并确认使用',
    addressCountText: '0 个地址',
    confirmButtonText: '设为默认地址',
    confirmSubText: '可将当前选中的地址设为默认地址'
  },

  onLoad(options) {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const from = options.from || ''
    const confirmCopy = buildConfirmCopy(from)
    this.setData({
      from,
      confirmButtonText: confirmCopy.buttonText,
      confirmSubText: confirmCopy.subText
    })
  },

  onShow() {
    if (!isLoggedIn()) {
      return
    }
    this.loadAddressList()
  },

  async loadAddressList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const list = await AddressAPI.getList()
      this.syncAddressState(list, this.data.activeAddressId)
    } catch (error) {
      console.error('加载地址列表失败:', error)
      wx.showToast({ title: '地址加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  syncAddressState(addressList, activeAddressId) {
    const nextState = buildAddressListState(addressList, activeAddressId)
    this.setData({
      addressList: nextState.addressList,
      defaultAddress: nextState.defaultAddress,
      otherAddresses: nextState.otherAddresses,
      activeAddress: nextState.activeAddress,
      activeAddressId: nextState.activeAddressId,
      addressCountText: `${nextState.addressList.length} 个地址`
    })
  },

  selectAddress(e) {
    const { id } = e.currentTarget.dataset
    this.syncAddressState(this.data.addressList, id)
  },

  addAddress() {
    wx.navigateTo({
      url: '/pages/address/edit'
    })
  },

  editAddress(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/address/edit?id=${id}`
    })
  },

  getActiveAddress() {
    return this.data.activeAddress
  },

  async confirmAddress() {
    const address = this.getActiveAddress()
    if (!address) {
      wx.showToast({ title: '请先新增地址', icon: 'none' })
      return
    }

    if (this.data.from === 'order') {
      const pages = getCurrentPages()
      const prevPage = pages[pages.length - 2]
      if (prevPage) {
        prevPage.setData({ address })
        wx.navigateBack()
        return
      }
    }

    if (address.isDefault) {
      wx.showToast({ title: '当前已是默认地址', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '设置中...' })
      await AddressAPI.setDefault(address.id)
      wx.showToast({ title: '默认地址已更新', icon: 'success' })
      await this.loadAddressList()
    } catch (error) {
      console.error('设置默认地址失败:', error)
      wx.showToast({ title: '设置失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  }
})
