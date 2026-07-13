package com.slim.blogbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ArticleCreateDTO {

    @NotBlank(message = "文章标题不能为空")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    private Long categoryId;

    private List<String> tags;

    private String status;

    private String visibility;

    private String metaTitle;

    private String metaDescription;

    private String featuredImage;

    private String summary;
}
