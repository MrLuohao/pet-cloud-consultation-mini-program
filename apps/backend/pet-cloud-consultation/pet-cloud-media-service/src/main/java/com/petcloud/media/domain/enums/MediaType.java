package com.petcloud.media.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum MediaType {
    IMAGE("image"),
    VIDEO("video");

    private final String code;

    public static MediaType fromMimeType(String mimeType) {
        if (StringUtils.hasText(mimeType) && mimeType.toLowerCase(Locale.ROOT).startsWith("video/")) {
            return VIDEO;
        }
        return IMAGE;
    }
}
