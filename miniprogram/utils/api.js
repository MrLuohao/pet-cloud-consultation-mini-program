// utils/api.js - API配置和请求封装

// API基础地址配置
// 优先读取 Storage 中的 apiHost，方便切换环境；
// 未设置时使用下方默认IP（开发者本机局域网地址）。
// 修改方法：把下方 IP 替换为你的电脑局域网 IP 即可。
//   Windows: ipconfig 查看 IPv4 地址
//   Mac/Linux: ifconfig 查看局域网 IP
const DEFAULT_API_HOST = '10.0.12.147'
const API_HOST = wx.getStorageSync('apiHost') || DEFAULT_API_HOST
const API_BASE_URL = `http://${API_HOST}:8117`  // 用户服务地址
const SHOP_API_BASE_URL = `http://${API_HOST}:8118`  // 商城服务地址

function normalizeUploadUrl(url, useShopPort = false) {
  if (!url || typeof url !== 'string') return url
  // 已经是完整URL则直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  // 本地路径，根据服务类型拼接完整URL
  if (url.startsWith('/uploads/')) {
    const port = useShopPort ? 8118 : 8117
    return `http://${API_HOST}:${port}${url}`
  }
  return url
}

function normalizeUploadUrlList(list, useShopPort = false) {
  if (!Array.isArray(list)) return []
  return list
    .map(item => normalizeUploadUrl((item || '').trim(), useShopPort))
    .filter(Boolean)
}

// 统一处理商品图片URL（商城服务端口8118）
function normalizeShopProduct(product) {
  if (!product || typeof product !== 'object') return product
  return {
    ...product,
    coverUrl: normalizeUploadUrl(product.coverUrl, true),
    imageUrls: product.imageUrls ? normalizeUploadUrlList(product.imageUrls, true) : product.imageUrls,
    productImage: product.productImage ? normalizeUploadUrl(product.productImage, true) : product.productImage
  }
}

function normalizeProductReview(review) {
  if (!review || typeof review !== 'object') return review
  return {
    ...review,
    userAvatar: normalizeUploadUrl(review.userAvatar),
    images: normalizeUploadUrlList(review.images)
  }
}

function normalizeArticleComment(comment) {
  if (!comment || typeof comment !== 'object') return comment
  return {
    ...comment,
    userAvatar: normalizeUploadUrl(comment.userAvatar),
    replies: Array.isArray(comment.replies)
      ? comment.replies.map(reply => ({
          ...reply,
          userAvatar: normalizeUploadUrl(reply.userAvatar)
        }))
      : []
  }
}

/**
 * 获取Token
 */
function getToken() {
  return wx.getStorageSync('token') || ''
}

/**
 * iOS 兼容的日期解析
 * iOS 不支持 "yyyy-MM-dd HH:mm:ss"（空格分隔）格式，需替换为 T
 * @param {string|number} val - 日期字符串或 Unix 时间戳
 * @returns {Date}
 */
function parseDate(val) {
  if (!val) return new Date(NaN)
  if (typeof val === 'number') return new Date(val)
  return new Date(String(val).replace(' ', 'T'))
}

/**
 * 检查是否已登录
 */
function isLoggedIn() {
  return !!getToken()
}

/**
 * 跳转到登录页
 * 仅清除内存状态（globalData），不清除 Storage。
 * Storage 中的 token 只在服务器确认 token 无效（401）时才清除，
 * 避免用户因误操作或网络抖动导致被强制退出。
 */
function navigateToLogin() {
  // 仅清除内存状态，保留 Storage（下次启动可恢复）
  const app = getApp()
  if (app && app.globalData) {
    app.globalData.token = null
    app.globalData.userId = null
    app.globalData.userInfo = null
  }

  // 设置标志位防止重复跳转
  if (app && app.globalData) {
    if (app.globalData._isNavigatingToLogin) {
      return
    }
    app.globalData._isNavigatingToLogin = true
  }

  // 使用 navigateTo 保留返回功能
  wx.navigateTo({
    url: '/pages/login/login',
    complete: () => {
      // 跳转完成后重置标志位
      setTimeout(() => {
        if (app && app.globalData) {
          app.globalData._isNavigatingToLogin = false
        }
      }, 500)
    }
  })
}

/**
 * 通用请求方法
 * @param {string} url - 接口地址
 * @param {string} method - 请求方法
 * @param {object} data - 请求数据
 * @param {string} base_url - 基础URL
 * @param {boolean} requireAuth - 是否需要登录认证，默认true
 */
function request(url, method = 'GET', data = null, base_url = API_BASE_URL, requireAuth = true) {
  return new Promise((resolve, reject) => {
    // 需要认证的接口，未登录时直接跳转登录页（静默处理，不reject）
    if (requireAuth && !isLoggedIn()) {
      navigateToLogin()
      // 必须 reject，避免调用方 await 一直挂起
      reject({ message: '未登录', code: 401, needLogin: true })
      return
    }

    const header = {
      'content-type': 'application/json'
    }

    // 添加认证token
    const token = getToken()
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    wx.request({
      url: `${base_url}${url}`,
      method: method,
      data: data,
      header: header,
      success: (res) => {
        if (res.statusCode === 200) {
          // 后端返回格式: {success: true, code: 200, message: "成功", data: ...}
          if (res.data.success === true || res.data.status === true) {
            resolve(res.data.data || res.data)
          } else {
            // 业务错误
            const errorMsg = res.data.message || res.data.msg || '请求失败'
            wx.hideLoading()
            wx.showToast({
              title: errorMsg,
              icon: 'none',
              duration: 2000
            })
            reject({ message: errorMsg, code: res.data.code })
          }
        } else if (res.statusCode === 401) {
          // 服务器确认 token 无效/过期，此时才清除本地凭证
          wx.removeStorageSync('token')
          wx.removeStorageSync('userId')
          wx.removeStorageSync('userInfo')
          wx.showToast({ title: '登录已过期，请重新登录', icon: 'none', duration: 1500 })
          navigateToLogin()
          // 必须 reject，避免调用方 await 一直挂起
          reject({ message: '登录已过期', code: 401, needLogin: true })
          return
        } else {
          wx.showToast({
            title: `请求失败(${res.statusCode})`,
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        console.error('网络请求失败:', err)
        wx.showToast({
          title: '网络连接失败，请检查服务是否启动',
          icon: 'none',
          duration: 3000
        })
        reject(err)
      }
    })
  })
}

/**
 * 公开接口请求（不需要登录）
 */
function publicRequest(url, method = 'GET', data = null, base_url = API_BASE_URL) {
  return request(url, method, data, base_url, false)
}

/**
 * 需认证接口请求（需要登录）
 */
function authRequest(url, method = 'GET', data = null, base_url = API_BASE_URL) {
  return request(url, method, data, base_url, true)
}

// ==================== 认证相关API ====================

const AuthAPI = {
  /**
   * 微信登录（公开接口）
   */
  login(code, nickname = '', avatarUrl = '', gender = 0) {
    return publicRequest('/api/auth/login', 'POST', {
      code,
      nickname,
      avatarUrl,
      gender
    })
  },

  /**
   * 刷新Token（需登录）
   */
  refreshToken(token) {
    return authRequest('/api/auth/refresh', 'POST', { token })
  },

  /**
   * 获取用户信息（需登录）
   */
  getUserInfo() {
    return authRequest('/api/auth/userinfo', 'GET')
  },

  /**
   * 更新用户信息（需登录）
   * @param {Object} data - { nickname, avatarUrl, gender }
   */
  updateUserInfo(data) {
    // 后端使用 @RequestParam 接收参数，需要拼接到 URL
    const params = [];
    if (data.nickname !== undefined) {
      params.push(`nickname=${encodeURIComponent(data.nickname)}`);
    }
    if (data.avatarUrl !== undefined) {
      params.push(`avatarUrl=${encodeURIComponent(data.avatarUrl)}`);
    }
    if (data.gender !== undefined) {
      params.push(`gender=${data.gender}`);
    }
    const url = `/api/auth/userinfo?${params.join('&')}`;
    return authRequest(url, 'PUT');
  },

  /**
   * 登出（需登录）
   */
  logout() {
    return authRequest('/api/auth/logout', 'POST')
  }
}

// ==================== 会员相关API ====================

const VipAPI = {
  /**
   * 开通/续费会员
   */
  subscribe(planId, payConfirmed = true) {
    return authRequest('/api/vip/subscribe', 'POST', { planId, payConfirmed })
  },

  /**
   * 发起 VIP 支付，获取 wx.requestPayment 参数（BE-4.2）
   */
  pay(planType) {
    return authRequest('/api/vip/pay', 'POST', { planType })
  }
}

// ==================== 课程相关API（公开） ====================

const CourseAPI = {
  /**
   * 获取课程列表（公开）
   */
  getList() {
    return publicRequest('/api/course/list', 'GET')
  },

  /**
   * 获取课程详情（公开）
   */
  getDetail(id) {
    return publicRequest(`/api/course/${id}`, 'GET')
  },

  getProgress(courseId) {
    return authRequest(`/api/course/${courseId}/progress`, 'GET')
  },

  updateProgress(courseId, data) {
    return authRequest(`/api/course/${courseId}/progress`, 'PUT', data)
  },

  submitReview(courseId, data) {
    return authRequest(`/api/course/${courseId}/review`, 'POST', data)
  },

  getReviews(courseId) {
    return publicRequest(`/api/course/${courseId}/reviews`, 'GET')
  }
}

// ==================== 美容服务相关API ====================

const BeautyAPI = {
  /**
   * 获取门店列表（公开）
   */
  getStoreList() {
    return publicRequest('/api/beauty/stores', 'GET')
  },

  /**
   * 获取门店详情（公开）
   */
  getStoreDetail(id) {
    return publicRequest(`/api/beauty/store/${id}`, 'GET')
  },

  /**
   * 获取门店服务项目（公开）
   */
  getStoreServices(storeId) {
    return publicRequest(`/api/beauty/store/${storeId}/services`, 'GET')
  },

  /**
   * 获取可预约时间段（公开）
   */
  getAvailableSlots(storeId, date) {
    return publicRequest(`/api/beauty/store/${storeId}/available-slots?date=${date}`, 'GET')
  },

  /**
   * 创建预约（需登录）
   */
  createBooking(data) {
    return authRequest('/api/beauty/booking', 'POST', data)
  },

  /**
   * 获取我的预约列表（需登录）
   */
  getBookingList(status = null) {
    const url = status !== null ? `/api/beauty/bookings?status=${status}` : '/api/beauty/bookings'
    return authRequest(url, 'GET')
  },

  /**
   * 获取预约详情（需登录）
   */
  getBookingDetail(id) {
    return authRequest(`/api/beauty/booking/${id}`, 'GET')
  },

  /**
   * 取消预约（需登录）
   */
  cancelBooking(id) {
    return authRequest(`/api/beauty/booking/${id}/cancel`, 'PUT')
  }
}

// ==================== 消息相关API（需登录） ====================

const MessageAPI = {
  /**
   * 获取消息列表（支持类型过滤）
   * @param {string} type - system/order/consultation/activity（可选）
   */
  getList(type = null) {
    const url = type ? `/api/message/list?type=${type}` : '/api/message/list'
    return authRequest(url, 'GET')
  },

  /**
   * 标记消息已读
   */
  markAsRead(id) {
    return authRequest(`/api/message/${id}/read`, 'PUT')
  },

  /**
   * 全部标记已读
   */
  markAllAsRead() {
    return authRequest('/api/message/read-all', 'PUT')
  },

  /**
   * 获取未读数量
   */
  getUnreadCount() {
    return authRequest('/api/message/unread-count', 'GET')
  }
}

// ==================== 文章相关API ====================

const ArticleAPI = {
  /**
   * 获取文章列表（分页）
   * @param {object} params - { tag, page, pageSize }
   */
  getList({ tag = null, page = 1, pageSize = 10 } = {}) {
    const params = []
    if (tag) params.push(`tag=${encodeURIComponent(tag)}`)
    params.push(`page=${page}`)
    params.push(`pageSize=${pageSize}`)
    return publicRequest(`/api/article/list?${params.join('&')}`, 'GET')
  },

  /**
   * 获取文章详情（公开，但登录后显示点赞/收藏状态）
   */
  getDetail(id) {
    return publicRequest(`/api/article/${id}`, 'GET')
  },

  /**
   * 点赞文章（需登录）
   */
  like(id) {
    return authRequest(`/api/article/${id}/like`, 'POST')
  },

  /**
   * 取消点赞（需登录）
   */
  unlike(id) {
    return authRequest(`/api/article/${id}/like`, 'DELETE')
  },

  /**
   * 收藏文章（需登录）
   */
  collect(id) {
    return authRequest(`/api/article/${id}/collect`, 'POST')
  },

  /**
   * 取消收藏（需登录）
   */
  uncollect(id) {
    return authRequest(`/api/article/${id}/collect`, 'DELETE')
  },

  /**
   * 获取评论列表（公开）
   */
  getComments(id) {
    return publicRequest(`/api/article/${id}/comments`, 'GET')
      .then(list => (list || []).map(normalizeArticleComment))
  },

  /**
   * 发表评论（需登录）
   */
  createComment(articleId, content, parentId = null, replyToUserId = null) {
    return authRequest('/api/article/comment', 'POST', {
      articleId,
      content,
      parentId,
      replyToUserId
    })
  },

  /**
   * 删除评论（需登录）
   */
  deleteComment(commentId) {
    return authRequest(`/api/article/comment/${commentId}`, 'DELETE')
  }
}

// ==================== 商城相关API（公开浏览） ====================

const ShopAPI = {
  /**
   * 获取商品分类（公开）
   */
  getCategories() {
    return publicRequest('/api/product/categories', 'GET', null, SHOP_API_BASE_URL)
  },

  /**
   * 获取商品列表（分页）
   * @param {object} params - { categoryId, page, pageSize }
   */
  getProducts({ categoryId = null, page = 1, pageSize = 10 } = {}) {
    const params = []
    if (categoryId != null) params.push(`categoryId=${categoryId}`)
    params.push(`page=${page}`)
    params.push(`pageSize=${pageSize}`)
    return publicRequest(`/api/product/list?${params.join('&')}`, 'GET', null, SHOP_API_BASE_URL)
      .then(res => {
        if (!res || !res.list) return res
        return {
          ...res,
          list: res.list.map(product => ({
            ...product,
            coverUrl: normalizeUploadUrl(product.coverUrl, true)
          }))
        }
      })
  },

  /**
   * 获取商品详情（公开）
   */
  getProductDetail(id) {
    return publicRequest(`/api/product/${id}`, 'GET', null, SHOP_API_BASE_URL)
      .then(product => normalizeShopProduct(product))
  }
}

// ==================== 购物车相关API（需登录） ====================

const CartAPI = {
  /**
   * 获取购物车列表
   */
  getList() {
    return authRequest('/api/cart/list', 'GET', null, SHOP_API_BASE_URL)
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeShopProduct(item))
      })
  },

  /**
   * 添加商品到购物车
   */
  add(productId, quantity = 1) {
    return authRequest('/api/cart/add', 'POST', { productId, quantity }, SHOP_API_BASE_URL)
  },

  /**
   * 更新购物车商品数量
   */
  updateQuantity(cartId, quantity) {
    return authRequest('/api/cart/update', 'PUT', { cartId, quantity }, SHOP_API_BASE_URL)
  },

  /**
   * 删除购物车商品
   */
  delete(cartId) {
    return authRequest('/api/cart/delete', 'DELETE', { cartId }, SHOP_API_BASE_URL)
  },

  /**
   * 清空购物车
   */
  clear() {
    return authRequest('/api/cart/clear', 'DELETE', null, SHOP_API_BASE_URL)
  },

  /**
   * 更新购物车商品规格
   */
  updateSpec(cartId, spec) {
    return authRequest('/api/cart/spec', 'PUT', { cartId, spec }, SHOP_API_BASE_URL)
  },

  /**
   * 获取购物车商品数量
   */
  getCount() {
    return authRequest('/api/cart/count', 'GET', null, SHOP_API_BASE_URL)
  }
}

// ==================== 订单相关API（需登录） ====================

const OrderAPI = {
  /**
   * 获取订单确认页信息
   */
  getConfirm(productIds, quantities, cartIds) {
    return authRequest('/api/order/confirm', 'POST', { productIds, quantities, cartIds }, SHOP_API_BASE_URL)
      .then(res => {
        if (!res || typeof res !== 'object') return res
        return {
          ...res,
          items: Array.isArray(res.items) ? res.items.map(normalizeShopProduct) : res.items
        }
      })
  },

  /**
   * 提交订单
   */
  submit(productIds, quantities, addressId, couponId, remark) {
    return authRequest('/api/order/submit', 'POST', { productIds, quantities, addressId, couponId, remark }, SHOP_API_BASE_URL)
  },

  /**
   * 获取订单列表
   */
  getList(status = null) {
    const url = status ? `/api/order/list?status=${status}` : '/api/order/list'
    return authRequest(url, 'GET', null, SHOP_API_BASE_URL)
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(order => ({
          ...order,
          items: Array.isArray(order.items) ? order.items.map(normalizeShopProduct) : order.items
        }))
      })
  },

  /**
   * 获取订单详情
   */
  getDetail(id) {
    return authRequest(`/api/order/${id}`, 'GET', null, SHOP_API_BASE_URL)
      .then(order => {
        if (!order || typeof order !== 'object') return order
        return {
          ...order,
          items: Array.isArray(order.items) ? order.items.map(normalizeShopProduct) : order.items
        }
      })
  },

  /**
   * 取消订单
   */
  cancel(orderId) {
    return authRequest('/api/order/cancel', 'PUT', { orderId }, SHOP_API_BASE_URL)
  },

  /**
   * 确认收货
   */
  confirmReceive(orderId) {
    return authRequest('/api/order/confirm-receive', 'PUT', { orderId }, SHOP_API_BASE_URL)
  },

  /**
   * 支付订单
   */
  pay(orderId) {
    return authRequest('/api/order/pay', 'POST', { orderId }, SHOP_API_BASE_URL)
  },

  /**
   * 获取各状态订单数量
   */
  getCount() {
    return authRequest('/api/order/count', 'GET', null, SHOP_API_BASE_URL)
  },

  /**
   * 获取待评价订单列表
   */
  getPendingReviewList(page = 1, size = 10) {
    return authRequest(`/api/order/pending-review?page=${page}&size=${size}`, 'GET', null, SHOP_API_BASE_URL)
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => ({
          ...item,
          products: Array.isArray(item.products) ? item.products.map(normalizeShopProduct) : item.products
        }))
      })
  },

  /**
   * 获取待评价商品数量
   */
  getPendingReviewCount() {
    return authRequest('/api/order/pending-review/count', 'GET', null, SHOP_API_BASE_URL)
  }
}

// ==================== 商品详情相关API ====================

const ProductAPI = {
  /**
   * 获取商品详情（含评价）（公开）
   */
  getDetail(id) {
    return publicRequest(`/api/product/${id}/detail`, 'GET', null, SHOP_API_BASE_URL)
      .then(detail => {
        if (!detail || typeof detail !== 'object') return detail
        return {
          ...detail,
          coverUrl: normalizeUploadUrl(detail.coverUrl, true),
          imageUrls: normalizeUploadUrlList(detail.imageUrls, true),
          reviews: Array.isArray(detail.reviews) ? detail.reviews.map(normalizeProductReview) : []
        }
      })
  },

  /**
   * 获取商品评价列表（公开）
   */
  getReviews(id, page = 1, size = 10) {
    return publicRequest(`/api/product/${id}/reviews?page=${page}&size=${size}`, 'GET', null, SHOP_API_BASE_URL)
      .then(list => (list || []).map(normalizeProductReview))
  },

  /**
   * 提交商品评价（需登录）
   */
  createReview(orderItemId, productId, rating, content, images) {
    return authRequest('/api/product/review', 'POST', { orderItemId, productId, rating, content, images }, SHOP_API_BASE_URL)
  },

  /**
   * 获取评价列表（带筛选）（公开）
   */
  getReviewsWithFilter(productId, filter = 'all', page = 1, size = 10) {
    return publicRequest(`/api/product/${productId}/reviews?filter=${filter}&page=${page}&size=${size}`, 'GET', null, SHOP_API_BASE_URL)
      .then(list => (list || []).map(normalizeProductReview))
  },

  /**
   * 点赞评价（需登录）
   */
  toggleReviewLike(reviewId) {
    return authRequest(`/api/product/review/${reviewId}/like`, 'POST', null, SHOP_API_BASE_URL)
  },

  /**
   * 编辑评价（需登录）
   */
  updateReview(reviewId, rating, content, images) {
    return authRequest(`/api/product/review/${reviewId}`, 'PUT', { rating, content, images }, SHOP_API_BASE_URL)
  },

  /**
   * 添加追评（需登录）
   */
  addFollowUp(reviewId, content) {
    return authRequest(`/api/product/review/${reviewId}/follow-up`, 'POST', { content }, SHOP_API_BASE_URL)
  },

  /**
   * 获取可评价的订单项（需登录）
   */
  getReviewableOrderItem(productId) {
    return authRequest(`/api/product/${productId}/reviewable-order-item`, 'GET', null, SHOP_API_BASE_URL)
  },

  /**
   * 获取商品评价统计（公开）
   */
  getReviewSummary(productId) {
    return publicRequest(`/api/product/${productId}/reviews/summary`, 'GET', null, SHOP_API_BASE_URL)
  }
}

// ==================== 优惠券相关API（需登录） ====================

const CouponAPI = {
  /**
   * 获取可领取优惠券列表
   */
  getList() {
    return authRequest('/api/coupon/list', 'GET', null, SHOP_API_BASE_URL)
  },

  /**
   * 获取我的优惠券
   */
  getMyCoupons(status = null) {
    const url = status !== null && status !== undefined ? `/api/coupon/my?status=${status}` : '/api/coupon/my'
    return authRequest(url, 'GET', null, SHOP_API_BASE_URL)
  },

  /**
   * 领取优惠券
   */
  receive(couponId) {
    return authRequest('/api/coupon/receive', 'POST', { couponId }, SHOP_API_BASE_URL)
  },

  /**
   * 获取订单可用优惠券
   */
  getAvailable(totalAmount) {
    return authRequest(`/api/coupon/available?totalAmount=${totalAmount}`, 'GET', null, SHOP_API_BASE_URL)
  }
}

// ==================== 满减活动相关API（公开） ====================

const PromotionAPI = {
  /**
   * 获取当前有效的满减活动
   */
  getActivePromotions() {
    return publicRequest('/api/promotion/active', 'GET', null, SHOP_API_BASE_URL)
  }
}

// ==================== 收藏相关API（需登录） ====================

const CollectionAPI = {
  /**
   * 添加收藏
   */
  add(productId) {
    return authRequest('/api/collection/add', 'POST', { productId }, SHOP_API_BASE_URL)
  },

  /**
   * 取消收藏
   */
  remove(productId) {
    return authRequest('/api/collection/remove', 'POST', { productId }, SHOP_API_BASE_URL)
  },

  /**
   * 获取收藏列表
   */
  getList() {
    return authRequest('/api/collection/list', 'GET', null, SHOP_API_BASE_URL)
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeShopProduct(item))
      })
  },

  /**
   * 检查是否已收藏
   */
  check(productId) {
    return authRequest(`/api/collection/check?productId=${productId}`, 'GET', null, SHOP_API_BASE_URL)
  },

  /**
   * 切换收藏状态
   */
  toggle(productId) {
    return authRequest('/api/collection/toggle', 'POST', { productId }, SHOP_API_BASE_URL)
  }
}

// ==================== 搜索相关API（公开） ====================

const SearchAPI = {
  /**
   * 搜索商品
   */
  searchProducts(keyword) {
    return publicRequest(`/api/search/products?keyword=${encodeURIComponent(keyword)}`, 'GET', null, SHOP_API_BASE_URL)
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(product => ({
          ...product,
          coverUrl: normalizeUploadUrl(product.coverUrl, true)
        }))
      })
  },

  /**
   * 获取热门搜索词
   */
  getHotKeywords() {
    return publicRequest('/api/search/hot', 'GET', null, SHOP_API_BASE_URL)
  }
}

// ==================== 推荐相关API（公开） ====================

const RecommendationAPI = {
  /**
   * 根据购物车获取推荐商品（需登录）
   */
  getByCart(limit = 10) {
    return authRequest(`/api/recommendation/by-cart?limit=${limit}`, 'GET', null, SHOP_API_BASE_URL)
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeShopProduct(item))
      })
  },

  /**
   * 获取热销商品推荐（公开）
   */
  getHot(limit = 10) {
    return publicRequest(`/api/recommendation/hot?limit=${limit}`, 'GET', null, SHOP_API_BASE_URL)
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeShopProduct(item))
      })
  },

  /**
   * 获取相似商品推荐（公开）
   */
  getSimilar(productId, limit = 5) {
    return publicRequest(`/api/recommendation/similar/${productId}?limit=${limit}`, 'GET', null, SHOP_API_BASE_URL)
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeShopProduct(item))
      })
  }
}

// ==================== 医生相关API（公开） ====================

const DoctorAPI = {
  /**
   * 获取医生列表
   */
  getList(department = null) {
    const url = department ? `/api/doctor/list?department=${department}` : '/api/doctor/list'
    return publicRequest(url, 'GET')
  },

  /**
   * 获取医生详情
   */
  getDetail(id) {
    return publicRequest(`/api/doctor/${id}`, 'GET')
  },

  /**
   * 获取科室列表
   */
  getDepartments() {
    return publicRequest('/api/doctor/departments', 'GET')
  },

  getReviews(doctorId) {
    return publicRequest(`/api/consultation/doctor/${doctorId}/reviews`, 'GET')
  }
}

// ==================== 咨询相关API（需登录） ====================

const ConsultationAPI = {
  /**
   * 创建咨询（urgentType: 'normal' | 'urgent'）
   */
  create(petId, doctorId, type, description, images, urgentType = 'normal') {
    return authRequest('/api/consultation/create', 'POST', { petId, doctorId, type, description, images, urgentType })
  },

  /**
   * 获取我的咨询列表
   */
  getList() {
    return authRequest('/api/consultation/list', 'GET')
  },

  /**
   * 获取咨询详情
   */
  getDetail(id) {
    return authRequest(`/api/consultation/${id}`, 'GET')
  },

  /**
   * 发送消息
   */
  sendMessage(consultationId, messageType, content, mediaUrl) {
    return authRequest('/api/consultation/message', 'POST', { consultationId, messageType, content, mediaUrl })
  },

  /**
   * 获取聊天记录
   */
  getMessages(id) {
    return authRequest(`/api/consultation/${id}/messages`, 'GET')
  },

  /**
   * 完成咨询
   */
  finish(id) {
    return authRequest(`/api/consultation/${id}/finish`, 'PUT')
  },

  /**
   * 取消咨询
   */
  cancel(id) {
    return authRequest(`/api/consultation/${id}/cancel`, 'PUT')
  },

  submitReview(consultationId, data) {
    return authRequest(`/api/consultation/${consultationId}/review`, 'POST', data)
  },

  pay(consultationId) {
    return authRequest(`/api/consultation/${consultationId}/pay`, 'PUT')
  }
}

// ==================== 健康档案相关API（需登录） ====================

const HealthAPI = {
  /**
   * 获取健康档案列表
   */
  getList() {
    return authRequest('/api/health/list', 'GET')
  },

  /**
   * 获取指定宠物的健康档案
   */
  getByPet(petId) {
    return authRequest(`/api/health/pet/${petId}`, 'GET')
  },

  /**
   * 创建健康档案
   */
  create(petId, recordType, title, content, hospitalName, doctorName, recordDate, nextDate, images) {
    return authRequest('/api/health/create', 'POST', { petId, recordType, title, content, hospitalName, doctorName, recordDate, nextDate, images })
  },

  /**
   * 更新健康档案
   */
  update(id, recordType, title, content, hospitalName, doctorName, recordDate, nextDate, images) {
    return authRequest('/api/health/update', 'PUT', { id, recordType, title, content, hospitalName, doctorName, recordDate, nextDate, images })
  },

  /**
   * 删除健康档案
   */
  delete(recordId) {
    return authRequest('/api/health/delete', 'DELETE', { recordId })
  },

  /**
   * 获取健康提醒列表
   */
  getReminders() {
    return authRequest('/api/health/reminder/list', 'GET')
  },

  createReminder(data) {
    return authRequest('/api/health/reminder/create', 'POST', data)
  },

  markReminderDone(id) {
    return authRequest(`/api/health/reminder/${id}/done`, 'PUT')
  },

  deleteReminder(id) {
    return authRequest(`/api/health/reminder/${id}`, 'DELETE')
  }
}

// ==================== 任务相关API（需登录） ====================

const TaskAPI = {
  /**
   * 获取今日任务列表
   */
  getTodayTasks() {
    return authRequest('/api/task/today', 'GET')
  },

  /**
   * 完成任务
   */
  completeTask(taskId) {
    return authRequest(`/api/task/${taskId}/complete`, 'POST')
  },

  /**
   * 获取任务积分
   */
  getPoints() {
    return authRequest('/api/task/points', 'GET')
  },

  /**
   * 获取任务历史
   */
  getHistory(page = 1, size = 10) {
    return authRequest(`/api/task/history?page=${page}&size=${size}`, 'GET')
  }
}

// ==================== 宠物管理相关API（需登录） ====================

const PetAPI = {
  /**
   * 获取宠物列表
   */
  getList() {
    return authRequest('/api/pet/list', 'GET')
  },

  /**
   * 获取宠物详情
   */
  getDetail(id) {
    return authRequest(`/api/pet/${id}`, 'GET')
  },

  /**
   * 创建宠物
   */
  create(name, type, breed, gender, birthday, weight, avatarUrl, healthStatus, personality, motto) {
    return authRequest('/api/pet/create', 'POST', { name, type, breed, gender, birthday, weight, avatarUrl, healthStatus, personality, motto })
  },

  /**
   * 更新宠物
   */
  update(id, name, type, breed, gender, birthday, weight, avatarUrl, healthStatus, personality, motto) {
    return authRequest('/api/pet/update', 'PUT', { id, name, type, breed, gender, birthday, weight, avatarUrl, healthStatus, personality, motto })
  },

  /**
   * 删除宠物
   */
  delete(petId) {
    return authRequest('/api/pet/delete', 'DELETE', { petId })
  },

  getTimeline(petId) {
    return authRequest(`/api/pet/${petId}/timeline`, 'GET')
  },

  getMonthlyReport(petId, year, month) {
    return authRequest(`/api/pet/${petId}/monthly-report?year=${year}&month=${month}`, 'GET')
  }
}

// ==================== 地址管理相关API（需登录） ====================

const AddressAPI = {
  /**
   * 获取地址列表
   */
  getList() {
    return authRequest('/api/address/list', 'GET')
  },

  /**
   * 获取地址详情
   */
  getDetail(id) {
    return authRequest(`/api/address/${id}`, 'GET')
  },

  /**
   * 创建地址
   */
  create(contactName, contactPhone, province, city, district, detailAddress, isDefault) {
    return authRequest('/api/address/create', 'POST', { contactName, contactPhone, province, city, district, detailAddress, isDefault })
  },

  /**
   * 更新地址
   */
  update(id, contactName, contactPhone, province, city, district, detailAddress, isDefault) {
    return authRequest('/api/address/update', 'PUT', { id, contactName, contactPhone, province, city, district, detailAddress, isDefault })
  },

  /**
   * 删除地址
   */
  delete(addressId) {
    return authRequest('/api/address/delete', 'DELETE', { addressId })
  },

  /**
   * 设置默认地址
   */
  setDefault(addressId) {
    return authRequest('/api/address/default', 'PUT', { addressId })
  },

  /**
   * 获取默认地址
   */
  getDefault() {
    return authRequest('/api/address/default', 'GET')
  }
}

// ==================== AI相关API（公开） ====================

const AIAPI = {
  /**
   * 上传图片文件（可选登录）
   */
  uploadImage(filePath) {
    return new Promise((resolve, reject) => {
      const token = getToken()
      wx.uploadFile({
        url: `${API_BASE_URL}/api/upload/image`,
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data)
              if (data.status === true) {
                resolve(data.data)
              } else {
                reject({ message: data.msg || '上传失败' })
              }
            } catch (e) {
              reject({ message: '响应数据格式错误' })
            }
          } else {
            reject({ message: `上传失败(${res.statusCode})` })
          }
        },
        fail: (err) => {
          wx.showToast({
            title: '图片上传失败',
            icon: 'none'
          })
          reject(err)
        }
      })
    })
  },

  /**
   * QwenMax3 聊天
   */
  chatQwenMax(message) {
    // 直接返回文本，不需要Response包装
    return new Promise((resolve, reject) => {
      const token = getToken()
      wx.request({
        url: `${API_BASE_URL}/api/chat/qwen3Max`,
        method: 'POST',
        data: message,
        header: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            // 后端直接返回字符串
            resolve(typeof res.data === 'string' ? res.data : JSON.stringify(res.data))
          } else {
            reject(new Error(`请求失败: ${res.statusCode}`))
          }
        },
        fail: (err) => {
          wx.showToast({
            title: 'AI服务连接失败',
            icon: 'none'
          })
          reject(err)
        }
      })
    })
  },

  /**
   * DeepSeekV3 聊天
   */
  chatDeepSeek(message) {
    // 直接返回文本，不需要Response包装
    return new Promise((resolve, reject) => {
      const token = getToken()
      wx.request({
        url: `${API_BASE_URL}/api/chat/deepSeekV3`,
        method: 'POST',
        data: message,
        header: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            // 后端直接返回字符串
            resolve(typeof res.data === 'string' ? res.data : JSON.stringify(res.data))
          } else {
            reject(new Error(`请求失败: ${res.statusCode}`))
          }
        },
        fail: (err) => {
          wx.showToast({
            title: 'AI服务连接失败',
            icon: 'none'
          })
          reject(err)
        }
      })
    })
  },

  /**
   * AI宠物健康诊断（公开，访客有次数限制）
   * @param {object} data 诊断数据
   * @param {string} deviceId 设备ID（访客必传）
   */
  diagnose(data, deviceId = null) {
    const url = deviceId
      ? `/api/ai/diagnosis?deviceId=${encodeURIComponent(deviceId)}`
      : '/api/ai/diagnosis'
    return publicRequest(url, 'POST', data)
  },

  /**
   * 获取访客剩余诊断次数
   * @param {string} deviceId 设备ID
   */
  getDiagnosisRemainingCount(deviceId) {
    return publicRequest(`/api/ai/diagnosis/remaining?deviceId=${encodeURIComponent(deviceId)}`, 'GET')
  },

  /**
   * 发送AI消息（带会话支持）（需登录）
   */
  sendWithConversation(conversationId, content, modelType = 'qwen') {
    return authRequest('/api/chat/send', 'POST', { conversationId, content, modelType })
  },

  /**
   * 获取AI聊天历史（需登录）
   */
  getHistory(conversationId) {
    return authRequest(`/api/chat/history/${conversationId}`, 'GET')
  }
}

// ==================== 会话相关API（需登录） ====================

const ConversationAPI = {
  /**
   * 获取消息中心聚合数据
   */
  getCenter() {
    return authRequest('/api/conversation/center', 'GET')
  },

  /**
   * 获取会话列表
   */
  getList() {
    return authRequest('/api/conversation/list', 'GET')
  },

  /**
   * 获取或创建AI会话
   */
  getOrCreateAi() {
    return authRequest('/api/conversation/ai', 'POST')
  },

  /**
   * 获取会话详情
   */
  getDetail(id) {
    return authRequest(`/api/conversation/${id}`, 'GET')
  },

  /**
   * 标记会话已读
   */
  markAsRead(id) {
    return authRequest(`/api/conversation/${id}/read`, 'PUT')
  },

  /**
   * 删除会话
   */
  delete(id) {
    return authRequest(`/api/conversation/${id}`, 'DELETE')
  },

  /**
   * 置顶/取消置顶会话
   */
  togglePin(id, pinned) {
    return authRequest(`/api/conversation/${id}/pin?pinned=${pinned}`, 'PUT')
  },

  /**
   * 获取AI聊天历史
   */
  getHistory(id) {
    return authRequest(`/api/conversation/${id}/history`, 'GET')
  }
}

// ==================== 商品订阅定期购 API（需登录） ====================
const SubscriptionAPI = {
  create(data) {
    return authRequest('/api/subscribe/create', 'POST', data, SHOP_API_BASE_URL)
  },
  getList() {
    return authRequest('/api/subscribe/list', 'GET', null, SHOP_API_BASE_URL)
  },
  pause(id) {
    return authRequest(`/api/subscribe/${id}/pause`, 'PUT', null, SHOP_API_BASE_URL)
  },
  resume(id) {
    return authRequest(`/api/subscribe/${id}/resume`, 'PUT', null, SHOP_API_BASE_URL)
  },
  cancel(id) {
    return authRequest(`/api/subscribe/${id}/cancel`, 'PUT', null, SHOP_API_BASE_URL)
  },
  updateConfig(id, data) {
    return authRequest(`/api/subscribe/${id}/config`, 'PUT', data, SHOP_API_BASE_URL)
  }
}

// ==================== 社区模块 API（公开+登录） ====================
const CommunityAPI = {
  getPosts(page = 1, pageSize = 20) {
    return authRequest(`/api/community/posts?page=${page}&pageSize=${pageSize}`, 'GET')
  },
  createPost(data) {
    return authRequest('/api/community/post', 'POST', data)
  },
  likePost(id) {
    return authRequest(`/api/community/post/${id}/like`, 'POST')
  },
  unlikePost(id) {
    return authRequest(`/api/community/post/${id}/like`, 'DELETE')
  },
  addComment(id, content) {
    return authRequest(`/api/community/post/${id}/comment`, 'POST', { content })
  },
  getComments(id) {
    return authRequest(`/api/community/post/${id}/comments`, 'GET')
  }
}

// 导出工具函数供其他模块使用
module.exports = {
  isLoggedIn,
  navigateToLogin,
  parseDate,
  // API模块
  AuthAPI,
  VipAPI,
  CourseAPI,
  BeautyAPI,
  MessageAPI,
  ArticleAPI,
  ShopAPI,
  AIAPI,
  CartAPI,
  OrderAPI,
  ProductAPI,
  CouponAPI,
  PromotionAPI,
  CollectionAPI,
  SearchAPI,
  RecommendationAPI,
  DoctorAPI,
  ConsultationAPI,
  HealthAPI,
  PetAPI,
  AddressAPI,
  ConversationAPI,
  TaskAPI,
  SubscriptionAPI,
  CommunityAPI,
  API_BASE_URL
}
