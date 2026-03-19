package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录DTO
 *
 * @author luohao
 */
@Data
public class WxLoginDTO {

    /**
     * 微信登录code
     */
    @NotBlank(message = "登录凭证不能为空")
    private String code;

    /**
     * 用户昵称（可选，用于新用户注册）
     */
    private String nickname;

    /**
     * 头像URL（可选，用于新用户注册）
     */
    private String avatarUrl;

    /**
     * 性别（可选，0-未知，1-男，2-女）
     */
    private Integer gender;
}
