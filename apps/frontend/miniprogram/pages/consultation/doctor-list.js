// pages/consultation/doctor-list.js
const { DoctorAPI } = require('../../utils/api')

Page({
  data: {
    departments: [],
    selectedDepartment: '',
    doctorList: []
  },

  onLoad() {
    this.loadDepartments()
    this.loadDoctorList()
  },

  // 加载科室列表
  async loadDepartments() {
    try {
      const departments = await DoctorAPI.getDepartments()
      this.setData({ departments })
    } catch (error) {
      console.error('加载科室失败:', error)
    }
  },

  // 加载医生列表
  async loadDoctorList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const list = await DoctorAPI.getList(this.data.selectedDepartment)
      this.setData({ doctorList: list })
    } catch (error) {
      console.error('加载医生列表失败:', error)
    } finally {
      wx.hideLoading()
    }
  },

  // 选择科室
  selectDepartment(e) {
    const department = e.currentTarget.dataset.department
    this.setData({ selectedDepartment: department })
    this.loadDoctorList()
  },

  // 查看详情
  gotoDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/consultation/doctor-detail?id=${id}`
    })
  }
})
