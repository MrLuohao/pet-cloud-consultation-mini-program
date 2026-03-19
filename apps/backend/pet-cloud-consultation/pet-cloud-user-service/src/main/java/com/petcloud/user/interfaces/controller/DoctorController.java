package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.user.domain.service.DoctorService;
import com.petcloud.user.domain.vo.DoctorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * 获取医生列表
     */
    @GetMapping("/list")
    public Response<List<DoctorVO>> getDoctorList(@RequestParam(required = false) String department) {
        log.info("获取医生列表，department: {}", department);
        List<DoctorVO> doctors = doctorService.getDoctorList(department);
        return Response.succeed(doctors);
    }

    /**
     * 获取医生详情
     */
    @GetMapping("/{id}")
    public Response<DoctorVO> getDoctorDetail(@PathVariable Long id) {
        log.info("获取医生详情，doctorId: {}", id);
        DoctorVO doctor = doctorService.getDoctorDetail(id);
        return Response.succeed(doctor);
    }

    /**
     * 获取科室列表
     */
    @GetMapping("/departments")
    public Response<List<String>> getDepartments() {
        log.info("获取科室列表");
        List<String> departments = doctorService.getDepartments();
        return Response.succeed(departments);
    }
}
