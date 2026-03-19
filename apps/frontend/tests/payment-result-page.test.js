const assert = require('assert')
const path = require('path')

const PAGE_PATH = path.join(__dirname, '../miniprogram/pages/order/pay-result/index.js')

function clearModule(modulePath) {
  delete require.cache[require.resolve(modulePath)]
}

function capturePage() {
  clearModule(PAGE_PATH)
  let pageConfig = null
  global.Page = config => {
    pageConfig = config
  }
  require(PAGE_PATH)
  delete global.Page
  return pageConfig
}

function createPageInstance(config) {
  const instance = {
    data: JSON.parse(JSON.stringify(config.data)),
    setData(update) {
      this.data = {
        ...this.data,
        ...update
      }
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

function testSuccessResultRedirectsToOrderDetail() {
  const pageConfig = capturePage()
  const page = createPageInstance(pageConfig)
  let redirectedUrl = ''
  let scheduledDelay = null

  global.wx = {
    redirectTo({ url }) {
      redirectedUrl = url
    },
    switchTab() {
      throw new Error('should not switch tab when orderId exists')
    }
  }

  const originalSetTimeout = global.setTimeout
  const originalClearTimeout = global.clearTimeout
  global.setTimeout = (fn, delay) => {
    scheduledDelay = delay
    fn()
    return 1
  }
  global.clearTimeout = () => {}

  pageConfig.onLoad.call(page, { status: 'success', orderId: '88', amount: '289.00' })

  assert.strictEqual(scheduledDelay, 1200)
  assert.strictEqual(redirectedUrl, '/pages/order/detail?id=88')

  global.setTimeout = originalSetTimeout
  global.clearTimeout = originalClearTimeout
}

function testCloseSuccessFallsBackToShopTab() {
  const pageConfig = capturePage()
  const page = createPageInstance(pageConfig)
  let switchedTab = ''

  global.wx = {
    redirectTo() {
      throw new Error('should not redirect without orderId')
    },
    switchTab({ url }) {
      switchedTab = url
    }
  }

  page.data.orderId = null
  pageConfig.closeSuccess.call(page)

  assert.strictEqual(switchedTab, '/pages/shop/shop')
}

testSuccessResultRedirectsToOrderDetail()
testCloseSuccessFallsBackToShopTab()

console.log('payment-result-page tests passed')
