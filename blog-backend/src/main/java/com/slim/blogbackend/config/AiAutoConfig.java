package com.slim.blogbackend.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfig {

    @Autowired
    private AiProperties aiProperties;

    @PostConstruct
    public void onInit() {
        if (!aiProperties.isEnabled()) {
            log.info("[AI] novablog.ai.enabled=false，AI 功能全局关闭");
            return;
        }
        AiProperties.AliyunProperties aliyun = aiProperties.getAliyun();
        boolean hasKey = aliyun.getApiKey() != null && !aliyun.getApiKey().isBlank();
        String keyMasked = hasKey
                ? aliyun.getApiKey().substring(0, Math.min(4, aliyun.getApiKey().length())) + "***"
                : "(空)";
        log.info(
                "[AI] 配置已加载：defaultProvider={}, aliyun.model={}, aliyun.apiKey={}, endpoint={}, timeout={}s",
                aiProperties.getDefaultProvider(),
                aliyun.getModel(),
                keyMasked,
                aliyun.getEndpoint(),
                aliyun.getTimeoutSeconds()
        );
        if (!hasKey) {
            log.warn("[AI] ALIYUN_API_KEY 环境变量未设置或为空。AI 请求将失败。请在启动环境里 export ALIYUN_API_KEY=sk-xxx");
        }
    }
}
