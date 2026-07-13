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
 * 媒体文件实体类
 * 对应数据库 media 表
 * 存储上传的图片、视频等媒体文件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "media", indexes = {
    @Index(name = "idx_uploader", columnList = "uploader_id"),
    @Index(name = "idx_article", columnList = "article_id"),
    @Index(name = "idx_mime_type", columnList = "mime_type")
})
public class Media {

    /**
     * 媒体ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 文件名
     */
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    /**
     * 原始文件名
     */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /**
     * MIME类型
     */
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * 文件存储路径
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 访问URL
     */
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    // ==================== 图片特有字段 ====================

    /**
     * 图片宽度
     */
    @Column(name = "width")
    private Integer width;

    /**
     * 图片高度
     */
    @Column(name = "height")
    private Integer height;

    /**
     * 替代文本（SEO和无障碍）
     */
    @Column(name = "alt_text", length = 200)
    private String altText;

    // ==================== AI分析字段 ====================

    /**
     * AI生成的图片描述
     */
    @Column(name = "ai_description", columnDefinition = "TEXT")
    private String aiDescription;

    /**
     * AI识别的标签（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "ai_tags", columnDefinition = "JSON")
    private java.util.List<String> aiTags;

    // ==================== 元数据 ====================

    /**
     * 其他元数据（JSON格式）
     */
    @Type(com.hibernate.type.JsonType.class)
    @Column(name = "metadata", columnDefinition = "JSON")
    private Object metadata;

    // ==================== 关联 ====================

    /**
     * 上传者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    /**
     * 关联文章（可选）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

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
}
