package com.slim.blogbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentCreateDTO {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private String guestName;

    private String guestEmail;

    private String guestWebsite;
}
