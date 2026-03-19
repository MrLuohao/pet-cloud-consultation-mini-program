package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 社区评论VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityCommentVO {

    /**
     * 评论ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 评论者用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 评论者昵称
     */
    private String nickname;

    /**
     * 评论者头像
     */
    private String avatarUrl;

    /**
     * 回复目标评论ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long replyToId;

    /**
     * 回复目标用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long replyToUserId;

    /**
     * 回复目标用户昵称
     */
    private String replyToNickname;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 媒体URL列表
     */
    private List<String> mediaUrls;

    /**
     * 媒体类型（image/video）
     */
    private String mediaType;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户是否是评论作者（用于判断是否显示删除按钮）
     */
    private Boolean isSelf;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
