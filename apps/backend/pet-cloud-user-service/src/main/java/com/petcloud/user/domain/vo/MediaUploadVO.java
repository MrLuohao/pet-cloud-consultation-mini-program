package com.petcloud.user.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MediaUploadVO {

    private Long assetId;
    private String assetNo;
    private String url;
    private String mediaType;
    private String uploadStatus;
    private String moderationStatus;
    private Boolean availableForSubmit;
    private List<String> riskTags;
    private String reason;
}
