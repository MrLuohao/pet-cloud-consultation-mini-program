package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Date;

/**
 * 任务定义实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_definition")
public class TaskDefinition extends BaseEntity {

    /**
     * 任务编码，唯一标识
     */
    @TableField("task_code")
    private String taskCode;

    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 任务描述
     */
    @TableField("task_desc")
    private String taskDesc;

    /**
     * 任务图标（emoji或图标名）
     */
    @TableField("task_icon")
    private String taskIcon;

    /**
     * 任务类型: 1-每日任务 2-每周任务 3-一次性任务
     */
    @TableField("task_type")
    private Integer taskType;

    /**
     * 完成奖励积分
     */
    @TableField("points")
    private Integer points;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态: 0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 任务类型枚举
     */
    public enum TaskType {
        DAILY(1, "每日任务"),
        WEEKLY(2, "每周任务"),
        ONE_TIME(3, "一次性任务");

        private final Integer code;
        private final String desc;

        TaskType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
