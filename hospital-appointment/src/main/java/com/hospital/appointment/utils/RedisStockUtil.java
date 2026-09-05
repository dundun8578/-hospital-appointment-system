package com.hospital.appointment.utils;

import com.hospital.doctor.config.RedisKeyConstants;
import com.hospital.doctor.entity.Schedule;
import com.hospital.doctor.service.ScheduleService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis库存工具类
 * 库存扣减通过Lua脚本在Redis侧原子执行「判断余量→扣减」，单次往返防超卖
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

    @Value("${appointment.lua-script-path:lua/deduct_stock.lua}")
    private String luaScriptPath;

    private DefaultRedisScript<Long> deductScript;

    @PostConstruct
    public void init() {
        deductScript = new DefaultRedisScript<>();
        deductScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(luaScriptPath)));
        deductScript.setResultType(Long.class);
    }

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
     * 原子扣减库存：Lua脚本在Redis侧原子执行「判断余量→扣减」，单次往返防超卖
     * 脚本返回值：-1=库存Key不存在（未预热），0=库存不足，>0=扣减后剩余
     *
     * @param scheduleId 排班ID
     * @return true-扣减成功，false-库存不足
     */
    public boolean deductStock(Long scheduleId) {
        String key = String.format(STOCK_KEY_PREFIX, scheduleId);
        Long result = redisTemplate.execute(deductScript, Collections.singletonList(key), "1");
        // Key不存在（预热遗漏/过期）：从DB同步后重试一次
        if (result != null && result == -1) {
            log.info("Redis库存Key不存在，同步DB后重试: scheduleId={}", scheduleId);
            syncStockFromDb(scheduleId);
            result = redisTemplate.execute(deductScript, Collections.singletonList(key), "1");
        }
        return result != null && result >= 1;
    }

    /**
     * 归还库存（取消预约/预约失败回补）
     *
     * @param scheduleId 排班ID
     */
    public void restoreStock(Long scheduleId) {
        String key = String.format(STOCK_KEY_PREFIX, scheduleId);
        redisTemplate.opsForValue().increment(key);
        log.info("库存已回补: scheduleId={}", scheduleId);
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
