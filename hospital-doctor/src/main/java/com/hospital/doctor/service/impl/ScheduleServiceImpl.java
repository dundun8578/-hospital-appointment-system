package com.hospital.doctor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.common.BusinessException;
import com.hospital.common.ResultCode;
import com.hospital.doctor.config.RedisKeyConstants;
import com.hospital.doctor.dto.ScheduleCreateDTO;
import com.hospital.doctor.dto.ScheduleQueryDTO;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.Schedule;
import com.hospital.doctor.mapper.ScheduleMapper;
import com.hospital.doctor.service.DoctorService;
import com.hospital.doctor.service.ScheduleService;
import com.hospital.doctor.vo.ScheduleDetailVO;
import com.hospital.doctor.vo.ScheduleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 排班服务实现类
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private com.hospital.doctor.service.HospitalService hospitalService;

    @Autowired
    private com.hospital.doctor.service.DepartmentService departmentService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * Redis Key前缀
     */
    private static final String REDIS_KEY_STOCK = "hospital:stock:%s";

    /**
     * 缓存过期时间：24小时
     */
    private static final long CACHE_EXPIRE_HOURS = 24;

    /**
     * 创建排班
     *
     * @param scheduleCreateDTO 排班信息
     * @return 排班ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSchedule(ScheduleCreateDTO scheduleCreateDTO) {
        log.info("创建排班: doctorId={}, workDate={}, timeSlot={}",
                scheduleCreateDTO.getDoctorId(), scheduleCreateDTO.getWorkDate(), scheduleCreateDTO.getTimeSlot());

        // 1. 查询医生信息
        Doctor doctor = doctorService.getById(scheduleCreateDTO.getDoctorId());
        if (doctor == null) {
            throw new BusinessException(ResultCode.DOCTOR_NOT_FOUND);
        }

        // 2. 检查排班是否已存在
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, scheduleCreateDTO.getDoctorId())
               .eq(Schedule::getWorkDate, scheduleCreateDTO.getWorkDate())
               .eq(Schedule::getTimeSlot, scheduleCreateDTO.getTimeSlot());
        Schedule existSchedule = baseMapper.selectOne(wrapper);

        if (existSchedule != null) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "该医生该时段已创建排班");
        }

        // 3. 创建排班
        Schedule schedule = new Schedule();
        schedule.setDoctorId(scheduleCreateDTO.getDoctorId());
        schedule.setHospitalId(doctor.getHospitalId());
        schedule.setDepartmentId(doctor.getDepartmentId());
        schedule.setWorkDate(scheduleCreateDTO.getWorkDate());
        schedule.setTimeSlot(scheduleCreateDTO.getTimeSlot());
        schedule.setTotalCount(scheduleCreateDTO.getTotalCount());
        schedule.setAvailableCount(scheduleCreateDTO.getTotalCount()); // 初始库存=总号源
        schedule.setPrice(scheduleCreateDTO.getPrice());
        schedule.setStatus(1); // 已发布

        baseMapper.insert(schedule);

        log.info("排班创建成功: scheduleId={}", schedule.getScheduleId());

        // 4. 同步库存到Redis
        syncStockToRedis(schedule.getScheduleId());

        return schedule.getScheduleId();
    }

    /**
     * 查询排班列表
     *
     * @param queryDTO 查询条件
     * @return 排班列表
     */
    @Override
    public List<ScheduleVO> getScheduleList(ScheduleQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getHospitalId() != null) {
            wrapper.eq(Schedule::getHospitalId, queryDTO.getHospitalId());
        }
        if (queryDTO.getDepartmentId() != null) {
            wrapper.eq(Schedule::getDepartmentId, queryDTO.getDepartmentId());
        }
        if (queryDTO.getDoctorId() != null) {
            wrapper.eq(Schedule::getDoctorId, queryDTO.getDoctorId());
        }
        if (queryDTO.getWorkDate() != null) {
            wrapper.eq(Schedule::getWorkDate, queryDTO.getWorkDate());
        }

        wrapper.eq(Schedule::getStatus, 1)
               .ge(Schedule::getWorkDate, LocalDate.now())
               .orderByAsc(Schedule::getWorkDate)
               .orderByAsc(Schedule::getTimeSlot);

        List<Schedule> schedules = baseMapper.selectList(wrapper);

        return schedules.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据ID查询排班详情
     *
     * @param scheduleId 排班ID
     * @return 排班详情
     */
    @Override
    public ScheduleDetailVO getScheduleDetail(Long scheduleId) {
        Schedule schedule = baseMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(ResultCode.SCHEDULE_NOT_FOUND);
        }

        ScheduleDetailVO vo = new ScheduleDetailVO();
        BeanUtils.copyProperties(schedule, vo);

        // 填充医生信息
        Doctor doctor = doctorService.getById(schedule.getDoctorId());
        if (doctor != null) {
            vo.setDoctorId(doctor.getDoctorId());
            vo.setDoctorName(doctor.getDoctorName());
            vo.setDoctorTitle(doctor.getTitle());
            vo.setDoctorAvatar(doctor.getAvatar());
            vo.setDoctorSpecialty(doctor.getSpecialty());
        }

        // 填充医院和科室信息
        com.hospital.doctor.entity.Hospital hospital = hospitalService.getById(schedule.getHospitalId());
        if (hospital != null) {
            vo.setHospitalId(hospital.getHospitalId());
            vo.setHospitalName(hospital.getHospitalName());
            vo.setHospitalAddress(hospital.getAddress());
            vo.setHospitalPhone(hospital.getPhone());
        }

        com.hospital.doctor.entity.Department department = departmentService.getById(schedule.getDepartmentId());
        if (department != null) {
            vo.setDepartmentId(department.getDepartmentId());
            vo.setDepartmentName(department.getDepartmentName());
        }

        return vo;
    }

    /**
     * 根据医生ID查询未来7天排班
     *
     * @param doctorId 医生ID
     * @return 排班列表
     */
    @Override
    public List<ScheduleVO> getDoctorScheduleNext7Days(Long doctorId) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);

        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
               .ge(Schedule::getWorkDate, startDate)
               .le(Schedule::getWorkDate, endDate)
               .eq(Schedule::getStatus, 1)
               .orderByAsc(Schedule::getWorkDate)
               .orderByAsc(Schedule::getTimeSlot);

        List<Schedule> schedules = baseMapper.selectList(wrapper);
        return schedules.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 同步号源库存到Redis
     *
     * @param scheduleId 排班ID
     */
    @Override
    public void syncStockToRedis(Long scheduleId) {
        Schedule schedule = baseMapper.selectById(scheduleId);
        if (schedule == null) {
            log.warn("排班不存在: scheduleId={}", scheduleId);
            return;
        }

        String key = String.format(REDIS_KEY_STOCK, scheduleId);
        redisTemplate.opsForValue().set(key, String.valueOf(schedule.getAvailableCount()), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        log.info("号源库存同步到Redis: scheduleId={}, availableCount={}", scheduleId, schedule.getAvailableCount());
    }

    /**
     * 批量同步号源到Redis（每日凌晨执行）
     */
    @Override
    public void batchSyncStockToRedis() {
        log.info("开始批量同步号源库存到Redis");

        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30); // 同步未来30天

        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Schedule::getWorkDate, today)
               .le(Schedule::getWorkDate, futureDate)
               .eq(Schedule::getStatus, 1)
               .eq(Schedule::getDeleted, 0);

        List<Schedule> schedules = baseMapper.selectList(wrapper);

        for (Schedule schedule : schedules) {
            syncStockToRedis(schedule.getScheduleId());
        }

        log.info("批量同步完成: 共{}条排班", schedules.size());
    }

    /**
     * 更新排班状态
     *
     * @param scheduleId 排班ID
     * @param status     状态
     */
    @Override
    public void updateScheduleStatus(Long scheduleId, Integer status) {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(scheduleId);
        schedule.setStatus(status);
        baseMapper.updateById(schedule);

        log.info("更新排班状态: scheduleId={}, status={}", scheduleId, status);
    }

    /**
     * 获取最近可预约日期
     *
     * @param doctorId 医生ID
     * @return 最近可预约日期
     */
    public LocalDate getNextAvailableDate(Long doctorId) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
               .gt(Schedule::getAvailableCount, 0)
               .ge(Schedule::getWorkDate, LocalDate.now())
               .eq(Schedule::getStatus, 1)
               .orderByAsc(Schedule::getWorkDate)
               .last("LIMIT 1");

        Schedule schedule = baseMapper.selectOne(wrapper);
        return schedule != null ? schedule.getWorkDate() : null;
    }

    /**
     * 实体转VO
     *
     * @param schedule 排班实体
     * @return 排班VO
     */
    private ScheduleVO convertToVO(Schedule schedule) {
        ScheduleVO vo = new ScheduleVO();
        BeanUtils.copyProperties(schedule, vo);

        // 填充医生信息
        Doctor doctor = doctorService.getById(schedule.getDoctorId());
        if (doctor != null) {
            vo.setDoctorName(doctor.getDoctorName());
            vo.setDoctorTitle(doctor.getTitle());
            vo.setDoctorAvatar(doctor.getAvatar());
        }

        // 填充医院信息
        if (schedule.getHospitalId() != null) {
            com.hospital.doctor.entity.Hospital hospital = hospitalService.getById(schedule.getHospitalId());
            if (hospital != null) {
                vo.setHospitalName(hospital.getHospitalName());
            }
        }

        // 填充科室信息
        com.hospital.doctor.entity.Department department = departmentService.getById(schedule.getDepartmentId());
        if (department != null) {
            vo.setDepartmentName(department.getDepartmentName());
        }

        // 设置时段名称
        vo.setTimeSlotName(getTimeSlotName(schedule.getTimeSlot()));

        // 设置状态名称
        vo.setStatusName(getStatusName(schedule.getStatus()));

        // 判断是否可预约
        vo.setCanAppointment(schedule.getStatus() == 1 && schedule.getAvailableCount() > 0);

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
            case 0 -> "未发布";
            case 1 -> "已发布";
            case 2 -> "已约满";
            case 3 -> "已停诊";
            default -> "未知";
        };
    }
}
