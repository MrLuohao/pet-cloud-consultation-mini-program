const assert = require('assert')
const path = require('path')

const FRONTEND_ROOT = path.resolve(__dirname, '..')
const API_MODULE_PATH = path.join(FRONTEND_ROOT, 'miniprogram/utils/api.js')
const ADDRESS_EDIT_PAGE_PATH = path.join(FRONTEND_ROOT, 'miniprogram/pages/address/edit.js')
const PRODUCT_DETAIL_PAGE_PATH = path.join(FRONTEND_ROOT, 'miniprogram/pages/product/detail.js')

function clearModule(modulePath) {
  delete require.cache[require.resolve(modulePath)]
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
    setNavigationBarTitle() {},
    navigateBack() {},
    navigateTo() {}
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
      const next = { ...this.data }
      Object.entries(update).forEach(([key, value]) => {
        if (!key.includes('.')) {
          next[key] = value
          return
        }
        const segments = key.split('.')
        let cursor = next
        for (let i = 0; i < segments.length - 1; i++) {
          cursor[segments[i]] = { ...cursor[segments[i]] }
          cursor = cursor[segments[i]]
        }
        cursor[segments[segments.length - 1]] = value
      })
      this.data = next
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

function wait(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

async function testAddressEditSaveCarriesMapFields() {
  let savedPayload = null
  const pageConfig = capturePage(ADDRESS_EDIT_PAGE_PATH, {
    AddressAPI: {
      async create(payload) {
        savedPayload = payload
        return 1
      }
    },
    MapAPI: {
      async geocode() {
        return {
          formattedAddress: '上海市浦东新区世纪大道1188号',
          latitude: 31.230416,
          longitude: 121.473701
        }
      },
      async reverseGeocode() {
        return {
          formattedAddress: '上海市浦东新区世纪大道1188号',
          province: '上海市',
          city: '上海市',
          district: '浦东新区'
        }
      }
    },
    isLoggedIn() {
      return true
    },
    navigateToLogin() {
      throw new Error('should not navigate to login')
    }
  })

  const page = createPageInstance(pageConfig)
  page.data.formData = {
    contactName: '张宁',
    contactPhone: '13812345678',
    province: '上海市',
    city: '上海市',
    district: '浦东新区',
    detailAddress: '世纪大道1188号2栋1702',
    isDefault: true
  }
  page.data.pasteText = '张宁 13812345678 上海市浦东新区世纪大道1188号2栋1702'
  page.data.mapDetail = '上海市浦东新区世纪大道1188号'
  page.data.regionText = '上海市 · 上海市 · 浦东新区'
  page.data.mapPreview = {
    latitude: 31.230416,
    longitude: 121.473701
  }

  await pageConfig.saveAddress.call(page)

  assert.strictEqual(savedPayload.latitude, 31.230416)
  assert.strictEqual(savedPayload.longitude, 121.473701)
  assert.strictEqual(savedPayload.rawText, '张宁 13812345678 上海市浦东新区世纪大道1188号2栋1702')
  assert.strictEqual(savedPayload.mapAddress, '上海市浦东新区世纪大道1188号')
  assert.strictEqual(savedPayload.addressTag, 'default')
}

async function testAddressSearchSuggestionUpdatesMapAndForm() {
  let reverseLookupCount = 0
  let searchSuggestCount = 0
  const pageConfig = capturePage(ADDRESS_EDIT_PAGE_PATH, {
    AddressAPI: {
      async create() {
        return 1
      }
    },
    MapAPI: {
      async geocode() {
        return null
      },
      async reverseGeocode(latitude, longitude) {
        reverseLookupCount += 1
        assert.strictEqual(latitude, 31.230416)
        assert.strictEqual(longitude, 121.473701)
        return {
          formattedAddress: '上海市浦东新区世纪大道1188号',
          province: '上海市',
          city: '上海市',
          district: '浦东新区'
        }
      },
      async searchSuggest(keyword) {
        searchSuggestCount += 1
        assert.strictEqual(keyword, '世纪大道')
        return [
          {
            poiId: 'B001',
            name: '世纪大道 1188 号',
            address: '上海市浦东新区世纪大道1188号',
            latitude: 31.230416,
            longitude: 121.473701
          }
        ]
      }
    },
    isLoggedIn() {
      return true
    },
    navigateToLogin() {
      throw new Error('should not navigate to login')
    }
  })

  const page = createPageInstance(pageConfig)
  page.data.formData = {
    contactName: '',
    contactPhone: '',
    province: '上海市',
    city: '上海市',
    district: '浦东新区',
    detailAddress: '',
    isDefault: false
  }

  await pageConfig.onSearchKeywordInput.call(page, { detail: { value: '世纪大道' } })
  assert.strictEqual(page.data.searchLoading, true)
  assert.strictEqual(page.data.searchSuggestions.length, 0)
  await wait(320)

  assert.strictEqual(searchSuggestCount, 1)
  assert.strictEqual(page.data.searchSuggestions.length, 1)

  await pageConfig.selectSearchSuggestion.call(page, {
    currentTarget: {
      dataset: {
        index: 0
      }
    }
  })

  assert.strictEqual(reverseLookupCount, 1)
  assert.strictEqual(page.data.formData.detailAddress, '世纪大道 1188 号')
  assert.strictEqual(page.data.mapPreview.latitude, 31.230416)
  assert.strictEqual(page.data.mapPreview.longitude, 121.473701)
  assert.strictEqual(page.data.mapDetail, '上海市浦东新区世纪大道1188号')
  assert.strictEqual(page.data.searchSuggestions.length, 0)
}

async function testAddressSearchSkipsShortKeywordAndNormalizesSuggestion() {
  let searchSuggestCount = 0
  const pageConfig = capturePage(ADDRESS_EDIT_PAGE_PATH, {
    AddressAPI: {
      async create() {
        return 1
      }
    },
    MapAPI: {
      async geocode() {
        return null
      },
      async reverseGeocode() {
        return null
      },
      async searchSuggest(keyword) {
        searchSuggestCount += 1
        assert.strictEqual(keyword, '世纪大道')
        return [
          {
            poiId: 'B002',
            latitude: 31.23,
            longitude: 121.47
          }
        ]
      }
    },
    isLoggedIn() {
      return true
    },
    navigateToLogin() {
      throw new Error('should not navigate to login')
    }
  })

  const page = createPageInstance(pageConfig)

  await pageConfig.onSearchKeywordInput.call(page, { detail: { value: '世' } })
  await wait(320)

  assert.strictEqual(searchSuggestCount, 0)
  assert.strictEqual(page.data.searchLoading, false)
  assert.deepStrictEqual(page.data.searchSuggestions, [])

  await pageConfig.onSearchKeywordInput.call(page, { detail: { value: '世纪大道' } })
  await wait(320)

  assert.strictEqual(searchSuggestCount, 1)
  assert.strictEqual(page.data.searchSuggestions.length, 1)
  assert.strictEqual(page.data.searchSuggestions[0].name, '相关地点')
  assert.strictEqual(page.data.searchSuggestions[0].address, '请点击后同步到地图')
}

async function testAddressSaveRegeocodesAfterManualDetailChange() {
  let savedPayload = null
  let geocodeCallCount = 0
  const pageConfig = capturePage(ADDRESS_EDIT_PAGE_PATH, {
    AddressAPI: {
      async update(payload) {
        savedPayload = payload
      }
    },
    MapAPI: {
      async geocode(address, cityCode) {
        geocodeCallCount += 1
        assert.strictEqual(cityCode, '上海市')
        assert.ok(address.includes('上海市'))
        assert.ok(address.includes('徐汇区'))
        assert.ok(address.includes('龙腾大道 2888 号'))
        return {
          formattedAddress: '上海市徐汇区龙腾大道2888号',
          latitude: 31.184512,
          longitude: 121.454321
        }
      },
      async reverseGeocode() {
        return null
      },
      async searchSuggest() {
        return []
      }
    },
    isLoggedIn() {
      return true
    },
    navigateToLogin() {
      throw new Error('should not navigate to login')
    }
  })

  const page = createPageInstance(pageConfig)
  page.data.addressId = 9
  page.data.formData = {
    contactName: '林如夏',
    contactPhone: '13800138000',
    province: '上海市',
    city: '上海市',
    district: '徐汇区',
    detailAddress: '龙腾大道 2879 号',
    isDefault: false
  }
  page.data.mapPreview = {
    latitude: 31.180001,
    longitude: 121.440001
  }

  pageConfig.onInputChange.call(page, {
    currentTarget: { dataset: { field: 'detailAddress' } },
    detail: { value: '龙腾大道 2888 号' }
  })

  await pageConfig.saveAddress.call(page)

  assert.strictEqual(geocodeCallCount, 1)
  assert.strictEqual(savedPayload.latitude, 31.184512)
  assert.strictEqual(savedPayload.longitude, 121.454321)
  assert.strictEqual(savedPayload.mapAddress, '上海市徐汇区龙腾大道2888号')
}

async function testProductDetailCanJumpToWriteReview() {
  let navigateUrl = ''
  const pageConfig = capturePage(PRODUCT_DETAIL_PAGE_PATH, {
    ProductAPI: {
      async getDetail() {
        return {
          id: 101,
          name: '低温烘焙鸡肉主粮',
          summary: '稳定、轻负担的日常主食选择。',
          price: 129
        }
      },
      async getReviewSummary() {
        return { total: 3, goodCount: 3, badCount: 0, withImagesCount: 2, avgRating: 5 }
      },
      async getReviewableOrderItem() {
        return {
          orderId: 9001,
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
      async add() {
        return 1
      }
    },
    isLoggedIn() {
      return true
    },
    navigateToLogin() {
      throw new Error('should not navigate to login')
    }
  })

  global.wx = {
    showToast() {},
    showLoading() {},
    hideLoading() {},
    navigateBack() {},
    navigateTo({ url }) {
      navigateUrl = url
    }
  }

  const page = createPageInstance(pageConfig)
  pageConfig.onLoad.call(page, { id: '101' })
  await Promise.resolve()
  await Promise.resolve()
  await Promise.resolve()
  await Promise.resolve()

  assert.strictEqual(page.data.reviewableOrderItem.orderItemId, 8001)

  pageConfig.goToWriteReview.call(page)

  assert.ok(navigateUrl.includes('/pages/order/review?'))
  assert.ok(navigateUrl.includes('orderId=9001'))
  assert.ok(navigateUrl.includes('itemData='))
}

async function run() {
  await testAddressEditSaveCarriesMapFields()
  await testAddressSearchSuggestionUpdatesMapAndForm()
  await testAddressSearchSkipsShortKeywordAndNormalizesSuggestion()
  await testAddressSaveRegeocodesAfterManualDetailChange()
  await testProductDetailCanJumpToWriteReview()
  console.log('address-edit-map-review-flow tests passed')
}

run().catch(error => {
  console.error(error)
  process.exit(1)
})
