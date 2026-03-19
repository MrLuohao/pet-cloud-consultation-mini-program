package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 课程实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    /**
     * 课程标题
     */
    @TableField("title")
    private String title;

    /**
     * 课程描述
     */
    @TableField("description")
    private String description;

    /**
     * 封面图
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 课时数量
     */
    @TableField("lesson_count")
    private Integer lessonCount;

    /**
     * 学习人数
     */
    @TableField("student_count")
    private Integer studentCount;

    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 标签(入门/进阶/热门)
     */
    @TableField("tag")
    private String tag;

    /**
     * 状态(0下架/1上架)
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 讲师姓名
     */
    @TableField("instructor_name")
    private String instructorName;

    /**
     * 讲师头像
     */
    @TableField("instructor_avatar")
    private String instructorAvatar;

    /**
     * 讲师简介
     */
    @TableField("instructor_bio")
    private String instructorBio;

    /**
     * 章节列表（JSON格式）
     */
    @TableField("chapters")
    private String chapters;

    /**
     * 状态枚举
     */
    public enum Status {
        OFFLINE(0, "下架"),
        ONLINE(1, "上架");

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
