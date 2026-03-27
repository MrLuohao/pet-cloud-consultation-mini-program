package com.petcloud.shop.interfaces.controller.trade;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.shop.domain.dto.CartAddDTO;
import com.petcloud.shop.domain.dto.CartDeleteDTO;
import com.petcloud.shop.domain.dto.CartUpdateDTO;
import com.petcloud.shop.domain.service.CartService;
import com.petcloud.shop.domain.vo.CartPageVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车控制器
 *
 * @author luohao
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Response<CartPageVO> getCartList(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取购物车列表，userId: {}", userId);
        CartPageVO cartPage = cartService.getCartList(userId);
        return Response.succeed(cartPage);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Response<Long> addToCart(HttpServletRequest request,
                                      @RequestBody CartAddDTO addRequest) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("添加商品到购物车，userId: {}, productId: {}, quantity: {}, specLabel: {}",
                userId, addRequest.getProductId(), addRequest.getQuantity(), addRequest.getSpecLabel());
        Long cartId = cartService.addToCart(userId, addRequest.getProductId(), addRequest.getQuantity(), addRequest.getSpecLabel());
        return Response.succeed(cartId);
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/update")
    public Response<Void> updateQuantity(HttpServletRequest request,
                                          @RequestBody CartUpdateDTO updateRequest) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("更新购物车商品数量，userId: {}, cartId: {}, quantity: {}", userId, updateRequest.getCartId(), updateRequest.getQuantity());
        cartService.updateQuantity(userId, updateRequest.getCartId(), updateRequest.getQuantity());
        return Response.succeed();
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/delete")
    public Response<Void> deleteCartItem(HttpServletRequest request,
                                          @RequestBody CartDeleteDTO deleteRequest) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("删除购物车商品，userId: {}, cartId: {}", userId, deleteRequest.getCartId());
        cartService.deleteCartItem(userId, deleteRequest.getCartId());
        return Response.succeed();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Response<Void> clearCart(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("清空购物车，userId: {}", userId);
        cartService.clearCart(userId);
        return Response.succeed();
    }

    /**
     * 获取购物车商品数量
     */
    @GetMapping("/count")
    public Response<Integer> getCartCount(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取购物车商品数量，userId: {}", userId);
        Integer count = cartService.getCartCount(userId);
        return Response.succeed(count);
    }
}
