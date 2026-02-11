// pages/address/edit.js
const { AddressAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    isEdit: false,
    addressId: null,
    region: ['北京市', '北京市', '朝阳区'],
    formData: {
      contactName: '',
      contactPhone: '',
      province: '北京市',
      city: '北京市',
      district: '朝阳区',
      detailAddress: '',
      isDefault: false
    }
  },

  onLoad(options) {
    // 地址编辑页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    if (options.id) {
      this.setData({ addressId: options.id, isEdit: true })
      this.loadAddressDetail()
    }
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
  },

  // 加载地址详情
  async loadAddressDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      const address = await AddressAPI.getDetail(this.data.addressId)
      this.setData({
        region: [address.province, address.city, address.district],
        formData: {
          contactName: address.contactName,
          contactPhone: address.contactPhone,
          province: address.province,
          city: address.city,
          district: address.district,
          detailAddress: address.detailAddress,
          isDefault: address.isDefault
        }
      })
    } catch (error) {
      console.error('加载地址详情失败:', error)
    } finally {
      wx.hideLoading()
    }
  },

  // 输入变化
  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    const value = e.detail.value
    this.setData({
      [`formData.${field}`]: value
    })
  },

  // 地区选择
  onRegionChange(e) {
    const region = e.detail.value
    this.setData({
      region,
      'formData.province': region[0],
      'formData.city': region[1],
      'formData.district': region[2]
    })
  },

  // 默认地址开关
  onDefaultChange(e) {
    this.setData({
      'formData.isDefault': e.detail.value
    })
  },

  // 保存地址
  async saveAddress() {
    const formData = this.data.formData

    if (!formData.contactName) {
      wx.showToast({ title: '请输入联系人', icon: 'none' })
      return
    }
    if (!formData.contactPhone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' })
      return
    }
    if (!/^1[3-9]\d{9}$/.test(formData.contactPhone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }
    if (!formData.detailAddress) {
      wx.showToast({ title: '请输入详细地址', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '保存中...' })
      const isDefault = formData.isDefault ? 1 : 0
      if (this.data.addressId) {
        await AddressAPI.update(
          this.data.addressId,
          formData.contactName,
          formData.contactPhone,
          formData.province,
          formData.city,
          formData.district,
          formData.detailAddress,
          isDefault
        )
      } else {
        await AddressAPI.create(
          formData.contactName,
          formData.contactPhone,
          formData.province,
          formData.city,
          formData.district,
          formData.detailAddress,
          isDefault
        )
      }
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (error) {
      console.error('保存地址失败:', error)
      wx.hideLoading()
      wx.showToast({ title: '保存失败', icon: 'none' })
    }
  }
})
