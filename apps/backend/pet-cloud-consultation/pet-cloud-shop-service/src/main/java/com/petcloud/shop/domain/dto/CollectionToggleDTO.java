package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 切换收藏状态请求DTO
 *
 * @author luohao
 */
@Data
public class CollectionToggleDTO {
    /**
     * 商品ID
     */
    private Long productId;
}
