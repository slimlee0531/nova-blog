package com.slim.blogbackend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slim.blogbackend.ai.dto.LlmChatRequest;
import com.slim.blogbackend.ai.dto.LlmChatResponse;
import com.slim.blogbackend.ai.enums.AiProviderEnum;
import com.slim.blogbackend.ai.enums.AiTaskTypeEnum;
import com.slim.blogbackend.ai.service.LlmService;
import com.slim.blogbackend.ai.service.TaskPromptService;
import com.slim.blogbackend.config.AiProperties;
import com.slim.blogbackend.exception.BusinessException;
import com.slim.blogbackend.vo.Result;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/api/admin/ai")
public class AiAdminController {

    @Autowired
    private LlmService llmService;

    @Autowired
    private AiProperties aiProperties;

    @Autowired
    private TaskPromptService taskPromptService;

    private final ObjectMapper om = new ObjectMapper();

    @GetMapping("/config")
    public Result<AiConfigDTO> getConfig() {
        AiProperties.AliyunProperties a = aiProperties.getAliyun();
        String keyMasked = mask(a.getApiKey());
        boolean keySet = StringUtils.hasText(a.getApiKey());
        String providerName;
        try {
            AiProviderEnum p = AiProviderEnum.of(aiProperties.getDefaultProvider());
            providerName = p == AiProviderEnum.ALIYUN ? "阿里云 DashScope（通义千问）" : p.getCode();
        } catch (Exception ignore) {
            providerName = aiProperties.getDefaultProvider();
        }
        return Result.success(AiConfigDTO.builder()
                .enabled(aiProperties.isEnabled())
                .defaultProvider(aiProperties.getDefaultProvider())
                .providerName(providerName)
                .model(a.getModel())
                .apiKeyMasked(keyMasked)
                .apiKeySet(keySet)
                .timeoutSeconds(a.getTimeoutSeconds())
                .build());
    }

    @PostMapping("/test")
    public Result<TestResultDTO> testConnection() {
        long start = System.currentTimeMillis();
        LlmChatResponse resp;
        try {
            resp = llmService.testConnection();
        } catch (RuntimeException e) {
            long latency = System.currentTimeMillis() - start;
            return Result.success(TestResultDTO.builder()
                    .ok(false)
                    .provider(aiProperties.getDefaultProvider())
                    .model(aiProperties.getAliyun().getModel())
                    .latencyMs(latency)
                    .error("请求前异常：" + e.getMessage())
                    .build());
        }
        long latency = resp.getLatencyMs() != null ? resp.getLatencyMs() : (System.currentTimeMillis() - start);
        TestResultDTO.TestResultDTOBuilder b = TestResultDTO.builder()
                .ok(resp.isSuccess())
                .provider(aiProperties.getDefaultProvider())
                .model(aiProperties.getAliyun().getModel())
                .latencyMs(latency)
                .text(resp.getText())
                .requestId(resp.getRequestId())
                .promptTokens(resp.getPromptTokens())
                .completionTokens(resp.getCompletionTokens())
                .totalTokens(resp.getTotalTokens());
        if (!resp.isSuccess()) {
            b.error((resp.getErrorCode() != null ? resp.getErrorCode() + ": " : "") + resp.getErrorMessage());
        }
        return Result.success(b.build());
    }

    @GetMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generate(
            @RequestParam("taskType") String taskType,
            @RequestParam(value = "articleId", required = false) Long articleId,
            @RequestParam(value = "title", required = false, defaultValue = "") String title,
            @RequestParam(value = "content", required = false, defaultValue = "") String content,
            @RequestParam(value = "summary", required = false, defaultValue = "") String summary,
            @RequestParam(value = "tags", required = false, defaultValue = "") String tags
    ) {
        AiTaskTypeEnum type;
        try {
            type = AiTaskTypeEnum.valueOf(taskType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return immediatelyErrorEmitter("BAD_PARAMS", "不支持的 taskType: " + taskType);
        }

        LlmChatRequest chatReq;
        try {
            chatReq = taskPromptService.buildChatRequest(type, title, content, summary, tags);
        } catch (BusinessException e) {
            return immediatelyErrorEmitter("BAD_PARAMS", e.getMessage());
        }

        SseEmitter emitter = new SseEmitter(Math.max(60000L, aiProperties.getAliyun().getTimeoutSeconds() * 1000L + 10000L));
        AtomicInteger seq = new AtomicInteger(0);
        StringBuilder received = new StringBuilder();

        sendSse(emitter, "meta", Map.of(
                "taskType", type.getCode(),
                "articleId", articleId != null ? articleId : -1,
                "provider", aiProperties.getDefaultProvider(),
                "model", chatReq.getModel()
        ));
        sendSse(emitter, "progress", Map.of("stage", "已提交，正在排队执行", "percent", 5));

        try {
            llmService.streamChatAsync(
                    chatReq,
                    token -> {
                        received.append(token);
                        int s = seq.incrementAndGet();
                        sendSse(emitter, "token", Map.of(
                                "text", token,
                                "seq", s
                        ));
                        if (s % 8 == 1) {
                            sendSse(emitter, "progress", Collections.singletonMap("percent", Math.min(80, 10 + s)));
                        }
                    },
                    full -> {
                        sendSse(emitter, "progress", Map.of("stage", "整理结果中", "percent", 95));
                        Map<String, Object> donePayload = new HashMap<>();
                        donePayload.put("text", full.getText() != null ? full.getText() : received.toString());
                        Map<String, Object> usage = new HashMap<>();
                        usage.put("promptTokens", full.getPromptTokens() != null ? full.getPromptTokens() : 0);
                        usage.put("completionTokens", full.getCompletionTokens() != null ? full.getCompletionTokens() : 0);
                        usage.put("totalTokens", full.getTotalTokens() != null ? full.getTotalTokens() : 0);
                        donePayload.put("usage", usage);
                        donePayload.put("latencyMs", full.getLatencyMs() != null ? full.getLatencyMs() : 0);
                        donePayload.put("logId", -1L);
                        donePayload.put("requestId", full.getRequestId() != null ? full.getRequestId() : "");
                        sendSse(emitter, "done", donePayload);
                        emitter.complete();
                    },
                    err -> {
                        String code = "UNKNOWN";
                        String msg = err.getMessage() != null ? err.getMessage() : err.getClass().getSimpleName();
                        if (msg.contains("KEY_EMPTY") || msg.contains("NoApiKey")) code = "KEY_INVALID";
                        else if (msg.contains("QUOTA") || msg.contains("quota")) code = "QUOTA_EXHAUSTED";
                        else if (msg.contains("timeout") || msg.contains("Timeout")) code = "PROVIDER_TIMEOUT";
                        else if (msg.contains("429") || msg.contains("rate") || msg.contains("RateLimit")) code = "PROVIDER_RATE_LIMIT";
                        else if (msg.contains("AI 功能已全局关闭") || msg.contains("未找到默认 Provider")) code = "AI_DISABLED";
                        else if (msg.contains("BAD_PARAMS")) code = "BAD_PARAMS";
                        sendSseError(emitter, code, msg, true);
                        emitter.complete();
                    }
            );
        } catch (BusinessException e) {
            sendSseError(emitter, e.getMessage().startsWith("AI") ? "AI_DISABLED" : "NO_PROVIDER_ENABLED", e.getMessage(), false);
            emitter.complete();
        } catch (Throwable t) {
            sendSseError(emitter, "UNKNOWN", t.getClass().getSimpleName() + ": " + t.getMessage(), false);
            emitter.complete();
        }

        emitter.onTimeout(() -> {
            log.warn("[AI][SSE] 客户端超时关闭 taskType={}", type);
            sendSseError(emitter, "PROVIDER_TIMEOUT", "服务端流式等待超时，请重试", false);
            emitter.complete();
        });
        emitter.onCompletion(() -> log.debug("[AI][SSE] 完成 taskType={}, receivedLen={}", type, received.length()));
        emitter.onError(t -> log.warn("[AI][SSE] 客户端异常断开 taskType={}: {}", type, t.toString()));

        return emitter;
    }

    private SseEmitter immediatelyErrorEmitter(String code, String message) {
        SseEmitter emitter = new SseEmitter(10000L);
        sendSseError(emitter, code, message, false);
        emitter.complete();
        return emitter;
    }

    private void sendSse(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event)
                    .data(om.writeValueAsString(data)));
        } catch (IOException e) {
            log.warn("[AI][SSE] send {} 失败，客户端可能已断开: {}", event, e.toString());
        }
    }

    private void sendSseError(SseEmitter emitter, String code, String message, boolean retryable) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("code", code);
            payload.put("message", message);
            payload.put("retryable", retryable);
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(om.writeValueAsString(payload)));
        } catch (IOException ignore) {}
    }

    private String mask(String key) {
        if (!StringUtils.hasText(key)) return "";
        if (key.length() <= 8) return key.charAt(0) + "***";
        return key.substring(0, 4) + "***" + key.substring(key.length() - 4);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiConfigDTO {
        private boolean enabled;
        private String defaultProvider;
        private String providerName;
        private String model;
        private String apiKeyMasked;
        private boolean apiKeySet;
        private int timeoutSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResultDTO {
        private boolean ok;
        private String provider;
        private String model;
        private Long latencyMs;
        private String text;
        private String requestId;
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
        private String error;
    }
}
