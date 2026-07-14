package com.slim.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slim.blogbackend.entity.Article;
import com.slim.blogbackend.entity.Bookmark;
import com.slim.blogbackend.mapper.ArticleMapper;
import com.slim.blogbackend.mapper.BookmarkMapper;
import com.slim.blogbackend.vo.PageResult;
import com.slim.blogbackend.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookmarkService {

    @Autowired
    private BookmarkMapper bookmarkMapper;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 切换收藏状态（收藏/取消收藏）
     */
    @Transactional
    public Result<Map<String, Object>> toggleBookmark(Long userId, Long articleId) {
        // 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return Result.error("文章不存在");
        }

        // 检查是否已收藏
        LambdaQueryWrapper<Bookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bookmark::getUserId, userId)
                .eq(Bookmark::getArticleId, articleId);
        Bookmark existingBookmark = bookmarkMapper.selectOne(wrapper);

        Map<String, Object> result = new HashMap<>();

        if (existingBookmark != null) {
            // 已收藏，取消收藏
            bookmarkMapper.deleteById(existingBookmark.getId());

            // 更新文章收藏数
            article.setBookmarkCount(Math.max(0, article.getBookmarkCount() - 1));
            articleMapper.updateById(article);

            result.put("bookmarked", false);
            result.put("bookmarkCount", article.getBookmarkCount());
            return Result.success("取消收藏成功", result);
        } else {
            // 未收藏，添加收藏
            Bookmark bookmark = Bookmark.builder()
                    .userId(userId)
                    .articleId(articleId)
                    .createdAt(LocalDateTime.now())
                    .build();
            bookmarkMapper.insert(bookmark);

            // 更新文章收藏数
            article.setBookmarkCount(article.getBookmarkCount() + 1);
            articleMapper.updateById(article);

            result.put("bookmarked", true);
            result.put("bookmarkCount", article.getBookmarkCount());
            return Result.success("收藏成功", result);
        }
    }

    /**
     * 检查是否已收藏
     */
    public Result<Map<String, Object>> checkBookmark(Long userId, Long articleId) {
        LambdaQueryWrapper<Bookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bookmark::getUserId, userId)
                .eq(Bookmark::getArticleId, articleId);
        Bookmark bookmark = bookmarkMapper.selectOne(wrapper);

        Article article = articleMapper.selectById(articleId);

        Map<String, Object> result = new HashMap<>();
        result.put("bookmarked", bookmark != null);
        result.put("bookmarkCount", article != null ? article.getBookmarkCount() : 0);

        return Result.success(result);
    }

    /**
     * 获取用户收藏列表
     */
    public Result<PageResult<Map<String, Object>>> getUserBookmarks(Long userId, int page, int size) {
        Page<Bookmark> bookmarkPage = new Page<>(page, size);

        LambdaQueryWrapper<Bookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bookmark::getUserId, userId)
                .orderByDesc(Bookmark::getCreatedAt);

        bookmarkMapper.selectPage(bookmarkPage, wrapper);

        List<Map<String, Object>> records = bookmarkPage.getRecords().stream()
                .map(bookmark -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", bookmark.getId());
                    item.put("createdAt", bookmark.getCreatedAt());

                    // 获取文章信息
                    Article article = articleMapper.selectById(bookmark.getArticleId());
                    if (article != null) {
                        item.put("articleId", article.getId());
                        item.put("title", article.getTitle());
                        item.put("slug", article.getSlug());
                        item.put("summary", article.getSummary());
                        item.put("viewCount", article.getViewCount());
                        item.put("likeCount", article.getLikeCount());
                        item.put("commentCount", article.getCommentCount());
                        item.put("bookmarkCount", article.getBookmarkCount());
                        item.put("featuredImage", article.getFeaturedImage());
                        item.put("publishedAt", article.getPublishedAt());
                        item.put("categoryId", article.getCategoryId());
                    }

                    return item;
                })
                .collect(Collectors.toList());

        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal((int) bookmarkPage.getTotal());
        pageResult.setPage(page);
        pageResult.setSize(size);

        return Result.success(pageResult);
    }

    /**
     * 获取用户收藏数量
     */
    public Result<Long> getUserBookmarkCount(Long userId) {
        LambdaQueryWrapper<Bookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bookmark::getUserId, userId);
        Long count = bookmarkMapper.selectCount(wrapper);
        return Result.success(count);
    }
}
