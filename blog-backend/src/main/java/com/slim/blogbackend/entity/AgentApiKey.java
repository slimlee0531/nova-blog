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
 * Agent API密钥实体类
 * 对应数据库 agent_api_keys 表
 * 管理AI Agent的API访问权限
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "agent_api_keys", indexes = {
    @Index(name = "idx_user", columnList = "user_id"),
    @Index(name = "idx_agent_name", columnList = "agent_name")
})
public class AgentApiKey {

    /**
     * 密钥ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ==================== Agent信息 ====================

    /**
     * Agent名称
     */
    @Column(name = "agent_name", nullable = false, length = 100)
    private String agentName;

    /**
     * Agent描述
     */
    @Column(name = "agent_description", columnDefinition = "TEXT")
    private String agentDescription;

    /**
     * Agent类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false, length = 20)
    private AgentType agentType;

    // ==================== API密钥 ====================

    /**
     * API密钥哈希
     */
    @Column(name = "api_key_hash", nullable = false, unique = true)
    private String apiKeyHash;

    /**
     * API密钥前缀（用于显示）
     */
    @Column(name = "api_key_prefix", nullable = false, length = 10)
    private String apiKeyPrefix;

    // ==================== 权限控制 ====================

    /**
     * 权限列表（JSON格式，如：read, write, analyze）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "permissions", nullable = false, columnDefinition = "JSON")
    private java.util.List<String> permissions;

    /**
     * 每分钟请求限制
     */
    @Column(name = "rate_limit", nullable = false)
    @Builder.Default
    private Integer rateLimit = 100;

    /**
     * 每日请求限制
     */
    @Column(name = "daily_limit", nullable = false)
    @Builder.Default
    private Integer dailyLimit = 10000;

    // ==================== 状态 ====================

    /**
     * 是否激活
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 过期时间
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 最后使用时间
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * 总请求次数
     */
    @Column(name = "total_requests", nullable = false)
    @Builder.Default
    private Long totalRequests = 0L;

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
     * Agent类型枚举
     */
    public enum AgentType {
        AI_MODEL, SEARCH_ENGINE, CONTENT_AGGREGATOR, CUSTOM
    }
}
