package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.shop.domain.entity.OrderInfo;
import com.petcloud.shop.domain.entity.OrderItem;
import com.petcloud.shop.domain.entity.Product;
import com.petcloud.shop.domain.entity.ProductReview;
import com.petcloud.shop.domain.entity.ProductReviewLike;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.shop.domain.service.ProductDetailService;
import com.petcloud.shop.domain.service.UserRemoteService;
import com.petcloud.shop.domain.vo.ProductDetailVO;
import com.petcloud.shop.domain.vo.ProductReviewVO;
import com.petcloud.shop.domain.vo.ProductSpecGroupVO;
import com.petcloud.shop.domain.vo.ProductSpecOptionVO;
import com.petcloud.shop.domain.vo.ProductStorySectionVO;
import com.petcloud.shop.domain.vo.ProductUsageNoteVO;
import com.petcloud.shop.domain.vo.ReviewSummaryVO;
import com.petcloud.shop.domain.vo.ReviewableOrderItemVO;
import com.petcloud.shop.domain.vo.UserBriefVO;
import com.petcloud.shop.domain.dto.ProductReviewDTO;
import com.petcloud.shop.domain.enums.ProductReviewFilterType;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderInfoMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.OrderItemMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductCategoryMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductReviewLikeMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.ProductReviewMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品详情服务实现类
 *
 * @author luohao
 */
@Service
@RequiredArgsConstructor
public class ProductDetailServiceImpl implements ProductDetailService {

    private static final Logger log = LoggerFactory.getLogger(ProductDetailServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int RATING_GOOD_MIN = 4;
    private static final int RATING_BAD_MAX = 2;
    private static final int DEFAULT_REVIEW_LIMIT = 3;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductReviewMapper productReviewMapper;
    private final ProductReviewLikeMapper productReviewLikeMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final UserRemoteService userRemoteService;

    @Override
    public ProductDetailVO getProductDetail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return null;
        }

        // 获取分类名称
        String categoryName = null;
        if (product.getCategoryId() != null) {
            var category = productCategoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        // 获取评价列表
        LambdaQueryWrapper<ProductReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(ProductReview::getProductId, productId)
                .orderByDesc(ProductReview::getCreateTime);

        Page<ProductReview> reviewPage = new Page<>(1, DEFAULT_REVIEW_LIMIT);
        List<ProductReview> reviews = productReviewMapper.selectPage(reviewPage, reviewWrapper).getRecords();

        // 批量获取用户信息
        Map<Long, UserBriefVO> userMap = Collections.emptyMap();
        if (!reviews.isEmpty()) {
            List<Long> userIds = reviews.stream()
                    .map(ProductReview::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            userMap = userRemoteService.batchGetUsers(userIds);
        }

        // 解析商品图片
        List<String> imageUrlList = parseJsonArray(product.getImageUrls());
        List<ProductSpecGroupVO> specGroups = parseSpecGroups(product);
        ProductDetailContentConfig detailContent = parseDetailContent(product.getDetailContentJson());

        final Map<Long, UserBriefVO> finalUserMap = userMap;
        return ProductDetailVO.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .name(product.getName())
                .coverUrl(product.getCoverUrl())
                .imageUrls(imageUrlList)
                .summary(product.getSummary())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .stock(product.getStock())
                .sales(product.getSales())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .tag(product.getTag())
                .specGroups(specGroups)
                .highlights(detailContent.getHighlights())
                .storySections(detailContent.getStorySections())
                .usageNote(detailContent.getUsageNote())
                .reviews(reviews.stream()
                        .map(r -> convertToReviewVO(r, false, finalUserMap.get(r.getUserId())))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<ProductReviewVO> getProductReviews(Long productId, Integer page, Integer pageSize) {
        return getProductReviewsWithFilter(productId, ProductReviewFilterType.DEFAULT_CODE, page, pageSize, null);
    }

    @Override
    public List<ProductReviewVO> getProductReviewsWithFilter(Long productId, String filter, Integer page, Integer pageSize, Long userId) {
        int size = pageSize != null && pageSize > 0 ? pageSize : 10;
        int offset = (page != null && page > 0 ? page - 1 : 0) * size;
        int currentPage = page != null && page > 0 ? page : 1;

        LambdaQueryWrapper<ProductReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductReview::getProductId, productId);

        // 根据筛选条件添加过滤
        // 好评: 4-5星, 差评: 1-2星
        ProductReviewFilterType filterType = ProductReviewFilterType.fromCode(filter);
        if (ProductReviewFilterType.GOOD == filterType) {
            queryWrapper.ge(ProductReview::getRating, RATING_GOOD_MIN);
        } else if (ProductReviewFilterType.BAD == filterType) {
            queryWrapper.le(ProductReview::getRating, RATING_BAD_MAX);
        } else if (ProductReviewFilterType.WITH_IMAGES == filterType) {
            // 使用 JSON_LENGTH 判断数组是否非空
            queryWrapper.apply("JSON_LENGTH(images) > 0");
        }

        queryWrapper.orderByDesc(ProductReview::getCreateTime);

        // 使用 Page API 避免 SQL 拼接
        Page<ProductReview> pageParam = new Page<>(currentPage, size);
        List<ProductReview> reviews = productReviewMapper.selectPage(pageParam, queryWrapper).getRecords();

        // 批量获取用户信息
        Map<Long, UserBriefVO> userMap = Collections.emptyMap();
        if (!reviews.isEmpty()) {
            List<Long> userIds = reviews.stream()
                    .map(ProductReview::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            userMap = userRemoteService.batchGetUsers(userIds);
        }

        // 批量查询点赞状态（解决N+1问题）
        Set<Long> likedReviewIds = new java.util.HashSet<>();
        if (userId != null && !reviews.isEmpty()) {
            List<Long> reviewIds = reviews.stream()
                    .map(ProductReview::getId)
                    .collect(Collectors.toList());

            LambdaQueryWrapper<ProductReviewLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.in(ProductReviewLike::getReviewId, reviewIds)
                    .eq(ProductReviewLike::getUserId, userId);
            List<ProductReviewLike> userLikes = productReviewLikeMapper.selectList(likeWrapper);

            likedReviewIds = userLikes.stream()
                    .map(ProductReviewLike::getReviewId)
                    .collect(Collectors.toSet());
        }

        final Set<Long> finalLikedReviewIds = likedReviewIds;
        final Map<Long, UserBriefVO> finalUserMap = userMap;
        return reviews.stream()
                .map(r -> convertToReviewVO(r, finalLikedReviewIds.contains(r.getId()), finalUserMap.get(r.getUserId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createReview(Long userId, ProductReviewDTO dto) {
        validateReviewParams(dto.getRating(), dto.getContent());
        OrderItem orderItem = validateAndGetOrderItem(userId, dto.getOrderItemId(), dto.getProductId());
        UserBriefVO userInfo = getUserInfo(userId);
        ProductReview review = buildReviewEntity(userId, dto.getOrderItemId(), dto.getProductId(),
                dto.getRating(), dto.getContent(), dto.getImages(), userInfo);
        productReviewMapper.insert(review);
        updateProductReviewStats(dto.getProductId());
    }

    /**
     * 验证评价参数
     */
    private void validateReviewParams(Integer rating, String content) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(RespType.REVIEW_RATING_INVALID);
        }
        if (content != null && content.length() > 500) {
            throw new BusinessException(RespType.REVIEW_CONTENT_TOO_LONG);
        }
    }

    /**
     * 验证订单项并返回
     */
    private OrderItem validateAndGetOrderItem(Long userId, Long orderItemId, Long productId) {
        if (orderItemId == null) {
            throw new BusinessException(RespType.ORDER_ITEM_NOT_FOUND);
        }
        OrderItem orderItem = orderItemMapper.selectById(orderItemId);
        if (orderItem == null) {
            throw new BusinessException(RespType.ORDER_ITEM_NOT_FOUND);
        }

        OrderInfo orderInfo = orderInfoMapper.selectById(orderItem.getOrderId());
        if (orderInfo == null || !orderInfo.getUserId().equals(userId)) {
            throw new BusinessException(RespType.ORDER_NO_PERMISSION);
        }

        if (!OrderInfo.Status.COMPLETED.getCode().equals(orderInfo.getStatus())) {
            throw new BusinessException(RespType.ORDER_NOT_COMPLETED);
        }

        LambdaQueryWrapper<ProductReview> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(ProductReview::getOrderItemId, orderItemId);
        Long existCount = productReviewMapper.selectCount(existWrapper);
        // 防止 NPE：如果 selectCount 返回 null，视为 0
        if (existCount != null && existCount > 0) {
            throw new BusinessException(RespType.REVIEW_ALREADY_EXISTS);
        }

        if (!orderItem.getProductId().equals(productId)) {
            throw new BusinessException(RespType.PRODUCT_NOT_MATCH);
        }
        return orderItem;
    }

    /**
     * 获取用户信息
     */
    private UserBriefVO getUserInfo(Long userId) {
        return userRemoteService.getUser(userId);
    }

    /**
     * 构建评价实体
     */
    private ProductReview buildReviewEntity(Long userId, Long orderItemId, Long productId,
                                            Integer rating, String content, String images, UserBriefVO userInfo) {
        String nickname = userInfo != null && userInfo.getNickname() != null ? userInfo.getNickname() : "用户" + userId;
        String avatarUrl = userInfo != null ? userInfo.getAvatarUrl() : null;

        ProductReview review = new ProductReview();
        review.setOrderItemId(orderItemId);
        review.setProductId(productId);
        review.setUserId(userId);
        review.setUserNickname(nickname);
        review.setUserAvatar(avatarUrl);
        review.setRating(rating);
        review.setContent(content);
        review.setImages(formatReviewImages(images));
        review.setLikeCount(0);
        return review;
    }

    /**
     * 格式化评价图片为JSON数组
     */
    private String formatReviewImages(String images) {
        if (images == null || images.trim().isEmpty()) {
            return null;
        }
        String[] imageArray = images.split(",");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < imageArray.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(imageArray[i].trim()).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public ReviewableOrderItemVO getReviewableOrderItem(Long userId, Long productId) {
        // 查询用户已完成的订单项
        // 1. 先查询用户已完成(status=3)的订单
        LambdaQueryWrapper<OrderInfo> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getStatus, OrderInfo.Status.COMPLETED.getCode());
        List<OrderInfo> completedOrders = orderInfoMapper.selectList(orderWrapper);

        if (completedOrders.isEmpty()) {
            // 未购买过任何商品
            return ReviewableOrderItemVO.builder()
                    .hasPurchased(false)
                    .allReviewed(false)
                    .build();
        }

        List<Long> orderIds = completedOrders.stream()
                .map(OrderInfo::getId)
                .collect(Collectors.toList());

        // 2. 查询这些订单中包含该商品的订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds)
                .eq(OrderItem::getProductId, productId);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        if (orderItems.isEmpty()) {
            // 未购买过此商品
            return ReviewableOrderItemVO.builder()
                    .hasPurchased(false)
                    .allReviewed(false)
                    .build();
        }

        // 3. 批量查询已评价的订单项ID（解决N+1问题）
        List<Long> orderItemIds = orderItems.stream()
                .map(OrderItem::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<ProductReview> reviewedWrapper = new LambdaQueryWrapper<>();
        reviewedWrapper.in(ProductReview::getOrderItemId, orderItemIds);
        List<ProductReview> existingReviews = productReviewMapper.selectList(reviewedWrapper);

        Set<Long> reviewedOrderItemIds = existingReviews.stream()
                .map(ProductReview::getOrderItemId)
                .collect(Collectors.toSet());

        // 4. 查找第一个未评价的订单项
        Map<Long, OrderInfo> orderMap = completedOrders.stream()
                .collect(Collectors.toMap(OrderInfo::getId, o -> o));

        for (OrderItem item : orderItems) {
            if (!reviewedOrderItemIds.contains(item.getId())) {
                OrderInfo order = orderMap.get(item.getOrderId());
                return ReviewableOrderItemVO.builder()
                        .orderItemId(item.getId())
                        .orderId(item.getOrderId())
                        .orderNo(order != null ? order.getOrderNo() : null)
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .coverUrl(item.getCoverUrl())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .hasPurchased(true)
                        .allReviewed(false)
                        .build();
            }
        }

        // 已购买但全部已评价
        return ReviewableOrderItemVO.builder()
                .hasPurchased(true)
                .allReviewed(true)
                .build();
    }

    @Override
    @Transactional
    public ProductReviewVO toggleReviewLike(Long userId, Long reviewId) {
        ProductReview review = productReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(RespType.REVIEW_NOT_FOUND);
        }

        // 查询是否已点赞
        LambdaQueryWrapper<ProductReviewLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(ProductReviewLike::getReviewId, reviewId)
                .eq(ProductReviewLike::getUserId, userId);
        ProductReviewLike existingLike = productReviewLikeMapper.selectOne(likeWrapper);

        boolean isLiked;
        if (existingLike != null) {
            // 已点赞，取消点赞
            productReviewLikeMapper.deleteById(existingLike.getId());
            review.setLikeCount(Math.max(0, (review.getLikeCount() != null ? review.getLikeCount() : 0) - 1));
            isLiked = false;
        } else {
            // 未点赞，添加点赞
            ProductReviewLike like = new ProductReviewLike();
            like.setReviewId(reviewId);
            like.setUserId(userId);
            productReviewLikeMapper.insert(like);
            review.setLikeCount((review.getLikeCount() != null ? review.getLikeCount() : 0) + 1);
            isLiked = true;
        }

        productReviewMapper.updateById(review);

        return ProductReviewVO.builder()
                .id(reviewId)
                .isLiked(isLiked)
                .likeCount(review.getLikeCount())
                .build();
    }

    @Override
    @Transactional
    public void updateReview(Long userId, Long reviewId, Integer rating, String content, String images) {
        ProductReview review = productReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(RespType.REVIEW_NOT_FOUND);
        }

        // 验证是否是自己的评价
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(RespType.REVIEW_NO_PERMISSION);
        }

        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        review.setUpdateTime(new Date());
        productReviewMapper.updateById(review);

        // 更新商品评分
        updateProductReviewStats(review.getProductId());
    }

    @Override
    @Transactional
    public void addFollowUp(Long userId, Long reviewId, String content) {
        ProductReview review = productReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(RespType.REVIEW_NOT_FOUND);
        }

        // 验证是否是自己的评价
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(RespType.REVIEW_NO_PERMISSION);
        }

        // 验证是否已追评
        if (review.getFollowUpContent() != null && !review.getFollowUpContent().isEmpty()) {
            throw new BusinessException(RespType.REVIEW_ALREADY_FOLLOW_UP);
        }

        // 验证是否在30天内
        if (review.getCreateTime() != null) {
            long daysDiff = TimeUnit.MILLISECONDS.toDays(
                    new Date().getTime() - review.getCreateTime().getTime()
            );
            if (daysDiff > 30) {
                throw new BusinessException(RespType.REVIEW_FOLLOW_UP_EXPIRED);
            }
        }

        review.setFollowUpContent(content);
        review.setFollowUpTime(new Date());
        productReviewMapper.updateById(review);
    }

    @Override
    public ReviewSummaryVO getReviewSummary(Long productId) {
        // 查询所有评价
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, productId)
                .select(ProductReview::getRating, ProductReview::getImages);
        List<ProductReview> allReviews = productReviewMapper.selectList(wrapper);

        int total = allReviews.size();
        int goodCount = 0;
        int badCount = 0;
        int withImagesCount = 0;
        double ratingSum = 0;

        for (ProductReview review : allReviews) {
            // 统计好评（4-5星）
            if (review.getRating() != null && review.getRating() >= 4) {
                goodCount++;
            }
            // 统计差评（1-2星）
            if (review.getRating() != null && review.getRating() <= 2) {
                badCount++;
            }
            // 统计有图评价（解析JSON数组，判断是否非空）
            List<String> imageList = parseJsonArray(review.getImages());
            if (imageList != null && !imageList.isEmpty()) {
                withImagesCount++;
            }
            // 累计评分
            if (review.getRating() != null) {
                ratingSum += review.getRating();
            }
        }

        // 计算平均评分
        java.math.BigDecimal avgRating = total > 0
                ? java.math.BigDecimal.valueOf(ratingSum / total).setScale(1, java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.valueOf(5.0);

        return ReviewSummaryVO.builder()
                .total(total)
                .goodCount(goodCount)
                .badCount(badCount)
                .withImagesCount(withImagesCount)
                .avgRating(avgRating)
                .build();
    }

    private ProductReviewVO convertToReviewVO(ProductReview review, boolean isLiked, UserBriefVO userInfo) {
        // 优先使用从用户服务获取的实时用户信息，如果没有则使用评价表中存储的信息
        String nickname = userInfo != null && userInfo.getNickname() != null ? userInfo.getNickname() : review.getUserNickname();
        String avatarUrl = userInfo != null && userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : review.getUserAvatar();

        return ProductReviewVO.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .orderItemId(review.getOrderItemId())
                .userId(review.getUserId())
                .userNickname(nickname)
                .userAvatar(avatarUrl)
                .rating(review.getRating())
                .content(review.getContent())
                .images(parseJsonArray(review.getImages()))
                .replyContent(review.getReplyContent())
                .createTime(formatDate(review.getCreateTime()))
                .updateTime(formatDate(review.getUpdateTime()))
                .isEdited(review.getUpdateTime() != null)
                .isVerified(review.getOrderItemId() != null)
                .likeCount(review.getLikeCount() != null ? review.getLikeCount() : 0)
                .isLiked(isLiked)
                .followUpContent(review.getFollowUpContent())
                .followUpTime(formatDate(review.getFollowUpTime()))
                .build();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
    }

    private void updateProductReviewStats(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null) {
            // 使用聚合查询避免加载所有评价到内存
            LambdaQueryWrapper<ProductReview> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(ProductReview::getProductId, productId);
            Long reviewCount = productReviewMapper.selectCount(countWrapper);
            product.setReviewCount(reviewCount != null ? reviewCount.intValue() : 0);

            // 使用数据库计算平均评分，避免内存问题
            // 这里简化处理：评价数量少时直接计算，数量多时保持原评分
            if (reviewCount != null && reviewCount > 0 && reviewCount <= 1000) {
                List<ProductReview> allReviews = productReviewMapper.selectList(
                        new LambdaQueryWrapper<ProductReview>()
                                .eq(ProductReview::getProductId, productId)
                                .select(ProductReview::getRating)
                );
                double avgRating = allReviews.stream()
                        .mapToInt(ProductReview::getRating)
                        .average()
                        .orElse(5.0);
                product.setRating(java.math.BigDecimal.valueOf(avgRating));
            }
            productMapper.updateById(product);
        }
    }

    /**
     * 解析JSON数组字符串为List
     */
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            // 尝试逗号分隔格式
            return List.of(json.split(","));
        }
    }

    private List<ProductSpecGroupVO> parseSpecGroups(Product product) {
        List<ProductSpecGroupVO> specGroups = readJson(
                product.getSpecGroupsJson(),
                new TypeReference<List<ProductSpecGroupVO>>() {},
                List.of()
        );

        if (!specGroups.isEmpty()) {
            return specGroups;
        }

        String defaultSpec = safeText(product.getDefaultSpec());
        if (defaultSpec.isEmpty()) {
            return List.of();
        }

        return List.of(ProductSpecGroupVO.builder()
                .key("default")
                .label("规格")
                .selectedValue(defaultSpec)
                .options(List.of(ProductSpecOptionVO.builder()
                        .value(defaultSpec)
                        .label(defaultSpec)
                        .hint("默认")
                        .build()))
                .build());
    }

    private ProductDetailContentConfig parseDetailContent(String json) {
        ProductDetailContentConfig config = readJson(
                json,
                new TypeReference<ProductDetailContentConfig>() {},
                new ProductDetailContentConfig()
        );

        if (config.getHighlights() == null) {
            config.setHighlights(List.of());
        }
        if (config.getStorySections() == null) {
            config.setStorySections(List.of());
        }
        return config;
    }

    private <T> T readJson(String json, TypeReference<T> typeReference, T fallback) {
        if (json == null || json.isBlank()) {
            return fallback;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.warn("解析商品扩展配置失败: {}", json, e);
            return fallback;
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static class ProductDetailContentConfig {
        private List<String> highlights = List.of();
        private List<ProductStorySectionVO> storySections = List.of();
        private ProductUsageNoteVO usageNote;

        public List<String> getHighlights() {
            return highlights;
        }

        public void setHighlights(List<String> highlights) {
            this.highlights = highlights;
        }

        public List<ProductStorySectionVO> getStorySections() {
            return storySections;
        }

        public void setStorySections(List<ProductStorySectionVO> storySections) {
            this.storySections = storySections;
        }

        public ProductUsageNoteVO getUsageNote() {
            return usageNote;
        }

        public void setUsageNote(ProductUsageNoteVO usageNote) {
            this.usageNote = usageNote;
        }
    }
}
