package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.VipSubscribeDTO;
import com.petcloud.user.domain.vo.UserInfoVO;

/**
 * 会员服务接口
 */
public interface VipService {

    /**
     * 开通/续费会员
     *
     * @param userId 用户ID
     * @param dto 请求参数
     * @return 用户会员信息
     */
    UserInfoVO subscribe(Long userId, VipSubscribeDTO dto);
}
