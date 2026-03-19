package com.petcloud.shop.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class OrderTimelineVO {
    private String action;
    private Integer fromStatus;
    private String fromStatusDesc;
    private Integer toStatus;
    private String toStatusDesc;
    private String operatorType;
    private Long operatorId;
    private String operatorName;
    private String logisticsCompany;
    private String trackingNo;
    private String remark;
    private Date operateTime;
}
