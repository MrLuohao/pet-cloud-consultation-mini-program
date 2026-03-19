package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ProductCategory;
import com.petcloud.shop.domain.service.SearchService;
import com.petcloud.shop.domain.vo.ProductVO;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类
 *
 * @author luohao
 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public List<ProductVO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, Product.Status.ONLINE.getCode())
                .and(wrapper -> wrapper.like(Product::getName, keyword)
                        .or()
                        .like(Product::getSummary, keyword));
        queryWrapper.orderByDesc(Product::getSales);

        List<Product> products = productMapper.selectList(queryWrapper);

        // 获取分类名称映射
        List<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .toList();

        // 修复：空集合检查，防止 SQL "WHERE id IN ()" 语法错误
        final Map<Long, String> categoryNameMap;
        if (!categoryIds.isEmpty()) {
            categoryNameMap = productCategoryMapper.selectBatchIds(categoryIds)
                    .stream()
                    .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
        } else {
            categoryNameMap = Map.of();
        }

        return products.stream()
                .map(product -> ProductVO.builder()
                        .id(product.getId())
                        .categoryId(product.getCategoryId())
                        .categoryName(categoryNameMap.get(product.getCategoryId()))
                        .name(product.getName())
                        .coverUrl(product.getCoverUrl())
                        .summary(product.getSummary())
                        .price(product.getPrice())
                        .originalPrice(product.getOriginalPrice())
                        .sales(product.getSales())
                        .tag(product.getTag())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getHotKeywords() {
        // Return default hot keywords for now
        // TODO: Implement database-backed hot keywords
        return Arrays.asList("猫粮", "狗粮", "驱虫", "疫苗", "宠物医院", "美容", "洗护", "玩具");
    }
}
