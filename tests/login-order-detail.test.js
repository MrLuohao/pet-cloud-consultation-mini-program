const assert = require('assert')
const path = require('path')

const ORDER_DETAIL_PRESENTER_PATH = path.join(__dirname, '..', 'miniprogram/pages/order/order-detail-presenter.js')
const LOGIN_PAGE_PATH = path.join(__dirname, '..', 'miniprogram/pages/login/login.js')
const API_MODULE_PATH = path.join(__dirname, '..', 'miniprogram/utils/api.js')

function clearModule(modulePath) {
  delete require.cache[require.resolve(modulePath)]
}

function capturePage(pagePath, mockedApi) {
  clearModule(pagePath)
  clearModule(API_MODULE_PATH)

  let pageConfig = null
  global.getApp = () => ({ globalData: {} })
  global.wx = {
    navigateBack() {},
    switchTab() {},
    showToast() {},
    login() {},
    setStorageSync() {},
    getStorageSync() {
      return ''
    }
  }
  global.Page = config => {
    pageConfig = config
  }

  require.cache[require.resolve(API_MODULE_PATH)] = {
    exports: mockedApi
  }

  require(pagePath)

  delete require.cache[require.resolve(API_MODULE_PATH)]
  delete global.Page

  return pageConfig
}

function createPageInstance(config) {
  const instance = {
    data: JSON.parse(JSON.stringify(config.data)),
    setData(update) {
      this.data = { ...this.data, ...update }
    }
  }

  Object.keys(config).forEach(key => {
    if (key === 'data') return
    if (typeof config[key] === 'function') {
      instance[key] = config[key]
    }
  })

  return instance
}

function testPendingOrderPresenterMatchesPenSections() {
  clearModule(ORDER_DETAIL_PRESENTER_PATH)
  const { formatOrderDetail } = require(ORDER_DETAIL_PRESENTER_PATH)

  const detail = formatOrderDetail({
    status: 0,
    statusDesc: '待支付',
    receiverName: '林如夏',
    receiverPhone: '13800138000',
    receiverAddress: '上海市徐汇区龙腾大道2879号2栋903室',
    items: [
      {
        productName: '冻干鸡肉双拼主粮',
        specLabel: '成猫配方 · 1.5kg',
        price: 268,
        quantity: 1
      }
    ],
    totalAmount: 268,
    freight: 12,
    couponDiscount: 24,
    payAmount: 256,
    orderNo: 'PC20260315000124',
    createTime: '2026-03-15 16:49:00',
    payDeadlineText: '剩余 29 分 48 秒',
    remark: '如果宠物不适，请尽快联系在线医生'
  })

  assert.strictEqual(detail.statusTitle, '待支付')
  assert.strictEqual(detail.primaryAction.label, '继续支付')
  assert.strictEqual(detail.secondaryAction.label, '取消订单')
  assert.strictEqual(detail.receiverLine, '林如夏  ·  138 0013 8000')
  assert.strictEqual(detail.amountRows[2].label, '优惠')
  assert.strictEqual(detail.amountRows[3].value, '¥256')
}

function testCompletedOrderPresenterMatchesCompletedPenSections() {
  clearModule(ORDER_DETAIL_PRESENTER_PATH)
  const { formatOrderDetail } = require(ORDER_DETAIL_PRESENTER_PATH)

  const detail = formatOrderDetail({
    status: 3,
    statusDesc: '已完成',
    receiverName: '林如夏',
    receiverPhone: '13800138000',
    receiverAddress: '上海市徐汇区龙腾大道2879号2栋903室',
    items: [
      {
        productName: '冻干鸡肉双拼主粮',
        specLabel: '成猫配方 · 1.5kg',
        price: 268,
        quantity: 1,
        reviewed: false
      }
    ],
    totalAmount: 446,
    freight: 12,
    couponDiscount: 24,
    payAmount: 434,
    orderNo: 'PC20260315000124',
    createTime: '2026-03-15 14:28',
    payTime: '2026-03-15 14:31',
    afterSaleStatusText: '已完成 · 暂未申请售后'
  })

  assert.strictEqual(detail.statusTitle, '已完成')
  assert.strictEqual(detail.statusChip, '已完成')
  assert.strictEqual(detail.statusVariant, 'completed')
  assert.strictEqual(detail.statusProgressText, '交易已完成，订单信息已归档保存。')
  assert.strictEqual(detail.amountTitle, '实付信息')
  assert.strictEqual(detail.amountRows[3].label, '实付金额')
  assert.strictEqual(detail.secondaryAction.label, '申请售后')
  assert.strictEqual(detail.primaryAction.label, '去评价')
  assert.strictEqual(detail.infoRows[2].label, '支付时间')
  assert.strictEqual(detail.infoRows[3].label, '完成状态')
}

function testLoginHydratesDraftProfileFromStorage() {
  const pageConfig = capturePage(LOGIN_PAGE_PATH, {
    AuthAPI: {},
    AIAPI: {}
  })

  global.wx = {
    getStorageSync(key) {
      if (key === 'userInfo') {
        return {
          nickname: '团子',
          avatarUrl: 'https://cdn.example.com/avatar.png'
        }
      }
      return ''
    }
  }

  const page = createPageInstance(pageConfig)
  pageConfig.onLoad.call(page)

  assert.strictEqual(page.data.nickname, '团子')
  assert.strictEqual(page.data.avatarUrl, 'https://cdn.example.com/avatar.png')
}

testPendingOrderPresenterMatchesPenSections()
testCompletedOrderPresenterMatchesCompletedPenSections()
testLoginHydratesDraftProfileFromStorage()

console.log('login-order-detail tests passed')
