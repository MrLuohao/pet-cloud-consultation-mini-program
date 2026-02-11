package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.vo.ProductDetailVO;
import com.petcloud.shop.domain.vo.ProductReviewVO;
import com.petcloud.shop.domain.vo.ReviewSummaryVO;
import com.petcloud.shop.domain.vo.ReviewableOrderItemVO;

import java.util.List;

/**
 * 商品详情服务接口
 *
 * @author luohao
 */
public interface ProductDetailService {

    /**
     * 获取商品详情（含评价）
     *
     * @param productId 商品ID
     * @return 商品详情VO
     */
    ProductDetailVO getProductDetail(Long productId);

    /**
     * 获取商品评价列表
     *
     * @param productId 商品ID
     * @param page      页码
     * @param size      每页大小
     * @return 评价列表
     */
    List<ProductReviewVO> getProductReviews(Long productId, Integer page, Integer size);

    /**
     * 获取商品评价列表（带筛选）
     *
     * @param productId 商品ID
     * @param filter    筛选条件(all/good/bad/withImages)
     * @param page      页码
     * @param size      每页大小
     * @param userId    当前用户ID（用于判断是否点赞）
     * @return 评价列表
     */
    List<ProductReviewVO> getProductReviewsWithFilter(Long productId, String filter, Integer page, Integer size, Long userId);

    /**
     * 获取商品评价统计
     *
     * @param productId 商品ID
     * @return 评价统计
     */
    ReviewSummaryVO getReviewSummary(Long productId);

    /**
     * 提交商品评价
     *
     * @param userId     用户ID
     * @param orderItemId 订单项ID
     * @param productId  商品ID
     * @param rating     评分
     * @param content    评价内容
     * @param images     评价图片
     */
    void createReview(Long userId, Long orderItemId, Long productId, Integer rating, String content, String images);

    /**
     * 获取用户可评价的订单项
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 可评价的订单项，无则返回null
     */
    ReviewableOrderItemVO getReviewableOrderItem(Long userId, Long productId);

    /**
     * 点赞/取消点赞评价
     *
     * @param userId   用户ID
     * @param reviewId 评价ID
     * @return 点赞结果(isLiked, likeCount)
     */
    ProductReviewVO toggleReviewLike(Long userId, Long reviewId);

    /**
     * 编辑评价
     *
     * @param userId   用户ID
     * @param reviewId 评价ID
     * @param rating   评分
     * @param content  评价内容
     * @param images   评价图片
     */
    void updateReview(Long userId, Long reviewId, Integer rating, String content, String images);

    /**
     * 添加追评
     *
     * @param userId   用户ID
     * @param reviewId 评价ID
     * @param content  追评内容
     */
    void addFollowUp(Long userId, Long reviewId, String content);
}
