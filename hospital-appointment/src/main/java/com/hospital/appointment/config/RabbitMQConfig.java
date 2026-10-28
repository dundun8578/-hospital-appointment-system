package com.hospital.appointment.config;

import com.hospital.appointment.mq.AppointmentMessageSender;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ拓扑配置
 * 预约队列绑定死信交换机：消费失败（重试耗尽后requeue=false）的消息进入死信队列，
 * 避免无限重入队；死信保留在队列中供人工排查与重放
 */
@Configuration
public class RabbitMQConfig {

    public static final String DLX_EXCHANGE = "appointment.dlx.exchange";
    public static final String DLQ_QUEUE = "appointment.dlq";

    public static final String ORDER_CANCEL_EXCHANGE = "order.cancel.exchange";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    @Bean
    public DirectExchange appointmentExchange() {
        return new DirectExchange(AppointmentMessageSender.EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue appointmentQueue() {
        return QueueBuilder.durable(AppointmentMessageSender.QUEUE_NAME)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DLQ_QUEUE)
                .build();
    }

    @Bean
    public Binding appointmentBinding(Queue appointmentQueue, DirectExchange appointmentExchange) {
        return BindingBuilder.bind(appointmentQueue)
                .to(appointmentExchange)
                .with(AppointmentMessageSender.ROUTING_KEY);
    }

    @Bean
    public FanoutExchange appointmentDlxExchange() {
        return new FanoutExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue appointmentDlqQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public Binding appointmentDlqBinding(Queue appointmentDlqQueue, FanoutExchange appointmentDlxExchange) {
        return BindingBuilder.bind(appointmentDlqQueue).to(appointmentDlxExchange);
    }

    /**
     * 订单联动取消的交换机与队列（与 order 模块 OrderMQConfig 幂等声明，
     * 保证即使订单服务未启动，预约服务的取消通知也能投递）
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(name = "orderCancelExchange")
    public DirectExchange orderCancelExchange() {
        return new DirectExchange(ORDER_CANCEL_EXCHANGE, true, false);
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(name = "orderCancelQueue")
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE).build();
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(name = "orderCancelBinding")
    public Binding orderCancelBinding(Queue orderCancelQueue, DirectExchange orderCancelExchange) {
        return BindingBuilder.bind(orderCancelQueue)
                .to(orderCancelExchange)
                .with(ORDER_CANCEL_ROUTING_KEY);
    }
}
