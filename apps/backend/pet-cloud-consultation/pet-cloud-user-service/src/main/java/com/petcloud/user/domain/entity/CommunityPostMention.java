package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子@提及实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_post_mention")
public class CommunityPostMention extends BaseEntity {

    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 评论ID（如果是评论中的@）
     */
    @TableField("comment_id")
    private Long commentId;

    /**
     * 被@的用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * @发起者ID
     */
    @TableField("mention_by")
    private Long mentionBy;

    /**
     * 是否已读: 0否 1是
     */
    @TableField("is_read")
    private Integer isRead;
}
