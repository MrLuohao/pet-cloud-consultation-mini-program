// pages/health/list.js
const { HealthAPI, PetAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    petList: [],
    selectedPetId: null,
    healthList: []
  },

  onLoad() {
    // 健康档案页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadPetList()
    this.loadHealthList()
  },

  onShow() {
    // 未登录时不执行任何操作
    if (!isLoggedIn()) {
      return
    }
    this.loadHealthList()
  },

  // 加载宠物列表
  async loadPetList() {
    try {
      const list = await PetAPI.getList()
      this.setData({ petList: list })
    } catch (error) {
      console.error('加载宠物列表失败:', error)
    }
  },

  // 加载健康档案列表
  async loadHealthList() {
    try {
      wx.showLoading({ title: '加载中...' })
      let list
      if (this.data.selectedPetId) {
        list = await HealthAPI.getByPet(this.data.selectedPetId)
      } else {
        list = await HealthAPI.getList()
      }
      this.setData({ healthList: list })
    } catch (error) {
      console.error('加载健康档案失败:', error)
    } finally {
      wx.hideLoading()
    }
  },

  // 选择宠物
  selectPet(e) {
    const petId = e.currentTarget.dataset.id
    this.setData({ selectedPetId: petId === 'null' ? null : petId })
    this.loadHealthList()
  },

  // 添加档案
  addHealth() {
    if (this.data.petList.length === 0) {
      wx.showModal({
        title: '提示',
        content: '请先添加宠物',
        confirmText: '去添加',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/pet/list'
            })
          }
        }
      })
      return
    }
    wx.navigateTo({
      url: '/pages/health/edit'
    })
  },

  // 编辑档案
  editHealth(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/health/edit?id=${id}`
    })
  }
})
