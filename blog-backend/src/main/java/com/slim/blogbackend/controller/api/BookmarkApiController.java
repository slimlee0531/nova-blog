package com.slim.blogbackend.controller.api;

import com.slim.blogbackend.service.BookmarkService;
import com.slim.blogbackend.vo.PageResult;
import com.slim.blogbackend.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkApiController {

    @Autowired
    private BookmarkService bookmarkService;

    /**
     * 切换收藏状态
     */
    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleBookmark(@RequestBody Map<String, Long> body,
                                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long articleId = body.get("articleId");
        return bookmarkService.toggleBookmark(userId, articleId);
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/check/{articleId}")
    public Result<Map<String, Object>> checkBookmark(@PathVariable Long articleId,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return bookmarkService.checkBookmark(userId, articleId);
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> getUserBookmarks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return bookmarkService.getUserBookmarks(userId, page, size);
    }

    /**
     * 获取用户收藏数量
     */
    @GetMapping("/count")
    public Result<Long> getUserBookmarkCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return bookmarkService.getUserBookmarkCount(userId);
    }
}
