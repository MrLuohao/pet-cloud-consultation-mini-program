package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社区动态Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
}
