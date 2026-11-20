package com.hospital.ai;

import com.hospital.doctor.mapper.DepartmentMapper;
import com.hospital.doctor.service.impl.DepartmentServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * AI智能导诊服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.hospital.common", "com.hospital.ai"})
// 复用科室服务及其 Mapper 依赖，保持医生模块的 Web 接口和定时任务独立。
@Import(DepartmentServiceImpl.class)
@MapperScan(basePackageClasses = DepartmentMapper.class)
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
        System.out.println("========================================");
        System.out.println("  AI智能导诊服务启动成功！");
        System.out.println("========================================");
    }
}
