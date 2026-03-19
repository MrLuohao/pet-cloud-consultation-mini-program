const assert = require('assert');
const path = require('path');

const PAGE_PATH = path.join(__dirname, '../miniprogram/pages/order/review.js');
const API_MODULE_PATH = path.join(__dirname, '../miniprogram/utils/api.js');

function clearModule(modulePath) {
  delete require.cache[require.resolve(modulePath)];
}

function capturePage(mockedApi) {
  clearModule(PAGE_PATH);
  clearModule(API_MODULE_PATH);

  let pageConfig = null;
  global.Page = config => {
    pageConfig = config;
  };
  global.wx = {
    showToast() {},
    showLoading() {},
    hideLoading() {},
    navigateBack() {}
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

async function testOrderReviewSupportsMultipleItemsAndSubmitsSequentially() {
  const submissions = [];
  const pageConfig = capturePage({
    ProductAPI: {
      createReview(orderItemId, productId, rating, content, images) {
        submissions.push({ orderItemId, productId, rating, content, images });
        return Promise.resolve();
      }
    },
    AIAPI: {
      uploadMedia() {
        return Promise.resolve({ url: 'https://cdn.example.com/review-1.png' });
      }
    }
  });

  const page = createPageInstance(pageConfig);
  const itemsData = encodeURIComponent(
    JSON.stringify([
      {
        orderItemId: 11,
        productId: 101,
        productName: '冻干鸡肉双拼主粮',
        coverUrl: 'https://cdn.example.com/a.png',
        price: 268,
        quantity: 1
      },
      {
        orderItemId: 12,
        productId: 102,
        productName: '猫薄荷舒缓护理喷雾',
        coverUrl: 'https://cdn.example.com/b.png',
        price: 89,
        quantity: 2
      }
    ])
  );

  pageConfig.onLoad.call(page, { orderId: '88', itemsData });
  assert.strictEqual(page.data.reviewItems.length, 2);

  pageConfig.setRating.call(page, { currentTarget: { dataset: { index: 0, rating: 4 } } });
  pageConfig.onContentInput.call(page, { currentTarget: { dataset: { index: 0 } }, detail: { value: '冻干颗粒适口性不错，猫咪接受度很高。' } });
  pageConfig.onContentInput.call(page, { currentTarget: { dataset: { index: 1 } }, detail: { value: '喷头细腻，外出前补喷很方便。' } });
  page.data.reviewItems[0].images = ['https://cdn.example.com/review-1.png'];

  await pageConfig.submitReview.call(page);

  assert.strictEqual(submissions.length, 2);
  assert.strictEqual(submissions[0].orderItemId, 11);
  assert.strictEqual(submissions[0].rating, 4);
  assert.strictEqual(submissions[1].orderItemId, 12);
  assert.strictEqual(submissions[1].rating, 5);
}

testOrderReviewSupportsMultipleItemsAndSubmitsSequentially()
  .then(() => {
    console.log('order-review-state tests passed');
  })
  .catch(error => {
    console.error(error);
    process.exit(1);
  });
