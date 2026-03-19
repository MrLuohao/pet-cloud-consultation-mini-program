package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 优惠券实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
public class Coupon extends BaseEntity {

    /**
     * 优惠券名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型：1满减券 2折扣券
     */
    @TableField("type")
    private Integer type;

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
     * 最大优惠金额(折扣券用)
     */
    @TableField("max_discount")
    private BigDecimal maxDiscount;

    /**
     * 发行总量
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 已领取数量
     */
    @TableField("received_count")
    private Integer receivedCount;

    /**
     * 已使用数量
     */
    @TableField("used_count")
    private Integer usedCount;

    /**
     * 有效天数
     */
    @TableField("valid_days")
    private Integer validDays;

    /**
     * 生效时间
     */
    @TableField("start_time")
    private String startTime;

    /**
     * 失效时间
     */
    @TableField("end_time")
    private String endTime;

    /**
     * 使用说明
     */
    @TableField("description")
    private String description;

    /**
     * 状态：0禁用 1启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 优惠券类型枚举
     */
    public enum Type {
        FULL_REDUCTION(1, "满减券"),
        DISCOUNT(2, "折扣券");

        private final Integer code;
        private final String desc;

        Type(Integer code, String desc) {
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
