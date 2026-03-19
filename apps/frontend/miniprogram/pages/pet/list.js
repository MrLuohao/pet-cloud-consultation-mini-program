// pages/pet/list.js
const { PetAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')

const PRIMARY_ACTION_LABEL = '查看档案'

function formatCountChip(index, total) {
  const current = String(index + 1).padStart(2, '0')
  const overall = String(Math.max(total, 1)).padStart(2, '0')
  return `${current} / ${overall}`
}

function getGenderInfo(pet) {
  const desc = (pet.genderDesc || '').trim()
  if (desc) {
    if (desc.includes('男') || desc.includes('公')) return { text: '男孩', icon: '♂', themeClass: 'cool' }
    if (desc.includes('女') || desc.includes('母')) return { text: '女孩', icon: '♀', themeClass: 'warm' }
    return { text: desc, icon: '·', themeClass: 'warm' }
  }

  const gender = Number(pet.gender)
  if (gender === 1) return { text: '男孩', icon: '♂', themeClass: 'cool' }
  if (gender === 2) return { text: '女孩', icon: '♀', themeClass: 'warm' }
  return { text: '未设置', icon: '·', themeClass: 'warm' }
}

function getAgeLabel(pet) {
  if (pet.age && String(pet.age).trim()) return String(pet.age).trim()
  if (!pet.birthday) return ''
  const birth = new Date(String(pet.birthday).replace(' ', 'T'))
  if (Number.isNaN(birth.getTime())) return ''
  const now = new Date()
  let years = now.getFullYear() - birth.getFullYear()
  let months = now.getMonth() - birth.getMonth()
  if (months < 0) {
    years -= 1
    months += 12
  }
  if (years > 0) return `${years}岁`
  return `${Math.max(1, months)}个月`
}

function getSummaryText(healthStatus) {
  const hs = (healthStatus || '').trim()
  if (!hs) return '建议补充基础资料'
  if (['健康', '良好', '正常', 'healthy'].includes(hs)) return '今日稳定'
  if (['异常', '注意', '需复查', '生病'].some(k => hs.includes(k))) return '需要持续关注'
  return hs
}

function buildPetCardModel(pet, index, total) {
  const typeDesc = (pet.typeDesc || '').trim()
  const breed = (pet.breed || '未知品种').trim() || '未知品种'
  const ageLabel = getAgeLabel(pet)
  const genderInfo = getGenderInfo(pet)
  const avatarText = pet.name ? pet.name.trim().charAt(0) : '宠'
  const metaParts = [typeDesc || breed, ageLabel].filter(Boolean)
  return {
    id: pet.id,
    name: (pet.name || '未命名宠物').trim() || '未命名宠物',
    avatarUrl: pet.avatarUrl || '',
    avatarText,
    genderText: genderInfo.text,
    genderIcon: genderInfo.icon,
    themeClass: genderInfo.themeClass,
    metaText: metaParts.join(' · ') || '资料待补充',
    summaryText: getSummaryText(pet.healthStatus),
    countChip: formatCountChip(index, total),
    primaryActionLabel: PRIMARY_ACTION_LABEL
  }
}

Page({
  data: {
    petList: [],
    petCards: [],
    primaryPetCard: null,
    nextPetCard: null,
    hasPets: false
  },

  onLoad() {
    // 宠物列表页面必须登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.loadPetList()
  },

  onShow() {
    // 未登录时不执行任何操作
    if (!isLoggedIn()) {
      return
    }
    this.loadPetList()
  },

  // 加载宠物列表
  async loadPetList() {
    try {
      wx.showLoading({ title: '加载中...' })
      const list = await PetAPI.getList()
      const safeList = Array.isArray(list) ? list : []
      const petCards = safeList.map((pet, index) => buildPetCardModel(pet, index, safeList.length))
      this.setData({
        petList: safeList,
        petCards,
        primaryPetCard: petCards[0] || null,
        nextPetCard: petCards[1] || null,
        hasPets: petCards.length > 0
      })
    } catch (error) {
      console.error('加载宠物列表失败:', error)
    } finally {
      wx.hideLoading()
    }
  },

  // 添加宠物
  addPet() {
    wx.navigateTo({
      url: '/pages/pet/edit'
    })
  },

  openArchive(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({
      url: `/pages/pet/edit?id=${id}`
    })
  }
})
