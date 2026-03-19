package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收货地址Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
