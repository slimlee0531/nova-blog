package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 媒体文件实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("media")
public class Media {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String filename;

    private String originalName;

    private String mimeType;

    private Long fileSize;

    private String filePath;

    private String url;

    // ==================== 图片特有字段 ====================

    private Integer width;

    private Integer height;

    private String altText;

    // ==================== AI分析字段 ====================

    private String aiDescription;

    private String aiTags;

    // ==================== 元数据 ====================

    private String metadata;

    // ==================== 关联 ====================

    private Long uploaderId;

    private Long articleId;

    // ==================== 时间字段 ====================

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
