package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 满减活动VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionVO {

    /**
     * 活动ID
     */
    private Long id;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 类型：1满减
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 满减门槛金额
     */
    private BigDecimal threshold;

    /**
     * 优惠金额
     */
    private BigDecimal discount;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 状态：0禁用 1启用
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动规则描述（如：满100减20）
     */
    private String ruleDesc;
}
