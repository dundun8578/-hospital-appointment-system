package com.hospital.doctor.controller;

import com.hospital.common.Result;
import com.hospital.doctor.dto.ScheduleCreateDTO;
import com.hospital.doctor.dto.ScheduleQueryDTO;
import com.hospital.doctor.service.HospitalService;
import com.hospital.doctor.vo.HospitalVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 医院控制器
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/hospital")
@Tag(name = "医院管理", description = "医院查询接口")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     * 查询医院列表
     *
     * @param province 省份
     * @param city     城市
     * @param level    医院等级
     * @return 医院列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询医院列表", description = "支持按地区、等级筛选")
    public Result<List<HospitalVO>> getHospitalList(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String level) {
        log.info("查询医院列表: province={}, city={}, level={}", province, city, level);
        List<HospitalVO> list = hospitalService.getHospitalList(province, city, level);
        return Result.success(list);
    }

    /**
     * 根据ID查询医院详情
     *
     * @param hospitalId 医院ID
     * @return 医院详情
     */
    @GetMapping("/{hospitalId}")
    @Operation(summary = "查询医院详情", description = "包含科室数量、医生数量")
    public Result<HospitalVO> getHospitalDetail(@PathVariable Long hospitalId) {
        log.info("查询医院详情: hospitalId={}", hospitalId);
        HospitalVO hospital = hospitalService.getHospitalDetail(hospitalId);
        return Result.success(hospital);
    }
}
