package com.petcloud.shop.domain.dto;

import lombok.Data;

/**
 * 商品评价请求DTO
 *
 * @author luohao
 */
@Data
public class ProductReviewDTO {
    private Long orderItemId;
    private Long productId;
    private Integer rating;
    private String content;
    private String images;
}
