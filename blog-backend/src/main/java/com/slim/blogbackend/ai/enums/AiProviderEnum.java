package com.slim.blogbackend.ai.enums;

public enum AiProviderEnum {
    ALIYUN("aliyun", "阿里云 DashScope / 通义千问");

    private final String code;
    private final String label;

    AiProviderEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static AiProviderEnum of(String code) {
        if (code == null) return ALIYUN;
        for (AiProviderEnum v : values()) {
            if (v.code.equalsIgnoreCase(code)) return v;
        }
        return ALIYUN;
    }
}
