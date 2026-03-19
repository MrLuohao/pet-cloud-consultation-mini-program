const assert = require('assert');
const path = require('path');

const PAGE_PATH = path.join(__dirname, '../miniprogram/pages/order/detail.js');
const API_MODULE_PATH = path.join(__dirname, '../miniprogram/utils/api.js');

function clearModule(modulePath) {
  delete require.cache[require.resolve(modulePath)];
}

function capturePage(mockedApi) {
  clearModule(PAGE_PATH);
  clearModule(API_MODULE_PATH);

  let pageConfig = null;
  global.getApp = () => ({ globalData: {} });
  global.Page = config => {
    pageConfig = config;
  };
  global.wx = {
    showLoading() {},
    hideLoading() {},
    showToast() {},
    redirectTo() {},
    navigateBack() {},
    showModal() {
      throw new Error('native showModal should not be used');
    }
  };

  require.cache[require.resolve(API_MODULE_PATH)] = {
    exports: mockedApi
  };

  require(PAGE_PATH);

  delete require.cache[require.resolve(API_MODULE_PATH)];
  delete global.Page;

  return pageConfig;
}

function createPageInstance(config) {
  const instance = {
    data: JSON.parse(JSON.stringify(config.data)),
    setData(update) {
      this.data = {
        ...this.data,
        ...update
      };
    }
  };

  Object.keys(config).forEach(key => {
    if (key === 'data') return;
    if (typeof config[key] === 'function') {
      instance[key] = config[key];
    }
  });

  return instance;
}

async function testCancelSheetStateFlow() {
  let canceledOrderId = null;
  const pageConfig = capturePage({
    OrderAPI: {
      getDetail() {
        return Promise.resolve({})
      },
      getTimeline() {
        return Promise.resolve([])
      },
      cancel(orderId) {
        canceledOrderId = orderId;
        return Promise.resolve();
      }
    },
    isLoggedIn() {
      return true;
    },
    navigateToLogin() {}
  });

  const page = createPageInstance(pageConfig);
  page.data.orderId = 88;

  pageConfig.openCancelSheet.call(page);
  assert.strictEqual(page.data.showCancelSheet, true);
  assert.strictEqual(page.data.selectedCancelReason, 'no_need');

  pageConfig.selectCancelReason.call(page, {
    currentTarget: { dataset: { reason: 'change_address' } }
  });
  assert.strictEqual(page.data.selectedCancelReason, 'change_address');

  pageConfig.onCancelNoteInput.call(page, {
    detail: { value: '门牌号要改一下' }
  });
  assert.strictEqual(page.data.cancelNote, '门牌号要改一下');

  await pageConfig.confirmCancelOrder.call(page);
  assert.strictEqual(canceledOrderId, 88);
  assert.strictEqual(page.data.showCancelSheet, false);
}

testCancelSheetStateFlow()
  .then(() => {
    console.log('order-cancel-modal-state tests passed');
  })
  .catch(error => {
    console.error(error);
    process.exit(1);
  });
