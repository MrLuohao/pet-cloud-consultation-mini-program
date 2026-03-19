package com.petcloud.media.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("media_asset")
public class MediaAsset extends BaseEntity {

    @TableField("asset_no")
    private String assetNo;

    @TableField("owner_type")
    private String ownerType;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("user_id")
    private Long userId;

    @TableField("media_type")
    private String mediaType;

    @TableField("url")
    private String url;

    @TableField("mime_type")
    private String mimeType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("upload_status")
    private String uploadStatus;

    @TableField("moderation_status")
    private String moderationStatus;

    @TableField("risk_tags_json")
    private String riskTagsJson;

    @TableField("reason")
    private String reason;
}
