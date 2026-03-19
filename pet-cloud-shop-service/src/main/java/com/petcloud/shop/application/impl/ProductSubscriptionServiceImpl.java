package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.shop.domain.dto.SubscriptionConfigDTO;
import com.petcloud.shop.domain.dto.SubscriptionCreateDTO;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ProductSubscription;
import com.petcloud.shop.domain.service.ProductSubscriptionService;
import com.petcloud.shop.domain.vo.ProductSubscriptionVO;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品订阅服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSubscriptionServiceImpl implements ProductSubscriptionService {

    private final ProductSubscriptionMapper subscriptionMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public Long createSubscription(Long userId, SubscriptionCreateDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || !Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException(RespType.PRODUCT_NOT_FOUND);
        }

        ProductSubscription sub = new ProductSubscription();
        sub.setUserId(userId);
        sub.setProductId(dto.getProductId());
        sub.setSkuId(dto.getSkuId());
        sub.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        sub.setCycleDays(dto.getCycleDays());
        sub.setAddressId(dto.getAddressId());
        sub.setStatus(ProductSubscription.Status.ACTIVE.getCode());
        sub.setNextOrderDate(LocalDate.now().plusDays(dto.getCycleDays()));
        sub.setDiscountRate(new BigDecimal("0.90"));

        subscriptionMapper.insert(sub);
        return sub.getId();
    }

    @Override
    public List<ProductSubscriptionVO> getSubscriptionList(Long userId) {
        LambdaQueryWrapper<ProductSubscription> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductSubscription::getUserId, userId)
          .ne(ProductSubscription::getStatus, ProductSubscription.Status.CANCELLED.getCode())
          .orderByDesc(ProductSubscription::getCreateTime);
        List<ProductSubscription> subs = subscriptionMapper.selectList(qw);
        return subs.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void pauseSubscription(Long subscriptionId, Long userId) {
        ProductSubscription sub = getAndValidate(subscriptionId, userId);
        if (!ProductSubscription.Status.ACTIVE.getCode().equals(sub.getStatus())) {
            throw new BusinessException(RespType.SUBSCRIPTION_CANNOT_PAUSE);
        }
        sub.setStatus(ProductSubscription.Status.PAUSED.getCode());
        subscriptionMapper.updateById(sub);
    }

    @Override
    public void resumeSubscription(Long subscriptionId, Long userId) {
        ProductSubscription sub = getAndValidate(subscriptionId, userId);
        if (!ProductSubscription.Status.PAUSED.getCode().equals(sub.getStatus())) {
            throw new BusinessException(RespType.SUBSCRIPTION_CANNOT_RESUME);
        }
        sub.setStatus(ProductSubscription.Status.ACTIVE.getCode());
        subscriptionMapper.updateById(sub);
    }

    @Override
    public void cancelSubscription(Long subscriptionId, Long userId) {
        ProductSubscription sub = getAndValidate(subscriptionId, userId);
        sub.setStatus(ProductSubscription.Status.CANCELLED.getCode());
        subscriptionMapper.updateById(sub);
    }

    @Override
    public void updateConfig(Long subscriptionId, Long userId, SubscriptionConfigDTO dto) {
        ProductSubscription sub = getAndValidate(subscriptionId, userId);
        if (dto.getCycleDays() != null) {
            sub.setCycleDays(dto.getCycleDays());
            sub.setNextOrderDate(LocalDate.now().plusDays(dto.getCycleDays()));
        }
        if (dto.getQuantity() != null) {
            sub.setQuantity(dto.getQuantity());
        }
        if (dto.getAddressId() != null) {
            sub.setAddressId(dto.getAddressId());
        }
        subscriptionMapper.updateById(sub);
    }

    private ProductSubscription getAndValidate(Long subscriptionId, Long userId) {
        ProductSubscription sub = subscriptionMapper.selectById(subscriptionId);
        if (sub == null || !sub.getUserId().equals(userId)) {
            throw new BusinessException(RespType.SUBSCRIPTION_NOT_FOUND);
        }
        return sub;
    }

    private ProductSubscriptionVO convertToVO(ProductSubscription sub) {
        Product product = productMapper.selectById(sub.getProductId());
        String productName = product != null ? product.getName() : "";
        String coverUrl = product != null ? product.getCoverUrl() : "";
        BigDecimal price = product != null ? product.getPrice() : BigDecimal.ZERO;
        BigDecimal subscribePrice = price.multiply(sub.getDiscountRate()).setScale(2, RoundingMode.HALF_UP);

        return ProductSubscriptionVO.builder()
                .id(sub.getId())
                .productId(sub.getProductId())
                .productName(productName)
                .productCoverUrl(coverUrl)
                .quantity(sub.getQuantity())
                .cycleDays(sub.getCycleDays())
                .cycleDesc(sub.getCycleDays() + "天/次")
                .status(sub.getStatus())
                .statusDesc(getStatusDesc(sub.getStatus()))
                .nextOrderDate(sub.getNextOrderDate())
                .discountRate(sub.getDiscountRate())
                .unitPrice(price)
                .subscribePrice(subscribePrice)
                .build();
    }

    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 0: return "正常";
            case 1: return "已暂停";
            case 2: return "已取消";
            default: return "";
        }
    }
}
