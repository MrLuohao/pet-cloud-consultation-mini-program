package com.petcloud.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum VipPlanType {
    MONTHLY("monthly", "月卡", 2990, new BigDecimal("19.9"), new BigDecimal("29.9"), 30),
    QUARTERLY("quarterly", "季卡", 7990, new BigDecimal("49.9"), new BigDecimal("89.7"), 90),
    YEARLY("yearly", "年卡", 29900, new BigDecimal("149.9"), new BigDecimal("358.8"), 365);

    public static final String DEFAULT_CODE = "monthly";

    private final String code;
    private final String displayName;
    private final int feeInCent;
    private final BigDecimal price;
    private final BigDecimal originalPrice;
    private final int durationDays;

    public static VipPlanType fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
