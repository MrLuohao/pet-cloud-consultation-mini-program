// pages/recommend/detail.js - 推荐详情逻辑
const app = getApp();
const { ArticleAPI, isLoggedIn, navigateToLogin, parseDate } = require('../../utils/api');

Page({
  data: {
    article: null,
    isLoading: false,
    loadError: false,
    // 评论相关
    comments: [],
    commentText: '',
    showCommentInput: false,
    replyTo: null, // 回复目标 { commentId, userId, nickname }
    isSubmitting: false
  },

  onLoad(options) {
    const id = options.id;
    if (!id) {
      wx.showToast({
        title: '文章不存在',
        icon: 'none'
      });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }
    this.loadArticle(id);
  },

  // 加载文章详情
  async loadArticle(id) {
    this.setData({ isLoading: true, loadError: false });

    try {
      const article = await ArticleAPI.getDetail(id);
      if (!article) {
        throw new Error('文章不存在');
      }
      const formattedArticle = {
        id: article.id,
        title: article.title,
        tag: article.tag || '',
        date: this.formatDate(article.publishTime),
        views: this.formatNumber(article.viewCount || 0),
        coverUrl: article.coverUrl || '',
        emoji: this.getArticleEmoji(article.tag),
        color: this.getArticleColor(article.tag),
        liked: article.isLiked || false,
        collected: article.isCollected || false,
        likes: article.likeCount || 0,
        comments: article.commentCount || 0,
        collections: article.collectCount || 0,
        content: article.content || article.summary || '暂无内容'
      };
      this.setData({ article: formattedArticle, isLoading: false });
      // 加载评论列表
      this.loadComments(id);
    } catch (error) {
      console.error('加载文章失败:', error);
      this.setData({ isLoading: false, loadError: true });
      wx.showToast({
        title: '文章加载失败',
        icon: 'none'
      });
    }
  },

  // 格式化日期
  formatDate(timestamp) {
    if (!timestamp) return '';
    const date = parseDate(timestamp);
    return date.getFullYear() + '-' +
           (date.getMonth() + 1).toString().padStart(2, '0') + '-' +
           date.getDate().toString().padStart(2, '0');
  },

  // 格式化数字
  formatNumber(num) {
    if (num >= 10000) {
      return (num / 10000).toFixed(1) + '万';
    } else if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'k';
    }
    return num.toString();
  },

  // 根据标签获取emoji
  getArticleEmoji(tag) {
    const emojiMap = {
      '护理': '🌸',
      '活动': '🎁',
      '健康': '🏥',
      '训练': '🎓',
      '营养': '🍖'
    };
    return emojiMap[tag] || '📄';
  },

  // 根据标签获取颜色
  getArticleColor(tag) {
    const colorMap = {
      '护理': 'pink',
      '活动': 'blue',
      '健康': 'green',
      '训练': 'yellow',
      '营养': 'orange'
    };
    return colorMap[tag] || 'gray';
  },

  // 返回
  onBack() {
    wx.navigateBack();
  },

  // 分享
  onShare() {
    wx.showActionSheet({
      itemList: ['分享给微信好友', '生成海报'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 实际项目中调用分享API
        } else if (res.tapIndex === 1) {
          // 生成海报
        }
      }
    });
  },

  // 点赞（需要登录）
  async onLike() {
    // 未登录时跳转登录页
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const article = this.data.article;
    if (!article) return;

    try {
      if (article.liked) {
        // 已点赞，取消点赞
        await ArticleAPI.unlike(article.id);
        this.setData({
          'article.liked': false,
          'article.likes': Math.max(0, article.likes - 1)
        });
        wx.showToast({
          title: '已取消点赞',
          icon: 'none'
        });
      } else {
        // 未点赞，进行点赞
        await ArticleAPI.like(article.id);
        this.setData({
          'article.liked': true,
          'article.likes': article.likes + 1
        });
        wx.showToast({
          title: '点赞成功',
          icon: 'success'
        });
      }
    } catch (error) {
      const msg = error?.message || (typeof error === 'string' ? error : '');
      if (msg.includes('已经点赞')) {
        this.setData({ 'article.liked': true });
        wx.showToast({ title: '已点赞', icon: 'none' });
      } else {
        console.error('点赞操作失败:', error);
        wx.showToast({ title: '操作失败，请重试', icon: 'none' });
      }
    }
  },

  // 评论（需要登录）
  onComment() {
    // 未登录时跳转登录页
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    this.setData({
      showCommentInput: true,
      replyTo: null
    });
  },

  // 加载评论列表
  async loadComments(articleId) {
    try {
      const comments = await ArticleAPI.getComments(articleId);
      const currentUserId = app.globalData?.userId;
      const currentUserInfo = app.globalData?.userInfo || {};
      // 格式化评论数据
      const formattedComments = (comments || []).map(comment => ({
        ...this.patchCurrentUserProfile(comment, currentUserId, currentUserInfo),
        createTimeStr: this.formatCommentTime(comment.createTime),
        replies: (comment.replies || []).map(reply => ({
          ...this.patchCurrentUserProfile(reply, currentUserId, currentUserInfo),
          createTimeStr: this.formatCommentTime(reply.createTime)
        }))
      }));
      this.setData({ comments: formattedComments });
    } catch (error) {
      console.error('加载评论失败:', error);
    }
  },

  patchCurrentUserProfile(comment, currentUserId, currentUserInfo) {
    if (!comment || !currentUserId || comment.userId !== currentUserId) {
      return comment;
    }
    return {
      ...comment,
      userNickname: currentUserInfo.nickname || comment.userNickname,
      userAvatar: currentUserInfo.avatarUrl || comment.userAvatar
    };
  },

  // 格式化评论时间
  formatCommentTime(timestamp) {
    if (!timestamp) return '';
    const date = parseDate(timestamp);
    const now = new Date();
    const diff = now - date;

    if (diff < 60000) { // 1分钟内
      return '刚刚';
    } else if (diff < 3600000) { // 1小时内
      return Math.floor(diff / 60000) + '分钟前';
    } else if (diff < 86400000) { // 24小时内
      return Math.floor(diff / 3600000) + '小时前';
    } else if (diff < 604800000) { // 7天内
      return Math.floor(diff / 86400000) + '天前';
    } else {
      return date.getMonth() + 1 + '月' + date.getDate() + '日';
    }
  },

  // 输入评论内容
  onCommentInput(e) {
    this.setData({ commentText: e.detail.value });
  },

  // 取消评论
  onCancelComment() {
    this.setData({
      showCommentInput: false,
      commentText: '',
      replyTo: null
    });
  },

  // 提交评论
  async onSubmitComment() {
    const { commentText, replyTo, article, isSubmitting } = this.data;

    if (isSubmitting) return;

    if (!commentText.trim()) {
      wx.showToast({
        title: '请输入评论内容',
        icon: 'none'
      });
      return;
    }

    this.setData({ isSubmitting: true });

    try {
      await ArticleAPI.createComment(
        article.id,
        commentText.trim(),
        replyTo ? replyTo.commentId : null,
        replyTo ? replyTo.userId : null
      );

      wx.showToast({
        title: '评论成功',
        icon: 'success'
      });

      // 清空输入，关闭输入框
      this.setData({
        commentText: '',
        showCommentInput: false,
        replyTo: null,
        'article.comments': article.comments + 1
      });

      // 重新加载评论列表
      this.loadComments(article.id);
    } catch (error) {
      console.error('发表评论失败:', error);
    } finally {
      this.setData({ isSubmitting: false });
    }
  },

  // 回复评论（需要登录）
  onReplyComment(e) {
    // 未登录时跳转登录页
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }
    const { commentId, userId, nickname } = e.currentTarget.dataset;
    this.setData({
      showCommentInput: true,
      replyTo: { commentId, userId, nickname }
    });
  },

  // 删除评论
  onDeleteComment(e) {
    const { commentId } = e.currentTarget.dataset;
    const article = this.data.article;

    wx.showModal({
      title: '提示',
      content: '确定要删除这条评论吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await ArticleAPI.deleteComment(commentId);
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });
            this.setData({
              'article.comments': Math.max(0, article.comments - 1)
            });
            // 重新加载评论列表
            this.loadComments(article.id);
          } catch (error) {
            console.error('删除评论失败:', error);
          }
        }
      }
    });
  },

  // 收藏（需要登录）
  async onCollect() {
    // 未登录时跳转登录页
    if (!isLoggedIn()) {
      navigateToLogin()
      return
    }

    const article = this.data.article;
    if (!article) return;

    try {
      if (article.collected) {
        // 已收藏，取消收藏
        await ArticleAPI.uncollect(article.id);
        this.setData({
          'article.collected': false,
          'article.collections': Math.max(0, article.collections - 1)
        });
        wx.showToast({
          title: '已取消收藏',
          icon: 'none'
        });
      } else {
        // 未收藏，进行收藏
        await ArticleAPI.collect(article.id);
        this.setData({
          'article.collected': true,
          'article.collections': article.collections + 1
        });
        wx.showToast({
          title: '收藏成功',
          icon: 'success'
        });
      }
    } catch (error) {
      const msg = error?.message || (typeof error === 'string' ? error : '');
      if (msg.includes('已经收藏')) {
        this.setData({ 'article.collected': true });
        wx.showToast({ title: '已收藏', icon: 'none' });
      } else {
        wx.showToast({ title: '操作失败，请重试', icon: 'none' });
      }
    }
  }
});
