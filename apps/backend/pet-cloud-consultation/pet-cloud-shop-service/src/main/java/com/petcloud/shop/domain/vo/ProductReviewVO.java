package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品评价VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewVO {

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 订单项ID
     */
    private Long orderItemId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 评分
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片列表
     */
    private List<String> images;

    /**
     * 商家回复
     */
    private String replyContent;

    /**
     * 评价时间
     */
    private String createTime;

    /**
     * 更新时间（编辑时间）
     */
    private String updateTime;

    /**
     * 是否已编辑
     */
    private Boolean isEdited;

    /**
     * 是否已验证购买
     */
    private Boolean isVerified;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 追评内容
     */
    private String followUpContent;

    /**
     * 追评时间
     */
    private String followUpTime;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ProductReviewVO vo = new ProductReviewVO();

        public Builder id(Long id) {
            vo.id = id;
            return this;
        }

        public Builder productId(Long productId) {
            vo.productId = productId;
            return this;
        }

        public Builder orderItemId(Long orderItemId) {
            vo.orderItemId = orderItemId;
            return this;
        }

        public Builder userId(Long userId) {
            vo.userId = userId;
            return this;
        }

        public Builder userNickname(String userNickname) {
            vo.userNickname = userNickname;
            return this;
        }

        public Builder userAvatar(String userAvatar) {
            vo.userAvatar = userAvatar;
            return this;
        }

        public Builder rating(Integer rating) {
            vo.rating = rating;
            return this;
        }

        public Builder content(String content) {
            vo.content = content;
            return this;
        }

        public Builder images(List<String> images) {
            vo.images = images;
            return this;
        }

        public Builder replyContent(String replyContent) {
            vo.replyContent = replyContent;
            return this;
        }

        public Builder createTime(String createTime) {
            vo.createTime = createTime;
            return this;
        }

        public Builder updateTime(String updateTime) {
            vo.updateTime = updateTime;
            return this;
        }

        public Builder isEdited(Boolean isEdited) {
            vo.isEdited = isEdited;
            return this;
        }

        public Builder isVerified(Boolean isVerified) {
            vo.isVerified = isVerified;
            return this;
        }

        public Builder likeCount(Integer likeCount) {
            vo.likeCount = likeCount;
            return this;
        }

        public Builder isLiked(Boolean isLiked) {
            vo.isLiked = isLiked;
            return this;
        }

        public Builder followUpContent(String followUpContent) {
            vo.followUpContent = followUpContent;
            return this;
        }

        public Builder followUpTime(String followUpTime) {
            vo.followUpTime = followUpTime;
            return this;
        }

        public ProductReviewVO build() {
            return vo;
        }
    }
}
