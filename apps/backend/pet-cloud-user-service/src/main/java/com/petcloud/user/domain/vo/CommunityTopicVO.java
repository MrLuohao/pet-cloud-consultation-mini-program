package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 社区话题VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityTopicVO {

    /**
     * 话题ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 话题名称
     */
    private String name;

    /**
     * 话题图标
     */
    private String icon;

    /**
     * 话题描述
     */
    private String description;

    /**
     * 话题封面
     */
    private String coverUrl;

    /**
     * 动态数量
     */
    private Integer postCount;

    /**
     * 是否热门
     */
    private Boolean isHot;
}
