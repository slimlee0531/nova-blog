package com.slim.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 知识库文档实体类
 * 对应数据库 knowledge_base_documents 表
 * 存储知识库中的文档分块
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_base_documents", indexes = {
    @Index(name = "idx_config", columnList = "config_id"),
    @Index(name = "idx_source", columnList = "source_type, source_id"),
    @Index(name = "idx_content_hash", columnList = "content_hash"),
    @Index(name = "idx_status", columnList = "status")
})
public class KnowledgeBaseDocument {

    /**
     * 文档ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ==================== 文档内容 ====================

    /**
     * 文档内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 内容哈希（用于去重）
     */
    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    // ==================== 元数据 ====================

    /**
     * 元数据（标题、作者、标签等，JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Object metadata;

    /**
     * 分块索引
     */
    @Column(name = "chunk_index")
    private Integer chunkIndex;

    /**
     * token数量
     */
    @Column(name = "token_count")
    private Integer tokenCount;

    // ==================== 向量嵌入 ====================

    /**
     * 向量嵌入（JSON数组格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "embedding", columnDefinition = "JSON")
    private Object embedding;

    /**
     * 使用的嵌入模型
     */
    @Column(name = "embedding_model", length = 100)
    private String embeddingModel;

    // ==================== 来源 ====================

    /**
     * 来源类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    /**
     * 来源ID
     */
    @Column(name = "source_id")
    private Long sourceId;

    // ==================== 状态 ====================

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.ACTIVE;

    // ==================== 关联 ====================

    /**
     * 知识库配置
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false)
    private KnowledgeBaseConfig config;

    // ==================== 时间字段 ====================

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
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
