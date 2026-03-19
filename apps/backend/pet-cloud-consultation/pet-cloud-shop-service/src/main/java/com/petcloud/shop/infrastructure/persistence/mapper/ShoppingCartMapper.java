package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
