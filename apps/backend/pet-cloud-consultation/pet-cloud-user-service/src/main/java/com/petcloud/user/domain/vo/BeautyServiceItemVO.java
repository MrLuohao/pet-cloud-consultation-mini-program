package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 美容服务项目VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeautyServiceItemVO {

    private Long id;

    private Long storeId;

    private String name;

    private String description;

    private String suitableWeight;

    /**
     * 服务时长（分钟）
     */
    private Integer duration;

    private BigDecimal price;
}
