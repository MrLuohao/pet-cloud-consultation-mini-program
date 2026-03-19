package com.petcloud.user.infrastructure.feign.dto;

import lombok.Data;

import java.util.List;

@Data
public class MediaAssetVO {

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
