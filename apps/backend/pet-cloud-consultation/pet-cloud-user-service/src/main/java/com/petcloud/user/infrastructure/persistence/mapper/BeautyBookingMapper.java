package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.BeautyBooking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美容预约Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface BeautyBookingMapper extends BaseMapper<BeautyBooking> {
}
