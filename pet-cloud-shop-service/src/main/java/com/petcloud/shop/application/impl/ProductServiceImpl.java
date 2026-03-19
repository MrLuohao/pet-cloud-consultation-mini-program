package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.web.constant.CacheConstants;
import com.petcloud.common.web.utils.RedisUtil;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ProductCategory;
import com.petcloud.shop.domain.enums.ShopCategoryVisualMeta;
import com.petcloud.shop.domain.service.ProductService;
import com.petcloud.shop.domain.vo.ProductCategoryVO;
import com.petcloud.shop.domain.vo.ProductVO;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductCategoryMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductCategoryVO> getCategoryList() {
        // 1. 先从缓存获取
        Object cached = redisUtil.get(CacheConstants.PRODUCT_CATEGORY_LIST);
        if (cached != null) {
            log.debug("从缓存获取商品分类列表");
            return (List<ProductCategoryVO>) cached;
        }

        // 2. 缓存没有，从数据库查询
        log.debug("从数据库查询商品分类列表");
        LambdaQueryWrapper<ProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCategory::getStatus, ProductCategory.Status.ENABLED.getCode())
                .orderByAsc(ProductCategory::getSortOrder);
        List<ProductCategory> categories = productCategoryMapper.selectList(queryWrapper);
        List<ProductCategoryVO> result = categories.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());

        // 3. 存入缓存
        redisUtil.set(CacheConstants.PRODUCT_CATEGORY_LIST, result, CacheConstants.CATEGORY_EXPIRE_SECONDS);
        log.debug("商品分类列表已缓存");

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductVO> getProductList(Long categoryId) {
        // 构建缓存key
        String cacheKey = CacheConstants.PRODUCT_LIST_PREFIX + (categoryId != null ? categoryId : "all");

        // 1. 先从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取商品列表，categoryId: {}", categoryId);
            return (List<ProductVO>) cached;
        }

        // 2. 缓存没有，从数据库查询
        log.debug("从数据库查询商品列表，categoryId: {}", categoryId);
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, Product.Status.ONLINE.getCode());

        if (categoryId != null) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        queryWrapper.orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getSales);

        List<Product> products = productMapper.selectList(queryWrapper);

        // 获取分类名称映射
        List<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .toList();
        Map<Long, String> categoryNameMap = productCategoryMapper.selectBatchIds(categoryIds)
                .stream()
                .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));

        List<ProductVO> result = products.stream()
                .map(product -> convertToProductVO(product, categoryNameMap.get(product.getCategoryId())))
                .collect(Collectors.toList());

        // 3. 存入缓存
        redisUtil.set(cacheKey, result, CacheConstants.PRODUCT_LIST_EXPIRE_SECONDS);
        log.debug("商品列表已缓存，key: {}", cacheKey);

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProductVO getProductDetail(Long productId) {
        // 构建缓存key
        String cacheKey = CacheConstants.PRODUCT_DETAIL_PREFIX + productId;

        // 1. 先从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取商品详情，productId: {}", productId);
            return (ProductVO) cached;
        }

        // 2. 缓存没有，从数据库查询
        log.debug("从数据库查询商品详情，productId: {}", productId);
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(RespType.PRODUCT_NOT_FOUND);
        }

        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        String categoryName = category != null ? category.getName() : null;

        ProductVO result = convertToProductVO(product, categoryName);

        // 3. 存入缓存
        redisUtil.set(cacheKey, result, CacheConstants.PRODUCT_DETAIL_EXPIRE_SECONDS);
        log.debug("商品详情已缓存，key: {}", cacheKey);

        return result;
    }

    @Override
    public PageVO<ProductVO> getProductPage(Long categoryId, int page, int pageSize) {
        // pageSize 上限保护
        int effectivePageSize = Math.min(pageSize, 50);

        IPage<Product> pageObj = new Page<>(page, effectivePageSize);
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, Product.Status.ONLINE.getCode());
        if (categoryId != null) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }
        queryWrapper.orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getSales);

        IPage<Product> result = productMapper.selectPage(pageObj, queryWrapper);

        List<Long> categoryIds = result.getRecords().stream()
                .map(Product::getCategoryId)
                .distinct()
                .toList();
        Map<Long, String> categoryNameMap = categoryIds.isEmpty() ? Collections.emptyMap() :
                productCategoryMapper.selectBatchIds(categoryIds)
                        .stream()
                        .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));

        List<ProductVO> list = result.getRecords().stream()
                .map(product -> convertToProductVO(product, categoryNameMap.get(product.getCategoryId())))
                .collect(Collectors.toList());

        log.debug("商品分页查询，categoryId: {}, page: {}, pageSize: {}, total: {}",
                categoryId, page, effectivePageSize, result.getTotal());
        return PageVO.of(list, result.getTotal(), page, effectivePageSize);
    }

    private ProductCategoryVO convertToCategoryVO(ProductCategory category) {
        ShopCategoryVisualMeta visualMeta = ShopCategoryVisualMeta.resolve(category.getName(),
                category.getSortOrder() != null ? Math.max(category.getSortOrder() - 1, 0) : 0);
        return ProductCategoryVO.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .iconKey(visualMeta.getIconKey())
                .activeIconKey(visualMeta.getActiveIconKey())
                .sortOrder(category.getSortOrder())
                .build();
    }

    private ProductVO convertToProductVO(Product product, String categoryName) {
        // 解析图片列表
        List<String> imageUrlsList = null;
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            try {
                imageUrlsList = objectMapper.readValue(product.getImageUrls(), new TypeReference<List<String>>() {
                });
            } catch (Exception e) {
                log.warn("解析商品图片列表失败, productId: {}", product.getId(), e);
            }
        }

        return ProductVO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .name(product.getName())
                .coverUrl(product.getCoverUrl())
                .imageUrls(imageUrlsList)
                .summary(product.getSummary())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .sales(product.getSales())
                .tag(product.getTag())
                .build();
    }
}
