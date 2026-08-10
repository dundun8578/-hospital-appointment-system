package com.hospital.appointment.config;

import com.hospital.doctor.config.RedisKeyConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 预约配置属性
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "appointment")
public class AppointmentConfig {

    /**
     * 分布式锁等待时间（秒）
     */
    private int lockWaitTime = 5;

    /**
     * 分布式锁自动释放时间（秒）
     */
    private int lockLeaseTime = 30;

    /**
     * Redis库存扣减Lua脚本路径
     */
    private String luaScriptPath = "lua/deduct_stock.lua";
}
