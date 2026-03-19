package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.vo.UserBriefVO;

import java.util.List;
import java.util.Map;

/**
 * 用户信息服务接口（调用用户服务）
 *
 * @author luohao
 */
public interface UserRemoteService {

    /**
     * 批量获取用户简要信息
     *
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户简要信息 的映射
     */
    Map<Long, UserBriefVO> batchGetUsers(List<Long> userIds);

    /**
     * 获取单个用户简要信息
     *
     * @param userId 用户ID
     * @return 用户简要信息
     */
    UserBriefVO getUser(Long userId);
}
