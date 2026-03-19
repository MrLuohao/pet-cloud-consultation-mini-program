package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.CommunityTopic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社区话题Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CommunityTopicMapper extends BaseMapper<CommunityTopic> {
}
