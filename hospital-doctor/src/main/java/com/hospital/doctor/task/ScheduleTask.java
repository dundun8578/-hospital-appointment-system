package com.hospital.doctor.task;

import com.hospital.doctor.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 排班定时任务
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class ScheduleTask {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 每日凌晨0点同步号源库存到Redis
     * Cron表达式：每天0点执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncScheduleStockToRedis() {
        log.info("定时任务：开始同步号源库存到Redis");
        try {
            scheduleService.batchSyncStockToRedis();
            log.info("定时任务：号源库存同步完成");
        } catch (Exception e) {
            log.error("定时任务：号源库存同步失败", e);
        }
    }
}
