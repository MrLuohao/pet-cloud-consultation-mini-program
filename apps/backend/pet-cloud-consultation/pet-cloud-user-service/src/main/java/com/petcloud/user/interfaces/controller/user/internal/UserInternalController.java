package com.petcloud.user.interfaces.controller.user.internal;

import com.petcloud.common.core.response.Response;
import com.petcloud.user.domain.vo.UserBriefVO;
import com.petcloud.user.domain.service.UserBriefService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户信息内部接口控制器（供其他微服务调用）
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/user")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserBriefService userBriefService;

    /**
     * 批量获取用户简要信息
     *
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户简要信息 的映射
     */
    @PostMapping("/batch")
    public Response<Map<Long, UserBriefVO>> batchGetUsers(@RequestBody List<Long> userIds) {
        log.info("批量获取用户信息，数量: {}", userIds.size());
        Map<Long, UserBriefVO> userMap = userBriefService.batchGetUsers(userIds);
        return Response.succeed(userMap);
    }

    /**
     * 获取单个用户简要信息
     *
     * @param userId 用户ID
     * @return 用户简要信息
     */
    @GetMapping("/{userId}")
    public Response<UserBriefVO> getUser(@PathVariable Long userId) {
        log.info("获取用户信息，userId: {}", userId);
        UserBriefVO user = userBriefService.getUser(userId);
        return Response.succeed(user);
    }
}
