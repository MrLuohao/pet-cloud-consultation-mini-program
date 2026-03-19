package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 咨询消息VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationMessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 咨询ID
     */
    private Long consultationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者类型：1用户 2医生
     */
    private Integer senderType;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 消息类型：1文字 2图片 3语音
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 媒体文件URL
     */
    private String mediaUrl;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 发送时间
     */
    private String createTime;
}
