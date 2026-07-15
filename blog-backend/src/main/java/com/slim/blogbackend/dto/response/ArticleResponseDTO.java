package com.slim.blogbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponseDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagInfo {
        private Long id;
        private String name;
        private String color;
    }

    private Long id;
    private String title;
    private String slug;
    private String content;
    private String contentHtml;
    private String summary;
    private String status;
    private String visibility;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long bookmarkCount;
    private String metaTitle;
    private String metaDescription;
    private String ogImage;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
    private String featuredImage;
    private List<String> tags;
    private List<TagInfo> tagInfos;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // AI字段
    private String aiSummary;
    private String aiKeywords;
    private Integer aiReadingTime;
    private BigDecimal aiContentQualityScore;
    private String aiSuggestedTags;
    private String structuredData;
    private String entities;
}
