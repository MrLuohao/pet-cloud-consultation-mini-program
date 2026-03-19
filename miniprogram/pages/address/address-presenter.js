function cleanText(value) {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

function formatPhoneDisplay(phone) {
  const digits = cleanText(phone).replace(/\D/g, '')
  if (digits.length !== 11) return cleanText(phone)
  return `${digits.slice(0, 3)} ${digits.slice(3, 7)} ${digits.slice(7)}`
}

function formatAddressDisplay(address) {
  return cleanText(address)
    .replace(/(\d+)(号|弄|栋|室|单元|层|幢)/g, '$1 $2')
    .replace(/(号|弄|栋|单元|层|幢)(\d+)/g, '$1 $2')
    .replace(/\s{2,}/g, ' ')
    .trim()
}

function normalizeCoordinate(value, fallback) {
  const number = Number(value)
  return Number.isFinite(number) ? number : fallback
}

function buildMapPreviewState(options = {}) {
  const latitude = normalizeCoordinate(options.latitude, 31.230416)
  const longitude = normalizeCoordinate(options.longitude, 121.473701)
  const title = cleanText(options.title) || '地图选点位置'

  return {
    latitude,
    longitude,
    scale: 16,
    rotate: 0,
    skew: 40,
    enable3d: true,
    enableOverlooking: true,
    showLocation: true,
    markers: [
      {
        id: 1,
        latitude,
        longitude,
        width: 28,
        height: 28,
        callout: {
          content: title,
          display: 'ALWAYS',
          padding: 8,
          borderRadius: 12,
          bgColor: '#FFFDF9',
          color: '#5E5A53',
          textAlign: 'center'
        }
      }
    ]
  }
}

function normalizeAddress(address) {
  if (!address || typeof address !== 'object') return null
  const contactPhone = cleanText(address.contactPhone || address.receiverPhone)
  const fullAddress = cleanText(address.fullAddress) || [
    cleanText(address.province),
    cleanText(address.city),
    cleanText(address.district),
    cleanText(address.detailAddress)
  ].filter(Boolean).join('')
  return {
    ...address,
    contactName: cleanText(address.contactName || address.receiverName),
    contactPhone,
    displayContactPhone: formatPhoneDisplay(contactPhone),
    province: cleanText(address.province),
    city: cleanText(address.city),
    district: cleanText(address.district),
    detailAddress: cleanText(address.detailAddress),
    fullAddress,
    displayFullAddress: formatAddressDisplay(fullAddress),
    displayRegionText: [cleanText(address.province), cleanText(address.district)].filter(Boolean).join(' · ')
  }
}

function buildAddressListState(addressList, activeAddressId = null) {
  const normalized = Array.isArray(addressList)
    ? addressList.map(normalizeAddress).filter(Boolean)
    : []

  const defaultAddress = normalized.find(item => item.isDefault) || null
  const fallbackActive = normalized.find(item => String(item.id) === String(activeAddressId))
    || defaultAddress
    || normalized[0]
    || null

  return {
    addressList: normalized,
    defaultAddress,
    otherAddresses: normalized.filter(item => !defaultAddress || item.id !== defaultAddress.id),
    activeAddress: fallbackActive,
    activeAddressId: fallbackActive ? fallbackActive.id : null
  }
}

function parsePastedAddress(rawText) {
  const text = cleanText(rawText)
  if (!text) {
    return {
      contactName: '',
      contactPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: ''
    }
  }

  const phoneMatch = text.match(/1[3-9]\d{9}/)
  const contactPhone = phoneMatch ? phoneMatch[0] : ''
  const provinceMatch = text.match(/(北京市|天津市|上海市|重庆市|[^省]+省|[^自治区]+自治区|[^特别行政区]+特别行政区)/)
  const districtMatch = text.match(/([^市区县旗]+(?:区|县|旗))/)

  let province = provinceMatch ? provinceMatch[0] : ''
  let city = ''
  let district = districtMatch ? districtMatch[0] : ''

  if (province === '北京市' || province === '天津市' || province === '上海市' || province === '重庆市') {
    city = province
  } else if (province) {
    const cityPart = text.slice(text.indexOf(province) + province.length).match(/([^市]+市)/)
    city = cityPart ? cityPart[0] : ''
  }

  const namePart = contactPhone ? text.slice(0, text.indexOf(contactPhone)).replace(/[，,]/g, ' ') : ''
  const contactName = cleanText(namePart.split(/\s+/).filter(Boolean)[0])

  const regionText = [province, city && city !== province ? city : '', district]
    .filter(Boolean)
    .join('')
  const detailAddress = cleanText(
    regionText && text.includes(regionText)
      ? text.slice(text.indexOf(regionText) + regionText.length)
      : text.replace(contactName, '').replace(contactPhone, '')
  )

  return {
    contactName,
    contactPhone,
    province,
    city: city || province,
    district,
    detailAddress
  }
}

module.exports = {
  buildAddressListState,
  parsePastedAddress,
  formatPhoneDisplay,
  formatAddressDisplay,
  buildMapPreviewState
}
