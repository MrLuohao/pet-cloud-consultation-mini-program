package com.petcloud.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("featured_content_draft")
public class FeaturedContentDraft extends BaseEntity {

    @TableField("draft_date")
    private Date draftDate;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_id")
    private Long sourceId;

    @TableField("title")
    private String title;

    @TableField("summary")
    private String summary;

    @TableField("tag")
    private String tag;

    @TableField("reason_label")
    private String reasonLabel;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("ranking_score")
    private Integer rankingScore;

    @TableField("status")
    private String status;
}
