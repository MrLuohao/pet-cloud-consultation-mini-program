package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 文章实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {

    /**
     * 文章标题
     */
    @TableField("title")
    private String title;

    /**
     * 封面图
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 摘要
     */
    @TableField("summary")
    private String summary;

    /**
     * 文章内容
     */
    @TableField("content")
    private String content;

    /**
     * 标签
     */
    @TableField("tag")
    private String tag;

    /**
     * 浏览量
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 收藏数
     */
    @TableField("collect_count")
    private Integer collectCount;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("publish_time")
    private Date publishTime;

    /**
     * 状态(0草稿/1已发布)
     */
    @TableField("status")
    private Integer status;

    /**
     * 状态枚举
     */
    public enum Status {
        DRAFT(0, "草稿"),
        PUBLISHED(1, "已发布");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
