package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 微信小程序用户实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wx_user")
public class WxUser extends BaseEntity {

    /**
     * 微信OpenID
     */
    @TableField("openid")
    private String openid;

    /**
     * 微信UnionID
     */
    @TableField("unionid")
    private String unionid;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

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
     * 状态(0禁用/1正常)
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否会员(0-否 1-是)
     */
    @TableField("is_vip")
    private Integer isVip;

    /**
     * 会员等级
     */
    @TableField("vip_level")
    private String vipLevel;

    /**
     * 会员开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("vip_start_time")
    private Date vipStartTime;

    /**
     * 会员到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("vip_expire_time")
    private Date vipExpireTime;

    /**
     * 会员累计已省金额
     */
    @TableField("vip_saving_amount")
    private BigDecimal vipSavingAmount;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("last_login_time")
    private Date lastLoginTime;

    /**
     * 性别枚举
     */
    public enum Gender {
        UNKNOWN(0, "未知"),
        MALE(1, "男"),
        FEMALE(2, "女");

        private final Integer code;
        private final String desc;

        Gender(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        NORMAL(1, "正常");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
