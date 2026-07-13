package com.slim.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 知识库配置实体类
 * 对应数据库 knowledge_base_configs 表
 * 配置博客作为AI知识库的参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_base_configs", indexes = {
    @Index(name = "idx_user", columnList = "user_id"),
    @Index(name = "idx_active", columnList = "is_active")
})
public class KnowledgeBaseConfig {

    /**
     * 配置ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ==================== 配置信息 ====================

    /**
     * 知识库名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 知识库描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ==================== 内容范围 ====================

    /**
     * 包含的分类ID列表（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "include_categories", columnDefinition = "JSON")
    private java.util.List<Long> includeCategories;

    /**
     * 包含的标签ID列表（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "include_tags", columnDefinition = "JSON")
    private java.util.List<Long> includeTags;

    /**
     * 包含的文章状态（JSON格式，默认PUBLISHED）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "include_statuses", columnDefinition = "JSON")
    private java.util.List<String> includeStatuses;

    /**
     * 开始日期
     */
    @Column(name = "date_range_start")
    private LocalDate dateRangeStart;

    /**
     * 结束日期
     */
    @Column(name = "date_range_end")
    private LocalDate dateRangeEnd;

    // ==================== 处理选项 ====================

    /**
     * 文本分块大小（字符数）
     */
    @Column(name = "chunk_size", nullable = false)
    @Builder.Default
    private Integer chunkSize = 1000;

    /**
     * 分块重叠大小
     */
    @Column(name = "chunk_overlap", nullable = false)
    @Builder.Default
    private Integer chunkOverlap = 200;

    /**
     * 是否包含元数据
     */
    @Column(name = "include_metadata", nullable = false)
    @Builder.Default
    private Boolean includeMetadata = true;

    /**
     * 是否包含评论
     */
    @Column(name = "include_comments", nullable = false)
    @Builder.Default
    private Boolean includeComments = false;

    // ==================== 向量化选项 ====================

    /**
     * 嵌入模型
     */
    @Column(name = "embedding_model", nullable = false, length = 100)
    @Builder.Default
    private String embeddingModel = "text-embedding-ada-002";

    /**
     * 嵌入维度
     */
    @Column(name = "embedding_dimensions", nullable = false)
    @Builder.Default
    private Integer embeddingDimensions = 1536;

    // ==================== 访问控制 ====================

    /**
     * 访问级别
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 20)
    @Builder.Default
    private AccessLevel accessLevel = AccessLevel.PRIVATE;

    /**
     * 允许访问的Agent列表（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "allowed_agents", columnDefinition = "JSON")
    private java.util.List<String> allowedAgents;

    // ==================== 状态 ====================

    /**
     * 是否激活
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 最后同步时间
     */
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    /**
     * 文档总数
     */
    @Column(name = "total_documents", nullable = false)
    @Builder.Default
    private Long totalDocuments = 0L;

    // ==================== 关联 ====================

    /**
     * 创建者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
     * 访问级别枚举
     */
    public enum AccessLevel {
        PUBLIC, PRIVATE, RESTRICTED
    }
}
