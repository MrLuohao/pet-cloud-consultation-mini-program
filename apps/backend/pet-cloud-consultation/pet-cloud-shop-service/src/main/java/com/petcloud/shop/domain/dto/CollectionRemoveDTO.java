package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 取消收藏请求DTO
 *
 * @author luohao
 */
@Data
public class CollectionRemoveDTO {
    /**
     * 商品ID
     */
    private Long productId;
}
