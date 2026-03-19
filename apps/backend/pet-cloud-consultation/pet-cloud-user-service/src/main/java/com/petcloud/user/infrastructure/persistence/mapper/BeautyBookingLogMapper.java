package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.BeautyBookingLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美容预约日志Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface BeautyBookingLogMapper extends BaseMapper<BeautyBookingLog> {
}
