package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 会话实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("conversation")
public class Conversation extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 会话类型: ai_chat/doctor_consultation/customer_service
     */
    @TableField("type")
    private String type;

    /**
     * 目标ID(医生ID等)
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 目标名称
     */
    @TableField("target_name")
    private String targetName;

    /**
     * 目标头像
     */
    @TableField("target_avatar")
    private String targetAvatar;

    /**
     * 最后一条消息内容
     */
    @TableField("last_message")
    private String lastMessage;

    /**
     * 最后消息时间
     */
    @TableField("last_message_time")
    private Date lastMessageTime;

    /**
     * 未读消息数
     */
    @TableField("unread_count")
    private Integer unreadCount;

    /**
     * 是否置顶: 0否 1是
     */
    @TableField("is_pinned")
    private Integer isPinned;

    /**
     * 状态: active/archived/deleted
     */
    @TableField("status")
    private String status;

    /**
     * 会话类型枚举
     */
    public enum Type {
        AI_CHAT("ai_chat", "AI助手"),
        DOCTOR_CONSULTATION("doctor_consultation", "医生咨询"),
        CUSTOMER_SERVICE("customer_service", "在线客服");

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

        public static Type fromCode(String code) {
            for (Type type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 会话状态枚举
     */
    public enum Status {
        ACTIVE("active", "活跃"),
        ARCHIVED("archived", "已归档"),
        DELETED("deleted", "已删除");

        private final String code;
        private final String desc;

        Status(String code, String desc) {
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
