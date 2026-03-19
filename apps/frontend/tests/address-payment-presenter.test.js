const assert = require('assert')

const {
  buildAddressListState,
  parsePastedAddress,
  formatPhoneDisplay,
  formatAddressDisplay,
  buildMapPreviewState
} = require('../miniprogram/pages/address/address-presenter')
const {
  PAYMENT_PASSWORD_KEYS,
  applyPasswordKey,
  getPaymentMethodLabel,
  buildVerificationMethodText
} = require('../miniprogram/pages/order/payment-presenter')

function testAddressListStateSeparatesDefaultAndOthers() {
  const state = buildAddressListState([
    {
      id: 101,
      contactName: '张宁',
      contactPhone: '13812345678',
      province: '上海市',
      city: '上海市',
      district: '浦东新区',
      detailAddress: '世纪大道1188号2栋1702',
      fullAddress: '上海市浦东新区世纪大道1188号2栋1702',
      isDefault: true
    },
    {
      id: 102,
      contactName: '林月',
      contactPhone: '13666771208',
      province: '上海市',
      city: '上海市',
      district: '徐汇区',
      detailAddress: '云锦路258号1单元802',
      fullAddress: '上海市徐汇区云锦路258号1单元802',
      isDefault: false
    }
  ])

  assert.strictEqual(state.defaultAddress.id, 101)
  assert.strictEqual(state.otherAddresses.length, 1)
  assert.strictEqual(state.otherAddresses[0].id, 102)
  assert.strictEqual(state.activeAddress.id, 101)
}

function testParsePastedAddressExtractsStructuredFields() {
  const parsed = parsePastedAddress('张宁 13812345678 上海市浦东新区世纪大道1188号2栋1702')

  assert.strictEqual(parsed.contactName, '张宁')
  assert.strictEqual(parsed.contactPhone, '13812345678')
  assert.strictEqual(parsed.province, '上海市')
  assert.strictEqual(parsed.city, '上海市')
  assert.strictEqual(parsed.district, '浦东新区')
  assert.strictEqual(parsed.detailAddress, '世纪大道1188号2栋1702')
}

function testPaymentPasswordKeysMatchPenLayout() {
  assert.deepStrictEqual(
    PAYMENT_PASSWORD_KEYS.map(row => row.map(item => item.label)),
    [
      ['1', '2', '3'],
      ['4', '5', '6'],
      ['7', '8', '9'],
      ['Face ID', '0', '删除']
    ]
  )
}

function testPaymentPasswordInputCapsAtSixDigitsAndSupportsDelete() {
  let password = ''
  ;['1', '2', '3', '4', '5', '6', '7'].forEach(key => {
    password = applyPasswordKey(password, key)
  })
  assert.strictEqual(password, '123456')

  password = applyPasswordKey(password, '删除')
  assert.strictEqual(password, '12345')
}

function testPaymentMethodTextStaysOnSingleSpecLine() {
  assert.strictEqual(getPaymentMethodLabel('wechat'), '微信支付')
  assert.strictEqual(buildVerificationMethodText('wechat', 'face'), '微信支付 · Face ID 验证')
  assert.strictEqual(buildVerificationMethodText('wechat', 'password'), '微信支付 · 支付密码验证')
}

function testDisplayFormattingMatchesPenReadingRhythm() {
  assert.strictEqual(formatPhoneDisplay('13812345678'), '138 1234 5678')
  assert.strictEqual(
    formatAddressDisplay('上海市浦东新区世纪大道1188号2栋1702'),
    '上海市浦东新区世纪大道1188 号 2 栋 1702'
  )
}

function testBuildMapPreviewStateUses3dMapDefaults() {
  const preview = buildMapPreviewState({
    latitude: 31.230416,
    longitude: 121.473701,
    title: '世纪大道 1188 号'
  })

  assert.strictEqual(preview.latitude, 31.230416)
  assert.strictEqual(preview.longitude, 121.473701)
  assert.strictEqual(preview.scale, 16)
  assert.strictEqual(preview.skew, 40)
  assert.strictEqual(preview.enable3d, true)
  assert.strictEqual(preview.enableOverlooking, true)
  assert.strictEqual(preview.markers.length, 1)
  assert.strictEqual(preview.markers[0].callout.content, '世纪大道 1188 号')
}

testAddressListStateSeparatesDefaultAndOthers()
testParsePastedAddressExtractsStructuredFields()
testPaymentPasswordKeysMatchPenLayout()
testPaymentPasswordInputCapsAtSixDigitsAndSupportsDelete()
testPaymentMethodTextStaysOnSingleSpecLine()
testDisplayFormattingMatchesPenReadingRhythm()
testBuildMapPreviewStateUses3dMapDefaults()

console.log('address-payment-presenter tests passed')
