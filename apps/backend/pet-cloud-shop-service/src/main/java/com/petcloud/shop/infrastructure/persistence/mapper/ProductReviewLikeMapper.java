package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.ProductReviewLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价点赞Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ProductReviewLikeMapper extends BaseMapper<ProductReviewLike> {
}
