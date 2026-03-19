package com.petcloud.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityShareType {
    WECHAT("wechat");

    public static final String DEFAULT_CODE = "wechat";

    private final String code;

    public static String normalize(String code) {
        if (WECHAT.code.equalsIgnoreCase(code)) {
            return WECHAT.code;
        }
        return WECHAT.code;
    }
}
