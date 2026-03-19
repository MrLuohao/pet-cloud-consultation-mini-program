package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评论DTO
 *
 * @author luohao
 */
@Data
public class CommentCreateDTO {

    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    /**
     * 父评论ID（回复时使用）
     */
    private Long parentId;

    /**
     * 回复目标用户ID（回复时使用）
     */
    private Long replyToUserId;
}
