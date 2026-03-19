package com.petcloud.media.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.media.domain.entity.MediaAsset;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MediaAssetMapper extends BaseMapper<MediaAsset> {
}
