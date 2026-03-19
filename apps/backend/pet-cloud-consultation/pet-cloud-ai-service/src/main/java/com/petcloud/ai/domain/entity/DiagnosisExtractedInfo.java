package com.petcloud.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diagnosis_extracted_info")
public class DiagnosisExtractedInfo extends BaseEntity {

    @TableField("record_id")
    private Long recordId;

    @TableField("primary_symptoms_json")
    private String primarySymptomsJson;

    @TableField("duration_text")
    private String durationText;

    @TableField("severity")
    private String severity;

    @TableField("suspected_issues_json")
    private String suspectedIssuesJson;

    @TableField("affected_parts_json")
    private String affectedPartsJson;

    @TableField("follow_up_focus_json")
    private String followUpFocusJson;

    @TableField("extract_version")
    private String extractVersion;
}
