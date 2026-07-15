package com.slim.blogbackend.ai.service;

import com.slim.blogbackend.ai.dto.LlmChatRequest;
import com.slim.blogbackend.ai.dto.LlmChatResponse;
import com.slim.blogbackend.ai.enums.AiProviderEnum;
import com.slim.blogbackend.ai.spi.LlmProvider;
import com.slim.blogbackend.config.AiProperties;
import com.slim.blogbackend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@Service
public class LlmService {

    private final Map<AiProviderEnum, LlmProvider> providers = new HashMap<>();

    private final ExecutorService streamExecutor = Executors.newFixedThreadPool(8, r -> {
        Thread t = new Thread(r, "ai-stream-" + r.hashCode());
        t.setDaemon(true);
        return t;
    });

    @Autowired
    private AiProperties aiProperties;

    public LlmService(List<LlmProvider> providerList) {
        for (LlmProvider p : providerList) {
            providers.put(p.provider(), p);
        }
    }

    public LlmProvider getDefaultProvider() {
        if (!aiProperties.isEnabled()) {
            throw new BusinessException("AI 功能已全局关闭，请先在 application.yml 中开启 novablog.ai.enabled=true");
        }
        AiProviderEnum provider = AiProviderEnum.of(aiProperties.getDefaultProvider());
        LlmProvider impl = providers.get(provider);
        if (impl == null) {
            throw new BusinessException("未找到默认 Provider 实现：" + provider.getCode());
        }
        return impl;
    }

    public LlmChatResponse simpleChat(String userPrompt, String systemPrompt) {
        if (!aiProperties.isEnabled()) {
            throw new BusinessException("AI 功能已全局关闭");
        }
        LlmChatRequest req = LlmChatRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .build();
        return getDefaultProvider().chat(req);
    }

    public LlmChatResponse testConnection() {
        LlmProvider provider = getDefaultProvider();
        LlmChatRequest req = LlmChatRequest.builder()
                .systemPrompt("你是一个连通性测试助手")
                .userPrompt("请只回复一个单词 pong")
                .maxOutputTokens(16)
                .temperature(0.0)
                .build();
        return provider.chat(req);
    }

    public void streamChatAsync(
            LlmChatRequest request,
            Consumer<String> onToken,
            Consumer<LlmChatResponse> onDone,
            Consumer<Throwable> onError
    ) {
        LlmProvider provider = getDefaultProvider();
        streamExecutor.submit(() -> provider.streamChat(request, onToken, onDone, onError));
    }
}
