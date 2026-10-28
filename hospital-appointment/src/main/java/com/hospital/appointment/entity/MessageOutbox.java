package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 与预约业务同事务落库的待投递消息。 */
@Data
@TableName("message_outbox")
public class MessageOutbox {
    @TableId(value = "outbox_id", type = IdType.AUTO)
    private Long outboxId;
    private Long aggregateId;
    private String eventType;
    private String payload;
    /** 0-待发送，1-已发送，2-等待重试。 */
    private Integer status;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private String lastError;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
