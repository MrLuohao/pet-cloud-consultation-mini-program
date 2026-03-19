// pages/community/index.js - 宠物社区（Story 5.3）
const { CommunityAPI, AIAPI, isLoggedIn, navigateToLogin } = require('../../utils/api');
const { formatCommunityPostCard } = require('./post-presenter');

Page({
  data: {
    posts: [],
    page: 1,
    pageSize: 20,
    loading: false,
    hasMore: true,
    showPostModal: false,
    newPostContent: '',
    newPostImages: [],
    posting: false,

    // 帖子详情/评论弹窗
    showCommentModal: false,
    activePost: null,
    comments: [],
    commentInput: '',
    commentPage: 1,
    loadingComments: false,

    // 用于判断是否需要刷新
    lastLoadTime: 0
  },

  onLoad() {
    this.loadPosts(true);
  },

  onShow() {
    // 从发布页返回时自动刷新（距离上次加载超过2秒才刷新，避免频繁刷新）
    const now = Date.now()
    if (now - this.data.lastLoadTime > 2000) {
      this.loadPosts(true)
    }
  },

  onPullDownRefresh() {
    this.loadPosts(true).then(() => wx.stopPullDownRefresh());
  },

  onReachBottom() {
    if (!this.data.loading && this.data.hasMore) {
      this.loadPosts(false);
    }
  },

  async loadPosts(reset = false) {
    if (this.data.loading) return;
    const page = reset ? 1 : this.data.page;
    this.setData({ loading: true });
    try {
      const result = await CommunityAPI.getPosts(page, this.data.pageSize);
      const sourcePosts = Array.isArray(result && result.records)
        ? result.records
        : (Array.isArray(result && result.list) ? result.list : (Array.isArray(result) ? result : []));
      const newPosts = Array.isArray(sourcePosts)
        ? sourcePosts.map(post => formatCommunityPostCard(post))
        : [];
      const total = result && result.total ? result.total : newPosts.length;
      const posts = reset ? newPosts : [...this.data.posts, ...newPosts];
      this.setData({
        posts,
        page: page + 1,
        hasMore: posts.length < total,
        lastLoadTime: Date.now()
      });
    } catch (e) {
      console.error('加载帖子失败:', e);
    } finally {
      this.setData({ loading: false });
    }
  },

  // 打开发帖弹窗
  onOpenPost() {
    if (!isLoggedIn()) { navigateToLogin(); return; }
    this.setData({ showPostModal: true, newPostContent: '', newPostImages: [] });
  },

  onClosePost() {
    this.setData({ showPostModal: false });
  },

  onPostInput(e) {
    this.setData({ newPostContent: e.detail.value });
  },

  async onAddPostImage() {
    if (this.data.newPostImages.length >= 6) {
      wx.showToast({ title: '最多6张图片', icon: 'none' });
      return;
    }
    const res = await new Promise((resolve, reject) =>
      wx.chooseMedia({
        count: 6 - this.data.newPostImages.length,
        mediaType: ['image'],
        success: resolve, fail: reject
      })
    );
    const files = res.tempFiles.map(f => f.tempFilePath);
    this.setData({ newPostImages: [...this.data.newPostImages, ...files] });
  },

  onRemovePostImage(e) {
    const idx = e.currentTarget.dataset.index;
    const imgs = [...this.data.newPostImages];
    imgs.splice(idx, 1);
    this.setData({ newPostImages: imgs });
  },

  // 发布帖子
  async onSubmitPost() {
    const { newPostContent, newPostImages, posting } = this.data;
    if (posting) return;
    if (!newPostContent.trim()) {
      wx.showToast({ title: '请输入内容', icon: 'none' });
      return;
    }
    this.setData({ posting: true });
    wx.showLoading({ title: '发布中...' });
    try {
      // 先上传所有图片到服务器
      const uploadedUrls = [];
      for (const tempPath of newPostImages) {
        const url = await AIAPI.uploadImage(tempPath);
        uploadedUrls.push(url);
      }
      await CommunityAPI.createPost({
        content: newPostContent,
        mediaUrls: uploadedUrls,
        mediaType: uploadedUrls.length > 0 ? 'image' : 'text'
      });
      wx.hideLoading();
      wx.showToast({ title: '发布成功！', icon: 'success' });
      this.setData({ showPostModal: false });
      setTimeout(() => this.loadPosts(true), 800);
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '发布失败', icon: 'none' });
    } finally {
      this.setData({ posting: false });
    }
  },

  // 点赞/取消点赞
  async onToggleLike(e) {
    if (!isLoggedIn()) { navigateToLogin(); return; }
    const { id, isliked, idx } = e.currentTarget.dataset;
    try {
      if (isliked) {
        await CommunityAPI.unlikePost(id);
      } else {
        await CommunityAPI.likePost(id);
      }
      const posts = [...this.data.posts];
      posts[idx] = {
        ...posts[idx],
        isLiked: !isliked,
        likeCount: isliked ? posts[idx].likeCount - 1 : posts[idx].likeCount + 1
      };
      this.setData({ posts });
    } catch (e) {
      console.error('点赞操作失败:', e);
      wx.showToast({ title: '操作失败，请重试', icon: 'none' });
    }
  },

  // 打开评论
  async onOpenComments(e) {
    const { post, idx } = e.currentTarget.dataset;
    this.setData({
      showCommentModal: true,
      activePost: { ...post, idx },
      comments: [],
      commentInput: '',
      commentPage: 1
    });
    this.loadComments(post.id);
  },

  onCloseComments() {
    this.setData({ showCommentModal: false, activePost: null });
  },

  async loadComments(postId) {
    this.setData({ loadingComments: true });
    try {
      const list = await CommunityAPI.getComments(postId);
      this.setData({ comments: list || [] });
    } catch (e) {
      console.error('加载评论失败:', e);
    } finally {
      this.setData({ loadingComments: false });
    }
  },

  onCommentInput(e) {
    this.setData({ commentInput: e.detail.value });
  },

  // 发送评论
  async onSendComment() {
    if (!isLoggedIn()) { navigateToLogin(); return; }
    const { commentInput, activePost } = this.data;
    if (!commentInput.trim()) return;
    try {
      await CommunityAPI.addComment(activePost.id, commentInput);
      this.setData({ commentInput: '' });
      this.loadComments(activePost.id);
      // 同步更新帖子列表评论数
      const posts = [...this.data.posts];
      if (activePost.idx !== undefined) {
        posts[activePost.idx] = {
          ...posts[activePost.idx],
          commentCount: posts[activePost.idx].commentCount + 1
        };
        this.setData({ posts });
      }
    } catch (e) {
      wx.showToast({ title: '评论失败', icon: 'none' });
    }
  },

  // 预览图片
  onPreviewMedia(e) {
    const { urls, current } = e.currentTarget.dataset;
    wx.previewImage({ urls, current });
  }
});
