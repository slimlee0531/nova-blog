package com.slim.blogbackend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmChatRequest {
    private String systemPrompt;
    private String userPrompt;
    private String model;
    private Integer maxOutputTokens;
    private Double temperature;
    @Builder.Default
    private Boolean stream = false;
    private List<String> stop;
}
