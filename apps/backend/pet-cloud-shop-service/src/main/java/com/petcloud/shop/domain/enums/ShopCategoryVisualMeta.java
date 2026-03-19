package com.petcloud.shop.domain.enums;

import java.util.Locale;

/**
 * 商城分类视觉元数据
 */
public enum ShopCategoryVisualMeta {

    FOOD("shop-food", "shop-food-active"),
    CARE("shop-care", "shop-care-active"),
    TOY("shop-toy", "shop-toy-active"),
    HEALTH("shop-health", "shop-health-active");

    private final String iconKey;
    private final String activeIconKey;

    ShopCategoryVisualMeta(String iconKey, String activeIconKey) {
        this.iconKey = iconKey;
        this.activeIconKey = activeIconKey;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getActiveIconKey() {
        return activeIconKey;
    }

    public static ShopCategoryVisualMeta resolve(String categoryName, int index) {
        String normalized = categoryName == null ? "" : categoryName.trim().toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "粮", "食", "零食", "冻干", "罐头")) {
            return FOOD;
        }
        if (containsAny(normalized, "玩", "球", "逗猫", "玩具")) {
            return TOY;
        }
        if (containsAny(normalized, "药", "医", "保健", "营养", "驱虫")) {
            return HEALTH;
        }
        if (containsAny(normalized, "用品", "清洁", "服饰", "牵引", "窝", "砂")) {
            return CARE;
        }

        ShopCategoryVisualMeta[] values = values();
        if (index >= 0 && index < values.length) {
            return values[index];
        }
        return CARE;
    }

    private static boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
