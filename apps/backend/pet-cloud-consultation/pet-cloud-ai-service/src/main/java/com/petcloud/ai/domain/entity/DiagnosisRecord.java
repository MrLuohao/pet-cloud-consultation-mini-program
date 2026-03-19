package com.petcloud.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diagnosis_record")
public class DiagnosisRecord extends BaseEntity {

    @TableField("task_id")
    private Long taskId;

    @TableField("user_id")
    private Long userId;

    @TableField("pet_id")
    private Long petId;

    @TableField("guest_device_hash")
    private String guestDeviceHash;

    @TableField("symptom_tags_json")
    private String symptomTagsJson;

    @TableField("symptom_description")
    private String symptomDescription;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("summary")
    private String summary;

    @TableField("possible_causes_json")
    private String possibleCausesJson;

    @TableField("care_suggestions_json")
    private String careSuggestionsJson;

    @TableField("next_actions_json")
    private String nextActionsJson;

    @TableField("observation_table_json")
    private String observationTableJson;

    @TableField("should_consult_doctor")
    private Integer shouldConsultDoctor;

    @TableField("status")
    private String status;

    @TableField("diagnosis_time")
    private Date diagnosisTime;
}
