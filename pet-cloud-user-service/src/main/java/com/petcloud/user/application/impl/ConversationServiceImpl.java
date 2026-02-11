package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.AiChatMessage;
import com.petcloud.user.domain.entity.Conversation;
import com.petcloud.user.domain.entity.Message;
import com.petcloud.user.domain.service.ChatService;
import com.petcloud.user.domain.service.ConversationService;
import com.petcloud.user.domain.vo.AiChatMessageVO;
import com.petcloud.user.domain.vo.ConversationVO;
import com.petcloud.user.domain.vo.MessageCenterVO;
import com.petcloud.user.domain.vo.MessageVO;
import com.petcloud.user.infrastructure.persistence.mapper.AiChatMessageMapper;
import com.petcloud.user.infrastructure.persistence.mapper.ConversationMapper;
import com.petcloud.user.infrastructure.persistence.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final MessageMapper messageMapper;
    private final ChatService chatService;

    // 图标映射
    private static final Map<String, String> ICON_MAP = new HashMap<>();
    // 颜色映射
    private static final Map<String, String> COLOR_MAP = new HashMap<>();

    static {
        ICON_MAP.put("system", "🔔");
        ICON_MAP.put("order", "📦");
        ICON_MAP.put("activity", "🎁");
        ICON_MAP.put("interaction", "💬");
        ICON_MAP.put("ai_chat", "🤖");
        ICON_MAP.put("doctor_consultation", "👨‍⚕️");
        ICON_MAP.put("customer_service", "👨‍💼");

        COLOR_MAP.put("system", "purple");
        COLOR_MAP.put("order", "green");
        COLOR_MAP.put("activity", "orange");
        COLOR_MAP.put("interaction", "blue");
    }

    @Override
    public MessageCenterVO getMessageCenter(Long userId) {
        // 获取会话列表
        List<ConversationVO> conversations = getConversationList(userId);

        // 计算各类型未读数
        int aiUnreadCount = 0;
        int consultationUnreadCount = 0;
        for (ConversationVO conv : conversations) {
            if ("ai_chat".equals(conv.getType())) {
                aiUnreadCount += conv.getUnreadCount() != null ? conv.getUnreadCount() : 0;
            } else if ("doctor_consultation".equals(conv.getType())) {
                consultationUnreadCount += conv.getUnreadCount() != null ? conv.getUnreadCount() : 0;
            }
        }

        // 获取系统通知统计
        MessageCenterVO.NotificationCounts notificationCounts = getNotificationCounts(userId);

        // 获取最近系统通知（最多5条）
        List<MessageVO> recentNotifications = getRecentNotifications(userId, 5);

        // 计算总未读数
        int totalUnreadCount = aiUnreadCount + consultationUnreadCount + notificationCounts.getTotalCount();

        return MessageCenterVO.builder()
                .totalUnreadCount(totalUnreadCount)
                .recentConversations(conversations)
                .aiUnreadCount(aiUnreadCount)
                .consultationUnreadCount(consultationUnreadCount)
                .notificationCounts(notificationCounts)
                .recentNotifications(recentNotifications)
                .build();
    }

    @Override
    public List<ConversationVO> getConversationList(Long userId) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getStatus, Conversation.Status.ACTIVE.getCode())
                .orderByDesc(Conversation::getIsPinned)
                .orderByDesc(Conversation::getLastMessageTime);

        List<Conversation> conversations = conversationMapper.selectList(queryWrapper);

        return conversations.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConversationVO getOrCreateAiConversation(Long userId) {
        // 查找是否存在AI会话
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getType, Conversation.Type.AI_CHAT.getCode())
                .eq(Conversation::getStatus, Conversation.Status.ACTIVE.getCode());

        Conversation conversation = conversationMapper.selectOne(queryWrapper);

        if (conversation == null) {
            // 创建新会话
            conversation = new Conversation();
            conversation.setUserId(userId);
            conversation.setType(Conversation.Type.AI_CHAT.getCode());
            conversation.setTargetName("AI智能助手");
            // 不设置头像，前端使用占位符显示🤖
            conversation.setTargetAvatar("");
            conversation.setUnreadCount(0);
            conversation.setIsPinned(0);
            conversation.setStatus(Conversation.Status.ACTIVE.getCode());
            conversation.setLastMessageTime(new Date());

            conversationMapper.insert(conversation);
            log.info("创建AI会话成功, userId: {}, conversationId: {}", userId, conversation.getId());
        }

        return convertToVO(conversation);
    }

    @Override
    public ConversationVO getConversationDetail(Long userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONVERSATION_NOT_FOUND);
        }
        return convertToVO(conversation);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONVERSATION_NOT_FOUND);
        }

        // 更新会话未读数为0
        LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Conversation::getId, conversationId)
                .set(Conversation::getUnreadCount, 0);
        conversationMapper.update(null, updateWrapper);

        log.info("标记会话已读, userId: {}, conversationId: {}", userId, conversationId);
    }

    @Override
    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONVERSATION_NOT_FOUND);
        }

        // 软删除会话
        LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Conversation::getId, conversationId)
                .set(Conversation::getStatus, Conversation.Status.DELETED.getCode());
        conversationMapper.update(null, updateWrapper);

        log.info("删除会话, userId: {}, conversationId: {}", userId, conversationId);
    }

    @Override
    @Transactional
    public void togglePin(Long userId, Long conversationId, Boolean pinned) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONVERSATION_NOT_FOUND);
        }

        LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Conversation::getId, conversationId)
                .set(Conversation::getIsPinned, pinned ? 1 : 0);
        conversationMapper.update(null, updateWrapper);

        log.info("会话置顶状态变更, userId: {}, conversationId: {}, pinned: {}", userId, conversationId, pinned);
    }

    @Override
    @Transactional
    public String sendAiMessage(Long userId, Long conversationId, String content, String modelType) {
        // 验证会话
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONVERSATION_NOT_FOUND);
        }

        if (!Conversation.Type.AI_CHAT.getCode().equals(conversation.getType())) {
            throw new BusinessException(RespType.CONSULTATION_STATUS_ERROR);
        }

        // 保存用户消息
        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setUserId(userId);
        userMessage.setConversationId(conversationId);
        userMessage.setRole(AiChatMessage.Role.USER.getCode());
        userMessage.setContent(content);
        aiChatMessageMapper.insert(userMessage);

        // 调用AI服务
        String aiResponse;
        if ("deepseek".equalsIgnoreCase(modelType)) {
            aiResponse = chatService.callWithMessageDeepSeekV3(content);
        } else {
            aiResponse = chatService.callWithMessageQwenMax3(content);
        }

        // 保存AI回复
        AiChatMessage aiMessage = new AiChatMessage();
        aiMessage.setUserId(userId);
        aiMessage.setConversationId(conversationId);
        aiMessage.setRole(AiChatMessage.Role.ASSISTANT.getCode());
        aiMessage.setContent(aiResponse);
        aiMessage.setModelType(modelType);
        aiChatMessageMapper.insert(aiMessage);

        // 更新会话最后消息
        updateConversationLastMessage(conversationId, content);

        return aiResponse;
    }

    @Override
    public List<AiChatMessageVO> getAiChatHistory(Long userId, Long conversationId) {
        // 验证会话
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(RespType.CONVERSATION_NOT_FOUND);
        }

        LambdaQueryWrapper<AiChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiChatMessage::getConversationId, conversationId)
                .orderByAsc(AiChatMessage::getCreateTime);

        List<AiChatMessage> messages = aiChatMessageMapper.selectList(queryWrapper);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return messages.stream()
                .map(msg -> AiChatMessageVO.builder()
                        .id(msg.getId())
                        .conversationId(msg.getConversationId())
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .modelType(msg.getModelType())
                        .createTime(msg.getCreateTime() != null ? sdf.format(msg.getCreateTime()) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateConversationLastMessage(Long conversationId, String message) {
        // 截取消息内容，最多200字符
        String truncatedMessage = message != null && message.length() > 200
                ? message.substring(0, 200) + "..."
                : message;

        LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Conversation::getId, conversationId)
                .set(Conversation::getLastMessage, truncatedMessage)
                .set(Conversation::getLastMessageTime, new Date())
                .setSql("unread_count = unread_count + 1");

        conversationMapper.update(null, updateWrapper);
    }

    // ==================== 私有方法 ====================

    private ConversationVO convertToVO(Conversation conversation) {
        Conversation.Type type = Conversation.Type.fromCode(conversation.getType());

        // 确定跳转路径
        String navigateUrl;
        if (type == Conversation.Type.AI_CHAT) {
            navigateUrl = "/pages/chat/chat?conversationId=" + conversation.getId();
        } else if (type == Conversation.Type.DOCTOR_CONSULTATION) {
            navigateUrl = "/pages/consultation/chat?id=" + conversation.getTargetId();
        } else {
            navigateUrl = "/pages/chat/chat?conversationId=" + conversation.getId();
        }

        return ConversationVO.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .typeDesc(type != null ? type.getDesc() : "")
                .targetId(conversation.getTargetId())
                .targetName(conversation.getTargetName())
                .targetAvatar(conversation.getTargetAvatar())
                .lastMessage(conversation.getLastMessage())
                .lastMessageTime(conversation.getLastMessageTime())
                .lastMessageTimeStr(formatTime(conversation.getLastMessageTime()))
                .unreadCount(conversation.getUnreadCount())
                .isPinned(conversation.getIsPinned() != null && conversation.getIsPinned() == 1)
                .navigateUrl(navigateUrl)
                .build();
    }

    private MessageCenterVO.NotificationCounts getNotificationCounts(Long userId) {
        // 获取各类型消息未读数
        Map<String, Integer> countMap = new HashMap<>();

        for (Message.Type type : Message.Type.values()) {
            LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Message::getUserId, userId)
                    .eq(Message::getType, type.getCode())
                    .eq(Message::getIsRead, 0);
            Long count = messageMapper.selectCount(queryWrapper);
            countMap.put(type.getCode(), count.intValue());
        }

        int totalCount = countMap.values().stream().mapToInt(Integer::intValue).sum();

        return MessageCenterVO.NotificationCounts.builder()
                .orderCount(countMap.getOrDefault(Message.Type.ORDER.getCode(), 0))
                .activityCount(countMap.getOrDefault(Message.Type.ACTIVITY.getCode(), 0))
                .systemCount(countMap.getOrDefault(Message.Type.SYSTEM.getCode(), 0))
                .interactionCount(countMap.getOrDefault("interaction", 0))
                .totalCount(totalCount)
                .build();
    }

    private List<MessageVO> getRecentNotifications(Long userId, int limit) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreateTime)
                .last("LIMIT " + limit);

        List<Message> messages = messageMapper.selectList(queryWrapper);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return messages.stream()
                .map(msg -> MessageVO.builder()
                        .id(msg.getId())
                        .type(msg.getType())
                        .typeDesc(getTypeDesc(msg.getType()))
                        .title(msg.getTitle())
                        .content(msg.getContent())
                        .icon(ICON_MAP.getOrDefault(msg.getType(), "📄"))
                        .color(COLOR_MAP.getOrDefault(msg.getType(), "purple"))
                        .extraData(msg.getExtraData())
                        .isRead(msg.getIsRead())
                        .createTime(msg.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }

    private String getTypeDesc(String type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "system":
                return "系统通知";
            case "order":
                return "订单通知";
            case "activity":
                return "活动通知";
            case "interaction":
                return "互动消息";
            case "consult":
                return "咨询回复";
            default:
                return "";
        }
    }

    private String formatTime(Date date) {
        if (date == null) {
            return "";
        }

        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;

        long minute = 60000;
        long hour = 3600000;
        long day = 86400000;

        if (diff < minute) {
            return "刚刚";
        } else if (diff < hour) {
            return (diff / minute) + "分钟前";
        } else if (diff < day) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return "今天 " + sdf.format(date);
        } else if (diff < day * 2) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return "昨天 " + sdf.format(date);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
            return sdf.format(date);
        }
    }
}
