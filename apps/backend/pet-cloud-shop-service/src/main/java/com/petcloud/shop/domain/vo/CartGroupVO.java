package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartGroupVO {
    private String merchantId;
    private String merchantName;
    private String serviceText;
    private Boolean allSelected;
    private Integer selectedCount;
    private BigDecimal totalAmount;
    private List<CartItemVO> items;
}
