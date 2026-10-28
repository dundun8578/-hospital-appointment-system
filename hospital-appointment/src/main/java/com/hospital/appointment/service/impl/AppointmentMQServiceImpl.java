package com.hospital.appointment.service.impl;

import com.hospital.appointment.dto.AppointmentCreateDTO;
import com.hospital.appointment.mq.AppointmentMessageDTO;
import com.hospital.appointment.service.AppointmentMQService;
import com.hospital.appointment.service.AppointmentOutboxService;
import com.hospital.appointment.vo.AppointmentResultVO;
import com.hospital.common.BusinessException;
import com.hospital.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private AppointmentOutboxService outboxService;

    /**
     * 创建预约并发送MQ消息
     *
     * @param userId 用户ID
     * @param dto 预约信息
     * @return 预约结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentResultVO createAppointmentWithMQ(Long userId, AppointmentCreateDTO dto) {
        log.info("开始创建预约（MQ模式）: userId={}, scheduleId={}", userId, dto.getScheduleId());

        // 1. 调用原有预约逻辑（包含分布式锁+Redis库存扣减）
        AppointmentResultVO result = appointmentService.createAppointment(userId, dto);

        if (result.getSuccess()) {
            // 2. 与预约记录同事务写入Outbox，后台任务收到Broker确认后标记成功
            AppointmentMessageDTO message = new AppointmentMessageDTO();
            message.setAppointmentId(result.getAppointmentId());
            message.setUserId(userId);
            message.setScheduleId(dto.getScheduleId());
            com.hospital.appointment.vo.AppointmentVO detail = appointmentService.getAppointmentDetail(result.getAppointmentId());
            message.setDoctorId(detail.getDoctorId());
            message.setHospitalId(detail.getHospitalId());
            message.setDepartmentId(detail.getDepartmentId());
            message.setWorkDate(String.valueOf(detail.getWorkDate()));
            message.setTimeSlot(detail.getTimeSlot());
            message.setPatientName(dto.getPatientName());
            message.setPatientPhone(dto.getPatientPhone());
            message.setPatientIdCard(dto.getPatientIdCard());
            message.setCreateTime(System.currentTimeMillis());
            message.setRetryCount(0);
            outboxService.savePending(message);
            log.info("预约成功并写入事务消息表: appointmentId={}", result.getAppointmentId());
        }

        return result;
    }
}
