package com.slim.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

/**
 * Agent访问日志实体类
 * 对应数据库 agent_access_logs 表
 * 记录Agent对博客的访问，用于分析和审计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "agent_access_logs", indexes = {
    @Index(name = "idx_agent", columnList = "agent_api_key_id"),
    @Index(name = "idx_endpoint", columnList = "endpoint"),
    @Index(name = "idx_created", columnList = "created_at"),
    @Index(name = "idx_resource", columnList = "resource_type, resource_id")
})
public class AgentAccessLog {

    /**
     * 日志ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ==================== Agent信息 ====================

    /**
     * 使用的API密钥
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_api_key_id")
    private AgentApiKey agentApiKey;

    /**
     * Agent名称
     */
    @Column(name = "agent_name", nullable = false, length = 100)
    private String agentName;

    /**
     * Agent IP地址
     */
    @Column(name = "agent_ip", length = 45)
    private String agentIp;

    /**
     * User-Agent字符串
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // ==================== 请求信息 ====================

    /**
     * 访问的端点
     */
    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint;

    /**
     * HTTP方法
     */
    @Column(name = "method", nullable = false, length = 10)
    private String method;

    /**
     * 查询参数（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "query_params", columnDefinition = "JSON")
    private Object queryParams;

    /**
     * 请求体（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "request_body", columnDefinition = "JSON")
    private Object requestBody;

    // ==================== 响应信息 ====================

    /**
     * 响应状态码
     */
    @Column(name = "response_status")
    private Integer responseStatus;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    /**
     * 响应大小（字节）
     */
    @Column(name = "response_size_bytes")
    private Integer responseSizeBytes;

    // ==================== 内容访问 ====================

    /**
     * 资源类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 20)
    private ResourceType resourceType;

    /**
     * 资源ID
     */
    @Column(name = "resource_id")
    private Long resourceId;

    // ==================== 分析 ====================

    /**
     * 访问目的
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 20)
    private AccessPurpose purpose;

    // ==================== 时间字段 ====================

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        ARTICLE, COMMENT, MEDIA, API, OTHER
    }

    /**
     * 访问目的枚举
     */
    public enum AccessPurpose {
        CONTENT_READ,
        CONTENT_ANALYZE,
        SEARCH,
        INDEX,
        SUMMARIZE,
        COMMENT,
        OTHER
    }
}
