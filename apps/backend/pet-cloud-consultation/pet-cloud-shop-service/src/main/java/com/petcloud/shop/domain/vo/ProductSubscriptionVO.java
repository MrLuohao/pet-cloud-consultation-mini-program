package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品订阅VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSubscriptionVO {

    private Long id;
    private Long productId;
    private String productName;
    private String productCoverUrl;
    private Integer quantity;
    private Integer cycleDays;
    private String cycleDesc;
    private Integer status;
    private String statusDesc;
    private LocalDate nextOrderDate;
    private BigDecimal discountRate;
    private BigDecimal unitPrice;
    private BigDecimal subscribePrice;
}
