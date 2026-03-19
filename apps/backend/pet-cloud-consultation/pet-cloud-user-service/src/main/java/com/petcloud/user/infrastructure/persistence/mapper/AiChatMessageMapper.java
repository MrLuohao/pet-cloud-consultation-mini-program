package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI聊天记录Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
}
