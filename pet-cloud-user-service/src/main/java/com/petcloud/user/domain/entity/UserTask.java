package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Date;

/**
 * 用户任务记录实体类
 *
 * 注意：此表为事实记录表，不支持软删除，因此显式排除 is_deleted 字段
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_task")
public class UserTask extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 任务定义ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 任务编码
     */
    @TableField("task_code")
    private String taskCode;

    /**
     * 获得积分
     */
    @TableField("points")
    private Integer points;

    /**
     * 状态: 0-未完成 1-已完成
     */
    @TableField("status")
    private Integer status;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("complete_time")
    private Date completeTime;

    /**
     * 任务日期（用于每日任务）
     */
    @TableField("task_date")
    private LocalDate taskDate;

    /**
     * 此表为事实记录表，不需要软删除功能
     * 显式设置为 false，MyBatis-Plus 将不会查询此字段
     */
    @TableField(exist = false)
    private Integer isDeleted;

    /**
     * 状态枚举
     */
    public enum Status {
        INCOMPLETE(0, "未完成"),
        COMPLETED(1, "已完成");

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
