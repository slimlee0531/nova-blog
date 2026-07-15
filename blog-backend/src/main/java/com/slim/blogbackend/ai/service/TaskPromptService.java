package com.slim.blogbackend.ai.service;

import com.slim.blogbackend.ai.dto.BuiltInPromptTemplate;
import com.slim.blogbackend.ai.dto.LlmChatRequest;
import com.slim.blogbackend.ai.enums.AiTaskTypeEnum;
import com.slim.blogbackend.config.AiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaskPromptService {

    @Autowired
    private AiProperties aiProperties;

    public LlmChatRequest buildChatRequest(
            AiTaskTypeEnum taskType,
            String title,
            String content,
            String summary,
            String tags
    ) {
        BuiltInPromptTemplate tpl = resolveTemplate(taskType);
        Map<String, String> vars = buildVars(
                safe(title), safe(content), safe(summary), safe(tags)
        );
        String system = applyVars(tpl.getSystemPrompt(), vars);
        String user = applyVars(tpl.getUserPromptTpl(), vars);

        AiProperties.AliyunProperties cfg = aiProperties.getAliyun();
        return LlmChatRequest.builder()
                .systemPrompt(system)
                .userPrompt(user)
                .model(cfg.getModel())
                .maxOutputTokens(cfg.getMaxOutputTokens())
                .temperature(aiProperties.getAliyun().getTemperature())
                .stream(true)
                .build();
    }

    private BuiltInPromptTemplate resolveTemplate(AiTaskTypeEnum taskType) {
        if (taskType == null) return BuiltInPromptTemplate.DEFAULT;
        return switch (taskType) {
            case SUMMARY -> BuiltInPromptTemplate.SUMMARY;
            case TITLE -> BuiltInPromptTemplate.TITLE;
            case TAG -> BuiltInPromptTemplate.TAG;
            case POLISH, REPHRASE, CONTINUE, PROOFREAD, TRANSLATE -> BuiltInPromptTemplate.POLISH;
            case QUALITY_SCORE -> BuiltInPromptTemplate.QUALITY_SCORE;
            default -> BuiltInPromptTemplate.DEFAULT;
        };
    }

    private Map<String, String> buildVars(String title, String content, String summary, String tags) {
        int titleLen = title.length();
        int contentLen = content.length();
        int summaryLen = summary.length();
        int tagCount = 0;
        if (StringUtils.hasText(tags)) {
            tagCount = tags.split("[,，、\\s]+").length;
        }
        String contentClipped = content.length() > 6000 ? content.substring(0, 6000) + "\n...(正文过长，已截断取前 6000 字)" : content;
        String contentHead = content.length() > 1500 ? content.substring(0, 1500) : content;
        Map<String, String> vars = new HashMap<>();
        vars.put("title", safe(title).isEmpty() ? "(未填写)" : title);
        vars.put("content", safe(contentClipped).isEmpty() ? "(未填写正文)" : contentClipped);
        vars.put("contentHead", contentHead);
        vars.put("summary", safe(summary).isEmpty() ? "(未填写摘要)" : summary);
        vars.put("tags", safe(tags).isEmpty() ? "(无)" : tags);
        vars.put("titleLen", String.valueOf(titleLen));
        vars.put("contentLen", String.valueOf(contentLen));
        vars.put("summaryLen", String.valueOf(summaryLen));
        vars.put("tagCount", String.valueOf(tagCount));
        return vars;
    }

    private String applyVars(String tpl, Map<String, String> vars) {
        if (!StringUtils.hasText(tpl)) return "";
        String out = tpl;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
