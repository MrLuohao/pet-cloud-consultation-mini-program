package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.response.PageVO;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.entity.UserFollow;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.service.FollowService;
import com.petcloud.user.domain.vo.UserProfileVO;
import com.petcloud.user.infrastructure.persistence.mapper.UserFollowMapper;
import com.petcloud.user.infrastructure.persistence.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户关注服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final UserFollowMapper userFollowMapper;
    private final WxUserMapper wxUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followingId) {
        // 不能关注自己
        if (followerId.equals(followingId)) {
            throw new BusinessException(UserRespType.FOLLOW_SELF_FORBIDDEN);
        }

        // 检查被关注用户是否存在
        WxUser targetUser = wxUserMapper.selectById(followingId);
        if (targetUser == null) {
            throw new BusinessException(UserRespType.FOLLOW_TARGET_USER_NOT_FOUND);
        }

        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        Long existCount = userFollowMapper.selectCount(queryWrapper);
        if (existCount != null && existCount > 0) {
            log.info("用户已关注该用户，忽略重复请求，followerId: {}, followingId: {}", followerId, followingId);
            return;
        }

        // 添加关注记录
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        userFollowMapper.insert(follow);

        log.info("用户关注成功，followerId: {}, followingId: {}", followerId, followingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followingId) {
        // 物理删除关注记录
        int deleted = userFollowMapper.physicalDelete(followerId, followingId);
        log.info("用户取消关注，followerId: {}, followingId: {}, deleted: {}", followerId, followingId, deleted);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId);
        Long count = userFollowMapper.selectCount(queryWrapper);
        return count != null && count > 0;
    }

    @Override
    public PageVO<UserProfileVO> getFollowers(Long userId, int page, int pageSize, Long currentUserId) {
        // 查询粉丝ID列表
        IPage<UserFollow> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowingId, userId)
                .orderByDesc(UserFollow::getCreateTime);

        IPage<UserFollow> result = userFollowMapper.selectPage(pageObj, queryWrapper);
        List<UserFollow> follows = result.getRecords();

        if (follows.isEmpty()) {
            return PageVO.of(Collections.emptyList(), 0L, page, pageSize);
        }

        // 获取粉丝用户ID列表
        Set<Long> followerIds = follows.stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, WxUser> userMap = wxUserMapper.selectBatchIds(followerIds).stream()
                .collect(Collectors.toMap(WxUser::getId, u -> u, (a, b) -> a));

        // 查询当前用户是否关注了这些粉丝
        Set<Long> followedIds = new HashSet<>();
        if (currentUserId != null && !followerIds.isEmpty()) {
            LambdaQueryWrapper<UserFollow> followQuery = new LambdaQueryWrapper<>();
            followQuery.eq(UserFollow::getFollowerId, currentUserId)
                    .in(UserFollow::getFollowingId, followerIds);
            List<UserFollow> existingFollows = userFollowMapper.selectList(followQuery);
            if (existingFollows != null) {
                existingFollows.forEach(f -> followedIds.add(f.getFollowingId()));
            }
        }

        List<UserProfileVO> voList = follows.stream()
                .map(follow -> convertToUserProfileVO(
                        userMap.get(follow.getFollowerId()),
                        followedIds.contains(follow.getFollowerId()),
                        currentUserId != null && follow.getFollowerId().equals(currentUserId)
                ))
                .collect(Collectors.toList());

        return PageVO.of(voList, result.getTotal(), page, pageSize);
    }

    @Override
    public PageVO<UserProfileVO> getFollowings(Long userId, int page, int pageSize, Long currentUserId) {
        // 查询关注ID列表
        IPage<UserFollow> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, userId)
                .orderByDesc(UserFollow::getCreateTime);

        IPage<UserFollow> result = userFollowMapper.selectPage(pageObj, queryWrapper);
        List<UserFollow> follows = result.getRecords();

        if (follows.isEmpty()) {
            return PageVO.of(Collections.emptyList(), 0L, page, pageSize);
        }

        // 获取关注的用户ID列表
        Set<Long> followingIds = follows.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, WxUser> userMap = wxUserMapper.selectBatchIds(followingIds).stream()
                .collect(Collectors.toMap(WxUser::getId, u -> u, (a, b) -> a));

        List<UserProfileVO> voList = follows.stream()
                .map(follow -> convertToUserProfileVO(
                        userMap.get(follow.getFollowingId()),
                        true, // 关注列表中的都是已关注的
                        currentUserId != null && follow.getFollowingId().equals(currentUserId)
                ))
                .collect(Collectors.toList());

        return PageVO.of(voList, result.getTotal(), page, pageSize);
    }

    @Override
    public int countFollowers(Long userId) {
        return userFollowMapper.countFollowers(userId);
    }

    @Override
    public int countFollowings(Long userId) {
        return userFollowMapper.countFollowings(userId);
    }

    // ========================= 私有辅助方法 =========================

    private UserProfileVO convertToUserProfileVO(WxUser user, boolean isFollowed, boolean isSelf) {
        if (user == null) {
            return UserProfileVO.builder()
                    .isFollowed(isFollowed)
                    .isSelf(isSelf)
                    .postCount(0)
                    .followingCount(0)
                    .followerCount(0)
                    .build();
        }

        return UserProfileVO.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .isVip(user.getIsVip() != null && user.getIsVip() == 1)
                .vipLevel(user.getVipLevel())
                .isFollowed(isFollowed)
                .isSelf(isSelf)
                .postCount(0)  // 需要额外查询，这里暂时返回0
                .followingCount(0)
                .followerCount(0)
                .build();
    }
}
