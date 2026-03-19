// utils/api.js - API配置和请求封装
const config = require('./config')

// 动态获取API基础地址（每次调用时获取最新值）
const getApiBaseUrl = config.getApiBaseUrl
const getShopApiBaseUrl = config.getShopApiBaseUrl
const getMapApiBaseUrl = config.getMapApiBaseUrl

function normalizeUploadUrl(url, useShopPort = false) {
  return config.getUploadUrl(url, useShopPort)
}

function normalizeUploadUrlList(list, useShopPort = false) {
  if (!Array.isArray(list)) return []
  return list
    .map(item => normalizeUploadUrl((item || '').trim(), useShopPort))
    .filter(Boolean)
}

/**
 * 处理 JSON 字符串中的图片URL（数据库中存储的JSON数组）
 * @param {string|Array} jsonStr - JSON字符串或数组
 * @param {boolean} useShopPort - 是否使用商城端口
 * @returns {Array}
 */
function normalizeJsonImages(jsonStr, useShopPort = false) {
  if (!jsonStr) return []
  // 如果已经是数组，直接处理
  if (Array.isArray(jsonStr)) {
    return normalizeUploadUrlList(jsonStr, useShopPort)
  }
  // 如果是字符串，尝试解析JSON
  if (typeof jsonStr === 'string') {
    try {
      const arr = JSON.parse(jsonStr)
      return normalizeUploadUrlList(arr, useShopPort)
    } catch (e) {
      return []
    }
  }
  return []
}

/**
 * 通用的图片URL处理函数 - 处理对象中所有可能的图片字段
 * @param {object} obj - 要处理的对象
 * @param {object} fieldConfig - 字段配置 { fieldName: useShopPort }
 */
function normalizeImageFields(obj, fieldConfig = {}) {
  if (!obj || typeof obj !== 'object') return obj

  const result = { ...obj }
  for (const [field, useShopPort] of Object.entries(fieldConfig)) {
    if (result[field]) {
      // 判断是数组/JSON还是单个URL
      if (Array.isArray(result[field]) || (typeof result[field] === 'string' && result[field].startsWith('['))) {
        result[field] = normalizeJsonImages(result[field], useShopPort)
      } else {
        result[field] = normalizeUploadUrl(result[field], useShopPort)
      }
    }
  }
  return result
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

function normalizeProductDetail(detail) {
  if (!detail || typeof detail !== 'object') return detail
  return {
    ...normalizeShopProduct(detail),
    storySections: Array.isArray(detail.storySections)
      ? detail.storySections.map(section => ({
          ...section,
          imageUrl: normalizeUploadUrl(section.imageUrl, true)
        }))
      : [],
    reviews: Array.isArray(detail.reviews) ? detail.reviews.map(normalizeProductReview) : []
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

function normalizeCommunityPost(post) {
  if (!post || typeof post !== 'object') return post
  return {
    ...post,
    avatarUrl: normalizeUploadUrl(post.avatarUrl),
    author: post.author
      ? {
          ...post.author,
          avatarUrl: normalizeUploadUrl(post.author.avatarUrl)
        }
      : null,
    pet: post.pet
      ? {
          ...post.pet,
          avatarUrl: normalizeUploadUrl(post.pet.avatarUrl)
        }
      : null,
    // 使用 normalizeJsonImages 处理 mediaUrls，支持 JSON 字符串和数组
    mediaUrls: normalizeJsonImages(post.mediaUrls),
    mediaType: post.mediaType
  }
}

function normalizeCommunityComment(comment) {
  if (!comment || typeof comment !== 'object') return comment
  return {
    ...comment,
    avatarUrl: normalizeUploadUrl(comment.avatarUrl),
    // 处理评论中的媒体URL（如果有的话）
    mediaUrls: normalizeJsonImages(comment.mediaUrls)
  }
}

function normalizeCartItem(item) {
  return normalizeShopProduct(item)
}

function normalizeCartPage(page) {
  if (!page || typeof page !== 'object' || Array.isArray(page)) {
    return page
  }

  return {
    ...page,
    cartGroups: Array.isArray(page.cartGroups)
      ? page.cartGroups.map(group => ({
          ...group,
          items: Array.isArray(group.items) ? group.items.map(normalizeCartItem) : []
        }))
      : [],
    invalidItems: Array.isArray(page.invalidItems)
      ? page.invalidItems.map(normalizeCartItem)
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
 * @param {string|null} base_url - 基础URL（null时动态获取）
 * @param {boolean} requireAuth - 是否需要登录认证，默认true
 */
function request(url, method = 'GET', data = null, base_url = null, requireAuth = true) {
  // 动态获取基础URL（每次请求时获取最新值）
  const baseUrl = base_url || getApiBaseUrl()
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
      url: `${baseUrl}${url}`,
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
          const isBadGateway = res.statusCode === 502
          const toastTitle = isBadGateway
            ? `网关异常(502): ${baseUrl}`
            : `请求失败(${res.statusCode})`
          wx.showToast({
            title: toastTitle,
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
function publicRequest(url, method = 'GET', data = null, base_url = null) {
  const baseUrl = base_url || getApiBaseUrl()
  return request(url, method, data, baseUrl, false)
}

/**
 * 需认证接口请求（需要登录）
 */
function authRequest(url, method = 'GET', data = null, base_url = null) {
  const baseUrl = base_url || getApiBaseUrl()
  return request(url, method, data, baseUrl, true)
}

function normalizeHomeSummary(summary) {
  if (!summary || typeof summary !== 'object') return summary

  const petCard = summary.petCard
    ? {
        ...summary.petCard,
        pets: Array.isArray(summary.petCard.pets)
          ? summary.petCard.pets.map(pet => ({
              ...pet,
              avatarUrl: normalizeUploadUrl(pet.avatarUrl)
            }))
          : []
      }
    : null

  const featuredContents = Array.isArray(summary.featuredContents)
    ? summary.featuredContents.map(item => ({
        ...item,
        coverUrl: normalizeUploadUrl(item.coverUrl)
      }))
    : []

  return {
    ...summary,
    petCard,
    featuredContents
  }
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
      .then(user => {
        if (!user || typeof user !== 'object') return user
        return {
          ...user,
          avatarUrl: normalizeUploadUrl(user.avatarUrl)
        }
      })
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

const HomeAPI = {
  getSummary() {
    return publicRequest('/api/home/summary', 'GET')
      .then(normalizeHomeSummary)
  },

  getCurrentPetCard() {
    return authRequest('/api/pets/current-card', 'GET')
      .then(petCard => {
        if (!petCard || typeof petCard !== 'object') return petCard
        return {
          ...petCard,
          pets: Array.isArray(petCard.pets)
            ? petCard.pets.map(pet => ({
                ...pet,
                avatarUrl: normalizeUploadUrl(pet.avatarUrl)
              }))
            : []
        }
      })
  }
}

const MapAPI = {
  geocode(address, cityCode = '') {
    return publicRequest('/api/map/geocode', 'POST', { address, cityCode }, getMapApiBaseUrl())
  },

  reverseGeocode(latitude, longitude) {
    return publicRequest('/api/map/reverse-geocode', 'POST', { latitude, longitude }, getMapApiBaseUrl())
  },

  searchSuggest(keyword, cityCode = '', poiType = 'address', latitude = null, longitude = null) {
    const query = [
      `keyword=${encodeURIComponent(keyword)}`,
      cityCode ? `cityCode=${encodeURIComponent(cityCode)}` : '',
      poiType ? `poiType=${encodeURIComponent(poiType)}` : '',
      latitude != null ? `latitude=${encodeURIComponent(latitude)}` : '',
      longitude != null ? `longitude=${encodeURIComponent(longitude)}` : ''
    ].filter(Boolean).join('&')
    return publicRequest(`/api/map/search/suggest?${query}`, 'GET', null, getMapApiBaseUrl())
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
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeImageFields(item, { coverUrl: false }))
      })
  },

  /**
   * 获取课程详情（公开）
   */
  getDetail(id) {
    return publicRequest(`/api/course/${id}`, 'GET')
      .then(item => {
        if (!item || typeof item !== 'object') return item
        return normalizeImageFields(item, { coverUrl: false })
      })
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
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeImageFields(item, { coverUrl: false, imageUrl: false }))
      })
  },

  /**
   * 获取门店详情（公开）
   */
  getStoreDetail(id) {
    return publicRequest(`/api/beauty/store/${id}`, 'GET')
      .then(item => {
        if (!item || typeof item !== 'object') return item
        return normalizeImageFields(item, { coverUrl: false, imageUrl: false })
      })
  },

  /**
   * 获取门店服务项目（公开）
   */
  getStoreServices(storeId) {
    return publicRequest(`/api/beauty/store/${storeId}/services`, 'GET')
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeImageFields(item, { imageUrl: false, coverUrl: false }))
      })
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
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeImageFields(item, { storeCoverUrl: false, coverUrl: false }))
      })
  },

  /**
   * 获取预约详情（需登录）
   */
  getBookingDetail(id) {
    return authRequest(`/api/beauty/booking/${id}`, 'GET')
      .then(item => {
        if (!item || typeof item !== 'object') return item
        return normalizeImageFields(item, { storeCoverUrl: false, coverUrl: false })
      })
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
    const url = type ? `/api/messages/notifications?type=${type}` : '/api/messages/notifications'
    return authRequest(url, 'GET')
  },

  /**
   * 标记消息已读
   */
  markAsRead(id) {
    return authRequest(`/api/messages/notifications/${id}/read`, 'PUT')
  },

  /**
   * 全部标记已读
   */
  markAllAsRead() {
    return authRequest('/api/messages/notifications/read-all', 'PUT')
  },

  /**
   * 获取未读数量
   */
  getUnreadCount() {
    return authRequest('/api/messages/notifications/unread-count', 'GET')
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
      .then(res => {
        if (!res || !res.list) return res
        return {
          ...res,
          list: res.list.map(article => ({
            ...article,
            coverUrl: normalizeUploadUrl(article.coverUrl)
          }))
        }
      })
  },

  /**
   * 获取文章详情（公开，但登录后显示点赞/收藏状态）
   */
  getDetail(id) {
    return publicRequest(`/api/article/${id}`, 'GET')
      .then(article => {
        if (!article || typeof article !== 'object') return article
        return {
          ...article,
          coverUrl: normalizeUploadUrl(article.coverUrl)
        }
      })
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
    return publicRequest('/api/product/categories', 'GET', null, getShopApiBaseUrl())
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
    return publicRequest(`/api/product/list?${params.join('&')}`, 'GET', null, getShopApiBaseUrl())
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
    return publicRequest(`/api/product/${id}`, 'GET', null, getShopApiBaseUrl())
      .then(product => normalizeShopProduct(product))
  }
}

// ==================== 购物车相关API（需登录） ====================

const CartAPI = {
  /**
   * 获取购物车列表
   */
  getList() {
    return authRequest('/api/cart/list', 'GET', null, getShopApiBaseUrl())
      .then(payload => {
        if (Array.isArray(payload)) {
          return payload.map(item => normalizeShopProduct(item))
        }
        return normalizeCartPage(payload)
      })
  },

  /**
   * 添加商品到购物车
   */
  add(productId, quantity = 1, specLabel = null) {
    return authRequest('/api/cart/add', 'POST', { productId, quantity, specLabel }, getShopApiBaseUrl())
  },

  /**
   * 更新购物车商品数量
   */
  updateQuantity(cartId, quantity) {
    return authRequest('/api/cart/update', 'PUT', { cartId, quantity }, getShopApiBaseUrl())
  },

  /**
   * 删除购物车商品
   */
  delete(cartId) {
    return authRequest('/api/cart/delete', 'DELETE', { cartId }, getShopApiBaseUrl())
  },

  /**
   * 清空购物车
   */
  clear() {
    return authRequest('/api/cart/clear', 'DELETE', null, getShopApiBaseUrl())
  },

  /**
   * 更新购物车商品规格
   */
  updateSpec(cartId, spec) {
    return authRequest('/api/cart/spec', 'PUT', { cartId, spec }, getShopApiBaseUrl())
  },

  /**
   * 获取购物车商品数量
   */
  getCount() {
    return authRequest('/api/cart/count', 'GET', null, getShopApiBaseUrl())
  }
}

// ==================== 订单相关API（需登录） ====================

const OrderAPI = {
  /**
   * 获取订单确认页信息
   */
  getConfirm(productIds, quantities, cartIds = [], specLabels = []) {
    return authRequest('/api/order/confirm', 'POST', { productIds, quantities, cartIds, specLabels }, getShopApiBaseUrl())
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
  submit(payloadOrProductIds, quantities, addressId, couponId, remark, cartIds, paymentMethod, verificationType) {
    const payload = Array.isArray(payloadOrProductIds)
      ? {
          productIds: payloadOrProductIds,
          quantities,
          addressId,
          couponId,
          remark,
          cartIds,
          paymentMethod,
          verificationType
        }
      : payloadOrProductIds

    return authRequest('/api/order/submit', 'POST', payload, getShopApiBaseUrl())
  },

  /**
   * 获取订单列表
   */
  getList(status = null) {
    const url = status ? `/api/order/list?status=${status}` : '/api/order/list'
    return authRequest(url, 'GET', null, getShopApiBaseUrl())
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
    return authRequest(`/api/order/${id}`, 'GET', null, getShopApiBaseUrl())
      .then(order => {
        if (!order || typeof order !== 'object') return order
        return {
          ...order,
          items: Array.isArray(order.items) ? order.items.map(normalizeShopProduct) : order.items
        }
      })
  },

  getTimeline(id) {
    return authRequest(`/api/order/${id}/timeline`, 'GET', null, getShopApiBaseUrl())
  },

  /**
   * 取消订单
   */
  cancel(orderId) {
    return authRequest('/api/order/cancel', 'PUT', { orderId }, getShopApiBaseUrl())
  },

  /**
   * 确认收货
   */
  confirmReceive(orderId) {
    return authRequest('/api/order/confirm-receive', 'PUT', { orderId }, getShopApiBaseUrl())
  },

  /**
   * 支付订单
   */
  pay(orderId) {
    return authRequest('/api/order/pay', 'POST', { orderId }, getShopApiBaseUrl())
  },

  /**
   * 获取各状态订单数量
   */
  getCount() {
    return authRequest('/api/order/count', 'GET', null, getShopApiBaseUrl())
  },

  /**
   * 获取待评价订单列表
   */
  getPendingReviewList(page = 1, size = 10) {
    return authRequest(`/api/order/pending-review?page=${page}&size=${size}`, 'GET', null, getShopApiBaseUrl())
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
    return authRequest('/api/order/pending-review/count', 'GET', null, getShopApiBaseUrl())
  }
}

// ==================== 商品详情相关API ====================

const ProductAPI = {
  /**
   * 获取商品详情（含评价）（公开）
   */
  getDetail(id) {
    return publicRequest(`/api/product/${id}/detail`, 'GET', null, getShopApiBaseUrl())
      .then(normalizeProductDetail)
  },

  /**
   * 获取商品评价列表（公开）
   */
  getReviews(id, page = 1, size = 10) {
    return publicRequest(`/api/product/${id}/reviews?page=${page}&size=${size}`, 'GET', null, getShopApiBaseUrl())
      .then(list => (list || []).map(normalizeProductReview))
  },

  /**
   * 提交商品评价（需登录）
   */
  createReview(orderItemId, productId, rating, content, images) {
    return authRequest('/api/product/review', 'POST', { orderItemId, productId, rating, content, images }, getShopApiBaseUrl())
  },

  /**
   * 获取评价列表（带筛选）（公开）
   */
  getReviewsWithFilter(productId, filter = 'all', page = 1, size = 10) {
    return publicRequest(`/api/product/${productId}/reviews?filter=${filter}&page=${page}&size=${size}`, 'GET', null, getShopApiBaseUrl())
      .then(list => (list || []).map(normalizeProductReview))
  },

  /**
   * 点赞评价（需登录）
   */
  toggleReviewLike(reviewId) {
    return authRequest(`/api/product/review/${reviewId}/like`, 'POST', null, getShopApiBaseUrl())
  },

  /**
   * 编辑评价（需登录）
   */
  updateReview(reviewId, rating, content, images) {
    return authRequest(`/api/product/review/${reviewId}`, 'PUT', { rating, content, images }, getShopApiBaseUrl())
  },

  /**
   * 添加追评（需登录）
   */
  addFollowUp(reviewId, content) {
    return authRequest(`/api/product/review/${reviewId}/follow-up`, 'POST', { content }, getShopApiBaseUrl())
  },

  /**
   * 获取可评价的订单项（需登录）
   */
  getReviewableOrderItem(productId) {
    return authRequest(`/api/product/${productId}/reviewable-order-item`, 'GET', null, getShopApiBaseUrl())
  },

  /**
   * 获取商品评价统计（公开）
   */
  getReviewSummary(productId) {
    return publicRequest(`/api/product/${productId}/reviews/summary`, 'GET', null, getShopApiBaseUrl())
  }
}

// ==================== 优惠券相关API（需登录） ====================

const CouponAPI = {
  /**
   * 获取可领取优惠券列表
   */
  getList() {
    return authRequest('/api/coupon/list', 'GET', null, getShopApiBaseUrl())
  },

  /**
   * 获取我的优惠券
   */
  getMyCoupons(status = null) {
    const url = status !== null && status !== undefined ? `/api/coupon/my?status=${status}` : '/api/coupon/my'
    return authRequest(url, 'GET', null, getShopApiBaseUrl())
  },

  /**
   * 领取优惠券
   */
  receive(couponId) {
    return authRequest('/api/coupon/receive', 'POST', { couponId }, getShopApiBaseUrl())
  },

  /**
   * 获取订单可用优惠券
   */
  getAvailable(totalAmount) {
    return authRequest(`/api/coupon/available?totalAmount=${totalAmount}`, 'GET', null, getShopApiBaseUrl())
  }
}

// ==================== 满减活动相关API（公开） ====================

const PromotionAPI = {
  /**
   * 获取当前有效的满减活动
   */
  getActivePromotions() {
    return publicRequest('/api/promotion/active', 'GET', null, getShopApiBaseUrl())
  }
}

// ==================== 收藏相关API（需登录） ====================

const CollectionAPI = {
  /**
   * 添加收藏
   */
  add(productId) {
    return authRequest('/api/collection/add', 'POST', { productId }, getShopApiBaseUrl())
  },

  /**
   * 取消收藏
   */
  remove(productId) {
    return authRequest('/api/collection/remove', 'POST', { productId }, getShopApiBaseUrl())
  },

  /**
   * 获取收藏列表
   */
  getList() {
    return authRequest('/api/collection/list', 'GET', null, getShopApiBaseUrl())
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeShopProduct(item))
      })
  },

  /**
   * 检查是否已收藏
   */
  check(productId) {
    return authRequest(`/api/collection/check?productId=${productId}`, 'GET', null, getShopApiBaseUrl())
  },

  /**
   * 切换收藏状态
   */
  toggle(productId) {
    return authRequest('/api/collection/toggle', 'POST', { productId }, getShopApiBaseUrl())
  }
}

// ==================== 搜索相关API（公开） ====================

const SearchAPI = {
  /**
   * 搜索商品
   */
  searchProducts(keyword) {
    return publicRequest(`/api/search/products?keyword=${encodeURIComponent(keyword)}`, 'GET', null, getShopApiBaseUrl())
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
    return publicRequest('/api/search/hot', 'GET', null, getShopApiBaseUrl())
  }
}

// ==================== 推荐相关API（公开） ====================

const RecommendationAPI = {
  /**
   * 根据购物车获取推荐商品（需登录）
   */
  getByCart(limit = 10) {
    return authRequest(`/api/recommendation/by-cart?limit=${limit}`, 'GET', null, getShopApiBaseUrl())
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeShopProduct(item))
      })
  },

  /**
   * 获取热销商品推荐（公开）
   */
  getHot(limit = 10) {
    return publicRequest(`/api/recommendation/hot?limit=${limit}`, 'GET', null, getShopApiBaseUrl())
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeShopProduct(item))
      })
  },

  /**
   * 获取相似商品推荐（公开）
   */
  getSimilar(productId, limit = 5) {
    return publicRequest(`/api/recommendation/similar/${productId}?limit=${limit}`, 'GET', null, getShopApiBaseUrl())
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
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(doctor => ({
          ...doctor,
          avatar: normalizeUploadUrl(doctor.avatar),
          avatarUrl: normalizeUploadUrl(doctor.avatar || doctor.avatarUrl)
        }))
      })
  },

  /**
   * 获取医生详情
   */
  getDetail(id) {
    return publicRequest(`/api/doctor/${id}`, 'GET')
      .then(doctor => {
        if (!doctor || typeof doctor !== 'object') return doctor
        return {
          ...doctor,
          avatar: normalizeUploadUrl(doctor.avatar),
          avatarUrl: normalizeUploadUrl(doctor.avatar || doctor.avatarUrl)
        }
      })
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
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeImageFields(item, {
          images: false,
          userAvatar: false,
          doctorAvatar: false
        }))
      })
  },

  /**
   * 获取咨询详情
   */
  getDetail(id) {
    return authRequest(`/api/consultation/${id}`, 'GET')
      .then(item => {
        if (!item || typeof item !== 'object') return item
        return normalizeImageFields(item, {
          images: false,
          userAvatar: false,
          doctorAvatar: false
        })
      })
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
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeImageFields(item, { images: false }))
      })
  },

  /**
   * 获取指定宠物的健康档案
   */
  getByPet(petId) {
    return authRequest(`/api/health/pet/${petId}`, 'GET')
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(item => normalizeImageFields(item, { images: false }))
      })
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
      .then(list => {
        if (!Array.isArray(list)) return list
        return list.map(pet => ({
          ...pet,
          avatarUrl: normalizeUploadUrl(pet.avatarUrl)
        }))
      })
  },

  /**
   * 获取宠物详情
   */
  getDetail(id) {
    return authRequest(`/api/pet/${id}`, 'GET')
      .then(pet => {
        if (!pet || typeof pet !== 'object') return pet
        return {
          ...pet,
          avatarUrl: normalizeUploadUrl(pet.avatarUrl)
        }
      })
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
  create(payloadOrContactName, contactPhone, province, city, district, detailAddress, isDefault) {
    const payload = typeof payloadOrContactName === 'object'
      ? payloadOrContactName
      : { contactName: payloadOrContactName, contactPhone, province, city, district, detailAddress, isDefault }
    return authRequest('/api/address/create', 'POST', payload)
  },

  /**
   * 更新地址
   */
  update(idOrPayload, contactName, contactPhone, province, city, district, detailAddress, isDefault) {
    const payload = typeof idOrPayload === 'object'
      ? idOrPayload
      : { id: idOrPayload, contactName, contactPhone, province, city, district, detailAddress, isDefault }
    return authRequest('/api/address/update', 'PUT', payload)
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
   * 统一上传媒体文件（可选登录）
   */
  uploadMedia(filePath, ownerType = 'diagnosis') {
    return new Promise((resolve, reject) => {
      const token = getToken()
      wx.uploadFile({
        url: `${getApiBaseUrl()}/api/media/upload?ownerType=${encodeURIComponent(ownerType)}`,
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

  uploadImage(filePath) {
    return this.uploadMedia(filePath, 'diagnosis')
  },

  /**
   * QwenMax3 聊天
   */
  chatQwenMax(message) {
    // 直接返回文本，不需要Response包装
    return new Promise((resolve, reject) => {
      const token = getToken()
      wx.request({
        url: `${getApiBaseUrl()}/api/chat/qwen3Max`,
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
        url: `${getApiBaseUrl()}/api/chat/deepSeekV3`,
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
  getDiagnosisEntry(deviceId = null) {
    const url = deviceId
      ? `/api/diagnosis/entry?deviceId=${encodeURIComponent(deviceId)}`
      : '/api/diagnosis/entry'
    return publicRequest(url, 'GET')
  },

  submitDiagnosis(data, deviceId = null) {
    const url = deviceId
      ? `/api/diagnosis/submit?deviceId=${encodeURIComponent(deviceId)}`
      : '/api/diagnosis/submit'
    return publicRequest(url, 'POST', data)
  },

  getDiagnosisTask(taskId) {
    return publicRequest(`/api/diagnosis/tasks/${taskId}`, 'GET')
  },

  getDiagnosisRemainingCount(deviceId) {
    return this.getDiagnosisEntry(deviceId).then(res => ({
      remainingCount: res.remainingCount,
      limitReached: res.remainingCount <= 0,
      isLoggedIn: res.loggedIn
    }))
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
    return authRequest('/api/messages/center', 'GET')
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
    return authRequest('/api/subscribe/create', 'POST', data, getShopApiBaseUrl())
  },
  getList() {
    return authRequest('/api/subscribe/list', 'GET', null, getShopApiBaseUrl())
  },
  pause(id) {
    return authRequest(`/api/subscribe/${id}/pause`, 'PUT', null, getShopApiBaseUrl())
  },
  resume(id) {
    return authRequest(`/api/subscribe/${id}/resume`, 'PUT', null, getShopApiBaseUrl())
  },
  cancel(id) {
    return authRequest(`/api/subscribe/${id}/cancel`, 'PUT', null, getShopApiBaseUrl())
  },
  updateConfig(id, data) {
    return authRequest(`/api/subscribe/${id}/config`, 'PUT', data, getShopApiBaseUrl())
  }
}

// ==================== 社区模块 API（公开+登录） ====================
const CommunityAPI = {
  // 帖子列表
  getFeed(page = 1, pageSize = 20, type = 'latest') {
    return publicRequest(`/api/community/feed?page=${page}&pageSize=${pageSize}&type=${type}`, 'GET')
      .then(res => normalizePageVO(res))
  },
  getPosts(page = 1, pageSize = 20, type = 'latest') {
    return publicRequest(`/api/community/posts?page=${page}&pageSize=${pageSize}&type=${type}`, 'GET')
      .then(res => normalizePageVO(res))
  },
  getHotPosts(page = 1, pageSize = 20) {
    return publicRequest(`/api/community/posts/hot?page=${page}&pageSize=${pageSize}`, 'GET')
      .then(res => normalizePageVO(res))
  },
  searchPosts(keyword, page = 1, pageSize = 20) {
    return publicRequest(`/api/community/posts/search?keyword=${encodeURIComponent(keyword)}&page=${page}&pageSize=${pageSize}`, 'GET')
      .then(res => normalizePageVO(res))
  },
  getTopicPosts(topicId, page = 1, pageSize = 20) {
    return publicRequest(`/api/community/topic/${topicId}/posts?page=${page}&pageSize=${pageSize}`, 'GET')
      .then(res => normalizePageVO(res))
  },
  getUserPosts(userId, page = 1, pageSize = 20) {
    return publicRequest(`/api/community/user/${userId}/posts?page=${page}&pageSize=${pageSize}`, 'GET')
      .then(res => normalizePageVO(res))
  },
  // 帖子详情
  getPostDetail(id) {
    return publicRequest(`/api/community/post/${id}`, 'GET')
      .then(res => normalizeCommunityPost(res))
  },
  // 帖子CRUD
  createPost(data) {
    return authRequest('/api/community/post', 'POST', data)
  },
  updatePost(id, data) {
    return authRequest(`/api/community/post/${id}`, 'PUT', data)
  },
  deletePost(id) {
    return authRequest(`/api/community/post/${id}`, 'DELETE')
  },
  // 互动功能
  likePost(id) {
    return authRequest(`/api/community/post/${id}/like`, 'POST')
  },
  unlikePost(id) {
    return authRequest(`/api/community/post/${id}/like`, 'DELETE')
  },
  collectPost(id) {
    return authRequest(`/api/community/post/${id}/collect`, 'POST')
  },
  uncollectPost(id) {
    return authRequest(`/api/community/post/${id}/collect`, 'DELETE')
  },
  sharePost(id, shareType = 'wechat') {
    return authRequest(`/api/community/post/${id}/share`, 'POST', { shareType })
  },
  reportPost(id, reason, reasonType) {
    return authRequest(`/api/community/post/${id}/report`, 'POST', { reason, reasonType })
  },
  // 评论功能
  addComment(id, content, replyToId, replyToUserId, mediaUrls, mediaType) {
    const data = { content }
    if (replyToId) data.replyToId = replyToId
    if (replyToUserId) data.replyToUserId = replyToUserId
    if (Array.isArray(mediaUrls) && mediaUrls.length > 0) data.mediaUrls = mediaUrls
    if (mediaType) data.mediaType = mediaType
    return authRequest(`/api/community/post/${id}/comment`, 'POST', data)
  },
  deleteComment(postId, commentId) {
    return authRequest(`/api/community/post/${postId}/comment/${commentId}`, 'DELETE')
  },
  getComments(id) {
    return publicRequest(`/api/community/post/${id}/comments`, 'GET')
      .then(list => (list || []).map(normalizeCommunityComment))
  },
  likeComment(postId, commentId) {
    return authRequest(`/api/community/post/${postId}/comment/${commentId}/like`, 'POST')
  },
  unlikeComment(postId, commentId) {
    return authRequest(`/api/community/post/${postId}/comment/${commentId}/like`, 'DELETE')
  },
  // 话题功能
  getHotTopics() {
    return publicRequest('/api/community/topics', 'GET')
  },
  searchTopics(keyword) {
    return publicRequest(`/api/community/topics/search?keyword=${encodeURIComponent(keyword)}`, 'GET')
  },
  getTopicDetail(topicId) {
    return publicRequest(`/api/community/topic/${topicId}`, 'GET')
  }
}

// 辅助函数：规范化分页数据
function normalizePageVO(res) {
  if (!res || !Array.isArray(res.list)) {
    return res
  }
  return {
    ...res,
    list: res.list.map(item => normalizeCommunityPost(item))
  }
}

// ==================== 关注模块 API（需登录） ====================
const FollowAPI = {
  follow(userId) {
    return authRequest(`/api/follow/${userId}`, 'POST')
  },
  unfollow(userId) {
    return authRequest(`/api/follow/${userId}`, 'DELETE')
  },
  checkFollow(userId) {
    return authRequest(`/api/follow/check/${userId}`, 'GET')
  },
  getFollowers(userId, page = 1, pageSize = 20) {
    return authRequest(`/api/follow/followers/${userId}?page=${page}&pageSize=${pageSize}`, 'GET')
  },
  getFollowings(userId, page = 1, pageSize = 20) {
    return authRequest(`/api/follow/followings/${userId}?page=${page}&pageSize=${pageSize}`, 'GET')
  },
  getFollowStats(userId) {
    return authRequest(`/api/follow/stats/${userId}`, 'GET')
  }
}

// ==================== 私信模块 API（需登录） ====================
const PrivateMsgAPI = {
  sendMessage(receiverId, content, msgType = 'text') {
    return authRequest('/api/private-message/send', 'POST', { receiverId, content, msgType })
  },
  getConversations(page = 1, pageSize = 20) {
    return authRequest(`/api/private-message/conversations?page=${page}&pageSize=${pageSize}`, 'GET')
  },
  getMessages(conversationId, page = 1, pageSize = 20) {
    return authRequest(`/api/private-message/conversation/${conversationId}?page=${page}&pageSize=${pageSize}`, 'GET')
  },
  getMessagesWithUser(targetId, page = 1, pageSize = 20) {
    return authRequest(`/api/private-message/chat/${targetId}?page=${page}&pageSize=${pageSize}`, 'GET')
  },
  markAsRead(conversationId) {
    return authRequest(`/api/private-message/read/${conversationId}`, 'POST')
  },
  getUnreadCount() {
    return authRequest('/api/private-message/unread-count', 'GET')
  },
  deleteConversation(conversationId) {
    return authRequest(`/api/private-message/conversation/${conversationId}`, 'DELETE')
  }
}

// 导出工具函数供其他模块使用
module.exports = {
  isLoggedIn,
  navigateToLogin,
  parseDate,
  // 配置相关（动态获取）
  getApiBaseUrl,
  getShopApiBaseUrl,
  // API模块
  AuthAPI,
  MapAPI,
  HomeAPI,
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
  FollowAPI,
  PrivateMsgAPI
}
