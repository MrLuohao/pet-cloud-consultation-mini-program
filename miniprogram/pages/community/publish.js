// pages/community/publish.js
const { AIAPI, CommunityAPI, PetAPI } = require('../../utils/api')

Page({
  data: {
    content: '',
    mediaUrls: [],
    mediaType: '',
    selectedTopic: null,
    selectedPet: null,
    canPublish: false,
    // 编辑模式
    editMode: false,
    editPostId: null,
    // 可见性设置
    visibility: 0, // 0-公开 1-部分可见 2-仅自己可见
    visibleUserIds: [],
    visibleUserNames: [],
    visibilityText: '公开',
    showVisibilityPicker: false,
    // 话题选择
    showTopicModal: false,
    topicKeyword: '',
    topicList: [],
    // 宠物选择
    showPetModal: false,
    petList: [],
    uploadingVideo: false,
    uploadedMediaAssets: []
  },

  onLoad(options) {
    // 编辑模式
    if (options.editId) {
      this.setData({ editMode: true, editPostId: options.editId })
      this.loadPostForEdit(options.editId)
      wx.setNavigationBarTitle({ title: '编辑动态' })
    } else {
      wx.setNavigationBarTitle({ title: '发布动态' })
    }

    // 如果从话题页进来，预设话题
    if (options.topicId) {
      this.loadTopic(options.topicId)
    }
    this.loadPets()
  },

  // 加载帖子内容用于编辑
  async loadPostForEdit(postId) {
    try {
      wx.showLoading({ title: '加载中...' })
      const post = await CommunityAPI.getPostDetail(postId)

      this.setData({
        content: post.content || '',
        mediaUrls: post.mediaUrls || [],
        mediaType: post.mediaType || '',
        selectedTopic: post.topicId ? { id: post.topicId, name: post.topicName } : null,
        selectedPet: post.pet ? {
          id: post.pet.petId,
          name: post.pet.name,
          breed: post.pet.breed,
          avatarUrl: post.pet.avatarUrl
        } : (post.petName ? { name: post.petName } : null),
        visibility: post.visibility || 0,
        visibleUserIds: post.visibleUserIds || [],
        visibleUserNames: post.visibleUserNames || [],
        visibilityText: this.getVisibilityText(post.visibility || 0),
        canPublish: (post.content || '').trim().length > 0,
        uploadedMediaAssets: []
      })
      wx.hideLoading()
    } catch (error) {
      wx.hideLoading()
      console.error('加载帖子失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  getVisibilityText(visibility) {
    switch (visibility) {
      case 0: return '公开'
      case 1: return '部分可见'
      case 2: return '仅自己可见'
      default: return '公开'
    }
  },

  async loadTopic(topicId) {
    try {
      const topic = await CommunityAPI.getTopicDetail(topicId)
      this.setData({ selectedTopic: topic })
    } catch (error) {
      console.error('加载话题失败:', error)
    }
  },

  async loadPets() {
    try {
      const result = await PetAPI.getList()
      this.setData({ petList: result.list || result || [] })
    } catch (error) {
      console.error('加载宠物失败:', error)
    }
  },

  onContentInput(e) {
    const content = e.detail.value
    this.setData({ content, canPublish: content.trim().length > 0 })
  },

  // ==================== 可见性设置 ====================

  showVisibilityPicker() {
    this.setData({ showVisibilityPicker: true })
  },

  hideVisibilityPicker() {
    this.setData({ showVisibilityPicker: false })
  },

  selectVisibility(e) {
    const { visibility } = e.currentTarget.dataset
    this.setData({ visibility: parseInt(visibility) })
  },

  addVisibleUser() {
    // 简化处理：手动输入用户ID
    wx.showModal({
      title: '添加可见用户',
      editable: true,
      placeholderText: '请输入用户ID或昵称',
      success: (res) => {
        if (res.confirm && res.content) {
          const userInput = res.content.trim()
          if (userInput) {
            this.setData({
              visibleUserIds: [...this.data.visibleUserIds, userInput],
              visibleUserNames: [...this.data.visibleUserNames, userInput]
            })
          }
        }
      }
    })
  },

  removeVisibleUser(e) {
    const { index } = e.currentTarget.dataset
    const userIds = [...this.data.visibleUserIds]
    const userNames = [...this.data.visibleUserNames]
    userIds.splice(index, 1)
    userNames.splice(index, 1)
    this.setData({
      visibleUserIds: userIds,
      visibleUserNames: userNames
    })
  },

  confirmVisibility() {
    const { visibility, visibleUserIds } = this.data

    if (visibility === 1 && visibleUserIds.length === 0) {
      wx.showToast({ title: '请选择可见用户', icon: 'none' })
      return
    }

    this.setData({
      showVisibilityPicker: false,
      visibilityText: this.getVisibilityText(visibility)
    })
  },

  // ==================== 媒体选择 ====================

  // 选择图片
  chooseMedia() {
    if (this.data.mediaType === 'video') {
      this.chooseVideo()
      return
    }

    if (this.data.mediaType === 'image') {
      this.chooseImages()
      return
    }

    wx.showActionSheet({
      itemList: ['添加图片', '添加视频'],
      success: (res) => {
        if (res.tapIndex === 0) {
          this.chooseImages()
          return
        }
        this.chooseVideo()
      }
    })
  },

  chooseImages() {
    const { mediaUrls } = this.data
    const remaining = 9 - mediaUrls.length
    if (remaining <= 0) {
      wx.showToast({ title: '最多上传9张图片', icon: 'none' })
      return
    }

    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newUrls = res.tempFiles.map(f => f.tempFilePath)
        this.setData({
          mediaUrls: [...mediaUrls, ...newUrls],
          mediaType: 'image'
        })
      }
    })
  },

  chooseVideo() {
    wx.chooseVideo({
      sourceType: ['album', 'camera'],
      compressed: true,
      maxDuration: 60,
      camera: 'back',
      success: async (res) => {
        const videoUrl = res.tempFilePath
        if (!videoUrl) return

        this.setData({
          mediaUrls: [videoUrl],
          mediaType: 'video',
          uploadingVideo: false,
          uploadedMediaAssets: []
        })
      }
    })
  },

  // 移除图片
  removeMedia(e) {
    const { index } = e.currentTarget.dataset
    const { mediaUrls } = this.data
    mediaUrls.splice(index, 1)
    this.setData({
      mediaUrls,
      mediaType: mediaUrls.length > 0 ? this.data.mediaType : '',
      uploadedMediaAssets: []
    })
  },

  // 话题选择
  async showTopicPicker() {
    this.setData({ showTopicModal: true })
    await this.loadHotTopics()
  },

  hideTopicPicker() {
    this.setData({ showTopicModal: false, topicKeyword: '' })
  },

  async loadHotTopics() {
    try {
      const topics = await CommunityAPI.getHotTopics()
      this.setData({ topicList: topics || [] })
    } catch (error) {
      console.error('加载话题失败:', error)
    }
  },

  async searchTopic(e) {
    const keyword = e.detail.value
    this.setData({ topicKeyword: keyword })

    if (!keyword.trim()) {
      this.loadHotTopics()
      return
    }

    try {
      const topics = await CommunityAPI.searchTopics(keyword)
      this.setData({ topicList: topics || [] })
    } catch (error) {
      console.error('搜索话题失败:', error)
    }
  },

  selectTopic(e) {
    const item = e.currentTarget.dataset.item
    this.setData({ selectedTopic: item, showTopicModal: false, topicKeyword: '' })
  },

  // 宠物选择
  showPetPicker() {
    this.setData({ showPetModal: true })
  },

  hidePetPicker() {
    this.setData({ showPetModal: false })
  },

  selectPet(e) {
    const item = e.currentTarget.dataset.item
    this.setData({ selectedPet: item, showPetModal: false })
  },

  // 发布
  async publishPost() {
    if (!this.data.canPublish) return
    if (this.data.uploadingVideo) {
      wx.showToast({ title: '视频处理中，请稍候', icon: 'none' })
      return
    }

    wx.showLoading({ title: this.data.editMode ? '保存中...' : '发布中...' })

    try {
      let uploadedAssets = []

      // 判断是否为本地临时文件的辅助函数
      const isLocalTempFile = (url) => {
        if (!url || typeof url !== 'string') return false
        // 微信小程序临时文件的各种可能格式
        return url.startsWith('http://tmp') ||      // http://tmp/xxx 或 http://tmp_xxx
               url.startsWith('http://usr') ||      // http://usr/xxx
               url.startsWith('wxfile://') ||       // wxfile://xxx
               url.startsWith('tmp/') ||            // tmp/xxx
               url.includes('tmp_') ||              // 包含 tmp_
               url.startsWith('file://') ||         // file://xxx
               url.startsWith('content://') ||      // Android content provider
               (url.startsWith('http://') && !url.includes('://') === false && url.split('/')[2].indexOf('.') === -1) // http://xxx 形式但没有域名
      }

      // 判断是否为有效远程URL
      const isRemoteUrl = (url) => {
        if (!url || typeof url !== 'string') return false
        // 必须是 http/https 开头，且不是临时文件
        if (!url.startsWith('http://') && !url.startsWith('https://')) return false
        // 排除临时文件
        if (isLocalTempFile(url)) return false
        return true
      }

      const localUrls = this.data.mediaUrls.filter(isLocalTempFile)
      const remoteUrls = this.data.mediaUrls.filter(isRemoteUrl)

      console.log('发布帖子 - 媒体文件:', {
        total: this.data.mediaUrls,
        localUrls,
        remoteUrls
      })

      if (localUrls.length > 0) {
        uploadedAssets = await this.uploadFiles(localUrls)
        console.log('上传结果:', uploadedAssets)
      }

      const unavailableAsset = uploadedAssets.find(item => !item.availableForSubmit)
      if (unavailableAsset) {
        throw new Error(unavailableAsset.reason || '存在未通过审核的媒体，暂不可发布')
      }

      const uploadedUrls = uploadedAssets.map(item => item.url).filter(Boolean)
      const allUrls = [...remoteUrls, ...uploadedUrls]
      const mediaAssetIds = uploadedAssets.map(item => item.assetId).filter(Boolean)

      // 帖子数据
      const postMediaType = this.data.mediaType || (allUrls.length > 0 ? 'image' : 'text')
      const data = {
        content: this.data.content,
        mediaUrls: remoteUrls,
        mediaAssetIds,
        mediaType: postMediaType,
        petId: this.data.selectedPet?.id,
        topicIds: this.data.selectedTopic ? [this.data.selectedTopic.id] : [],
        visibility: this.data.visibility,
        visibleUserIds: this.data.visibility === 1 ? this.data.visibleUserIds : []
      }

      console.log('即将发送的帖子数据:', JSON.stringify(data, null, 2))

      if (this.data.editMode) {
        // 编辑模式：更新帖子
        await CommunityAPI.updatePost(this.data.editPostId, data)
        wx.hideLoading()
        wx.showToast({ title: '保存成功', icon: 'success' })
      } else {
        // 新建模式：创建帖子
        await CommunityAPI.createPost(data)
        wx.hideLoading()
        wx.showToast({ title: '发布成功', icon: 'success' })
      }

      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (error) {
      wx.hideLoading()
      console.error(this.data.editMode ? '保存失败:' : '发布失败:', error)
      wx.showToast({ title: error.message || (this.data.editMode ? '保存失败' : '发布失败'), icon: 'none' })
    }
  },

  // 上传媒体文件（图片/视频）
  async uploadFiles(urls) {
    const uploadedAssets = []
    for (const url of urls) {
      try {
        console.log('正在上传文件:', url)
        const data = await AIAPI.uploadMedia(url, 'community')
        if (data && data.url) {
          uploadedAssets.push(data)
          console.log('上传成功，asset:', data)
        }
      } catch (error) {
        console.error('上传媒体失败:', error)
        throw error
      }
    }
    console.log('所有上传完成的资产:', uploadedAssets)
    return uploadedAssets
  }
})
