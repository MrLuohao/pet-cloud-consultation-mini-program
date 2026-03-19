// pages/chat/chat.js - AI聊天页面逻辑
const app = getApp();
const { AIAPI, ConversationAPI, parseDate } = require('../../utils/api');

Page({
  data: {
    messages: [],
    inputText: '',
    isLoading: false,
    modelType: 'qwen', // qwen | deepseek
    toView: '',
    userInfo: {},

    // 会话相关
    conversationId: null,
    isLoadingHistory: false
  },

  onLoad(options) {
    // 获取用户信息
    this.setData({
      userInfo: app.globalData.userInfo || {}
    });

    // 获取会话ID参数（注意：雪花ID是大数字，不能用parseInt，保持字符串）
    if (options.conversationId) {
      this.setData({ conversationId: options.conversationId });
      this.loadChatHistory();
    }
  },

  onShow() {
    // 页面显示时标记已读（静默处理错误）
    if (this.data.conversationId && app.globalData.token) {
      this.markConversationRead();
    }
  },

  // 加载聊天历史
  async loadChatHistory() {
    if (!this.data.conversationId || !app.globalData.token) return;

    this.setData({ isLoadingHistory: true });

    try {
      const history = await ConversationAPI.getHistory(this.data.conversationId);

      // 转换历史记录格式
      const messages = (history || []).map(msg => ({
        id: msg.id,
        role: msg.role === 'user' ? 'user' : 'ai',
        content: msg.content,
        parsedContent: msg.role === 'assistant' ? this.parseMarkdown(msg.content) : null,
        time: this.formatTimeFromString(msg.createTime)
      }));

      this.setData({
        messages,
        isLoadingHistory: false
      });

      // 滚动到底部，延迟确保DOM渲染完成
      setTimeout(() => this.scrollToBottom(), 150);

    } catch (error) {
      // 静默处理：会话可能不存在或属于其他用户，不影响新聊天
      console.log('加载聊天历史:', error.message || '暂无历史记录');
      this.setData({ isLoadingHistory: false });
    }
  },

  // 标记会话已读
  async markConversationRead() {
    try {
      await ConversationAPI.markAsRead(this.data.conversationId);
    } catch (error) {
      // 静默处理
      console.log('标记已读:', error.message || '操作失败');
    }
  },

  // 选择模型
  selectModel(e) {
    const model = e.currentTarget.dataset.model;
    this.setData({ modelType: model });
  },

  // 输入变化
  onInput(e) {
    this.setData({
      inputText: e.detail.value
    });
  },

  // 发送快捷消息
  sendQuickMessage(e) {
    const message = e.currentTarget.dataset.message;
    this.setData({ inputText: message });
    this.sendMessage();
  },

  // 跳转到AI诊断页面
  goToDiagnosis() {
    wx.navigateTo({
      url: '/pages/diagnosis/diagnosis'
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
        // 空行，结束当前段落
        if (currentParagraph.length > 0) {
          blocks.push({
            type: 'paragraph',
            content: this.parseInlineMarkdown(currentParagraph.join(' '))
          });
          currentParagraph = [];
        }
        continue;
      }

      // 跳过分隔线 --- *** ___
      if (/^[-*_]{3,}$/.test(line)) {
        continue;
      }

      // 检查是否是标题
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

      // 检查是否是有序列表项（1. 2. 等）
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

      // 检查是否是无序列表项（- * •）
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

      // 普通文本行
      currentParagraph.push(line);
    }

    // 处理剩余的段落
    if (currentParagraph.length > 0) {
      blocks.push({
        type: 'paragraph',
        content: this.parseInlineMarkdown(currentParagraph.join(' '))
      });
    }

    return blocks;
  },

  // 解析行内Markdown（加粗、斜体等）
  parseInlineMarkdown(text) {
    if (!text) return [];

    const segments = [];
    let remaining = text;

    while (remaining.length > 0) {
      // 匹配加粗 **text** 或 __text__
      const boldMatch = remaining.match(/\*\*(.+?)\*\*|__(.+?)__/);

      if (boldMatch) {
        const index = boldMatch.index;
        // 添加匹配前的普通文本
        if (index > 0) {
          segments.push({ type: 'text', content: remaining.substring(0, index) });
        }
        // 添加加粗文本
        segments.push({ type: 'bold', content: boldMatch[1] || boldMatch[2] });
        remaining = remaining.substring(index + boldMatch[0].length);
      } else {
        // 没有更多匹配，添加剩余文本
        segments.push({ type: 'text', content: remaining });
        break;
      }
    }

    return segments;
  },

  // 发送消息
  async sendMessage() {
    const content = this.data.inputText.trim();
    if (!content || this.data.isLoading) return;

    // 添加用户消息
    const userMessage = {
      id: Date.now(),
      role: 'user',
      content: content,
      time: this.formatTime(new Date())
    };

    this.setData({
      messages: [...this.data.messages, userMessage],
      inputText: '',
      isLoading: true,
      toView: `msg-${userMessage.id}`
    });

    // 滚动到底部
    this.scrollToBottom();

    try {
      let response = '';

      // 如果有会话ID，使用带会话的API
      if (this.data.conversationId && app.globalData.token) {
        response = await AIAPI.sendWithConversation(
          this.data.conversationId,
          content,
          this.data.modelType
        );
      } else {
        // 使用原有API
        if (this.data.modelType === 'qwen') {
          response = await AIAPI.chatQwenMax(content);
        } else {
          response = await AIAPI.chatDeepSeek(content);
        }
      }

      // 解析Markdown内容
      const parsedContent = this.parseMarkdown(response || '抱歉，我暂时无法回答这个问题。');

      // 添加AI回复
      const aiMessage = {
        id: Date.now() + 1,
        role: 'ai',
        content: response || '抱歉，我暂时无法回答这个问题。',
        parsedContent: parsedContent,
        time: this.formatTime(new Date())
      };

      this.setData({
        messages: [...this.data.messages, aiMessage],
        isLoading: false
      });

    } catch (error) {
      console.error('AI接口调用失败:', error);
      this.setData({
        isLoading: false
      });

      // 添加错误提示消息
      const errorContent = '抱歉，网络连接出现问题，请稍后再试。';
      const errorMessage = {
        id: Date.now() + 1,
        role: 'ai',
        content: errorContent,
        parsedContent: this.parseMarkdown(errorContent),
        time: this.formatTime(new Date())
      };

      this.setData({
        messages: [...this.data.messages, errorMessage]
      });
    }

    this.scrollToBottom();
  },

  // 滚动到底部
  scrollToBottom() {
    // 先重置为空，再设置目标值，确保每次都能触发滚动
    this.setData({ toView: '' });
    setTimeout(() => {
      this.setData({ toView: 'chat-bottom' });
    }, 50);
  },

  // 滚动到顶部
  scrollToTop() {
    // 先重置为空，再设置目标值，确保每次都能触发滚动
    this.setData({ toView: '' });
    setTimeout(() => {
      this.setData({ toView: 'chat-top' });
    }, 50);
  },

  // 滚动到顶部触发
  onScrollToUpper() {
    console.log('已滚动到顶部');
  },

  // 滚动到底部触发
  onScrollToLower() {
    console.log('已滚动到底部');
  },

  // 格式化时间
  formatTime(date) {
    const hour = date.getHours().toString().padStart(2, '0');
    const minute = date.getMinutes().toString().padStart(2, '0');
    return `${hour}:${minute}`;
  },

  // 从字符串格式化时间
  formatTimeFromString(timeStr) {
    if (!timeStr) return '';
    try {
      const date = parseDate(timeStr);
      return this.formatTime(date);
    } catch {
      return '';
    }
  },

  // 返回
  goBack() {
    wx.navigateBack();
  }
});
