package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {
}
