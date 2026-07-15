package com.slim.blogbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Long totalArticles;
    private Long publishedArticles;
    private Long draftArticles;
    private Long totalCategories;
    private Long totalTags;
    private Long totalViews;
    private Long totalComments;
    private Long totalBookmarks;
}
