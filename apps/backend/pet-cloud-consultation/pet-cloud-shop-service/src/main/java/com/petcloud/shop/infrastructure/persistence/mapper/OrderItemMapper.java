package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单商品Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
