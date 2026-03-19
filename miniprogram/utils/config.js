// utils/config.js - 动态配置管理

/**
 * 默认后端服务配置
 * 注意：微信小程序真机调试不支持本地回环地址，需要使用局域网IP
 *
 * 如何获取本机IP：
 *   Windows: 打开 cmd，输入 ipconfig，查看 IPv4 地址
 *   Mac/Linux: 打开终端，输入 ifconfig | grep "inet " | grep -v 127.0.0.1
 */
const DEFAULT_CONFIG = {
  // 默认API主机地址（真机调试请改为当前电脑局域网IP）
  API_HOST: '192.168.1.4',
  // 服务端口
  USER_SERVICE_PORT: 8117,    // 用户服务
  SHOP_SERVICE_PORT: 8118,    // 商城服务
  MAP_SERVICE_PORT: 8120      // 地图服务
}

// 避免 devtools 将 localhost 解析到 ::1 后触发媒体资源加载异常。
const DEVTOOLS_API_HOST = '127.0.0.1'
const LOOPBACK_HOSTS = ['127.0.0.1', 'localhost', '::1']

// 历史版本里用过的默认Host，统一迁移到当前平台推荐地址
const LEGACY_DEFAULT_HOSTS = ['10.0.12.147', '192.168.1.5', '192.168.1.3', '192.168.1.23', '127.0.0.1', 'localhost']

/**
 * 获取当前API主机地址
 * 优先级：Storage > 平台默认值
 * @returns {string}
 */
function isDevtoolsPlatform() {
  try {
    const systemInfo = wx.getSystemInfoSync()
    return systemInfo && systemInfo.platform === 'devtools'
  } catch (e) {
    return false
  }
}

function getDefaultApiHost() {
  return isDevtoolsPlatform() ? DEVTOOLS_API_HOST : DEFAULT_CONFIG.API_HOST
}

function getApiHost() {
  const storedHost = wx.getStorageSync('apiHost')
  const fallbackHost = getDefaultApiHost()
  if (storedHost) {
    if (LEGACY_DEFAULT_HOSTS.includes(storedHost) && storedHost !== fallbackHost) {
      wx.setStorageSync('apiHost', fallbackHost)
      return fallbackHost
    }
    return storedHost
  }

  return fallbackHost
}

function getUploadHost() {
  const apiHost = getApiHost()

  // 微信开发者工具对回环地址的视频资源加载兼容性较差。
  // API 请求继续走回环地址，但媒体资源优先走局域网 IP。
  if (isDevtoolsPlatform() && LOOPBACK_HOSTS.includes(apiHost) && !LOOPBACK_HOSTS.includes(DEFAULT_CONFIG.API_HOST)) {
    return DEFAULT_CONFIG.API_HOST
  }

  return apiHost
}

/**
 * 获取当前环境推荐的API Host
 * 开发者工具: 默认 127.0.0.1
 * 真机: 默认局域网IP
 */
function getRecommendedApiHost() {
  return getDefaultApiHost()
}

/**
 * 设置API主机地址
 * @param {string} host - 主机地址（IP或域名）
 */
function setApiHost(host) {
  if (host) {
    wx.setStorageSync('apiHost', host)
  } else {
    wx.removeStorageSync('apiHost')
  }
}

/**
 * 获取用户服务基础URL
 * @returns {string}
 */
function getApiBaseUrl() {
  return `http://${getApiHost()}:${DEFAULT_CONFIG.USER_SERVICE_PORT}`
}

/**
 * 获取商城服务基础URL
 * @returns {string}
 */
function getShopApiBaseUrl() {
  return `http://${getApiHost()}:${DEFAULT_CONFIG.SHOP_SERVICE_PORT}`
}

function getMapApiBaseUrl() {
  return `http://${getApiHost()}:${DEFAULT_CONFIG.MAP_SERVICE_PORT}`
}

/**
 * 获取完整的上传文件URL
 * @param {string} path - 文件路径（如 /uploads/xxx.jpg）
 * @param {boolean} useShopPort - 是否使用商城端口
 * @returns {string}
 */
function getUploadUrl(path, useShopPort = false) {
  if (!path || typeof path !== 'string') return path
  const raw = path.trim()
  if (!raw) return ''

  const port = useShopPort ? DEFAULT_CONFIG.SHOP_SERVICE_PORT : DEFAULT_CONFIG.USER_SERVICE_PORT
  const targetHost = getUploadHost()

  // 历史数据兼容：只要是 /uploads 路径的绝对地址，都改写为当前 Host（忽略原始 host/port）
  if (raw.startsWith('http://') || raw.startsWith('https://')) {
    const uploadPath = raw.match(/^https?:\/\/[^/]+(\/uploads\/.*)$/)
    if (uploadPath && uploadPath[1]) {
      return `http://${targetHost}:${port}${uploadPath[1]}`
    }
    return raw
  }

  if (raw.startsWith('/uploads/')) {
    return `http://${targetHost}:${port}${raw}`
  }
  return raw
}

/**
 * 测试后端连接
 * @returns {Promise<{success: boolean, message: string, latency?: number}>}
 */
function testConnection() {
  return new Promise((resolve) => {
    const startTime = Date.now()

    // 尝试请求文章列表接口作为健康检查（公开接口，不需要登录）
    wx.request({
      url: `${getApiBaseUrl()}/api/article/list?page=1&pageSize=1`,
      method: 'GET',
      timeout: 5000,
      success: (res) => {
        const latency = Date.now() - startTime
        if (res.statusCode === 200) {
          resolve({ success: true, message: `连接成功 (${latency}ms)`, latency })
        } else if (res.statusCode === 404) {
          resolve({ success: false, message: '接口不存在，请检查后端服务' })
        } else {
          resolve({ success: false, message: `服务返回 ${res.statusCode}` })
        }
      },
      fail: (err) => {
        resolve({ success: false, message: `连接失败: ${err.errMsg || '网络错误'}` })
      }
    })
  })
}

/**
 * 获取默认配置（用于显示）
 * @returns {object}
 */
function getDefaultConfig() {
  return { ...DEFAULT_CONFIG }
}

module.exports = {
  getApiHost,
  getUploadHost,
  getRecommendedApiHost,
  isDevtoolsPlatform,
  setApiHost,
  getApiBaseUrl,
  getShopApiBaseUrl,
  getMapApiBaseUrl,
  getUploadUrl,
  testConnection,
  getDefaultConfig
}
