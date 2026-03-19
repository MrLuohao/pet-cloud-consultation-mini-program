package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 优惠券VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponVO {

    /**
     * 优惠券ID
     */
    private Long id;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 类型：1满减券 2折扣券
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 减免金额
     */
    private BigDecimal discountAmount;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * 最低使用金额
     */
    private BigDecimal minAmount;

    /**
     * 最大优惠金额
     */
    private BigDecimal maxDiscount;

    /**
     * 有效天数
     */
    private Integer validDays;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 使用说明
     */
    private String description;

    /**
     * 发行总量
     */
    private Integer totalCount;

    /**
     * 已领取数量
     */
    private Integer receivedCount;

    /**
     * 是否可领取
     */
    private Boolean canReceive;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CouponVO vo = new CouponVO();

        public Builder id(Long id) {
            vo.id = id;
            return this;
        }

        public Builder name(String name) {
            vo.name = name;
            return this;
        }

        public Builder type(Integer type) {
            vo.type = type;
            return this;
        }

        public Builder typeDesc(String typeDesc) {
            vo.typeDesc = typeDesc;
            return this;
        }

        public Builder discountAmount(java.math.BigDecimal discountAmount) {
            vo.discountAmount = discountAmount;
            return this;
        }

        public Builder discountRate(java.math.BigDecimal discountRate) {
            vo.discountRate = discountRate;
            return this;
        }

        public Builder minAmount(java.math.BigDecimal minAmount) {
            vo.minAmount = minAmount;
            return this;
        }

        public Builder maxDiscount(java.math.BigDecimal maxDiscount) {
            vo.maxDiscount = maxDiscount;
            return this;
        }

        public Builder validDays(Integer validDays) {
            vo.validDays = validDays;
            return this;
        }

        public Builder startTime(String startTime) {
            vo.startTime = startTime;
            return this;
        }

        public Builder endTime(String endTime) {
            vo.endTime = endTime;
            return this;
        }

        public Builder description(String description) {
            vo.description = description;
            return this;
        }

        public Builder totalCount(Integer totalCount) {
            vo.totalCount = totalCount;
            return this;
        }

        public Builder receivedCount(Integer receivedCount) {
            vo.receivedCount = receivedCount;
            return this;
        }

        public Builder canReceive(Boolean canReceive) {
            vo.canReceive = canReceive;
            return this;
        }

        public CouponVO build() {
            return vo;
        }
    }
}
