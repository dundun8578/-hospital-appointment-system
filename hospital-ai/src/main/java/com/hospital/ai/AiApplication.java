package com.hospital.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI智能导诊服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.hospital.common", "com.hospital.ai"})
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
        System.out.println("========================================");
        System.out.println("  AI智能导诊服务启动成功！");
        System.out.println("========================================");
    }
}
