package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库文档实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_base_documents")
public class KnowledgeBaseDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 文档内容 ====================

    private String content;

    private String contentHash;

    // ==================== 元数据 ====================

    private String metadata;

    private Integer chunkIndex;

    private Integer tokenCount;

    // ==================== 向量嵌入 ====================

    private String embedding;

    private String embeddingModel;

    // ==================== 来源 ====================

    private String sourceType;

    private Long sourceId;

    // ==================== 状态 ====================

    private String status;

    // ==================== 关联 ====================

    private Long configId;

    // ==================== 时间字段 ====================

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 来源类型枚举
     */
    public enum SourceType {
        ARTICLE, COMMENT, CUSTOM
    }

    /**
     * 文档状态枚举
     */
    public enum DocumentStatus {
        ACTIVE, DELETED, UPDATED
    }
}
