package com.petcloud.shop.interfaces.controller.product;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.shop.domain.service.RecommendationService;
import com.petcloud.shop.domain.vo.ProductVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐商品控制器
 *
 * @author luohao
 */
@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 根据购物车获取推荐商品
     *
     * @param request HTTP请求
     * @param limit 返回数量限制，默认10
     * @return 推荐商品列表
     */
    @GetMapping("/by-cart")
    public Response<List<ProductVO>> getRecommendationsByCart(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        int safeLimit = normalizeLimit(limit);
        Long userId = userContextHolder.getCurrentUserId(request);
        if (userId == null) {
            log.info("未登录用户访问购物车推荐，降级返回热销商品，limit: {}", safeLimit);
            return Response.succeed(recommendationService.getHotProducts(safeLimit));
        }
        log.info("获取购物车推荐商品，userId: {}, limit: {}", userId, limit);
        List<ProductVO> recommendations = recommendationService.getRecommendationsByCart(userId, safeLimit);
        return Response.succeed(recommendations);
    }

    /**
     * 获取热销商品推荐
     *
     * @param limit 返回数量限制，默认10
     * @return 热销商品列表
     */
    @GetMapping("/hot")
    public Response<List<ProductVO>> getHotProducts(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        int safeLimit = normalizeLimit(limit);
        log.info("获取热销商品推荐，limit: {}", safeLimit);
        List<ProductVO> hotProducts = recommendationService.getHotProducts(safeLimit);
        return Response.succeed(hotProducts);
    }

    /**
     * 获取相似商品推荐
     *
     * @param productId 商品ID
     * @param limit 返回数量限制，默认5
     * @return 相似商品列表
     */
    @GetMapping("/similar/{productId}")
    public Response<List<ProductVO>> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        int safeLimit = normalizeLimit(limit);
        log.info("获取相似商品推荐，productId: {}, limit: {}", productId, safeLimit);
        List<ProductVO> similarProducts = recommendationService.getSimilarProducts(productId, safeLimit);
        return Response.succeed(similarProducts);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }
}
