package com.hospital.ai.controller;

import com.hospital.ai.service.TriageService;
import com.hospital.ai.vo.TriageResultVO;
import com.hospital.common.Result;
import com.hospital.common.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI智能导诊控制器
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "AI智能导诊")
public class TriageController {

    @Autowired
    private TriageService triageService;

    @Operation(summary = "症状导诊", description = "根据症状描述推荐就诊科室（AI生成，仅供参考）")
    @RateLimit(keyPrefix = "ai:triage", timeWindow = 60, maxRequests = 10, message = "导诊请求过于频繁，请稍后再试")
    @PostMapping("/triage")
    public Result<TriageResultVO> triage(@RequestBody Map<String, String> body) {
        String symptoms = body.getOrDefault("symptoms", "");
        if (symptoms.trim().isEmpty()) {
            return Result.error(400, "症状描述不能为空");
        }
        return Result.success(triageService.triage(symptoms.trim()));
    }
}
