package com.petcloud.shop.application.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.shop.domain.entity.*;
import com.petcloud.shop.domain.service.OrderService;
import com.petcloud.shop.domain.vo.*;
import com.petcloud.shop.infrastructure.persistence.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
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

    @Override
    public OrderConfirmVO getOrderConfirm(Long userId, List<Long> productIds, List<Integer> quantities, List<Long> cartIds) {
        List<OrderItemVO> items = new ArrayList<>();

        // 如果是从购物车下单
        if (cartIds != null && !cartIds.isEmpty()) {
            LambdaQueryWrapper<ShoppingCart> cartWrapper = new LambdaQueryWrapper<>();
            cartWrapper.eq(ShoppingCart::getUserId, userId)
                    .in(ShoppingCart::getId, cartIds);
            List<ShoppingCart> cartItems = shoppingCartMapper.selectList(cartWrapper);

            for (ShoppingCart cart : cartItems) {
                Product product = productMapper.selectById(cart.getProductId());
                if (product != null) {
                    items.add(OrderItemVO.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .coverUrl(product.getCoverUrl())
                            .price(product.getPrice())
                            .quantity(cart.getQuantity())
                            .subtotal(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                            .build());
                }
            }
        } else if (productIds != null && !productIds.isEmpty()) {
            // 直接购买
            for (int i = 0; i < productIds.size(); i++) {
                Product product = productMapper.selectById(productIds.get(i));
                if (product != null) {
                    int quantity = quantities != null && i < quantities.size() ? quantities.get(i) : 1;
                    items.add(OrderItemVO.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .coverUrl(product.getCoverUrl())
                            .price(product.getPrice())
                            .quantity(quantity)
                            .subtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                            .build());
                }
            }
        }

        // 计算总金额
        BigDecimal totalAmount = items.stream()
                .map(OrderItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 获取默认地址
        LambdaQueryWrapper<UserAddress> addressWrapper = new LambdaQueryWrapper<>();
        addressWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1);
        UserAddress defaultAddress = userAddressMapper.selectOne(addressWrapper);

        AddressVO addressVO = null;
        if (defaultAddress != null) {
            addressVO = AddressVO.builder()
                    .id(defaultAddress.getId())
                    .contactName(defaultAddress.getContactName())
                    .contactPhone(defaultAddress.getContactPhone())
                    .province(defaultAddress.getProvince())
                    .city(defaultAddress.getCity())
                    .district(defaultAddress.getDistrict())
                    .detailAddress(defaultAddress.getDetailAddress())
                    .fullAddress(defaultAddress.getProvince() + defaultAddress.getCity() + defaultAddress.getDistrict() + defaultAddress.getDetailAddress())
                    .isDefault(defaultAddress.getIsDefault() == 1)
                    .build();
        }

        // 获取可用优惠券
        List<UserCouponVO> availableCoupons = new ArrayList<>();
        LambdaQueryWrapper<UserCoupon> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(couponWrapper);
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        for (UserCoupon uc : userCoupons) {
            if (uc.getExpireTime().compareTo(now) < 0) {
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
                    .typeDesc(uc.getCouponType() == 1 ? "满减券" : "折扣券")
                    .discountAmount(uc.getDiscountAmount())
                    .discountRate(uc.getDiscountRate())
                    .minAmount(uc.getMinAmount())
                    .maxDiscount(uc.getMaxDiscount())
                    .status(uc.getStatus())
                    .expireTime(uc.getExpireTime())
                    .available(true)
                    .build());
        }

        return OrderConfirmVO.builder()
                .items(items)
                .address(addressVO)
                .totalCount(items.stream().mapToInt(OrderItemVO::getQuantity).sum())
                .totalAmount(totalAmount)
                .freight(BigDecimal.ZERO)  // 暂时免运费
                .availableCoupons(availableCoupons)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitOrder(Long userId, List<Long> productIds, List<Integer> quantities, Long addressId, Long couponId, String remark) {
        // 获取地址
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ADDRESS_NOT_FOUND);
        }

        // 构建商品列表
        List<OrderItemVO> items = new ArrayList<>();
        if (productIds != null && !productIds.isEmpty()) {
            for (int i = 0; i < productIds.size(); i++) {
                Product product = productMapper.selectById(productIds.get(i));
                if (product == null) {
                    throw new BusinessException(RespType.PRODUCT_NOT_FOUND);
                }
                int quantity = quantities != null && i < quantities.size() ? quantities.get(i) : 1;
                if (product.getStock() < quantity) {
                    throw new BusinessException(RespType.STOCK_INSUFFICIENT);
                }
                items.add(OrderItemVO.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .coverUrl(product.getCoverUrl())
                        .price(product.getPrice())
                        .quantity(quantity)
                        .subtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                        .build());
            }
        }

        // 计算金额
        BigDecimal totalAmount = items.stream()
                .map(OrderItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal couponDiscount = BigDecimal.ZERO;

        // 使用优惠券
        if (couponId != null) {
            UserCoupon userCoupon = userCouponMapper.selectById(couponId);
            if (userCoupon != null && userCoupon.getUserId().equals(userId) && userCoupon.getStatus() == 0) {
                String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                if (userCoupon.getExpireTime().compareTo(now) >= 0) {
                    if (userCoupon.getMinAmount() == null || totalAmount.compareTo(userCoupon.getMinAmount()) >= 0) {
                        if (userCoupon.getCouponType() == 1) {
                            couponDiscount = userCoupon.getDiscountAmount() != null ? userCoupon.getDiscountAmount() : BigDecimal.ZERO;
                        } else if (userCoupon.getCouponType() == 2 && userCoupon.getDiscountRate() != null) {
                            BigDecimal discount = totalAmount.multiply(BigDecimal.ONE.subtract(userCoupon.getDiscountRate().divide(BigDecimal.valueOf(10), 2, java.math.RoundingMode.HALF_UP)));
                            if (userCoupon.getMaxDiscount() != null && discount.compareTo(userCoupon.getMaxDiscount()) > 0) {
                                couponDiscount = userCoupon.getMaxDiscount();
                            } else {
                                couponDiscount = discount;
                            }
                        }
                    }
                }
            }
        }

        BigDecimal payAmount = totalAmount.subtract(couponDiscount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        OrderInfo order = new OrderInfo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setReceiverName(address.getContactName());
        order.setReceiverPhone(address.getContactPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setRemark(remark);
        order.setCouponId(couponId);
        order.setCouponDiscount(couponDiscount);
        order.setStatus(OrderInfo.Status.UNPAID.getCode());
        orderInfoMapper.insert(order);

        // 创建订单项
        for (OrderItemVO item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setCoverUrl(item.getCoverUrl());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(item.getSubtotal());
            orderItemMapper.insert(orderItem);

            // 扣减库存
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() - item.getQuantity());
                product.setSales(product.getSales() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        // 标记优惠券为已使用
        if (couponId != null && couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
            UserCoupon userCoupon = userCouponMapper.selectById(couponId);
            if (userCoupon != null) {
                userCoupon.setStatus(1);
                userCoupon.setUseTime(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                userCoupon.setOrderId(order.getId());
                userCouponMapper.updateById(userCoupon);
            }
        }

        // 创建支付记录
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(order.getId());
        paymentRecord.setOrderNo(orderNo);
        paymentRecord.setUserId(userId);
        paymentRecord.setAmount(payAmount);
        paymentRecord.setPaymentMethod("wechat");
        paymentRecord.setStatus(PaymentRecord.Status.PENDING.getCode());
        paymentRecordMapper.insert(paymentRecord);

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
                                    .price(item.getPrice())
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
        if (order.getStatus() == OrderInfo.Status.COMPLETED.getCode()) {
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
                        .price(item.getPrice())
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

        if (order.getStatus() != OrderInfo.Status.UNPAID.getCode()) {
            throw new BusinessException(RespType.ORDER_STATUS_ERROR);
        }

        order.setStatus(OrderInfo.Status.CANCELLED.getCode());
        orderInfoMapper.updateById(order);

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

        if (order.getStatus() != OrderInfo.Status.PENDING_RECEIPT.getCode()) {
            throw new BusinessException(RespType.ORDER_STATUS_ERROR);
        }

        order.setStatus(OrderInfo.Status.COMPLETED.getCode());
        order.setReceiveTime(new Date());
        orderInfoMapper.updateById(order);
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

        // 更新支付记录
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getOrderId, orderId)
                .eq(PaymentRecord::getStatus, PaymentRecord.Status.PENDING.getCode());
        PaymentRecord paymentRecord = paymentRecordMapper.selectOne(wrapper);
        if (paymentRecord != null) {
            paymentRecord.setStatus(PaymentRecord.Status.PAID.getCode());
            paymentRecord.setPayTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            paymentRecord.setTransactionId("WX" + IdUtil.getSnowflakeNextIdStr());
            paymentRecordMapper.updateById(paymentRecord);
        }

        return true;
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
        return result.subList(start, end);
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
        long reviewedCount = productReviewMapper.selectCount(reviewWrapper);

        return items.size() - (int) reviewedCount;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + IdUtil.getSnowflakeNextIdStr();
    }

    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 0:
                return "待付款";
            case 1:
                return "待发货";
            case 2:
                return "待收货";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "";
        }
    }
}
