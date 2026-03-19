package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
