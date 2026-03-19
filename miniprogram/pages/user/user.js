/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  用户中心逻辑 | User Center Page Logic                                          ║
 * ║  Design: Warm • Premium • Pet-Friendly                                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const app = getApp();
const { OrderAPI, AuthAPI, PetAPI, TaskAPI, CouponAPI } = require('../../utils/api');

Page({
  data: {
    // 登录状态
    isLoggedIn: false,
    userInfo: {
      avatar: '',
      name: '',
      id: ''
    },

    // 用户等级
    userLevel: 1,

    // 用户数据
    userPoints: 0,
    userCoupons: 0,
    userPets: 0,
    isVip: false,

    // 订单数据
    orders: [
      { type: 'unpaid', name: '待付款', iconText: '付', count: 0 },
      { type: 'pendingShipment', name: '待发货', iconText: '发', count: 0 },
      { type: 'pendingReceipt', name: '待收货', iconText: '收', count: 0 },
      { type: 'completed', name: '已完成', iconText: '成', count: 0 },
      { type: 'pendingReview', name: '待评价', iconText: '评', count: 0 }
    ],

    // 服务列表
    services: [
      { id: 1, type: 'pet', name: '我的宠物', iconText: '宠', desc: '档案与资料' },
      { id: 2, type: 'collection', name: '我的收藏', iconText: '藏', desc: '内容与商品' },
      { id: 3, type: 'address', name: '收货地址', iconText: '址', desc: '配送管理' },
      { id: 4, type: 'consult', name: '咨询记录', iconText: '诊', desc: '问诊历史' },
      { id: 5, type: 'coupon', name: '优惠券', iconText: '券', desc: '可用权益' },
      { id: 6, type: 'health', name: '健康档案', iconText: '档', desc: '诊断沉淀' },
      { id: 7, type: 'task', name: '每日任务', iconText: '任', desc: '积分成长' },
      { id: 8, type: 'feedback', name: '意见反馈', iconText: '反', desc: '问题建议' },
      { id: 9, type: 'reminder', name: '健康提醒', iconText: '提', desc: '照护提醒' },
      { id: 10, type: 'insurance', name: '宠物保险', iconText: '保', desc: '保障服务' }
    ],

    // 宠物列表
    pets: [],
    displayPets: []
  },

  onLoad() {
    this.checkLoginStatus();
    this.loadOrderCount();
  },

  onShow() {
    this.checkLoginStatus();
    if (this.data.isLoggedIn) {
      if (app.globalData._vipUpdated) {
        app.globalData._vipUpdated = false;
      }
      this.refreshUserInfo();
      this.loadUserStats();
      this.loadOrderCount();
      this.loadPets();
    }
  },

  // ==================== 登录相关 ====================
  onAvatarLoadError() {
    this.setData({ 'userInfo.avatar': '' })
  },

  checkLoginStatus() {
    const isLoggedIn = !!app.globalData.token;
    const userInfo = app.globalData.userInfo || {};

    this.setData({
      isLoggedIn,
      'userInfo.avatar': userInfo.avatarUrl || '',
      'userInfo.name': userInfo.nickname || '',
      'userInfo.id': userInfo.id || '',
      isVip: !!userInfo.isVip,
      displayPets: isLoggedIn ? this.data.displayPets : []
    });
  },

  // 从API刷新用户信息
  async refreshUserInfo() {
    if (!this.data.isLoggedIn) return;

    try {
      const userData = await AuthAPI.getUserInfo();
      if (userData) {
        const userInfo = {
          avatarUrl: userData.avatarUrl || '',
          nickname: userData.nickname || '',
          id: userData.id || ''
        };

        // 更新页面数据
        this.setData({
          'userInfo.avatar': userInfo.avatarUrl,
          'userInfo.name': userInfo.nickname,
          'userInfo.id': userInfo.id,
          isVip: !!userData.isVip
        });

        // 更新全局数据和本地缓存
        const mergedUserInfo = {
          ...userInfo,
          isVip: userData.isVip,
          vipLevel: userData.vipLevel,
          vipExpireDate: userData.vipExpireDate,
          savingAmount: userData.savingAmount
        };
        app.globalData.userInfo = mergedUserInfo;
        wx.setStorageSync('userInfo', mergedUserInfo);
      }
    } catch (error) {
      console.error('刷新用户信息失败:', error);
    }
  },

  // 加载用户统计数据
  async loadUserStats() {
    if (!this.data.isLoggedIn) return;

    try {
      // 并行加载积分和优惠券数据
      const [pointsData, couponsData] = await Promise.allSettled([
        TaskAPI.getPoints(),
        CouponAPI.getMyCoupons(0)
      ]);

      // 处理积分数据
      if (pointsData.status === 'fulfilled' && pointsData.value) {
        this.setData({
          userPoints: pointsData.value.total || 0
        });
      }

      // 处理优惠券数据
      if (couponsData.status === 'fulfilled' && couponsData.value) {
        const coupons = Array.isArray(couponsData.value) ? couponsData.value : [];
        this.setData({
          userCoupons: coupons.length
        });
      }
    } catch (error) {
      console.error('加载用户统计失败:', error);
    }
  },

  // ==================== 宠物相关 ====================
  async loadPets() {
    if (!this.data.isLoggedIn) return;

    try {
      const pets = await PetAPI.getList();
      if (pets && Array.isArray(pets)) {
        this.setData({
          pets: pets,
          userPets: pets.length,
          displayPets: pets.slice(0, 3)
        });
      }
    } catch (error) {
      console.error('加载宠物列表失败:', error);
      this.setData({
        pets: [],
        displayPets: [],
        userPets: 0
      });
    }
  },

  onPetTap() {
    wx.navigateTo({
      url: '/pages/pet/list'
    });
  },

  onPetDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/pet/edit?id=${id}`
    });
  },

  onAddPet() {
    wx.navigateTo({
      url: '/pages/pet/edit'
    });
  },

  // ==================== 订单相关 ====================
  async loadOrderCount() {
    if (!this.data.isLoggedIn) return;

    try {
      const count = await OrderAPI.getCount();
      const orders = this.data.orders.map(order => ({
        ...order,
        count: count[order.type] || 0
      }));
      this.setData({ orders });
    } catch (error) {
      console.error('加载订单数量失败:', error);
    }
  },

  onOrderAll() {
    wx.navigateTo({
      url: '/pages/order/detail?type=all'
    });
  },

  onOrderTap(e) {
    const type = e.currentTarget.dataset.type;
    const statusMap = {
      unpaid: 0,
      pendingShipment: 1,
      pendingReceipt: 2,
      completed: 3
    };

    // 待评价跳转到专门的待评价页面
    if (type === 'pendingReview') {
      wx.navigateTo({
        url: '/pages/order/pending-review'
      });
      return;
    }

    wx.navigateTo({
      url: `/pages/order/detail?type=${type}&status=${statusMap[type] || ''}`
    });
  },

  // ==================== 快捷操作相关 ====================
  onPointsTap() {
    if (!this.data.isLoggedIn) {
      this.navigateToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/task/index'
    });
  },

  onCouponTap() {
    if (!this.data.isLoggedIn) {
      this.navigateToLogin();
      return;
    }
    wx.navigateTo({
      url: '/pages/coupon/my'
    });
  },

  // ==================== 个人资料 ====================
  onProfileTap() {
    if (!this.data.isLoggedIn) {
      this.navigateToLogin();
    } else {
      wx.navigateTo({
        url: '/pages/user/profile'
      });
    }
  },

  navigateToLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    });
  },

  // ==================== VIP相关 ====================
  onVipTap() {
    wx.navigateTo({
      url: '/pages/vip/index'
    });
  },

  // ==================== 设置相关 ====================
  goToSettings() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    });
  },

  // ==================== 服务相关 ====================
  onServiceTap(e) {
    const type = e.currentTarget.dataset.type;
    const routes = {
      pet: '/pages/pet/list',
      collection: '/pages/collection/list',
      address: '/pages/address/list',
      consult: '/pages/consultation/doctor-list',
      coupon: '/pages/coupon/my',
      health: '/pages/health/list',
      task: '/pages/task/index',
      beauty: '/pages/beauty/booking-list/index',
      reminder: '/pages/health/reminder/index',
      insurance: '/pages/insurance/index'
    };

    if (routes[type]) {
      wx.navigateTo({ url: routes[type] });
    } else if (type === 'feedback') {
      wx.navigateTo({ url: '/pages/feedback/feedback' });
    }
  }
});
