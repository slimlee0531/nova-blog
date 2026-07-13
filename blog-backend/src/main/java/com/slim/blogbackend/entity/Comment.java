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
import java.util.ArrayList;
import java.util.List;

/**
 * 评论实体类
 * 对应数据库 comments 表
 * 支持人类和AI Agent的评论
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_article", columnList = "article_id"),
    @Index(name = "idx_user", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_parent", columnList = "parent_id")
})
public class Comment {

    /**
     * 评论ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 评论内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // ==================== 评论者信息 ====================

    /**
     * 用户ID（如果是登录用户）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 访客名称（未登录用户）
     */
    @Column(name = "guest_name", length = 100)
    private String guestName;

    /**
     * 访客邮箱（未登录用户）
     */
    @Column(name = "guest_email", length = 100)
    private String guestEmail;

    /**
     * 访客网站（未登录用户）
     */
    @Column(name = "guest_website", length = 200)
    private String guestWebsite;

    /**
     * 访客IP地址
     */
    @Column(name = "guest_ip", length = 45)
    private String guestIp;

    /**
     * 访客User-Agent
     */
    @Column(name = "guest_user_agent", length = 500)
    private String guestUserAgent;

    // ==================== Agent评论标识 ====================

    /**
     * 是否为Agent评论
     */
    @Column(name = "is_agent_comment", nullable = false)
    @Builder.Default
    private Boolean isAgentComment = false;

    /**
     * Agent标识（如：openai-gpt4, claude-3）
     */
    @Column(name = "agent_id", length = 100)
    private String agentId;

    /**
     * Agent元数据（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "agent_metadata", columnDefinition = "JSON")
    private Object agentMetadata;

    // ==================== 评论关系 ====================

    /**
     * 文章
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    /**
     * 父评论（支持多级评论）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /**
     * 子评论列表
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> children = new ArrayList<>();

    // ==================== 状态 ====================

    /**
     * 审核状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CommentStatus status = CommentStatus.PENDING;

    // ==================== 统计 ====================

    /**
     * 点赞数量
     */
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

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
     * 评论状态枚举
     */
    public enum CommentStatus {
        PENDING, APPROVED, REJECTED, SPAM
    }
}
