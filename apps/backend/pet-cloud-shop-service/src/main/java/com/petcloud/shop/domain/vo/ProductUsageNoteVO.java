package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品使用建议 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUsageNoteVO {

    private String title;

    private String content;
}
