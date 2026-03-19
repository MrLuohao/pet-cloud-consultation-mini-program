package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.OrderStatusHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderStatusHistoryMapper extends BaseMapper<OrderStatusHistory> {
}
