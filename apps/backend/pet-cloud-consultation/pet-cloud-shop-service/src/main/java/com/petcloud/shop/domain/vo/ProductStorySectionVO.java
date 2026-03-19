package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品详情故事段落 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStorySectionVO {

    private String title;

    private String description;

    private String imageUrl;
}
