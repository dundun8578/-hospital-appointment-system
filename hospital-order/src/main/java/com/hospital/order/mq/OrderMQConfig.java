package com.hospital.order.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单MQ拓扑配置
 * 延时关单：order.delay.queue 设置队列级TTL（15分钟）与死信交换机，
 * 消息到期后死信进入 order.cancel.queue，由消费者判定订单是否需要超时取消。
 * 队列级TTL避免消息级TTL的队头阻塞问题。
 */
@Configuration
public class OrderMQConfig {

    public static final String DELAY_EXCHANGE = "order.delay.exchange";
    public static final String DELAY_QUEUE = "order.delay.queue";
    public static final String DELAY_ROUTING_KEY = "order.delay";
    public static final long DELAY_TTL_MS = 15 * 60 * 1000L;

    public static final String CANCEL_EXCHANGE = "order.cancel.exchange";
    public static final String CANCEL_QUEUE = "order.cancel.queue";
    public static final String CANCEL_ROUTING_KEY = "order.cancel";

    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(DELAY_EXCHANGE, true, false);
    }

    /**
     * 延时队列：不设消费者，消息TTL到期后死信到取消队列
     */
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .withArgument("x-message-ttl", DELAY_TTL_MS)
                .withArgument("x-dead-letter-exchange", CANCEL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CANCEL_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderDelayBinding(Queue orderDelayQueue, DirectExchange orderDelayExchange) {
        return BindingBuilder.bind(orderDelayQueue)
                .to(orderDelayExchange)
                .with(DELAY_ROUTING_KEY);
    }

    @Bean
    public DirectExchange orderCancelExchange() {
        return new DirectExchange(CANCEL_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(CANCEL_QUEUE).build();
    }

    @Bean
    public Binding orderCancelBinding(Queue orderCancelQueue, DirectExchange orderCancelExchange) {
        return BindingBuilder.bind(orderCancelQueue)
                .to(orderCancelExchange)
                .with(CANCEL_ROUTING_KEY);
    }
}
