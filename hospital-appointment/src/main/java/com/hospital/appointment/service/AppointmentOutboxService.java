package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.entity.MessageOutbox;
import com.hospital.appointment.mapper.MessageOutboxMapper;
import com.hospital.appointment.mq.AppointmentMessageDTO;
import com.hospital.appointment.mq.AppointmentMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** 事务消息表：业务提交后持续重试，直到 RabbitMQ 确认接收。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentOutboxService {
    private static final String EVENT_TYPE = "APPOINTMENT_CREATED";
    private static final int BATCH_SIZE = 100;

    private final MessageOutboxMapper outboxMapper;
    private final AppointmentMessageSender messageSender;
    private final ObjectMapper objectMapper;

    public void savePending(AppointmentMessageDTO message) {
        MessageOutbox outbox = new MessageOutbox();
        outbox.setAggregateId(message.getAppointmentId());
        outbox.setEventType(EVENT_TYPE);
        try {
            outbox.setPayload(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            throw new IllegalStateException("预约消息序列化失败", e);
        }
        outbox.setStatus(0);
        outbox.setRetryCount(0);
        outbox.setNextRetryTime(LocalDateTime.now());
        outbox.setCreatedTime(LocalDateTime.now());
        outbox.setUpdatedTime(LocalDateTime.now());
        try {
            outboxMapper.insert(outbox);
        } catch (DuplicateKeyException ignored) {
            log.info("预约事务消息已存在: appointmentId={}", message.getAppointmentId());
        }
    }

    @Scheduled(fixedDelayString = "${appointment.outbox.publish-interval-ms:2000}")
    public void publishPending() {
        List<MessageOutbox> messages = outboxMapper.selectList(new LambdaQueryWrapper<MessageOutbox>()
                .in(MessageOutbox::getStatus, 0, 2)
                .le(MessageOutbox::getNextRetryTime, LocalDateTime.now())
                .orderByAsc(MessageOutbox::getOutboxId)
                .last("LIMIT " + BATCH_SIZE));
        messages.forEach(this::publishOne);
    }

    private void publishOne(MessageOutbox outbox) {
        try {
            AppointmentMessageDTO message = objectMapper.readValue(outbox.getPayload(), AppointmentMessageDTO.class);
            messageSender.sendAppointmentMessage(message);
            outbox.setStatus(1);
            outbox.setLastError(null);
        } catch (Exception e) {
            int retries = outbox.getRetryCount() == null ? 1 : outbox.getRetryCount() + 1;
            outbox.setStatus(2);
            outbox.setRetryCount(retries);
            outbox.setNextRetryTime(LocalDateTime.now().plusSeconds(Math.min(300, 1L << Math.min(retries, 8))));
            String error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            outbox.setLastError(error.substring(0, Math.min(error.length(), 500)));
            log.warn("预约消息投递失败，稍后重试: outboxId={}, retry={}", outbox.getOutboxId(), retries, e);
        }
        outbox.setUpdatedTime(LocalDateTime.now());
        outboxMapper.updateById(outbox);
    }
}
