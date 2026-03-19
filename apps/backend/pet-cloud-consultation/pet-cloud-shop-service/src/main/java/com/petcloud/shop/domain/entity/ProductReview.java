package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 商品评价实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_review")
public class ProductReview extends BaseEntity {

    /**
     * 订单项ID
     */
    @TableField("order_item_id")
    private Long orderItemId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户昵称
     */
    @TableField("user_nickname")
    private String userNickname;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 评分1-5
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 评价内容
     */
    @TableField("content")
    private String content;

    /**
     * 评价图片
     */
    @TableField("images")
    private String images;

    /**
     * 商家回复
     */
    @TableField("reply_content")
    private String replyContent;

    /**
     * 回复时间
     */
    @TableField("reply_time")
    private String replyTime;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 追评内容
     */
    @TableField("follow_up_content")
    private String followUpContent;

    /**
     * 追评时间
     */
    @TableField("follow_up_time")
    private Date followUpTime;

    /**
     * 更新时间（编辑时间）
     */
    @TableField("update_time")
    private Date updateTime;
}
