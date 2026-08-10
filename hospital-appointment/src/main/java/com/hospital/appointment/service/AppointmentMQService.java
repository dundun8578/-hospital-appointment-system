package com.hospital.appointment.service;

import com.hospital.appointment.dto.AppointmentCreateDTO;
import com.hospital.appointment.vo.AppointmentResultVO;

/**
 * 预约服务接口（扩展）
 * 包含MQ消息发送
 *
 * @author hospital
 * @date 2024-01-15
 */
public interface AppointmentMQService {

    /**
     * 创建预约并发送MQ消息
     *
     * @param userId 用户ID
     * @param dto 预约信息
     * @return 预约结果
     */
    AppointmentResultVO createAppointmentWithMQ(Long userId, AppointmentCreateDTO dto);
}
