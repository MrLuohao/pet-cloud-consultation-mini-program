package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ProductCategory;
import com.petcloud.shop.domain.entity.ShoppingCart;
import com.petcloud.shop.domain.service.CartService;
import com.petcloud.shop.domain.vo.CartVO;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductCategoryMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ShoppingCartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public List<CartVO> getCartList(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> cartList = shoppingCartMapper.selectList(queryWrapper);

        return cartList.stream()
                .map(cart -> {
                    Product product = productMapper.selectById(cart.getProductId());
                    if (product == null) {
                        return null;
                    }
                    // 查询分类名称
                    String categoryName = "";
                    if (product.getCategoryId() != null) {
                        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
                        if (category != null) {
                            categoryName = category.getName();
                        }
                    }
                    return CartVO.builder()
                            .id(cart.getId())
                            .productId(product.getId())
                            .productName(product.getName())
                            .categoryName(categoryName)
                            .coverUrl(product.getCoverUrl())
                            .price(product.getPrice())
                            .originalPrice(product.getOriginalPrice())
                            .quantity(cart.getQuantity())
                            .stock(product.getStock())
                            .subtotal(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                            .selected(false)
                            .build();
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    @Override
    public Long addToCart(Long userId, Long productId, Integer quantity) {
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(RespType.PRODUCT_NOT_FOUND);
        }

        // 检查库存
        if (product.getStock() < quantity) {
            throw new BusinessException(RespType.STOCK_INSUFFICIENT);
        }

        // 查询是否已存在
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getProductId, productId);
        ShoppingCart existing = shoppingCartMapper.selectOne(queryWrapper);

        if (existing != null) {
            // 更新数量
            int newQuantity = existing.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new BusinessException(RespType.STOCK_INSUFFICIENT);
            }
            existing.setQuantity(newQuantity);
            shoppingCartMapper.updateById(existing);
            return existing.getId();
        } else {
            // 新增
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            shoppingCartMapper.insert(cart);
            return cart.getId();
        }
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
        return cartList.stream()
                .mapToInt(ShoppingCart::getQuantity)
                .sum();
    }
}
