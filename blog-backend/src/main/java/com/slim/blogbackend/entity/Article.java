package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体类
 * 核心表，存储博客文章内容，包含AI增强字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("articles")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String slug;

    private String content;

    private String contentHtml;

    private String summary;

    // ==================== AI增强字段 ====================

    private String aiSummary;

    private String aiKeywords;

    private Integer aiReadingTime;

    private BigDecimal aiContentQualityScore;

    private String aiSuggestedTags;

    private String aiSuggestedCategory;

    // ==================== 结构化数据 ====================

    private String structuredData;

    private String entities;

    // ==================== 状态和元数据 ====================

    private String status;

    private String visibility;

    private String password;

    // ==================== 统计字段 ====================

    private Long viewCount;

    private Long likeCount;

    private Long commentCount;

    private Long bookmarkCount;

    // ==================== SEO字段 ====================

    private String metaTitle;

    private String metaDescription;

    private String ogImage;

    // ==================== 关联字段 ====================

    private Long authorId;

    private Long categoryId;

    private String featuredImage;

    // ==================== 时间字段 ====================

    private LocalDateTime publishedAt;

    private LocalDateTime scheduledAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
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
