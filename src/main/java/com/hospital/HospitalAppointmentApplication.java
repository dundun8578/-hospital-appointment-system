package com.hospital;

import com.hospital.common.GlobalExceptionHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 医院预约挂号系统启动类
 *
 * @author hospital
 * @date 2024-01-15
 */
@SpringBootApplication(scanBasePackages = "com.hospital")
@MapperScan("com.hospital.*.mapper")
public class HospitalAppointmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalAppointmentApplication.class, args);
        System.out.println("========================================");
        System.out.println("  医院预约挂号系统启动成功！");
        System.out.println("  接口文档地址: http://localhost:8080/api/doc.html");
        System.out.println("========================================");
    }

    /**
     * 密码编码器Bean
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
