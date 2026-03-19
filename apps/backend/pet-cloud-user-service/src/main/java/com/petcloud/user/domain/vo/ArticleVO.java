package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 文章VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleVO {

    /**
     * 文章ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 封面图
     */
    private String coverUrl;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 标签
     */
    private String tag;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    /**
     * 是否已点赞
     */
    private Boolean isLiked;

    /**
     * 是否已收藏
     */
    private Boolean isCollected;
}
