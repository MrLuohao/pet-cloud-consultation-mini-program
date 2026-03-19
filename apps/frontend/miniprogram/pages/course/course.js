// pages/course/course.js - 训练课程逻辑
const { CourseAPI } = require('../../utils/api');

Page({
  data: {
    courses: [],
    loading: false
  },

  onLoad() {
    this.loadCourses();
  },

  // 加载课程列表
  async loadCourses() {
    this.setData({ loading: true });
    try {
      const courses = await CourseAPI.getList();
      const formattedCourses = (courses || []).map((course, index) => ({
        id: course.id,
        name: course.title,
        lessons: course.lessonCount || 0,
        students: this.formatNumber(course.studentCount || 0),
        price: course.price > 0 ? `¥${course.price}` : '免费',
        tag: course.tag || '',
        emoji: this.getCourseEmoji(index),
        color: this.getCourseColor(index)
      }));
      this.setData({ courses: formattedCourses, loading: false });
    } catch (error) {
      console.error('加载课程失败:', error);
      this.setData({ courses: [], loading: false });
      wx.showToast({ title: '暂无课程，请稍后再来', icon: 'none' });
    }
  },

  // 格式化数字
  formatNumber(num) {
    if (num >= 10000) {
      return (num / 10000).toFixed(1) + '万';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'k';
    }
    return num.toString();
  },

  // 获取课程emoji
  getCourseEmoji(index) {
    const emojis = ['🐕', '🦮', '🐱', '🎾', '🎓', '🏆'];
    return emojis[index % emojis.length];
  },

  // 获取课程颜色
  getCourseColor(index) {
    const colors = ['pink', 'blue', 'green', 'yellow'];
    return colors[index % colors.length];
  },

  // 返回
  onBack() {
    wx.navigateBack();
  },

  // 课程点击 → 跳转详情页
  onCourseTap(e) {
    const course = e.currentTarget.dataset.course;
    wx.navigateTo({
      url: `/pages/course/detail/index?id=${course.id}`
    });
  }
});
