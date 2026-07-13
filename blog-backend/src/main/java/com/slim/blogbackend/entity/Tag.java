package com.slim.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 标签实体类
 * 对应数据库 tags 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tags")
public class Tag {

    /**
     * 标签ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 标签名称
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * URL友好的标识
     */
    @Column(name = "slug", nullable = false, unique = true, length = 50)
    private String slug;

    /**
     * 标签描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 标签颜色（十六进制）
     */
    @Column(name = "color", length = 7)
    private String color;

    /**
     * 文章数量（冗余字段）
     */
    @Column(name = "article_count", nullable = false)
    @Builder.Default
    private Integer articleCount = 0;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
