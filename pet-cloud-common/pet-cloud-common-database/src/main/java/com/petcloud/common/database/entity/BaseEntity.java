package com.petcloud.common.database.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础实体类 - 包含通用字段
 *
 * @author luohao
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableField("id")
    private Long id;

    /**
     * 创建人ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "creator_id", fill = FieldFill.INSERT)
    private Long creatorId;

    /**
     * 创建人姓名
     */
    @TableField(value = "creator_name", fill = FieldFill.INSERT)
    private String creatorName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改人ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "modifier_id", fill = FieldFill.INSERT_UPDATE)
    private Long modifierId;

    /**
     * 修改人姓名
     */
    @TableField(value = "modifier_name", fill = FieldFill.INSERT_UPDATE)
    private String modifierName;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;

    /**
     * 逻辑删除标识（0-正常，1-已删除）
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
