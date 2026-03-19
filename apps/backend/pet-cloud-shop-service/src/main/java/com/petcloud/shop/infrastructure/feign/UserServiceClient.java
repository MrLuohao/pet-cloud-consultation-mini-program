package com.petcloud.shop.infrastructure.feign;

import com.petcloud.common.core.response.Response;
import com.petcloud.shop.domain.vo.UserBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 用户服务 Feign Client
 *
 * @author luohao
 */
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8117}")
public interface UserServiceClient {

    /**
     * 批量获取用户简要信息
     *
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户简要信息 的映射
     */
    @PostMapping("/api/internal/user/batch")
    Response<Map<Long, UserBriefVO>> batchGetUsers(@RequestBody List<Long> userIds);

    /**
     * 获取单个用户简要信息
     *
     * @param userId 用户ID
     * @return 用户简要信息
     */
    @GetMapping("/api/internal/user/{userId}")
    Response<UserBriefVO> getUser(@PathVariable("userId") Long userId);
}
