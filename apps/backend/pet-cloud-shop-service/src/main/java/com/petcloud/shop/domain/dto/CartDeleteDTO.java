package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 删除购物车请求DTO
 *
 * @author luohao
 */
@Data
public class CartDeleteDTO {
    private Long cartId;
}
