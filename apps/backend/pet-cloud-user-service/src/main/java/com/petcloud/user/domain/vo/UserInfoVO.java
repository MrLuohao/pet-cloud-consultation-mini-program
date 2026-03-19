package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户信息VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 兼容字段：userId
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 性别(0未知/1男/2女)
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 是否会员
     */
    private Boolean isVip;

    /**
     * 会员等级
     */
    private String vipLevel;

    /**
     * 会员到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date vipExpireDate;

    /**
     * 会员累计已省金额
     */
    private String savingAmount;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;
}
