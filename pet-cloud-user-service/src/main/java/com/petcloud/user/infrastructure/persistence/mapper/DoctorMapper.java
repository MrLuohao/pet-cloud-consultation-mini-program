package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
}
