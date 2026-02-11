package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.Consultation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 咨询记录Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ConsultationMapper extends BaseMapper<Consultation> {
}
