package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求DTO
 *
 * @author luohao
 */
@Data
public class AdminLoginDTO {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 用户临时授权code码
     */
    @NotBlank(message = "code码不能为空")
    private String code;
}
