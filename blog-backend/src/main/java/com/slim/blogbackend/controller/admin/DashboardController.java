package com.slim.blogbackend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.slim.blogbackend.entity.Article;
import com.slim.blogbackend.entity.Bookmark;
import com.slim.blogbackend.entity.Category;
import com.slim.blogbackend.entity.Comment;
import com.slim.blogbackend.entity.Tag;
import com.slim.blogbackend.mapper.ArticleMapper;
import com.slim.blogbackend.mapper.BookmarkMapper;
import com.slim.blogbackend.mapper.CategoryMapper;
import com.slim.blogbackend.mapper.CommentMapper;
import com.slim.blogbackend.mapper.TagMapper;
import com.slim.blogbackend.vo.DashboardStats;
import com.slim.blogbackend.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private BookmarkMapper bookmarkMapper;

    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        LambdaQueryWrapper<Article> allArticleWrapper = new LambdaQueryWrapper<>();
        Long totalArticles = articleMapper.selectCount(allArticleWrapper);

        LambdaQueryWrapper<Article> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(Article::getStatus, "PUBLISHED");
        Long publishedArticles = articleMapper.selectCount(publishedWrapper);

        LambdaQueryWrapper<Article> draftWrapper = new LambdaQueryWrapper<>();
        draftWrapper.eq(Article::getStatus, "DRAFT");
        Long draftArticles = articleMapper.selectCount(draftWrapper);

        Long totalCategories = categoryMapper.selectCount(new LambdaQueryWrapper<>());
        Long totalTags = tagMapper.selectCount(new LambdaQueryWrapper<>());
        Long totalComments = commentMapper.selectCount(new LambdaQueryWrapper<>());
        Long totalBookmarks = bookmarkMapper.selectCount(new LambdaQueryWrapper<>());

        Long totalViews = 0L;
        try {
            totalViews = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                            .select(Article::getViewCount))
                    .stream()
                    .mapToLong(a -> a.getViewCount() == null ? 0 : a.getViewCount())
                    .sum();
        } catch (Exception ignored) {
        }

        DashboardStats stats = DashboardStats.builder()
                .totalArticles(totalArticles)
                .publishedArticles(publishedArticles)
                .draftArticles(draftArticles)
                .totalCategories(totalCategories)
                .totalTags(totalTags)
                .totalViews(totalViews)
                .totalComments(totalComments)
                .totalBookmarks(totalBookmarks)
                .build();

        return Result.success(stats);
    }
}
