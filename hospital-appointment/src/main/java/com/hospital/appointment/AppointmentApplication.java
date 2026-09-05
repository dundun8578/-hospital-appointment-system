package com.hospital.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 预约模块启动类
 *
 * @author hospital
 * @date 2024-01-15
 */
@SpringBootApplication(scanBasePackages = {"com.hospital.common", "com.hospital.appointment"})
public class AppointmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppointmentApplication.class, args);
        System.out.println("========================================");
        System.out.println("  预约挂号模块启动成功！");
        System.out.println("========================================");
    }
}
