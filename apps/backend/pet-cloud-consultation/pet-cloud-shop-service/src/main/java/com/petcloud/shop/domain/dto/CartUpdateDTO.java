package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 更新购物车请求DTO
 *
 * @author luohao
 */
@Data
public class CartUpdateDTO {
    private Long cartId;
    private Integer quantity;
}
