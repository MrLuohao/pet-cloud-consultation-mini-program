package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口
 *
 * @author luohao
 */
public interface CartService {

    /**
     * 获取购物车列表
     *
     * @param userId 用户ID
     * @return 购物车VO列表
     */
    List<CartVO> getCartList(Long userId);

    /**
     * 添加商品到购物车
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  数量
     * @return 购物车ID
     */
    Long addToCart(Long userId, Long productId, Integer quantity);

    /**
     * 更新购物车商品数量
     *
     * @param userId   用户ID
     * @param cartId   购物车ID
     * @param quantity 数量
     */
    void updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 删除购物车商品
     *
     * @param userId 用户ID
     * @param cartId 购物车ID
     */
    void deleteCartItem(Long userId, Long cartId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     */
    void clearCart(Long userId);

    /**
     * 获取购物车商品数量
     *
     * @param userId 用户ID
     * @return 商品数量
     */
    Integer getCartCount(Long userId);
}
