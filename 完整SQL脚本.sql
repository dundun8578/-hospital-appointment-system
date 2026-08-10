-- ========================================
-- blog_db 数据库完整创建脚本
-- 复制下面的所有内容到SQL编辑器运行即可
-- ========================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `blog_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 使用数据库
USE `blog_db`;

-- 3. 创建SQL模板表
CREATE TABLE IF NOT EXISTS `sql_template` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `template_category` VARCHAR(50) NOT NULL COMMENT '模板分类（如：用户管理、文章管理）',
  `sql_content` TEXT NOT NULL COMMENT 'SQL语句内容',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
  `params_json` TEXT DEFAULT NULL COMMENT '参数说明（JSON格式）',
  `is_public` TINYINT(1) DEFAULT '0' COMMENT '是否公开（0:私有 1:公开）',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除（0:未删除 1:已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`template_category`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL模板表';

-- 4. 创建SQL执行记录表
CREATE TABLE IF NOT EXISTS `sql_execution_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` BIGINT DEFAULT NULL COMMENT '关联模板ID',
  `sql_content` TEXT NOT NULL COMMENT '实际执行的SQL',
  `execution_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  `execution_duration` INT DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `affected_rows` INT DEFAULT NULL COMMENT '影响行数',
  `executed_by` BIGINT DEFAULT NULL COMMENT '执行人ID',
  `status` TINYINT DEFAULT '1' COMMENT '执行状态（0:失败 1:成功）',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `params_json` TEXT DEFAULT NULL COMMENT '执行参数（JSON格式）',
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_executed_by` (`executed_by`),
  KEY `idx_execution_time` (`execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL执行记录表';

-- 5. 创建SQL文件存储表
CREATE TABLE IF NOT EXISTS `sql_file` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名称',
  `file_category` VARCHAR(50) DEFAULT NULL COMMENT '文件分类',
  `sql_content` LONGTEXT NOT NULL COMMENT 'SQL文件完整内容',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '文件描述',
  `file_size` INT DEFAULT NULL COMMENT '文件大小（字节）',
  `line_count` INT DEFAULT NULL COMMENT 'SQL行数',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除（0:未删除 1:已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_name` (`file_name`),
  KEY `idx_category` (`file_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL文件存储表';

-- 6. 验证：查看所有表
SHOW TABLES;