package com.petcloud.ai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.ai.domain.entity.FeaturedContentDraft;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeaturedContentDraftMapper extends BaseMapper<FeaturedContentDraft> {
}
