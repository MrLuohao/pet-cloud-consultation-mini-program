package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.vo.ProductVO;

import java.util.List;

/**
 * 搜索服务接口
 *
 * @author luohao
 */
public interface SearchService {

    /**
     * 搜索商品
     *
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    List<ProductVO> searchProducts(String keyword);

    /**
     * 获取热门搜索词
     *
     * @return 热门搜索词列表
     */
    List<String> getHotKeywords();
}
