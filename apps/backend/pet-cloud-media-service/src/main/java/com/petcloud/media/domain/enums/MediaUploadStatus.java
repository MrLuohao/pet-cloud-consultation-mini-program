package com.petcloud.media.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaUploadStatus {
    UPLOADED("uploaded"),
    FAILED("failed");

    private final String code;
}
