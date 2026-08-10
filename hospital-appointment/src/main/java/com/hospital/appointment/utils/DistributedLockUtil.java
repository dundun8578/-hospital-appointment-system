package com.hospital.appointment.utils;

import com.hospital.doctor.entity.Schedule;
import com.hospital.doctor.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 * 基于Redisson实现分布式锁，防止重复预约
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Component
public class DistributedLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 预约锁Key前缀
     */
    private static final String APPOINTMENT_LOCK_PREFIX = "appointment:user:%s:schedule:%s";

    /**
     * 锁等待时间（秒）
     */
    private static final long LOCK_WAIT_TIME = 5;

    /**
     * 锁自动释放时间（秒）
     */
    private static final long LOCK_LEASE_TIME = 30;

    /**
     * 获取预约分布式锁
     *
     * @param userId    用户ID
     * @param scheduleId 排班ID
     * @return 锁对象，获取失败返回null
     */
    public RLock getAppointmentLock(Long userId, Long scheduleId) {
        String lockKey = String.format(APPOINTMENT_LOCK_PREFIX, userId, scheduleId);
        RLock lock = redissonClient.getLock(lockKey);

        // 尝试获取锁，等待5秒，锁30秒后自动释放
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (acquired) {
                log.info("获取分布式锁成功: userId={}, scheduleId={}, lockKey={}", userId, scheduleId, lockKey);
            } else {
                log.warn("获取分布式锁失败: userId={}, scheduleId={}, lockKey={}", userId, scheduleId, lockKey);
            }
        } catch (InterruptedException e) {
            log.error("获取分布式锁被中断: lockKey={}", lockKey, e);
            Thread.currentThread().interrupt();
        }

        return acquired ? lock : null;
    }

    /**
     * 释放锁
     *
     * @param lock 锁对象
     */
    public void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("释放分布式锁成功: {}", lock.getName());
        }
    }

    /**
     * 检查用户是否已预约该排班
     *
     * @param userId    用户ID
     * @param scheduleId 排班ID
     * @return true-已预约，false-未预约
     */
    public boolean isAppointed(Long userId, Long scheduleId) {
        // 这里需要查询appointment表
        // 为了简化，返回false，实际应该在Service中查询
        return false;
    }
}
