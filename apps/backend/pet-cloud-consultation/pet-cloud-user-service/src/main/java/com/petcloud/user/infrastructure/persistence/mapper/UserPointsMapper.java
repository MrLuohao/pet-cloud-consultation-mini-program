package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {
}
