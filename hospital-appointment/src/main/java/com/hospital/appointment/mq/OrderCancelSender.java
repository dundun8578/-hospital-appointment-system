package com.hospital.appointment.mq;

import com.hospital.common.mq.OrderCancelMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单联动取消发送器
 * 用户取消预约时通知订单服务同步取消待支付订单（号源由预约服务负责回补）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelSender {

    public static final String CANCEL_EXCHANGE = "order.cancel.exchange";
    public static final String CANCEL_ROUTING_KEY = "order.cancel";

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderCancelMessage(Long appointmentId, Long scheduleId) {
        try {
            OrderCancelMessage message = new OrderCancelMessage();
            message.setAppointmentId(appointmentId);
            message.setScheduleId(scheduleId);
            message.setSource("user_cancel");
            rabbitTemplate.convertAndSend(CANCEL_EXCHANGE, CANCEL_ROUTING_KEY, message);
            log.info("订单联动取消消息已发送: appointmentId={}", appointmentId);
        } catch (Exception e) {
            // 通知失败不影响取消主流程，订单服务定时任务可兜底
            log.error("订单联动取消消息发送失败: appointmentId={}", appointmentId, e);
        }
    }
}
