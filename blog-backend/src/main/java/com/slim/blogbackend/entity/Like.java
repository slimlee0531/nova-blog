package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 点赞实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("likes")
public class Like {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String ipAddress;

    private String fingerprint;

    private String likeableType;

    private Long likeableId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 点赞类型枚举
     */
    public enum LikeableType {
        ARTICLE, COMMENT
    }
}
