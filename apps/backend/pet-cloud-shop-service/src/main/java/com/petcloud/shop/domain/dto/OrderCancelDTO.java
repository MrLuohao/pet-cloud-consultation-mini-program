package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 取消订单请求DTO
 *
 * @author luohao
 */
@Data
public class OrderCancelDTO {
    private Long orderId;
}
