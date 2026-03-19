const assert = require('assert')
const path = require('path')

const FRONTEND_ROOT = path.resolve(__dirname, '..')
const API_MODULE_PATH = path.join(FRONTEND_ROOT, 'miniprogram/utils/api.js')
const PRODUCT_DETAIL_PAGE_PATH = path.join(FRONTEND_ROOT, 'miniprogram/pages/product/detail.js')

function clearModule(modulePath) {
  delete require.cache[require.resolve(modulePath)]
}

function capturePage(pagePath, mockedApi) {
  clearModule(pagePath)
  clearModule(API_MODULE_PATH)

  let pageConfig = null

  global.getApp = () => ({ globalData: {} })
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

async function flushTasks() {
  await Promise.resolve()
  await Promise.resolve()
}

async function testProductDetailConsumesBackendConfiguredContent() {
  let addToCartPayload = null
  let navigateUrl = ''

  const mockedApi = {
    ProductAPI: {
      async getDetail() {
        return {
          id: 101,
          name: '低温烘焙鸡肉主粮',
          summary: '稳定、轻负担的日常主食选择。',
          price: 129,
          specGroups: [
            {
              key: 'capacity',
              label: '规格',
              selectedValue: '400g',
              options: [
                { value: '400g', label: '400g', hint: '试吃装' },
                { value: '1.5kg', label: '1.5kg', hint: '主规格' }
              ]
            },
            {
              key: 'lifeStage',
              label: '适用阶段',
              selectedValue: '全阶段',
              options: [
                { value: '幼年期', label: '幼年期', hint: '成长支持' },
                { value: '全阶段', label: '全阶段', hint: '通用' }
              ]
            }
          ],
          highlights: [
            '低温烘焙，保留更自然的风味与适口性。',
            '温和主食配方，适合作为稳定的日常选择。'
          ],
          storySections: [
            {
              title: '温和日常配方',
              description: '适合作为稳定、轻负担的日常主食选择。',
              imageUrl: 'https://example.com/story-1.jpg'
            }
          ],
          usageNote: {
            title: '适用对象 / 使用建议',
            content: '适合全阶段猫咪日常喂养。'
          }
        }
      },
      async getReviewSummary() {
        return {
          total: 12,
          goodCount: 10,
          badCount: 1,
          withImagesCount: 6,
          avgRating: 4.8
        }
      },
      async getReviewableOrderItem() {
        return {
          orderItemId: 8001,
          productId: 101,
          productName: '低温烘焙鸡肉主粮',
          coverUrl: 'https://example.com/cover.jpg',
          price: 129,
          quantity: 1
        }
      }
    },
    CartAPI: {
      async add(productId, quantity, specLabel) {
        addToCartPayload = { productId, quantity, specLabel }
        return 1
      }
    },
    isLoggedIn() {
      return true
    },
    navigateToLogin() {
      throw new Error('should not navigate to login')
    }
  }

  const pageConfig = capturePage(PRODUCT_DETAIL_PAGE_PATH, mockedApi)
  global.wx = {
    showToast() {},
    showLoading() {},
    hideLoading() {},
    navigateTo({ url }) {
      navigateUrl = url
    }
  }

  const page = createPageInstance(pageConfig)
  pageConfig.onLoad.call(page, { id: '101' })
  await flushTasks()

  assert.deepStrictEqual(page.data.highlights, [
    '低温烘焙，保留更自然的风味与适口性。',
    '温和主食配方，适合作为稳定的日常选择。'
  ])
  assert.strictEqual(page.data.storySections.length, 1)
  assert.strictEqual(page.data.usageNote.content, '适合全阶段猫咪日常喂养。')
  assert.strictEqual(page.data.specGroups[0].selectedLabel, '400g')
  assert.strictEqual(page.data.specGroups[1].selectedLabel, '全阶段')
  assert.strictEqual(page.data.reviewSummary.total, 12)
  assert.strictEqual(page.data.reviewSummary.avgRatingDisplay, '4.8')
  assert.strictEqual(page.data.reviewableOrderItem.orderItemId, 8001)

  pageConfig.selectSpec.call(page, {
    currentTarget: {
      dataset: {
        group: 'lifeStage',
        value: '幼年期'
      }
    }
  })

  await pageConfig.addToCart.call(page)

  assert.deepStrictEqual(addToCartPayload, {
    productId: '101',
    quantity: 1,
    specLabel: '400g · 幼年期'
  })

  pageConfig.buyNow.call(page)
  const specLabelsParam = navigateUrl.split('specLabels=')[1]
  assert.deepStrictEqual(JSON.parse(decodeURIComponent(specLabelsParam)), ['400g · 幼年期'])
}

testProductDetailConsumesBackendConfiguredContent().then(() => {
  console.log('product-detail-config tests passed')
})
