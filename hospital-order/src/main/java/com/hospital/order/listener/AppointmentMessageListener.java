package com.hospital.order.listener;

import com.hospital.appointment.mq.AppointmentMessageDTO;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.service.DoctorService;
import com.hospital.order.entity.Order;
import com.hospital.order.service.OrderService;
import com.hospital.user.entity.User;
import com.hospital.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 预约消息消费者
 * 监听预约队列，异步创建订单
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class AppointmentMessageListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    /**
     * 消费预约消息
     * 1. 查询用户和医生信息
     * 2. 创建订单
     * 3. 发送通知（TODO）
     *
     * @param message 预约消息
     */
    @RabbitListener(queues = com.hospital.appointment.mq.AppointmentMessageSender.QUEUE_NAME)
    public void handleAppointmentMessage(AppointmentMessageDTO message) {
        log.info("收到预约消息: appointmentId={}, userId={}, scheduleId={}",
                message.getAppointmentId(), message.getUserId(), message.getScheduleId());

        try {
            // 1. 查询医生信息，获取挂号费
            Doctor doctor = doctorService.getById(message.getDoctorId());
            if (doctor == null) {
                log.error("医生不存在: doctorId={}", message.getDoctorId());
                // 抛出异常，消息重新入队
                throw new RuntimeException("医生不存在");
            }

            BigDecimal price = doctor.getPrice();

            // 2. 创建订单
            Order order = new Order();
            order.setAppointmentId(message.getAppointmentId());
            order.setUserId(message.getUserId());
            order.setDoctorId(message.getDoctorId());
            order.setAmount(price);
            order.setOrderStatus(1); // 待支付
            order.setPayStatus(0);   // 待支付
            order.setRemark("预约挂号订单");

            Long orderId = orderService.createOrder(order);

            log.info("订单创建成功: orderId={}, orderNo={}, appointmentId={}",
                    orderId, order.getOrderNo(), message.getAppointmentId());

            // 3. TODO: 发送预约成功通知（短信/站内信）
            // sendNotification(message);

        } catch (Exception e) {
            log.error("处理预约消息失败: appointmentId={}", message.getAppointmentId(), e);
            // 抛出异常让Spring AMQP重试
            throw e;
        }
    }

    /**
     * 处理死信队列中的消息
     * 消费失败多次后进入死信队列
     *
     * @param message 预约消息
     */
    @RabbitListener(queues = "appointment.dlq")
    public void handleDeadLetterMessage(AppointmentMessageDTO message) {
        log.error("处理死信消息: appointmentId={}, userId={}, retryCount={}",
                message.getAppointmentId(), message.getUserId(), message.getRetryCount());

        // TODO: 记录告警日志，通知人工处理
        // alertService.sendAlert("预约消息消费失败", message);

        // 记录到数据库，供后续人工处理
        // saveFailedMessage(message);
    }
}
