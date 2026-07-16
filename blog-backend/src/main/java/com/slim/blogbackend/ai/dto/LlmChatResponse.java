package com.slim.blogbackend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmChatResponse {
    private String text;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Long latencyMs;
    private String finishReason;
    private String requestId;
    private String errorCode;
    private String errorMessage;

    public boolean isSuccess() {
        return errorCode == null && text != null;
    }
}
