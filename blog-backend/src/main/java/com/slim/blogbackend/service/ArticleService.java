package com.slim.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slim.blogbackend.dto.request.ArticleCreateDTO;
import com.slim.blogbackend.dto.request.ArticleUpdateDTO;
import com.slim.blogbackend.dto.response.ArticleResponseDTO;
import com.slim.blogbackend.entity.Article;
import com.slim.blogbackend.entity.ArticleTag;
import com.slim.blogbackend.entity.Category;
import com.slim.blogbackend.entity.Tag;
import com.slim.blogbackend.entity.User;
import com.slim.blogbackend.exception.BusinessException;
import com.slim.blogbackend.mapper.ArticleMapper;
import com.slim.blogbackend.mapper.ArticleTagMapper;
import com.slim.blogbackend.mapper.CategoryMapper;
import com.slim.blogbackend.mapper.TagMapper;
import com.slim.blogbackend.mapper.UserMapper;
import com.slim.blogbackend.vo.PageResult;
import com.slim.blogbackend.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Transactional
    public Result<ArticleResponseDTO> createArticle(ArticleCreateDTO dto, Long authorId) {
        String slug = generateSlug(dto.getTitle());

        Article article = Article.builder()
                .title(dto.getTitle())
                .slug(slug)
                .content(dto.getContent())
                .summary(dto.getSummary())
                .status(dto.getStatus() != null ? dto.getStatus() : "DRAFT")
                .visibility(dto.getVisibility() != null ? dto.getVisibility() : "PUBLIC")
                .authorId(authorId)
                .categoryId(dto.getCategoryId())
                .featuredImage(dto.getFeaturedImage())
                .metaTitle(dto.getMetaTitle())
                .metaDescription(dto.getMetaDescription())
                .viewCount(0L)
                .likeCount(0L)
                .commentCount(0L)
                .bookmarkCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if ("PUBLISHED".equals(article.getStatus())) {
            article.setPublishedAt(LocalDateTime.now());
        }

        articleMapper.insert(article);

        // 处理标签关联并更新计数
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            saveArticleTags(article.getId(), dto.getTags());
        }

        // 更新分类计数
        if (dto.getCategoryId() != null) {
            incrCategoryCount(dto.getCategoryId(), 1);
        }

        return Result.success(toResponseDTO(article));
    }

    @Transactional
    public Result<ArticleResponseDTO> updateArticle(Long id, ArticleUpdateDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        Long oldCategoryId = article.getCategoryId();
        List<Long> oldTagIds = getArticleTagIds(id);

        if (dto.getTitle() != null) article.setTitle(dto.getTitle());
        if (dto.getContent() != null) article.setContent(dto.getContent());
        if (dto.getSummary() != null) article.setSummary(dto.getSummary());
        if (dto.getCategoryId() != null) article.setCategoryId(dto.getCategoryId());
        if (dto.getStatus() != null) article.setStatus(dto.getStatus());
        if (dto.getVisibility() != null) article.setVisibility(dto.getVisibility());
        if (dto.getMetaTitle() != null) article.setMetaTitle(dto.getMetaTitle());
        if (dto.getMetaDescription() != null) article.setMetaDescription(dto.getMetaDescription());
        if (dto.getFeaturedImage() != null) article.setFeaturedImage(dto.getFeaturedImage());

        article.setUpdatedAt(LocalDateTime.now());

        if ("PUBLISHED".equals(dto.getStatus()) && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }

        articleMapper.updateById(article);

        // 更新标签关联并同步计数
        if (dto.getTags() != null) {
            // 先减少旧标签计数
            if (!oldTagIds.isEmpty()) {
                incrTagCount(oldTagIds, -1);
            }
            articleTagMapper.deleteByArticleId(id);
            if (!dto.getTags().isEmpty()) {
                saveArticleTags(id, dto.getTags());
            }
        }

        // 更新分类计数
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(oldCategoryId)) {
            if (oldCategoryId != null) {
                incrCategoryCount(oldCategoryId, -1);
            }
            incrCategoryCount(dto.getCategoryId(), 1);
        }

        return Result.success(toResponseDTO(article));
    }

    @Transactional
    public Result<Void> deleteArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        // 减少旧标签计数
        List<Long> oldTagIds = getArticleTagIds(id);
        if (!oldTagIds.isEmpty()) {
            incrTagCount(oldTagIds, -1);
        }
        // 减少分类计数
        if (article.getCategoryId() != null) {
            incrCategoryCount(article.getCategoryId(), -1);
        }
        // 删除标签关联
        articleTagMapper.deleteByArticleId(id);
        articleMapper.deleteById(id);
        return Result.success();
    }

    public Result<ArticleResponseDTO> getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);

        return Result.success(toResponseDTO(article));
    }

    public Result<ArticleResponseDTO> getArticleBySlug(String slug) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getSlug, slug);
        Article article = articleMapper.selectOne(wrapper);

        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);

        return Result.success(toResponseDTO(article));
    }

    public Result<PageResult<ArticleResponseDTO>> getArticleList(int page, int size, String status) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        } else {
            wrapper.eq(Article::getStatus, "PUBLISHED");
        }

        wrapper.orderByDesc(Article::getPublishedAt);

        Page<Article> result = articleMapper.selectPage(pageParam, wrapper);

        List<ArticleResponseDTO> records = result.getRecords().stream()
                .map(this::toResponseDTO)
                .toList();

        return Result.success(PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    public Result<PageResult<ArticleResponseDTO>> getAdminArticleList(int page, int size, String status) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }

        wrapper.orderByDesc(Article::getCreatedAt);

        Page<Article> result = articleMapper.selectPage(pageParam, wrapper);

        List<ArticleResponseDTO> records = result.getRecords().stream()
                .map(this::toResponseDTO)
                .toList();

        return Result.success(PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    public Result<List<ArticleResponseDTO>> getArticlesByCategory(String categorySlug) {
        List<Article> articles = articleMapper.selectByCategorySlug(categorySlug);
        List<ArticleResponseDTO> list = articles.stream()
                .map(this::toResponseDTO)
                .toList();
        return Result.success(list);
    }

    public Result<List<ArticleResponseDTO>> getArticlesByTag(String tagSlug) {
        List<Article> articles = articleMapper.selectByTagSlug(tagSlug);
        List<ArticleResponseDTO> list = articles.stream()
                .map(this::toResponseDTO)
                .toList();
        return Result.success(list);
    }

    /**
     * 保存文章标签关联并更新计数
     */
    private void saveArticleTags(Long articleId, List<String> tagNames) {
        List<Long> addedTagIds = new ArrayList<>();
        for (String tagName : tagNames) {
            LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(Tag::getName, tagName);
            Tag tag = tagMapper.selectOne(tagWrapper);

            if (tag == null) {
                tag = Tag.builder()
                        .name(tagName)
                        .slug(tagName.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-"))
                        .articleCount(0)
                        .createdAt(LocalDateTime.now())
                        .build();
                tagMapper.insert(tag);
            }

            ArticleTag articleTag = ArticleTag.builder()
                    .articleId(articleId)
                    .tagId(tag.getId())
                    .createdAt(LocalDateTime.now())
                    .build();
            articleTagMapper.insert(articleTag);

            addedTagIds.add(tag.getId());
        }
        if (!addedTagIds.isEmpty()) {
            incrTagCount(addedTagIds, 1);
        }
    }

    /**
     * 获取文章关联的标签ID列表
     */
    private List<Long> getArticleTagIds(Long articleId) {
        LambdaQueryWrapper<ArticleTag> atWrapper = new LambdaQueryWrapper<>();
        atWrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(atWrapper);
        if (articleTags == null || articleTags.isEmpty()) {
            return Collections.emptyList();
        }
        return articleTags.stream().map(ArticleTag::getTagId).toList();
    }

    /**
     * 批量增减标签文章计数
     */
    private void incrTagCount(List<Long> tagIds, int delta) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Tag::getId, tagIds);
        List<Tag> tags = tagMapper.selectList(wrapper);
        if (tags != null) {
            for (Tag tag : tags) {
                int newCount = Math.max(0, (tag.getArticleCount() == null ? 0 : tag.getArticleCount()) + delta);
                tag.setArticleCount(newCount);
                tagMapper.updateById(tag);
            }
        }
    }

    /**
     * 增减分类文章计数
     */
    private void incrCategoryCount(Long categoryId, int delta) {
        if (categoryId == null) return;
        Category category = categoryMapper.selectById(categoryId);
        if (category != null) {
            int newCount = Math.max(0, (category.getArticleCount() == null ? 0 : category.getArticleCount()) + delta);
            category.setArticleCount(newCount);
            categoryMapper.updateById(category);
        }
    }

    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isEmpty()) {
            slug = "article-" + System.currentTimeMillis();
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getSlug, slug);
        if (articleMapper.selectCount(wrapper) > 0) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        return slug;
    }

    private ArticleResponseDTO toResponseDTO(Article article) {
        ArticleResponseDTO.ArticleResponseDTOBuilder builder = ArticleResponseDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .content(article.getContent())
                .contentHtml(article.getContentHtml())
                .summary(article.getSummary())
                .status(article.getStatus())
                .visibility(article.getVisibility())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .bookmarkCount(article.getBookmarkCount())
                .metaTitle(article.getMetaTitle())
                .metaDescription(article.getMetaDescription())
                .ogImage(article.getOgImage())
                .authorId(article.getAuthorId())
                .categoryId(article.getCategoryId())
                .featuredImage(article.getFeaturedImage())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .aiSummary(article.getAiSummary())
                .aiKeywords(article.getAiKeywords())
                .aiReadingTime(article.getAiReadingTime())
                .aiContentQualityScore(article.getAiContentQualityScore())
                .aiSuggestedTags(article.getAiSuggestedTags())
                .structuredData(article.getStructuredData())
                .entities(article.getEntities());

        if (article.getAuthorId() != null) {
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null) {
                builder.authorName(author.getDisplayName() != null ? author.getDisplayName() : author.getUsername());
            }
        }

        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                builder.categoryName(category.getName());
            }
        }

        // 查询标签
        LambdaQueryWrapper<ArticleTag> atWrapper = new LambdaQueryWrapper<>();
        atWrapper.eq(ArticleTag::getArticleId, article.getId());
        List<ArticleTag> articleTags = articleTagMapper.selectList(atWrapper);
        if (articleTags != null && !articleTags.isEmpty()) {
            List<Long> tagIds = articleTags.stream()
                    .map(ArticleTag::getTagId)
                    .toList();
            LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.in(Tag::getId, tagIds);
            List<Tag> tags = tagMapper.selectList(tagWrapper);
            List<String> tagNames = tags.stream()
                    .map(Tag::getName)
                    .toList();
            builder.tags(tagNames);

            List<ArticleResponseDTO.TagInfo> tagInfos = tags.stream()
                    .map(t -> ArticleResponseDTO.TagInfo.builder()
                            .id(t.getId())
                            .name(t.getName())
                            .color(t.getColor())
                            .build())
                    .toList();
            builder.tagInfos(tagInfos);
        }

        return builder.build();
    }
}
