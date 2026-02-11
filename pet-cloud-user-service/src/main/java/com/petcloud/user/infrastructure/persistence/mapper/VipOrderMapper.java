package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.VipOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员订单Mapper
 */
@Mapper
public interface VipOrderMapper extends BaseMapper<VipOrder> {
}
