package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin")
public class Admin extends BaseEntity {

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 性别(0未知/1男/2女)
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 脱敏手机号
     */
    @TableField("mask_phone")
    private String maskPhone;

    /**
     * 加密手机号
     */
    @TableField("encrypted_phone")
    private String encryptedPhone;

    /**
     * 加密盐
     */
    @TableField("salt")
    private String salt;

    /**
     * 加密账号密码
     */
    @TableField("encrypted_password")
    private String encryptedPassword;

    /**
     * 微信UnionID
     */
    @TableField("unionid")
    private String unionid;

    /**
     * 身份证号
     */
    @TableField("id_card")
    private String idCard;

    /**
     * 加密身份证号
     */
    @TableField("encrypted_id_card")
    private String encryptedIdCard;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 加密支付密码
     */
    @TableField("encrypted_payment_password")
    private String encryptedPaymentPassword;

    /**
     * 状态(0禁用/1启用)
     */
    @TableField("status")
    private Integer status;
}
