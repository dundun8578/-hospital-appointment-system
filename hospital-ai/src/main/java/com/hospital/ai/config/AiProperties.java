package com.hospital.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI导诊配置（OpenAI兼容协议，DeepSeek/智谱/通义/Kimi等均可）
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * AI总开关：关闭时导诊接口返回降级提示，不影响系统其他功能
     */
    private boolean enabled = false;

    /**
     * 对话模型 Base URL（OpenAI兼容，不含/v1）
     */
    private String baseUrl;

    private String apiKey;

    private String model;

    /**
     * LLM接口超时（毫秒）
     */
    private long timeoutMs = 30000;
}
