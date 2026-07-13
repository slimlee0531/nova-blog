package com.slim.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 对应数据库 system_configs 表
 * 存储系统配置项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_configs")
public class SystemConfig {

    /**
     * 配置ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 配置键
     */
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    /**
     * 配置值
     */
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    /**
     * 配置类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false, length = 20)
    @Builder.Default
    private ConfigType configType = ConfigType.STRING;

    /**
     * 配置描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 是否公开（前端可访问）
     */
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

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
     * 配置类型枚举
     */
    public enum ConfigType {
        STRING, NUMBER, BOOLEAN, JSON
    }
}
