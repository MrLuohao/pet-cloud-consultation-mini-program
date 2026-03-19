package com.petcloud.shop.application.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.shop.domain.dto.OrderSubmitDTO;
import com.petcloud.shop.domain.entity.*;
import com.petcloud.shop.domain.service.OrderService;
import com.petcloud.shop.domain.vo.*;
import com.petcloud.shop.infrastructure.persistence.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductMapper productMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final UserCouponMapper userCouponMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final UserAddressMapper userAddressMapper;
    private final ProductReviewMapper productReviewMapper;
    private final OrderStatusHistoryMapper orderStatusHistoryMapper;

    @Override
    public OrderConfirmVO getOrderConfirm(Long userId, List<Long> productIds, List<Integer> quantities, List<Long> cartIds,
                                          List<String> specLabels) {
        List<OrderItemVO> items = buildCheckoutItems(userId, productIds, quantities, cartIds, specLabels, false);
        BigDecimal goodsAmount = items.stream()
                .map(OrderItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        AddressVO addressVO = buildDefaultAddress(userId);
        List<UserCouponVO> availableCoupons = loadAvailableCoupons(userId, goodsAmount);
        BigDecimal freight = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return OrderConfirmVO.builder()
                .items(items)
                .address(addressVO)
                .totalCount(items.stream().mapToInt(OrderItemVO::getQuantity).sum())
                .totalAmount(goodsAmount)
                .goodsAmount(goodsAmount)
                .freight(freight)
                .couponDiscount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .payAmount(goodsAmount.add(freight))
                .availableCoupons(availableCoupons)
                .deliveryText("顺丰冷链")
                .orderHint("订单将于提交后创建，请在 30 分钟内完成支付。")
                .selectedPaymentMethod(PaymentRecord.PaymentMethod.WECHAT.getCode())
                .paymentMethods(buildPaymentMethods())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitOrder(Long userId, OrderSubmitDTO dto) {
        UserAddress address = userAddressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ADDRESS_NOT_FOUND);
        }

        List<OrderItemVO> items = buildCheckoutItems(
                userId,
                dto.getProductIds(),
                dto.getQuantities(),
                dto.getCartIds(),
                dto.getSpecLabels(),
                true
        );
        BigDecimal totalAmount = items.stream()
                .map(OrderItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal couponDiscount = calculateCouponDiscount(dto.getCouponId(), userId, totalAmount);
        BigDecimal payAmount = totalAmount.subtract(couponDiscount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        String orderNo = generateOrderNo();
        OrderInfo order = new OrderInfo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setReceiverName(address.getContactName());
        order.setReceiverPhone(address.getContactPhone());
        order.setReceiverAddress(address.getFullAddress());
        order.setRemark(dto.getRemark());
        order.setCouponId(dto.getCouponId());
        order.setCouponDiscount(couponDiscount);
        order.setStatus(OrderInfo.Status.UNPAID.getCode());
        orderInfoMapper.insert(order);
        recordOrderStatusHistory(order, null, OrderInfo.Status.UNPAID.getCode(), "submit_order",
                "user", userId, "用户下单", null, null, "用户提交订单");

        for (OrderItemVO item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setCoverUrl(item.getCoverUrl());
            orderItem.setShopId(item.getShopId());
            orderItem.setShopName(item.getShopName());
            orderItem.setServiceText(item.getServiceText());
            orderItem.setSpecSnapshot(item.getSpec());
            orderItem.setSkuSnapshot(item.getSpec());
            orderItem.setPrice(item.getPrice());
            orderItem.setOriginalPrice(item.getOriginalPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(item.getSubtotal());
            orderItemMapper.insert(orderItem);

            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() - item.getQuantity());
                product.setSales((product.getSales() != null ? product.getSales() : 0) + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        if (dto.getCouponId() != null && couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
            UserCoupon userCoupon = userCouponMapper.selectById(dto.getCouponId());
            if (userCoupon != null) {
                userCoupon.setStatus(1);
                userCoupon.setUseTime(nowString());
                userCoupon.setOrderId(order.getId());
                userCouponMapper.updateById(userCoupon);
            }
        }

        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(order.getId());
        paymentRecord.setOrderNo(orderNo);
        paymentRecord.setUserId(userId);
        paymentRecord.setAmount(payAmount);
        paymentRecord.setPaymentMethod(defaultPaymentMethod(dto.getPaymentMethod()));
        paymentRecord.setPaymentChannel(defaultPaymentMethod(dto.getPaymentMethod()));
        paymentRecord.setVerifyType(dto.getVerificationType() != null ? dto.getVerificationType() : "face");
        paymentRecord.setStatusDetail("待支付");
        paymentRecord.setClientScene("mini_program");
        paymentRecord.setStatus(PaymentRecord.Status.PENDING.getCode());
        paymentRecordMapper.insert(paymentRecord);

        if (dto.getCartIds() != null && !dto.getCartIds().isEmpty()) {
            for (Long cartId : dto.getCartIds()) {
                shoppingCartMapper.deleteById(cartId);
            }
        }

        return order.getId();
    }

    @Override
    public List<OrderDetailVO> getOrderList(Long userId, Integer status) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getUserId, userId);
        if (status != null) {
            queryWrapper.eq(OrderInfo::getStatus, status);
        }
        queryWrapper.orderByDesc(OrderInfo::getCreateTime);
        List<OrderInfo> orders = orderInfoMapper.selectList(queryWrapper);

        return orders.stream()
                .map(order -> {
                    // 获取订单项
                    LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                    itemWrapper.eq(OrderItem::getOrderId, order.getId());
                    List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

                    List<OrderItemVO> itemVOs = items.stream()
                            .map(item -> OrderItemVO.builder()
                                    .productId(item.getProductId())
                                    .productName(item.getProductName())
                                    .coverUrl(item.getCoverUrl())
                                    .shopId(item.getShopId())
                                    .shopName(item.getShopName())
                                    .serviceText(item.getServiceText())
                                    .spec(item.getSpecSnapshot())
                                    .price(item.getPrice())
                                    .originalPrice(item.getOriginalPrice())
                                    .quantity(item.getQuantity())
                                    .subtotal(item.getSubtotal())
                                    .build())
                            .collect(Collectors.toList());

                    return OrderDetailVO.builder()
                            .id(order.getId())
                            .orderNo(order.getOrderNo())
                            .status(order.getStatus())
                            .statusDesc(getStatusDesc(order.getStatus()))
                            .totalAmount(order.getTotalAmount())
                            .couponDiscount(order.getCouponDiscount() != null ? order.getCouponDiscount() : BigDecimal.ZERO)
                            .payAmount(order.getPayAmount())
                            .receiverName(order.getReceiverName())
                            .receiverPhone(order.getReceiverPhone())
                            .receiverAddress(order.getReceiverAddress())
                            .remark(order.getRemark())
                            .createTime(order.getCreateTime())
                            .payTime(order.getPayTime())
                            .items(itemVOs)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long userId, Long orderId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ORDER_NOT_FOUND);
        }

        // 获取订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        // 获取已评价的订单项ID
        Set<Long> reviewedItemIds = new HashSet<>();
        if (OrderInfo.Status.COMPLETED.getCode().equals(order.getStatus())) {
            LambdaQueryWrapper<ProductReview> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.eq(ProductReview::getUserId, userId)
                    .in(ProductReview::getOrderItemId, items.stream().map(OrderItem::getId).collect(Collectors.toList()));
            List<ProductReview> reviews = productReviewMapper.selectList(reviewWrapper);
            reviewedItemIds = reviews.stream()
                    .map(ProductReview::getOrderItemId)
                    .collect(Collectors.toSet());
        }

        final Set<Long> finalReviewedItemIds = reviewedItemIds;
        List<OrderItemVO> itemVOs = items.stream()
                .map(item -> OrderItemVO.builder()
                        .orderItemId(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .coverUrl(item.getCoverUrl())
                        .shopId(item.getShopId())
                        .shopName(item.getShopName())
                        .serviceText(item.getServiceText())
                        .spec(item.getSpecSnapshot())
                        .price(item.getPrice())
                        .originalPrice(item.getOriginalPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .reviewed(finalReviewedItemIds.contains(item.getId()))
                        .build())
                .collect(Collectors.toList());

        return OrderDetailVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .statusDesc(getStatusDesc(order.getStatus()))
                .totalAmount(order.getTotalAmount())
                .couponDiscount(order.getCouponDiscount() != null ? order.getCouponDiscount() : BigDecimal.ZERO)
                .payAmount(order.getPayAmount())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .remark(order.getRemark())
                .createTime(order.getCreateTime())
                .payTime(order.getPayTime())
                .items(itemVOs)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ORDER_NOT_FOUND);
        }

        if (!OrderInfo.Status.UNPAID.getCode().equals(order.getStatus())) {
            throw new BusinessException(RespType.ORDER_STATUS_ERROR);
        }

        order.setStatus(OrderInfo.Status.CANCELLED.getCode());
        orderInfoMapper.updateById(order);
        recordOrderStatusHistory(order, OrderInfo.Status.UNPAID.getCode(), OrderInfo.Status.CANCELLED.getCode(), "cancel_order",
                "user", userId, "用户", null, null, "用户取消订单");

        // 恢复库存
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(product.getSales() - item.getQuantity());
                productMapper.updateById(product);
            }
        }

        // 恢复优惠券
        if (order.getCouponId() != null) {
            UserCoupon userCoupon = userCouponMapper.selectById(order.getCouponId());
            if (userCoupon != null) {
                userCoupon.setStatus(0);
                userCoupon.setUseTime(null);
                userCoupon.setOrderId(null);
                userCouponMapper.updateById(userCoupon);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long userId, Long orderId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ORDER_NOT_FOUND);
        }

        if (!OrderInfo.Status.PENDING_RECEIPT.getCode().equals(order.getStatus())) {
            throw new BusinessException(RespType.ORDER_STATUS_ERROR);
        }

        order.setStatus(OrderInfo.Status.COMPLETED.getCode());
        order.setReceiveTime(new Date());
        orderInfoMapper.updateById(order);
        recordOrderStatusHistory(order, OrderInfo.Status.PENDING_RECEIPT.getCode(), OrderInfo.Status.COMPLETED.getCode(), "confirm_receive",
                "user", userId, "用户", null, null, "用户确认收货");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payOrder(Long userId, Long orderId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ORDER_NOT_FOUND);
        }

        if (!order.getStatus().equals(OrderInfo.Status.UNPAID.getCode())) {
            throw new BusinessException(RespType.ORDER_STATUS_ERROR);
        }

        // 模拟支付成功
        order.setStatus(OrderInfo.Status.PENDING_SHIPMENT.getCode());
        order.setPayTime(new Date());
        orderInfoMapper.updateById(order);
        recordOrderStatusHistory(order, OrderInfo.Status.UNPAID.getCode(), OrderInfo.Status.PENDING_SHIPMENT.getCode(), "pay_order",
                "payment", userId, "支付系统", null, null, "订单支付成功");

        // 更新支付记录
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getOrderId, orderId)
                .eq(PaymentRecord::getStatus, PaymentRecord.Status.PENDING.getCode());
        PaymentRecord paymentRecord = paymentRecordMapper.selectOne(wrapper);
        if (paymentRecord != null) {
            paymentRecord.setStatus(PaymentRecord.Status.PAID.getCode());
            paymentRecord.setPayTime(nowString());
            paymentRecord.setTransactionId("WX" + IdUtil.getSnowflakeNextIdStr());
            paymentRecordMapper.updateById(paymentRecord);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, String logisticsCompany, String trackingNo, String remark, Long operatorId, String operatorName) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(RespType.ORDER_NOT_FOUND);
        }
        if (!OrderInfo.Status.PENDING_SHIPMENT.getCode().equals(order.getStatus())) {
            throw new BusinessException(RespType.ORDER_STATUS_ERROR);
        }

        order.setStatus(OrderInfo.Status.PENDING_RECEIPT.getCode());
        order.setShipTime(new Date());
        orderInfoMapper.updateById(order);

        recordOrderStatusHistory(order, OrderInfo.Status.PENDING_SHIPMENT.getCode(), OrderInfo.Status.PENDING_RECEIPT.getCode(),
                "ship_order", "admin", operatorId, operatorName, logisticsCompany, trackingNo, remark);
    }

    @Override
    public List<OrderTimelineVO> getOrderTimeline(Long orderId) {
        LambdaQueryWrapper<OrderStatusHistory> wrapper = new LambdaQueryWrapper<OrderStatusHistory>()
                .eq(OrderStatusHistory::getOrderId, orderId)
                .orderByDesc(OrderStatusHistory::getOperateTime)
                .orderByDesc(OrderStatusHistory::getId);
        List<OrderStatusHistory> histories = orderStatusHistoryMapper.selectList(wrapper);
        return histories.stream()
                .map(item -> OrderTimelineVO.builder()
                        .action(item.getAction())
                        .fromStatus(item.getFromStatus())
                        .fromStatusDesc(getStatusDesc(item.getFromStatus()))
                        .toStatus(item.getToStatus())
                        .toStatusDesc(getStatusDesc(item.getToStatus()))
                        .operatorType(item.getOperatorType())
                        .operatorId(item.getOperatorId())
                        .operatorName(item.getOperatorName())
                        .logisticsCompany(item.getLogisticsCompany())
                        .trackingNo(item.getTrackingNo())
                        .remark(item.getRemark())
                        .operateTime(item.getOperateTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Object getOrderCount(Long userId) {
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put("unpaid", 0);
        countMap.put("pendingShipment", 0);
        countMap.put("pendingReceipt", 0);
        countMap.put("completed", 0);
        countMap.put("pendingReview", 0);

        List<OrderInfo> orders = orderInfoMapper.selectList(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getUserId, userId)
        );

        for (OrderInfo order : orders) {
            switch (order.getStatus()) {
                case 0:
                    countMap.put("unpaid", countMap.get("unpaid") + 1);
                    break;
                case 1:
                    countMap.put("pendingShipment", countMap.get("pendingShipment") + 1);
                    break;
                case 2:
                    countMap.put("pendingReceipt", countMap.get("pendingReceipt") + 1);
                    break;
                case 3:
                    // 已完成订单，检查是否有未评价的商品
                    int unreviewedCount = getUnreviewedItemCount(order.getId());
                    countMap.put("pendingReview", countMap.get("pendingReview") + unreviewedCount);
                    countMap.put("completed", countMap.get("completed") + 1);
                    break;
                default:
                    // 未知状态，忽略
                    break;
            }
        }

        return countMap;
    }

    @Override
    public List<PendingReviewOrderVO> getPendingReviewOrders(Long userId, Integer page, Integer size) {
        List<PendingReviewOrderVO> result = new ArrayList<>();

        // 获取已完成的订单
        LambdaQueryWrapper<OrderInfo> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getStatus, OrderInfo.Status.COMPLETED.getCode())
                .orderByDesc(OrderInfo::getCreateTime);
        List<OrderInfo> orders = orderInfoMapper.selectList(orderWrapper);

        for (OrderInfo order : orders) {
            // 获取订单项
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

            if (items.isEmpty()) {
                continue;
            }

            // 获取已评价的订单项ID
            List<Long> itemIds = items.stream().map(OrderItem::getId).collect(Collectors.toList());
            LambdaQueryWrapper<ProductReview> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.in(ProductReview::getOrderItemId, itemIds);
            List<ProductReview> reviews = productReviewMapper.selectList(reviewWrapper);
            Set<Long> reviewedItemIds = reviews.stream()
                    .map(ProductReview::getOrderItemId)
                    .collect(Collectors.toSet());

            // 过滤未评价的商品
            List<PendingReviewOrderVO.PendingReviewItemVO> pendingItems = items.stream()
                    .filter(item -> !reviewedItemIds.contains(item.getId()))
                    .map(item -> PendingReviewOrderVO.PendingReviewItemVO.builder()
                            .orderItemId(item.getId())
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .coverUrl(item.getCoverUrl())
                            .price(item.getPrice())
                            .quantity(item.getQuantity())
                            .reviewed(false)
                            .build())
                    .collect(Collectors.toList());

            // 如果有待评价商品，添加到结果
            if (!pendingItems.isEmpty()) {
                result.add(PendingReviewOrderVO.builder()
                        .orderId(order.getId())
                        .orderNo(order.getOrderNo())
                        .completeTime(order.getReceiveTime() != null ? order.getReceiveTime() : order.getModifyTime())
                        .items(pendingItems)
                        .build());
            }
        }

        // 分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, result.size());
        if (start >= result.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(result.subList(start, end));
    }

    @Override
    public Integer getPendingReviewCount(Long userId) {
        int count = 0;

        // 获取已完成的订单
        LambdaQueryWrapper<OrderInfo> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getStatus, OrderInfo.Status.COMPLETED.getCode());
        List<OrderInfo> orders = orderInfoMapper.selectList(orderWrapper);

        for (OrderInfo order : orders) {
            count += getUnreviewedItemCount(order.getId());
        }

        return count;
    }

    /**
     * 获取订单中未评价的商品数量
     */
    private int getUnreviewedItemCount(Long orderId) {
        // 获取订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        if (items.isEmpty()) {
            return 0;
        }

        // 获取已评价的订单项
        LambdaQueryWrapper<ProductReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.in(ProductReview::getOrderItemId, items.stream().map(OrderItem::getId).collect(Collectors.toList()));
        Long reviewedCount = productReviewMapper.selectCount(reviewWrapper);
        // 防止 NPE：如果 selectCount 返回 null，视为 0
        long count = reviewedCount != null ? reviewedCount : 0L;

        return items.size() - (int) count;
    }

    /**
     * 计算优惠券折扣金额
     */
    private BigDecimal calculateCouponDiscount(Long couponId, Long userId, BigDecimal totalAmount) {
        if (couponId == null) {
            return BigDecimal.ZERO;
        }

        UserCoupon userCoupon = userCouponMapper.selectById(couponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId) || !Integer.valueOf(0).equals(userCoupon.getStatus())) {
            return BigDecimal.ZERO;
        }

        if (isCouponExpired(userCoupon.getExpireTime())) {
            return BigDecimal.ZERO;
        }

        if (userCoupon.getMinAmount() != null && totalAmount.compareTo(userCoupon.getMinAmount()) < 0) {
            return BigDecimal.ZERO;
        }

        if (Integer.valueOf(1).equals(userCoupon.getCouponType())) {
            return userCoupon.getDiscountAmount() != null ? userCoupon.getDiscountAmount() : BigDecimal.ZERO;
        }

        if (Integer.valueOf(2).equals(userCoupon.getCouponType()) && userCoupon.getDiscountRate() != null) {
            BigDecimal discount = totalAmount.multiply(
                BigDecimal.ONE.subtract(userCoupon.getDiscountRate().divide(BigDecimal.valueOf(10), 2, java.math.RoundingMode.HALF_UP)));
            if (userCoupon.getMaxDiscount() != null && discount.compareTo(userCoupon.getMaxDiscount()) > 0) {
                return userCoupon.getMaxDiscount();
            }
            return discount;
        }

        return BigDecimal.ZERO;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + IdUtil.getSnowflakeNextIdStr();
    }

    private List<OrderItemVO> buildCheckoutItems(Long userId, List<Long> productIds, List<Integer> quantities,
                                                 List<Long> cartIds, List<String> specLabels, boolean validateStock) {
        List<OrderItemVO> items = new ArrayList<>();
        if (cartIds != null && !cartIds.isEmpty()) {
            LambdaQueryWrapper<ShoppingCart> cartWrapper = new LambdaQueryWrapper<>();
            cartWrapper.eq(ShoppingCart::getUserId, userId).in(ShoppingCart::getId, cartIds);
            List<ShoppingCart> cartItems = shoppingCartMapper.selectList(cartWrapper);
            for (ShoppingCart cart : cartItems) {
                Product product = requiredProduct(cart.getProductId());
                if (validateStock && product.getStock() < cart.getQuantity()) {
                    throw new BusinessException(RespType.STOCK_INSUFFICIENT);
                }
                items.add(buildOrderItem(product, cart.getQuantity(), cart.getShopId(), cart.getShopName(),
                        cart.getServiceText(), cart.getSpecLabel(),
                        cart.getPriceSnapshot(), cart.getOriginalPriceSnapshot()));
            }
            return items;
        }

        if (productIds == null) {
            return items;
        }
        for (int i = 0; i < productIds.size(); i++) {
            Product product = requiredProduct(productIds.get(i));
            int quantity = quantities != null && i < quantities.size() ? quantities.get(i) : 1;
            if (validateStock && product.getStock() < quantity) {
                throw new BusinessException(RespType.STOCK_INSUFFICIENT);
            }
            String selectedSpec = resolveDirectBuySpecLabel(product, specLabels, i);
            items.add(buildOrderItem(product, quantity, product.getShopId(), product.getShopName(),
                    product.getServiceText(), selectedSpec, product.getPrice(), product.getOriginalPrice()));
        }
        return items;
    }

    private OrderItemVO buildOrderItem(Product product, Integer quantity, String shopId, String shopName,
                                       String serviceText, String spec, BigDecimal priceSnapshot, BigDecimal originalPriceSnapshot) {
        BigDecimal price = priceSnapshot != null ? priceSnapshot : product.getPrice();
        BigDecimal originalPrice = originalPriceSnapshot != null
                ? originalPriceSnapshot
                : (product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getPrice());
        return OrderItemVO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .coverUrl(product.getCoverUrl())
                .shopId(defaultString(shopId, product.getShopId(), "official"))
                .shopName(defaultString(shopName, product.getShopName(), "伴宠云诊自营"))
                .serviceText(defaultString(serviceText, product.getServiceText(), "包邮 · 正品保障"))
                .spec(defaultString(spec, product.getDefaultSpec(), "默认规格"))
                .price(price)
                .originalPrice(originalPrice)
                .quantity(quantity)
                .subtotal(price.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }

    private Product requiredProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(RespType.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    private String resolveDirectBuySpecLabel(Product product, List<String> specLabels, int index) {
        String selectedSpec = specLabels != null && index < specLabels.size() ? specLabels.get(index) : null;
        return defaultString(selectedSpec, product.getDefaultSpec(), "默认规格");
    }

    private AddressVO buildDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> addressWrapper = new LambdaQueryWrapper<>();
        addressWrapper.eq(UserAddress::getUserId, userId).eq(UserAddress::getIsDefault, 1);
        UserAddress defaultAddress = userAddressMapper.selectOne(addressWrapper);
        if (defaultAddress == null) {
            return null;
        }
        return AddressVO.builder()
                .id(defaultAddress.getId())
                .contactName(defaultAddress.getContactName())
                .contactPhone(defaultAddress.getContactPhone())
                .receiverName(defaultAddress.getContactName())
                .receiverPhone(defaultAddress.getContactPhone())
                .province(defaultAddress.getProvince())
                .city(defaultAddress.getCity())
                .district(defaultAddress.getDistrict())
                .detailAddress(defaultAddress.getDetailAddress())
                .fullAddress(defaultAddress.getFullAddress())
                .isDefault(Integer.valueOf(1).equals(defaultAddress.getIsDefault()))
                .longitude(defaultAddress.getLongitude())
                .latitude(defaultAddress.getLatitude())
                .businessArea(defaultAddress.getBusinessArea())
                .doorNo(defaultAddress.getDoorNo())
                .mapAddress(defaultAddress.getMapAddress())
                .build();
    }

    private List<UserCouponVO> loadAvailableCoupons(Long userId, BigDecimal totalAmount) {
        List<UserCouponVO> availableCoupons = new ArrayList<>();
        LambdaQueryWrapper<UserCoupon> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.eq(UserCoupon::getUserId, userId).eq(UserCoupon::getStatus, 0);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(couponWrapper);
        for (UserCoupon uc : userCoupons) {
            if (isCouponExpired(uc.getExpireTime())) {
                continue;
            }
            if (uc.getMinAmount() != null && totalAmount.compareTo(uc.getMinAmount()) < 0) {
                continue;
            }
            availableCoupons.add(UserCouponVO.builder()
                    .id(uc.getId())
                    .couponId(uc.getCouponId())
                    .couponName(uc.getCouponName())
                    .couponType(uc.getCouponType())
                    .typeDesc(Integer.valueOf(1).equals(uc.getCouponType()) ? "满减券" : "折扣券")
                    .discountAmount(uc.getDiscountAmount())
                    .discountRate(uc.getDiscountRate())
                    .minAmount(uc.getMinAmount())
                    .maxDiscount(uc.getMaxDiscount())
                    .status(uc.getStatus())
                    .expireTime(uc.getExpireTime())
                    .available(true)
                    .build());
        }
        return availableCoupons;
    }

    private List<PaymentMethodVO> buildPaymentMethods() {
        return List.of(
                PaymentMethodVO.builder().key("wechat").title("微信支付").subtitle("默认推荐 · 快速完成支付").verifyType("face").build(),
                PaymentMethodVO.builder().key("alipay").title("支付宝").subtitle("切换支付方式 · 支持余额与花呗").verifyType("password").build(),
                PaymentMethodVO.builder().key("bank").title("银行卡").subtitle("储蓄卡与借记卡账户").verifyType("password").build(),
                PaymentMethodVO.builder().key("credit").title("信用卡").subtitle("支持分期与国际信用卡").verifyType("password").build()
        );
    }

    private boolean isCouponExpired(String expireTime) {
        if (expireTime == null || expireTime.isBlank()) {
            return false;
        }
        return LocalDateTime.parse(expireTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .isBefore(LocalDateTime.now());
    }

    private String nowString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String defaultPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return PaymentRecord.PaymentMethod.WECHAT.getCode();
        }
        return paymentMethod;
    }

    private String defaultString(String value, String fallback, String finalValue) {
        if (value != null && !value.isBlank()) {
            return value;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return finalValue;
    }

    private void recordOrderStatusHistory(OrderInfo order,
                                          Integer fromStatus,
                                          Integer toStatus,
                                          String action,
                                          String operatorType,
                                          Long operatorId,
                                          String operatorName,
                                          String logisticsCompany,
                                          String trackingNo,
                                          String remark) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(order.getId());
        history.setOrderNo(order.getOrderNo());
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setAction(action);
        history.setOperatorType(operatorType);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setLogisticsCompany(logisticsCompany);
        history.setTrackingNo(trackingNo);
        history.setRemark(remark);
        history.setOperateTime(new Date());
        orderStatusHistoryMapper.insert(history);
    }

    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "";
        };
    }
}
