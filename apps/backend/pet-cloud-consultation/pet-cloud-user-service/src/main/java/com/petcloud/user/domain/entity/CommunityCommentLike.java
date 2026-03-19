package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论点赞实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_comment_like")
public class CommunityCommentLike extends BaseEntity {

    /**
     * 评论ID
     */
    @TableField("comment_id")
    private Long commentId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
}
