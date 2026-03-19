package com.petcloud.shop.domain.dto;

import lombok.Data;

@Data
public class OrderShipDTO {
    private Long orderId;
    private String logisticsCompany;
    private String trackingNo;
    private String remark;
}
