const { TaskAPI } = require('../../utils/api')

Page({
  data: {
    loading: false,
    points: {},
    tasks: [],
    history: []
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const [pointsRes, tasksRes, historyRes] = await Promise.allSettled([
        TaskAPI.getPoints(),
        TaskAPI.getTodayTasks(),
        TaskAPI.getHistory(1, 20)
      ])

      const points = pointsRes.status === 'fulfilled' && pointsRes.value ? pointsRes.value : {}
      const tasks = tasksRes.status === 'fulfilled' && Array.isArray(tasksRes.value) ? tasksRes.value : []
      const history = historyRes.status === 'fulfilled' && Array.isArray(historyRes.value) ? historyRes.value : []

      this.setData({ points, tasks, history })
    } catch (error) {
      wx.showToast({
        title: '加载任务失败',
        icon: 'none'
      })
    } finally {
      this.setData({ loading: false })
    }
  },

  async onCompleteTask(e) {
    const taskId = e.currentTarget.dataset.id
    if (!taskId) return

    try {
      wx.showLoading({ title: '处理中...' })
      const points = await TaskAPI.completeTask(taskId)
      wx.showToast({
        title: `完成成功 +${points || 0}`,
        icon: 'none'
      })
      await this.loadData()
    } catch (error) {
      wx.showToast({
        title: error?.message || '任务完成失败',
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
    }
  }
})
