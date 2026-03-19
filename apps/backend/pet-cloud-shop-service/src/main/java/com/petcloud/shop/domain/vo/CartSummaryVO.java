package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryVO {
    private Integer selectedCount;
    private BigDecimal totalAmount;
    private BigDecimal totalDiscount;
    private Boolean allSelected;
}
