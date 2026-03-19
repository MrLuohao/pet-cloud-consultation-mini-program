package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.ArticleCreateDTO;
import com.petcloud.user.domain.dto.CommentCreateDTO;
import com.petcloud.user.domain.vo.ArticleCommentVO;
import com.petcloud.user.domain.vo.ArticleVO;
import com.petcloud.common.core.response.PageVO;

import java.util.List;

/**
 * 文章服务接口
 *
 * @author luohao
 */
public interface ArticleService {

    /**
     * 获取文章列表
     *
     * @return 文章VO列表
     */
    List<ArticleVO> getArticleList();

    /**
     * 根据标签获取文章列表
     */
    List<ArticleVO> getArticleListByTag(String tag);

    /**
     * 获取文章列表（分页）- BE-0.2
     *
     * @param tag      标签过滤（可选）
     * @param page     页码（从1开始）
     * @param pageSize 每页数量
     */
    PageVO<ArticleVO> getArticlePage(String tag, int page, int pageSize);

    /**
     * 新增文章
     *
     * @param createDTO 新增文章DTO
     * @param userId 用户ID
     * @return 文章ID
     */
    Long createArticle(ArticleCreateDTO createDTO, Long userId);

    /**
     * 获取文章详情
     *
     * @param articleId 文章ID
     * @param userId 用户ID（用于统计阅读量）
     * @return 文章VO
     */
    ArticleVO getArticleDetail(Long articleId, Long userId);

    /**
     * 点赞文章
     *
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void likeArticle(Long articleId, Long userId);

    /**
     * 收藏文章
     *
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void collectArticle(Long articleId, Long userId);

    /**
     * 取消点赞
     *
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void unlikeArticle(Long articleId, Long userId);

    /**
     * 取消收藏
     *
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void uncollectArticle(Long articleId, Long userId);

    /**
     * 获取文章评论列表
     *
     * @param articleId 文章ID
     * @param userId 当前用户ID（用于判断是否是自己的评论）
     * @return 评论列表
     */
    List<ArticleCommentVO> getCommentList(Long articleId, Long userId);

    /**
     * 发表评论
     *
     * @param createDTO 评论内容
     * @param userId 用户ID
     * @param userNickname 用户昵称
     * @param userAvatar 用户头像
     * @return 评论ID
     */
    Long createComment(CommentCreateDTO createDTO, Long userId, String userNickname, String userAvatar);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID（只能删除自己的评论）
     */
    void deleteComment(Long commentId, Long userId);
}
