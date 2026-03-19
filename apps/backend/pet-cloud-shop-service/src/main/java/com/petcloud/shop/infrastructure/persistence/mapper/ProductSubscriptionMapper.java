package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.ProductSubscription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品订阅 Mapper
 *
 * @author luohao
 */
@Mapper
public interface ProductSubscriptionMapper extends BaseMapper<ProductSubscription> {
}
