package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.core.utils.JsonUtils;
import com.petcloud.user.domain.dto.AdminLoginDTO;
import com.petcloud.user.domain.service.AdminLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员登录控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/v1/safety")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminLoginService adminLoginService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Response<?> login(@RequestBody @Valid AdminLoginDTO dto) {
        log.info("管理员登录请求: {}", JsonUtils.toString(dto));
        return adminLoginService.login(dto);
    }
}
