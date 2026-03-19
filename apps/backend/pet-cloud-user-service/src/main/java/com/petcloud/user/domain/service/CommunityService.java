package com.petcloud.user.domain.service;

import com.petcloud.common.core.response.PageVO;
import com.petcloud.user.domain.dto.CommunityPostCreateDTO;
import com.petcloud.user.domain.dto.CommunityPostUpdateDTO;
import com.petcloud.user.domain.dto.PostReportDTO;
import com.petcloud.user.domain.vo.CommunityCommentVO;
import com.petcloud.user.domain.vo.CommunityPostVO;
import com.petcloud.user.domain.vo.CommunityTopicVO;

import java.util.List;

/**
 * 社区服务接口
 *
 * @author luohao
 */
public interface CommunityService {

    /**
     * 发布社区动态
     *
     * @param userId 用户ID
     * @param dto    创建DTO
     * @return 动态ID
     */
    Long createPost(Long userId, CommunityPostCreateDTO dto);

    /**
     * 更新社区动态
     *
     * @param postId  动态ID
     * @param userId  用户ID
     * @param dto     更新DTO
     */
    void updatePost(Long postId, Long userId, CommunityPostUpdateDTO dto);

    /**
     * 删除社区动态
     *
     * @param postId 动态ID
     * @param userId 用户ID
     */
    void deletePost(Long postId, Long userId);

    /**
     * 分页获取社区动态列表
     *
     * @param page          页码（从1开始）
     * @param pageSize      每页数量
     * @param currentUserId 当前用户ID（可为null，未登录时不返回isLiked状态）
     * @return 分页动态列表
     */
    PageVO<CommunityPostVO> getPosts(int page, int pageSize, Long currentUserId);

    /**
     * 获取热门动态列表
     *
     * @param page          页码（从1开始）
     * @param pageSize      每页数量
     * @param currentUserId 当前用户ID
     * @return 分页动态列表
     */
    PageVO<CommunityPostVO> getHotPosts(int page, int pageSize, Long currentUserId);

    /**
     * 获取关注用户的动态列表
     *
     * @param userId        当前用户ID
     * @param page          页码
     * @param pageSize      每页数量
     * @return 分页动态列表
     */
    PageVO<CommunityPostVO> getFollowingPosts(Long userId, int page, int pageSize);

    /**
     * 搜索动态
     *
     * @param keyword       搜索关键词
     * @param page          页码
     * @param pageSize      每页数量
     * @param currentUserId 当前用户ID
     * @return 分页动态列表
     */
    PageVO<CommunityPostVO> searchPosts(String keyword, int page, int pageSize, Long currentUserId);

    /**
     * 获取话题下的动态列表
     *
     * @param topicId       话题ID
     * @param page          页码
     * @param pageSize      每页数量
     * @param currentUserId 当前用户ID
     * @return 分页动态列表
     */
    PageVO<CommunityPostVO> getPostsByTopic(Long topicId, int page, int pageSize, Long currentUserId);

    /**
     * 获取用户的动态列表
     *
     * @param userId        用户ID
     * @param page          页码
     * @param pageSize      每页数量
     * @param currentUserId 当前用户ID
     * @return 分页动态列表
     */
    PageVO<CommunityPostVO> getPostsByUser(Long userId, int page, int pageSize, Long currentUserId);

    /**
     * 获取动态详情
     *
     * @param postId        动态ID
     * @param currentUserId 当前用户ID
     * @return 动态详情
     */
    CommunityPostVO getPostDetail(Long postId, Long currentUserId);

    /**
     * 点赞动态
     *
     * @param postId 动态ID
     * @param userId 用户ID
     */
    void likePost(Long postId, Long userId);

    /**
     * 取消点赞
     *
     * @param postId 动态ID
     * @param userId 用户ID
     */
    void unlikePost(Long postId, Long userId);

    /**
     * 收藏动态
     *
     * @param postId 动态ID
     * @param userId 用户ID
     */
    void collectPost(Long postId, Long userId);

    /**
     * 取消收藏
     *
     * @param postId 动态ID
     * @param userId 用户ID
     */
    void uncollectPost(Long postId, Long userId);

    /**
     * 记录分享
     *
     * @param postId    动态ID
     * @param userId    用户ID
     * @param shareType 分享类型
     */
    void sharePost(Long postId, Long userId, String shareType);

    /**
     * 举报动态
     *
     * @param postId 动态ID
     * @param userId 用户ID
     * @param dto    举报DTO
     */
    void reportPost(Long postId, Long userId, PostReportDTO dto);

    /**
     * 置顶动态（管理员）
     *
     * @param postId 动态ID
     */
    void pinPost(Long postId);

    /**
     * 取消置顶（管理员）
     *
     * @param postId 动态ID
     */
    void unpinPost(Long postId);

    /**
     * 设置热门（管理员）
     *
     * @param postId 动态ID
     * @param isHot  是否热门
     */
    void setHot(Long postId, boolean isHot);

    /**
     * 发表评论
     *
     * @param postId  动态ID
     * @param userId  用户ID
     * @param content 评论内容
     * @return 评论ID
     */
    Long addComment(Long postId, Long userId, String content);

    /**
     * 发表评论（含媒体）
     *
     * @param postId    动态ID
     * @param userId    用户ID
     * @param content   评论内容
     * @param mediaUrls 媒体URL列表
     * @param mediaType 媒体类型（image/video）
     * @return 评论ID
     */
    Long addComment(Long postId, Long userId, String content, List<String> mediaUrls, String mediaType);

    /**
     * 回复评论
     *
     * @param postId        动态ID
     * @param userId        用户ID
     * @param content       评论内容
     * @param replyToId     回复目标评论ID
     * @param replyToUserId 回复目标用户ID
     * @return 评论ID
     */
    Long replyComment(Long postId, Long userId, String content, Long replyToId, Long replyToUserId);

    /**
     * 回复评论（含媒体）
     *
     * @param postId        动态ID
     * @param userId        用户ID
     * @param content       评论内容
     * @param replyToId     回复目标评论ID
     * @param replyToUserId 回复目标用户ID
     * @param mediaUrls     媒体URL列表
     * @param mediaType     媒体类型（image/video）
     * @return 评论ID
     */
    Long replyComment(Long postId, Long userId, String content, Long replyToId, Long replyToUserId, List<String> mediaUrls, String mediaType);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 获取动态评论列表
     *
     * @param postId        动态ID
     * @param currentUserId 当前用户ID
     * @return 评论列表
     */
    List<CommunityCommentVO> getComments(Long postId, Long currentUserId);

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    void likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    void unlikeComment(Long commentId, Long userId);

    // ========================= 话题相关 =========================

    /**
     * 获取热门话题列表
     *
     * @return 话题列表
     */
    List<CommunityTopicVO> getHotTopics();

    /**
     * 搜索话题
     *
     * @param keyword 搜索关键词
     * @return 话题列表
     */
    List<CommunityTopicVO> searchTopics(String keyword);

    /**
     * 获取话题详情
     *
     * @param topicId 话题ID
     * @return 话题详情
     */
    CommunityTopicVO getTopicDetail(Long topicId);
}
