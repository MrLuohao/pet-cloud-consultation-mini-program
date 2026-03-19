package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新增文章DTO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCreateDTO {

    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200字符")
    private String title;

    /**
     * 封面图
     */
    @Size(max = 500, message = "封面图URL长度不能超过500字符")
    private String coverUrl;

    /**
     * 摘要
     */
    @Size(max = 500, message = "摘要长度不能超过500字符")
    private String summary;

    /**
     * 文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;

    /**
     * 标签
     */
    @Size(max = 100, message = "标签长度不能超过100字符")
    private String tag;

    /**
     * 状态(0草稿/1已发布)，默认为草稿
     */
    private Integer status;
}
