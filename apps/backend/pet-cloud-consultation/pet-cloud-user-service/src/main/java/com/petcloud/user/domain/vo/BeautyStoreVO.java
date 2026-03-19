package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 美容门店VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeautyStoreVO {

    /**
     * 门店ID
     */
    private Long id;

    /**
     * 门店名称
     */
    private String name;

    /**
     * 封面图
     */
    private String coverUrl;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 距离
     */
    private String distance;

    /**
     * 地址
     */
    private String address;

    /**
     * 服务标签
     */
    private String tags;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 营业时间
     */
    private String businessHours;
}
