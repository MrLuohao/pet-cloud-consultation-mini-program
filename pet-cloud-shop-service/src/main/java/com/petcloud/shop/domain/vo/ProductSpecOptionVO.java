package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品规格选项 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecOptionVO {

    private String value;

    private String label;

    private String hint;
}
