package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ShoppingCart;
import com.petcloud.shop.domain.service.CartService;
import com.petcloud.shop.domain.vo.CartGroupVO;
import com.petcloud.shop.domain.vo.CartItemVO;
import com.petcloud.shop.domain.vo.CartPageVO;
import com.petcloud.shop.domain.vo.CartSummaryVO;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductCategoryMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ShoppingCartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final ProductMapper productMapper;
    @SuppressWarnings("unused")
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public CartPageVO getCartList(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> cartList = shoppingCartMapper.selectList(queryWrapper);

        List<CartItemVO> validItems = new ArrayList<>();
        List<CartItemVO> invalidItems = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) {
                continue;
            }
            CartItemVO item = buildCartItem(cart, product);
            if (isInvalidItem(item, product)) {
                invalidItems.add(item.toBuilder().status("invalid").selected(false).build());
            } else {
                validItems.add(item);
            }
        }

        return CartPageVO.builder()
                .cartGroups(buildCartGroups(validItems))
                .invalidItems(invalidItems)
                .summary(buildSummary(validItems, !invalidItems.isEmpty()))
                .build();
    }

    @Override
    public Long addToCart(Long userId, Long productId, Integer quantity, String specLabel) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(RespType.PRODUCT_NOT_FOUND);
        }
        if (product.getStock() < quantity) {
            throw new BusinessException(RespType.STOCK_INSUFFICIENT);
        }
        String resolvedSpecLabel = resolveSpecLabel(product, specLabel);

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getProductId, productId)
                .eq(ShoppingCart::getSpecLabel, resolvedSpecLabel);
        ShoppingCart existing = shoppingCartMapper.selectOne(queryWrapper);

        if (existing != null) {
            int newQuantity = existing.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new BusinessException(RespType.STOCK_INSUFFICIENT);
            }
            existing.setQuantity(newQuantity);
            existing.setSelected(1);
            fillCartSnapshot(existing, product, resolvedSpecLabel);
            shoppingCartMapper.updateById(existing);
            return existing.getId();
        }

        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setSelected(1);
        fillCartSnapshot(cart, product, resolvedSpecLabel);
        shoppingCartMapper.insert(cart);
        return cart.getId();
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        ShoppingCart cart = shoppingCartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CART_ITEM_NOT_FOUND);
        }
        if (quantity <= 0) {
            throw new BusinessException(RespType.INVALID_QUANTITY);
        }

        Product product = productMapper.selectById(cart.getProductId());
        if (product == null) {
            throw new BusinessException(RespType.PRODUCT_NOT_FOUND);
        }
        if (product.getStock() < quantity) {
            throw new BusinessException(RespType.STOCK_INSUFFICIENT);
        }

        cart.setQuantity(quantity);
        fillCartSnapshot(cart, product, cart.getSpecLabel());
        shoppingCartMapper.updateById(cart);
    }

    @Override
    public void deleteCartItem(Long userId, Long cartId) {
        ShoppingCart cart = shoppingCartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CART_ITEM_NOT_FOUND);
        }
        shoppingCartMapper.deleteById(cartId);
    }

    @Override
    public void clearCart(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartMapper.delete(queryWrapper);
    }

    @Override
    public Integer getCartCount(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> cartList = shoppingCartMapper.selectList(queryWrapper);
        return cartList.stream().mapToInt(ShoppingCart::getQuantity).sum();
    }

    private CartItemVO buildCartItem(ShoppingCart cart, Product product) {
        BigDecimal price = cart.getPriceSnapshot() != null ? cart.getPriceSnapshot() : product.getPrice();
        BigDecimal originalPrice = cart.getOriginalPriceSnapshot() != null
                ? cart.getOriginalPriceSnapshot()
                : (product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getPrice());
        int quantity = cart.getQuantity() != null ? cart.getQuantity() : 0;
        return CartItemVO.builder()
                .id(cart.getId())
                .productId(product.getId())
                .name(product.getName())
                .coverUrl(product.getCoverUrl())
                .price(price)
                .originalPrice(originalPrice)
                .quantity(quantity)
                .stock(product.getStock())
                .shopId(defaultString(cart.getShopId(), product.getShopId(), "official"))
                .shopName(defaultString(cart.getShopName(), product.getShopName(), "伴宠云诊自营"))
                .serviceText(defaultString(cart.getServiceText(), product.getServiceText(), "包邮 · 正品保障"))
                .spec(defaultString(cart.getSpecLabel(), product.getDefaultSpec(), "默认规格"))
                .selected(Integer.valueOf(1).equals(cart.getSelected()))
                .status(defaultString(cart.getStatus(), resolveCartStatus(product)))
                .subtotal(price.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }

    private List<CartGroupVO> buildCartGroups(List<CartItemVO> items) {
        Map<String, List<CartItemVO>> grouped = new LinkedHashMap<>();
        for (CartItemVO item : items) {
            grouped.computeIfAbsent(item.getShopId(), key -> new ArrayList<>()).add(item);
        }

        List<CartGroupVO> groups = new ArrayList<>();
        for (Map.Entry<String, List<CartItemVO>> entry : grouped.entrySet()) {
            List<CartItemVO> groupItems = entry.getValue();
            BigDecimal total = groupItems.stream()
                    .filter(CartItemVO::getSelected)
                    .map(CartItemVO::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int selectedCount = groupItems.stream()
                    .filter(CartItemVO::getSelected)
                    .mapToInt(CartItemVO::getQuantity)
                    .sum();
            groups.add(CartGroupVO.builder()
                    .merchantId(entry.getKey())
                    .merchantName(groupItems.get(0).getShopName())
                    .serviceText(groupItems.get(0).getServiceText())
                    .allSelected(groupItems.stream().allMatch(CartItemVO::getSelected))
                    .selectedCount(selectedCount)
                    .totalAmount(total)
                    .items(groupItems)
                    .build());
        }
        return groups;
    }

    private CartSummaryVO buildSummary(List<CartItemVO> items, boolean hasInvalidItems) {
        BigDecimal totalAmount = items.stream()
                .filter(CartItemVO::getSelected)
                .map(CartItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = items.stream()
                .filter(CartItemVO::getSelected)
                .map(item -> {
                    BigDecimal original = item.getOriginalPrice() != null ? item.getOriginalPrice() : item.getPrice();
                    return original.subtract(item.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int selectedCount = items.stream()
                .filter(CartItemVO::getSelected)
                .mapToInt(CartItemVO::getQuantity)
                .sum();
        boolean allSelected = !hasInvalidItems && !items.isEmpty() && items.stream().allMatch(CartItemVO::getSelected);
        return CartSummaryVO.builder()
                .selectedCount(selectedCount)
                .totalAmount(totalAmount)
                .totalDiscount(totalDiscount.max(BigDecimal.ZERO))
                .allSelected(allSelected)
                .build();
    }

    private boolean isInvalidItem(CartItemVO item, Product product) {
        return "invalid".equalsIgnoreCase(item.getStatus())
                || product.getStock() == null
                || product.getStock() <= 0
                || !Product.Status.ONLINE.getCode().equals(product.getStatus());
    }

    private void fillCartSnapshot(ShoppingCart cart, Product product, String specLabel) {
        cart.setShopId(defaultString(product.getShopId(), "official"));
        cart.setShopName(defaultString(product.getShopName(), "伴宠云诊自营"));
        cart.setServiceText(defaultString(product.getServiceText(), "包邮 · 正品保障"));
        cart.setSpecLabel(resolveSpecLabel(product, specLabel));
        cart.setPriceSnapshot(product.getPrice());
        cart.setOriginalPriceSnapshot(product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getPrice());
        cart.setStatus(resolveCartStatus(product));
    }

    private String resolveSpecLabel(Product product, String specLabel) {
        return defaultString(specLabel, product.getDefaultSpec(), "默认规格");
    }

    private String resolveCartStatus(Product product) {
        if (product.getStock() == null || product.getStock() <= 0 || !Product.Status.ONLINE.getCode().equals(product.getStatus())) {
            return "invalid";
        }
        return "active";
    }

    private String defaultString(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
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
}
