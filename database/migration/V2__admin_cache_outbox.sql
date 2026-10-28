USE `hospital_appointment`;

ALTER TABLE `user`
    ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER'
    COMMENT '角色：USER-普通用户 ADMIN-管理员' AFTER `status`;

CREATE TABLE IF NOT EXISTS `message_outbox` (
    `outbox_id` BIGINT NOT NULL AUTO_INCREMENT,
    `aggregate_id` BIGINT NOT NULL,
    `event_type` VARCHAR(64) NOT NULL,
    `payload` TEXT NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `retry_count` INT NOT NULL DEFAULT 0,
    `next_retry_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_error` VARCHAR(500) DEFAULT NULL,
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`outbox_id`),
    UNIQUE KEY `uk_aggregate_event` (`aggregate_id`, `event_type`),
    KEY `idx_status_retry` (`status`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事务消息表';

-- 按实际管理员账号修改条件；示例数据使用 zhangsan。
UPDATE `user` SET `role` = 'ADMIN' WHERE `username` = 'zhangsan';
