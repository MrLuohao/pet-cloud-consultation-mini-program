package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.Promotion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 满减活动Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface PromotionMapper extends BaseMapper<Promotion> {
}
