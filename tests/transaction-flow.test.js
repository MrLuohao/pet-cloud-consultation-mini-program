const assert = require('assert')
const path = require('path')

const FRONTEND_ROOT = path.resolve(__dirname, '..')
const API_MODULE_PATH = path.join(FRONTEND_ROOT, 'miniprogram/utils/api.js')
const CONFIG_MODULE_PATH = path.join(FRONTEND_ROOT, 'miniprogram/utils/config.js')
const CART_PAGE_PATH = path.join(FRONTEND_ROOT, 'miniprogram/pages/cart/cart.js')
const ORDER_CONFIRM_PAGE_PATH = path.join(FRONTEND_ROOT, 'miniprogram/pages/order/confirm.js')

function createWxStub({ storage = {}, requestImpl } = {}) {
  return {
    getSystemInfoSync() {
      return { platform: 'devtools' }
    },
    getStorageSync(key) {
      return storage[key]
    },
    setStorageSync(key, value) {
      storage[key] = value
    },
    removeStorageSync(key) {
      delete storage[key]
    },
    request(options) {
      requestImpl(options)
    },
    showToast() {},
    hideLoading() {},
    showLoading() {},
    showActionSheet() {},
    navigateTo() {},
    switchTab() {}
  }
}

function clearModule(modulePath) {
  delete require.cache[require.resolve(modulePath)]
}

function loadApiModule({ requestImpl, storage = { token: 'test-token' } } = {}) {
  clearModule(CONFIG_MODULE_PATH)
  clearModule(API_MODULE_PATH)

  global.getApp = () => ({ globalData: {} })
  global.wx = createWxStub({ storage, requestImpl })

  return require(API_MODULE_PATH)
}

function capturePage(pagePath, mockedApi) {
  clearModule(pagePath)
  clearModule(API_MODULE_PATH)

  let pageConfig = null

  global.getApp = () => ({ globalData: {} })
  global.wx = {
    showToast() {},
    hideLoading() {},
    showLoading() {},
    showActionSheet() {},
    navigateTo() {},
    switchTab() {}
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

async function testCartApiNormalizesStructuredPayload() {
  const api = loadApiModule({
    requestImpl(options) {
      assert.strictEqual(options.url, 'http://127.0.0.1:8118/api/cart/list')
      options.success({
        statusCode: 200,
        data: {
          status: true,
          data: {
            cartGroups: [
              {
                merchantId: 'official',
                merchantName: '伴宠云诊自营',
                serviceText: '包邮 · 正品保障',
                items: [
                  {
                    id: '2',
                    productId: '1',
                    name: '进口天然猫粮 2kg',
                    coverUrl: '/uploads/products/cat_food.jpg',
                    quantity: 1,
                    price: 199
                  }
                ]
              }
            ],
            invalidItems: [
              {
                id: '9',
                name: '失效商品',
                coverUrl: '/uploads/products/expired.jpg'
              }
            ],
            summary: {
              selectedCount: 1,
              totalAmount: 199,
              totalDiscount: 100,
              allSelected: true
            }
          }
        }
      })
    }
  })

  const config = require(CONFIG_MODULE_PATH)
  const payload = await api.CartAPI.getList()

  assert.strictEqual(payload.cartGroups.length, 1)
  assert.strictEqual(
    payload.cartGroups[0].items[0].coverUrl,
    config.getUploadUrl('/uploads/products/cat_food.jpg', true)
  )
  assert.strictEqual(
    payload.invalidItems[0].coverUrl,
    config.getUploadUrl('/uploads/products/expired.jpg', true)
  )
}

async function testCartPageCheckoutCarriesCartIds() {
  const mockedApi = {
    CartAPI: {
      async getList() {
        return {
          cartGroups: [
            {
              merchantId: 'official',
              merchantName: '伴宠云诊自营',
              serviceText: '包邮 · 正品保障',
              allSelected: true,
              items: [
                {
                  id: '2',
                  productId: '1',
                  name: '进口天然猫粮 2kg',
                  price: 199,
                  originalPrice: 299,
                  quantity: 1,
                  selected: true,
                  status: 'active'
                }
              ]
            }
          ],
          invalidItems: [],
          summary: {
            selectedCount: 1,
            totalAmount: 199,
            totalDiscount: 100,
            allSelected: true
          }
        }
      }
    },
    RecommendationAPI: {
      async getByCart() {
        return []
      }
    },
    isLoggedIn() {
      return true
    },
    navigateToLogin() {
      throw new Error('should not navigate to login')
    }
  }

  let navigateUrl = ''
  const pageConfig = capturePage(CART_PAGE_PATH, mockedApi)
  global.wx = {
    showToast() {},
    hideLoading() {},
    showLoading() {},
    navigateTo({ url }) {
      navigateUrl = url
    },
    switchTab() {}
  }
  const page = createPageInstance(pageConfig)

  await pageConfig.loadCartList.call(page)
  pageConfig.checkout.call(page)

  assert.strictEqual(page.data.cartList.length, 1)
  const cartIdsParam = navigateUrl.split('cartIds=')[1]
  assert.deepStrictEqual(JSON.parse(decodeURIComponent(cartIdsParam)), ['2'])
}

async function testOrderConfirmUsesBackendPaymentDraft() {
  let submitPayload = null
  let confirmArgs = null
  const mockedApi = {
    OrderAPI: {
      async getConfirm(productIds, quantities, cartIds, specLabels) {
        confirmArgs = { productIds, quantities, cartIds, specLabels }
        return {
          items: [
            {
              productId: '1',
              productName: '进口天然猫粮 2kg',
              price: 199,
              quantity: 1
            }
          ],
          goodsAmount: 199,
          freight: 0,
          couponDiscount: 0,
          payAmount: 199,
          deliveryText: '顺丰冷链',
          paymentMethods: [
            {
              key: 'alipay',
              title: '支付宝',
              subtitle: '切换支付方式 · 支持余额与花呗',
              verifyType: 'password'
            }
          ],
          selectedPaymentMethod: 'alipay'
        }
      },
      async submit(payload) {
        submitPayload = payload
        return '88'
      }
    },
    AddressAPI: {
      async getDefault() {
        return {
          id: '1',
          receiverName: '张宁',
          receiverPhone: '13812345678',
          fullAddress: '上海市浦东新区世纪大道1188号2栋1702'
        }
      }
    },
    CouponAPI: {},
    isLoggedIn() {
      return true
    },
    navigateToLogin() {
      throw new Error('should not navigate to login')
    }
  }

  let navigateUrl = ''
  const pageConfig = capturePage(ORDER_CONFIRM_PAGE_PATH, mockedApi)
  global.wx = {
    showToast() {},
    hideLoading() {},
    showLoading() {},
    showActionSheet() {},
    navigateTo({ url }) {
      navigateUrl = url
    }
  }
  const page = createPageInstance(pageConfig)

  pageConfig.onLoad.call(page, {
    productIds: '[1]',
    quantities: '[1]',
    cartIds: '[2]',
    specLabels: encodeURIComponent('["400g · 幼年期"]')
  })

  await Promise.resolve()
  await Promise.resolve()

  assert.strictEqual(page.data.selectedPaymentMethod, 'alipay')
  assert.strictEqual(page.data.paymentMethods.length, 1)
  assert.strictEqual(page.data.paymentMethods[0].key, 'alipay')
  assert.deepStrictEqual(page.data.cartIds, [2])
  assert.deepStrictEqual(page.data.specLabels, ['400g · 幼年期'])
  assert.deepStrictEqual(confirmArgs, {
    productIds: [1],
    quantities: [1],
    cartIds: [2],
    specLabels: ['400g · 幼年期']
  })

  pageConfig.submitOrder.call(page)
  pageConfig.confirmPaymentMethod.call(page)

  assert.strictEqual(page.data.showVerification, true)
  assert.strictEqual(page.data.verificationStage, 'face')

  pageConfig.completeFaceVerification.call(page)

  assert.strictEqual(page.data.verificationStage, 'password')
  assert.strictEqual(page.data.paymentPassword, '')
  assert.deepStrictEqual(
    page.data.passwordKeypadRows.map(row => row.map(item => item.label)),
    [
      ['1', '2', '3'],
      ['4', '5', '6'],
      ['7', '8', '9'],
      ['Face ID', '0', '删除']
    ]
  )

  for (const key of ['1', '2', '3', '4', '5', '6']) {
    await pageConfig.handlePasswordKey.call(page, {
      currentTarget: {
        dataset: { key }
      }
    })
  }

  assert.deepStrictEqual(submitPayload, {
    productIds: [1],
    quantities: [1],
    cartIds: [2],
    specLabels: ['400g · 幼年期'],
    addressId: '1',
    couponId: null,
    remark: '',
    paymentMethod: 'alipay',
    verificationType: 'password'
  })
  assert.ok(navigateUrl.includes('orderId=88'))
  assert.strictEqual(page.data.paymentPassword, '')
}

async function run() {
  await testCartApiNormalizesStructuredPayload()
  await testCartPageCheckoutCarriesCartIds()
  await testOrderConfirmUsesBackendPaymentDraft()
  console.log('transaction-flow tests passed')
}

run().catch(error => {
  console.error(error)
  process.exit(1)
})
