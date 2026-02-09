package com.petcloud.user.application.impl;

import com.petcloud.common.core.response.Response;
import com.petcloud.user.domain.dto.AdminLoginDTO;
import com.petcloud.user.domain.service.AdminLoginService;
import com.petcloud.user.domain.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 管理员登录应用服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLoginServiceImpl implements AdminLoginService {

    private final AdminService adminService;

    @Override
    public Response<?> login(AdminLoginDTO dto) {
        // TODO: 实现登录逻辑
        return Response.succeed();
    }
}
