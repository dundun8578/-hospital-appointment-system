package com.hospital.order.listener;

import com.hospital.common.BusinessException;
import com.hospital.common.ResultCode;
import com.hospital.doctor.config.RedisKeyConstants;
import com.hospital.doctor.entity.Schedule;
import com.hospital.doctor.service.ScheduleService;
import com.hospital.order.entity.Order;
import com.hospital.common.mq.OrderCancelMessage;
import com.hospital.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单关单消费者
 * 消费 order.cancel.queue（两类来源）：
 * 1. 超时未支付：延时队列TTL到期死信，订单仍待支付则取消并回补号源
 * 2. 用户取消预约：appointment服务发来的联动取消，订单待支付则同步取消（号源由预约服务回补）
 */
@Slf4j
@Component
public class OrderTimeoutListener {

    private static final String STOCK_KEY_PREFIX = RedisKeyConstants.SCHEDULE_STOCK_KEY;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(queues = "order.cancel.queue")
    public void handleOrderCancel(OrderCancelMessage message) {
        log.info("收到关单消息: orderId={}, appointmentId={}, source={}",
                message.getOrderId(), message.getAppointmentId(), message.getSource());
        try {
            // orderId可能为空（用户取消预约场景由预约服务发起），按appointmentId兜底解析
            Order order;
            if (message.getOrderId() != null) {
                order = orderService.getById(message.getOrderId());
            } else {
                order = orderService.lambdaQuery()
                        .eq(Order::getAppointmentId, message.getAppointmentId())
                        .eq(Order::getDeleted, 0)
                        .one();
            }
            if (order == null) {
                log.warn("订单不存在，忽略关单消息: orderId={}, appointmentId={}",
                        message.getOrderId(), message.getAppointmentId());
                return;
            }
            // 已支付/已取消的订单不再处理（幂等）
            if (order.getPayStatus() != null && order.getPayStatus() != 0) {
                log.info("订单已支付，跳过关单: orderId={}", order.getOrderId());
                return;
            }
            if (order.getOrderStatus() != null && order.getOrderStatus() != 1) {
                log.info("订单已处理过，跳过关单: orderId={}, status={}", order.getOrderId(), order.getOrderStatus());
                return;
            }

            boolean isUserCancel = "user_cancel".equals(message.getSource());
            order.setOrderStatus(isUserCancel ? 3 : 2); // 3-已取消 2-超时关闭
            order.setRemark(isUserCancel ? "用户取消预约，订单同步取消" : "超时未支付，系统自动取消");
            orderService.updateById(order);

            // 超时关单需要回补号源（用户取消预约时号源已由预约服务回补，不重复操作）
            if (!isUserCancel) {
                restoreStock(message.getScheduleId());
            }
            log.info("关单处理完成: orderId={}, source={}", order.getOrderId(), message.getSource());
        } catch (BusinessException e) {
            log.warn("关单业务异常: {}", e.getMessage());
        } catch (Exception e) {
            log.error("关单消息处理失败, 转入死信队列: orderId={}", message.getOrderId(), e);
            throw e;
        }
    }

    /**
     * 回补号源：Redis库存INCR + DB排班available_count+1
     */
    private void restoreStock(Long scheduleId) {
        try {
            String key = String.format(STOCK_KEY_PREFIX, scheduleId);
            redisTemplate.opsForValue().increment(key);
            scheduleService.lambdaUpdate()
                    .eq(Schedule::getScheduleId, scheduleId)
                    .setSql("available_count = available_count + 1")
                    .update();
            log.info("号源已回补: scheduleId={}", scheduleId);
        } catch (Exception e) {
            log.error("号源回补失败，需人工核对: scheduleId={}", scheduleId, e);
        }
    }
}
