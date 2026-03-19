package com.petcloud.shop.application.impl;

import com.petcloud.shop.domain.dto.OrderSubmitDTO;
import com.petcloud.shop.domain.entity.OrderItem;
import com.petcloud.shop.domain.entity.PaymentRecord;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.UserCoupon;
import com.petcloud.shop.domain.service.OrderService;
import com.petcloud.shop.domain.vo.OrderConfirmVO;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderInfoMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderItemMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderStatusHistoryMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.PaymentRecordMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductReviewMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ShoppingCartMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.UserAddressMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.UserCouponMapper;
import com.petcloud.shop.domain.entity.UserAddress;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceImplCheckoutTest {

    @Test
    void shouldBuildCheckoutDraftWithPaymentMethodsAndAmountSummary() {
        Product product = new Product();
        product.setId(101L);
        product.setName("猫咪冻干主食");
        product.setCoverUrl("https://img/p1.png");
        product.setPrice(new BigDecimal("89.00"));
        product.setOriginalPrice(new BigDecimal("109.00"));
        product.setStock(20);
        product.setStatus(Product.Status.ONLINE.getCode());
        product.setShopId("official");
        product.setShopName("伴宠云诊自营");
        product.setServiceText("包邮 · 正品保障");
        product.setDefaultSpec("冻干桶装 400g");

        UserAddress address = new UserAddress();
        address.setId(88L);
        address.setUserId(9L);
        address.setContactName("张宁");
        address.setContactPhone("13812345678");
        address.setProvince("上海市");
        address.setCity("上海市");
        address.setDistrict("浦东新区");
        address.setDetailAddress("世纪大道1188号2栋1702");
        address.setIsDefault(1);
        address.setLongitude(new BigDecimal("121.506300"));
        address.setLatitude(new BigDecimal("31.245520"));
        address.setBusinessArea("陆家嘴");
        address.setDoorNo("2栋1702");
        address.setMapAddress("上海市浦东新区世纪大道1188号");

        UserCoupon coupon = new UserCoupon();
        coupon.setId(501L);
        coupon.setCouponId(301L);
        coupon.setCouponName("满199减30");
        coupon.setCouponType(1);
        coupon.setDiscountAmount(new BigDecimal("30.00"));
        coupon.setMinAmount(new BigDecimal("199.00"));
        coupon.setStatus(0);
        coupon.setExpireTime("2099-01-01 00:00:00");

        OrderService service = new OrderServiceImpl(
                proxy(ProductMapper.class, (method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return product;
                    }
                    if ("updateById".equals(method.getName())) {
                        return 1;
                    }
                    return null;
                }),
                proxy(OrderInfoMapper.class, unsupported()),
                proxy(OrderItemMapper.class, unsupported()),
                proxy(ShoppingCartMapper.class, (method, args) -> List.of()),
                proxy(UserCouponMapper.class, (method, args) -> List.of(coupon)),
                proxy(PaymentRecordMapper.class, unsupported()),
                proxy(UserAddressMapper.class, (method, args) -> address),
                proxy(ProductReviewMapper.class, unsupported()),
                proxy(OrderStatusHistoryMapper.class, unsupported())
        );

        OrderConfirmVO result = service.getOrderConfirm(9L, List.of(101L), List.of(3), List.of(), List.of("冻干桶装 400g · 幼年期"));

        assertNotNull(result.getAddress());
        assertEquals("张宁", result.getAddress().getReceiverName());
        assertEquals(new BigDecimal("267.00"), result.getGoodsAmount());
        assertEquals(new BigDecimal("0.00"), result.getCouponDiscount());
        assertEquals(new BigDecimal("267.00"), result.getPayAmount());
        assertEquals("顺丰冷链", result.getDeliveryText());
        assertTrue(result.getPaymentMethods().stream().anyMatch(method -> "alipay".equals(method.getKey())));
        assertEquals("wechat", result.getSelectedPaymentMethod());
        assertEquals("订单将于提交后创建，请在 30 分钟内完成支付。", result.getOrderHint());
        assertEquals("冻干桶装 400g · 幼年期", result.getItems().get(0).getSpec());
    }

    @Test
    void shouldPersistSelectedPaymentMethodWhenSubmittingOrder() {
        Product product = new Product();
        product.setId(101L);
        product.setName("猫咪冻干主食");
        product.setCoverUrl("https://img/p1.png");
        product.setPrice(new BigDecimal("89.00"));
        product.setOriginalPrice(new BigDecimal("109.00"));
        product.setStock(20);
        product.setStatus(Product.Status.ONLINE.getCode());
        product.setShopId("official");
        product.setShopName("伴宠云诊自营");
        product.setServiceText("包邮 · 正品保障");
        product.setDefaultSpec("冻干桶装 400g");

        UserAddress address = new UserAddress();
        address.setId(88L);
        address.setUserId(9L);
        address.setContactName("张宁");
        address.setContactPhone("13812345678");
        address.setProvince("上海市");
        address.setCity("上海市");
        address.setDistrict("浦东新区");
        address.setDetailAddress("世纪大道1188号2栋1702");
        address.setIsDefault(1);

        List<PaymentRecord> insertedPaymentRecords = new ArrayList<>();
        List<OrderItem> insertedOrderItems = new ArrayList<>();

        OrderService service = new OrderServiceImpl(
                proxy(ProductMapper.class, (method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return product;
                    }
                    if ("updateById".equals(method.getName())) {
                        return 1;
                    }
                    return null;
                }),
                proxy(OrderInfoMapper.class, (method, args) -> 1),
                proxy(OrderItemMapper.class, (method, args) -> {
                    if ("insert".equals(method.getName())) {
                        insertedOrderItems.add((OrderItem) args[0]);
                    }
                    return 1;
                }),
                proxy(ShoppingCartMapper.class, (method, args) -> List.of()),
                proxy(UserCouponMapper.class, (method, args) -> null),
                proxy(PaymentRecordMapper.class, (method, args) -> {
                    if ("insert".equals(method.getName())) {
                        insertedPaymentRecords.add((PaymentRecord) args[0]);
                    }
                    return 1;
                }),
                proxy(UserAddressMapper.class, (method, args) -> address),
                proxy(ProductReviewMapper.class, unsupported()),
                proxy(OrderStatusHistoryMapper.class, (method, args) -> 1)
        );

        OrderSubmitDTO dto = new OrderSubmitDTO();
        dto.setProductIds(List.of(101L));
        dto.setQuantities(List.of(2));
        dto.setSpecLabels(List.of("冻干桶装 400g · 幼年期"));
        dto.setAddressId(88L);
        dto.setPaymentMethod("alipay");
        dto.setVerificationType("password");

        service.submitOrder(9L, dto);

        assertEquals(1, insertedPaymentRecords.size());
        assertEquals(1, insertedOrderItems.size());
        assertEquals("alipay", insertedPaymentRecords.get(0).getPaymentMethod());
        assertEquals("password", insertedPaymentRecords.get(0).getVerifyType());
        assertEquals("冻干桶装 400g · 幼年期", insertedOrderItems.get(0).getSpecSnapshot());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> invocation.invoke(method, args)
        );
    }

    private Invocation unsupported() {
        return (method, args) -> null;
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
