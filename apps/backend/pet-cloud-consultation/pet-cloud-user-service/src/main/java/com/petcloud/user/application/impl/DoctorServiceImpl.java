package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.Doctor;
import com.petcloud.user.domain.service.DoctorService;
import com.petcloud.user.domain.vo.DoctorVO;
import com.petcloud.user.infrastructure.persistence.mapper.DoctorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;

    @Override
    public List<DoctorVO> getDoctorList(String department) {
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Doctor::getStatus, 1);
        if (department != null && !department.isEmpty()) {
            queryWrapper.eq(Doctor::getDepartment, department);
        }
        queryWrapper.orderByAsc(Doctor::getSortOrder)
                .orderByDesc(Doctor::getConsultationCount);

        List<Doctor> doctors = doctorMapper.selectList(queryWrapper);
        return doctors.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorVO getDoctorDetail(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new BusinessException(RespType.DOCTOR_NOT_FOUND);
        }
        return convertToVO(doctor);
    }

    @Override
    public List<String> getDepartments() {
        return doctorMapper.selectList(
                new LambdaQueryWrapper<Doctor>()
                        .select(Doctor::getDepartment)
                        .eq(Doctor::getStatus, 1)
                        .groupBy(Doctor::getDepartment)
        ).stream()
                .map(Doctor::getDepartment)
                .filter(dept -> dept != null && !dept.isEmpty())
                .collect(Collectors.toList());
    }

    private DoctorVO convertToVO(Doctor doctor) {
        return DoctorVO.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .avatar(doctor.getAvatar())
                .title(doctor.getTitle())
                .specialty(doctor.getSpecialty())
                .department(doctor.getDepartment())
                .experience(doctor.getExperience())
                .description(doctor.getDescription())
                .hospitalName(doctor.getHospitalName())
                .consultationFee(doctor.getConsultationFee())
                .rating(doctor.getRating())
                .consultationCount(doctor.getConsultationCount())
                .tags(doctor.getTags() != null ? doctor.getTags().split(",") : new String[0])
                .build();
    }
}
