package com.petcloud.shop.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 订单确认请求DTO
 *
 * @author luohao
 */
@Data
public class OrderConfirmDTO {
    private List<Long> productIds;
    private List<Integer> quantities;
    private List<Long> cartIds;
}
