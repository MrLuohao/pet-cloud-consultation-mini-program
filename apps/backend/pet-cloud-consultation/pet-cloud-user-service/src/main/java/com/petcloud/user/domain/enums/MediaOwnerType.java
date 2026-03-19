package com.petcloud.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@RequiredArgsConstructor
public enum MediaOwnerType {
    DIAGNOSIS("diagnosis");

    public static final String DEFAULT_CODE = "diagnosis";

    private final String code;

    public static String normalize(String code) {
        if (!StringUtils.hasText(code)) {
            return DEFAULT_CODE;
        }
        for (MediaOwnerType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type.code;
            }
        }
        return code;
    }
}
