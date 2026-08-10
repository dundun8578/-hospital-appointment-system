package com.hospital.doctor.controller;

import com.hospital.common.Result;
import com.hospital.doctor.service.DoctorService;
import com.hospital.doctor.vo.DoctorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生控制器
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/doctor")
@Tag(name = "医生管理", description = "医生查询接口")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    /**
     * 根据科室ID查询医生列表
     *
     * @param departmentId 科室ID
     * @return 医生列表
     */
    @GetMapping("/list/{departmentId}")
    @Operation(summary = "查询科室医生列表", description = "按从业年限降序排列")
    public Result<List<DoctorVO>> getDoctorListByDepartment(@PathVariable Long departmentId) {
        log.info("查询医生列表: departmentId={}", departmentId);
        List<DoctorVO> list = doctorService.getDoctorListByDepartment(departmentId);
        return Result.success(list);
    }

    /**
     * 根据医生ID查询医生详情
     *
     * @param doctorId 医生ID
     * @return 医生详情
     */
    @GetMapping("/{doctorId}")
    @Operation(summary = "查询医生详情", description = "包含最近可预约日期")
    public Result<DoctorVO> getDoctorDetail(@PathVariable Long doctorId) {
        log.info("查询医生详情: doctorId={}", doctorId);
        DoctorVO doctor = doctorService.getDoctorDetail(doctorId);
        return Result.success(doctor);
    }

    /**
     * 根据医院ID查询医生列表
     *
     * @param hospitalId 医院ID
     * @return 医生列表
     */
    @GetMapping("/list/hospital/{hospitalId}")
    @Operation(summary = "查询医院医生列表", description = "按医院查询医生")
    public Result<List<DoctorVO>> getDoctorListByHospital(@PathVariable Long hospitalId) {
        log.info("查询医院医生列表: hospitalId={}", hospitalId);
        List<DoctorVO> list = doctorService.getDoctorListByHospital(hospitalId);
        return Result.success(list);
    }

    /**
     * 搜索医生
     *
     * @param keyword 关键词（姓名、擅长领域）
     * @return 医生列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索医生", description = "按姓名或擅长领域搜索")
    public Result<List<DoctorVO>> searchDoctors(@RequestParam String keyword) {
        log.info("搜索医生: keyword={}", keyword);
        List<DoctorVO> list = doctorService.searchDoctors(keyword);
        return Result.success(list);
    }
}
