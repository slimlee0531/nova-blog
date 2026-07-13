package com.slim.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文章实体类
 * 对应数据库 articles 表
 * 核心表，存储博客文章内容，包含AI增强字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "articles", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_author", columnList = "author_id"),
    @Index(name = "idx_category", columnList = "category_id"),
    @Index(name = "idx_published", columnList = "published_at")
})
public class Article {

    /**
     * 文章ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 文章标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * URL友好的标识
     */
    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    /**
     * 文章内容（Markdown格式）
     */
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 渲染后的HTML内容（缓存）
     */
    @Column(name = "content_html", columnDefinition = "LONGTEXT")
    private String contentHtml;

    /**
     * 文章摘要
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    // ==================== AI增强字段 ====================

    /**
     * AI生成的摘要
     */
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    /**
     * AI提取的关键词（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "ai_keywords", columnDefinition = "JSON")
    private List<String> aiKeywords;

    /**
     * AI估算的阅读时间（分钟）
     */
    @Column(name = "ai_reading_time")
    private Integer aiReadingTime;

    /**
     * AI内容质量评分（0.00-1.00）
     */
    @Column(name = "ai_content_quality_score", precision = 3, scale = 2)
    private BigDecimal aiContentQualityScore;

    /**
     * AI建议的标签（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "ai_suggested_tags", columnDefinition = "JSON")
    private List<String> aiSuggestedTags;

    /**
     * AI建议的分类
     */
    @Column(name = "ai_suggested_category", length = 100)
    private String aiSuggestedCategory;

    // ==================== 结构化数据 ====================

    /**
     * 结构化元数据（用于Schema.org等，JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "structured_data", columnDefinition = "JSON")
    private Object structuredData;

    /**
     * AI识别的实体（人物、地点、概念等，JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "entities", columnDefinition = "JSON")
    private List<String> entities;

    // ==================== 状态和元数据 ====================

    /**
     * 文章状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT;

    /**
     * 可见性
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    /**
     * 访问密码（如果需要）
     */
    @Column(name = "password", length = 100)
    private String password;

    // ==================== 统计字段 ====================

    /**
     * 浏览次数
     */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    /**
     * 点赞次数
     */
    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

    /**
     * 评论数量
     */
    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Long commentCount = 0L;

    /**
     * 收藏次数
     */
    @Column(name = "bookmark_count", nullable = false)
    @Builder.Default
    private Long bookmarkCount = 0L;

    // ==================== SEO字段 ====================

    /**
     * SEO标题
     */
    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    /**
     * SEO描述
     */
    @Column(name = "meta_description", length = 300)
    private String metaDescription;

    /**
     * Open Graph图片
     */
    @Column(name = "og_image", length = 500)
    private String ogImage;

    // ==================== 关联字段 ====================

    /**
     * 作者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * 分类
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * 特色图片URL
     */
    @Column(name = "featured_image", length = 500)
    private String featuredImage;

    /**
     * 标签列表
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "article_tags",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // ==================== 时间字段 ====================

    /**
     * 发布时间
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 计划发布时间
     */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

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
     * 文章状态枚举
     */
    public enum ArticleStatus {
        DRAFT, PUBLISHED, ARCHIVED, SCHEDULED
    }

    /**
     * 可见性枚举
     */
    public enum Visibility {
        PUBLIC, PRIVATE, PASSWORD_PROTECTED
    }
}
