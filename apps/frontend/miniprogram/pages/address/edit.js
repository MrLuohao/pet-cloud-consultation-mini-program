const { AddressAPI, MapAPI, isLoggedIn, navigateToLogin } = require('../../utils/api')
const { parsePastedAddress, buildMapPreviewState } = require('./address-presenter')

const DEFAULT_FORM = {
  contactName: '',
  contactPhone: '',
  province: '上海市',
  city: '上海市',
  district: '浦东新区',
  detailAddress: '',
  isDefault: false
}

const DEFAULT_REGION = ['上海市', '上海市', '浦东新区']

function cleanText(value) {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

function countParsedFields(parsed) {
  return [
    parsed.contactName,
    parsed.contactPhone,
    parsed.province || parsed.city,
    parsed.detailAddress
  ].filter(Boolean).length
}

function buildRegionText(formData) {
  return [formData.province, formData.city, formData.district]
    .map(cleanText)
    .filter(Boolean)
    .join(' · ')
}

function buildFullAddress(formData) {
  return [
    cleanText(formData.province),
    cleanText(formData.city),
    cleanText(formData.district),
    cleanText(formData.detailAddress)
  ].filter(Boolean).join('')
}

function normalizeSuggestion(item) {
  if (!item || typeof item !== 'object') return null
  const name = cleanText(item.name)
  const address = cleanText(item.address)
  return {
    id: item.poiId || item.id || `${item.name || ''}-${item.address || ''}`,
    name: name || '相关地点',
    address: address || '请点击后同步到地图',
    latitude: item.latitude,
    longitude: item.longitude
  }
}

Page({
  data: {
    isEdit: false,
    addressId: null,
    region: DEFAULT_REGION,
    formData: DEFAULT_FORM,
    pasteText: '',
    parseCount: 0,
    regionText: buildRegionText(DEFAULT_FORM),
    searchKeyword: '',
    searchSuggestions: [],
    searchLoading: false,
    mapTitle: '世纪大道 1188 号',
    mapDetail: '张宁 13812345678 上海市浦东新区世纪大道1188号2栋1702',
    mapHint: '输入地址后同步更新',
    mapPreview: buildMapPreviewState({
      title: '世纪大道 1188 号'
    }),
    locationDirty: false,
    detailHint: '门牌号、楼层与房间号等详细信息请手动补充，也可通过粘贴完整地址辅助识别。'
  },

  onLoad(options) {
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    if (options.id) {
      this.setData({ addressId: options.id, isEdit: true })
      wx.setNavigationBarTitle({ title: '编辑地址' })
      this.loadAddressDetail()
      return
    }

    wx.setNavigationBarTitle({ title: '新增地址' })
    this.refreshDerivedState()
  },

  async loadAddressDetail() {
    try {
      wx.showLoading({ title: '加载中...' })
      const address = await AddressAPI.getDetail(this.data.addressId)
      const formData = {
        contactName: cleanText(address.contactName),
        contactPhone: cleanText(address.contactPhone),
        province: cleanText(address.province) || DEFAULT_FORM.province,
        city: cleanText(address.city) || DEFAULT_FORM.city,
        district: cleanText(address.district) || DEFAULT_FORM.district,
        detailAddress: cleanText(address.detailAddress),
        isDefault: !!address.isDefault
      }
      this.setData({
        region: [formData.province, formData.city, formData.district],
        formData,
        pasteText: address.fullAddress || '',
        searchKeyword: cleanText(address.detailAddress || address.mapAddress || address.fullAddress)
      })
      this.refreshDerivedState({
        mapTitle: address.detailAddress || this.data.mapTitle,
        mapDetail: address.fullAddress || this.data.mapDetail,
        mapPreview: buildMapPreviewState({
          latitude: address.latitude,
          longitude: address.longitude,
          title: address.detailAddress || address.mapAddress || address.fullAddress
        }),
        locationDirty: false
      })
    } catch (error) {
      console.error('加载地址详情失败:', error)
      wx.showToast({ title: '地址加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  refreshDerivedState(extra = {}) {
    this.setData({
      regionText: buildRegionText(this.data.formData),
      ...extra
    })
  },

  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    const value = e.detail.value
    this.setData({
      [`formData.${field}`]: value
    })

    if (field === 'detailAddress') {
      this.refreshDerivedState({
        mapTitle: value || this.data.mapTitle,
        searchKeyword: value,
        mapHint: '请输入地点后搜索更新地图',
        locationDirty: true
      })
      return
    }

    this.refreshDerivedState()
  },

  onRegionChange(e) {
    const region = e.detail.value
    this.setData({
      region,
      'formData.province': region[0],
      'formData.city': region[1],
      'formData.district': region[2]
    })
    this.refreshDerivedState({
      mapDetail: `${region.join(' · ')}${this.data.formData.detailAddress ? ` · ${this.data.formData.detailAddress}` : ''}`,
      mapHint: '地区已更新，请重新搜索或选点同步地图',
      locationDirty: true
    })
  },

  onDefaultToggle() {
    this.setData({
      'formData.isDefault': !this.data.formData.isDefault
    })
  },

  onPasteInput(e) {
    this.setData({
      pasteText: e.detail.value,
      locationDirty: true
    })
  },

  onPasteBlur() {
    if (!this.data.pasteText) return
    this.handlePasteRecognition(false)
  },

  applyParsedAddress(parsed, options = {}) {
    const nextForm = {
      ...this.data.formData
    }

    ;['contactName', 'contactPhone', 'province', 'city', 'district', 'detailAddress'].forEach(field => {
      if (parsed[field]) {
        nextForm[field] = parsed[field]
      }
    })

    const nextRegion = [
      nextForm.province || DEFAULT_FORM.province,
      nextForm.city || DEFAULT_FORM.city,
      nextForm.district || DEFAULT_FORM.district
    ]

    this.setData({
      formData: nextForm,
      region: nextRegion,
      parseCount: countParsedFields(parsed),
      searchKeyword: options.searchKeyword || parsed.detailAddress || nextForm.detailAddress || this.data.searchKeyword,
      searchSuggestions: []
    })

    this.refreshDerivedState({
      mapTitle: options.mapTitle || parsed.detailAddress || nextForm.detailAddress || this.data.mapTitle,
      mapDetail: options.mapDetail || `${buildRegionText(nextForm)}${nextForm.detailAddress ? ` · ${nextForm.detailAddress}` : ''}`,
      mapHint: options.mapHint || this.data.mapHint,
      mapPreview: buildMapPreviewState({
        latitude: options.latitude,
        longitude: options.longitude,
        title: options.mapTitle || parsed.detailAddress || nextForm.detailAddress || this.data.mapTitle
      }),
      locationDirty: options.locationDirty === undefined ? false : !!options.locationDirty
    })
  },

  async onSearchKeywordInput(e) {
    const keyword = cleanText(e.detail.value)

    if (this.searchSuggestTimer) {
      clearTimeout(this.searchSuggestTimer)
      this.searchSuggestTimer = null
    }

    this.setData({
      searchKeyword: keyword,
      searchLoading: keyword.length >= 2
    })

    if (keyword.length < 2) {
      this.setData({
        searchSuggestions: [],
        searchLoading: false
      })
      return
    }

    const requestKeyword = keyword
    this.searchSuggestTimer = setTimeout(async () => {
      try {
        const list = await MapAPI.searchSuggest(
          requestKeyword,
          this.data.formData.city || this.data.formData.province || '',
          'address'
        )
        if (this.data.searchKeyword !== requestKeyword) {
          return
        }
        this.setData({
          searchSuggestions: (list || []).map(normalizeSuggestion).filter(Boolean).slice(0, 6),
          searchLoading: false
        })
      } catch (error) {
        if (this.data.searchKeyword !== requestKeyword) {
          return
        }
        console.warn('搜索地点失败:', error)
        this.setData({
          searchSuggestions: [],
          searchLoading: false
        })
      }
    }, 260)
  },

  async selectSearchSuggestion(e) {
    const index = Number(e.currentTarget.dataset.index)
    const suggestion = this.data.searchSuggestions[index]
    if (!suggestion) return

    const parsed = parsePastedAddress(`${suggestion.name} ${suggestion.address}`)
    parsed.detailAddress = suggestion.name || parsed.detailAddress || suggestion.address

    let latitude = suggestion.latitude
    let longitude = suggestion.longitude
    let reverseGeocodeResult = null

    if (latitude != null && longitude != null) {
      try {
        reverseGeocodeResult = await MapAPI.reverseGeocode(latitude, longitude)
      } catch (error) {
        console.warn('搜索地点逆解析失败:', error)
      }
    } else {
      try {
        const geocodeResult = await MapAPI.geocode(`${suggestion.name}${suggestion.address}`, this.data.formData.city || this.data.formData.province || '')
        latitude = geocodeResult && geocodeResult.latitude
        longitude = geocodeResult && geocodeResult.longitude
        reverseGeocodeResult = geocodeResult
      } catch (error) {
        console.warn('搜索地点 geocode 失败:', error)
      }
    }

    this.applyParsedAddress(parsed, {
      mapTitle: suggestion.name || parsed.detailAddress || this.data.mapTitle,
      mapDetail: reverseGeocodeResult && reverseGeocodeResult.formattedAddress
        ? reverseGeocodeResult.formattedAddress
        : (suggestion.address || this.data.mapDetail),
      latitude,
      longitude,
      mapHint: '地点已同步到地图',
      searchKeyword: suggestion.name || this.data.searchKeyword
    })

    if (reverseGeocodeResult) {
      const province = reverseGeocodeResult.province || this.data.formData.province
      const city = reverseGeocodeResult.city || this.data.formData.city
      const district = reverseGeocodeResult.district || this.data.formData.district
      this.setData({
        'formData.province': province,
        'formData.city': city,
        'formData.district': district,
        region: [province, city, district]
      })
      this.refreshDerivedState()
    }
  },

  async handlePasteRecognition(showToast = true) {
    const parsed = parsePastedAddress(this.data.pasteText)
    if (!parsed.contactPhone && !parsed.detailAddress) {
      if (showToast) {
        wx.showToast({ title: '请先粘贴完整地址', icon: 'none' })
      }
      return
    }

    let geocodeResult = null
    try {
      geocodeResult = await MapAPI.geocode(this.data.pasteText, parsed.city || parsed.province || '')
    } catch (error) {
      console.warn('地址 geocode 失败，回退本地解析:', error)
    }

    this.applyParsedAddress(parsed, {
      mapTitle: parsed.detailAddress || this.data.mapTitle,
      mapDetail: geocodeResult && geocodeResult.formattedAddress
        ? geocodeResult.formattedAddress
        : this.data.mapDetail,
      latitude: geocodeResult && geocodeResult.latitude,
      longitude: geocodeResult && geocodeResult.longitude,
      mapHint: geocodeResult ? '已根据粘贴地址更新地图' : '已识别基础信息，请继续确认地图位置',
      searchKeyword: parsed.detailAddress || this.data.searchKeyword
    })
    if (showToast) {
      wx.showToast({ title: '已识别地址信息', icon: 'success' })
    }
  },

  chooseLocation() {
    if (!wx.chooseLocation) {
      wx.showToast({ title: '当前环境不支持地图选点', icon: 'none' })
      return
    }

    wx.chooseLocation({
      success: async location => {
        const rawText = [location.name, location.address].filter(Boolean).join(' ')
        const parsed = parsePastedAddress(rawText)
        if (!parsed.detailAddress && location.address) {
          parsed.detailAddress = location.address
        }
        let reverseGeocodeResult = null
        try {
          reverseGeocodeResult = await MapAPI.reverseGeocode(location.latitude, location.longitude)
        } catch (error) {
          console.warn('逆地理编码失败，回退原始选点地址:', error)
        }
        this.applyParsedAddress(parsed, {
          mapTitle: location.name || parsed.detailAddress || '已更新地图位置',
          mapDetail: reverseGeocodeResult && reverseGeocodeResult.formattedAddress
            ? reverseGeocodeResult.formattedAddress
            : (location.address || this.data.mapDetail),
          latitude: location.latitude,
          longitude: location.longitude,
          mapHint: '地图位置已更新',
          searchKeyword: location.name || parsed.detailAddress || this.data.searchKeyword
        })
        if (reverseGeocodeResult) {
          this.setData({
            'formData.province': reverseGeocodeResult.province || this.data.formData.province,
            'formData.city': reverseGeocodeResult.city || this.data.formData.city,
            'formData.district': reverseGeocodeResult.district || this.data.formData.district,
            region: [
              reverseGeocodeResult.province || this.data.formData.province,
              reverseGeocodeResult.city || this.data.formData.city,
              reverseGeocodeResult.district || this.data.formData.district
            ]
          })
          this.refreshDerivedState()
        }
      },
      fail: error => {
        if (error && /cancel/i.test(error.errMsg || '')) {
          return
        }
        wx.showToast({ title: '地图定位失败', icon: 'none' })
      }
    })
  },

  async saveAddress() {
    const formData = this.data.formData

    if (!formData.contactName) {
      wx.showToast({ title: '请输入联系人', icon: 'none' })
      return
    }
    if (!formData.contactPhone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' })
      return
    }
    if (!/^1[3-9]\d{9}$/.test(formData.contactPhone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }
    if (!formData.detailAddress) {
      wx.showToast({ title: '请输入详细地址', icon: 'none' })
      return
    }

    try {
      wx.showLoading({ title: '保存中...' })
      let latitude = this.data.mapPreview.latitude
      let longitude = this.data.mapPreview.longitude
      let mapAddress = this.data.mapDetail

      if (this.data.locationDirty || latitude == null || longitude == null) {
        const fullAddress = buildFullAddress(formData)
        try {
          const geocodeResult = await MapAPI.geocode(fullAddress, formData.city || formData.province || '')
          if (geocodeResult && geocodeResult.latitude != null && geocodeResult.longitude != null) {
            latitude = geocodeResult.latitude
            longitude = geocodeResult.longitude
            mapAddress = geocodeResult.formattedAddress || fullAddress
            this.refreshDerivedState({
              mapTitle: formData.detailAddress || this.data.mapTitle,
              mapDetail: mapAddress,
              mapHint: '最新地址已同步到地图',
              mapPreview: buildMapPreviewState({
                latitude,
                longitude,
                title: formData.detailAddress || this.data.mapTitle
              }),
              locationDirty: false
            })
          }
        } catch (error) {
          console.warn('保存前 geocode 失败:', error)
        }

        if (latitude == null || longitude == null) {
          wx.hideLoading()
          wx.showToast({ title: '请先搜索或地图选点确认地址', icon: 'none' })
          return
        }
      }

      const isDefault = formData.isDefault ? 1 : 0
      const payload = {
        id: this.data.addressId,
        contactName: formData.contactName,
        contactPhone: formData.contactPhone,
        province: formData.province,
        city: formData.city,
        district: formData.district,
        detailAddress: formData.detailAddress,
        isDefault,
        longitude,
        latitude,
        businessArea: this.data.regionText,
        doorNo: formData.detailAddress,
        rawText: this.data.pasteText,
        parsedName: formData.contactName,
        parsedPhone: formData.contactPhone,
        mapAddress,
        addressTag: formData.isDefault ? 'default' : 'user_saved'
      }
      if (this.data.addressId) {
        await AddressAPI.update(payload)
      } else {
        await AddressAPI.create(payload)
      }
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        wx.navigateBack()
      }, 1000)
    } catch (error) {
      console.error('保存地址失败:', error)
      wx.showToast({ title: '保存失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  }
})
