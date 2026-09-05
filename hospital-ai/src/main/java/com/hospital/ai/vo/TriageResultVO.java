package com.hospital.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * AI导诊结果
 */
@Data
public class TriageResultVO {

    /**
     * 推荐科室列表
     */
    private List<DepartmentRecommendation> recommendations;

    /**
     * 就诊建议
     */
    private String advice;

    /**
     * 免责声明
     */
    private String disclaimer;

    /**
     * 是否AI降级结果（LLM不可用时为true）
     */
    private Boolean fallback;

    @Data
    public static class DepartmentRecommendation {

        private Long departmentId;

        private String departmentName;

        /**
         * 推荐理由
         */
        private String reason;

        /**
         * 紧急程度：常规 / 尽快 / 紧急
         */
        private String urgency;
    }
}
