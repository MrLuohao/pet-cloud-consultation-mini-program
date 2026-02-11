// pages/health/edit.js
const { HealthAPI, PetAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    recordId: null,
    petList: [],
    selectedPet: null,
    selectedType: null,
    recordTypes: [
      { value: 'vaccine', name: '疫苗接种' },
      { value: 'checkup', name: '健康检查' },
      { value: 'medicine', name: '用药记录' },
      { value: 'surgery', name: '手术记录' },
      { value: 'other', name: '其他' }
    ],
    formData: {
      title: '',
      recordDate: '',
      nextDate: '',
      hospitalName: '',
      doctorName: '',
      content: ''
    }
  },

  onLoad(options) {
    // 健康档案编辑页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadPetList()
    if (options.id) {
      this.setData({ recordId: options.id })
    }
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
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

  // 选择宠物
  onPetChange(e) {
    const index = e.detail.value
    this.setData({ selectedPet: this.data.petList[index] })
  },

  // 选择类型
  onTypeChange(e) {
    const index = e.detail.value
    this.setData({ selectedType: this.data.recordTypes[index] })
  },

  // 输入变化
  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    const value = e.detail.value
    this.setData({
      [`formData.${field}`]: value
    })
  },

  // 记录日期
  onRecordDateChange(e) {
    this.setData({
      'formData.recordDate': e.detail.value
    })
  },

  // 下次日期
  onNextDateChange(e) {
    this.setData({
      'formData.nextDate': e.detail.value
    })
  },

  // 保存
  async saveRecord() {
    if (!this.data.selectedPet) {
      wx.showToast({ title: '请选择宠物', icon: 'none' })
      return
    }
    if (!this.data.selectedType) {
      wx.showToast({ title: '请选择类型', icon: 'none' })
      return
    }
    if (!this.data.formData.title) {
      wx.showToast({ title: '请输入标题', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '保存中...' })
      if (this.data.recordId) {
        await HealthAPI.update(
          this.data.recordId,
          this.data.selectedType.value,
          this.data.formData.title,
          this.data.formData.content,
          this.data.formData.hospitalName,
          this.data.formData.doctorName,
          this.data.formData.recordDate,
          this.data.formData.nextDate,
          null
        )
      } else {
        await HealthAPI.create(
          this.data.selectedPet.id,
          this.data.selectedType.value,
          this.data.formData.title,
          this.data.formData.content,
          this.data.formData.hospitalName,
          this.data.formData.doctorName,
          this.data.formData.recordDate,
          this.data.formData.nextDate,
          null
        )
      }
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (error) {
      wx.hideLoading()
      wx.showToast({ title: '保存失败', icon: 'none' })
    }
  },

  // 删除
  deleteRecord() {
    wx.showModal({
      title: '提示',
      content: '确定要删除这条档案吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '删除中...' })
            await HealthAPI.delete(this.data.recordId)
            wx.showToast({ title: '删除成功', icon: 'success' })
            setTimeout(() => {
              wx.navigateBack()
            }, 1500)
          } catch (error) {
            wx.hideLoading()
            wx.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      }
    })
  }
})
