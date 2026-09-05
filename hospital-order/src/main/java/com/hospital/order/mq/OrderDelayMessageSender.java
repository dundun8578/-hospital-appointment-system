package com.hospital.order.mq;

import com.hospital.common.mq.OrderCancelMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 延时关单消息发送器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDelayMessageSender {

    private final RabbitTemplate rabbitTemplate;

    public void sendDelayMessage(OrderCancelMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    OrderMQConfig.DELAY_EXCHANGE,
                    OrderMQConfig.DELAY_ROUTING_KEY,
                    message
            );
            log.info("延时关单消息已发送: orderId={}, 15分钟后判定是否超时取消", message.getOrderId());
        } catch (Exception e) {
            // 延时消息发送失败只影响自动关单，不影响订单本身，记录日志人工兜底
            log.error("延时关单消息发送失败: orderId={}", message.getOrderId(), e);
        }
    }
}
