package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcloud.user.domain.entity.Message;
import com.petcloud.user.domain.service.MessageService;
import com.petcloud.user.domain.vo.MessageVO;
import com.petcloud.user.infrastructure.persistence.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public List<MessageVO> getMessageList(Long userId, String type) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Message::getType, type);
        }
        queryWrapper.orderByDesc(Message::getCreateTime);
        List<Message> messages = messageMapper.selectList(queryWrapper);
        return messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getId, messageId)
                .eq(Message::getUserId, userId)
                .set(Message::getIsRead, 1);
        messageMapper.update(null, updateWrapper);
        log.info("标记消息已读，messageId: {}", messageId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getUserId, userId)
                .set(Message::getIsRead, 1);
        messageMapper.update(null, updateWrapper);
        log.info("标记所有消息已读，userId: {}", userId);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0);
        return messageMapper.selectCount(queryWrapper);
    }

    private MessageVO convertToVO(Message message) {
        return MessageVO.builder()
                .id(message.getId())
                .type(message.getType())
                .title(message.getTitle())
                .content(message.getContent())
                .extraData(message.getExtraData())
                .isRead(message.getIsRead())
                .createTime(message.getCreateTime())
                .build();
    }
}
