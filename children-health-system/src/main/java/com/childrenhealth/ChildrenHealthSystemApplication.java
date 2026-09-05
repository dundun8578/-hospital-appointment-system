package com.childrenhealth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 儿童健康帮扶系统启动类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.childrenhealth.mapper")
public class ChildrenHealthSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChildrenHealthSystemApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("儿童健康帮扶系统启动成功！");
        System.out.println("访问地址：http://localhost:12306");
        System.out.println("注册接口：POST http://localhost:12306/api/auth/register");
        System.out.println("登录接口：POST http://localhost:12306/api/auth/login");
        System.out.println("========================================\n");
    }
}
