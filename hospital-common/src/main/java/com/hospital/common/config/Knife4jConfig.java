package com.hospital.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j接口文档配置
 *
 * @author hospital
 * @date 2024-01-15
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("医院预约挂号系统API文档")
                        .description("医院在线预约挂号平台后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("hospital")
                                .email("hospital@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://github.com/hospital")))
                ;
    }
}
