package com.petcloud.shop.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.shop.domain.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
