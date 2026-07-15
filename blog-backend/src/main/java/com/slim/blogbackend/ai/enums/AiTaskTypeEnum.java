package com.slim.blogbackend.ai.enums;

public enum AiTaskTypeEnum {
    SUMMARY("SUMMARY", "摘要生成"),
    TITLE("TITLE", "标题生成"),
    TAG("TAG", "标签/分类推荐"),
    POLISH("POLISH", "正文润色"),
    CONTINUE("CONTINUE", "续写"),
    PROOFREAD("PROOFREAD", "纠错"),
    REPHRASE("REPHRASE", "风格改写"),
    QUALITY_SCORE("QUALITY_SCORE", "质量评分"),
    SEO_META("SEO_META", "SEO 元信息"),
    SUGGEST_OUTLINE("SUGGEST_OUTLINE", "大纲建议"),
    EXTRACT_ENTITIES("EXTRACT_ENTITIES", "实体抽取"),
    SPEED_READ("SPEED_READ", "AI 速览"),
    TRANSLATE("TRANSLATE", "翻译");

    private final String code;
    private final String label;

    AiTaskTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
