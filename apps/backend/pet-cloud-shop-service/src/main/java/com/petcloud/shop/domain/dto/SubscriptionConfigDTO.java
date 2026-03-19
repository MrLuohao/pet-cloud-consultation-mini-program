package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 修改订阅配置DTO
 *
 * @author luohao
 */
@Data
public class SubscriptionConfigDTO {
    private Integer cycleDays;
    private Integer quantity;
    private Long addressId;
}
