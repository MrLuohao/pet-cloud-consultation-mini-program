package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.BeautyStore;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美容门店Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface BeautyStoreMapper extends BaseMapper<BeautyStore> {
}
