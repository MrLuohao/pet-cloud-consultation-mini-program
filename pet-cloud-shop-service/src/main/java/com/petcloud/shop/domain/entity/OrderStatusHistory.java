package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_status_history")
public class OrderStatusHistory extends BaseEntity {

    @TableField("order_id")
    private Long orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("from_status")
    private Integer fromStatus;

    @TableField("to_status")
    private Integer toStatus;

    @TableField("action")
    private String action;

    @TableField("operator_type")
    private String operatorType;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("logistics_company")
    private String logisticsCompany;

    @TableField("tracking_no")
    private String trackingNo;

    @TableField("remark")
    private String remark;

    @TableField("operate_time")
    private Date operateTime;
}
