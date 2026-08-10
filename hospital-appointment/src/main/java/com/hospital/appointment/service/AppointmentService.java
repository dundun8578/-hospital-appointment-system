package com.hospital.appointment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.appointment.dto.AppointmentCreateDTO;
import com.hospital.appointment.dto.AppointmentQueryDTO;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.vo.AppointmentVO;
import com.hospital.common.PageResult;

import java.util.List;

/**
 * 预约服务接口
 *
 * @author hospital
 * @date 2024-01-15
 */
public interface AppointmentService extends IService<Appointment> {

    /**
     * 创建预约（核心方法，包含分布式锁和Redis库存扣减）
     *
     * @param userId            用户ID
     * @param appointmentCreateDTO 预约信息
     * @return 预约结果
     */
    com.hospital.appointment.vo.AppointmentResultVO createAppointment(Long userId, AppointmentCreateDTO appointmentCreateDTO);

    /**
     * 根据ID查询预约详情
     *
     * @param appointmentId 预约ID
     * @return 预约VO
     */
    AppointmentVO getAppointmentDetail(Long appointmentId);

    /**
     * 查询用户预约列表
     *
     * @param queryDTO 查询条件
     * @return 预约列表
     */
    List<AppointmentVO> getUserAppointments(AppointmentQueryDTO queryDTO);

    /**
     * 分页查询用户预约列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AppointmentVO> getUserAppointmentsPage(AppointmentQueryDTO queryDTO);

    /**
     * 取消预约
     *
     * @param appointmentId 预约ID
     * @param userId        用户ID
     * @param cancelReason  取消原因
     * @return 是否成功
     */
    boolean cancelAppointment(Long appointmentId, Long userId, String cancelReason);

    /**
     * 检查用户是否已预约该排班
     *
     * @param userId    用户ID
     * @param scheduleId 排班ID
     * @return true-已预约，false-未预约
     */
    boolean checkUserAppointed(Long userId, Long scheduleId);
}
