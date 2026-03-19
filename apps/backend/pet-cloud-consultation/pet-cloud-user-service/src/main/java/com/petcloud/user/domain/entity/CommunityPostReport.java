package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子举报实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_post_report")
public class CommunityPostReport extends BaseEntity {

    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 举报用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 举报原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 举报类型: spam,abuse,inappropriate,other
     */
    @TableField("reason_type")
    private String reasonType;

    /**
     * 状态: 0待处理 1已处理 2已驳回
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理备注
     */
    @TableField("handler_remark")
    private String handlerRemark;
}
