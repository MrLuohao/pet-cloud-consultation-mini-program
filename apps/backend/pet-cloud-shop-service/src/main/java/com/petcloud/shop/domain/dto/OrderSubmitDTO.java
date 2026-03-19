package com.petcloud.shop.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 订单提交请求DTO
 *
 * @author luohao
 */
@Data
public class OrderSubmitDTO {
    private List<Long> productIds;
    private List<Integer> quantities;
    private List<Long> cartIds;
    private List<String> specLabels;
    private Long addressId;
    private Long couponId;
    private String remark;
    private String paymentMethod;
    private String verificationType;
}
