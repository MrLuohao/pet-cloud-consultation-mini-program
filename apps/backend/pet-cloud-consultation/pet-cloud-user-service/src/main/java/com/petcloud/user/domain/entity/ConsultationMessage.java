package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 咨询聊天记录实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consultation_message")
public class ConsultationMessage extends BaseEntity {

    /**
     * 咨询ID
     */
    @TableField("consultation_id")
    private Long consultationId;

    /**
     * 发送者ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 发送者类型：1用户 2医生
     */
    @TableField("sender_type")
    private Integer senderType;

    /**
     * 发送者名称
     */
    @TableField("sender_name")
    private String senderName;

    /**
     * 发送者头像
     */
    @TableField("sender_avatar")
    private String senderAvatar;

    /**
     * 消息类型：1文字 2图片 3语音
     */
    @TableField("message_type")
    private Integer messageType;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 媒体文件URL
     */
    @TableField("media_url")
    private String mediaUrl;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 发送者类型枚举
     */
    public enum SenderType {
        USER(1, "用户"),
        DOCTOR(2, "医生");

        private final Integer code;
        private final String desc;

        SenderType(Integer code, String desc) {
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

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        TEXT(1, "文字"),
        IMAGE(2, "图片"),
        VOICE(3, "语音");

        private final Integer code;
        private final String desc;

        MessageType(Integer code, String desc) {
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
