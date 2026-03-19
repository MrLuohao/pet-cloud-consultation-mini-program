package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.vo.ProductCollectionVO;

import java.util.List;

/**
 * 商品收藏服务接口
 *
 * @author luohao
 */
public interface ProductCollectionService {

    /**
     * 添加收藏
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 收藏ID
     */
    Long addCollection(Long userId, Long productId);

    /**
     * 取消收藏
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    void removeCollection(Long userId, Long productId);

    /**
     * 获取用户收藏列表
     *
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<ProductCollectionVO> getCollectionList(Long userId);

    /**
     * 检查是否已收藏
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否已收藏
     */
    Boolean isCollected(Long userId, Long productId);

    /**
     * 切换收藏状态
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 切换后的收藏状态（true-已收藏，false-已取消）
     */
    Boolean toggleCollection(Long userId, Long productId);
}
