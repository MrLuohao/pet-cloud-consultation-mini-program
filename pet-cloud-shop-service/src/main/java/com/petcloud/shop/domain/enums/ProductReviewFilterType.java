package com.petcloud.shop.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductReviewFilterType {
    ALL("all"),
    GOOD("good"),
    BAD("bad"),
    WITH_IMAGES("withImages");

    public static final String DEFAULT_CODE = "all";

    private final String code;

    public static ProductReviewFilterType fromCode(String code) {
        for (ProductReviewFilterType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return ALL;
    }
}
