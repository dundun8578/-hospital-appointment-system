package com.hospital.doctor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.doctor.dto.ScheduleCreateDTO;
import com.hospital.doctor.dto.ScheduleQueryDTO;
import com.hospital.doctor.entity.Schedule;
import com.hospital.doctor.vo.ScheduleDetailVO;
import com.hospital.doctor.vo.ScheduleVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班服务接口
 *
 * @author hospital
 * @date 2024-01-15
 */
public interface ScheduleService extends IService<Schedule> {

    /**
     * 创建排班
     *
     * @param scheduleCreateDTO 排班信息
     * @return 排班ID
     */
    Long createSchedule(ScheduleCreateDTO scheduleCreateDTO);

    /**
     * 查询排班列表
     *
     * @param queryDTO 查询条件
     * @return 排班列表
     */
    List<ScheduleVO> getScheduleList(ScheduleQueryDTO queryDTO);

    /**
     * 根据ID查询排班详情
     *
     * @param scheduleId 排班ID
     * @return 排班详情
     */
    ScheduleDetailVO getScheduleDetail(Long scheduleId);

    /**
     * 根据医生ID查询未来7天排班
     *
     * @param doctorId 医生ID
     * @return 排班列表
     */
    List<ScheduleVO> getDoctorScheduleNext7Days(Long doctorId);

    /**
     * 同步号源库存到Redis
     *
     * @param scheduleId 排班ID
     */
    void syncStockToRedis(Long scheduleId);

    /**
     * 批量同步号源到Redis（每日凌晨执行）
     */
    void batchSyncStockToRedis();

    /**
     * 更新排班状态
     *
     * @param scheduleId 排班ID
     * @param status     状态
     */
    /**
     * 获取医生最近可预约日期
     */
    java.time.LocalDate getNextAvailableDate(Long doctorId);

    void updateScheduleStatus(Long scheduleId, Integer status);
}
