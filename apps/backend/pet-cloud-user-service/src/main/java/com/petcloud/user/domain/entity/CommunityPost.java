package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区动态实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_post")
public class CommunityPost extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("content")
    private String content;

    /** 媒体URL数组（JSON） */
    @TableField("media_urls")
    private String mediaUrls;

    /** image / video */
    @TableField("media_type")
    private String mediaType;

    @TableField("pet_id")
    private Long petId;

    /**
     * 话题ID
     */
    @TableField("topic_id")
    private Long topicId;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 分享数
     */
    @TableField("share_count")
    private Integer shareCount;

    /**
     * 收藏数
     */
    @TableField("collect_count")
    private Integer collectCount;

    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 是否置顶: 0否 1是
     */
    @TableField("is_pinned")
    private Integer isPinned;

    /**
     * 是否热门: 0否 1是
     */
    @TableField("is_hot")
    private Integer isHot;

    /**
     * 可见性: 0-所有人可见 1-指定人可见 2-仅自己可见
     */
    @TableField("visibility")
    private Integer visibility;

    /**
     * 指定可见用户ID列表（JSON数组，当visibility=1时使用）
     */
    @TableField("visible_user_ids")
    private String visibleUserIds;
}
