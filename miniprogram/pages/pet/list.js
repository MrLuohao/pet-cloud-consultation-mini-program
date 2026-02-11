// pages/pet/list.js
const { PetAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    petList: []
  },

  onLoad() {
    // 宠物列表页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadPetList()
  },

  onShow() {
    // 未登录时不执行任何操作
    if (!isLoggedIn()) {
      return
    }
    this.loadPetList()
  },

  // 加载宠物列表
  async loadPetList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const list = await PetAPI.getList()
      this.setData({ petList: list })
    } catch (error) {
      console.error('加载宠物列表失败:', error)
    } finally {
      wx.hideLoading()
    }
  },

  // 添加宠物
  addPet() {
    wx.navigateTo({
      url: '/pages/pet/edit'
    })
  },

  // 查看详情
  gotoDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/pet/edit?id=${id}`
    })
  },

  // 查看宠物时间轴
  gotoTimeline(e) {
    const { id, name } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/pet/timeline/index?petId=${id}&petName=${encodeURIComponent(name)}`
    })
  }
})
