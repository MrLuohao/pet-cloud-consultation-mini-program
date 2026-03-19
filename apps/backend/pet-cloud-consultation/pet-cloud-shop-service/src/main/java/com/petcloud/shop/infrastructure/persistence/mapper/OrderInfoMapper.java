package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
}
