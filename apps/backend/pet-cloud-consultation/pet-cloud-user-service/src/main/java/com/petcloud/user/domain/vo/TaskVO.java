package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskVO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务编码
     */
    private String code;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String desc;

    /**
     * 任务图标
     */
    private String icon;

    /**
     * 完成奖励积分
     */
    private Integer points;

    /**
     * 是否已完成
     */
    private Boolean completed;

    /**
     * 任务类型: 1-每日任务 2-每周任务 3-一次性任务
     */
    private Integer type;
}
