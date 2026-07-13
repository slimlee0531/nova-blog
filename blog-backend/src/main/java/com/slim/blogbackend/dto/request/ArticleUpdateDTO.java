package com.slim.blogbackend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ArticleUpdateDTO {

    private String title;

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
