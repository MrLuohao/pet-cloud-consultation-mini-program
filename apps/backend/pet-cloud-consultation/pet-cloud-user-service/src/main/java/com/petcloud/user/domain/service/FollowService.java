package com.petcloud.user.domain.service;

import com.petcloud.common.core.response.PageVO;
import com.petcloud.user.domain.vo.UserProfileVO;

/**
 * 用户关注服务接口
 *
 * @author luohao
 */
public interface FollowService {

    /**
     * 关注用户
     *
     * @param followerId   关注者ID
     * @param followingId  被关注者ID
     */
    void follow(Long followerId, Long followingId);

    /**
     * 取消关注
     *
     * @param followerId   关注者ID
     * @param followingId  被关注者ID
     */
    void unfollow(Long followerId, Long followingId);

    /**
     * 检查是否关注
     *
     * @param followerId   关注者ID
     * @param followingId  被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取粉丝列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param pageSize 每页数量
     * @param currentUserId 当前用户ID
     * @return 粉丝列表
     */
    PageVO<UserProfileVO> getFollowers(Long userId, int page, int pageSize, Long currentUserId);

    /**
     * 获取关注列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param pageSize 每页数量
     * @param currentUserId 当前用户ID
     * @return 关注列表
     */
    PageVO<UserProfileVO> getFollowings(Long userId, int page, int pageSize, Long currentUserId);

    /**
     * 统计粉丝数量
     *
     * @param userId 用户ID
     * @return 粉丝数量
     */
    int countFollowers(Long userId);

    /**
     * 统计关注数量
     *
     * @param userId 用户ID
     * @return 关注数量
     */
    int countFollowings(Long userId);
}
