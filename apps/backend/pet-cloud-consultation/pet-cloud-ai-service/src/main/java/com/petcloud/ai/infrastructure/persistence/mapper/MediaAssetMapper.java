package com.petcloud.ai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.ai.domain.entity.MediaAsset;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MediaAssetMapper extends BaseMapper<MediaAsset> {
}
