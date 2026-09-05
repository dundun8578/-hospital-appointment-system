package com.hospital.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.ai.client.LlmClient;
import com.hospital.ai.vo.TriageResultVO;
import com.hospital.doctor.entity.Department;
import com.hospital.doctor.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI智能导诊
 * 把系统真实科室列表注入Prompt做grounding，让LLM只从实际开设的科室中推荐，
 * 避免推荐不存在的科室导致用户无法挂号
 */
@Slf4j
@Service
public class TriageService {

    private static final String SYSTEM_PROMPT =
            "你是医院的智能导诊助手。根据患者描述的症状，从给定的科室列表中推荐最合适的就诊科室。"
                    + "只输出严格的JSON，不要任何其他文字，格式："
                    + "{\"recommendations\":[{\"departmentId\":数字,\"departmentName\":\"科室名\","
                    + "\"reason\":\"不超过40字的推荐理由\",\"urgency\":\"常规|尽快|紧急\"}],"
                    + "\"advice\":\"不超过60字的就诊建议\"}"
                    + "要求：最多推荐3个科室且必须来自给定列表； urgency根据症状严重程度判断；"
                    + "症状描述疑似急症（胸痛/呼吸困难/大出血等）时urgency必须为紧急并在advice中提示立即拨打120。";

    private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*}", Pattern.DOTALL);

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 症状导诊：LLM分析症状并推荐系统内真实科室
     * AI关闭或调用失败时返回降级结果（不抛异常）
     */
    public TriageResultVO triage(String symptoms) {
        List<Department> departments = departmentService.list();
        String departmentList = departments.stream()
                .map(d -> String.format("{\"departmentId\":%d,\"departmentName\":\"%s\"}",
                        d.getDepartmentId(), d.getDepartmentName()))
                .collect(Collectors.joining(","));

        String answer = llmClient.chat(SYSTEM_PROMPT,
                "患者症状：" + symptoms + "\n本院开设科室：[" + departmentList + "]");
        if (answer == null) {
            return buildFallback();
        }

        try {
            Matcher matcher = JSON_PATTERN.matcher(answer);
            String json = matcher.find() ? matcher.group() : answer;
            JsonNode root = objectMapper.readTree(json);

            TriageResultVO result = new TriageResultVO();
            List<TriageResultVO.DepartmentRecommendation> recommendations = new ArrayList<>();
            JsonNode recNode = root.path("recommendations");
            if (recNode.isArray()) {
                for (JsonNode node : recNode) {
                    TriageResultVO.DepartmentRecommendation rec = new TriageResultVO.DepartmentRecommendation();
                    rec.setDepartmentId(node.path("departmentId").asLong(0));
                    rec.setDepartmentName(node.path("departmentName").asText(null));
                    rec.setReason(node.path("reason").asText(""));
                    rec.setUrgency(node.path("urgency").asText("常规"));
                    // 只保留系统真实存在的科室（防LLM幻觉）
                    Department real = departments.stream()
                            .filter(d -> d.getDepartmentId().equals(rec.getDepartmentId()))
                            .findFirst().orElse(null);
                    if (rec.getDepartmentName() != null && real != null) {
                        recommendations.add(rec);
                    }
                }
            }
            if (recommendations.isEmpty()) {
                return buildFallback();
            }
            result.setRecommendations(recommendations.stream().limit(3).collect(Collectors.toList()));
            result.setAdvice(root.path("advice").asText("请及时就医，遵医嘱检查。"));
            result.setDisclaimer("AI导诊仅供参考，不构成医疗诊断建议，如有急症请立即前往急诊或拨打120。");
            result.setFallback(false);
            return result;
        } catch (Exception e) {
            log.warn("AI导诊结果解析失败: {}", answer);
            return buildFallback();
        }
    }

    private TriageResultVO buildFallback() {
        TriageResultVO result = new TriageResultVO();
        result.setRecommendations(new ArrayList<>());
        result.setAdvice("AI导诊暂时不可用，请前往门诊大厅导诊台人工咨询，或拨打医院服务电话。");
        result.setDisclaimer("如有急症请立即前往急诊或拨打120。");
        result.setFallback(true);
        return result;
    }
}
