package com.hospital.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单服务启动类
 * 监听预约队列异步创建订单
 */
@SpringBootApplication(scanBasePackages = {"com.hospital.common", "com.hospital.order", "com.hospital.doctor.service", "com.hospital.user.service"})
@org.mybatis.spring.annotation.MapperScan({"com.hospital.order.mapper", "com.hospital.doctor.mapper", "com.hospital.user.mapper"})
@org.springframework.context.annotation.Import(com.hospital.appointment.config.RabbitMQConfig.class)
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        System.out.println("========================================");
        System.out.println("  订单模块启动成功！");
        System.out.println("========================================");
    }
}
