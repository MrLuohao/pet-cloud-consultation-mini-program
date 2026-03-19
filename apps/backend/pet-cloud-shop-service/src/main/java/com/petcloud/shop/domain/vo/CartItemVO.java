package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVO {
    private Long id;
    private Long productId;
    private String name;
    private String coverUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer quantity;
    private Integer stock;
    private String shopId;
    private String shopName;
    private String serviceText;
    private String spec;
    private Boolean selected;
    private String status;
    private BigDecimal subtotal;
}
