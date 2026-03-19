package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程章节VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterVO {

    private String id;

    private String title;

    /** 时长（秒） */
    private Integer duration;

    private String videoUrl;

    /** 是否免费试看 */
    private Boolean isFree;

    private Integer sortOrder;
}
