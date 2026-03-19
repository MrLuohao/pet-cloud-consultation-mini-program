package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品评价统计VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryVO {

    /**
     * 总评价数
     */
    private Integer total;

    /**
     * 好评数（评分>=4）
     */
    private Integer goodCount;

    /**
     * 差评数（评分<=3）
     */
    private Integer badCount;

    /**
     * 有图评价数
     */
    private Integer withImagesCount;

    /**
     * 平均评分
     */
    private BigDecimal avgRating;
}
