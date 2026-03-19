package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新社区动态DTO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostUpdateDTO {

    /**
     * 动态内容
     */
    @Size(max = 1000, message = "动态内容长度不能超过1000字符")
    private String content;

    /**
     * 媒体URL列表
     */
    private List<String> mediaUrls;

    /**
     * 审核通过的媒体资产ID列表
     */
    private List<Long> mediaAssetIds;

    /**
     * 媒体类型（image/video）
     */
    private String mediaType;

    /**
     * 关联宠物ID
     */
    private Long petId;

    /**
     * 话题ID
     */
    private Long topicId;

    /**
     * 可见性: 0-所有人可见 1-指定人可见 2-仅自己可见
     */
    private Integer visibility;

    /**
     * 指定可见用户ID列表（当visibility=1时使用）
     */
    private List<Long> visibleUserIds;
}
