package com.petcloud.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_task")
public class AiTask extends BaseEntity {

    @TableField("task_no")
    private String taskNo;

    @TableField("task_type")
    private String taskType;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private Long bizId;

    @TableField("user_id")
    private Long userId;

    @TableField("guest_device_hash")
    private String guestDeviceHash;

    @TableField("model_provider")
    private String modelProvider;

    @TableField("model_name")
    private String modelName;

    @TableField("prompt_version")
    private String promptVersion;

    @TableField("template_version")
    private String templateVersion;

    @TableField("status")
    private String status;

    @TableField("input_snapshot")
    private String inputSnapshot;

    @TableField("output_snapshot")
    private String outputSnapshot;

    @TableField("error_message")
    private String errorMessage;

    @TableField("latency_ms")
    private Long latencyMs;

    @TableField("trace_id")
    private String traceId;
}
