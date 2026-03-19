package com.petcloud.shop.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShopDomainEnumTest {

    @Test
    void shouldFallbackToAllWhenReviewFilterUnknown() {
        assertEquals(ProductReviewFilterType.WITH_IMAGES, ProductReviewFilterType.fromCode("withImages"));
        assertEquals(ProductReviewFilterType.ALL, ProductReviewFilterType.fromCode("unknown"));
    }

    @Test
    void shouldResolveShopCategoryVisualMeta() {
        assertEquals("shop-food", ShopCategoryVisualMeta.resolve("主粮专区", 0).getIconKey());
        assertEquals("shop-health-active", ShopCategoryVisualMeta.resolve("营养保健", 1).getActiveIconKey());
        assertEquals("shop-toy", ShopCategoryVisualMeta.resolve(null, 2).getIconKey());
        assertEquals("shop-care", ShopCategoryVisualMeta.resolve("", 99).getIconKey());
    }
}
