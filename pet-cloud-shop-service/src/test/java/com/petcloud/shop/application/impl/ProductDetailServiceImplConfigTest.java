package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.service.UserRemoteService;
import com.petcloud.shop.domain.vo.ProductDetailVO;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderInfoMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderItemMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductCategoryMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductReviewLikeMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductReviewMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductDetailServiceImplConfigTest {

    @Test
    void shouldExposeConfiguredSpecGroupsAndDetailContent() {
        Product product = new Product();
        product.setId(101L);
        product.setName("低温烘焙鸡肉主粮");
        product.setSummary("稳定、轻负担的日常主食选择。");
        product.setPrice(new BigDecimal("129.00"));
        product.setCoverUrl("https://img/p1.jpg");
        product.setSpecGroupsJson("[{\"key\":\"capacity\",\"label\":\"规格\",\"selectedValue\":\"400g\",\"options\":[{\"value\":\"400g\",\"label\":\"400g\",\"hint\":\"试吃装\"}]},{\"key\":\"lifeStage\",\"label\":\"适用阶段\",\"selectedValue\":\"全阶段\",\"options\":[{\"value\":\"全阶段\",\"label\":\"全阶段\",\"hint\":\"通用\"}]}]");
        product.setDetailContentJson("{\"highlights\":[\"低温烘焙，保留更自然的风味与适口性。\"],\"storySections\":[{\"title\":\"温和日常配方\",\"description\":\"适合作为稳定、轻负担的日常主食选择。\",\"imageUrl\":\"https://img/story-1.jpg\"}],\"usageNote\":{\"title\":\"适用对象 / 使用建议\",\"content\":\"适合全阶段猫咪日常喂养。\"}}");

        ProductDetailServiceImpl service = new ProductDetailServiceImpl(
                proxy(ProductMapper.class, (method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return product;
                    }
                    return null;
                }),
                proxy(ProductCategoryMapper.class, (method, args) -> null),
                proxy(ProductReviewMapper.class, (method, args) -> {
                    if ("selectPage".equals(method.getName())) {
                        Page<?> page = (Page<?>) args[0];
                        page.setRecords(Collections.emptyList());
                        return page;
                    }
                    return null;
                }),
                proxy(ProductReviewLikeMapper.class, (method, args) -> null),
                proxy(OrderItemMapper.class, (method, args) -> null),
                proxy(OrderInfoMapper.class, (method, args) -> null),
                proxy(UserRemoteService.class, (method, args) -> Collections.emptyMap())
        );

        ProductDetailVO detail = service.getProductDetail(101L);

        assertNotNull(detail);
        assertEquals(2, detail.getSpecGroups().size());
        assertEquals("规格", detail.getSpecGroups().get(0).getLabel());
        assertEquals("400g", detail.getSpecGroups().get(0).getSelectedValue());
        assertEquals(1, detail.getHighlights().size());
        assertEquals(1, detail.getStorySections().size());
        assertEquals("适用对象 / 使用建议", detail.getUsageNote().getTitle());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> invocation.invoke(method, args)
        );
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
