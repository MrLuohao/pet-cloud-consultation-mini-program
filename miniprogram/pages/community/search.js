// pages/community/search.js
const { CommunityAPI } = require('../../utils/api')

Page({
  data: {
    keyword: '',
    hasSearched: false,
    historyList: [],
    hotTopics: [],
    currentTab: 'posts',
    posts: [],
    topics: [],
    isLoading: false
  },

  onLoad() {
    this.loadHistory()
    this.loadHotTopics()
  },

  // 加载搜索历史
  loadHistory() {
    const history = wx.getStorageSync('searchHistory') || []
    this.setData({ historyList: history })
  },

  // 加载热门话题
  async loadHotTopics() {
    try {
      const topics = await CommunityAPI.getHotTopics()
      this.setData({ hotTopics: topics || [] })
    } catch (error) {
      console.error('加载热门话题失败:', error)
    }
  },

  // 输入
  onInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  // 清空关键词
  clearKeyword() {
    this.setData({ keyword: '', hasSearched: false, posts: [], topics: [] })
  },

  // 清空历史
  clearHistory() {
    wx.removeStorageSync('searchHistory')
    this.setData({ historyList: [] })
  },

  // 搜索历史
  searchHistory(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword }, () => {
      this.doSearch()
    })
  },

  // 执行搜索
  async doSearch() {
    const { keyword } = this.data
    if (!keyword.trim()) return

    this.setData({ isLoading: true, hasSearched: true })

    // 保存搜索历史
    let history = wx.getStorageSync('searchHistory') || []
    history = history.filter(h => h !== keyword)
    history.unshift(keyword)
    history = history.slice(0, 10)
    wx.setStorageSync('searchHistory', history)
    this.setData({ historyList: history })

    try {
      const [posts, topics] = await Promise.all([
        CommunityAPI.searchPosts(keyword, 1, 20),
        CommunityAPI.searchTopics(keyword)
      ])

      this.setData({
        posts: posts.list || [],
        topics: topics || [],
        isLoading: false
      })
    } catch (error) {
      console.error('搜索失败:', error)
      this.setData({ isLoading: false })
    }
  },

  // 切换Tab
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ currentTab: tab })
  },

  // 返回
  goBack() {
    wx.navigateBack()
  },

  // 跳转帖子详情
  goToDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/community/detail?id=${id}` })
  },

  // 跳转话题
  goToTopic(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/community/topic?id=${id}` })
  }
})
