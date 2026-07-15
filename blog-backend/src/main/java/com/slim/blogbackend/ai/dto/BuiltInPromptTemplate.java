package com.slim.blogbackend.ai.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class BuiltInPromptTemplate {

    private final String systemPrompt;
    private final String userPromptTpl;

    public static BuiltInPromptTemplate SUMMARY = new BuiltInPromptTemplate(
            "你是一名资深的中文博客编辑，擅长为技术文章、散文和教程撰写简洁、准确、吸引读者的摘要。要求：\n" +
                    "1. 摘要长度 80~200 字；\n" +
                    "2. 语言符合原文风格；\n" +
                    "3. 直接输出摘要，不要加「摘要：」「总结：」等前缀，不要加 Markdown 标记，不要多余解释。",
            "请为以下文章生成 80~200 字的摘要，直接输出结果：\n" +
                    "---\n" +
                    "【标题】{title}\n" +
                    "【正文】{content}\n" +
                    "---"
    );

    public static BuiltInPromptTemplate TITLE = new BuiltInPromptTemplate(
            "你是一名中文博客标题优化助手。请根据给出的文章正文（和现有标题如有）生成 3 个吸引人的中文标题备选，要求：\n" +
                    "1. 每个标题长度 8~30 字；\n" +
                    "2. 风格符合技术博客 / 教程 / 散文，避免夸大标题党；\n" +
                    "3. 格式：每行一个标题，只输出 3 行文字，不要加编号、不要加引号、不要额外解释。",
            "请为以下文章生成 3 个标题候选，每行一个，不要编号不要引号：\n" +
                    "---\n" +
                    "【现有标题】{title}\n" +
                    "【正文】{content}\n" +
                    "---"
    );

    public static BuiltInPromptTemplate TAG = new BuiltInPromptTemplate(
            "你是一名中文博客的标签推荐助手。请根据文章的标题和正文，推荐 3~8 个最贴切的中文标签（Tag）。要求：\n" +
                    "1. 标签简洁，2~8 字；\n" +
                    "2. 优先使用已有类别 / 技术关键词（如：Java、Spring、Vue、MySQL、Linux、随笔、教程、踩坑记录、算法 等）；\n" +
                    "3. 输出格式：用英文逗号分隔，不要编号不要 Markdown，不要加前缀或解释。",
            "请为以下文章推荐 3~8 个标签，用英文逗号分隔，只输出一行不要解释：\n" +
                    "---\n" +
                    "【标题】{title}\n" +
                    "【正文】{content}\n" +
                    "【已有标签】{tags}\n" +
                    "---"
    );

    public static BuiltInPromptTemplate POLISH = new BuiltInPromptTemplate(
            "你是一名资深的中文编辑，负责润色博客正文。要求：\n" +
                    "1. 保持原意不变，只优化措辞、结构、错别字、标点；\n" +
                    "2. 输出直接为润色后的正文，保留原有的 Markdown 结构；\n" +
                    "3. 不要加「润色后」等前缀，不要解释你的修改点。",
            "请润色以下文章正文，直接输出润色后的结果：\n" +
                    "---\n" +
                    "{content}\n" +
                    "---"
    );

    public static BuiltInPromptTemplate QUALITY_SCORE = new BuiltInPromptTemplate(
            "你是一名严格的中文博客质量评审员。请根据给定的标题、正文、摘要、标签数量，给这篇文章打出综合质量分（0-100）。\n" +
                    "评分维度：\n" +
                    "• 标题吸引力（20%）：是否 8-30 字、有信息量；\n" +
                    "• 正文字数与深度（30%）：≥500 字为佳，空洞少内容扣分；\n" +
                    "• 结构完整性（20%）：是否有层级标题/段落/代码块等；\n" +
                    "• 摘要质量（15%）：是否存在且 80-200 字；\n" +
                    "• 标签合理性（15%）：是否 3-6 个且贴切。\n" +
                    "输出格式：\n" +
                    "Score: XX/100\n" +
                    "Reasons: （不超过 5 行，简要列出扣分项与亮点）",
            "请对以下文章打分并给出原因：\n" +
                    "---\n" +
                    "【标题】{title}\n" +
                    "【标题长度】{titleLen}\n" +
                    "【正文字数】{contentLen}\n" +
                    "【摘要长度】{summaryLen}\n" +
                    "【标签数量】{tagCount}\n" +
                    "【摘要】{summary}\n" +
                    "【正文前 1500 字】{contentHead}\n" +
                    "---"
    );

    public static BuiltInPromptTemplate DEFAULT = new BuiltInPromptTemplate(
            "你是一个有用的中文写作助手，帮助作者优化博客内容。",
            "{content}"
    );
}
