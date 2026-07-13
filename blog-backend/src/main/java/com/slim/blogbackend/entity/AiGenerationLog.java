package com.slim.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI生成日志实体类
 * 对应数据库 ai_generation_logs 表
 * 记录所有AI辅助创作的操作日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_generation_logs", indexes = {
    @Index(name = "idx_user", columnList = "user_id"),
    @Index(name = "idx_article", columnList = "article_id"),
    @Index(name = "idx_operation", columnList = "operation_type"),
    @Index(name = "idx_created", columnList = "created_at")
})
public class AiGenerationLog {

    /**
     * 日志ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ==================== 操作信息 ====================

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 30)
    private OperationType operationType;

    /**
     * 输入数据（如文章内容、提示词等，JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "input_data", columnDefinition = "JSON")
    private Object inputData;

    /**
     * 输出结果（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "output_data", columnDefinition = "JSON")
    private Object outputData;

    // ==================== AI模型信息 ====================

    /**
     * AI模型名称
     */
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    /**
     * 模型版本
     */
    @Column(name = "model_version", length = 50)
    private String modelVersion;

    /**
     * 使用的提示词模板
     */
    @Column(name = "prompt_template", columnDefinition = "TEXT")
    private String promptTemplate;

    // ==================== 性能指标 ====================

    /**
     * 消耗的token数量
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed;

    /**
     * 处理时间（毫秒）
     */
    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    /**
     * 成本（美元）
     */
    @Column(name = "cost_usd", precision = 10, scale = 6)
    private BigDecimal costUsd;

    // ==================== 关联 ====================

    /**
     * 触发操作的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 关联的文章
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    // ==================== 状态 ====================

    /**
     * 执行状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.SUCCESS;

    /**
     * 错误信息（如果失败）
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // ==================== 时间字段 ====================

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
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
