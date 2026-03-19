package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
}
