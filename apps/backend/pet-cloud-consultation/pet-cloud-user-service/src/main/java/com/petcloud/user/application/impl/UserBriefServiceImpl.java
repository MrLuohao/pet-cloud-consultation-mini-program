package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.service.UserBriefService;
import com.petcloud.user.domain.vo.UserBriefVO;
import com.petcloud.user.infrastructure.persistence.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户简要信息服务实现
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBriefServiceImpl implements UserBriefService {

    private final WxUserMapper wxUserMapper;

    @Override
    public Map<Long, UserBriefVO> batchGetUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 去重
        List<Long> distinctIds = userIds.stream().distinct().collect(Collectors.toList());

        // 批量查询
        LambdaQueryWrapper<WxUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WxUser::getId, distinctIds);
        List<WxUser> users = wxUserMapper.selectList(wrapper);

        // 转换为 Map
        return users.stream()
                .collect(Collectors.toMap(
                        WxUser::getId,
                        user -> UserBriefVO.builder()
                                .id(user.getId())
                                .nickname(user.getNickname())
                                .avatarUrl(user.getAvatarUrl())
                                .build()
                ));
    }

    @Override
    public UserBriefVO getUser(Long userId) {
        if (userId == null) {
            return null;
        }

        WxUser user = wxUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        return UserBriefVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
