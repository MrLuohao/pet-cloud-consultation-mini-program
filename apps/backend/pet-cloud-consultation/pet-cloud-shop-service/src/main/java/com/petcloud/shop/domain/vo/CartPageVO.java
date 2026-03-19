package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartPageVO {
    private List<CartGroupVO> cartGroups;
    private List<CartItemVO> invalidItems;
    private CartSummaryVO summary;
}
