package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户主页信息VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 性别(0未知/1男/2女)
     */
    private Integer gender;

    /**
     * 是否会员
     */
    private Boolean isVip;

    /**
     * 会员等级
     */
    private String vipLevel;

    /**
     * 帖子数量
     */
    private Integer postCount;

    /**
     * 关注数量
     */
    private Integer followingCount;

    /**
     * 粉丝数量
     */
    private Integer followerCount;

    /**
     * 当前用户是否已关注该用户
     */
    private Boolean isFollowed;

    /**
     * 是否是当前用户自己
     */
    private Boolean isSelf;
}
