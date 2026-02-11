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
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 发布时间（格式化字符串）
     */
    private String createTime;
}
