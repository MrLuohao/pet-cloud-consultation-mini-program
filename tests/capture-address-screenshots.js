const path = require('path')
const fs = require('fs')

const automator = require('/tmp/miniprogram-automator-run/node_modules/miniprogram-automator')

const PROJECT_PATH = path.resolve(__dirname, '..')
const CLI_PATH = '/Applications/wechatwebdevtools.app/Contents/MacOS/cli'
const OUTPUT_DIR = '/tmp/address-ui-verify'

const SAMPLE_ADDRESS_LIST = [
  {
    id: 101,
    contactName: '张宁',
    contactPhone: '13812345678',
    displayContactPhone: '138 1234 5678',
    province: '上海市',
    city: '上海市',
    district: '浦东新区',
    detailAddress: '世纪大道1188号2栋1702',
    fullAddress: '上海市浦东新区世纪大道1188号2栋1702',
    displayFullAddress: '上海市浦东新区世纪大道1188 号 2 栋 1702',
    displayRegionText: '上海市 · 浦东新区',
    isDefault: true
  },
  {
    id: 102,
    contactName: '林月',
    contactPhone: '13666771208',
    displayContactPhone: '136 6677 1208',
    province: '上海市',
    city: '上海市',
    district: '徐汇区',
    detailAddress: '云锦路258号1单元802',
    fullAddress: '上海市徐汇区云锦路258号1单元802',
    displayFullAddress: '上海市徐汇区云锦路258 号 1 单元 802',
    displayRegionText: '上海市 · 徐汇区',
    isDefault: false
  },
  {
    id: 103,
    contactName: '周乔',
    contactPhone: '15088992476',
    displayContactPhone: '150 8899 2476',
    province: '上海市',
    city: '上海市',
    district: '静安区',
    detailAddress: '南京西路1198号3号906',
    fullAddress: '上海市静安区南京西路1198号3号906',
    displayFullAddress: '上海市静安区南京西路1198 号 3 号 906',
    displayRegionText: '上海市 · 静安区',
    isDefault: false
  },
  {
    id: 104,
    contactName: '许乔',
    contactPhone: '13766665555',
    displayContactPhone: '137 6666 5555',
    province: '上海市',
    city: '上海市',
    district: '长宁区',
    detailAddress: '武夷路188号2幢1201',
    fullAddress: '上海市长宁区武夷路188号2幢1201',
    displayFullAddress: '上海市长宁区武夷路188 号 2 幢 1201',
    displayRegionText: '上海市 · 长宁区',
    isDefault: false
  }
]

const SAMPLE_ORDER_ITEMS = [
  {
    productId: '1',
    productName: '冻干鸡肉双拼主粮',
    spec: '成猫配方 · 1.5kg',
    price: '268',
    quantity: 1,
    coverUrl: '',
    fallbackIcon: '/image/icons/shop-food.svg'
  },
  {
    productId: '2',
    productName: '猫薄荷舒缓护理喷雾',
    spec: '日常护理 · 120ml',
    price: '89',
    quantity: 2,
    coverUrl: '',
    fallbackIcon: '/image/icons/shop-care.svg'
  }
]

async function wait(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

async function prepareStorage(miniProgram) {
  await miniProgram.callWxMethod('setStorageSync', 'token', 'ui-verify-token')
  await miniProgram.callWxMethod('setStorageSync', 'userInfo', {
    id: 1,
    nickname: 'UI Verify'
  })
}

async function captureAddressList(miniProgram) {
  const page = await miniProgram.reLaunch('/pages/address/list?from=order')
  await wait(1500)
  await page.callMethod('syncAddressState', SAMPLE_ADDRESS_LIST, 101)
  await page.setData({
    from: 'order',
    defaultAddress: SAMPLE_ADDRESS_LIST[0],
    otherAddresses: SAMPLE_ADDRESS_LIST.slice(1),
    activeAddressId: 102,
    activeAddress: SAMPLE_ADDRESS_LIST[1],
    helperTitle: '默认地址会优先用于下单',
    helperSub: '从订单进入时，也可以直接切换并确认使用',
    confirmButtonText: '确认并使用该地址',
    confirmSubText: '已切换到当前选中的地址'
  })
  await wait(800)
  await miniProgram.screenshot({
    path: path.join(OUTPUT_DIR, 'address-list-actual.png')
  })
}

async function captureAddressEdit(miniProgram) {
  const page = await miniProgram.reLaunch('/pages/address/edit')
  await wait(1500)
  await page.setData({
    isEdit: false,
    addressId: null,
    region: ['上海市', '上海市', '浦东新区'],
    regionText: '上海市 · 上海市 · 浦东新区',
    parseCount: 4,
    pasteText: '张宁 13812345678 上海市浦东新区世纪大道1188号2栋1702',
    mapTitle: '世纪大道 1188 号',
    mapHint: '输入地址后同步更新',
    mapDetail: '',
    formData: {
      contactName: '',
      contactPhone: '',
      province: '上海市',
      city: '上海市',
      district: '浦东新区',
      detailAddress: '',
      isDefault: false
    }
  })
  await wait(800)
  await miniProgram.screenshot({
    path: path.join(OUTPUT_DIR, 'address-edit-actual.png')
  })
}

async function captureCheckoutAddress(miniProgram) {
  const page = await miniProgram.reLaunch('/pages/order/confirm')
  await wait(1500)
  await page.setData({
    address: {
      id: 101,
      contactName: '张宁',
      contactPhone: '13812345678',
      contactPhoneDisplay: '138 1234 5678',
      fullAddress: '上海市浦东新区世纪大道1188号2栋1702'
    },
    orderItems: SAMPLE_ORDER_ITEMS,
    totalAmount: '446',
    freight: '12',
    couponDiscount: '24',
    payAmount: '434',
    deliveryText: '顺丰冷链',
    orderHint: '订单将于提交后创建，请在 30 分钟内完成支付。'
  })
  await wait(800)
  await miniProgram.screenshot({
    path: path.join(OUTPUT_DIR, 'checkout-address-actual.png')
  })
}

;(async () => {
  let miniProgram
  try {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true })
    miniProgram = await automator.launch({
      cliPath: CLI_PATH,
      projectPath: PROJECT_PATH,
      trustProject: true
    })
    await prepareStorage(miniProgram)
    await captureAddressList(miniProgram)
    await captureAddressEdit(miniProgram)
    await captureCheckoutAddress(miniProgram)
    console.log(JSON.stringify({
      ok: true,
      outputDir: OUTPUT_DIR,
      files: [
        path.join(OUTPUT_DIR, 'address-list-actual.png'),
        path.join(OUTPUT_DIR, 'address-edit-actual.png'),
        path.join(OUTPUT_DIR, 'checkout-address-actual.png')
      ]
    }))
  } catch (error) {
    console.error(error && error.stack ? error.stack : String(error))
    process.exitCode = 1
  } finally {
    if (miniProgram) {
      try {
        await miniProgram.close()
      } catch (error) {}
    }
  }
})()
