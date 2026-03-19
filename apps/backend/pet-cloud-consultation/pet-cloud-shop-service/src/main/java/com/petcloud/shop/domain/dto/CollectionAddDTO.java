package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 收藏商品请求DTO
 *
 * @author luohao
 */
@Data
public class CollectionAddDTO {
    /**
     * 商品ID
     */
    private Long productId;
}
