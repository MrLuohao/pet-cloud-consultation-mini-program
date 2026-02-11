// pages/consultation/doctor-detail.js
const { DoctorAPI, PetAPI } = require('../../utils/api')

Page({
  data: {
    doctorId: null,
    doctor: {
      name: '',
      avatar: '',
      title: '',
      hospitalName: '',
      department: '',
      experience: 0,
      specialty: '',
      description: '',
      consultationFee: 0,
      rating: 5,
      consultationCount: 0,
      goodReviewRate: null,
      avgResponseMinutes: null,
      tags: []
    },
    reviews: [],
    petList: [],
    selectedType: 1
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ doctorId: options.id })
      this.loadDoctorDetail()
      this.loadReviews()
    }
    this.loadPetList()
  },

  goBack() {
    wx.navigateBack()
  },

  async loadDoctorDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      const doctor = await DoctorAPI.getDetail(this.data.doctorId)
      this.setData({ doctor })
    } catch (error) {
      console.error('加载医生详情失败:', error)
    } finally {
      wx.hideLoading()
    }
  },

  async loadReviews() {
    try {
      const reviews = await DoctorAPI.getReviews(this.data.doctorId)
      this.setData({ reviews: reviews || [] })
    } catch (error) {
      console.error('加载评价失败:', error)
    }
  },

  async loadPetList() {
    try {
      const list = await PetAPI.getList()
      this.setData({ petList: list })
    } catch (error) {
      console.error('加载宠物列表失败:', error)
    }
  },

  selectType(e) {
    const type = e.currentTarget.dataset.type
    this.setData({ selectedType: type })
  },

  startConsultation() {
    if (this.data.petList.length === 0) {
      wx.showModal({
        title: '提示',
        content: '请先添加宠物信息',
        confirmText: '去添加',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/pet/list' })
          }
        }
      })
      return
    }
    wx.navigateTo({
      url: `/pages/consultation/create?doctorId=${this.data.doctorId}&type=${this.data.selectedType}&fee=${this.data.doctor.consultationFee}`
    })
  }
})
