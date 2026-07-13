package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论实体类
 * 支持人类和AI Agent的评论
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    // ==================== 评论者信息 ====================

    private Long userId;

    private String guestName;

    private String guestEmail;

    private String guestWebsite;

    private String guestIp;

    private String guestUserAgent;

    // ==================== Agent评论标识 ====================

    private Boolean isAgentComment;

    private String agentId;

    private String agentMetadata;

    // ==================== 评论关系 ====================

    private Long articleId;

    private Long parentId;

    // ==================== 状态 ====================

    private String status;

    // ==================== 统计 ====================

    private Long likeCount;

    // ==================== 时间字段 ====================

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 评论状态枚举
     */
    public enum CommentStatus {
        PENDING, APPROVED, REJECTED, SPAM
    }
}
