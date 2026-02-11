package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 添加购物车请求DTO
 *
 * @author luohao
 */
@Data
public class CartAddDTO {
    private Long productId;
    private Integer quantity = 1;
}
