package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建社区动态DTO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostCreateDTO {

    /**
     * 动态内容（必填）
     */
    @NotBlank(message = "动态内容不能为空")
    @Size(max = 1000, message = "动态内容长度不能超过1000字符")
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
     * 关联宠物ID
     */
    private Long petId;

    /**
     * 话题ID列表
     */
    private List<Long> topicIds;
}
