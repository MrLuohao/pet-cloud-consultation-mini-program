package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcloud.user.domain.entity.Conversation;
import com.petcloud.user.domain.entity.Message;
import com.petcloud.user.domain.service.ChatService;
import com.petcloud.user.domain.vo.MessageCenterVO;
import com.petcloud.user.infrastructure.persistence.mapper.AiChatMessageMapper;
import com.petcloud.user.infrastructure.persistence.mapper.ConversationMapper;
import com.petcloud.user.infrastructure.persistence.mapper.MessageMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConversationServiceImplTest {

    @Test
    void shouldBuildUnifiedMessageCenterPayload() {
        Conversation aiConversation = buildConversation(11L, Conversation.Type.AI_CHAT.getCode(),
                "AI智能助手", 3, 1, "诊断结果已更新");
        Conversation doctorConversation = buildConversation(12L, Conversation.Type.DOCTOR_CONSULTATION.getCode(),
                "王医生", 2, 0, "建议补充一张患处图片");
        Conversation customerServiceConversation = buildConversation(13L, Conversation.Type.CUSTOMER_SERVICE.getCode(),
                "在线客服", 1, 0, "订单问题已受理");

        ConversationMapper conversationMapper = proxy(ConversationMapper.class, (method, args) -> {
            if ("selectList".equals(method.getName())) {
                return List.of(aiConversation, doctorConversation, customerServiceConversation);
            }
            throw new UnsupportedOperationException(method.getName());
        });

        AtomicInteger countCallIndex = new AtomicInteger();
        MessageMapper messageMapper = proxy(MessageMapper.class, (method, args) -> {
            if ("selectCount".equals(method.getName())) {
                return switch (countCallIndex.getAndIncrement()) {
                    case 0 -> 2L;
                    case 1 -> 1L;
                    case 2 -> 4L;
                    default -> 0L;
                };
            }
            if ("selectPage".equals(method.getName())) {
                Page<Message> page = (Page<Message>) args[0];
                page.setRecords(List.of(
                        buildMessage(101L, Message.Type.SYSTEM.getCode(), "诊断完成提醒", 0),
                        buildMessage(102L, Message.Type.ORDER.getCode(), "商城发货提醒", 0)
                ));
                return page;
            }
            throw new UnsupportedOperationException(method.getName());
        });

        ConversationServiceImpl conversationService = new ConversationServiceImpl(
                conversationMapper,
                proxy(AiChatMessageMapper.class, (method, args) -> {
                    throw new UnsupportedOperationException(method.getName());
                }),
                messageMapper,
                proxy(ChatService.class, (method, args) -> {
                    throw new UnsupportedOperationException(method.getName());
                })
        );

        MessageCenterVO center = conversationService.getMessageCenter(1L);

        assertNotNull(center.getUnreadSummary());
        assertEquals(13, center.getUnreadSummary().getTotalUnreadCount());
        assertEquals(6, center.getUnreadSummary().getConversationUnreadCount());
        assertEquals(7, center.getUnreadSummary().getNotificationUnreadCount());

        assertEquals(3, center.getQuickEntries().size());
        MessageCenterVO.QuickEntryVO aiEntry = center.getQuickEntries().get(0);
        assertEquals("ai_assistant", aiEntry.getKey());
        assertEquals(3, aiEntry.getUnreadCount());

        MessageCenterVO.QuickEntryVO consultationEntry = center.getQuickEntries().get(1);
        assertEquals("consultation", consultationEntry.getKey());
        assertEquals(2, consultationEntry.getUnreadCount());

        MessageCenterVO.QuickEntryVO serviceEntry = center.getQuickEntries().get(2);
        assertEquals("customer_service", serviceEntry.getKey());
        assertEquals(1, serviceEntry.getUnreadCount());

        assertEquals(2, center.getSystemNotifications().size());
        assertEquals(3, center.getEventSlots().size());
        assertEquals("diagnosis_complete", center.getEventSlots().get(0).getKey());
        assertEquals(0, center.getEventSlots().get(0).getUnreadCount());
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> invocation.invoke(method, args)
        );
    }

    private Conversation buildConversation(Long id, String type, String name, int unreadCount, int isPinned, String lastMessage) {
        Conversation conversation = new Conversation();
        conversation.setId(id);
        conversation.setUserId(1L);
        conversation.setType(type);
        conversation.setTargetName(name);
        conversation.setTargetAvatar("");
        conversation.setUnreadCount(unreadCount);
        conversation.setIsPinned(isPinned);
        conversation.setStatus(Conversation.Status.ACTIVE.getCode());
        conversation.setLastMessage(lastMessage);
        conversation.setLastMessageTime(new Date());
        return conversation;
    }

    private Message buildMessage(Long id, String type, String title, int isRead) {
        Message message = new Message();
        message.setId(id);
        message.setUserId(1L);
        message.setType(type);
        message.setTitle(title);
        message.setContent(title + "内容");
        message.setIsRead(isRead);
        message.setCreateTime(new Date());
        return message;
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(java.lang.reflect.Method method, Object[] args) throws Throwable;
    }
}
