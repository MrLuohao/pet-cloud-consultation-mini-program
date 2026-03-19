const assert = require('assert')

function loadConfig({ platform = 'devtools', storage = {} } = {}) {
  global.wx = {
    getSystemInfoSync() {
      return { platform }
    },
    getStorageSync(key) {
      return storage[key]
    },
    setStorageSync(key, value) {
      storage[key] = value
    },
    removeStorageSync(key) {
      delete storage[key]
    }
  }

  const configPath = require.resolve('../miniprogram/utils/config')
  delete require.cache[configPath]

  return {
    config: require(configPath),
    storage
  }
}

function testDevtoolsDefaultsToLoopbackIpv4() {
  const { config } = loadConfig()

  assert.strictEqual(config.getRecommendedApiHost(), '127.0.0.1')
  assert.strictEqual(config.getApiHost(), '127.0.0.1')
  assert.strictEqual(config.getApiBaseUrl(), 'http://127.0.0.1:8117')
  assert.strictEqual(config.getShopApiBaseUrl(), 'http://127.0.0.1:8118')
}

function testLegacyDevtoolsHostMigratesToLoopbackIpv4() {
  const { config, storage } = loadConfig({
    storage: { apiHost: 'localhost' }
  })

  assert.strictEqual(config.getApiHost(), '127.0.0.1')
  assert.strictEqual(storage.apiHost, '127.0.0.1')
}

function testCustomHostStillWins() {
  const { config } = loadConfig({
    storage: { apiHost: '192.168.1.200' }
  })

  assert.strictEqual(config.getApiHost(), '192.168.1.200')
}

testDevtoolsDefaultsToLoopbackIpv4()
testLegacyDevtoolsHostMigratesToLoopbackIpv4()
testCustomHostStillWins()

console.log('config-host tests passed')
