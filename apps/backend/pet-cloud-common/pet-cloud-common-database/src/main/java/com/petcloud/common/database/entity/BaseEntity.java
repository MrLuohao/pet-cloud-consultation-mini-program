package com.petcloud.common.database.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础实体类 - 包含通用字段
 *
 * 注意：isDeleted 字段不再使用 @TableLogic 注解，
 * 如果需要逻辑删除功能，请在具体实体类中单独声明并添加 @TableLogic 注解。
 * 这样可以避免关联表（如点赞、收藏、购物车等）在删除后再次添加时的唯一约束冲突问题。
 *
 * @author luohao
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 删除标识（0-正常，1-已删除）
     * 注意：此字段不自动启用逻辑删除，需要在具体实体中添加 @TableLogic 注解才会生效
     */
    @TableField("is_deleted")
    private Integer isDeleted;
}
