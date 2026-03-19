package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.utils.DateUtils;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final String EVENT_SLOT_DIAGNOSIS_COMPLETE = "diagnosis_complete";
    private static final String EVENT_SLOT_MODERATION_REJECT = "moderation_reject";
    private static final String EVENT_SLOT_FEATURED_CONTENT_PUSH = "featured_content_push";

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
        List<ConversationVO> conversations = getConversationList(userId);
        int aiUnreadCount = 0;
        int consultationUnreadCount = 0;
        int customerServiceUnreadCount = 0;
        for (ConversationVO conv : conversations) {
            if (Conversation.Type.AI_CHAT.getCode().equals(conv.getType())) {
                aiUnreadCount += conv.getUnreadCount() != null ? conv.getUnreadCount() : 0;
            } else if (Conversation.Type.DOCTOR_CONSULTATION.getCode().equals(conv.getType())) {
                consultationUnreadCount += conv.getUnreadCount() != null ? conv.getUnreadCount() : 0;
            } else if (Conversation.Type.CUSTOMER_SERVICE.getCode().equals(conv.getType())) {
                customerServiceUnreadCount += conv.getUnreadCount() != null ? conv.getUnreadCount() : 0;
            }
        }

        MessageCenterVO.NotificationCounts notificationCounts = getNotificationCounts(userId);
        List<MessageVO> recentNotifications = getRecentNotifications(userId, 5);
        int conversationUnreadCount = aiUnreadCount + consultationUnreadCount + customerServiceUnreadCount;
        int notificationUnreadCount = notificationCounts.getTotalCount() != null ? notificationCounts.getTotalCount() : 0;
        int totalUnreadCount = conversationUnreadCount + notificationUnreadCount;
        return MessageCenterVO.builder()
                .quickEntries(buildQuickEntries(aiUnreadCount, consultationUnreadCount, customerServiceUnreadCount))
                .unreadSummary(MessageCenterVO.UnreadSummaryVO.builder()
                        .totalUnreadCount(totalUnreadCount)
                        .conversationUnreadCount(conversationUnreadCount)
                        .notificationUnreadCount(notificationUnreadCount)
                        .aiUnreadCount(aiUnreadCount)
                        .consultationUnreadCount(consultationUnreadCount)
                        .customerServiceUnreadCount(customerServiceUnreadCount)
                        .build())
                .totalUnreadCount(totalUnreadCount)
                .recentConversations(conversations)
                .aiUnreadCount(aiUnreadCount)
                .consultationUnreadCount(consultationUnreadCount)
                .notificationCounts(notificationCounts)
                .recentNotifications(recentNotifications)
                .systemNotifications(recentNotifications)
                .eventSlots(buildEventSlots())
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

        return messages.stream()
                .map(msg -> AiChatMessageVO.builder()
                        .id(msg.getId())
                        .conversationId(msg.getConversationId())
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .modelType(msg.getModelType())
                        .createTime(DateUtils.format(msg.getCreateTime()))
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
                .lastMessageTimeStr(DateUtils.formatRelative(conversation.getLastMessageTime()))
                .unreadCount(conversation.getUnreadCount())
                .isPinned(Integer.valueOf(1).equals(conversation.getIsPinned()))
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
            // 防止 NPE：如果 selectCount 返回 null，默认为 0
            countMap.put(type.getCode(), count != null ? count.intValue() : 0);
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
        // 使用 MyBatis-Plus 分页，避免 SQL 拼接
        Page<Message> pageParam = new Page<>(1, limit);
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreateTime);

        List<Message> messages = messageMapper.selectPage(pageParam, queryWrapper).getRecords();

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
            case "interaction":
                return "互动消息";
            case "consult":
                return "咨询回复";
            default:
                return resolveMessageTypeDesc(type);
        }
    }

    private List<MessageCenterVO.QuickEntryVO> buildQuickEntries(int aiUnreadCount,
                                                                 int consultationUnreadCount,
                                                                 int customerServiceUnreadCount) {
        return List.of(
                MessageCenterVO.QuickEntryVO.builder()
                        .key("ai_assistant")
                        .title("AI助手")
                        .subtitle(aiUnreadCount > 0 ? aiUnreadCount + "条未读" : "随时继续诊断对话")
                        .iconKey("msg-ai")
                        .unreadCount(aiUnreadCount)
                        .navigateUrl("/pages/chat/chat")
                        .build(),
                MessageCenterVO.QuickEntryVO.builder()
                        .key("consultation")
                        .title("我的咨询")
                        .subtitle(consultationUnreadCount > 0 ? consultationUnreadCount + "条提醒" : "查看医生回复进度")
                        .iconKey("msg-consult")
                        .unreadCount(consultationUnreadCount)
                        .navigateUrl("/pages/consultation/list")
                        .build(),
                MessageCenterVO.QuickEntryVO.builder()
                        .key("customer_service")
                        .title("在线客服")
                        .subtitle(customerServiceUnreadCount > 0 ? customerServiceUnreadCount + "条未读" : "订单与服务问题")
                        .iconKey("msg-service")
                        .unreadCount(customerServiceUnreadCount)
                        .navigateUrl("/pages/consultation/doctor-list")
                        .build()
        );
    }

    private List<MessageCenterVO.EventSlotVO> buildEventSlots() {
        return List.of(
                MessageCenterVO.EventSlotVO.builder()
                        .key(EVENT_SLOT_DIAGNOSIS_COMPLETE)
                        .title("诊断结果完成")
                        .description("预留 AI 诊断完成提醒槽位")
                        .unreadCount(0)
                        .enabled(false)
                        .build(),
                MessageCenterVO.EventSlotVO.builder()
                        .key(EVENT_SLOT_MODERATION_REJECT)
                        .title("内容审核反馈")
                        .description("预留素材审核驳回提醒槽位")
                        .unreadCount(0)
                        .enabled(false)
                        .build(),
                MessageCenterVO.EventSlotVO.builder()
                        .key(EVENT_SLOT_FEATURED_CONTENT_PUSH)
                        .title("精选内容更新")
                        .description("预留精选内容推送槽位")
                        .unreadCount(0)
                        .enabled(false)
                        .build()
        );
    }

    private String resolveMessageTypeDesc(String type) {
        for (Message.Type messageType : Message.Type.values()) {
            if (messageType.getCode().equals(type)) {
                return messageType.getDesc();
            }
        }
        return "";
    }
}
