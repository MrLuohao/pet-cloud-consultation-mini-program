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

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField("is_deleted")
    private Integer isDeleted;
}
