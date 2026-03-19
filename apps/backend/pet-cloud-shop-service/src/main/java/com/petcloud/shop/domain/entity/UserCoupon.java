package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户优惠券实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_coupon")
public class UserCoupon extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 优惠券ID
     */
    @TableField("coupon_id")
    private Long couponId;

    /**
     * 优惠券名称
     */
    @TableField("coupon_name")
    private String couponName;

    /**
     * 类型：1满减券 2折扣券
     */
    @TableField("coupon_type")
    private Integer couponType;

    /**
     * 减免金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 折扣率
     */
    @TableField("discount_rate")
    private BigDecimal discountRate;

    /**
     * 最低使用金额
     */
    @TableField("min_amount")
    private BigDecimal minAmount;

    /**
     * 最大优惠金额
     */
    @TableField("max_discount")
    private BigDecimal maxDiscount;

    /**
     * 状态：0未使用 1已使用 2已过期
     */
    @TableField("status")
    private Integer status;

    /**
     * 使用时间
     */
    @TableField("use_time")
    private String useTime;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private String expireTime;

    /**
     * 用户优惠券状态枚举
     */
    public enum Status {
        UNUSED(0, "未使用"),
        USED(1, "已使用"),
        EXPIRED(2, "已过期");

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
