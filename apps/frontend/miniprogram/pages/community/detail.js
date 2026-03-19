// pages/community/detail.js
const { CommunityAPI, FollowAPI, AIAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')
const { formatCommunityPostDetail } = require('./post-presenter')

Page({
  data: {
    postId: null,
    post: null,
    comments: [],
    displayComments: [],
    commentSort: 'hot',
    inputContent: '',
    replyToId: null,
    replyToUserId: null,
    replyToNickname: '',
    showReport: false,
    reportReasons: ['垃圾广告', '辱骂攻击', '不当内容', '虚假信息', '其他原因'],
    // 新增：删除和媒体上传相关
    showMoreModal: false,
    showMediaPicker: false,
    commentMediaUrls: [],
    commentMediaType: '', // 'image' 或 'video'
    // 新增：可见性设置相关
    showVisibilityPicker: false,
    tempVisibility: 0,
    tempVisibleUserIds: [],
    tempVisibleUserNames: [],
    visibilityText: '公开'
  },

  onLoad(options) {
    this.setData({ postId: options.id })
    this.loadPostDetail()
    this.loadComments()
  },

  // 加载帖子详情
  async loadPostDetail() {
    try {
      const post = formatCommunityPostDetail(await CommunityAPI.getPostDetail(this.data.postId))
      // 设置可见性文字
      const visibilityText = this.getVisibilityText(post.visibility)
      this.setData({
        post,
        visibilityText
      })
      wx.setNavigationBarTitle({ title: '动态详情' })
    } catch (error) {
      console.error('加载详情失败:', error)
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  // 获取可见性文字描述
  getVisibilityText(visibility) {
    switch (visibility) {
      case 0: return '公开'
      case 1: return '部分可见'
      case 2: return '仅自己可见'
      default: return '公开'
    }
  },

  // 加载评论
  async loadComments() {
    try {
      const comments = await CommunityAPI.getComments(this.data.postId)
      this.setData({ comments: comments || [] })
      this.refreshDisplayComments()
    } catch (error) {
      console.error('加载评论失败:', error)
    }
  },

  // 切换评论排序
  switchCommentSort(e) {
    const { type } = e.currentTarget.dataset
    if (!type || type === this.data.commentSort) return
    this.setData({ commentSort: type })
    this.refreshDisplayComments()
  },

  // 刷新展示评论
  refreshDisplayComments() {
    const { comments, commentSort } = this.data
    const list = [...(comments || [])]

    if (commentSort === 'new') {
      list.sort((a, b) => this.parseTime(b.createTime) - this.parseTime(a.createTime))
    } else {
      list.sort((a, b) => {
        const likeDiff = (b.likeCount || 0) - (a.likeCount || 0)
        if (likeDiff !== 0) return likeDiff
        return this.parseTime(b.createTime) - this.parseTime(a.createTime)
      })
    }

    this.setData({ displayComments: list })
  },

  parseTime(timeValue) {
    const time = new Date(timeValue).getTime()
    return Number.isNaN(time) ? 0 : time
  },

  // 输入内容
  onInputChange(e) {
    this.setData({ inputContent: e.detail.value })
  },

  // 回复评论
  replyTo(e) {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    const { id, userid, nickname } = e.currentTarget.dataset
    this.setData({
      replyToId: id,
      replyToUserId: userid,
      replyToNickname: nickname
    })
  },

  // 提交评论
  async submitComment() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { inputContent, replyToId, replyToUserId, postId, commentMediaUrls, commentMediaType } = this.data
    if (!inputContent.trim() && commentMediaUrls.length === 0) {
      wx.showToast({ title: '请输入评论内容或添加媒体', icon: 'none' })
      return
    }

    wx.showLoading({ title: '发送中...' })

    try {
      // 先上传本地媒体文件，远程URL直接复用
      const isLocalTempFile = (url) => {
        if (!url || typeof url !== 'string') return false
        return url.startsWith('http://tmp') ||
               url.startsWith('http://usr') ||
               url.startsWith('wxfile://') ||
               url.startsWith('tmp/') ||
               url.includes('tmp_') ||
               url.startsWith('file://') ||
               url.startsWith('content://')
      }
      const localUrls = (commentMediaUrls || []).filter(isLocalTempFile)
      const remoteUrls = (commentMediaUrls || []).filter(url => !isLocalTempFile(url))
      const uploadedMediaUrls = [...remoteUrls]

      for (const url of localUrls) {
        try {
          const uploaded = await AIAPI.uploadMedia(url, 'community')
          if (uploaded && uploaded.url) {
            uploadedMediaUrls.push(uploaded.url)
          }
        } catch (error) {
          console.error('上传媒体失败:', error)
        }
      }

      // 提交评论（携带媒体）
      await CommunityAPI.addComment(
        postId,
        inputContent || '',
        replyToId,
        replyToUserId,
        uploadedMediaUrls,
        uploadedMediaUrls.length > 0 ? commentMediaType : ''
      )
      wx.hideLoading()
      wx.showToast({ title: '评论成功', icon: 'success' })

      // 清空输入并刷新评论
      this.setData({
        inputContent: '',
        replyToId: null,
        replyToUserId: null,
        replyToNickname: '',
        commentMediaUrls: [],
        commentMediaType: ''
      })
      this.loadComments()

      // 更新评论数
      if (this.data.post) {
        this.setData({
          'post.commentCount': this.data.post.commentCount + 1
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('评论失败:', error)
      wx.showToast({ title: '评论失败', icon: 'none' })
    }
  },

  // 点赞帖子
  async toggleLike() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { post } = this.data
    const isLiked = post.isLiked

    try {
      if (isLiked) {
        await CommunityAPI.unlikePost(post.id)
      } else {
        await CommunityAPI.likePost(post.id)
      }

      this.setData({
        'post.isLiked': !isLiked,
        'post.likeCount': post.likeCount + (isLiked ? -1 : 1)
      })
    } catch (error) {
      console.error('操作失败:', error)
    }
  },

  // 收藏帖子
  async toggleCollect() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { post } = this.data
    const isCollected = post.isCollected

    try {
      if (isCollected) {
        await CommunityAPI.uncollectPost(post.id)
      } else {
        await CommunityAPI.collectPost(post.id)
      }

      this.setData({
        'post.isCollected': !isCollected,
        'post.collectCount': post.collectCount + (isCollected ? -1 : 1)
      })

      wx.showToast({
        title: isCollected ? '已取消收藏' : '收藏成功',
        icon: 'none'
      })
    } catch (error) {
      console.error('操作失败:', error)
    }
  },

  // 关注/取消关注
  async toggleFollow() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { post } = this.data
    const isFollowed = post.isFollowed

    try {
      if (isFollowed) {
        await FollowAPI.unfollow(post.userId)
      } else {
        await FollowAPI.follow(post.userId)
      }

      this.setData({ 'post.isFollowed': !isFollowed })
    } catch (error) {
      console.error('操作失败:', error)
    }
  },

  // 点赞评论
  async likeComment(e) {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const { id } = e.currentTarget.dataset
    const index = this.data.comments.findIndex(item => String(item.id) === String(id))
    if (index < 0) return
    const comment = this.data.comments[index]
    const isLiked = comment.isLiked

    try {
      if (isLiked) {
        await CommunityAPI.unlikeComment(this.data.postId, id)
      } else {
        await CommunityAPI.likeComment(this.data.postId, id)
      }

      this.setData({
        [`comments[${index}].isLiked`]: !isLiked,
        [`comments[${index}].likeCount`]: (comment.likeCount || 0) + (isLiked ? -1 : 1)
      })
      this.refreshDisplayComments()
    } catch (error) {
      console.error('操作失败:', error)
    }
  },

  // 删除评论
  async deleteComment(e) {
    const { id } = e.currentTarget.dataset

    const result = await new Promise((resolve) => {
      wx.showModal({
        title: '确认删除',
        content: '确定要删除这条评论吗？',
        success: resolve
      })
    })

    if (!result.confirm) return

    try {
      wx.showLoading({ title: '删除中...' })
      await CommunityAPI.deleteComment(this.data.postId, id)
      wx.hideLoading()
      wx.showToast({ title: '删除成功', icon: 'success' })

      // 从列表中移除评论
      const comments = this.data.comments.filter(item => String(item.id) !== String(id))
      this.setData({ comments })

      // 更新帖子评论数
      if (this.data.post) {
        this.setData({
          'post.commentCount': Math.max(0, this.data.post.commentCount - 1)
        })
      }

      this.refreshDisplayComments()
    } catch (error) {
      wx.hideLoading()
      console.error('删除评论失败:', error)
      wx.showToast({ title: '删除失败', icon: 'none' })
    }
  },

  // 预览图片
  previewImage(e) {
    const { urls, current } = e.currentTarget.dataset
    wx.previewImage({ urls, current })
  },

  onDetailVideoFullscreenChange() {},

  onDetailVideoEnterPip() {},

  onDetailVideoLeavePip() {},

  // 跳转到话题
  goToTopic(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/community/topic?id=${id}` })
  },

  // 跳转到用户主页
  goToUserProfile(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/user/profile?id=${id}` })
  },

  // 显示举报弹窗
  showReportModal() {
    this.setData({ showReport: true })
  },

  // 隐藏举报弹窗
  hideReportModal() {
    this.setData({ showReport: false })
  },

  // 提交举报
  async submitReport(e) {
    const reason = e.currentTarget.dataset.reason
    try {
      await CommunityAPI.reportPost(this.data.postId, reason)
      wx.showToast({ title: '举报成功', icon: 'success' })
      this.setData({ showReport: false })
    } catch (error) {
      console.error('举报失败:', error)
      wx.showToast({ title: '举报失败', icon: 'none' })
    }
  },

  // 分享
  onShareAppMessage() {
    const { post } = this.data
    return {
      title: post ? post.content.substring(0, 50) : '分享动态',
      path: `/pages/community/detail?id=${this.data.postId}`
    }
  },

  // ==================== 删除功能 ====================

  // 显示更多操作（删除/举报）
  showMoreActions() {
    this.setData({ showMoreModal: true })
  },

  // 隐藏更多操作
  hideMoreModal() {
    this.setData({ showMoreModal: false })
  },

  // 删除帖子
  async deletePost() {
    const result = await new Promise((resolve) => {
      wx.showModal({
        title: '确认删除',
        content: '删除后不可恢复，确定要删除吗？',
        success: resolve
      })
    })

    if (!result.confirm) {
      this.setData({ showMoreModal: false })
      return
    }

    try {
      wx.showLoading({ title: '删除中...' })
      await CommunityAPI.deletePost(this.data.postId)
      wx.hideLoading()
      wx.showToast({ title: '删除成功', icon: 'success' })

      // 返回上一页
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (error) {
      wx.hideLoading()
      console.error('删除失败:', error)
      wx.showToast({ title: '删除失败', icon: 'none' })
    }
  },

  // ==================== 评论媒体上传功能 ====================

  // 显示媒体选择器
  showMediaPicker() {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.setData({ showMediaPicker: true })
  },

  // 隐藏媒体选择器
  hideMediaPicker() {
    this.setData({ showMediaPicker: false })
  },

  // 选择评论图片
  chooseCommentImage() {
    const maxCount = 9 - this.data.commentMediaUrls.length
    if (maxCount <= 0) {
      wx.showToast({ title: '最多上传9张图片', icon: 'none' })
      return
    }

    wx.chooseMedia({
      count: maxCount,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newUrls = res.tempFiles.map(f => f.tempFilePath)
        this.setData({
          commentMediaUrls: [...this.data.commentMediaUrls, ...newUrls],
          commentMediaType: 'image',
          showMediaPicker: false
        })
      },
      fail: () => {
        wx.showToast({ title: '选择失败', icon: 'none' })
      }
    })
  },

  // 选择评论视频
  chooseCommentVideo() {
    if (this.data.commentMediaUrls.length > 0) {
      wx.showToast({ title: '视频只能上传1个，请先清除已有媒体', icon: 'none' })
      return
    }

    wx.chooseVideo({
      sourceType: ['album', 'camera'],
      compressed: true,
      maxDuration: 60,
      success: (res) => {
        const videoUrl = res.tempFilePath
        this.setData({
          commentMediaUrls: [videoUrl],
          commentMediaType: 'video',
          showMediaPicker: false
        })
      },
      fail: () => {
        wx.showToast({ title: '选择失败', icon: 'none' })
      }
    })
  },

  // 移除评论媒体
  removeCommentMedia(e) {
    const { index } = e.currentTarget.dataset
    const urls = [...this.data.commentMediaUrls]
    urls.splice(index, 1)
    this.setData({
      commentMediaUrls: urls,
      commentMediaType: urls.length > 0 ? this.data.commentMediaType : ''
    })
  },

  // 预览评论图片
  previewCommentImage(e) {
    const { urls, current } = e.currentTarget.dataset
    wx.previewImage({ urls, current })
  },

  // ==================== 帖子编辑功能 ====================

  // 编辑帖子 - 跳转到发布页
  editPost() {
    this.setData({ showMoreModal: false })
    const { post } = this.data
    // 跳转到发布页，带上编辑参数
    wx.navigateTo({
      url: `/pages/community/publish?editId=${post.id}`
    })
  },

  // ==================== 可见性设置功能 ====================

  // 显示可见性设置弹窗
  showVisibilityPicker() {
    const { post } = this.data
    this.setData({
      showMoreModal: false,
      showVisibilityPicker: true,
      tempVisibility: post.visibility || 0,
      tempVisibleUserIds: post.visibleUserIds || [],
      tempVisibleUserNames: post.visibleUserNames || []
    })
  },

  // 隐藏可见性设置弹窗
  hideVisibilityPicker() {
    this.setData({ showVisibilityPicker: false })
  },

  // 选择可见性类型
  selectVisibility(e) {
    const { visibility } = e.currentTarget.dataset
    this.setData({
      tempVisibility: parseInt(visibility)
    })
  },

  // 添加可见用户
  addVisibleUser() {
    // 获取当前用户的好友列表或关注列表
    wx.chooseContact({
      success: (res) => {
        const userIds = res.userIds || []
        const userNames = res.userNames || []

        // 合并已有用户
        const existingIds = this.data.tempVisibleUserIds
        const existingNames = this.data.tempVisibleUserNames
        const newIds = [...new Set([...existingIds, ...userIds])]
        const newNames = [...existingNames]

        userNames.forEach((name, idx) => {
          if (!existingIds.includes(userIds[idx])) {
            newNames.push(name)
          }
        })

        this.setData({
          tempVisibleUserIds: newIds,
          tempVisibleUserNames: newNames
        })
      },
      fail: () => {
        // 如果 chooseContact 不可用，使用手动输入
        wx.showModal({
          title: '添加可见用户',
          editable: true,
          placeholderText: '请输入用户ID',
          success: (res) => {
            if (res.confirm && res.content) {
              const userId = res.content.trim()
              if (userId && !this.data.tempVisibleUserIds.includes(userId)) {
                this.setData({
                  tempVisibleUserIds: [...this.data.tempVisibleUserIds, userId],
                  tempVisibleUserNames: [...this.data.tempVisibleUserNames, `用户${userId}`]
                })
              }
            }
          }
        })
      }
    })
  },

  // 移除可见用户
  removeVisibleUser(e) {
    const { index } = e.currentTarget.dataset
    const userIds = [...this.data.tempVisibleUserIds]
    const userNames = [...this.data.tempVisibleUserNames]
    userIds.splice(index, 1)
    userNames.splice(index, 1)
    this.setData({
      tempVisibleUserIds: userIds,
      tempVisibleUserNames: userNames
    })
  },

  // 确认可见性设置
  async confirmVisibility() {
    const { tempVisibility, tempVisibleUserIds, post } = this.data

    // 如果选择了部分可见但没有选择用户，提示
    if (tempVisibility === 1 && tempVisibleUserIds.length === 0) {
      wx.showToast({ title: '请选择可见用户', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '保存中...' })

      // 调用更新接口
      await CommunityAPI.updatePost(post.id, {
        visibility: tempVisibility,
        visibleUserIds: tempVisibility === 1 ? tempVisibleUserIds : []
      })

      wx.hideLoading()
      wx.showToast({ title: '设置成功', icon: 'success' })

      // 更新本地状态
      this.setData({
        showVisibilityPicker: false,
        'post.visibility': tempVisibility,
        'post.visibleUserIds': tempVisibleUserIds,
        'post.visibleUserNames': this.data.tempVisibleUserNames,
        visibilityText: this.getVisibilityText(tempVisibility)
      })
    } catch (error) {
      wx.hideLoading()
      console.error('设置可见性失败:', error)
      wx.showToast({ title: '设置失败', icon: 'none' })
    }
  }
})
