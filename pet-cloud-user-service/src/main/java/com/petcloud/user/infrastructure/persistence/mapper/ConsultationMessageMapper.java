package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.ConsultationMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 咨询聊天记录Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ConsultationMessageMapper extends BaseMapper<ConsultationMessage> {
}
