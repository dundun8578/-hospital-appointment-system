package com.hospital.doctor.controller;

import com.hospital.common.Result;
import com.hospital.doctor.service.DepartmentService;
import com.hospital.doctor.vo.DepartmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科室控制器
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/department")
@Tag(name = "科室管理", description = "科室查询接口")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 根据医院ID查询科室列表
     *
     * @param hospitalId 医院ID
     * @return 科室列表
     */
    @GetMapping("/list/{hospitalId}")
    @Operation(summary = "查询医院科室列表", description = "包含医生数量")
    public Result<List<DepartmentVO>> getDepartmentList(@PathVariable Long hospitalId) {
        log.info("查询科室列表: hospitalId={}", hospitalId);
        List<DepartmentVO> list = departmentService.getDepartmentList(hospitalId);
        return Result.success(list);
    }

    /**
     * 查询一级科室
     *
     * @param hospitalId 医院ID
     * @return 一级科室列表
     */
    @GetMapping("/top/{hospitalId}")
    @Operation(summary = "查询一级科室", description = "仅返回parentId=0的科室")
    public Result<List<DepartmentVO>> getTopLevelDepartments(@PathVariable Long hospitalId) {
        log.info("查询一级科室: hospitalId={}", hospitalId);
        List<DepartmentVO> list = departmentService.getTopLevelDepartments(hospitalId);
        return Result.success(list);
    }

    /**
     * 查询子科室
     *
     * @param parentId 父科室ID
     * @return 子科室列表
     */
    @GetMapping("/sub/{parentId}")
    @Operation(summary = "查询子科室", description = "根据父科室ID查询子科室")
    public Result<List<DepartmentVO>> getSubDepartments(@PathVariable Long parentId) {
        log.info("查询子科室: parentId={}", parentId);
        List<DepartmentVO> list = departmentService.getSubDepartments(parentId);
        return Result.success(list);
    }
}
