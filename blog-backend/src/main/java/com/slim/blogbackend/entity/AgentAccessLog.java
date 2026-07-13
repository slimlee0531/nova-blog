package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent访问日志实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("agent_access_logs")
public class AgentAccessLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== Agent信息 ====================

    private Long agentApiKeyId;

    private String agentName;

    private String agentIp;

    private String userAgent;

    // ==================== 请求信息 ====================

    private String endpoint;

    private String method;

    private String queryParams;

    private String requestBody;

    // ==================== 响应信息 ====================

    private Integer responseStatus;

    private Integer responseTimeMs;

    private Integer responseSizeBytes;

    // ==================== 内容访问 ====================

    private String resourceType;

    private Long resourceId;

    // ==================== 分析 ====================

    private String purpose;

    // ==================== 时间字段 ====================

    @TableField(fill = FieldFill.INSERT)
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
