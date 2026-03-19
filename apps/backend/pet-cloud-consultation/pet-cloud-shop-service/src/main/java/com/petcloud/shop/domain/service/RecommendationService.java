package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.vo.ProductVO;

import java.util.List;

/**
 * 推荐服务接口
 *
 * @author luohao
 */
public interface RecommendationService {

    /**
     * 根据购物车商品获取推荐
     *
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 推荐商品列表
     */
    List<ProductVO> getRecommendationsByCart(Long userId, Integer limit);

    /**
     * 获取热销商品推荐
     *
     * @param limit 返回数量限制
     * @return 热销商品列表
     */
    List<ProductVO> getHotProducts(Integer limit);

    /**
     * 获取相似商品推荐
     *
     * @param productId 商品ID
     * @param limit 返回数量限制
     * @return 相似商品列表
     */
    List<ProductVO> getSimilarProducts(Long productId, Integer limit);
}
