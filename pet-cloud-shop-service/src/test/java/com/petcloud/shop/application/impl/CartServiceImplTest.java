package com.petcloud.shop.application.impl;

import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ShoppingCart;
import com.petcloud.shop.domain.service.CartService;
import com.petcloud.shop.domain.vo.CartGroupVO;
import com.petcloud.shop.domain.vo.CartPageVO;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductCategoryMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ShoppingCartMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartServiceImplTest {

    @Test
    void shouldSnapshotExplicitSpecLabelWhenAddingToCart() {
        Product product = new Product();
        product.setId(101L);
        product.setName("猫咪冻干主食");
        product.setPrice(new BigDecimal("89.00"));
        product.setOriginalPrice(new BigDecimal("109.00"));
        product.setStock(12);
        product.setStatus(Product.Status.ONLINE.getCode());
        product.setShopId("official");
        product.setShopName("伴宠云诊自营");
        product.setServiceText("包邮 · 正品保障");
        product.setDefaultSpec("冻干桶装 400g");

        List<ShoppingCart> insertedCarts = new ArrayList<>();

        CartService service = new CartServiceImpl(
                proxy(ShoppingCartMapper.class, (method, args) -> {
                    if ("selectOne".equals(method.getName())) {
                        return null;
                    }
                    if ("insert".equals(method.getName())) {
                        insertedCarts.add((ShoppingCart) args[0]);
                        return 1;
                    }
                    return null;
                }),
                proxy(ProductMapper.class, (method, args) -> product),
                proxy(ProductCategoryMapper.class, (method, args) -> null)
        );

        service.addToCart(9L, 101L, 1, "冻干桶装 400g · 幼年期");

        assertEquals(1, insertedCarts.size());
        assertEquals("冻干桶装 400g · 幼年期", insertedCarts.get(0).getSpecLabel());
    }

    @Test
    void shouldBuildGroupedCartPageFromMerchantSnapshots() {
        ShoppingCart cart1 = new ShoppingCart();
        cart1.setId(1L);
        cart1.setUserId(9L);
        cart1.setProductId(101L);
        cart1.setQuantity(2);
        cart1.setShopId("official");
        cart1.setShopName("伴宠云诊自营");
        cart1.setServiceText("包邮 · 正品保障");
        cart1.setSpecLabel("冻干桶装 400g");
        cart1.setSelected(1);
        cart1.setStatus("active");
        cart1.setPriceSnapshot(new BigDecimal("89.00"));
        cart1.setOriginalPriceSnapshot(new BigDecimal("109.00"));

        ShoppingCart cart2 = new ShoppingCart();
        cart2.setId(2L);
        cart2.setUserId(9L);
        cart2.setProductId(102L);
        cart2.setQuantity(1);
        cart2.setShopId("brand");
        cart2.setShopName("合作品牌馆");
        cart2.setServiceText("极速达");
        cart2.setSpecLabel("洁齿骨 6 支");
        cart2.setSelected(0);
        cart2.setStatus("invalid");
        cart2.setPriceSnapshot(new BigDecimal("49.00"));
        cart2.setOriginalPriceSnapshot(new BigDecimal("59.00"));

        Product product1 = new Product();
        product1.setId(101L);
        product1.setName("猫咪冻干主食");
        product1.setCoverUrl("https://img/p1.png");
        product1.setPrice(new BigDecimal("89.00"));
        product1.setOriginalPrice(new BigDecimal("109.00"));
        product1.setStock(12);
        product1.setStatus(Product.Status.ONLINE.getCode());

        Product product2 = new Product();
        product2.setId(102L);
        product2.setName("狗狗洁齿骨");
        product2.setCoverUrl("https://img/p2.png");
        product2.setPrice(new BigDecimal("49.00"));
        product2.setOriginalPrice(new BigDecimal("59.00"));
        product2.setStock(0);
        product2.setStatus(Product.Status.ONLINE.getCode());

        CartService service = new CartServiceImpl(
                proxy(ShoppingCartMapper.class, (method, args) -> List.of(cart1, cart2)),
                proxy(ProductMapper.class, (method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        Long productId = (Long) args[0];
                        if (Long.valueOf(101L).equals(productId)) {
                            return product1;
                        }
                        if (Long.valueOf(102L).equals(productId)) {
                            return product2;
                        }
                    }
                    return null;
                }),
                proxy(ProductCategoryMapper.class, (method, args) -> null)
        );

        CartPageVO result = service.getCartList(9L);

        assertEquals(1, result.getCartGroups().size());
        CartGroupVO group = result.getCartGroups().get(0);
        assertEquals("伴宠云诊自营", group.getMerchantName());
        assertEquals(1, group.getItems().size());
        assertEquals("冻干桶装 400g", group.getItems().get(0).getSpec());
        assertTrue(group.getItems().get(0).getSelected());
        assertEquals(1, result.getInvalidItems().size());
        assertEquals("invalid", result.getInvalidItems().get(0).getStatus());
        assertEquals(2, result.getSummary().getSelectedCount());
        assertEquals(new BigDecimal("178.00"), result.getSummary().getTotalAmount());
        assertFalse(result.getSummary().getAllSelected());
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
