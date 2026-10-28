package com.hospital.appointment.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 预约消息发送者
 * 负责发送预约消息到RabbitMQ
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentMessageSender {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Exchange名称
     */
    public static final String EXCHANGE_NAME = "appointment.exchange";

    /**
     * Queue名称
     */
    public static final String QUEUE_NAME = "appointment.queue";

    /**
     * Routing Key
     */
    public static final String ROUTING_KEY = "appointment.create";

    /**
     * 发送预约消息
     *
     * @param message 预约消息
     */
    public void sendAppointmentMessage(AppointmentMessageDTO message) {
        try {
            log.info("发送预约消息到MQ: appointmentId={}, userId={}, scheduleId={}",
                    message.getAppointmentId(), message.getUserId(), message.getScheduleId());

            CorrelationData correlationData = new CorrelationData(String.valueOf(message.getAppointmentId()));
            rabbitTemplate.convertAndSend(
                    EXCHANGE_NAME,
                    ROUTING_KEY,
                    message,
                    msg -> {
                        // 消息PostProcessor：添加消息属性
                        msg.getMessageProperties().setPriority(5); // 优先级
                        return msg;
                    },
                    correlationData
            );

            CorrelationData.Confirm confirm = correlationData.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ未确认消息: " + confirm.getReason());
            }

            log.info("预约消息发送成功: appointmentId={}", message.getAppointmentId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待RabbitMQ确认被中断", e);
        } catch (Exception e) {
            log.error("预约消息发送失败: appointmentId={}", message.getAppointmentId(), e);
            throw new IllegalStateException("预约消息发送失败", e);
        }
    }
}
