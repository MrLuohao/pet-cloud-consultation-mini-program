// pages/index/index.js
Page({
  data: {
    keyword: ""
  },

  // 处理搜索
  handleSearch() {
    const keyword = this.data.keyword.trim();
    if (!keyword) return;
    // 搜索逻辑...
  },

  // 输入变化
  inputChanged(e) {
    this.setData({ keyword: e.detail.value });
  },
  
  // 功能点击处理
  handleFunctionTap(e) {
    const target = e.currentTarget.dataset.target;
    const app = getApp();
    
    // 定义跳转函数
    const jumpFunction = () => {
      switch(target) {
        case 'health':
          wx.navigateTo({ url: '/pages/health/health' });
          break;
        case 'companion':
          wx.navigateTo({ url: '/pages/companion/companion' });
          break;
        case 'training':
          wx.navigateTo({ url: '/pages/course/course' });
          break;
        case 'beauty':
          wx.navigateTo({ url: '/pages/beauty/beauty' });
          break;
        case 'recommend1':
          wx.navigateTo({ url: '/pages/recommend/detail?id=1' });
          break;
        case 'recommend2':
          wx.navigateTo({ url: '/pages/recommend/detail?id=2' });
          break;
      }
    };

    // 检查登录状态
    app.checkLogin(jumpFunction);
  }
});