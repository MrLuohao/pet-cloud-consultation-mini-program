// pages/diagnosis/diagnosis.js - AI诊断页面逻辑
const app = getApp();
const { AIAPI } = require('../../utils/api');

Page({
  data: {
    heroLoaded: false,
    petType: 1, // 1-狗 2-猫 3-其他
    petAge: '',
    symptoms: '',
    symptomTags: [],
    uploadedAssets: [],
    diagnosisResult: null,
    isLoading: false,
    diagnosisTaskId: null,
    // 访客限制相关
    isLoggedIn: false,
    remainingCount: -1, // -1表示已登录用户无限制
    showLimitModal: false
  },

  onLoad() {
    // 页面加载后触发动画
    setTimeout(() => {
      this.setData({ heroLoaded: true });
    }, 100);

    // 检查登录状态和访客剩余次数
    this.checkLoginAndRemainingCount();
  },

  /**
   * 检查登录状态和访客剩余次数
   */
  async checkLoginAndRemainingCount() {
    const isLoggedIn = !!app.globalData?.token;
    this.setData({ isLoggedIn });

    // 访客检查剩余次数
    try {
      const deviceId = isLoggedIn ? null : this.getDeviceId();
      const result = await AIAPI.getDiagnosisEntry(deviceId);
      this.setData({ remainingCount: result.loggedIn ? -1 : result.remainingCount });

      if (!result.loggedIn && result.remainingCount <= 0) {
        this.showLimitReachedModal();
      }
    } catch (e) {
      console.error('获取剩余次数失败:', e);
    }
  },

  /**
   * 获取设备唯一标识
   */
  getDeviceId() {
    let deviceId = wx.getStorageSync('guest_device_id');
    if (!deviceId) {
      // 使用设备信息生成唯一ID
      const deviceInfo = wx.getDeviceInfo();
      const systemInfo = wx.getSystemInfoSync();
      deviceId = `${deviceInfo.brand}_${deviceInfo.model}_${systemInfo.pixelRatio}_${Date.now()}`;
      wx.setStorageSync('guest_device_id', deviceId);
    }
    return deviceId;
  },

  /**
   * 显示限制提示弹窗
   */
  showLimitReachedModal() {
    this.setData({ showLimitModal: true });
    wx.showModal({
      title: '体验次数已用完',
      content: '您的免费诊断次数已用完，登录后可无限次使用',
      confirmText: '去登录',
      cancelText: '稍后再说',
      success: (res) => {
        if (res.confirm) {
          wx.navigateTo({ url: '/pages/login/login' });
        }
      }
    });
  },

  // 解析Markdown内容为结构化数据
  parseMarkdown(content) {
    if (!content) return [];

    const blocks = [];
    const lines = content.split('\n');
    let currentParagraph = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();

      if (!line) {
        if (currentParagraph.length > 0) {
          blocks.push({
            type: 'paragraph',
            content: this.parseInlineMarkdown(currentParagraph.join(' '))
          });
          currentParagraph = [];
        }
        continue;
      }

      // 跳过分隔线
      if (/^[-*_]{3,}$/.test(line)) {
        continue;
      }

      // 标题
      const headingMatch = line.match(/^(#{1,3})\s+(.+)$/);
      if (headingMatch) {
        if (currentParagraph.length > 0) {
          blocks.push({
            type: 'paragraph',
            content: this.parseInlineMarkdown(currentParagraph.join(' '))
          });
          currentParagraph = [];
        }
        blocks.push({
          type: 'heading',
          level: headingMatch[1].length,
          content: this.parseInlineMarkdown(headingMatch[2])
        });
        continue;
      }

      // 有序列表
      const orderedListMatch = line.match(/^(\d+)[.、]\s*(.+)$/);
      if (orderedListMatch) {
        if (currentParagraph.length > 0) {
          blocks.push({
            type: 'paragraph',
            content: this.parseInlineMarkdown(currentParagraph.join(' '))
          });
          currentParagraph = [];
        }
        blocks.push({
          type: 'listItem',
          ordered: true,
          number: orderedListMatch[1],
          content: this.parseInlineMarkdown(orderedListMatch[2])
        });
        continue;
      }

      // 无序列表
      const unorderedListMatch = line.match(/^[-*•]\s+(.+)$/);
      if (unorderedListMatch) {
        if (currentParagraph.length > 0) {
          blocks.push({
            type: 'paragraph',
            content: this.parseInlineMarkdown(currentParagraph.join(' '))
          });
          currentParagraph = [];
        }
        blocks.push({
          type: 'listItem',
          ordered: false,
          content: this.parseInlineMarkdown(unorderedListMatch[1])
        });
        continue;
      }

      currentParagraph.push(line);
    }

    if (currentParagraph.length > 0) {
      blocks.push({
        type: 'paragraph',
        content: this.parseInlineMarkdown(currentParagraph.join(' '))
      });
    }

    return blocks;
  },

  // 解析行内Markdown
  parseInlineMarkdown(text) {
    if (!text) return [];

    const segments = [];
    let remaining = text;

    while (remaining.length > 0) {
      const boldMatch = remaining.match(/\*\*(.+?)\*\*|__(.+?)__/);

      if (boldMatch) {
        const index = boldMatch.index;
        if (index > 0) {
          segments.push({ type: 'text', content: remaining.substring(0, index) });
        }
        segments.push({ type: 'bold', content: boldMatch[1] || boldMatch[2] });
        remaining = remaining.substring(index + boldMatch[0].length);
      } else {
        segments.push({ type: 'text', content: remaining });
        break;
      }
    }

    return segments;
  },

  // 点击卡片滚动到表单
  scrollToForm() {
    wx.createSelectorQuery()
      .select('.form-section')
      .boundingClientRect((rect) => {
        if (rect) {
          wx.pageScrollTo({
            scrollTop: rect.top - 20,
            duration: 300
          });
        }
      })
      .exec();
  },

  // 选择宠物类型
  selectPetType(e) {
    const type = parseInt(e.currentTarget.dataset.type);
    this.setData({ petType: type });
  },

  // 输入宠物年龄
  onPetAgeInput(e) {
    this.setData({
      petAge: e.detail.value
    });
  },

  // 切换症状标签
  toggleSymptom(e) {
    const symptom = e.currentTarget.dataset.symptom;
    let tags = [...this.data.symptomTags];

    const index = tags.indexOf(symptom);
    if (index > -1) {
      tags.splice(index, 1);
    } else {
      tags.push(symptom);
    }

    this.setData({ symptomTags: tags });
    this.updateSymptoms();
  },

  // 输入症状描述
  onSymptomsInput(e) {
    this.setData({
      symptoms: e.detail.value
    });
  },

  // 更新症状描述（包含标签）
  updateSymptoms() {
    const tagsStr = this.data.symptomTags.join('、');
    this.setData({ symptoms: tagsStr });
  },

  // 上传图片
  uploadImage() {
    const remainCount = 6 - this.data.uploadedAssets.length
    if (remainCount <= 0) {
      wx.showToast({ title: '最多上传6张图片', icon: 'none' })
      return
    }

    wx.chooseImage({
      count: remainCount,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        wx.showLoading({ title: '上传中...' });
        const nextAssets = [...this.data.uploadedAssets]
        try {
          for (const tempPath of res.tempFilePaths) {
            const uploaded = await AIAPI.uploadMedia(tempPath, 'diagnosis')
            if (!uploaded.availableForSubmit) {
              throw new Error(uploaded.reason || '内容不符合发布规范，请更换图片后重试')
            }
            nextAssets.push(uploaded)
          }
          this.setData({ uploadedAssets: nextAssets })
          wx.hideLoading();
        } catch (err) {
          wx.hideLoading();
          console.error('图片上传失败:', err);
          wx.showToast({ title: err.message || '图片上传失败', icon: 'none' });
        }
      }
    });
  },

  // 删除图片
  removeImage(e) {
    const index = e?.currentTarget?.dataset?.index
    if (index === undefined) {
      return
    }
    const uploadedAssets = [...this.data.uploadedAssets]
    uploadedAssets.splice(index, 1)
    this.setData({ uploadedAssets })
  },

  // 提交诊断
  async submitDiagnosis() {
    // 验证表单
    if (!this.data.symptoms.trim()) {
      wx.showToast({
        title: '请描述宠物症状',
        icon: 'none'
      });
      return;
    }

    // 访客检查是否还有次数
    if (!this.data.isLoggedIn && this.data.remainingCount <= 0) {
      this.showLimitReachedModal();
      return;
    }

    this.setData({ isLoading: true });

    // 构建诊断数据
    const diagnosisData = {
      petType: this.data.petType,
      petAgeMonths: this.data.petAge ? parseInt(this.data.petAge) : 0,
      symptomTags: this.data.symptomTags,
      symptomDescription: this.data.symptoms,
      mediaAssetIds: this.data.uploadedAssets.map(item => item.assetId)
    };

    // 访客需要传递设备ID
    const deviceId = this.data.isLoggedIn ? null : this.getDeviceId();

    try {
      const submitResult = await AIAPI.submitDiagnosis(diagnosisData, deviceId);

      if (submitResult.limitReached) {
        this.setData({ isLoading: false });
        this.showLimitReachedModal();
        return;
      }

      if (submitResult.remainingCount !== undefined && submitResult.remainingCount >= 0) {
        this.setData({ remainingCount: submitResult.remainingCount });
      }

      this.setData({
        diagnosisTaskId: submitResult.taskId
      });

      const result = await this.pollDiagnosisTask(submitResult.taskId)

      this.setData({
        diagnosisResult: result,
        isLoading: false
      });

      // 标记需要刷新任务列表（诊断成功后后端会自动完成任务）
      if (app.globalData) {
        app.globalData.needRefreshTasks = true;
      }

      // 显示剩余次数提示（访客）
      if (!this.data.isLoggedIn && submitResult.remainingCount !== undefined && submitResult.remainingCount > 0) {
        wx.showToast({
          title: `剩余${submitResult.remainingCount}次免费机会`,
          icon: 'none',
          duration: 2000
        });
      }

      // 滚动到结果区域
      setTimeout(() => {
        wx.createSelectorQuery()
          .select('.result-card')
          .boundingClientRect()
          .exec((res) => {
            if (res[0]) {
              wx.pageScrollTo({
                scrollTop: res[0].top - 120,
                duration: 300
              });
            }
          });
      }, 100);

    } catch (error) {
      console.error('AI诊断失败:', error);
      this.setData({ isLoading: false });

      wx.showModal({
        title: '诊断失败',
        content: '网络连接失败，请稍后再试。如症状紧急，请及时就医！',
        confirmText: '知道了',
        showCancel: false
      });
    }
  },

  // 返回
  goBack() {
    wx.navigateBack();
  },

  async pollDiagnosisTask(taskId, maxAttempts = 6) {
    for (let index = 0; index < maxAttempts; index++) {
      const result = await AIAPI.getDiagnosisTask(taskId)
      if (result.status === 'completed') {
        return result
      }
      await new Promise(resolve => setTimeout(resolve, 1200))
    }
    throw new Error('诊断结果生成超时，请稍后重试')
  }
});
