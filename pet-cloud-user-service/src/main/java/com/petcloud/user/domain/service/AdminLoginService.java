package com.petcloud.user.domain.service;

import com.petcloud.common.core.response.Response;
import com.petcloud.user.domain.dto.AdminLoginDTO;

/**
 * 管理员登录领域服务接口
 *
 * @author luohao
 */
public interface AdminLoginService {

    /**
     * 管理员登录
     */
    Response<?> login(AdminLoginDTO dto);
}
