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
 * 基础实体类 - 仅包含通用审计字段
 *
 * 软删除字段是否存在由具体实体按业务需要自行声明，
 * BaseEntity 默认不再携带 isDeleted。
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
     * 创建人名称
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
     * 修改人名称
     */
    @TableField(value = "modifier_name", fill = FieldFill.INSERT_UPDATE)
    private String modifierName;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;

}
