-- ===================================================
-- 医院预约挂号系统数据库设计
-- Database: hospital_appointment
-- Author: hospital
-- Date: 2024-01-15
-- Description: 高并发预约挂号系统核心表结构
-- ===================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `hospital_appointment` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `hospital_appointment`;

-- ===================================================
-- 1. 用户表（user）
-- 存储患者用户信息
-- ===================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
    `id_card` VARCHAR(18) NOT NULL COMMENT '身份证号',
    `gender` TINYINT NOT NULL DEFAULT 1 COMMENT '性别：1-男 2-女 0-未知',
    `birthday` DATE DEFAULT NULL COMMENT '出生日期',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '联系地址',
    `avatar` VARCHAR(200) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_id_card` (`id_card`),
    KEY `idx_username` (`username`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表（患者）';

-- 插入测试用户（密码：123456，BCrypt加密）
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `id_card`, `gender`, `birthday`, `address`) VALUES
('zhangsan', '$2a$10$YQN.9Z8W5xH7p2s9xYvKOuW8p5s7xYvKOuW8p5s7xYvKOuW8p5s7x', '张三', '13800138001', '110101199001011234', 1, '1990-01-01', '北京市朝阳区'),
('lisi', '$2a$10$YQN.9Z8W5xH7p2s9xYvKOuW8p5s7xYvKOuW8p5s7xYvKOuW8p5s7x', '李四', '13800138002', '110101199102022345', 2, '1991-02-02', '北京市海淀区'),
('wangwu', '$2a$10$YQN.9Z8W5xH7p2s9xYvKOuW8p5s7xYvKOuW8p5s7xYvKOuW8p5s7x', '王五', '13800138003', '110101199203033456', 1, '1992-03-03', '北京市西城区');


-- ===================================================
-- 2. 医院表（hospital）
-- 存储医院基本信息
-- ===================================================
DROP TABLE IF EXISTS `hospital`;
CREATE TABLE `hospital` (
    `hospital_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '医院ID，主键',
    `hospital_name` VARCHAR(100) NOT NULL COMMENT '医院名称',
    `hospital_code` VARCHAR(50) NOT NULL COMMENT '医院编码',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) NOT NULL COMMENT '区县',
    `address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `level` VARCHAR(20) NOT NULL COMMENT '医院等级：三级甲等/三级乙等/二级甲等/二级乙等/一级',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '医院类型：1-综合医院 2-专科医院 3-中医医院',
    `description` TEXT DEFAULT NULL COMMENT '医院简介',
    `logo` VARCHAR(200) DEFAULT NULL COMMENT '医院logo',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-停用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`hospital_id`),
    UNIQUE KEY `uk_hospital_code` (`hospital_code`),
    KEY `idx_province_city` (`province`, `city`),
    KEY `idx_level` (`level`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医院表';

-- 插入测试医院数据
INSERT INTO `hospital` (`hospital_name`, `hospital_code`, `province`, `city`, `district`, `address`, `phone`, `level`, `type`, `description`) VALUES
('北京协和医院', 'BJXH001', '北京市', '北京市', '东城区', '东城区帅府园1号', '010-69156114', '三级甲等', 1, '中国医学科学院北京协和医院是集医疗、教学、科研于一体的现代化综合医院'),
('北京301医院', 'BJ301002', '北京市', '北京市', '海淀区', '海淀区复兴路28号', '010-66939000', '三级甲等', 1, '中国人民解放军总医院是集医疗、保健、教学、科研于一体的大型现代化综合性医院'),
('北京大学第一医院', 'BJDYH003', '北京市', '北京市', '西城区', '西城区西什库大街8号', '010-83572211', '三级甲等', 1, '北京大学第一医院是中央保健基地医院，是一所融医疗、教学、科研、预防为一体的大型综合性医院');


-- ===================================================
-- 3. 科室表（department）
-- 存储医院科室信息
-- ===================================================
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
    `department_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '科室ID，主键',
    `hospital_id` BIGINT NOT NULL COMMENT '医院ID，关联hospital表',
    `department_name` VARCHAR(50) NOT NULL COMMENT '科室名称',
    `department_code` VARCHAR(50) NOT NULL COMMENT '科室编码',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父科室ID，0表示一级科室',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '科室描述',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '科室位置（楼层/房间号）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-停用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`department_id`),
    UNIQUE KEY `uk_hospital_department_code` (`hospital_id`, `department_code`),
    KEY `idx_hospital_id` (`hospital_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_department_hospital` FOREIGN KEY (`hospital_id`) REFERENCES `hospital` (`hospital_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室表';

-- 插入测试科室数据
INSERT INTO `department` (`hospital_id`, `department_name`, `department_code`, `parent_id`, `description`, `location`) VALUES
-- 北京协和医院科室
(1, '内科', 'NEIKE001', 0, '内科系统疾病诊疗', '门诊楼3层'),
(1, '外科', 'WAIKE001', 0, '外科系统疾病诊疗', '门诊楼4层'),
(1, '心血管内科', 'XXGNK001', 1, '心血管疾病诊疗', '门诊楼3层A区'),
(1, '消化内科', 'XHNK001', 1, '消化系统疾病诊疗', '门诊楼3层B区'),
(1, '骨科', 'GUKE001', 2, '骨科疾病诊疗', '门诊楼4层A区'),
-- 北京301医院科室
(2, '内科', 'NEIKE002', 0, '内科系统疾病诊疗', '门诊楼2层'),
(2, '神经内科', 'SJNK002', 1, '神经系统疾病诊疗', '门诊楼2层A区'),
(2, '皮肤科', 'PFK002', 0, '皮肤病诊疗', '门诊楼5层'),
-- 北京大学第一医院科室
(3, '儿科', 'ERKE003', 0, '儿童疾病诊疗', '门诊楼1层'),
(3, '妇产科', 'FCK003', 0, '妇产科疾病诊疗', '门诊楼2层');


-- ===================================================
-- 4. 医生表（doctor）
-- 存储医生信息
-- ===================================================
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor` (
    `doctor_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '医生ID，主键',
    `hospital_id` BIGINT NOT NULL COMMENT '医院ID，关联hospital表',
    `department_id` BIGINT NOT NULL COMMENT '科室ID，关联department表',
    `doctor_name` VARCHAR(50) NOT NULL COMMENT '医生姓名',
    `doctor_code` VARCHAR(50) NOT NULL COMMENT '医生编码',
    `title` VARCHAR(50) NOT NULL COMMENT '职称：主任医师/副主任医师/主治医师/住院医师',
    `specialty` VARCHAR(500) DEFAULT NULL COMMENT '擅长领域',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '医生简介',
    `avatar` VARCHAR(200) DEFAULT NULL COMMENT '头像URL',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '挂号费（元）',
    `experience` INT DEFAULT 0 COMMENT '从业年限',
    `education` VARCHAR(50) DEFAULT NULL COMMENT '学历',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常出诊 0-停诊',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`doctor_id`),
    UNIQUE KEY `uk_doctor_code` (`doctor_code`),
    KEY `idx_hospital_id` (`hospital_id`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_doctor_hospital` FOREIGN KEY (`hospital_id`) REFERENCES `hospital` (`hospital_id`),
    CONSTRAINT `fk_doctor_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生表';

-- 插入测试医生数据
INSERT INTO `doctor` (`hospital_id`, `department_id`, `doctor_name`, `doctor_code`, `title`, `specialty`, `description`, `price`, `experience`, `education`) VALUES
-- 北京协和医院医生
(1, 3, '张建国', 'DOC001', '主任医师', '冠心病、高血压、心律失常等心血管疾病', '从事心血管内科临床工作30余年，经验丰富', 100.00, 30, '博士'),
(1, 3, '李明', 'DOC002', '副主任医师', '冠心病、心肌病、心力衰竭', '从事心血管内科临床工作20年，擅长复杂冠心病介入治疗', 80.00, 20, '硕士'),
(1, 4, '王芳', 'DOC003', '主任医师', '胃食管反流病、幽门螺杆菌感染、消化道肿瘤', '从事消化内科临床工作25年，在幽门螺杆菌感染诊疗方面有丰富经验', 90.00, 25, '博士'),
(1, 5, '刘强', 'DOC004', '副主任医师', '脊柱外科、关节外科、创伤骨科', '从事骨科临床工作18年，擅长微创手术', 70.00, 18, '硕士'),
-- 北京301医院医生
(2, 7, '赵伟', 'DOC005', '主任医师', '脑血管病、癫痫、神经系统感染', '从事神经内科临床工作35年，在脑血管病救治方面经验丰富', 120.00, 35, '博士'),
(2, 7, '陈静', 'DOC006', '副主任医师', '头痛、眩晕、睡眠障碍', '从事神经内科临床工作15年，擅长疑难杂症诊治', 60.00, 15, '硕士'),
(2, 8, '孙丽', 'DOC007', '主任医师', '湿疹、银屑病、性病', '从事皮肤科临床工作22年，对疑难皮肤病有深入研究', 80.00, 22, '博士'),
-- 北京大学第一医院医生
(3, 9, '周涛', 'DOC008', '主任医师', '小儿呼吸系统疾病、哮喘', '从事儿科临床工作28年，在小儿哮喘防治方面有丰富经验', 90.00, 28, '博士'),
(3, 9, '吴敏', 'DOC009', '副主任医师', '小儿消化系统疾病、营养障碍', '从事儿科临床工作12年，擅长儿童营养指导', 50.00, 12, '硕士'),
(3, 10, '郑华', 'DOC010', '主任医师', '高危妊娠、产前诊断、妇科肿瘤', '从事妇产科临床工作26年，处理产科危重病人经验丰富', 100.00, 26, '博士');


-- ===================================================
-- 5. 排班表（schedule）
-- 存储医生排班信息和号源库存（核心表）
-- ===================================================
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
    `schedule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班ID，主键',
    `doctor_id` BIGINT NOT NULL COMMENT '医生ID，关联doctor表',
    `hospital_id` BIGINT NOT NULL COMMENT '医院ID（冗余字段，方便查询）',
    `department_id` BIGINT NOT NULL COMMENT '科室ID（冗余字段，方便查询）',
    `work_date` DATE NOT NULL COMMENT '排班日期',
    `time_slot` TINYINT NOT NULL COMMENT '时段：1-上午 2-下午 3-晚上',
    `total_count` INT NOT NULL COMMENT '总号源数',
    `available_count` INT NOT NULL COMMENT '剩余号源数',
    `price` DECIMAL(10,2) NOT NULL COMMENT '挂号费（元）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-未发布 1-已发布 2-已约满 3-已停诊',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`schedule_id`),
    UNIQUE KEY `uk_doctor_date_slot` (`doctor_id`, `work_date`, `time_slot`),
    KEY `idx_hospital_id` (`hospital_id`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_work_date` (`work_date`),
    KEY `idx_status` (`status`),
    KEY `idx_available_count` (`available_count`),
    CONSTRAINT `fk_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生排班表（含号源库存）';

-- 插入测试排班数据（未来7天）
INSERT INTO `schedule` (`doctor_id`, `hospital_id`, `department_id`, `work_date`, `time_slot`, `total_count`, `available_count`, `price`, `status`) VALUES
-- 北京协和医院 张建国 未来7天上午
(1, 1, 3, CURDATE() + INTERVAL 1 DAY, 1, 20, 20, 100.00, 1),
(1, 1, 3, CURDATE() + INTERVAL 2 DAY, 1, 20, 20, 100.00, 1),
(1, 1, 3, CURDATE() + INTERVAL 3 DAY, 1, 20, 20, 100.00, 1),
(1, 1, 3, CURDATE() + INTERVAL 4 DAY, 1, 20, 20, 100.00, 1),
(1, 1, 3, CURDATE() + INTERVAL 5 DAY, 1, 20, 20, 100.00, 1),
-- 北京协和医院 张建国 未来7天下午
(1, 1, 3, CURDATE() + INTERVAL 1 DAY, 2, 15, 15, 100.00, 1),
(1, 1, 3, CURDATE() + INTERVAL 2 DAY, 2, 15, 15, 100.00, 1),

-- 北京协和医院 李明 未来7天上午
(2, 1, 3, CURDATE() + INTERVAL 1 DAY, 1, 20, 20, 80.00, 1),
(2, 1, 3, CURDATE() + INTERVAL 3 DAY, 1, 20, 20, 80.00, 1),
(2, 1, 3, CURDATE() + INTERVAL 5 DAY, 1, 20, 20, 80.00, 1),

-- 北京协和医院 王芳 未来7天上午
(3, 1, 4, CURDATE() + INTERVAL 2 DAY, 1, 20, 20, 90.00, 1),
(3, 1, 4, CURDATE() + INTERVAL 4 DAY, 1, 20, 20, 90.00, 1),
(3, 1, 4, CURDATE() + INTERVAL 6 DAY, 1, 20, 20, 90.00, 1),

-- 北京301医院 赵伟 未来7天
(5, 2, 7, CURDATE() + INTERVAL 1 DAY, 1, 15, 15, 120.00, 1),
(5, 2, 7, CURDATE() + INTERVAL 3 DAY, 1, 15, 15, 120.00, 1),
(5, 2, 7, CURDATE() + INTERVAL 5 DAY, 1, 15, 15, 120.00, 1),
(5, 2, 7, CURDATE() + INTERVAL 1 DAY, 2, 10, 10, 120.00, 1),

-- 北京大学第一医院 周涛 未来7天
(8, 3, 9, CURDATE() + INTERVAL 1 DAY, 1, 20, 20, 90.00, 1),
(8, 3, 9, CURDATE() + INTERVAL 2 DAY, 1, 20, 20, 90.00, 1),
(8, 3, 9, CURDATE() + INTERVAL 3 DAY, 1, 20, 20, 90.00, 1);

-- 创建排班数据查询视图
CREATE OR REPLACE VIEW `v_schedule_detail` AS
SELECT
    s.schedule_id,
    s.doctor_id,
    s.hospital_id,
    s.department_id,
    s.work_date,
    s.time_slot,
    s.total_count,
    s.available_count,
    s.price,
    s.status,
    s.created_time,
    h.hospital_name,
    h.address AS hospital_address,
    h.phone AS hospital_phone,
    d.department_name,
    doc.doctor_name,
    doc.title AS doctor_title,
    doc.specialty,
    doc.avatar AS doctor_avatar,
    doc.price AS doctor_price
FROM `schedule` s
LEFT JOIN `hospital` h ON s.hospital_id = h.hospital_id
LEFT JOIN `department` d ON s.department_id = d.department_id
LEFT JOIN `doctor` doc ON s.doctor_id = doc.doctor_id
WHERE s.deleted = 0;


-- ===================================================
-- 6. 预约表（appointment）
-- 存储用户预约记录（核心表，唯一索引防重复预约）
-- ===================================================
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment` (
    `appointment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID，主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user表',
    `schedule_id` BIGINT NOT NULL COMMENT '排班ID，关联schedule表',
    `doctor_id` BIGINT NOT NULL COMMENT '医生ID（冗余字段）',
    `hospital_id` BIGINT NOT NULL COMMENT '医院ID（冗余字段）',
    `department_id` BIGINT NOT NULL COMMENT '科室ID（冗余字段）',
    `work_date` DATE NOT NULL COMMENT '预约日期',
    `time_slot` TINYINT NOT NULL COMMENT '时段：1-上午 2-下午 3-晚上',
    `patient_name` VARCHAR(50) NOT NULL COMMENT '就诊人姓名',
    `patient_phone` VARCHAR(11) NOT NULL COMMENT '就诊人手机号',
    `patient_id_card` VARCHAR(18) NOT NULL COMMENT '就诊人身份证号',
    `id_card_type` TINYINT NOT NULL DEFAULT 1 COMMENT '证件类型：1-身份证 2-护照 3-港澳通行证',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待就诊 2-已就诊 3-已取消 4-已过期 5-已爽约',
    `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
    `visit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '就诊状态：0-未就诊 1-已就诊',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`appointment_id`),
    -- 【关键】唯一索引：防止同一用户重复预约同一号源
    UNIQUE KEY `uk_user_schedule` (`user_id`, `schedule_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_schedule_id` (`schedule_id`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_work_date` (`work_date`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`),
    CONSTRAINT `fk_appointment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
    CONSTRAINT `fk_appointment_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表（唯一索引防止重复预约）';

-- 插入测试预约数据
INSERT INTO `appointment` (`user_id`, `schedule_id`, `doctor_id`, `hospital_id`, `department_id`, `work_date`, `time_slot`, `patient_name`, `patient_phone`, `patient_id_card`) VALUES
(1, 1, 1, 1, 3, CURDATE() + INTERVAL 1 DAY, 1, '张三', '13800138001', '110101199001011234'),
(2, 5, 3, 1, 4, CURDATE() + INTERVAL 1 DAY, 1, '李四', '13800138002', '110101199102022345');


-- ===================================================
-- 7. 预约订单表（appointment_order）
-- 存储预约订单信息
-- ===================================================
DROP TABLE IF EXISTS `appointment_order`;
CREATE TABLE `appointment_order` (
    `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID，主键',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号（全局唯一）',
    `appointment_id` BIGINT NOT NULL COMMENT '预约ID，关联appointment表',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user表',
    `doctor_id` BIGINT NOT NULL COMMENT '医生ID（冗余字段）',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额（元）',
    `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付 1-已支付 2-已取消 3-已退款',
    `pay_type` TINYINT DEFAULT NULL COMMENT '支付方式：1-微信 2-支付宝 3-医保',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `order_status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态：1-待支付 2-已完成 3-已取消',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`order_id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    UNIQUE KEY `uk_appointment_id` (`appointment_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_created_time` (`created_time`),
    CONSTRAINT `fk_order_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约订单表';

-- 插入测试订单数据
INSERT INTO `appointment_order` (`order_no`, `appointment_id`, `user_id`, `doctor_id`, `amount`, `pay_status`, `pay_type`, `pay_time`, `order_status`) VALUES
('ORD202401010001', 1, 1, 1, 100.00, 1, 1, NOW(), 2),
('ORD202401010002', 2, 2, 3, 90.00, 1, 2, NOW(), 2);


-- ===================================================
-- 8. 创建数据库初始化完成标记
-- ===================================================
SELECT '数据库初始化完成！共创建8张表：user、hospital、department、doctor、schedule、appointment、appointment_order、视图' AS message;
