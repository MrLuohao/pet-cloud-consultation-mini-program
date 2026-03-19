package com.petcloud.shop.application.impl;

import com.petcloud.shop.domain.entity.OrderInfo;
import com.petcloud.shop.domain.entity.OrderStatusHistory;
import com.petcloud.shop.domain.service.OrderService;
import com.petcloud.shop.domain.vo.OrderTimelineVO;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderInfoMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderItemMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderStatusHistoryMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.PaymentRecordMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductReviewMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ShoppingCartMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.UserAddressMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.UserCouponMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceImplShipmentTest {

    @Test
    void shouldShipPendingShipmentOrderAndRecordTimeline() {
      OrderInfo order = new OrderInfo();
      order.setId(9001L);
      order.setUserId(7L);
      order.setStatus(OrderInfo.Status.PENDING_SHIPMENT.getCode());

      List<OrderStatusHistory> insertedHistory = new ArrayList<>();

      OrderService service = new OrderServiceImpl(
              proxy(ProductMapper.class, unsupported()),
              proxy(OrderInfoMapper.class, (method, args) -> {
                  if ("selectById".equals(method.getName())) {
                      return order;
                  }
                  if ("updateById".equals(method.getName())) {
                      return 1;
                  }
                  return null;
              }),
              proxy(OrderItemMapper.class, unsupported()),
              proxy(ShoppingCartMapper.class, unsupported()),
              proxy(UserCouponMapper.class, unsupported()),
              proxy(PaymentRecordMapper.class, unsupported()),
              proxy(UserAddressMapper.class, unsupported()),
              proxy(ProductReviewMapper.class, unsupported()),
              proxy(OrderStatusHistoryMapper.class, (method, args) -> {
                  if ("insert".equals(method.getName())) {
                      insertedHistory.add((OrderStatusHistory) args[0]);
                      return 1;
                  }
                  if ("selectList".equals(method.getName())) {
                      return insertedHistory;
                  }
                  return null;
              })
      );

      service.shipOrder(9001L, "SF", "SF10086", "冷链发货", 1001L, "运营后台");

      assertEquals(OrderInfo.Status.PENDING_RECEIPT.getCode(), order.getStatus());
      assertNotNull(order.getShipTime());
      assertEquals(1, insertedHistory.size());
      assertEquals("ship_order", insertedHistory.get(0).getAction());
      assertEquals("SF10086", insertedHistory.get(0).getTrackingNo());
      assertEquals(OrderInfo.Status.PENDING_SHIPMENT.getCode(), insertedHistory.get(0).getFromStatus());
      assertEquals(OrderInfo.Status.PENDING_RECEIPT.getCode(), insertedHistory.get(0).getToStatus());
    }

    @Test
    void shouldReturnTimelineOrderedByOperateTimeDesc() {
        OrderStatusHistory newer = new OrderStatusHistory();
        newer.setOrderId(9001L);
        newer.setAction("ship_order");
        newer.setOperateTime(new Date(2_000L));
        newer.setFromStatus(OrderInfo.Status.PENDING_SHIPMENT.getCode());
        newer.setToStatus(OrderInfo.Status.PENDING_RECEIPT.getCode());
        newer.setRemark("冷链发货");

        OrderStatusHistory earlier = new OrderStatusHistory();
        earlier.setOrderId(9001L);
        earlier.setAction("submit_order");
        earlier.setOperateTime(new Date(1_000L));
        earlier.setFromStatus(null);
        earlier.setToStatus(OrderInfo.Status.UNPAID.getCode());
        earlier.setRemark("用户提交订单");

        OrderService service = new OrderServiceImpl(
                proxy(ProductMapper.class, unsupported()),
                proxy(OrderInfoMapper.class, unsupported()),
                proxy(OrderItemMapper.class, unsupported()),
                proxy(ShoppingCartMapper.class, unsupported()),
                proxy(UserCouponMapper.class, unsupported()),
                proxy(PaymentRecordMapper.class, unsupported()),
                proxy(UserAddressMapper.class, unsupported()),
                proxy(ProductReviewMapper.class, unsupported()),
                proxy(OrderStatusHistoryMapper.class, (method, args) -> {
                    if ("selectList".equals(method.getName())) {
                        return List.of(newer, earlier);
                    }
                    return null;
                })
        );

        List<OrderTimelineVO> timeline = service.getOrderTimeline(9001L);

        assertEquals(2, timeline.size());
        assertEquals("ship_order", timeline.get(0).getAction());
        assertEquals("submit_order", timeline.get(1).getAction());
        assertEquals("待收货", timeline.get(0).getToStatusDesc());
        assertTrue(timeline.get(0).getOperateTime().after(timeline.get(1).getOperateTime()));
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
