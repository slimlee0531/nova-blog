package com.slim.blogbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {

    private Long id;
    private String content;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String guestName;
    private String guestEmail;
    private Boolean isAgentComment;
    private String agentId;
    private Long articleId;
    private Long parentId;
    private String status;
    private Long likeCount;
    private List<CommentResponseDTO> children;
    private LocalDateTime createdAt;
}
