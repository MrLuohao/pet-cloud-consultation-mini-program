package com.petcloud.shop.interfaces.controller.product;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.shop.domain.dto.ProductReviewDTO;
import com.petcloud.shop.domain.enums.ProductReviewFilterType;
import com.petcloud.shop.domain.service.ProductDetailService;
import com.petcloud.shop.domain.vo.ProductDetailVO;
import com.petcloud.shop.domain.vo.ProductReviewVO;
import com.petcloud.shop.domain.vo.ReviewSummaryVO;
import com.petcloud.shop.domain.vo.ReviewableOrderItemVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品详情控制器
 *
 * @author luohao
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductDetailController {

    private static final Logger log = LoggerFactory.getLogger(ProductDetailController.class);

    private final ProductDetailService productDetailService;
    private final UserContextHolderWeb userContextHolderWeb;

    /**
     * 获取商品详情（含评价）
     */
    @GetMapping("/{id}/detail")
    public Response<ProductDetailVO> getProductDetail(@PathVariable Long id) {
        log.info("获取商品详情，productId: {}", id);
        ProductDetailVO productDetail = productDetailService.getProductDetail(id);
        return Response.succeed(productDetail);
    }

    /**
     * 获取商品评价列表（带筛选）
     */
    @GetMapping("/{id}/reviews")
    public Response<List<ProductReviewVO>> getProductReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = ProductReviewFilterType.DEFAULT_CODE) String filter,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        ProductReviewFilterType filterType = ProductReviewFilterType.fromCode(filter);
        log.info("获取商品评价，productId: {}, filter: {}, page: {}, size: {}", id, filterType.getCode(), page, size);
        Long userId = null;
        try {
            userId = userContextHolderWeb.getRequiredUserId(request);
        } catch (Exception e) {
            log.debug("未登录用户访问评价列表，productId: {}", id);
        }
        List<ProductReviewVO> reviews = productDetailService.getProductReviewsWithFilter(id, filterType.getCode(), page, size, userId);
        return Response.succeed(reviews);
    }

    /**
     * 提交商品评价
     */
    @PostMapping("/review")
    public Response<Void> createReview(HttpServletRequest request,
                                    @RequestBody ProductReviewDTO reviewRequest) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("提交商品评价，userId: {}, productId: {}", userId, reviewRequest.getProductId());
        productDetailService.createReview(userId, reviewRequest);
        return Response.succeed();
    }

    /**
     * 获取商品评价统计
     */
    @GetMapping("/{id}/reviews/summary")
    public Response<ReviewSummaryVO> getReviewSummary(@PathVariable Long id) {
        log.info("获取商品评价统计，productId: {}", id);
        ReviewSummaryVO summary = productDetailService.getReviewSummary(id);
        return Response.succeed(summary);
    }

    /**
     * 获取可评价的订单项
     */
    @GetMapping("/{productId}/reviewable-order-item")
    public Response<ReviewableOrderItemVO> getReviewableOrderItem(
            @PathVariable Long productId,
            HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取可评价订单项，userId: {}, productId: {}", userId, productId);
        ReviewableOrderItemVO item = productDetailService.getReviewableOrderItem(userId, productId);
        return Response.succeed(item);
    }

    /**
     * 点赞/取消点赞评价
     */
    @PostMapping("/review/{reviewId}/like")
    public Response<ProductReviewVO> toggleReviewLike(
            @PathVariable Long reviewId,
            HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("点赞评价，userId: {}, reviewId: {}", userId, reviewId);
        ProductReviewVO result = productDetailService.toggleReviewLike(userId, reviewId);
        return Response.succeed(result);
    }

    /**
     * 编辑评价
     */
    @PutMapping("/review/{reviewId}")
    public Response<Void> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ProductReviewDTO reviewRequest,
            HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("编辑评价，userId: {}, reviewId: {}", userId, reviewId);
        productDetailService.updateReview(
                userId,
                reviewId,
                reviewRequest.getRating(),
                reviewRequest.getContent(),
                reviewRequest.getImages()
        );
        return Response.succeed();
    }

    /**
     * 添加追评
     */
    @PostMapping("/review/{reviewId}/follow-up")
    public Response<Void> addFollowUp(
            @PathVariable Long reviewId,
            @RequestBody ProductReviewDTO reviewRequest,
            HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("添加追评，userId: {}, reviewId: {}", userId, reviewId);
        productDetailService.addFollowUp(userId, reviewId, reviewRequest.getContent());
        return Response.succeed();
    }
}
