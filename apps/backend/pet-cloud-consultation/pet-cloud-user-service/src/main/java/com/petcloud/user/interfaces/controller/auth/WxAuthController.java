package com.petcloud.user.interfaces.controller.auth;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.WxLoginDTO;
import com.petcloud.user.domain.service.WxAuthService;
import com.petcloud.user.domain.vo.LoginVO;
import com.petcloud.user.domain.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 微信认证控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class WxAuthController {

    private final WxAuthService wxAuthService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 微信小程序登录
     *
     * @param loginDTO 登录请求DTO
     * @return 登录响应
     */
    @PostMapping("/login")
    public Response<LoginVO> login(@Valid @RequestBody WxLoginDTO loginDTO) {
        log.info("微信登录请求");
        LoginVO loginVO = wxAuthService.login(loginDTO);
        return Response.succeed(loginVO);
    }

    /**
     * 刷新Token
     *
     * @param token 旧Token
     * @return 新Token
     */
    @PostMapping("/refresh")
    public Response<String> refreshToken(@RequestHeader("Authorization") String token) {
        // 去除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String newToken = wxAuthService.refreshToken(token);
        return Response.succeed(newToken);
    }

    /**
     * 获取用户信息
     *
     * @param request HttpServletRequest
     * @return 用户信息
     */
    @GetMapping("/userinfo")
    public Response<UserInfoVO> getUserInfo(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取用户信息，userId: {}", userId);
        UserInfoVO userInfoVO = wxAuthService.getUserInfo(userId);
        return Response.succeed(userInfoVO);
    }

    /**
     * 更新用户信息
     *
     * @param request HttpServletRequest
     * @param nickname 昵称
     * @param avatarUrl 头像URL
     * @param gender 性别
     * @return 操作结果
     */
    @PutMapping("/userinfo")
    public Response<Void> updateUserInfo(
            HttpServletRequest request,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) Integer gender) {
        Long userId = userContextHolder.getRequiredUserId(request);
        wxAuthService.updateUserInfo(userId, nickname, avatarUrl, gender);
        return Response.succeed();
    }

    /**
     * 登出
     *
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Response<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        log.info("用户登出请求");
        wxAuthService.logout(token);
        return Response.succeed();
    }
}
