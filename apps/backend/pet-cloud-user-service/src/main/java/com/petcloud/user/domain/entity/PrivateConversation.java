package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 私信会话实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("private_conversation")
public class PrivateConversation extends BaseEntity {

    /**
     * 用户1 (较小的ID)
     */
    @TableField("user1_id")
    private Long user1Id;

    /**
     * 用户2 (较大的ID)
     */
    @TableField("user2_id")
    private Long user2Id;

    /**
     * 最后一条消息内容
     */
    @TableField("last_message")
    private String lastMessage;

    /**
     * 最后消息时间
     */
    @TableField("last_time")
    private Date lastTime;

    /**
     * 用户1未读数
     */
    @TableField("unread_1")
    private Integer unread1;

    /**
     * 用户2未读数
     */
    @TableField("unread_2")
    private Integer unread2;
}
