package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.CommunityPostShare;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子分享记录Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CommunityPostShareMapper extends BaseMapper<CommunityPostShare> {
}
