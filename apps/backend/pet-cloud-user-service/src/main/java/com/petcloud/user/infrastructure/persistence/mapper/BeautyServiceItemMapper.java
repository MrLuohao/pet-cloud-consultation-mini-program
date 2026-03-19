package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.BeautyServiceItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美容服务项目Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface BeautyServiceItemMapper extends BaseMapper<BeautyServiceItem> {
}
