// pages/insurance/index.js
Page({
  data: {
    products: [
      { id: 1, name: '宠物意外险', icon: '🛡️', desc: '意外伤害，全程保障', price: '9.9', unit: '月', tag: '热门' },
      { id: 2, name: '宠物医疗险', icon: '🏥', desc: '就诊费用报销高达80%', price: '29.9', unit: '月', tag: '推荐' },
      { id: 3, name: '宠物重疾险', icon: '💊', desc: '覆盖20种重大疾病', price: '49.9', unit: '月', tag: '' },
      { id: 4, name: '宠物综合险', icon: '🌟', desc: '意外+医疗+重疾全覆盖', price: '79.9', unit: '月', tag: '全面' }
    ]
  },

  onPreRegister() {
    wx.showToast({ title: '预约成功，上线后第一时间通知您', icon: 'none', duration: 2000 })
  },

  onProductTap() {
    wx.showToast({ title: '功能即将上线，敬请期待', icon: 'none' })
  }
})
