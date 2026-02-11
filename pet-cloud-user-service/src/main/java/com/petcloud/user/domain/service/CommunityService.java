package com.petcloud.user.domain.service;

import com.petcloud.common.core.response.PageVO;
import com.petcloud.user.domain.dto.CommunityPostCreateDTO;
import com.petcloud.user.domain.vo.CommunityCommentVO;
import com.petcloud.user.domain.vo.CommunityPostVO;

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
     * 分页获取社区动态列表
     *
     * @param page          页码（从1开始）
     * @param pageSize      每页数量
     * @param currentUserId 当前用户ID（可为null，未登录时不返回isLiked状态）
     * @return 分页动态列表
     */
    PageVO<CommunityPostVO> getPosts(int page, int pageSize, Long currentUserId);

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
     * 发表评论
     *
     * @param postId  动态ID
     * @param userId  用户ID
     * @param content 评论内容
     * @return 评论ID
     */
    Long addComment(Long postId, Long userId, String content);

    /**
     * 获取动态评论列表
     *
     * @param postId 动态ID
     * @return 评论列表
     */
    List<CommunityCommentVO> getComments(Long postId);
}
