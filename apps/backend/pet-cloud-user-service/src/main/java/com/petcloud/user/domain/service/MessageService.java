package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.MessageVO;

import java.util.List;

/**
 * 消息服务接口
 *
 * @author luohao
 */
public interface MessageService {

    /**
     * 获取消息列表（支持类型过滤）- BE-0.3
     *
     * @param userId 用户ID
     * @param type   消息类型：system/order/consultation/activity，null 表示全部
     */
    List<MessageVO> getMessageList(Long userId, String type);

    /**
     * 标记消息已读
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 全部标记已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCount(Long userId);
}
