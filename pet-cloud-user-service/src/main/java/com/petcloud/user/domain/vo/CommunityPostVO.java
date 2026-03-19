package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 社区动态VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostVO {

    /**
     * 动态ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 发布者用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 发布者昵称
     */
    private String nickname;

    /**
     * 帖子类型：pet_post / author_post
     */
    private String postType;

    /**
     * 发布者头像
     */
    private String avatarUrl;

    /**
     * 动态内容
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
     * 关联宠物名称
     */
    private String petName;

    /**
     * 作者摘要
     */
    private AuthorVO author;

    /**
     * 宠物身份摘要
     */
    private PetIdentityVO pet;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户是否已收藏
     */
    private Boolean isCollected;

    /**
     * 当前用户是否是作者（用于判断是否显示删除按钮）
     */
    private Boolean isSelf;

    /**
     * 可见性: 0-所有人可见 1-指定人可见 2-仅自己可见
     */
    private Integer visibility;

    /**
     * 指定可见用户ID列表（当visibility=1时使用）
     */
    private List<Long> visibleUserIds;

    /**
     * 指定可见用户的昵称列表（用于前端显示）
     */
    private List<String> visibleUserNames;

    /**
     * 话题ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long topicId;

    /**
     * 话题名称
     */
    private String topicName;

    /**
     * 是否置顶
     */
    private Boolean isPinned;

    /**
     * 是否热门
     */
    private Boolean isHot;

    /**
     * 发布时间（格式化字符串）
     */
    private String createTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long userId;
        private String displayName;
        private String avatarUrl;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetIdentityVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long petId;
        private String name;
        private String avatarUrl;
        private String breed;
        private String ageText;
        private String signature;
    }
}
