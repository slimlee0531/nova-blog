package com.slim.blogbackend.controller.api;

import com.slim.blogbackend.dto.response.ArticleResponseDTO;
import com.slim.blogbackend.service.ArticleService;
import com.slim.blogbackend.vo.PageResult;
import com.slim.blogbackend.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleApiController {

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public Result<PageResult<ArticleResponseDTO>> getArticleList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return articleService.getArticleList(page, size, "PUBLISHED");
    }

    @GetMapping("/{slug}")
    public Result<ArticleResponseDTO> getArticleBySlug(@PathVariable String slug) {
        return articleService.getArticleBySlug(slug);
    }

    @GetMapping("/{id}")
    public Result<ArticleResponseDTO> getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }

    @GetMapping("/category/{categorySlug}")
    public Result<List<ArticleResponseDTO>> getArticlesByCategory(@PathVariable String categorySlug) {
        return articleService.getArticlesByCategory(categorySlug);
    }

    @GetMapping("/tag/{tagSlug}")
    public Result<List<ArticleResponseDTO>> getArticlesByTag(@PathVariable String tagSlug) {
        return articleService.getArticlesByTag(tagSlug);
    }
}
