package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 创建商品订阅请求DTO
 *
 * @author luohao
 */
@Data
public class SubscriptionCreateDTO {
    private Long productId;
    private Long skuId;
    private Integer quantity;
    private Integer cycleDays;
    private Long addressId;
}
