package com.petcloud.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityPostQueryType {
    LATEST("latest"),
    HOT("hot"),
    FOLLOWING("following");

    public static final String DEFAULT_CODE = "latest";

    private final String code;

    public static CommunityPostQueryType fromCode(String code) {
        for (CommunityPostQueryType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return LATEST;
    }
}
