package com.hospital.common.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单关单消息（预约服务与订单服务共用，JDK序列化要求双方类路径一致）
 * 经 order.delay.queue(TTL 15分钟) 死信进入 order.cancel.queue 消费
 */
@Data
public class OrderCancelMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    private Long appointmentId;

    private Long scheduleId;

    /**
     * 取消来源: timeout-超时未支付, user_cancel-用户取消预约
     */
    private String source;
}
