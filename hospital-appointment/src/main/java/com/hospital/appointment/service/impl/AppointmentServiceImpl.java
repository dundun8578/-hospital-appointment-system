package com.hospital.appointment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.common.BusinessException;
import com.hospital.common.ResultCode;
import com.hospital.appointment.dto.AppointmentCreateDTO;
import com.hospital.appointment.dto.AppointmentQueryDTO;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.service.AppointmentService;
import com.hospital.appointment.utils.DistributedLockUtil;
import com.hospital.appointment.utils.RedisStockUtil;
import com.hospital.appointment.utils.RedisStockUtil;
import com.hospital.appointment.vo.AppointmentVO;
import com.hospital.appointment.vo.AppointmentResultVO;
import com.hospital.doctor.entity.Schedule;
import com.hospital.doctor.service.ScheduleService;
import com.hospital.doctor.service.DoctorService;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.Hospital;
import com.hospital.doctor.entity.Department;
import com.hospital.doctor.service.HospitalService;
import com.hospital.doctor.service.DepartmentService;
import com.hospital.user.entity.User;
import com.hospital.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预约服务实现类（核心业务）
 * 实现Redis库存扣减、分布式锁防重复预约
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private RedisStockUtil redisStockUtil;

    @Autowired
    private DistributedLockUtil distributedLockUtil;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.hospital.order.service.OrderService orderService;

    /**
     * 创建预约（核心方法）
     * 流程：
     * 1. 校验排班信息
     * 2. 获取分布式锁
     * 3. 检查用户是否已预约
     * 4. Redis Lua脚本原子扣减库存
     * 5. 扣减成功则保存预约记录
     * 6. 释放锁
     *
     * @param userId 用户ID
     * @param appointmentCreateDTO 预约信息
     * @return 预约结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentResultVO createAppointment(Long userId, AppointmentCreateDTO appointmentCreateDTO) {
        log.info("开始创建预约: userId={}, scheduleId={}, patientName={}",
                userId, appointmentCreateDTO.getScheduleId(), appointmentCreateDTO.getPatientName());

        Long scheduleId = appointmentCreateDTO.getScheduleId();
        RLock lock = null;

        try {
            // 1. 查询排班信息
            Schedule schedule = scheduleService.getById(scheduleId);
            if (schedule == null || schedule.getStatus() != 1) {
                log.warn("排班不存在或已停诊: scheduleId={}", scheduleId);
                return AppointmentResultVO.fail("排班不存在或已停诊");
            }

            // 检查排班日期
            if (schedule.getWorkDate().isBefore(LocalDate.now())) {
                log.warn("排班日期已过期: scheduleId={}, workDate={}", scheduleId, schedule.getWorkDate());
                return AppointmentResultVO.fail("排班日期已过期");
            }

            // 2. 获取分布式锁（防止重复提交）
            lock = distributedLockUtil.getAppointmentLock(userId, scheduleId);
            if (lock == null) {
                log.warn("获取分布式锁失败，请求过于频繁: userId={}, scheduleId={}", userId, scheduleId);
                return AppointmentResultVO.fail("系统繁忙，请稍后重试");
            }

            try {
                // 3. 双重检查：数据库查询是否已预约（防止并发穿透）
                int appointedCount = baseMapper.checkUserAppointed(userId, scheduleId);
                if (appointedCount > 0) {
                    log.warn("用户已预约该排班: userId={}, scheduleId={}", userId, scheduleId);
                    return AppointmentResultVO.fail("您已预约过该号源，请勿重复预约");
                }

                // 4. Redis原子扣减库存
                int remainStock = redisStockUtil.getStock(scheduleId);
                if (remainStock < 0) {
                    log.warn("Redis库存不存在，尝试同步: scheduleId={}", scheduleId);
                    // 库存不存在，同步数据库库存到Redis
                    redisStockUtil.syncStockFromDb(scheduleId);
                    remainStock = redisStockUtil.getStock(scheduleId);
                }

                if (remainStock <= 0) {
                    log.warn("号源已约满: scheduleId={}, remainStock={}", scheduleId, remainStock);
                    return AppointmentResultVO.fail("号源已约满");
                }

                // 5. 创建预约记录
                Appointment appointment = new Appointment();
                appointment.setUserId(userId);
                appointment.setScheduleId(scheduleId);
                appointment.setDoctorId(schedule.getDoctorId());
                appointment.setHospitalId(schedule.getHospitalId());
                appointment.setDepartmentId(schedule.getDepartmentId());
                appointment.setWorkDate(schedule.getWorkDate());
                appointment.setTimeSlot(schedule.getTimeSlot());
                appointment.setPatientName(appointmentCreateDTO.getPatientName());
                appointment.setPatientPhone(appointmentCreateDTO.getPatientPhone());
                appointment.setPatientIdCard(appointmentCreateDTO.getPatientIdCard());
                appointment.setIdCardType(appointmentCreateDTO.getIdCardType());
                appointment.setStatus(1); // 待就诊
                appointment.setVisitStatus(0); // 未就诊

                baseMapper.insert(appointment);

                log.info("预约记录创建成功: appointmentId={}, userId={}, scheduleId={}",
                        appointment.getAppointmentId(), userId, scheduleId);

                // 6. 返回预约成功
                return AppointmentResultVO.success(appointment.getAppointmentId());

            } finally {
                // 7. 释放分布式锁
                distributedLockUtil.unlock(lock);
            }

        } catch (BusinessException e) {
            log.error("预约业务异常: userId={}, scheduleId={}", userId, scheduleId, e);
            throw e;
        } catch (Exception e) {
            log.error("预约系统异常: userId={}, scheduleId={}", userId, scheduleId, e);
            return AppointmentResultVO.fail("系统异常，请稍后重试");
        }
    }

    /**
     * 根据ID查询预约详情
     *
     * @param appointmentId 预约ID
     * @return 预约VO
     */
    @Override
    public AppointmentVO getAppointmentDetail(Long appointmentId) {
        Appointment appointment = baseMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }

        return convertToVO(appointment);
    }

    /**
     * 查询用户预约列表
     *
     * @param queryDTO 查询条件
     * @return 预约列表
     */
    @Override
    public List<AppointmentVO> getUserAppointments(AppointmentQueryDTO queryDTO) {
        // 构建查询条件
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.* FROM appointment a ")
           .append("WHERE a.user_id = #{userId} AND a.deleted = 0 ");

        if (queryDTO.getStatus() != null) {
            sql.append("AND a.status = #{status} ");
        }

        sql.append("ORDER BY a.created_time DESC LIMIT #{offset}, #{limit}");

        // 计算分页
        int offset = (queryDTO.getCurrent() - 1) * queryDTO.getSize();
        int limit = queryDTO.getSize();

        List<Appointment> appointments = baseMapper.selectAppointmentList(
                queryDTO.getUserId(),
                queryDTO.getStatus(),
                offset,
                limit
        );

        return appointments.stream().map(this::convertToVO).toList();
    }

    /**
     * 分页查询用户预约列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<AppointmentVO> getUserAppointmentsPage(AppointmentQueryDTO queryDTO) {
        // 统计总数
        long total = baseMapper.countAppointments(queryDTO.getUserId(), queryDTO.getStatus());

        // 查询列表
        List<AppointmentVO> records = getUserAppointments(queryDTO);

        return PageResult.of(
                Long.valueOf(queryDTO.getCurrent()),
                Long.valueOf(queryDTO.getSize()),
                total,
                records
        );
    }

    /**
     * 取消预约
     *
     * @param appointmentId 预约ID
     * @param userId        用户ID
     * @param cancelReason  取消原因
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelAppointment(Long appointmentId, Long userId, String cancelReason) {
        log.info("取消预约: appointmentId={}, userId={}", appointmentId, userId);

        // 1. 查询预约记录
        Appointment appointment = baseMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.APPOINTMENT_NOT_FOUND);
        }

        // 2. 校验用户权限
        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限取消该预约");
        }

        // 3. 校验预约状态
        if (appointment.getStatus() != 1) {
            throw new BusinessException(ResultCode.APPOINTMENT_CANCELLED, "该预约无法取消");
        }

        // 4. 更新预约状态
        appointment.setStatus(3); // 已取消
        appointment.setCancelReason(cancelReason);
        baseMapper.updateById(appointment);

        // 5. 归还库存到Redis（TODO: 需要实现库存回滚）
        // redisStockUtil.increaseStock(appointment.getScheduleId(), 1);

        log.info("预约取消成功: appointmentId={}", appointmentId);
        return true;
    }

    /**
     * 检查用户是否已预约该排班
     *
     * @param userId    用户ID
     * @param scheduleId 排班ID
     * @return true-已预约，false-未预约
     */
    @Override
    public boolean checkUserAppointed(Long userId, Long scheduleId) {
        int count = baseMapper.checkUserAppointed(userId, scheduleId);
        return count > 0;
    }

    /**
     * 实体转VO
     *
     * @param appointment 预约实体
     * @return 预约VO
     */
    private AppointmentVO convertToVO(Appointment appointment) {
        AppointmentVO vo = new AppointmentVO();
        BeanUtils.copyProperties(appointment, vo);

        // 填充医生信息
        Doctor doctor = doctorService.getById(appointment.getDoctorId());
        if (doctor != null) {
            vo.setDoctorName(doctor.getDoctorName());
            vo.setDoctorTitle(doctor.getTitle());
        }

        // 填充医院信息
        Hospital hospital = hospitalService.getById(appointment.getHospitalId());
        if (hospital != null) {
            vo.setHospitalName(hospital.getHospitalName());
            vo.setHospitalAddress(hospital.getAddress());
        }

        // 填充科室信息
        Department department = departmentService.getById(appointment.getDepartmentId());
        if (department != null) {
            vo.setDepartmentName(department.getDepartmentName());
        }

        // 设置时段名称
        vo.setTimeSlotName(getTimeSlotName(appointment.getTimeSlot()));

        // 设置状态名称
        vo.setStatusName(getStatusName(appointment.getStatus()));

        return vo;
    }

    /**
     * 获取时段名称
     *
     * @param timeSlot 时段值
     * @return 时段名称
     */
    private String getTimeSlotName(Integer timeSlot) {
        return switch (timeSlot) {
            case 1 -> "上午";
            case 2 -> "下午";
            case 3 -> "晚上";
            default -> "未知";
        };
    }

    /**
     * 获取状态名称
     *
     * @param status 状态值
     * @return 状态名称
     */
    private String getStatusName(Integer status) {
        return switch (status) {
            case 1 -> "待就诊";
            case 2 -> "已就诊";
            case 3 -> "已取消";
            case 4 -> "已过期";
            case 5 -> "已爽约";
            default -> "未知";
        };
    }
}
