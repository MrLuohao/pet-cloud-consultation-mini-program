package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.PointsHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分流水Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface PointsHistoryMapper extends BaseMapper<PointsHistory> {
}
