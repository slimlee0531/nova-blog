package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 知识库配置实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_base_configs")
public class KnowledgeBaseConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 配置信息 ====================

    private String name;

    private String description;

    // ==================== 内容范围 ====================

    private String includeCategories;

    private String includeTags;

    private String includeStatuses;

    private LocalDate dateRangeStart;

    private LocalDate dateRangeEnd;

    // ==================== 处理选项 ====================

    private Integer chunkSize;

    private Integer chunkOverlap;

    private Boolean includeMetadata;

    private Boolean includeComments;

    // ==================== 向量化选项 ====================

    private String embeddingModel;

    private Integer embeddingDimensions;

    // ==================== 访问控制 ====================

    private String accessLevel;

    private String allowedAgents;

    // ==================== 状态 ====================

    private Boolean isActive;

    private LocalDateTime lastSyncedAt;

    private Long totalDocuments;

    // ==================== 关联 ====================

    private Long userId;

    // ==================== 时间字段 ====================

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 访问级别枚举
     */
    public enum AccessLevel {
        PUBLIC, PRIVATE, RESTRICTED
    }
}
