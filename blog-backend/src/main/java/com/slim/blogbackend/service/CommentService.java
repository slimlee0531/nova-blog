package com.slim.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slim.blogbackend.dto.request.CommentCreateDTO;
import com.slim.blogbackend.dto.response.CommentResponseDTO;
import com.slim.blogbackend.entity.Article;
import com.slim.blogbackend.entity.Comment;
import com.slim.blogbackend.entity.User;
import com.slim.blogbackend.exception.BusinessException;
import com.slim.blogbackend.mapper.ArticleMapper;
import com.slim.blogbackend.mapper.CommentMapper;
import com.slim.blogbackend.mapper.UserMapper;
import com.slim.blogbackend.vo.PageResult;
import com.slim.blogbackend.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public Result<CommentResponseDTO> createComment(CommentCreateDTO dto, Long userId, String guestIp) {
        Article article = articleMapper.selectById(dto.getArticleId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .userId(userId)
                .guestName(dto.getGuestName())
                .guestEmail(dto.getGuestEmail())
                .guestWebsite(dto.getGuestWebsite())
                .guestIp(guestIp)
                .articleId(dto.getArticleId())
                .parentId(dto.getParentId())
                .status(Comment.CommentStatus.PENDING.name())
                .isAgentComment(false)
                .likeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        commentMapper.insert(comment);

        article.setCommentCount(article.getCommentCount() + 1);
        articleMapper.updateById(article);

        return Result.success(toResponseDTO(comment));
    }

    public Result<List<CommentResponseDTO>> getCommentsByArticle(Long articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, "APPROVED")
                .orderByAsc(Comment::getCreatedAt);

        List<Comment> allComments = commentMapper.selectList(wrapper);

        Map<Long, List<Comment>> parentComments = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.groupingBy(Comment::getId));

        List<CommentResponseDTO> result = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .map(this::toResponseDTO)
                .toList();

        return Result.success(result);
    }

    @Transactional
    public Result<Void> deleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        commentMapper.deleteById(id);

        Article article = articleMapper.selectById(comment.getArticleId());
        if (article != null) {
            article.setCommentCount(Math.max(0, article.getCommentCount() - 1));
            articleMapper.updateById(article);
        }

        return Result.success();
    }

    @Transactional
    public Result<Void> approveComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        comment.setStatus(Comment.CommentStatus.APPROVED.name());
        commentMapper.updateById(comment);
        return Result.success();
    }

    @Transactional
    public Result<Void> rejectComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        comment.setStatus(Comment.CommentStatus.REJECTED.name());
        commentMapper.updateById(comment);
        return Result.success();
    }

    public Result<PageResult<CommentResponseDTO>> adminGetComments(int page, int size, String status, Long articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Comment::getStatus, status);
        }
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }

        wrapper.orderByDesc(Comment::getCreatedAt);

        Page<Comment> pageResult = commentMapper.selectPage(new Page<>(page, size), wrapper);

        List<CommentResponseDTO> records = pageResult.getRecords().stream()
                .map(this::toResponseDTO)
                .toList();

        return Result.success(PageResult.of(records, pageResult.getTotal(), page, size));
    }

    private CommentResponseDTO toResponseDTO(Comment comment) {
        CommentResponseDTO.CommentResponseDTOBuilder builder = CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .guestName(comment.getGuestName())
                .guestEmail(comment.getGuestEmail())
                .isAgentComment(comment.getIsAgentComment())
                .agentId(comment.getAgentId())
                .articleId(comment.getArticleId())
                .parentId(comment.getParentId())
                .status(comment.getStatus())
                .likeCount(comment.getLikeCount())
                .createdAt(comment.getCreatedAt());

        if (comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                builder.userName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
                builder.userAvatar(user.getAvatarUrl());
            }
        }

        return builder.build();
    }
}
