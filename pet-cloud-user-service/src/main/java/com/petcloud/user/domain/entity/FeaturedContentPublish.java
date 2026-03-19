package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("featured_content_publish")
public class FeaturedContentPublish extends BaseEntity {

    @TableField("publish_date")
    private LocalDate publishDate;

    @TableField("position_no")
    private Integer positionNo;

    @TableField("draft_id")
    private Long draftId;

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

    @TableField("target_page")
    private String targetPage;

    @TableField("target_id")
    private Long targetId;

    @TableField("status")
    private String status;

    @TableField("start_time")
    private Date startTime;

    @TableField("end_time")
    private Date endTime;

    public enum Status {
        PUBLISHED("published", "已发布"),
        OFFLINE("offline", "已下线");

        private final String code;
        private final String desc;

        Status(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
