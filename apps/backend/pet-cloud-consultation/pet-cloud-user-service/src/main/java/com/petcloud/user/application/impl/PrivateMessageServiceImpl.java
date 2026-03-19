package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.core.utils.DateUtils;
import com.petcloud.user.domain.dto.PrivateMessageDTO;
import com.petcloud.user.domain.enums.PrivateMessageType;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.entity.PrivateConversation;
import com.petcloud.user.domain.entity.PrivateMessage;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.service.PrivateMessageService;
import com.petcloud.user.domain.vo.PrivateConversationVO;
import com.petcloud.user.domain.vo.PrivateMessageVO;
import com.petcloud.user.infrastructure.persistence.mapper.PrivateConversationMapper;
import com.petcloud.user.infrastructure.persistence.mapper.PrivateMessageMapper;
import com.petcloud.user.infrastructure.persistence.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 私信服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final PrivateConversationMapper conversationMapper;
    private final PrivateMessageMapper messageMapper;
    private final WxUserMapper wxUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long senderId, PrivateMessageDTO dto) {
        Long receiverId = dto.getReceiverId();

        // 不能给自己发消息
        if (senderId.equals(receiverId)) {
            throw new BusinessException(UserRespType.PRIVATE_MESSAGE_SEND_TO_SELF_FORBIDDEN);
        }

        // 检查接收者是否存在
        WxUser receiver = wxUserMapper.selectById(receiverId);
        if (receiver == null) {
            throw new BusinessException(UserRespType.PRIVATE_MESSAGE_RECEIVER_NOT_FOUND);
        }

        // 获取或创建会话
        Long user1Id = Math.min(senderId, receiverId);
        Long user2Id = Math.max(senderId, receiverId);

        PrivateConversation conversation = conversationMapper.findByUsers(user1Id, user2Id);
        if (conversation == null) {
            conversation = new PrivateConversation();
            conversation.setUser1Id(user1Id);
            conversation.setUser2Id(user2Id);
            conversation.setLastMessage(dto.getContent());
            conversation.setLastTime(new java.util.Date());
            conversation.setUnread1(0);
            conversation.setUnread2(0);
            conversationMapper.insert(conversation);
        } else {
            // 更新会话最后消息
            conversation.setLastMessage(dto.getContent());
            conversation.setLastTime(new java.util.Date());
            // 增加接收者未读数
            if (senderId.equals(user1Id)) {
                conversation.setUnread2(conversation.getUnread2() + 1);
            } else {
                conversation.setUnread1(conversation.getUnread1() + 1);
            }
            conversationMapper.updateById(conversation);
        }

        // 创建消息
        PrivateMessage message = new PrivateMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(dto.getContent());
        message.setMsgType(PrivateMessageType.normalize(dto.getMsgType()));
        message.setIsRead(0);
        messageMapper.insert(message);

        log.debug("发送私信成功，senderId: {}, receiverId: {}, messageId: {}", senderId, receiverId, message.getId());
        return message.getId();
    }

    @Override
    public PageVO<PrivateConversationVO> getConversations(Long userId, int page, int pageSize) {
        IPage<PrivateConversation> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<PrivateConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w.eq(PrivateConversation::getUser1Id, userId)
                        .or().eq(PrivateConversation::getUser2Id, userId))
                .eq(PrivateConversation::getIsDeleted, 0)
                .orderByDesc(PrivateConversation::getLastTime);

        IPage<PrivateConversation> result = conversationMapper.selectPage(pageObj, queryWrapper);
        List<PrivateConversation> conversations = result.getRecords();

        if (conversations.isEmpty()) {
            return PageVO.of(Collections.emptyList(), 0L, page, pageSize);
        }

        // 获取所有对话用户ID
        Set<Long> targetUserIds = conversations.stream()
                .map(c -> c.getUser1Id().equals(userId) ? c.getUser2Id() : c.getUser1Id())
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, WxUser> userMap = wxUserMapper.selectBatchIds(targetUserIds).stream()
                .collect(Collectors.toMap(WxUser::getId, u -> u, (a, b) -> a));

        List<PrivateConversationVO> voList = conversations.stream()
                .map(c -> convertToConversationVO(c, userId, userMap))
                .collect(Collectors.toList());

        return PageVO.of(voList, result.getTotal(), page, pageSize);
    }

    @Override
    public PageVO<PrivateMessageVO> getMessages(Long userId, Long conversationId, int page, int pageSize) {
        // 验证会话属于当前用户
        PrivateConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(UserRespType.PRIVATE_CONVERSATION_NOT_FOUND);
        }
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new BusinessException(UserRespType.PRIVATE_CONVERSATION_ACCESS_DENIED);
        }

        IPage<PrivateMessage> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<PrivateMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrivateMessage::getConversationId, conversationId)
                .eq(PrivateMessage::getIsDeleted, 0)
                .orderByDesc(PrivateMessage::getCreateTime);

        IPage<PrivateMessage> result = messageMapper.selectPage(pageObj, queryWrapper);
        List<PrivateMessage> messages = result.getRecords();

        if (messages.isEmpty()) {
            return PageVO.of(Collections.emptyList(), 0L, page, pageSize);
        }

        // 获取发送者信息
        Set<Long> senderIds = messages.stream()
                .map(PrivateMessage::getSenderId)
                .collect(Collectors.toSet());
        Map<Long, WxUser> userMap = wxUserMapper.selectBatchIds(senderIds).stream()
                .collect(Collectors.toMap(WxUser::getId, u -> u, (a, b) -> a));

        // 反转消息列表（最新的在后面）
        List<PrivateMessageVO> voList = messages.stream()
                .map(m -> convertToMessageVO(m, userId, userMap))
                .collect(Collectors.toList());
        Collections.reverse(voList);

        return PageVO.of(voList, result.getTotal(), page, pageSize);
    }

    @Override
    public PageVO<PrivateMessageVO> getMessagesWithUser(Long userId, Long targetId, int page, int pageSize) {
        // 获取或创建会话
        Long user1Id = Math.min(userId, targetId);
        Long user2Id = Math.max(userId, targetId);

        PrivateConversation conversation = conversationMapper.findByUsers(user1Id, user2Id);
        if (conversation == null) {
            return PageVO.of(Collections.emptyList(), 0L, page, pageSize);
        }

        return getMessages(userId, conversation.getId(), page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long conversationId) {
        PrivateConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return;
        }

        // 清除当前用户的未读数
        if (conversation.getUser1Id().equals(userId)) {
            conversationMapper.clearUnread1(conversationId);
        } else if (conversation.getUser2Id().equals(userId)) {
            conversationMapper.clearUnread2(conversationId);
        }

        // 标记消息为已读
        messageMapper.markAsReadByConversation(conversationId, userId);

        log.debug("标记会话已读，userId: {}, conversationId: {}", userId, conversationId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return messageMapper.countUnreadByReceiver(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long userId, Long conversationId) {
        PrivateConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(UserRespType.PRIVATE_CONVERSATION_NOT_FOUND);
        }
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new BusinessException(UserRespType.PRIVATE_CONVERSATION_DELETE_DENIED);
        }

        conversation.setIsDeleted(1);
        conversationMapper.updateById(conversation);

        log.debug("删除会话，userId: {}, conversationId: {}", userId, conversationId);
    }

    // ========================= 私有辅助方法 =========================

    private PrivateConversationVO convertToConversationVO(PrivateConversation conversation,
                                                           Long userId,
                                                           Map<Long, WxUser> userMap) {
        // 确定对话目标用户
        Long targetUserId = conversation.getUser1Id().equals(userId)
                ? conversation.getUser2Id() : conversation.getUser1Id();
        WxUser targetUser = userMap.get(targetUserId);

        // 获取当前用户的未读数
        int unreadCount = conversation.getUser1Id().equals(userId)
                ? conversation.getUnread1() : conversation.getUnread2();

        String targetNickname = targetUser != null && StringUtils.hasText(targetUser.getNickname())
                ? targetUser.getNickname() : "匿名用户";
        String targetAvatarUrl = targetUser != null && StringUtils.hasText(targetUser.getAvatarUrl())
                ? targetUser.getAvatarUrl() : "";

        String lastTimeStr = DateUtils.format(conversation.getLastTime());

        return PrivateConversationVO.builder()
                .id(conversation.getId())
                .targetUserId(targetUserId)
                .targetNickname(targetNickname)
                .targetAvatarUrl(targetAvatarUrl)
                .lastMessage(conversation.getLastMessage())
                .lastTime(lastTimeStr)
                .unreadCount(unreadCount)
                .build();
    }

    private PrivateMessageVO convertToMessageVO(PrivateMessage message,
                                                 Long currentUserId,
                                                 Map<Long, WxUser> userMap) {
        WxUser sender = userMap.get(message.getSenderId());
        String senderNickname = sender != null && StringUtils.hasText(sender.getNickname())
                ? sender.getNickname() : "匿名用户";
        String senderAvatarUrl = sender != null && StringUtils.hasText(sender.getAvatarUrl())
                ? sender.getAvatarUrl() : "";

        String createTimeStr = DateUtils.format(message.getCreateTime());

        return PrivateMessageVO.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .senderNickname(senderNickname)
                .senderAvatarUrl(senderAvatarUrl)
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .msgType(message.getMsgType())
                .isRead(message.getIsRead() == 1)
                .isSelf(message.getSenderId().equals(currentUserId))
                .createTime(createTimeStr)
                .build();
    }
}
