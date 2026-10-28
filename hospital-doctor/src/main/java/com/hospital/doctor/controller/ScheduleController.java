package com.hospital.doctor.controller;

import com.hospital.common.Result;
import com.hospital.common.annotation.AdminOnly;
import com.hospital.doctor.dto.ScheduleCreateDTO;
import com.hospital.doctor.dto.ScheduleQueryDTO;
import com.hospital.doctor.service.ScheduleService;
import com.hospital.doctor.vo.ScheduleDetailVO;
import com.hospital.doctor.vo.ScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排班控制器
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/schedule")
@Tag(name = "排班管理", description = "排班查询和号源管理")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 创建排班
     *
     * @param scheduleCreateDTO 排班信息
     * @return 排班ID
     */
    @PostMapping("/create")
    @AdminOnly
    @Operation(summary = "创建排班", description = "管理员创建排班并同步号源到Redis")
    public Result<Long> createSchedule(@Valid @RequestBody ScheduleCreateDTO scheduleCreateDTO) {
        log.info("创建排班: {}", scheduleCreateDTO);
        Long scheduleId = scheduleService.createSchedule(scheduleCreateDTO);
        return Result.success(scheduleId);
    }

    /**
     * 查询排班列表
     *
     * @param queryDTO 查询条件
     * @return 排班列表
     */
    @PostMapping("/list")
    @Operation(summary = "查询排班列表", description = "支持医院、科室、医生、日期筛选")
    public Result<List<ScheduleVO>> getScheduleList(@Valid @RequestBody ScheduleQueryDTO queryDTO) {
        log.info("查询排班列表: {}", queryDTO);
        List<ScheduleVO> list = scheduleService.getScheduleList(queryDTO);
        return Result.success(list);
    }

    /**
     * 根据ID查询排班详情
     *
     * @param scheduleId 排班ID
     * @return 排班详情
     */
    @GetMapping("/{scheduleId}")
    @Operation(summary = "查询排班详情", description = "包含医生、医院、科室完整信息")
    public Result<ScheduleDetailVO> getScheduleDetail(@PathVariable Long scheduleId) {
        log.info("查询排班详情: scheduleId={}", scheduleId);
        ScheduleDetailVO detail = scheduleService.getScheduleDetail(scheduleId);
        return Result.success(detail);
    }

    /**
     * 查询医生未来7天排班
     *
     * @param doctorId 医生ID
     * @return 排班列表
     */
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "查询医生排班", description = "查询医生未来7天排班")
    public Result<List<ScheduleVO>> getDoctorSchedule(@PathVariable Long doctorId) {
        log.info("查询医生排班: doctorId={}", doctorId);
        List<ScheduleVO> list = scheduleService.getDoctorScheduleNext7Days(doctorId);
        return Result.success(list);
    }

    /**
     * 手动同步号源到Redis
     *
     * @param scheduleId 排班ID
     * @return 成功标识
     */
    @PostMapping("/sync/{scheduleId}")
    @AdminOnly
    @Operation(summary = "同步号源到Redis", description = "手动同步单个排班库存到Redis")
    public Result<Void> syncStockToRedis(@PathVariable Long scheduleId) {
        log.info("手动同步号源到Redis: scheduleId={}", scheduleId);
        scheduleService.syncStockToRedis(scheduleId);
        return Result.success();
    }

    /**
     * 批量同步号源到Redis
     *
     * @return 成功标识
     */
    @PostMapping("/sync/batch")
    @AdminOnly
    @Operation(summary = "批量同步号源", description = "同步未来30天所有排班库存到Redis")
    public Result<Void> batchSyncStockToRedis() {
        log.info("手动批量同步号源到Redis");
        scheduleService.batchSyncStockToRedis();
        return Result.success();
    }
}
