const app = getApp()
const { HomeAPI, TaskAPI, parseDate } = require('../../utils/api')

const SERVICE_CARDS = [
  { key: 'diagnosis', title: 'AI 诊断', subtitle: '上传图片快速初筛', iconText: 'AI', action: 'goToDiagnosis' },
  { key: 'chat', title: '在线问诊', subtitle: '和医生助手聊一聊', iconText: '问', action: 'goToChat' },
  { key: 'course', title: '训练课程', subtitle: '日常行为养成建议', iconText: '课', action: 'goToCourse' },
  { key: 'beauty', title: '美容护理', subtitle: '预约洗护与造型', iconText: '护', action: 'goToBeauty' }
]

function clampIndex(index, length) {
  if (!length) return 0
  if (index < 0) return 0
  if (index >= length) return length - 1
  return index
}

function formatRelativeTime(time) {
  if (!time) return '刚刚'
  const date = parseDate(time)
  if (!(date instanceof Date) || Number.isNaN(date.getTime())) {
    return '近期更新'
  }

  const diff = Date.now() - date.getTime()
  const hour = 60 * 60 * 1000
  const day = 24 * hour

  if (diff < hour) return '刚刚更新'
  if (diff < day) return `${Math.max(1, Math.floor(diff / hour))}小时前`
  if (diff < day * 7) return `${Math.max(1, Math.floor(diff / day))}天前`
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function formatViewCount(value) {
  const count = Number(value || 0)
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`
  }
  return `${count}`
}

function buildSummaryState(summary, currentPetIndex) {
  const petCard = summary && summary.petCard ? summary.petCard : null
  const petList = petCard && Array.isArray(petCard.pets) ? petCard.pets : []
  const nextIndex = clampIndex(currentPetIndex, petList.length)
  const serviceRows = [
    SERVICE_CARDS.slice(0, 2),
    SERVICE_CARDS.slice(2, 4)
  ]
  const featuredContents = Array.isArray(summary && summary.featuredContents)
    ? summary.featuredContents.map(item => ({
        ...item,
        displayTime: formatRelativeTime(item.publishTime),
        displayViews: formatViewCount(item.viewCount),
        summary: item.summary || '查看更多内容详情'
      }))
    : []
  const currentPet = petList[nextIndex] || null
  const todayCareSummary = summary && summary.todayCareSummary
    ? {
        ...summary.todayCareSummary,
        tasks: Array.isArray(summary.todayCareSummary.tasks)
          ? summary.todayCareSummary.tasks.map(task => ({
              ...task,
              pointsText: task.points ? `+${task.points}` : ''
            }))
          : []
      }
    : null

  return {
    loggedIn: !!(summary && summary.loggedIn),
    petCard,
    currentPetIndex: nextIndex,
    currentPet,
    reminderSummary: summary && summary.reminderSummary ? summary.reminderSummary : null,
    todayCareSummary,
    featuredContents,
    featuredPrimary: featuredContents[0] || null,
    featuredSecondary: featuredContents[1] || null,
    serviceRows,
    petHighlightText: currentPet
      ? (currentPet.statusSummary || '今日状态稳定，已记录饮水、食欲与精神状态变化。')
      : '',
    careSummaryTitle: todayCareSummary && todayCareSummary.completedCount
      ? `已完成：${todayCareSummary.completedCount}/${todayCareSummary.totalCount || 0}`
      : '今日照护待开始',
    careSummarySubtitle: todayCareSummary && todayCareSummary.tasks && todayCareSummary.tasks.length
      ? `待关注：${todayCareSummary.tasks.slice(0, 2).map(task => task.name).join('、')}`
      : '待关注：饮水记录、晚间散步',
    reminderHeadline: summary && summary.reminderSummary && summary.reminderSummary.hasPending
      ? `${summary.reminderSummary.nextPetName || '当前宠物'}的${summary.reminderSummary.nextTitle || '健康提醒'}`
      : '当前暂无待处理提醒',
    reminderSubline: summary && summary.reminderSummary
      ? (summary.reminderSummary.nextRemindDateText || '后续可添加疫苗、驱虫和复查计划。')
      : '后续可添加疫苗、驱虫和复查计划。'
  }
}

Page({
  data: {
    keyword: '',
    searchFocused: false,
    loading: false,
    loggedIn: false,
    currentPetIndex: 0,
    currentPet: null,
    petCard: null,
    reminderSummary: null,
    todayCareSummary: null,
    featuredContents: [],
    featuredPrimary: null,
    featuredSecondary: null,
    serviceCards: SERVICE_CARDS,
    serviceRows: [
      SERVICE_CARDS.slice(0, 2),
      SERVICE_CARDS.slice(2, 4)
    ],
    petHighlightText: '',
    careSummaryTitle: '今日照护待开始',
    careSummarySubtitle: '待关注：饮水记录、晚间散步',
    reminderHeadline: '当前暂无待处理提醒',
    reminderSubline: '后续可添加疫苗、驱虫和复查计划。'
  },

  onLoad() {
    this.setData({
      loggedIn: !!app.globalData?.token
    })
  },

  onShow() {
    this.loadHomeSummary()
  },

  async onPullDownRefresh() {
    await this.loadHomeSummary()
    wx.stopPullDownRefresh()
  },

  async loadHomeSummary() {
    this.setData({ loading: true })
    try {
      const summary = await HomeAPI.getSummary()
      this.setData({
        ...buildSummaryState(summary, this.data.currentPetIndex),
        loading: false
      })
    } catch (error) {
      console.error('加载首页摘要失败:', error)
      this.setData({ loading: false })
    }
  },

  onSearchInput(e) {
    this.setData({
      keyword: e.detail.value || ''
    })
  },

  onSearchFocus() {
    this.setData({ searchFocused: true })
  },

  onSearchBlur() {
    this.setData({ searchFocused: false })
  },

  onClearSearch() {
    this.setData({ keyword: '' })
  },

  focusSearch() {
    wx.navigateTo({
      url: '/pages/search/result'
    })
  },

  onSearch() {
    this.focusSearch()
  },

  switchPet(e) {
    const index = Number(e.currentTarget.dataset.index || 0)
    const pets = this.data.petCard && Array.isArray(this.data.petCard.pets) ? this.data.petCard.pets : []
    const nextIndex = clampIndex(index, pets.length)
    this.setData({
      currentPetIndex: nextIndex,
      currentPet: pets[nextIndex] || null
    })
  },

  handlePetEntryTap() {
    if (this.data.currentPet) {
      this.goToPetCardDetail()
      return
    }
    if (this.data.loggedIn) {
      this.goToAddPet()
      return
    }
    this.goToLogin()
  },

  goToPetCardDetail() {
    const currentPet = this.data.currentPet
    if (!currentPet || !currentPet.petId) {
      this.goToAddPet()
      return
    }

    wx.navigateTo({
      url: `/pages/pet/edit?id=${currentPet.petId}`
    })
  },

  goToAddPet() {
    wx.navigateTo({
      url: '/pages/pet/edit'
    })
  },

  handleServiceTap(e) {
    const action = e.currentTarget.dataset.action
    if (action && typeof this[action] === 'function') {
      this[action]()
    }
  },

  handleReminderTap() {
    if (!this.data.loggedIn) {
      this.goToLogin()
      return
    }

    if (!this.data.currentPet) {
      this.goToAddPet()
      return
    }

    wx.navigateTo({
      url: '/pages/consultation/create'
    })
  },

  async handleTodayTaskTap(e) {
    const index = Number(e.currentTarget.dataset.index || 0)
    const taskList = this.data.todayCareSummary && Array.isArray(this.data.todayCareSummary.tasks)
      ? this.data.todayCareSummary.tasks
      : []
    const task = taskList[index]
    if (!task || task.completed) {
      return
    }

    if (task.code === 'AI_DIAGNOSIS') {
      this.goToDiagnosis()
      return
    }

    if (task.code === 'DAILY_SIGN') {
      try {
        await TaskAPI.completeTask(task.taskId)
        wx.showToast({
          title: '已完成签到',
          icon: 'success'
        })
        this.loadHomeSummary()
      } catch (error) {
        console.error('完成任务失败:', error)
      }
      return
    }

    wx.showToast({
      title: '请前往对应页面完成',
      icon: 'none'
    })
  },

  goToArticleDetail(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/recommend/detail?id=${id}`
    })
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    })
  },

  goToChat() {
    wx.navigateTo({
      url: '/pages/chat/chat'
    })
  },

  goToDiagnosis() {
    wx.navigateTo({
      url: '/pages/diagnosis/diagnosis'
    })
  },

  goToCourse() {
    wx.navigateTo({
      url: '/pages/course/course'
    })
  },

  goToBeauty() {
    wx.navigateTo({
      url: '/pages/beauty/beauty'
    })
  }
})
