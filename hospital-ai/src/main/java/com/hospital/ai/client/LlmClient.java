package com.hospital.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.ai.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI兼容协议LLM客户端
 * 调用失败返回null由调用方降级，AI故障不影响挂号主流程
 */
@Slf4j
@Component
public class LlmClient {

    private final AiProperties properties;
    private final ObjectMapper objectMapper;

    private RestTemplate restTemplate;

    public LlmClient(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getTimeoutMs());
        factory.setReadTimeout((int) properties.getTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 对话补全：返回content文本，失败返回null
     */
    public String chat(String systemPrompt, String userPrompt) {
        if (!properties.isEnabled() || properties.getApiKey() == null) {
            return null;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", properties.getModel());
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            body.put("temperature", 0.2);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + properties.getApiKey());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String response = restTemplate.postForObject(
                    properties.getBaseUrl() + "/chat/completions", entity, String.class);
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").path(0).path("message").path("content").asText(null);
        } catch (Exception e) {
            log.warn("LLM对话调用失败: {}", e.getMessage());
            return null;
        }
    }
}
