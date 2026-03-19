package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户优惠券Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
}
