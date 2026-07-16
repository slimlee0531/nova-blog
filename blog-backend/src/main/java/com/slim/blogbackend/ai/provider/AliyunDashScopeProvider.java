package com.slim.blogbackend.ai.provider;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.protocol.Protocol;
import com.slim.blogbackend.ai.dto.LlmChatRequest;
import com.slim.blogbackend.ai.dto.LlmChatResponse;
import com.slim.blogbackend.ai.enums.AiProviderEnum;
import com.slim.blogbackend.ai.spi.LlmProvider;
import com.slim.blogbackend.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AliyunDashScopeProvider implements LlmProvider {

    @Autowired
    private AiProperties aiProperties;

    @Override
    public AiProviderEnum provider() {
        return AiProviderEnum.ALIYUN;
    }

    @Override
    public LlmChatResponse chat(LlmChatRequest request) {
        AiProperties.AliyunProperties cfg = aiProperties.getAliyun();
        String apiKey = resolveApiKey(request, cfg);
        if (!StringUtils.hasText(apiKey)) {
            return LlmChatResponse.builder()
                    .errorCode("KEY_EMPTY")
                    .errorMessage("ALIYUN_API_KEY 环境变量或配置 novablog.ai.aliyun.api-key 为空")
                    .build();
        }

        String model = StringUtils.hasText(request.getModel()) ? request.getModel() : cfg.getModel();
        int maxTokens = request.getMaxOutputTokens() != null ? request.getMaxOutputTokens() : cfg.getMaxOutputTokens();
        double temperature = request.getTemperature() != null ? request.getTemperature() : cfg.getTemperature();

        long start = System.currentTimeMillis();
        try {
            if (isMultimodalModel(model)) {
                return callMultimodalModel(apiKey, model, request.getSystemPrompt(), request.getUserPrompt(),
                        maxTokens, temperature, cfg.getEndpoint(), start);
            } else {
                return callTextModel(apiKey, model, request.getSystemPrompt(), request.getUserPrompt(),
                        maxTokens, temperature, cfg.getEndpoint(), start);
            }
        } catch (ApiException | NoApiKeyException | InputRequiredException | UploadFileException e) {
            long latency = System.currentTimeMillis() - start;
            String errCode = e instanceof NoApiKeyException ? "NO_API_KEY"
                    : e instanceof InputRequiredException ? "INPUT_REQUIRED" : "API_ERROR";
            String errMsg = e.getMessage();
            log.warn(
                    "[AI][ALIYUN] 请求失败：errCode={} latency={}ms message={}",
                    errCode, latency, errMsg
            );
            return LlmChatResponse.builder()
                    .errorCode(errCode)
                    .errorMessage(errMsg)
                    .latencyMs(latency)
                    .build();
        } catch (RuntimeException e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[AI][ALIYUN] 未预期异常：latency={}ms", latency, e);
            return LlmChatResponse.builder()
                    .errorCode("INTERNAL_ERROR")
                    .errorMessage(e.getClass().getSimpleName() + ": " + e.getMessage())
                    .latencyMs(latency)
                    .build();
        }
    }

    private boolean isMultimodalModel(String model) {
        if (model == null) return false;
        String lower = model.toLowerCase();
        return lower.contains("qwen3.7") || lower.contains("qwen3-vl") || lower.contains("qwen-vl");
    }

    private LlmChatResponse callTextModel(String apiKey, String model, String systemPrompt, String userPrompt,
                                          int maxTokens, double temperature, String endpoint, long startTime)
            throws ApiException, NoApiKeyException, InputRequiredException {
        List<Message> messages = new ArrayList<>(2);
        if (StringUtils.hasText(systemPrompt)) {
            messages.add(Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt)
                    .build());
        }
        messages.add(Message.builder()
                .role(Role.USER.getValue())
                .content(userPrompt)
                .build());

        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(messages)
                .maxTokens(maxTokens)
                .temperature((float) temperature)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        Generation gen = StringUtils.hasText(endpoint)
                ? new Generation(Protocol.HTTP.getValue(), endpoint)
                : new Generation();
        GenerationResult result = gen.call(param);

        long latency = System.currentTimeMillis() - startTime;
        String text = extractText(result);
        Integer in = result.getUsage() != null ? result.getUsage().getInputTokens() : null;
        Integer out = result.getUsage() != null ? result.getUsage().getOutputTokens() : null;
        Integer total = (in != null && out != null) ? (in + out) : null;

        log.debug(
                "[AI][ALIYUN] 请求成功：model={} promptTokens={} completionTokens={} latency={}ms requestId={}",
                model, in, out, latency, result.getRequestId()
        );
        return LlmChatResponse.builder()
                .text(text)
                .promptTokens(in)
                .completionTokens(out)
                .totalTokens(total)
                .latencyMs(latency)
                .finishReason(result.getOutput() != null
                        ? String.valueOf(result.getOutput().getFinishReason())
                        : null)
                .requestId(result.getRequestId())
                .build();
    }

    private LlmChatResponse callMultimodalModel(String apiKey, String model, String systemPrompt, String userPrompt,
                                                 int maxTokens, double temperature, String endpoint, long startTime)
            throws ApiException, NoApiKeyException, InputRequiredException, UploadFileException {
        List<MultiModalMessage> messages = new ArrayList<>(2);

        if (StringUtils.hasText(systemPrompt)) {
            Map<String, Object> systemContent = new HashMap<>(1);
            systemContent.put("text", systemPrompt);
            messages.add(MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(Collections.singletonList(systemContent))
                    .build());
        }

        Map<String, Object> userContent = new HashMap<>(1);
        userContent.put("text", userPrompt);
        messages.add(MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Collections.singletonList(userContent))
                .build());

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(messages)
                .temperature((float) temperature)
                .build();

        MultiModalConversation conv = StringUtils.hasText(endpoint)
                ? new MultiModalConversation(Protocol.HTTP.getValue(), endpoint)
                : new MultiModalConversation();
        MultiModalConversationResult result = conv.call(param);

        long latency = System.currentTimeMillis() - startTime;
        String text = extractMultimodalText(result);
        Integer in = result.getUsage() != null ? result.getUsage().getInputTokens() : null;
        Integer out = result.getUsage() != null ? result.getUsage().getOutputTokens() : null;
        Integer total = (in != null && out != null) ? (in + out) : null;

        log.debug(
                "[AI][ALIYUN] 请求成功：model={} promptTokens={} completionTokens={} latency={}ms requestId={}",
                model, in, out, latency, result.getRequestId()
        );
        return LlmChatResponse.builder()
                .text(text)
                .promptTokens(in)
                .completionTokens(out)
                .totalTokens(total)
                .latencyMs(latency)
                .finishReason(result.getOutput() != null && result.getOutput().getChoices() != null && !result.getOutput().getChoices().isEmpty()
                        ? String.valueOf(result.getOutput().getChoices().get(0).getFinishReason())
                        : null)
                .requestId(result.getRequestId())
                .build();
    }

    private String extractMultimodalText(MultiModalConversationResult result) {
        if (result == null || result.getOutput() == null || result.getOutput().getChoices() == null || result.getOutput().getChoices().isEmpty()) {
            return null;
        }
        var choice = result.getOutput().getChoices().get(0);
        if (choice.getMessage() == null || choice.getMessage().getContent() == null || choice.getMessage().getContent().isEmpty()) {
            return null;
        }
        Object contentObj = choice.getMessage().getContent().get(0);
        if (contentObj instanceof Map<?, ?> map) {
            Object text = map.get("text");
            if (text != null) return text.toString();
        }
        return contentObj.toString();
    }

    private String extractText(GenerationResult result) {
        if (result == null || result.getOutput() == null || result.getOutput().getChoices() == null || result.getOutput().getChoices().isEmpty()) {
            return null;
        }
        var choice = result.getOutput().getChoices().get(0);
        if (choice.getMessage() == null) return null;
        Object content = choice.getMessage().getContent();
        if (content == null) return null;
        if (content instanceof String s) return s;
        return content.toString();
    }

    private String resolveApiKey(LlmChatRequest request, AiProperties.AliyunProperties cfg) {
        if (request != null && request instanceof CustomApiKeyProvider provider && StringUtils.hasText(provider.getOverrideApiKey())) {
            return provider.getOverrideApiKey();
        }
        return cfg.getApiKey();
    }

    public interface CustomApiKeyProvider {
        String getOverrideApiKey();
    }
}
