package com.slim.blogbackend.controller.admin;

import com.slim.blogbackend.dto.response.CommentResponseDTO;
import com.slim.blogbackend.service.CommentService;
import com.slim.blogbackend.vo.PageResult;
import com.slim.blogbackend.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/list")
    public Result<PageResult<CommentResponseDTO>> getCommentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long articleId) {
        return commentService.adminGetComments(page, size, status, articleId);
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approveComment(@PathVariable Long id) {
        return commentService.approveComment(id);
    }

    @PutMapping("/{id}/reject")
    public Result<Void> rejectComment(@PathVariable Long id) {
        return commentService.rejectComment(id);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        return commentService.deleteComment(id);
    }
}
