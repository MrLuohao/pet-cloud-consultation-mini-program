// pages/search/result.js
const { SearchAPI } = require('../../utils/api')

Page({
  data: {
    keyword: '',
    hotKeywords: [],
    productList: [],
    isSearching: false,
    inputFocus: false
  },

  // 防抖定时器
  searchTimer: null,

  onLoad() {
    this.loadHotKeywords()
  },

  onReady() {
    // 页面渲染完成后聚焦
    this.focusInput()
  },

  onShow() {
    // 每次显示页面时聚焦
    this.focusInput()
  },

  // 聚焦输入框
  focusInput() {
    this.setData({ inputFocus: false })
    setTimeout(() => {
      this.setData({ inputFocus: true })
    }, 100)
  },

  onUnload() {
    // 页面卸载时清除定时器
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
  },

  // 加载热门搜索词
  async loadHotKeywords() {
    try {
      const keywords = await SearchAPI.getHotKeywords()
      this.setData({ hotKeywords: keywords })
    } catch (error) {
      console.error('加载热门搜索失败:', error)
    }
  },

  // 输入变化 - 实时搜索（带防抖）
  onInput(e) {
    const keyword = e.detail.value
    this.setData({ keyword })

    // 清除之前的定时器
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }

    // 如果关键词为空，清空结果
    if (!keyword.trim()) {
      this.setData({ productList: [], isSearching: false })
      return
    }

    // 设置搜索中状态
    this.setData({ isSearching: true })

    // 防抖：300ms 后执行搜索
    this.searchTimer = setTimeout(() => {
      this.doSearch(keyword.trim())
    }, 300)
  },

  // 清空关键词
  clearKeyword() {
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
    this.setData({
      keyword: '',
      productList: [],
      isSearching: false
    })
  },

  // 搜索热门词
  searchHot(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ keyword })
    this.doSearch(keyword)
  },

  // 执行搜索
  async doSearch(keyword) {
    const searchKeyword = keyword || this.data.keyword.trim()
    if (!searchKeyword) {
      this.setData({ isSearching: false })
      return
    }

    try {
      const products = await SearchAPI.searchProducts(searchKeyword)
      // 确保结果对应当前关键词（防止旧请求覆盖新结果）
      if (this.data.keyword.trim() === searchKeyword) {
        this.setData({
          productList: products,
          isSearching: false
        })
      }
    } catch (error) {
      console.error('搜索失败:', error)
      this.setData({ isSearching: false })
    }
  },

  // 图片加载失败时使用默认图片
  onImageError(e) {
    const index = e.currentTarget.dataset.index
    // 使用在线占位图
    const defaultCover = 'https://via.placeholder.com/300x300?text=No+Image'
    this.setData({
      [`productList[${index}].coverUrl`]: defaultCover
    })
  },

  // 查看商品详情
  gotoProduct(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/product/detail?id=${id}`
    })
  }
})
