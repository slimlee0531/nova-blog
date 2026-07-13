package com.slim.blogbackend.controller.api;

import com.slim.blogbackend.dto.request.CommentCreateDTO;
import com.slim.blogbackend.dto.response.CommentResponseDTO;
import com.slim.blogbackend.service.CommentService;
import com.slim.blogbackend.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentApiController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public Result<CommentResponseDTO> createComment(@Valid @RequestBody CommentCreateDTO dto,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String guestIp = request.getRemoteAddr();
        return commentService.createComment(dto, userId, guestIp);
    }

    @GetMapping("/article/{articleId}")
    public Result<List<CommentResponseDTO>> getCommentsByArticle(@PathVariable Long articleId) {
        return commentService.getCommentsByArticle(articleId);
    }
}
