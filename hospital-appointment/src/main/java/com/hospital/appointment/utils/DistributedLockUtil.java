package com.hospital.appointment.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    /**
     * 预约锁Key前缀
     */
    private static final String APPOINTMENT_LOCK_PREFIX = "appointment:user:%s:schedule:%s";

    /**
     * 锁等待时间（秒）
     */
    private static final long LOCK_WAIT_TIME = 5;

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

        // 等待最多5秒，不指定固定租期，由 Redisson watchdog 续期直到主动释放。
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_WAIT_TIME, TimeUnit.SECONDS);
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
     * 有事务时延迟至事务完成后解锁。应在业务 finally 中调用，
     * 使之前注册的库存回滚补偿先执行，再允许后续预约进入。
     */
    public void unlockAfterTransaction(RLock lock) {
        if (lock == null) {
            return;
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    try {
                        unlock(lock);
                    } catch (Exception e) {
                        // 事务已经结束，解锁失败不能把已提交的预约报告为业务失败。
                        log.error("事务完成后释放预约锁失败: lockKey={}, transactionStatus={}",
                                lock.getName(), status, e);
                    }
                }
            });
        } else {
            // 非事务调用也必须释放已获取的锁。
            unlock(lock);
        }
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
