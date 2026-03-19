package com.petcloud.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@RequiredArgsConstructor
public enum PrivateMessageType {
    TEXT("text"),
    IMAGE("image"),
    VOICE("voice");

    public static final String DEFAULT_CODE = "text";

    private final String code;

    public static String normalize(String code) {
        if (!StringUtils.hasText(code)) {
            return DEFAULT_CODE;
        }
        for (PrivateMessageType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type.code;
            }
        }
        return DEFAULT_CODE;
    }
}
