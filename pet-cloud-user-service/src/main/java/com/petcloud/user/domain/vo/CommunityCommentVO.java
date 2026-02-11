package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 评论内容
     */
    private String content;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
