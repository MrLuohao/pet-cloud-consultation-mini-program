package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 美容服务项目实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("beauty_service")
public class BeautyServiceItem extends BaseEntity {

    @TableField("store_id")
    private Long storeId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("suitable_weight")
    private String suitableWeight;

    /**
     * 服务时长（分钟）
     */
    @TableField("duration")
    private Integer duration;

    @TableField("price")
    private BigDecimal price;

    /**
     * 状态(0下架/1上架)
     */
    @TableField("status")
    private Integer status;

    @TableField("sort_order")
    private Integer sortOrder;
}
