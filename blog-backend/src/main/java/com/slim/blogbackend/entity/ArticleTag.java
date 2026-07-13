package com.slim.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 文章标签关联实体类
 * 对应数据库 article_tags 表
 * 虽然JPA的@ManyToMany已经处理了关联，但保留此实体类用于特殊查询
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "article_tags")
public class ArticleTag {

    /**
     * 文章ID
     */
    @Id
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    /**
     * 标签ID
     */
    @Id
    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 文章
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("articleId")
    @JoinColumn(name = "article_id")
    private Article article;

    /**
     * 标签
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
