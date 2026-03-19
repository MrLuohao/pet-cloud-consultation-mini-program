// pages/pet/edit.js - 宠物编辑页（卡片式分步向导）
const { PetAPI, AIAPI, isLoggedIn, navigateToLogin, parseDate, getApiBaseUrl } = require('../../utils/api')

// 常见品种数据（含图片路径，用于卡片轮播展示）
const BREED_IMAGE_BASE = `${getApiBaseUrl()}/uploads/breeds`
const BREED_DATA = {
  1: [
    { name: '金毛寻回犬', image: `${BREED_IMAGE_BASE}/golden-retriever.jpg` },
    { name: '拉布拉多', image: `${BREED_IMAGE_BASE}/labrador.jpg` },
    { name: '柯基', image: `${BREED_IMAGE_BASE}/corgi.jpg` },
    { name: '泰迪', image: `${BREED_IMAGE_BASE}/poodle.jpg` },
    { name: '哈士奇', image: `${BREED_IMAGE_BASE}/husky.jpg` },
    { name: '萨摩耶', image: `${BREED_IMAGE_BASE}/samoyed.jpg` },
    { name: '边牧', image: `${BREED_IMAGE_BASE}/border-collie.jpg` },
    { name: '柴犬', image: `${BREED_IMAGE_BASE}/shiba-inu.jpg` },
    { name: '法斗', image: `${BREED_IMAGE_BASE}/french-bulldog.jpg` },
    { name: '德牧', image: `${BREED_IMAGE_BASE}/german-shepherd.jpg` },
    { name: '比熊', image: `${BREED_IMAGE_BASE}/bichon-frise.jpg` },
    { name: '博美', image: `${BREED_IMAGE_BASE}/pomeranian.jpg` }
  ],
  2: [
    { name: '英短', image: `${BREED_IMAGE_BASE}/british-shorthair.jpg` },
    { name: '美短', image: `${BREED_IMAGE_BASE}/american-shorthair.jpg` },
    { name: '布偶', image: `${BREED_IMAGE_BASE}/ragdoll.jpg` },
    { name: '暹罗', image: `${BREED_IMAGE_BASE}/siamese.jpg` },
    { name: '橘猫', image: `${BREED_IMAGE_BASE}/orange-tabby.jpg` },
    { name: '狸花猫', image: `${BREED_IMAGE_BASE}/dragon-li.jpg` },
    { name: '蓝猫', image: `${BREED_IMAGE_BASE}/russian-blue.jpg` },
    { name: '加菲', image: `${BREED_IMAGE_BASE}/exotic-shorthair.jpg` },
    { name: '缅因', image: `${BREED_IMAGE_BASE}/maine-coon.jpg` },
    { name: '斯芬克斯', image: `${BREED_IMAGE_BASE}/sphynx.jpg` },
    { name: '曼基康', image: `${BREED_IMAGE_BASE}/munchkin.jpg` },
    { name: '波斯猫', image: `${BREED_IMAGE_BASE}/persian.jpg` }
  ],
  3: [
    { name: '仓鼠', image: `${BREED_IMAGE_BASE}/hamster.jpg` },
    { name: '兔子', image: `${BREED_IMAGE_BASE}/rabbit.jpg` },
    { name: '鹦鹉', image: `${BREED_IMAGE_BASE}/parrot.jpg` },
    { name: '乌龟', image: `${BREED_IMAGE_BASE}/turtle.jpg` },
    { name: '龙猫', image: `${BREED_IMAGE_BASE}/chinchilla.jpg` },
    { name: '蜥蜴', image: `${BREED_IMAGE_BASE}/lizard.jpg` },
    { name: '金鱼', image: `${BREED_IMAGE_BASE}/goldfish.jpg` },
    { name: '其他', image: `${BREED_IMAGE_BASE}/other-pet.jpg` }
  ]
}

// 性格标签
const PERSONALITY_TAGS = ['活泼', '温顺', '黏人', '独立', '调皮', '安静', '勇敢', '胆小', '聪明', '贪吃', '好动', '慵懒']

Page({
  data: {
    petId: null,
    isEdit: false,
    currentStep: 0,
    totalSteps: 3,
    animating: false,
    today: '',

    // Step 1: Pet Type & Name
    petTypes: [
      { value: 1, name: '狗狗', emoji: '🐕', desc: '忠诚伙伴' },
      { value: 2, name: '猫咪', emoji: '🐱', desc: '优雅小主' },
      { value: 3, name: '其他', emoji: '🐾', desc: '特别的TA' }
    ],

    // Step 2: Info
    genders: [
      { value: 1, name: '男孩', icon: '♂', color: 'blue' },
      { value: 2, name: '女孩', icon: '♀', color: 'pink' },
      { value: 0, name: '保密', icon: '?', color: 'gray' }
    ],
    breedSuggestions: [],
    breedSwiperIndex: 0,
    breedCardBlur: [],
    breedCardOpacity: [],
    calculatedAge: '',

    // Step 3: Profile
    personalityTags: [
      { name: '活泼', selected: false },
      { name: '温顺', selected: false },
      { name: '黏人', selected: false },
      { name: '独立', selected: false },
      { name: '调皮', selected: false },
      { name: '安静', selected: false },
      { name: '勇敢', selected: false },
      { name: '胆小', selected: false },
      { name: '聪明', selected: false },
      { name: '贪吃', selected: false },
      { name: '好动', selected: false },
      { name: '慵懒', selected: false }
    ],
    selectedTags: [],

    // Form data
    formData: {
      name: '',
      type: 0,
      breed: '',
      gender: -1,
      birthday: '',
      weight: '',
      avatarUrl: '',
      healthStatus: '',
      personality: '',
      motto: ''
    }
  },

  onLoad(options) {
    const today = new Date()
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

    this.setData({ today: todayStr })

    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    if (options.id) {
      this.setData({ petId: options.id, isEdit: true })
      this.loadPetDetail()
    }
  },

  goBack() {
    wx.navigateBack()
  },

  // 加载宠物详情
  async loadPetDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      const pet = await PetAPI.getDetail(this.data.petId)

      // 处理性格标签
      const selectedTagNames = pet.personality ? pet.personality.split('、').filter(t => t) : []
      const personalityTags = this.data.personalityTags.map(tag => ({
        ...tag,
        selected: selectedTagNames.includes(tag.name)
      }))

      this.setData({
        formData: {
          name: pet.name || '',
          type: pet.type || 0,
          breed: pet.breed || '',
          gender: pet.gender !== undefined ? pet.gender : -1,
          birthday: pet.birthday || '',
          weight: pet.weight ? String(pet.weight) : '',
          avatarUrl: pet.avatarUrl || '',
          healthStatus: pet.healthStatus || '',
          personality: pet.personality || '',
          motto: pet.motto || ''
        },
        personalityTags: personalityTags,
        selectedTags: selectedTagNames
      })

      if (pet.type) {
        const breeds = BREED_DATA[pet.type] || []
        const breedIdx = breeds.findIndex(b => b.name === pet.breed)
        this.setData({
          breedSuggestions: breeds,
          breedSwiperIndex: breedIdx >= 0 ? breedIdx : 0
        })
        // 初始化卡片模糊效果
        this.updateBreedCardEffects(breedIdx >= 0 ? breedIdx : 0, 0)
      }
      if (pet.birthday) {
        this.calculateAge(pet.birthday)
      }
    } catch (error) {
      console.error('加载宠物详情失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  // === 步骤导航 ===
  nextStep() {
    if (this.data.animating) return
    const { currentStep, totalSteps, formData } = this.data

    if (currentStep === 0) {
      if (!formData.type) {
        wx.showToast({ title: '请选择宠物类型', icon: 'none' })
        return
      }
      if (!formData.name.trim()) {
        wx.showToast({ title: '请输入宠物名字', icon: 'none' })
        return
      }
    }

    if (currentStep < totalSteps - 1) {
      this.setData({ animating: true, currentStep: currentStep + 1 })
      setTimeout(() => this.setData({ animating: false }), 400)
    }
  },

  prevStep() {
    if (this.data.animating) return
    if (this.data.currentStep > 0) {
      this.setData({ animating: true, currentStep: this.data.currentStep - 1 })
      setTimeout(() => this.setData({ animating: false }), 400)
    }
  },

  goToStep(e) {
    const step = Number(e.currentTarget.dataset.step)
    if (step <= this.data.currentStep && !this.data.animating) {
      this.setData({ animating: true, currentStep: step })
      setTimeout(() => this.setData({ animating: false }), 400)
    }
  },

  // === Step 1 ===
  onSelectType(e) {
    const type = Number(e.currentTarget.dataset.type)
    const breeds = BREED_DATA[type] || []
    this.setData({
      'formData.type': type,
      'formData.breed': breeds.length ? breeds[0].name : '',
      breedSuggestions: breeds,
      breedSwiperIndex: 0
    })
    // 初始化卡片模糊效果
    this.updateBreedCardEffects(0, 0)
  },

  onNameInput(e) {
    this.setData({ 'formData.name': e.detail.value })
  },

  // === Step 2 ===
  onSelectGender(e) {
    const gender = Number(e.currentTarget.dataset.gender)
    this.setData({ 'formData.gender': gender })
  },

  onBreedInput(e) {
    const inputValue = e.detail.value.trim()
    this.setData({ 'formData.breed': inputValue })

    // 搜索匹配品种并自动滚动
    if (inputValue && this.data.breedSuggestions.length > 0) {
      const breeds = this.data.breedSuggestions
      // 查找完全匹配或部分匹配的品种
      let matchedIndex = breeds.findIndex(b =>
        b.name === inputValue || b.name.includes(inputValue) || inputValue.includes(b.name)
      )

      // 如果找到匹配项且不是当前索引，则滚动到对应卡片
      if (matchedIndex >= 0 && matchedIndex !== this.data.breedSwiperIndex) {
        this.setData({
          breedSwiperIndex: matchedIndex
        })
      }
    }
  },

  onSelectBreed(e) {
    const breed = e.currentTarget.dataset.breed
    const breeds = this.data.breedSuggestions
    const idx = breeds.findIndex(b => b.name === breed)
    this.setData({
      'formData.breed': breed,
      breedSwiperIndex: idx >= 0 ? idx : this.data.breedSwiperIndex
    })
  },

  onBreedSwiperChange(e) {
    const idx = e.detail.current
    const breeds = this.data.breedSuggestions
    if (breeds[idx]) {
      this.setData({
        breedSwiperIndex: idx,
        'formData.breed': breeds[idx].name
      })
      this.updateBreedCardEffects(idx, 0)
    }
  },

  onBreedSwiperTransition(e) {
    // 节流处理，避免过于频繁的更新
    if (this.breedTransitionTimer) return

    this.breedTransitionTimer = setTimeout(() => {
      this.breedTransitionTimer = null
    }, 50)

    const currentIndex = this.data.breedSwiperIndex
    this.updateBreedCardEffects(currentIndex, 0)
  },

  onBreedSwiperAnimationFinish(e) {
    const idx = e.detail.current
    this.setData({ breedSwiperIndex: idx })
    this.updateBreedCardEffects(idx, 0)
  },

  updateBreedCardEffects(centerIndex, progress) {
    const total = this.data.breedSuggestions.length
    if (total === 0) return

    const blur = []
    const opacity = []

    for (let i = 0; i < total; i++) {
      // 计算与中心卡片的距离
      let distance = Math.abs(i - centerIndex)

      // 处理循环情况
      if (distance > total / 2) {
        distance = total - distance
      }

      // 根据距离计算模糊值和透明度
      // 中心卡片：blur=0, opacity=1
      // 相邻卡片：blur=3px, opacity=0.75
      // 更远的卡片：blur=6px, opacity=0.5
      const blurValue = Math.min(distance * 3, 8)
      const opacityValue = Math.max(1 - distance * 0.25, 0.5)

      blur.push(blurValue)
      opacity.push(opacityValue)
    }

    this.setData({
      breedCardBlur: blur,
      breedCardOpacity: opacity
    })
  },

  onBirthdayChange(e) {
    const birthday = e.detail.value
    this.setData({ 'formData.birthday': birthday })
    this.calculateAge(birthday)
  },

  calculateAge(birthday) {
    if (!birthday) {
      this.setData({ calculatedAge: '' })
      return
    }
    const birth = parseDate(birthday)
    const now = new Date()
    let years = now.getFullYear() - birth.getFullYear()
    let months = now.getMonth() - birth.getMonth()
    if (months < 0) { years--; months += 12 }
    let ageStr = ''
    if (years > 0) ageStr += years + '岁'
    if (months > 0) ageStr += months + '个月'
    if (!ageStr) ageStr = '不到1个月'
    this.setData({ calculatedAge: ageStr })
  },

  onWeightInput(e) {
    this.setData({ 'formData.weight': e.detail.value })
  },

  onQuickWeight(e) {
    const weight = e.currentTarget.dataset.weight
    this.setData({ 'formData.weight': String(weight) })
  },

  // === Step 3 ===
  changeAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const tempPath = res.tempFilePaths[0]
        // 先显示临时图片作为预览
        this.setData({ 'formData.avatarUrl': tempPath })
        try {
          wx.showLoading({ title: '上传中...' })
          const uploadedUrl = await AIAPI.uploadImage(tempPath)
          this.setData({ 'formData.avatarUrl': uploadedUrl })
          wx.hideLoading()
        } catch (err) {
          wx.hideLoading()
          console.error('头像上传失败:', err)
          wx.showToast({ title: '图片上传失败', icon: 'none' })
          this.setData({ 'formData.avatarUrl': '' })
        }
      }
    })
  },

  onToggleTag(e) {
    const index = e.currentTarget.dataset.index
    const tags = this.data.personalityTags
    const selectedCount = tags.filter(t => t.selected).length

    // 如果要选中，检查是否已达到上限
    if (!tags[index].selected && selectedCount >= 5) {
      wx.showToast({
        title: '最多选择5个标签',
        icon: 'none',
        duration: 1500
      })
      return
    }

    // 切换选中状态
    tags[index].selected = !tags[index].selected

    // 更新选中的标签列表
    const selectedTags = tags.filter(t => t.selected).map(t => t.name)

    this.setData({
      personalityTags: tags,
      selectedTags: selectedTags,
      'formData.personality': selectedTags.join('、')
    })
  },

  onHealthInput(e) {
    this.setData({ 'formData.healthStatus': e.detail.value })
  },

  onMottoInput(e) {
    this.setData({ 'formData.motto': e.detail.value })
  },

  // === 保存 ===
  async savePet() {
    const { formData, petId } = this.data

    if (!formData.name.trim()) {
      wx.showToast({ title: '请输入宠物名字', icon: 'none' })
      return
    }
    if (!formData.type) {
      wx.showToast({ title: '请选择宠物类型', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '保存中...' })
      const gender = formData.gender >= 0 ? formData.gender : 0
      if (petId) {
        await PetAPI.update(
          petId, formData.name, formData.type, formData.breed,
          gender, formData.birthday, formData.weight, formData.avatarUrl,
          formData.healthStatus, formData.personality, formData.motto
        )
      } else {
        await PetAPI.create(
          formData.name, formData.type, formData.breed,
          gender, formData.birthday, formData.weight, formData.avatarUrl,
          formData.healthStatus, formData.personality, formData.motto
        )
      }
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1500)
    } catch (error) {
      wx.hideLoading()
      wx.showToast({ title: '保存失败', icon: 'none' })
    }
  },

  // === 删除 ===
  deletePet() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个宠物吗？删除后不可恢复。',
      confirmColor: '#EF4444',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '删除中...' })
            await PetAPI.delete(this.data.petId)
            wx.showToast({ title: '删除成功', icon: 'success' })
            setTimeout(() => wx.navigateBack(), 1500)
          } catch (error) {
            wx.hideLoading()
            wx.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      }
    })
  }
})
