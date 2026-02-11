// pages/course/player/index.js - 课程播放器页
const { CourseAPI } = require('../../../utils/api');

const PROGRESS_REPORT_INTERVAL = 5000; // 每5秒上报进度

Page({
  data: {
    courseId: null,
    chapterId: null,
    chapterTitle: '',
    videoUrl: '',
    playing: false,
    progress: 0,
    currentTime: 0,
    duration: 0
  },

  _progressTimer: null,
  _videoContext: null,

  onLoad(options) {
    this.courseId = options.courseId;
    this.setData({
      courseId: options.courseId,
      chapterId: options.chapterId,
      chapterTitle: decodeURIComponent(options.chapterTitle || ''),
      videoUrl: decodeURIComponent(options.videoUrl || '')
    });
    wx.setNavigationBarTitle({ title: this.data.chapterTitle || '课程学习' });
    this._videoContext = wx.createVideoContext('courseVideo');
  },

  onUnload() {
    this._stopProgressTimer();
  },

  onHide() {
    this._stopProgressTimer();
    if (this._videoContext) {
      this._videoContext.pause();
    }
  },

  // 播放事件
  onVideoPlay() {
    this.setData({ playing: true });
    this._startProgressTimer();
  },

  // 暂停事件
  onVideoPause() {
    this.setData({ playing: false });
    this._stopProgressTimer();
    this._reportProgress();
  },

  // 播放结束
  onVideoEnded() {
    this.setData({ playing: false, progress: 100 });
    this._stopProgressTimer();
    this._reportProgress(100);
    wx.showToast({ title: '本章节已完成！', icon: 'success' });
  },

  // 时间更新
  onTimeUpdate(e) {
    const { currentTime, duration } = e.detail;
    if (duration > 0) {
      const progress = Math.min(100, Math.floor((currentTime / duration) * 100));
      this.setData({ currentTime, duration, progress });
    }
  },

  onVideoError(e) {
    console.error('视频播放错误:', e.detail);
    wx.showToast({ title: '视频加载失败', icon: 'none' });
  },

  _startProgressTimer() {
    this._stopProgressTimer();
    this._progressTimer = setInterval(() => {
      this._reportProgress();
    }, PROGRESS_REPORT_INTERVAL);
  },

  _stopProgressTimer() {
    if (this._progressTimer) {
      clearInterval(this._progressTimer);
      this._progressTimer = null;
    }
  },

  async _reportProgress(overrideProgress) {
    const { courseId, chapterId, progress, currentTime } = this.data;
    if (!courseId) return;
    const reportProgress = overrideProgress !== undefined ? overrideProgress : progress;
    try {
      await CourseAPI.updateProgress(courseId, {
        chapterId,
        progress: reportProgress,
        watchSeconds: Math.floor(currentTime)
      });
    } catch (error) {
      console.error('上报进度失败:', error);
    }
  }
});
