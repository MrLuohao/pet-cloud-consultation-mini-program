/**
 * ╔══════════════════════════════════════════════════════════════════════════════
 * ║  商城逻辑 | Shop Page Logic                                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════
 */
const { ShopAPI, CartAPI, isLoggedIn, navigateToLogin } = require('../../utils/api');

function splitProductColumns(products) {
  return products.reduce((columns, product, index) => {
    if (index % 2 === 0) {
      columns.left.push(product)
    } else {
      columns.right.push(product)
    }
    return columns
  }, { left: [], right: [] })
}

function buildCategoryTabs(categories) {
  return [
    {
      id: null,
      name: '推荐',
      anchorId: 'shop-category-recommend'
    },
    ...categories.map((cat, index) => ({
      id: cat.id,
      name: cat.name,
      anchorId: `shop-category-${cat.id || index}`
    }))
  ]
}

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
      {
        id: null,
        name: '推荐',
        anchorId: 'shop-category-recommend'
      }
    ],
    categoryScrollIntoView: 'shop-category-recommend',

    // 推广Banner
    promoBanner: {
      title: '今日推荐',
      desc: '主粮零食专区限时优惠',
      emoji: '',
      color: 'gradient'
    },

    // 商品列表（初始为空，由接口加载）
    products: [],
    leftProducts: [],
    rightProducts: []
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
    const buttonSize = 72 * screenWidth / 750;
    const marginRight = 24 * screenWidth / 750;
    const safeBottom = systemInfo.safeArea
      ? Math.max(screenHeight - systemInfo.safeArea.bottom, 0)
      : 0;
    const bottomOffset = 112 + safeBottom;

    this.setData({
      cartX: screenWidth - buttonSize - marginRight,
      cartY: screenHeight - buttonSize - bottomOffset
    });
  },

  // ==================== 分类相关 ====================
  async loadCategories() {
    try {
      const categories = await ShopAPI.getCategories();
      this.setData({ categories: buildCategoryTabs(categories) });
    } catch (error) {
      console.error('加载分类失败:', error);
      wx.showToast({ title: '分类加载失败，请重试', icon: 'none' });
    }
  },

  onCategoryChange(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    const category = this.data.categories[index];
    this.setData({
      activeCategory: index,
      categoryScrollIntoView: category.anchorId
    });
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
      const mergedProducts = reset ? newProducts : [...this.data.products, ...newProducts]
      const columns = splitProductColumns(mergedProducts)
      this.setData({
        products: mergedProducts,
        leftProducts: columns.left,
        rightProducts: columns.right,
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

  onSearchTap() {
    wx.navigateTo({
      url: '/pages/search/result'
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
