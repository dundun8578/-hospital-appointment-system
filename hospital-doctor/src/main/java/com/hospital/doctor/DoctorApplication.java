package com.hospital.doctor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 医生模块启动类
 *
 * @author hospital
 * @date 2024-01-15
 */
@SpringBootApplication
@EnableScheduling
public class DoctorApplication {
    public static void main(String[] args) {
        SpringApplication.run(DoctorApplication.class, args);
        System.out.println("========================================");
        System.out.println("  医院医生排班模块启动成功！");
        System.out.println("========================================");
    }
}
