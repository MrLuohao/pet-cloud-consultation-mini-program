package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
