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
 * 会员订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vip_order")
public class VipOrder extends BaseEntity {

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 套餐ID
     */
    @TableField("plan_id")
    private String planId;

    /**
     * 套餐名称
     */
    @TableField("plan_name")
    private String planName;

    /**
     * 支付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 有效天数
     */
    @TableField("duration_days")
    private Integer durationDays;

    /**
     * 状态: 0-待支付 1-已支付 2-已取消
     */
    @TableField("status")
    private Integer status;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("pay_time")
    private Date payTime;

    /**
     * 到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("expire_time")
    private Date expireTime;
}
