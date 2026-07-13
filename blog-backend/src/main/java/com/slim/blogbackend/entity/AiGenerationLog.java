package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI生成日志实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_generation_logs")
public class AiGenerationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 操作信息 ====================

    private String operationType;

    private String inputData;

    private String outputData;

    // ==================== AI模型信息 ====================

    private String modelName;

    private String modelVersion;

    private String promptTemplate;

    // ==================== 性能指标 ====================

    private Integer tokensUsed;

    private Integer processingTimeMs;

    private BigDecimal costUsd;

    // ==================== 关联 ====================

    private Long userId;

    private Long articleId;

    // ==================== 状态 ====================

    private String status;

    private String errorMessage;

    // ==================== 时间字段 ====================

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        SUMMARY_GENERATION,
        KEYWORD_EXTRACTION,
        TAG_SUGGESTION,
        CATEGORY_SUGGESTION,
        CONTENT_IMPROVEMENT,
        TITLE_SUGGESTION,
        CONTENT_EXPANSION,
        GRAMMAR_CHECK,
        READABILITY_ANALYSIS,
        ENTITY_EXTRACTION,
        STRUCTURED_DATA_GENERATION,
        TRANSLATION,
        CUSTOM_PROMPT
    }

    /**
     * 执行状态枚举
     */
    public enum ExecutionStatus {
        SUCCESS, FAILED, TIMEOUT, CANCELLED
    }
}
