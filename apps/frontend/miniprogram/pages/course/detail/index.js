// pages/course/detail/index.js - 课程详情页
const app = getApp();
const { CourseAPI } = require('../../../utils/api');

Page({
  data: {
    course: null,
    reviews: [],
    loading: true,
    reviewContent: '',
    reviewRating: 5,
    showReviewModal: false
  },

  onLoad(options) {
    this.courseId = options.id;
    this.loadCourseDetail();
    this.loadReviews();
  },

  async loadCourseDetail() {
    try {
      const course = await CourseAPI.getDetail(this.courseId);
      this.setData({ course, loading: false });
      wx.setNavigationBarTitle({ title: course.title || '课程详情' });
    } catch (error) {
      console.error('加载课程详情失败:', error);
      this.setData({ loading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  async loadReviews() {
    try {
      const reviews = await CourseAPI.getReviews(this.courseId);
      this.setData({ reviews: reviews || [] });
    } catch (error) {
      console.error('加载评价失败:', error);
    }
  },

  // 点击章节
  onChapterTap(e) {
    const chapter = e.currentTarget.dataset.chapter;
    const { course } = this.data;

    if (!app.globalData.token) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }

    wx.navigateTo({
      url: `/pages/course/player/index?courseId=${this.courseId}&chapterId=${chapter.id}&chapterTitle=${encodeURIComponent(chapter.title)}&videoUrl=${encodeURIComponent(chapter.videoUrl || '')}`
    });
  },

  // 开始/继续学习
  onStartLearn() {
    if (!app.globalData.token) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const { course } = this.data;
    const chapters = course.chapters || [];
    if (chapters.length === 0) {
      wx.showToast({ title: '暂无课时', icon: 'none' });
      return;
    }
    // 跳转到第一章或上次进度章节
    const firstChapter = chapters[0];
    wx.navigateTo({
      url: `/pages/course/player/index?courseId=${this.courseId}&chapterId=${firstChapter.id}&chapterTitle=${encodeURIComponent(firstChapter.title)}&videoUrl=${encodeURIComponent(firstChapter.videoUrl || '')}`
    });
  },

  // 显示评价弹窗
  onShowReview() {
    if (!app.globalData.token) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.setData({ showReviewModal: true });
  },

  onHideReview() {
    this.setData({ showReviewModal: false });
  },

  onRatingChange(e) {
    this.setData({ reviewRating: e.detail.value });
  },

  onReviewInput(e) {
    this.setData({ reviewContent: e.detail.value });
  },

  async onSubmitReview() {
    const { reviewRating, reviewContent } = this.data;
    if (!reviewContent.trim()) {
      wx.showToast({ title: '请输入评价内容', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '提交中...' });
    try {
      await CourseAPI.submitReview(this.courseId, { rating: reviewRating, content: reviewContent });
      wx.hideLoading();
      wx.showToast({ title: '评价成功', icon: 'success' });
      this.setData({ showReviewModal: false, reviewContent: '', reviewRating: 5 });
      this.loadReviews();
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: error.message || '评价失败', icon: 'none' });
    }
  },

  formatRating(rating) {
    return '★'.repeat(rating || 0) + '☆'.repeat(5 - (rating || 0));
  }
});
