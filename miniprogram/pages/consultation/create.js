// pages/consultation/create.js
const { DoctorAPI, ConsultationAPI, PetAPI, AIAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

Page({
  data: {
    doctorId: null,
    doctor: null,
    fee: 0,
    consultType: 1,
    petList: [],
    selectedPet: null,
    description: '',
    images: []
  },

  onLoad(options) {
    // 创建咨询页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    if (options.doctorId) {
      this.setData({ doctorId: options.doctorId, fee: options.fee })
      this.loadDoctorDetail()
    }
    if (options.type) {
      this.setData({ consultType: parseInt(options.type) })
    }
    this.loadPetList()
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
  },

  // 加载医生详情
  async loadDoctorDetail() {
    try {
      const doctor = await DoctorAPI.getDetail(this.data.doctorId)
      this.setData({ doctor })
    } catch (error) {
      console.error('加载医生详情失败:', error)
    }
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

  // 选择咨询类型
  selectType(e) {
    const type = e.currentTarget.dataset.type
    this.setData({ consultType: type })
  },

  // 病情描述输入
  onDescriptionInput(e) {
    this.setData({ description: e.detail.value })
  },

  // 选择图片
  chooseImage() {
    wx.chooseImage({
      count: 4 - this.data.images.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const images = this.data.images.concat(res.tempFilePaths)
        this.setData({ images })
      }
    })
  },

  // 删除图片
  deleteImage(e) {
    const index = e.currentTarget.dataset.index
    const images = this.data.images.filter((_, i) => i !== index)
    this.setData({ images })
  },

  // 提交咨询
  async submitConsultation() {
    if (!this.data.selectedPet) {
      wx.showToast({ title: '请选择宠物', icon: 'none' })
      return
    }
    if (!this.data.description) {
      wx.showToast({ title: '请描述病情', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '提交中...' })
      // 先上传所有图片到服务器
      const uploadedUrls = []
      for (const tempPath of this.data.images) {
        const url = await AIAPI.uploadImage(tempPath)
        uploadedUrls.push(url)
      }
      const consultationId = await ConsultationAPI.create(
        this.data.selectedPet.id,
        this.data.doctorId,
        this.data.consultType,
        this.data.description,
        JSON.stringify(uploadedUrls)
      )
      wx.hideLoading()
      wx.showToast({ title: '提交成功', icon: 'success' })
      setTimeout(() => {
        wx.redirectTo({
          url: `/pages/consultation/chat?id=${consultationId}`
        })
      }, 1500)
    } catch (error) {
      wx.hideLoading()
      wx.showToast({ title: '提交失败', icon: 'none' })
    }
  }
})
