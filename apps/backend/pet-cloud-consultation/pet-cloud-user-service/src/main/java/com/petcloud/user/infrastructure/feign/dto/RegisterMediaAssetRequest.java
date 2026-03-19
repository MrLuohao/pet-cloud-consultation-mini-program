package com.petcloud.user.infrastructure.feign.dto;

import lombok.Data;

@Data
public class RegisterMediaAssetRequest {

    private Long userId;
    private String ownerType;
    private Long ownerId;
    private String mediaType;
    private String url;
    private String mimeType;
    private Long fileSize;
    private String originalFilename;
}
