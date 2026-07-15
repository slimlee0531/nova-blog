package com.slim.blogbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "novablog.ai")
public class AiProperties {

    private boolean enabled = true;

    private String defaultProvider = "aliyun";

    private AliyunProperties aliyun = new AliyunProperties();

    @Data
    public static class AliyunProperties {
        private String apiKey = "";
        private String model = "qwen-plus";
        private String endpoint = "https://dashscope-intl.aliyuncs.com/api/v1";
        private int timeoutSeconds = 30;
        private int maxOutputTokens = 2048;
        private double temperature = 0.7;
    }
}
