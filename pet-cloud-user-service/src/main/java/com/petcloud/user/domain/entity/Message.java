package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 类型(system/consult/order/activity)
     */
    @TableField("type")
    private String type;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 额外数据(JSON)
     */
    @TableField("extra_data")
    private String extraData;

    /**
     * 是否已读(0未读/1已读)
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 消息类型枚举
     */
    public enum Type {
        SYSTEM("system", "系统通知"),
        CONSULT("consult", "咨询回复"),
        ORDER("order", "订单通知"),
        ACTIVITY("activity", "活动通知");

        private final String code;
        private final String desc;

        Type(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
