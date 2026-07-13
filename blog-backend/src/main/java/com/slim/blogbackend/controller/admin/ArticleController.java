package com.slim.blogbackend.controller.admin;

import com.slim.blogbackend.dto.request.ArticleCreateDTO;
import com.slim.blogbackend.dto.request.ArticleUpdateDTO;
import com.slim.blogbackend.dto.response.ArticleResponseDTO;
import com.slim.blogbackend.service.ArticleService;
import com.slim.blogbackend.vo.PageResult;
import com.slim.blogbackend.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PostMapping
    public Result<ArticleResponseDTO> createArticle(@Valid @RequestBody ArticleCreateDTO dto,
                                                     HttpServletRequest request) {
        Long authorId = (Long) request.getAttribute("userId");
        return articleService.createArticle(dto, authorId);
    }

    @PutMapping("/{id}")
    public Result<ArticleResponseDTO> updateArticle(@PathVariable Long id,
                                                     @RequestBody ArticleUpdateDTO dto) {
        return articleService.updateArticle(id, dto);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        return articleService.deleteArticle(id);
    }

    @GetMapping("/{id}")
    public Result<ArticleResponseDTO> getArticle(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }

    @GetMapping("/list")
    public Result<PageResult<ArticleResponseDTO>> getArticleList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return articleService.getAdminArticleList(page, size, status);
    }
}
