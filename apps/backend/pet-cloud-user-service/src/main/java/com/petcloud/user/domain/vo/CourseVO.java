package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseVO {

    private Long id;

    private String title;

    private String description;

    private String coverUrl;

    private Integer lessonCount;

    private Integer studentCount;

    private BigDecimal price;

    private String tag;

    private String instructorName;

    private String instructorAvatar;

    private String instructorBio;

    /** 章节列表（从JSON解析） */
    private List<ChapterVO> chapters;

    /** 当前用户学习进度 0-100，未学习为null */
    private Integer userProgress;

    /** 是否已完成 */
    private Boolean isCompleted;
}
