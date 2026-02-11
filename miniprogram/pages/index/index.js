/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  首页逻辑 | Home Page Logic                                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const app = getApp();
const { ArticleAPI, PetAPI, TaskAPI, HealthAPI, parseDate } = require('../../utils/api');

Page({
  data: {
    // 搜索相关
    keyword: '',
    searchFocused: false,

    // UI状态
    heroLoaded: false,
    loading: false,
    hasMore: true,
    activeTab: 0,
    fabMenuOpen: false,

    // 悬浮按钮拖拽相关
    fabX: 0,
    fabY: 0,
    fabDragging: false,
    fabTouchStartTime: 0,
    fabInitialized: false,

    // 宠物相关
    pets: [],
    petsLoading: false,
    currentPetIndex: 0,
    petTouchStartX: 0,
    petTouchStartY: 0,

    // 扇形展开相关
    fanExpanded: false,
    fanAnimating: false,
    touchedCardIndex: -1,
    fanCardStyles: [],
    petTouchStartTime: 0,

    // 健康提醒
    reminders: [],

    // 今日任务
    tasks: [],
    totalPoints: 0,
    taskProgress: 0,
    tasksLoading: false,

    // 新手引导
    showGuide: false,
    guideStep: 0,
    guideSteps: [
      {
        title: '欢迎使用伴宠云诊！',
        desc: '您的专属宠物健康管理平台，让爱宠健康每一天。',
        icon: '🎉'
      },
      {
        title: 'AI智能诊断',
        desc: '点击顶部炫酷卡片，上传照片即可快速诊断宠物健康状况。',
        icon: '🏥'
      },
      {
        title: '快捷服务',
        desc: 'AI诊断、AI助手、训练课程、美容服务，一键直达。',
        icon: '⚡'
      },
      {
        title: '每日任务',
        desc: '完成签到、喂食打卡、遛狗记录，获取积分奖励！',
        icon: '📅'
      },
      {
        title: 'AI悬浮助手',
        desc: '右下角✨按钮，随时唤起AI诊断、AI聊天、人工咨询。',
        icon: '✨'
      },
      {
        title: '开始探索吧！',
        desc: '快去添加您的爱宠，开始记录健康生活！',
        icon: '🚀'
      }
    ],

    // 庆祝撒花
    showConfetti: false,
    confettiPieces: [],

    // Toast
    toastShow: false,
    toastType: 'success',
    toastIcon: '✓',
    toastText: '',

    // 快捷服务
    quickServices: [
      { id: 1, name: 'AI诊断', icon: '🏥', color: 'purple', action: 'goToDiagnosis' },
      { id: 2, name: 'AI助手', icon: '🤖', color: 'blue', action: 'goToChat' },
      { id: 3, name: '训练课程', icon: '🎓', color: 'green', action: 'goToCourse' },
      { id: 4, name: '美容服务', icon: '✨', color: 'orange', action: 'goToBeauty' }
    ],

    // 数据统计
    stats: [
      { value: '10万+', label: '服务用户' },
      { value: '500+', label: '合作医生' },
      { value: '98%', label: '好评率' }
    ],

    // 标签页
    tabs: ['推荐', '健康', '护理', '训练'],

    // 文章列表
    articles: [],
    articlePage: 1,

    // 登录状态
    isLoggedIn: false,

    // 下拉刷新
    isRefreshing: false
  },

  onLoad() {
    // 检查登录状态
    const isLoggedIn = !!app.globalData?.token;
    this.setData({
      isLoggedIn,
      heroLoaded: true
    });

    // 延迟初始化悬浮按钮位置（确保页面渲染完成）
    setTimeout(() => {
      this.initFabPosition();
      this.setData({ fabInitialized: true });
    }, 100);

    this.loadArticles();

    // 加载宠物列表和提醒
    if (isLoggedIn) {
      this.loadPets();
      this.loadReminders();
      this.loadTasks();

      // 检查是否需要显示新手引导
      this.checkShowGuide();
    }
  },

  onShow() {
    // 每次显示页面时更新登录状态
    const isLoggedIn = !!app.globalData?.token;
    this.setData({ isLoggedIn });

    // 刷新文章列表以获取最新数据（点赞数、收藏状态等）
    const currentTab = this.data.activeTab;
    const tabs = ['推荐', '健康', '护理', '训练'];
    const tagMap = [null, '健康', '护理', '训练'];
    this.loadArticles(tagMap[currentTab]);

    // 每次回到首页都刷新宠物和提醒（确保添加/编辑/删除后数据同步）
    if (isLoggedIn) {
      this.loadPets();
      this.loadReminders();
      this.loadTasks();
    }

    // 检查是否从其他页面完成任务返回（如AI诊断）
    const needRefreshTasks = app.globalData?.needRefreshTasks;
    if (needRefreshTasks) {
      app.globalData.needRefreshTasks = false;
      this.loadTasks();
    }
  },

  onReady() {
    // 页面渲染完成后的动画
    setTimeout(() => {
      this.setData({ heroLoaded: true });
    }, 100);
  },

  // 下拉刷新
  async onPullDownRefresh() {
    try {
      // 并行刷新所有数据
      await Promise.all([
        this.loadArticles(),
        this.loadPets(),
        this.loadTasks()
      ]);
    } catch (e) {
      console.error('刷新失败:', e);
    } finally {
      wx.stopPullDownRefresh();
    }
  },

  // ==================== 搜索相关 ====================
  onSearchInput(e) {
    this.setData({
      keyword: e.detail.value
    });
  },

  onSearchFocus() {
    this.setData({ searchFocused: true });
  },

  onSearchBlur() {
    this.setData({ searchFocused: false });
  },

  // 点击搜索框直接跳转到搜索页
  focusSearch() {
    wx.navigateTo({
      url: '/pages/search/result'
    });
  },

  onClearSearch() {
    this.setData({ keyword: '' });
  },

  onSearch() {
    // 输入框回车时跳转
    this.focusSearch();
  },

  // ==================== 宠物相关 ====================

  // 扇形角度计算
  computeFanStyles(count) {
    const angleStep = 40 / count;
    return Array.from({length: count}, (_, i) => {
      const center = i - (count - 1) / 2;
      const angle = center * angleStep;
      const tx = angle * 2;
      const ty = -Math.abs(angle) * 1.5;
      const z = 50 - Math.abs(Math.round(center));
      return `--fan-angle:${angle}deg; --fan-tx:${tx}rpx; --fan-ty:${ty}rpx; --fan-z:${z};`;
    });
  },

  // 切换扇形展开/收起
  toggleFanDeck(expand) {
    if (expand) {
      const styles = this.computeFanStyles(this.data.pets.length);
      this.setData({
        fanExpanded: true,
        fanAnimating: false,
        fanCardStyles: styles,
        touchedCardIndex: -1
      });
      setTimeout(() => {
        this.setData({ fanAnimating: true });
      }, 500);
    } else {
      this.setData({
        fanExpanded: false,
        fanAnimating: false,
        touchedCardIndex: -1,
        fanCardStyles: []
      });
    }
  },

  // 展开/收起按钮点击
  onFanToggleTap() {
    this.toggleFanDeck(!this.data.fanExpanded);
  },

  // 扇形模式卡片触摸开始
  onFanCardTouchStart(e) {
    if (!this.data.fanExpanded) return;
    const index = e.currentTarget.dataset.index;
    this.setData({ touchedCardIndex: index });
  },

  // 扇形模式卡片触摸结束
  onFanCardTouchEnd() {
    if (!this.data.fanExpanded) return;
    this.setData({ touchedCardIndex: -1 });
  },

  async loadPets() {
    try {
      this.setData({ petsLoading: true });
      const pets = await PetAPI.getList();

      // 格式化宠物数据
      const formattedPets = (pets || []).map(pet => {
        const hs = (pet.healthStatus || '').trim()
        // 将用户输入的健康状态映射为展示类型
        let healthType = 'unknown'
        let healthLabel = '未检测'
        if (hs) {
          if (['健康', '良好', '正常', 'healthy'].includes(hs)) {
            healthType = 'healthy'
            healthLabel = '健康'
          } else {
            healthType = 'attention'
            healthLabel = hs
          }
        }
        return {
          id: pet.id,
          name: pet.name,
          type: pet.type,
          breed: pet.breed,
          gender: pet.gender,
          genderText: pet.gender === 1 ? '公' : pet.gender === 2 ? '母' : '未知',
          genderIcon: pet.gender === 1 ? '♂' : pet.gender === 2 ? '♀' : '',
          age: this.calculateAge(pet.birthday),
          weight: pet.weight,
          avatarUrl: pet.avatarUrl,
          healthType,
          healthLabel,
          personality: pet.personality || '',
          motto: pet.motto || ''
        }
      });

      this.setData({
        pets: formattedPets,
        petsLoading: false,
        currentPetIndex: formattedPets.length > 0 ? Math.min(this.data.currentPetIndex, formattedPets.length - 1) : 0
      });
    } catch (error) {
      console.error('加载宠物列表失败:', error);
      this.setData({
        pets: [],
        petsLoading: false
      });
    }
  },

  calculateAge(birthday) {
    if (!birthday) return '';
    const birth = parseDate(birthday);
    const now = new Date();
    const diffYears = now.getFullYear() - birth.getFullYear();
    const diffMonths = now.getMonth() - birth.getMonth();

    if (diffYears > 0) {
      return `${diffYears}岁`;
    } else if (diffMonths > 0) {
      return `${diffMonths}个月`;
    }
    return '幼崽';
  },

  goToPetDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/pet/edit?id=${id}`
    });
  },

  goToAddPet() {
    // 检查是否已达到最大数量
    if (this.data.pets.length >= 5) {
      wx.showToast({
        title: '最多添加5只宠物',
        icon: 'none'
      });
      return;
    }
    wx.navigateTo({
      url: '/pages/pet/edit'
    });
  },

  // 宠物卡片点击
  onPetCardTap(e) {
    const index = e.currentTarget.dataset.index;
    const id = e.currentTarget.dataset.id;

    // 扇形模式下点击卡片 → 选中并收起
    if (this.data.fanExpanded) {
      this.setData({
        currentPetIndex: index,
        fanExpanded: false,
        fanAnimating: false,
        touchedCardIndex: -1,
        fanCardStyles: []
      });
      return;
    }

    if (index === this.data.currentPetIndex) {
      // 点击当前卡片，跳转到详情
      wx.navigateTo({
        url: `/pages/pet/edit?id=${id}`
      });
    } else {
      // 点击其他卡片，切换到该卡片
      this.setData({ currentPetIndex: index });
    }
  },

  // 指示器点击
  onIndicatorTap(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ currentPetIndex: index });
  },

  // 宠物卡片触摸开始
  onPetTouchStart(e) {
    this.setData({
      petTouchStartX: e.touches[0].clientX,
      petTouchStartY: e.touches[0].clientY,
      petTouchStartTime: Date.now()
    });
  },

  // 宠物卡片触摸结束
  onPetTouchEnd(e) {
    // 扇形模式下跳过滑动切换逻辑
    if (this.data.fanExpanded) return;

    const { petTouchStartX, currentPetIndex, pets } = this.data;
    const touchEndX = e.changedTouches[0].clientX;
    const diffX = touchEndX - petTouchStartX;

    // 滑动距离大于50px才触发切换
    if (Math.abs(diffX) > 50) {
      if (diffX > 0 && currentPetIndex > 0) {
        // 向右滑动，显示上一张
        this.setData({ currentPetIndex: currentPetIndex - 1 });
      } else if (diffX < 0 && currentPetIndex < pets.length - 1) {
        // 向左滑动，显示下一张
        this.setData({ currentPetIndex: currentPetIndex + 1 });
      }
    }
  },

  // ==================== 健康提醒相关 ====================
  async loadReminders() {
    try {
      const list = await HealthAPI.getReminders();
      const reminders = (list || []).slice(0, 3).map(r => ({
        id: r.id,
        type: r.reminderType,
        icon: this.getReminderIcon(r.reminderType),
        title: r.title,
        petName: r.petName || '',
        date: this.formatReminderDate(r.remindDate),
        actionText: r.reminderType === 'vaccine' || r.reminderType === 'checkup' ? '预约' : '购买'
      }));
      this.setData({ reminders });
    } catch (error) {
      console.error('加载提醒失败:', error);
      this.setData({ reminders: [] });
    }
  },

  getReminderIcon(type) {
    const map = { vaccine: '💉', checkup: '🏥', medicine: '💊', deworming: '🐛', other: '🔔' };
    return map[type] || '🔔';
  },

  formatReminderDate(dateStr) {
    if (!dateStr) return '';
    const diff = parseDate(dateStr) - new Date();
    const days = Math.ceil(diff / (1000 * 60 * 60 * 24));
    if (days < 0) return '已逾期';
    if (days === 0) return '今天';
    if (days === 1) return '明天';
    return `${days}天后到期`;
  },

  handleReminder(e) {
    const id = e.currentTarget.dataset.id;
    const reminder = this.data.reminders.find(r => r.id === id);

    if (!reminder) return;

    if (reminder.type === 'vaccine' || reminder.type === 'checkup') {
      wx.navigateTo({
        url: '/pages/consultation/create'
      });
    } else {
      wx.switchTab({
        url: '/pages/shop/shop'
      });
    }
  },

  // ==================== 今日任务相关 ====================
  async loadTasks() {
    try {
      this.setData({ tasksLoading: true });

      const [tasks, pointsData] = await Promise.all([
        TaskAPI.getTodayTasks(),
        TaskAPI.getPoints()
      ]);

      const totalPoints = pointsData?.total || 0;
      const completedCount = (tasks || []).filter(t => t.completed).length;
      const taskProgress = tasks && tasks.length > 0 ? Math.round((completedCount / tasks.length) * 100) : 0;

      this.setData({
        tasks: tasks || [],
        totalPoints,
        taskProgress,
        tasksLoading: false
      });
    } catch (error) {
      console.error('加载任务失败:', error);
      this.setData({ tasks: [], tasksLoading: false });
    }
  },

  async handleTask(e) {
    const id = e.currentTarget.dataset.id;
    const task = this.data.tasks.find(t => t.id === id);

    if (!task || task.completed) return;

    // 根据任务类型执行不同操作
    const taskCode = task.code;

    // AI诊断任务：跳转到诊断页面
    if (taskCode === 'AI_DIAGNOSIS') {
      wx.navigateTo({
        url: '/pages/diagnosis/diagnosis'
      });
      return;
    }

    // 每日签到任务：直接完成
    if (taskCode === 'DAILY_SIGN') {
      await this.completeTaskDirectly(task);
      return;
    }

    // 其他任务：提示功能开发中
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  /**
   * 直接完成任务（用于签到类任务）
   */
  async completeTaskDirectly(task) {
    const id = task.id;

    // 更新任务状态
    const tasks = this.data.tasks.map(t => {
      if (t.id === id) {
        return { ...t, completed: true };
      }
      return t;
    });

    // 计算新进度
    const completedCount = tasks.filter(t => t.completed).length;
    const taskProgress = Math.round((completedCount / tasks.length) * 100);
    const newPoints = this.data.totalPoints + task.points;

    this.setData({
      tasks,
      taskProgress,
      totalPoints: newPoints
    });

    // 显示庆祝效果
    this.showToast('success', '✓', `+${task.points} 积分`);

    // 尝试调用API
    try {
      await TaskAPI.completeTask(id);
    } catch (e) {
      console.log('任务完成同步失败，已本地更新');
    }

    // 如果所有任务完成，显示撒花
    if (completedCount === tasks.length) {
      setTimeout(() => {
        this.showConfetti();
      }, 500);
    }
  },

  // ==================== 新手引导相关 ====================
  checkShowGuide() {
    // 检查是否是首次使用
    const hasShownGuide = wx.getStorageSync('hasShownGuide');
    if (!hasShownGuide) {
      this.setData({ showGuide: true });
    }
  },

  nextGuideStep() {
    const { guideStep, guideSteps } = this.data;

    if (guideStep < guideSteps.length - 1) {
      this.setData({ guideStep: guideStep + 1 });
    } else {
      // 完成引导
      this.completeGuide();
    }
  },

  skipGuide() {
    this.completeGuide();
  },

  completeGuide() {
    wx.setStorageSync('hasShownGuide', true);
    this.setData({ showGuide: false });

    // 显示欢迎Toast
    this.showToast('success', '🎉', '欢迎加入伴宠云诊！');
  },

  // 长按搜索栏重置新手引导（开发者调试用）
  resetGuide() {
    wx.removeStorageSync('hasShownGuide');
    this.setData({
      showGuide: true,
      guideStep: 0
    });
    wx.showToast({
      title: '已重置新手引导',
      icon: 'none'
    });
  },

  preventTouchMove() {
    // 阻止遮罩层下的滚动
  },

  // ==================== Toast 相关 ====================
  showToast(type, icon, text, duration = 1500) {
    this.setData({
      toastShow: true,
      toastType: type,
      toastIcon: icon,
      toastText: text
    });

    setTimeout(() => {
      this.setData({ toastShow: false });
    }, duration);
  },

  // ==================== 庆祝撒花 ====================
  showConfetti() {
    const colors = ['#6D32FF', '#A855F7', '#EC4899', '#3B82F6', '#10B981', '#F59E0B'];
    const confettiPieces = [];

    for (let i = 0; i < 50; i++) {
      confettiPieces.push({
        left: Math.random() * 100,
        color: colors[Math.floor(Math.random() * colors.length)],
        delay: Math.random() * 0.5,
        duration: 2 + Math.random() * 2
      });
    }

    this.setData({
      showConfetti: true,
      confettiPieces
    });

    setTimeout(() => {
      this.setData({ showConfetti: false, confettiPieces: [] });
    }, 4000);
  },

  // ==================== AI 悬浮按钮相关 ====================
  initFabPosition() {
    // 从本地存储读取上次保存的位置
    const savedFabX = wx.getStorageSync('fabPositionX');
    const savedFabY = wx.getStorageSync('fabPositionY');

    // 获取系统信息计算默认位置
    const systemInfo = wx.getSystemInfoSync();
    const screenWidth = systemInfo.windowWidth;
    const screenHeight = systemInfo.windowHeight;
    const safeAreaBottom = systemInfo.safeArea ? systemInfo.screenHeight - systemInfo.safeArea.bottom : 0;

    // 按钮大小约50px，边距24px，tabbar高度约50px
    const fabSize = 50;
    const margin = 24;
    const tabbarHeight = 50;
    const bottomOffset = tabbarHeight + safeAreaBottom + margin;

    // 默认位置：右下角
    const defaultX = screenWidth - fabSize - margin;
    const defaultY = screenHeight - fabSize - bottomOffset - margin;

    if (savedFabX !== '' && savedFabY !== '') {
      // 确保保存的位置在屏幕范围内
      const validX = Math.min(Math.max(0, savedFabX), screenWidth - fabSize);
      const validY = Math.min(Math.max(0, savedFabY), screenHeight - fabSize - bottomOffset);
      this.setData({
        fabX: validX,
        fabY: validY
      });
    } else {
      this.setData({
        fabX: defaultX,
        fabY: defaultY
      });
    }
  },

  onFabMove(e) {
    if (e.detail.source === 'touch') {
      this.setData({ fabDragging: true });
    }
  },

  onFabTouchStart() {
    this.setData({
      fabTouchStartTime: Date.now()
    });
  },

  onFabTouchEnd(e) {
    const touchDuration = Date.now() - this.data.fabTouchStartTime;

    // 如果是快速点击（小于200ms）且没有拖拽，则触发点击事件
    if (touchDuration < 200 && !this.data.fabDragging) {
      this.toggleFabMenu();
    }

    // 保存当前位置到本地存储
    if (this.data.fabDragging) {
      wx.setStorageSync('fabPositionX', this.data.fabX);
      wx.setStorageSync('fabPositionY', this.data.fabY);
    }

    this.setData({ fabDragging: false });
  },

  toggleFabMenu() {
    this.setData({
      fabMenuOpen: !this.data.fabMenuOpen
    });
  },

  goToConsultation() {
    this.setData({ fabMenuOpen: false });
    wx.navigateTo({
      url: '/pages/consultation/create'
    });
  },

  // ==================== 导航跳转 ====================
  goToChat() {
    this.setData({ fabMenuOpen: false });
    wx.navigateTo({
      url: '/pages/chat/chat'
    });
  },

  goToDiagnosis() {
    this.setData({ fabMenuOpen: false });
    wx.navigateTo({
      url: '/pages/diagnosis/diagnosis'
    });
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    });
  },

  goToCourse() {
    wx.navigateTo({
      url: '/pages/course/course'
    });
  },

  goToBeauty() {
    wx.navigateTo({
      url: '/pages/beauty/beauty'
    });
  },

  // ==================== 文章相关 ====================
  async loadArticles(tag = null, reset = true) {
    try {
      this.setData({ loading: true });
      const page = reset ? 1 : this.data.articlePage;
      const result = await ArticleAPI.getList({ tag, page });
      const articles = (result && result.list) ? result.list : (Array.isArray(result) ? result : []);
      // 格式化文章数据
      const formattedArticles = articles.map((article, index) => ({
        id: article.id,
        title: article.title,
        desc: article.summary || '',
        tag: article.tag || '推荐',
        tagClass: this.getTagClass(article.tag),
        time: this.formatTime(article.publishTime || article.createTime),
        views: this.formatNumber(article.viewCount || 0),
        likes: this.formatNumber(article.likeCount || 0),
        coverUrl: article.coverUrl || '',
        emoji: this.getCoverEmoji(article.tag),
        color: index % 4,
        // 点赞和收藏状态
        liked: article.isLiked || false,
        collected: article.isCollected || false
      }));
      this.setData({
        articles: reset ? formattedArticles : [...this.data.articles, ...formattedArticles],
        hasMore: (result && result.hasMore) || false,
        articlePage: page,
        loading: false
      });
    } catch (error) {
      console.error('加载文章失败:', error);
      // 显示空列表，不使用模拟数据
      this.setData({
        articles: [],
        loading: false
      });
      wx.showToast({
        title: '加载失败，请稍后重试',
        icon: 'none'
      });
    }
  },

  formatNumber(num) {
    if (num >= 10000) {
      return (num / 10000).toFixed(1) + '万';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'k';
    }
    return num.toString();
  },

  formatTime(time) {
    if (!time) return '刚刚';
    const now = Date.now();
    const diff = now - parseDate(time).getTime();
    const hour = 60 * 60 * 1000;
    const day = 24 * hour;

    if (diff < hour) return '刚刚';
    if (diff < hour * 6) return Math.floor(diff / hour) + '小时前';
    if (diff < day) return '今天';
    if (diff < day * 2) return '昨天';
    return Math.floor(diff / day) + '天前';
  },

  getCoverEmoji(tag) {
    const emojiMap = {
      '护理': '🌸',
      '活动': '🎁',
      '健康': '🏥',
      '训练': '🎓',
      '营养': '🍖',
      '指南': '📖'
    };
    return emojiMap[tag] || '📄';
  },

  getTagClass(tag) {
    const classMap = {
      '护理': 'tag-success',
      '活动': 'tag-warning',
      '健康': 'tag-error',
      '训练': 'tag-primary',
      '营养': 'tag-primary',
      '指南': 'tag-secondary'
    };
    return classMap[tag] || '';
  },

  goToArticleDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/recommend/detail?id=${id}`
    });
  },

  // ==================== Tab切换 ====================
  onTabChange(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({
      activeTab: index
    });
    // 根据tab加载不同内容
    this.loadArticlesByTab(index);
  },

  loadArticlesByTab(index) {
    // 根据tab加载不同类型的文章
    const tabs = this.data.tabs;
    // index 0 是推荐，加载全部；其他根据标签筛选
    const tag = index === 0 ? null : tabs[index];
    this.loadArticles(tag);
  },

  // ==================== 其他事件 ====================
  handleNotice() {
    wx.switchTab({
      url: '/pages/news/news'
    });
  },

  handleViewAllServices() {
    wx.showActionSheet({
      itemList: ['在线问诊', 'AI智能诊断', '美容预约', '训练课程', '健康档案'],
      success(res) {
        const routes = [
          '/pages/consultation/doctor-list',
          '/pages/diagnosis/diagnosis',
          '/pages/beauty/beauty',
          '/pages/course/course',
          '/pages/health/list'
        ];
        wx.navigateTo({ url: routes[res.tapIndex] });
      }
    });
  },

  // ==================== 滚动事件 ====================
  onScroll(e) {
    const scrollTop = e.detail.scrollTop;
    // 滚动时关闭悬浮菜单
    if (this.data.fabMenuOpen) {
      this.setData({ fabMenuOpen: false });
    }
  },

  // ==================== 加载更多 ====================
  async onLoadMore() {
    if (this.data.loading || !this.data.hasMore) return;

    const nextPage = this.data.articlePage + 1;
    this.setData({ articlePage: nextPage });
    const tagMap = [null, '健康', '护理', '训练'];
    const tag = tagMap[this.data.activeTab];
    await this.loadArticles(tag, false);
  },

  // ==================== 分享 ====================
  onShareAppMessage() {
    return {
      title: '伴宠云诊 - 守护爱宠健康每一天',
      path: '/pages/index/index',
      imageUrl: '/image/share-cover.png'
    };
  }
});
