const { ProductAPI, CartAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

const FALLBACK_HERO = {
  title: '低温烘焙鸡肉主粮',
  subtitle: '稳定、轻负担的日常主食选择。'
}

const FALLBACK_HIGHLIGHTS = [
  '低温烘焙，保留更自然的风味与适口性。',
  '温和主食配方，适合作为稳定的日常选择。'
]

const FALLBACK_STORY = [
  {
    title: '温和日常配方',
    description: '适合作为稳定、轻负担的日常主食选择。'
  },
  {
    title: '低温烘焙，风味更自然',
    description: '减少高温处理带来的风味损失，让日常喂养更轻松。'
  },
  {
    title: '适合作为稳定的日常节奏',
    description: '根据宠物年龄、体型与习惯，灵活调整每日喂养量。'
  }
]

const FALLBACK_USAGE_NOTE = {
  title: '适用对象 / 使用建议',
  content: '适合全阶段猫咪的日常喂养，可根据体型与活动量灵活调整。建议搭配稳定饮水与规律作息一起建立更轻松的喂养节奏。'
}

function buildSpecOption(label, activeValue, index) {
  return {
    value: label,
    label,
    hint: index === 1 ? '推荐' : '可选',
    selected: label === activeValue
  }
}

function buildConfiguredSpecOption(option = {}, activeValue, index) {
  const value = option.value || option.label || ''
  const label = option.label || value
  return {
    value,
    label,
    hint: option.hint || (index === 1 ? '推荐' : '可选'),
    selected: value === activeValue
  }
}

function buildConfiguredSpecGroup(group = {}, index = 0) {
  const rawOptions = Array.isArray(group.options)
    ? group.options
    : Array.isArray(group.values)
      ? group.values.map(item => ({ value: item, label: item }))
      : []
  const firstValue = rawOptions[0] && (rawOptions[0].value || rawOptions[0].label || '')
  const selectedLabel = group.selectedValue || group.selectedLabel || firstValue || ''
  const options = rawOptions
    .map((option, optionIndex) => buildConfiguredSpecOption(option, selectedLabel, optionIndex))
    .filter(option => option.value)

  if (!options.length) {
    return null
  }

  return {
    key: group.key || `group_${index}`,
    label: group.label || group.name || '规格',
    selectedLabel,
    options
  }
}

function buildSpecGroups(detail = {}) {
  if (Array.isArray(detail.specGroups) && detail.specGroups.length) {
    const configuredGroups = detail.specGroups
      .map((group, index) => buildConfiguredSpecGroup(group, index))
      .filter(Boolean)

    if (configuredGroups.length) {
      return configuredGroups
    }
  }

  const capacityCandidates = []
  const lifeStageCandidates = []
  if (detail.spec) {
    capacityCandidates.push(detail.spec)
  }
  if (Array.isArray(detail.specGroups)) {
    detail.specGroups.forEach(group => {
      const name = String(group.name || group.label || '').toLowerCase()
      const values = Array.isArray(group.values) ? group.values : []
      if (name.includes('规格') || name.includes('容量')) {
        capacityCandidates.push(...values)
      }
      if (name.includes('阶段') || name.includes('life')) {
        lifeStageCandidates.push(...values)
      }
    })
  }

  const capacities = [...new Set(capacityCandidates.filter(Boolean))]
  const lifeStages = [...new Set(lifeStageCandidates.filter(Boolean))]

  const capacityList = capacities.length ? capacities : ['500g', '1.5kg', '3kg']
  const lifeStageList = lifeStages.length ? lifeStages : ['幼年期', '全阶段', '成猫期']

  const selectedCapacity = capacityList[1] || capacityList[0]
  const selectedLifeStage = lifeStageList[1] || lifeStageList[0]

  return [
    {
      key: 'capacity',
      label: '规格',
      selectedLabel: selectedCapacity,
      options: capacityList.map((item, index) => buildSpecOption(item, selectedCapacity, index))
    },
    {
      key: 'lifeStage',
      label: '适用阶段',
      selectedLabel: selectedLifeStage,
      options: lifeStageList.map((item, index) => buildSpecOption(item, selectedLifeStage, index))
    }
  ]
}

function updateGroupSelection(groups, groupKey, selectedValue) {
  return groups.map(group => {
    if (group.key !== groupKey) {
      return group
    }
    return {
      ...group,
      selectedLabel: selectedValue,
      options: group.options.map(option => ({
        ...option,
        selected: option.value === selectedValue
      }))
    }
  })
}

function buildHighlights(detail = {}) {
  return Array.isArray(detail.highlights) && detail.highlights.length
    ? detail.highlights.filter(Boolean)
    : FALLBACK_HIGHLIGHTS
}

function buildStorySections(detail = {}) {
  if (Array.isArray(detail.storySections) && detail.storySections.length) {
    return detail.storySections.map(section => ({
      title: section.title || '',
      description: section.description || '',
      imageUrl: section.imageUrl || ''
    }))
  }
  return FALLBACK_STORY
}

function buildUsageNote(detail = {}) {
  if (detail.usageNote && (detail.usageNote.title || detail.usageNote.content)) {
    return {
      title: detail.usageNote.title || FALLBACK_USAGE_NOTE.title,
      content: detail.usageNote.content || FALLBACK_USAGE_NOTE.content
    }
  }
  return FALLBACK_USAGE_NOTE
}

function getSelectedSpecLabel(groups = []) {
  const selectedLabels = groups
    .map(group => group && group.selectedLabel)
    .filter(Boolean)
  return selectedLabels.join(' · ')
}

Page({
  data: {
    productId: null,
    product: {
      name: '',
      summary: '',
      price: 0,
      coverUrl: ''
    },
    displayPrice: '129.00',
    heroCopy: FALLBACK_HERO,
    highlights: FALLBACK_HIGHLIGHTS,
    storySections: FALLBACK_STORY,
    usageNote: FALLBACK_USAGE_NOTE,
    specGroups: buildSpecGroups(),
    reviewSummary: {
      total: 0,
      goodCount: 0,
      badCount: 0,
      withImagesCount: 0,
      avgRating: 5,
      avgRatingDisplay: '5.0'
    },
    reviewableOrderItem: null
  },

  onLoad(options) {
    if (!options.id) {
      return
    }
    this.setData({ productId: options.id })
    this.loadProductDetail()
  },

  async loadProductDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      const [detail, reviewSummary, reviewableOrderItem] = await Promise.all([
        ProductAPI.getDetail(this.data.productId),
        ProductAPI.getReviewSummary(this.data.productId).catch(() => null),
        isLoggedIn() ? ProductAPI.getReviewableOrderItem(this.data.productId).catch(() => null) : Promise.resolve(null)
      ])
      const price = Number(detail && detail.price) || 129

      this.setData({
        product: {
          ...detail,
          name: detail && detail.name ? detail.name : FALLBACK_HERO.title,
          summary: detail && detail.summary ? detail.summary : FALLBACK_HERO.subtitle
        },
        displayPrice: price.toFixed(2),
        highlights: buildHighlights(detail),
        storySections: buildStorySections(detail),
        usageNote: buildUsageNote(detail),
        specGroups: buildSpecGroups(detail),
        reviewSummary: {
          total: reviewSummary && Number(reviewSummary.total) ? Number(reviewSummary.total) : 0,
          goodCount: reviewSummary && Number(reviewSummary.goodCount) ? Number(reviewSummary.goodCount) : 0,
          badCount: reviewSummary && Number(reviewSummary.badCount) ? Number(reviewSummary.badCount) : 0,
          withImagesCount: reviewSummary && Number(reviewSummary.withImagesCount) ? Number(reviewSummary.withImagesCount) : 0,
          avgRating: reviewSummary && Number(reviewSummary.avgRating) ? Number(reviewSummary.avgRating) : 5,
          avgRatingDisplay: reviewSummary && Number(reviewSummary.avgRating)
            ? Number(reviewSummary.avgRating).toFixed(1)
            : '5.0'
        },
        reviewableOrderItem
      })
    } catch (error) {
      console.error('加载商品详情失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({
        highlights: FALLBACK_HIGHLIGHTS,
        storySections: FALLBACK_STORY,
        usageNote: FALLBACK_USAGE_NOTE,
        specGroups: buildSpecGroups(),
        reviewSummary: {
          total: 0,
          goodCount: 0,
          badCount: 0,
          withImagesCount: 0,
          avgRating: 5,
          avgRatingDisplay: '5.0'
        },
        reviewableOrderItem: null
      })
    } finally {
      wx.hideLoading()
    }
  },

  selectSpec(event) {
    const { group, value } = event.currentTarget.dataset
    this.setData({
      specGroups: updateGroupSelection(this.data.specGroups, group, value)
    })
  },

  async addToCart() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    try {
      wx.showLoading({ title: '添加中...' })
      await CartAPI.add(this.data.productId, 1, getSelectedSpecLabel(this.data.specGroups))
      wx.hideLoading()
      wx.showToast({ title: '已加入购物袋', icon: 'success' })
    } catch (error) {
      wx.hideLoading()
      console.error('加入购物车失败:', error)
      wx.showToast({ title: error.message || '加入失败', icon: 'none' })
    }
  },

  buyNow() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    wx.navigateTo({
      url: `/pages/order/confirm?productIds=${encodeURIComponent(JSON.stringify([this.data.productId]))}&quantities=${encodeURIComponent(JSON.stringify([1]))}&specLabels=${encodeURIComponent(JSON.stringify([getSelectedSpecLabel(this.data.specGroups)]))}`
    })
  },

  goToReviews() {
    wx.navigateTo({
      url: `/pages/product/reviews?productId=${this.data.productId}`
    })
  },

  goToWriteReview() {
    if (!this.data.reviewableOrderItem) {
      this.goToReviews()
      return
    }
    const itemData = encodeURIComponent(JSON.stringify(this.data.reviewableOrderItem))
    wx.navigateTo({
      url: `/pages/order/review?orderId=${this.data.reviewableOrderItem.orderId || ''}&itemData=${itemData}`
    })
  }
})
