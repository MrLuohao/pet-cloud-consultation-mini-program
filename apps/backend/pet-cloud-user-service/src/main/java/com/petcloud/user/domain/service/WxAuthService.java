package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.WxLoginDTO;
import com.petcloud.user.domain.vo.LoginVO;
import com.petcloud.user.domain.vo.UserInfoVO;

/**
 * 微信认证服务接口
 *
 * @author luohao
 */
public interface WxAuthService {

    /**
     * 微信小程序登录
     *
     * @param loginDTO 登录请求DTO
     * @return 登录响应VO
     */
    LoginVO login(WxLoginDTO loginDTO);

    /**
     * 刷新Token
     *
     * @param token 旧Token
     * @return 新Token
     */
    String refreshToken(String token);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param nickname 昵称
     * @param avatarUrl 头像URL
     * @param gender 性别
     */
    void updateUserInfo(Long userId, String nickname, String avatarUrl, Integer gender);

    /**
     * 登出（将Token加入黑名单）
     *
     * @param token Token
     */
    void logout(String token);
}
