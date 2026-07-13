package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent API密钥实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("agent_api_keys")
public class AgentApiKey {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== Agent信息 ====================

    private String agentName;

    private String agentDescription;

    private String agentType;

    // ==================== API密钥 ====================

    private String apiKeyHash;

    private String apiKeyPrefix;

    // ==================== 权限控制 ====================

    private String permissions;

    private Integer rateLimit;

    private Integer dailyLimit;

    // ==================== 状态 ====================

    private Boolean isActive;

    private LocalDateTime expiresAt;

    private LocalDateTime lastUsedAt;

    private Long totalRequests;

    // ==================== 关联 ====================

    private Long userId;

    // ==================== 时间字段 ====================

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * Agent类型枚举
     */
    public enum AgentType {
        AI_MODEL, SEARCH_ENGINE, CONTENT_AGGREGATOR, CUSTOM
    }
}
