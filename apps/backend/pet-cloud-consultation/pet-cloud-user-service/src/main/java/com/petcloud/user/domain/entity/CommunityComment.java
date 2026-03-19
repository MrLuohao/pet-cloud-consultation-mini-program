package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区评论实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_comment")
public class CommunityComment extends BaseEntity {

    @TableField("post_id")
    private Long postId;

    @TableField("user_id")
    private Long userId;

    /**
     * 回复目标评论ID
     */
    @TableField("reply_to_id")
    private Long replyToId;

    /**
     * 回复目标用户ID
     */
    @TableField("reply_to_user_id")
    private Long replyToUserId;

    @TableField("content")
    private String content;

    /**
     * 媒体URL数组（JSON）
     */
    @TableField("media_urls")
    private String mediaUrls;

    /**
     * 媒体类型（image/video）
     */
    @TableField("media_type")
    private String mediaType;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    @TableField("is_deleted")
    private Integer isDeleted;
}
