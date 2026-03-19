package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.DoctorVO;

import java.util.List;

/**
 * 医生服务接口
 *
 * @author luohao
 */
public interface DoctorService {

    /**
     * 获取医生列表
     *
     * @param department 科室（可选）
     * @return 医生列表
     */
    List<DoctorVO> getDoctorList(String department);

    /**
     * 获取医生详情
     *
     * @param doctorId 医生ID
     * @return 医生详情
     */
    DoctorVO getDoctorDetail(Long doctorId);

    /**
     * 获取科室列表
     *
     * @return 科室列表
     */
    List<String> getDepartments();
}
