package com.petcloud.shop.application.impl;

import com.petcloud.common.core.response.Response;
import com.petcloud.shop.domain.service.UserRemoteService;
import com.petcloud.shop.domain.vo.UserBriefVO;
import com.petcloud.shop.infrastructure.feign.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户远程服务实现（通过 Feign 调用用户服务）
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRemoteServiceImpl implements UserRemoteService {

    private final UserServiceClient userServiceClient;

    @Override
    public Map<Long, UserBriefVO> batchGetUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            log.debug("Feign调用用户服务批量获取用户信息，数量: {}", userIds.size());
            Response<Map<Long, UserBriefVO>> response = userServiceClient.batchGetUsers(userIds);

            if (response != null && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Feign调用用户服务失败", e);
        }

        return Collections.emptyMap();
    }

    @Override
    public UserBriefVO getUser(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            log.debug("Feign调用用户服务获取用户信息，userId: {}", userId);
            Response<UserBriefVO> response = userServiceClient.getUser(userId);

            if (response != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Feign调用用户服务失败，userId: {}", userId, e);
        }

        return null;
    }
}
