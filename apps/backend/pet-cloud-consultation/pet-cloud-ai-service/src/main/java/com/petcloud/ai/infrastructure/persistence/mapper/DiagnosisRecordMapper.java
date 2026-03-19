package com.petcloud.ai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.ai.domain.entity.DiagnosisRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DiagnosisRecordMapper extends BaseMapper<DiagnosisRecord> {
}
