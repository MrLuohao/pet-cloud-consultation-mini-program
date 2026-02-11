/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  商城逻辑 | Shop Page Logic                                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const { ShopAPI, CartAPI, isLoggedIn, navigateToLogin } = require('../../utils/api');

Page({
  data: {
    activeCategory: 0,
    currentCategoryId: null,
    page: 1,
    pageSize: 10,
    cartCount: 0,
    loading: false,
    isLoadingMore: false,
    hasMore: true,
    cartX: 0,
    cartY: 0,

    // 分类（初始为空，由接口加载）
    categories: [
      { id: null, name: '全部', icon: '🏪' }
    ],

    // 推广Banner
    promoBanner: {
      title: '新用户专享',
      desc: '首单满99减50',
      emoji: '🎁',
      color: 'gradient'
    },

    // 商品列表（初始为空，由接口加载）
    products: []
  },

  onLoad() {
    this.initCartPosition();
    this.loadCategories();
    this.loadProducts();
    this.loadCartCount();
  },

  async loadCartCount() {
    if (!isLoggedIn()) return;
    try {
      const count = await CartAPI.getCount();
      this.setData({ cartCount: count || 0 });
    } catch (e) {
      // 未登录或失败时保持 0
    }
  },

  // 初始化购物车按钮位置到右下角
  initCartPosition() {
    const systemInfo = wx.getSystemInfoSync();
    const screenWidth = systemInfo.windowWidth;
    const screenHeight = systemInfo.windowHeight;
    // 按钮大小 96rpx，转换为 px
    const buttonSize = 96 * screenWidth / 750;
    // 边距 20px
    const margin = 20;
    // 底部 tabbar 高度约 100px，再加一些余量
    const bottomOffset = 120;

    this.setData({
      cartX: screenWidth - buttonSize - margin,
      cartY: screenHeight - buttonSize - bottomOffset
    });
  },

  // ==================== 分类相关 ====================
  async loadCategories() {
    try {
      const categories = await ShopAPI.getCategories();
      const formattedCategories = [
        { id: null, name: '全部', icon: '🏪' },
        ...categories.map((cat, index) => ({
          id: cat.id,
          name: cat.name,
          icon: this.getCategoryIcon(index)
        }))
      ];
      this.setData({ categories: formattedCategories });
    } catch (error) {
      console.error('加载分类失败:', error);
      wx.showToast({ title: '分类加载失败，请重试', icon: 'none' });
    }
  },

  getCategoryIcon(index) {
    const icons = ['🍖', '🧸', '🎾', '💊', '👗'];
    return icons[index % icons.length];
  },

  onCategoryChange(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    const category = this.data.categories[index];
    this.setData({ activeCategory: index });
    this.loadProducts(category.id, true);
  },

  // ==================== 商品相关 ====================
  async loadProducts(categoryId = null, reset = true) {
    if (reset) {
      this.setData({ page: 1, products: [], hasMore: true, currentCategoryId: categoryId });
    }
    this.setData({ loading: true });
    try {
      const res = await ShopAPI.getProducts({
        categoryId,
        page: this.data.page,
        pageSize: this.data.pageSize
      });
      const newProducts = (res.list || []).map((product, index) => ({
        id: product.id,
        name: product.name,
        price: product.price?.toString() || '0',
        originalPrice: product.originalPrice?.toString(),
        sales: this.formatNumber(product.sales || 0),
        coverUrl: product.coverUrl || '',
        emoji: this.getProductEmoji(index, product.categoryId || categoryId),
        color: index % 5,
        badge: this.getProductBadge(product),
        favorited: false
      }));
      this.setData({
        products: reset ? newProducts : [...this.data.products, ...newProducts],
        hasMore: res.hasMore || false,
        loading: false
      });
    } catch (error) {
      console.error('加载商品失败:', error);
      this.setData({ loading: false });
      wx.showToast({ title: '商品加载失败，请重试', icon: 'none' });
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

  // 根据分类ID获取对应的emoji组
  getCategoryEmojis(categoryId) {
    const categoryEmojiMap = {
      1: ['🐟', '🍖', '🦴', '🍗', '🥩', '🐔', '🥛', '🥚'],  // 食品
      2: ['🧸', '🛏️', '🏠', '🧹', '🪥', '🧴', '🪣', '🧺'],  // 用品
      3: ['🎾', '🥏', '🪀', '⚽', '🏀', '🎯', '🪁', '🎮'],  // 玩具
      4: ['💊', '💉', '🩺', '🩹', '🌿', '✨', '🧬', '🔬'],  // 保健
      5: ['👗', '🎀', '👔', '🧣', '🎩', '👠', '🧤', '🧢']   // 服饰
    };
    return categoryEmojiMap[categoryId] || ['🐾', '🐕', '🐈', '🐇', '🐹', '🦜', '🐠', '🐢'];
  },

  getProductEmoji(index, categoryId) {
    const emojis = this.getCategoryEmojis(categoryId);
    return emojis[index % emojis.length];
  },

  getProductBadge(product) {
    if (product.isNew) return '新品';
    if (product.isHot) return '热卖';
    if (product.discount) return '特价';
    return '';
  },

  // ==================== 交互事件 ====================
  onProductTap(e) {
    const product = e.currentTarget.dataset.product;
    wx.navigateTo({
      url: `/pages/product/detail?id=${product.id}`
    });
  },

  onFavorite(e) {
    // 收藏功能需要登录
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const id = e.currentTarget.dataset.id;
    const products = this.data.products.map(p => {
      if (p.id === id) {
        p.favorited = !p.favorited;
      }
      return p;
    });
    this.setData({ products });
  },

  onPromoTap() {
    wx.showToast({
      title: '活动详情页面',
      icon: 'none'
    });
  },

  goToCart() {
    // 点击动画效果
    const cartBtn = wx.createSelectorQuery().in(this);
    cartBtn.select('.cart-float-btn').boundingClientRect();

    wx.navigateTo({
      url: '/pages/cart/cart'
    });
  },

  // 购物车按钮拖动
  onCartChange(e) {
    // 只记录到实例变量，不 setData，避免触发重渲染导致按钮漂移
    this._cartX = e.detail.x;
    this._cartY = e.detail.y;
  },

  // ==================== 加载更多 ====================
  async onLoadMore() {
    if (this.data.isLoadingMore || !this.data.hasMore) return;
    this.setData({ isLoadingMore: true, page: this.data.page + 1 });
    try {
      const res = await ShopAPI.getProducts({
        categoryId: this.data.currentCategoryId,
        page: this.data.page,
        pageSize: this.data.pageSize
      });
      const newProducts = (res.list || []).map((product, index) => ({
        id: product.id,
        name: product.name,
        price: product.price?.toString() || '0',
        originalPrice: product.originalPrice?.toString(),
        sales: this.formatNumber(product.sales || 0),
        coverUrl: product.coverUrl || '',
        emoji: this.getProductEmoji(index, product.categoryId),
        color: index % 5,
        badge: this.getProductBadge(product),
        favorited: false
      }));
      this.setData({
        products: [...this.data.products, ...newProducts],
        hasMore: res.hasMore || false,
        isLoadingMore: false
      });
    } catch (error) {
      console.error('加载更多失败:', error);
      this.setData({ page: this.data.page - 1, isLoadingMore: false });
    }
  }
});
