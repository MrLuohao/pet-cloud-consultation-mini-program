package com.petcloud.shop.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.petcloud.shop.domain.vo.ReviewSummaryVO;
import com.petcloud.shop.domain.vo.ReviewableOrderItemVO;
import com.petcloud.shop.domain.vo.UserBriefVO;
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

import java.text.SimpleDateFormat;
import java.util.*;
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
    private static final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    );

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
                .orderByDesc(ProductReview::getCreateTime)
                .last("LIMIT 5");
        List<ProductReview> reviews = productReviewMapper.selectList(reviewWrapper);

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
                .reviews(reviews.stream()
                        .map(r -> convertToReviewVO(r, false, finalUserMap.get(r.getUserId())))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<ProductReviewVO> getProductReviews(Long productId, Integer page, Integer pageSize) {
        return getProductReviewsWithFilter(productId, "all", page, pageSize, null);
    }

    @Override
    public List<ProductReviewVO> getProductReviewsWithFilter(Long productId, String filter, Integer page, Integer pageSize, Long userId) {
        int size = pageSize != null && pageSize > 0 ? pageSize : 10;
        int offset = (page != null && page > 0 ? page - 1 : 0) * size;

        LambdaQueryWrapper<ProductReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductReview::getProductId, productId);

        // 根据筛选条件添加过滤
        // 好评: 4-5星, 差评: 1-2星
        if ("good".equals(filter)) {
            queryWrapper.ge(ProductReview::getRating, 4);
        } else if ("bad".equals(filter)) {
            queryWrapper.le(ProductReview::getRating, 2);
        } else if ("withImages".equals(filter)) {
            // 使用 JSON_LENGTH 判断数组是否非空
            queryWrapper.apply("JSON_LENGTH(images) > 0");
        }

        queryWrapper.orderByDesc(ProductReview::getCreateTime)
                .last("LIMIT " + offset + ", " + size);

        List<ProductReview> reviews = productReviewMapper.selectList(queryWrapper);

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
    public void createReview(Long userId, Long orderItemId, Long productId, Integer rating, String content, String images) {
        // 参数校验
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(RespType.REVIEW_RATING_INVALID);
        }
        if (content != null && content.length() > 500) {
            throw new BusinessException(RespType.REVIEW_CONTENT_TOO_LONG);
        }

        // 验证订单项存在且属于该用户
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

        // 验证订单是否已完成
        if (!OrderInfo.Status.COMPLETED.getCode().equals(orderInfo.getStatus())) {
            throw new BusinessException(RespType.ORDER_NOT_COMPLETED);
        }

        // 验证订单项是否已评价
        LambdaQueryWrapper<ProductReview> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(ProductReview::getOrderItemId, orderItemId);
        if (productReviewMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException(RespType.REVIEW_ALREADY_EXISTS);
        }

        // 验证商品ID与订单项中的商品ID一致
        if (!orderItem.getProductId().equals(productId)) {
            throw new BusinessException(RespType.PRODUCT_NOT_MATCH);
        }

        // 从用户服务获取用户信息
        UserBriefVO userInfo = userRemoteService.getUser(userId);
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
        // 处理图片：将逗号分隔的字符串转换为JSON数组格式
        String imagesJson = null;
        if (images != null && !images.trim().isEmpty()) {
            // 转换为JSON数组格式
            String[] imageArray = images.split(",");
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < imageArray.length; i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(imageArray[i].trim()).append("\"");
            }
            sb.append("]");
            imagesJson = sb.toString();
        }
        review.setImages(imagesJson);
        review.setLikeCount(0);
        productReviewMapper.insert(review);

        // 更新商品评价数和评分
        updateProductReviewStats(productId);
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
            throw new RuntimeException("评价不存在");
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
        return dateFormat.get().format(date);
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
}
