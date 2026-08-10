package com.hospital.appointment.config;

import com.hospital.doctor.config.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.PostConstruct;

/**
 * 号源库存初始化
 * 系统启动时加载号源到Redis
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Configuration
public class StockInitializer {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private com.hospital.doctor.service.ScheduleService scheduleService;

    /**
     * 系统启动时初始化库存
     */
    @PostConstruct
    public void init() {
        log.info("系统启动，开始初始化号源库存到Redis");
        try {
            scheduleService.batchSyncStockToRedis();
            log.info("号源库存初始化完成");
        } catch (Exception e) {
            log.error("号源库存初始化失败", e);
        }
    }
}
