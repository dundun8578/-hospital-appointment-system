package com.hospital.appointment.service.impl;

import com.hospital.appointment.dto.AppointmentCreateDTO;
import com.hospital.appointment.mq.AppointmentMessageDTO;
import com.hospital.appointment.mq.AppointmentMessageSender;
import com.hospital.appointment.service.AppointmentMQService;
import com.hospital.appointment.vo.AppointmentResultVO;
import com.hospital.common.BusinessException;
import com.hospital.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预约MQ服务实现
 * 创建预约后发送MQ消息异步创建订单
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Service
public class AppointmentMQServiceImpl implements AppointmentMQService {

    @Autowired
    private com.hospital.appointment.service.AppointmentService appointmentService;

    @Autowired
    private AppointmentMessageSender messageSender;

    /**
     * 创建预约并发送MQ消息
     *
     * @param userId 用户ID
     * @param dto 预约信息
     * @return 预约结果
     */
    @Override
    public AppointmentResultVO createAppointmentWithMQ(Long userId, AppointmentCreateDTO dto) {
        log.info("开始创建预约（MQ模式）: userId={}, scheduleId={}", userId, dto.getScheduleId());

        // 1. 调用原有预约逻辑（包含分布式锁+Redis库存扣减）
        AppointmentResultVO result = appointmentService.createAppointment(userId, dto);

        if (result.getSuccess()) {
            // 2. 预约成功，发送MQ消息异步创建订单
            try {
                AppointmentMessageDTO message = new AppointmentMessageDTO();
                message.setAppointmentId(result.getAppointmentId());
                message.setUserId(userId);
                message.setScheduleId(dto.getScheduleId());
                message.setPatientName(dto.getPatientName());
                message.setPatientPhone(dto.getPatientPhone());
                message.setPatientIdCard(dto.getPatientIdCard());
                message.setCreateTime(System.currentTimeMillis());
                message.setRetryCount(0);

                messageSender.sendAppointmentMessage(message);

                log.info("预约成功并发送MQ消息: appointmentId={}", result.getAppointmentId());
            } catch (Exception e) {
                // MQ发送失败不影响预约结果，但需要告警
                log.error("MQ消息发送失败，需人工处理: appointmentId={}", result.getAppointmentId(), e);
                // TODO: 发送告警通知
            }
        }

        return result;
    }
}
