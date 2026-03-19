package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品规格组 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecGroupVO {

    private String key;

    private String label;

    private String selectedValue;

    private List<ProductSpecOptionVO> options;
}
