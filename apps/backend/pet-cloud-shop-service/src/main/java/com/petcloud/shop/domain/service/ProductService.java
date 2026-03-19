package com.petcloud.shop.domain.service;

import com.petcloud.common.core.response.PageVO;
import com.petcloud.shop.domain.vo.ProductCategoryVO;
import com.petcloud.shop.domain.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 获取商品分类列表
     */
    List<ProductCategoryVO> getCategoryList();

    /**
     * 获取商品列表（无分页，供内部使用）
     */
    List<ProductVO> getProductList(Long categoryId);

    /**
     * 获取商品列表（分页）- BE-0.1
     *
     * @param categoryId 分类ID（可选）
     * @param page       页码（从1开始）
     * @param pageSize   每页数量（最大50）
     */
    PageVO<ProductVO> getProductPage(Long categoryId, int page, int pageSize);

    /**
     * 获取商品详情
     */
    ProductVO getProductDetail(Long productId);
}

