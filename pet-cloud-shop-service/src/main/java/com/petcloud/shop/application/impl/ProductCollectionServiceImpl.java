package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ProductCollection;
import com.petcloud.shop.domain.service.ProductCollectionService;
import com.petcloud.shop.domain.vo.ProductCollectionVO;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductCollectionMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品收藏服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCollectionServiceImpl implements ProductCollectionService {

    private final ProductCollectionMapper productCollectionMapper;
    private final ProductMapper productMapper;

    @Override
    public Long addCollection(Long userId, Long productId) {
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(RespType.PRODUCT_NOT_FOUND);
        }

        // 检查是否已收藏
        LambdaQueryWrapper<ProductCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCollection::getUserId, userId)
                .eq(ProductCollection::getProductId, productId);
        ProductCollection existing = productCollectionMapper.selectOne(queryWrapper);

        if (existing != null) {
            // 已收藏，返回现有收藏ID
            log.info("商品已收藏，userId: {}, productId: {}", userId, productId);
            return existing.getId();
        }

        // 新增收藏
        ProductCollection collection = new ProductCollection();
        collection.setUserId(userId);
        collection.setProductId(productId);
        collection.setProductName(product.getName());
        collection.setProductImage(product.getCoverUrl());
        collection.setProductPrice(product.getPrice());
        productCollectionMapper.insert(collection);

        log.info("添加收藏成功，userId: {}, productId: {}, collectionId: {}", userId, productId, collection.getId());
        return collection.getId();
    }

    @Override
    public void removeCollection(Long userId, Long productId) {
        LambdaQueryWrapper<ProductCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCollection::getUserId, userId)
                .eq(ProductCollection::getProductId, productId);
        int deleted = productCollectionMapper.delete(queryWrapper);

        if (deleted > 0) {
            log.info("取消收藏成功，userId: {}, productId: {}", userId, productId);
        } else {
            log.info("收藏记录不存在，userId: {}, productId: {}", userId, productId);
        }
    }

    @Override
    public List<ProductCollectionVO> getCollectionList(Long userId) {
        LambdaQueryWrapper<ProductCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCollection::getUserId, userId)
                .orderByDesc(ProductCollection::getCreateTime);
        List<ProductCollection> collections = productCollectionMapper.selectList(queryWrapper);

        return collections.stream()
                .map(collection -> {
                    // 获取最新的商品信息
                    Product product = productMapper.selectById(collection.getProductId());

                    ProductCollectionVO.Builder builder = ProductCollectionVO.builder()
                            .id(collection.getId())
                            .productId(collection.getProductId())
                            .productName(collection.getProductName())
                            .productImage(collection.getProductImage())
                            .productPrice(collection.getProductPrice())
                            .collectTime(collection.getCreateTime());

                    // 如果商品存在，更新最新信息
                    if (product != null) {
                        builder.productName(product.getName())
                                .productImage(product.getCoverUrl())
                                .productPrice(product.getPrice())
                                .originalPrice(product.getOriginalPrice())
                                .stock(product.getStock())
                                .status(product.getStatus());
                    } else {
                        // 商品已下架或删除
                        builder.stock(0)
                                .status(0);
                    }

                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isCollected(Long userId, Long productId) {
        LambdaQueryWrapper<ProductCollection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCollection::getUserId, userId)
                .eq(ProductCollection::getProductId, productId);
        Long count = productCollectionMapper.selectCount(queryWrapper);
        // 防止 NPE：如果 selectCount 返回 null，视为 0
        return count != null && count > 0;
    }

    @Override
    public Boolean toggleCollection(Long userId, Long productId) {
        Boolean collected = isCollected(userId, productId);
        if (collected) {
            // 已收藏，取消收藏
            removeCollection(userId, productId);
            log.info("切换收藏状态：取消收藏，userId: {}, productId: {}", userId, productId);
            return false;
        } else {
            // 未收藏，添加收藏
            addCollection(userId, productId);
            log.info("切换收藏状态：添加收藏，userId: {}, productId: {}", userId, productId);
            return true;
        }
    }
}
