package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 宠物时间轴事件VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetTimelineVO {

    /** 事件类型: health_record / consultation / reminder */
    private String eventType;

    private Long eventId;

    private String title;

    private String content;

    private LocalDate eventDate;

    /** 图标 emoji */
    private String icon;

    /** 颜色标签 */
    private String color;
}
