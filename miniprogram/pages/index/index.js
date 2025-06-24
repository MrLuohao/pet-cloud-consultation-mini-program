Page({
  data: {
    keyword: "", // 存储输入框内容
  },

  // 处理搜索事件
  handleSearch: function() {
    const keyword = this.data.keyword.trim(); // 获取并清理输入值
    if (!keyword) return; // 空值拦截
    
    // 调用API示例（实际URL替换为你的API）
    wx.request({
      url: 'https://your-api.com/search/pets',
      method: 'GET',
      data: {
        q: keyword, // 携带搜索关键词参数
        page: 1,
        size: 10
      },
      success: (res) => {
        console.log('API返回数据:', res.data);
        // 这里处理返回数据（如更新页面列表）
        // this.setData({ results: res.data.items });
      },
      fail: (err) => {
        console.error('API调用失败', err);
        wx.showToast({ title: '搜索失败', icon: 'none' });
      }
    });
  },

  // 实时同步输入值
  inputChanged: function(e) {
    this.setData({ keyword: e.detail.value });
  }
});