package com.hospital.appointment.controller;

import com.hospital.common.Result;
import com.hospital.common.annotation.RateLimit;
import com.hospital.appointment.dto.AppointmentCreateDTO;
import com.hospital.appointment.dto.AppointmentQueryDTO;
import com.hospital.appointment.service.AppointmentService;
import com.hospital.appointment.service.AppointmentMQService;
import com.hospital.appointment.vo.AppointmentResultVO;
import com.hospital.appointment.vo.AppointmentVO;
import com.hospital.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 预约控制器
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/appointment")
@Tag(name = "预约管理", description = "预约挂号核心接口")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentMQService appointmentMQService;

    /**
     * 创建预约（核心接口）
     * 包含分布式锁+Redis库存扣减+MQ异步订单
     * 限流：每分钟最多5次
     *
     * @param userId 用户ID（从Token解析）
     * @param appointmentCreateDTO 预约信息
     * @return 预约结果
     */
    @RateLimit(keyPrefix = "appointment:create", timeWindow = 60, maxRequests = 5, message = "预约过于频繁，请稍后再试")
    @PostMapping("/create")
    @Operation(summary = "创建预约", description = "预约挂号，使用分布式锁+Redis原子扣减库存+MQ异步订单")
    public Result<AppointmentResultVO> createAppointment(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody AppointmentCreateDTO appointmentCreateDTO) {
        log.info("用户创建预约: userId={}, scheduleId={}",
                userId, appointmentCreateDTO.getScheduleId());

        // 使用MQ模式创建预约
        AppointmentResultVO result = appointmentMQService.createAppointmentWithMQ(userId, appointmentCreateDTO);

        if (result.getSuccess()) {
            log.info("预约成功: appointmentId={}", result.getAppointmentId());
            return Result.success(result);
        } else {
            log.warn("预约失败: {}", result.getMessage());
            return Result.error(400, result.getMessage());
        }
    }

    /**
     * 查询预约详情
     *
     * @param appointmentId 预约ID
     * @return 预约详情
     */
    @GetMapping("/{appointmentId}")
    @Operation(summary = "查询预约详情", description = "包含医生、医院、科室信息")
    public Result<AppointmentVO> getAppointmentDetail(@PathVariable Long appointmentId) {
        log.info("查询预约详情: appointmentId={}", appointmentId);
        AppointmentVO appointment = appointmentService.getAppointmentDetail(appointmentId);
        return Result.success(appointment);
    }

    /**
     * 查询用户预约列表
     *
     * @param userId 用户ID
     * @param status 预约状态
     * @param current 页码
     * @param size 每页大小
     * @return 预约列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询用户预约列表", description = "支持按状态筛选")
    public Result<List<AppointmentVO>> getUserAppointments(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        AppointmentQueryDTO queryDTO = new AppointmentQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setStatus(status);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);

        List<AppointmentVO> list = appointmentService.getUserAppointments(queryDTO);
        return Result.success(list);
    }

    /**
     * 分页查询用户预约列表
     *
     * @param userId 用户ID
     * @param status 预约状态
     * @param current 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询用户预约列表", description = "支持按状态筛选")
    public Result<PageResult<AppointmentVO>> getUserAppointmentsPage(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        AppointmentQueryDTO queryDTO = new AppointmentQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setStatus(status);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);

        PageResult<AppointmentVO> pageResult = appointmentService.getUserAppointmentsPage(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 取消预约
     *
     * @param appointmentId 预约ID
     * @param userId 用户ID
     * @param cancelReason 取消原因
     * @return 是否成功
     */
    @PostMapping("/{appointmentId}/cancel")
    @Operation(summary = "取消预约", description = "取消已预约的号源")
    public Result<Boolean> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String cancelReason) {

        log.info("取消预约: appointmentId={}, userId={}", appointmentId, userId);
        boolean success = appointmentService.cancelAppointment(appointmentId, userId, cancelReason);

        if (success) {
            return Result.success(true);
        } else {
            return Result.error(500, "取消预约失败");
        }
    }

    /**
     * 检查是否已预约
     *
     * @param scheduleId 排班ID
     * @return true-已预约，false-未预约
     */
    @GetMapping("/check/{scheduleId}")
    @Operation(summary = "检查是否已预约", description = "检查当前用户是否已预约该排班")
    public Result<Boolean> checkAppointed(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long scheduleId) {

        boolean appointed = appointmentService.checkUserAppointed(userId, scheduleId);
        return Result.success(appointed);
    }
}
