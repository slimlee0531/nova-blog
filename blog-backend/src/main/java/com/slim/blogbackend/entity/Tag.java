package com.slim.blogbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tags")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String slug;

    private String description;

    private String color;

    private Integer articleCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
