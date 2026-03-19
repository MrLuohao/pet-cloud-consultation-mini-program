package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章评论实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_comment")
public class ArticleComment extends BaseEntity {

    /**
     * 文章ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户昵称
     */
    @TableField("user_nickname")
    private String userNickname;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 父评论ID（用于回复）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 回复目标用户ID
     */
    @TableField("reply_to_user_id")
    private Long replyToUserId;

    /**
     * 回复目标用户昵称
     */
    @TableField("reply_to_nickname")
    private String replyToNickname;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;
}
