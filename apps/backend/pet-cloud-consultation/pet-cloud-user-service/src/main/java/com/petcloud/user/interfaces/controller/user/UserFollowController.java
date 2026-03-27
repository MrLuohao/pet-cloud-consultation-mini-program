package com.petcloud.user.interfaces.controller.user;

import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.service.FollowService;
import com.petcloud.user.domain.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户关注控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final FollowService followService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 关注用户（需登录）
     *
     * @param userId  被关注用户ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/{userId}")
    public Response<Void> follow(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long currentUserId = userContextHolder.getRequiredUserId(request);
        log.info("用户关注，followerId: {}, followingId: {}", currentUserId, userId);
        followService.follow(currentUserId, userId);
        return Response.succeed();
    }

    /**
     * 取消关注（需登录）
     *
     * @param userId  被关注用户ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    public Response<Void> unfollow(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long currentUserId = userContextHolder.getRequiredUserId(request);
        log.info("用户取消关注，followerId: {}, followingId: {}", currentUserId, userId);
        followService.unfollow(currentUserId, userId);
        return Response.succeed();
    }

    /**
     * 检查是否关注（需登录）
     *
     * @param userId  被关注用户ID
     * @param request HttpServletRequest
     * @return 是否关注
     */
    @GetMapping("/check/{userId}")
    public Response<Map<String, Boolean>> checkFollow(
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long currentUserId = userContextHolder.getRequiredUserId(request);
        boolean isFollowing = followService.isFollowing(currentUserId, userId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isFollowing", isFollowing);
        return Response.succeed(result);
    }

    /**
     * 获取粉丝列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @param request  HttpServletRequest
     * @return 粉丝列表
     */
    @GetMapping("/followers/{userId}")
    public Response<PageVO<UserProfileVO>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 20;

        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.debug("获取粉丝列表，userId: {}, page: {}", userId, page);
        PageVO<UserProfileVO> result = followService.getFollowers(userId, page, pageSize, currentUserId);
        return Response.succeed(result);
    }

    /**
     * 获取关注列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @param request  HttpServletRequest
     * @return 关注列表
     */
    @GetMapping("/followings/{userId}")
    public Response<PageVO<UserProfileVO>> getFollowings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 20;

        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.debug("获取关注列表，userId: {}, page: {}", userId, page);
        PageVO<UserProfileVO> result = followService.getFollowings(userId, page, pageSize, currentUserId);
        return Response.succeed(result);
    }

    /**
     * 获取粉丝数和关注数
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    @GetMapping("/stats/{userId}")
    public Response<Map<String, Integer>> getFollowStats(@PathVariable Long userId) {
        int followerCount = followService.countFollowers(userId);
        int followingCount = followService.countFollowings(userId);
        Map<String, Integer> stats = new HashMap<>();
        stats.put("followerCount", followerCount);
        stats.put("followingCount", followingCount);
        return Response.succeed(stats);
    }
}
