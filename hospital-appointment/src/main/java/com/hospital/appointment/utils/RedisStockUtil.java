package com.hospital.appointment.utils;

import com.hospital.doctor.config.RedisKeyConstants;
import com.hospital.doctor.entity.Schedule;
import com.hospital.doctor.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis库存工具类
 * 提供库存查询、扣减、回滚等功能
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class RedisStockUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 库存Key前缀
     */
    private static final String STOCK_KEY_PREFIX = RedisKeyConstants.SCHEDULE_STOCK_KEY;

    /**
     * 缓存过期时间：24小时
     */
    private static final long CACHE_EXPIRE_HOURS = RedisKeyConstants.CACHE_EXPIRE_HOURS;

    /**
     * 查询Redis库存
     *
     * @param scheduleId 排班ID
     * @return 剩余库存，-1表示Redis中不存在
     */
    public int getStock(Long scheduleId) {
        String key = String.format(STOCK_KEY_PREFIX, scheduleId);
        String stock = redisTemplate.opsForValue().get(key);
        if (stock == null) {
            return -1;
        }
        return Integer.parseInt(stock);
    }

    /**
     * 同步数据库库存到Redis
     *
     * @param scheduleId 排班ID
     */
    public void syncStockFromDb(Long scheduleId) {
        Schedule schedule = scheduleService.getById(scheduleId);
        if (schedule != null) {
            String key = String.format(STOCK_KEY_PREFIX, scheduleId);
            redisTemplate.opsForValue().set(key, String.valueOf(schedule.getAvailableCount()), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("库存同步完成: scheduleId={}, availableCount={}", scheduleId, schedule.getAvailableCount());
        }
    }

    /**
     * 检查库存是否充足
     *
     * @param scheduleId 排班ID
     * @return true-充足，false-不足或不存在
     */
    public boolean hasStock(Long scheduleId) {
        int stock = getStock(scheduleId);
        return stock > 0;
    }

    /**
     * 获取排班详情（优先从缓存）
     *
     * @param scheduleId 排班ID
     * @return 排班信息
     */
    public Schedule getSchedule(Long scheduleId) {
        return scheduleService.getById(scheduleId);
    }
}
