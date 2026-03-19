package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.ProductCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品收藏Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ProductCollectionMapper extends BaseMapper<ProductCollection> {
}
