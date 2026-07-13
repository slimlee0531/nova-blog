package com.slim.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 点赞实体类
 * 对应数据库 likes 表
 * 记录用户对文章和评论的点赞
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "likes", indexes = {
    @Index(name = "idx_user", columnList = "user_id"),
    @Index(name = "idx_likeable", columnList = "likeable_type, likeable_id")
})
public class Like {

    /**
     * 点赞ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户ID（登录用户）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * IP地址（未登录用户）
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * 浏览器指纹（未登录用户）
     */
    @Column(name = "fingerprint", length = 255)
    private String fingerprint;

    /**
     * 点赞类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "likeable_type", nullable = false, length = 20)
    private LikeableType likeableType;

    /**
     * 点赞对象ID
     */
    @Column(name = "likeable_id", nullable = false)
    private Long likeableId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 点赞类型枚举
     */
    public enum LikeableType {
        ARTICLE, COMMENT
    }
}
