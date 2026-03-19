package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收货地址Mapper接口（shop-service本地读取）
 *
 * @author luohao
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
