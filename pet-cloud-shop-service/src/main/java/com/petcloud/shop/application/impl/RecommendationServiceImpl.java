package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ProductCategory;
import com.petcloud.shop.domain.entity.ShoppingCart;
import com.petcloud.shop.domain.service.RecommendationService;
import com.petcloud.shop.domain.vo.ProductVO;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductCategoryMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ShoppingCartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ShoppingCartMapper shoppingCartMapper;

    @Override
    public List<ProductVO> getRecommendationsByCart(Long userId, Integer limit) {
        log.info("获取购物车推荐商品，userId: {}, limit: {}", userId, limit);

        // 获取用户购物车中的商品
        LambdaQueryWrapper<ShoppingCart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> cartList = shoppingCartMapper.selectList(cartWrapper);

        if (cartList.isEmpty()) {
            // 购物车为空，返回热销商品
            return getHotProducts(limit);
        }

        // 获取购物车中商品的分类ID
        Set<Long> cartProductIds = new HashSet<>();
        Set<Long> categoryIds = new HashSet<>();

        for (ShoppingCart cart : cartList) {
            cartProductIds.add(cart.getProductId());
            Product product = productMapper.selectById(cart.getProductId());
            if (product != null && product.getCategoryId() != null) {
                categoryIds.add(product.getCategoryId());
            }
        }

        // 查询同分类下的其他商品
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, Product.Status.ONLINE.getCode())
                .notIn(Product::getId, cartProductIds);

        if (!categoryIds.isEmpty()) {
            queryWrapper.in(Product::getCategoryId, categoryIds);
        }

        queryWrapper.orderByDesc(Product::getSales)
                .orderByDesc(Product::getRating)
                .last("LIMIT " + limit);

        List<Product> products = productMapper.selectList(queryWrapper);

        // 如果同分类商品不足，补充热销商品
        if (products.size() < limit) {
            int remaining = limit - products.size();
            Set<Long> existingIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());
            existingIds.addAll(cartProductIds);

            LambdaQueryWrapper<Product> hotWrapper = new LambdaQueryWrapper<>();
            hotWrapper.eq(Product::getStatus, Product.Status.ONLINE.getCode())
                    .notIn(Product::getId, existingIds)
                    .orderByDesc(Product::getSales)
                    .last("LIMIT " + remaining);

            List<Product> hotProducts = productMapper.selectList(hotWrapper);
            products.addAll(hotProducts);
        }

        return convertToProductVOList(products);
    }

    @Override
    public List<ProductVO> getHotProducts(Integer limit) {
        log.info("获取热销商品推荐，limit: {}", limit);

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, Product.Status.ONLINE.getCode())
                .orderByDesc(Product::getSales)
                .orderByDesc(Product::getRating)
                .last("LIMIT " + limit);

        List<Product> products = productMapper.selectList(queryWrapper);
        return convertToProductVOList(products);
    }

    @Override
    public List<ProductVO> getSimilarProducts(Long productId, Integer limit) {
        log.info("获取相似商品推荐，productId: {}, limit: {}", productId, limit);

        Product targetProduct = productMapper.selectById(productId);
        if (targetProduct == null) {
            return new ArrayList<>();
        }

        // 查询同分类的其他商品
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, Product.Status.ONLINE.getCode())
                .ne(Product::getId, productId);

        if (targetProduct.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, targetProduct.getCategoryId());
        }

        queryWrapper.orderByDesc(Product::getSales)
                .orderByDesc(Product::getRating)
                .last("LIMIT " + limit);

        List<Product> products = productMapper.selectList(queryWrapper);

        // 如果同分类商品不足，补充其他热销商品
        if (products.size() < limit) {
            int remaining = limit - products.size();
            Set<Long> existingIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());
            existingIds.add(productId);

            LambdaQueryWrapper<Product> hotWrapper = new LambdaQueryWrapper<>();
            hotWrapper.eq(Product::getStatus, Product.Status.ONLINE.getCode())
                    .notIn(Product::getId, existingIds)
                    .orderByDesc(Product::getSales)
                    .last("LIMIT " + remaining);

            List<Product> hotProducts = productMapper.selectList(hotWrapper);
            products.addAll(hotProducts);
        }

        return convertToProductVOList(products);
    }

    /**
     * 将商品实体列表转换为VO列表
     */
    private List<ProductVO> convertToProductVOList(List<Product> products) {
        return products.stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());
    }

    /**
     * 将商品实体转换为VO
     */
    private ProductVO convertToProductVO(Product product) {
        // 获取分类名称
        String categoryName = "";
        if (product.getCategoryId() != null) {
            ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        // 格式化销量文本
        String salesText = formatSalesText(product.getSales());

        // 确定徽章
        String badge = determineBadge(product);

        return ProductVO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .name(product.getName())
                .coverUrl(product.getCoverUrl())
                .summary(product.getSummary())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .stock(product.getStock())
                .sales(product.getSales())
                .rating(product.getRating())
                .tag(product.getTag())
                .badge(badge)
                .salesText(salesText)
                .build();
    }

    /**
     * 格式化销量文本
     */
    private String formatSalesText(Integer sales) {
        if (sales == null) {
            return "月销0";
        }
        if (sales >= 10000) {
            return "月销" + String.format("%.1f", sales / 10000.0) + "万+";
        } else if (sales >= 1000) {
            return "月销" + (sales / 1000) + "k+";
        } else {
            return "月销" + sales;
        }
    }

    /**
     * 确定商品徽章
     */
    private String determineBadge(Product product) {
        String tag = product.getTag();
        if (tag != null && !tag.isEmpty()) {
            switch (tag.toLowerCase()) {
                case "hot":
                    return "热销";
                case "new":
                    return "新品";
                case "recommend":
                    return "推荐";
                default:
                    return tag;
            }
        }

        // 根据销量自动判断
        if (product.getSales() != null && product.getSales() > 1000) {
            return "热销";
        }

        return "";
    }
}
