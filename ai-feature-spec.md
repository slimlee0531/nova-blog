# NovaBlog AI 功能需求规格说明书（SPEC）

> 版本：v1.0
> 日期：2026-07-15
> 适用范围：NovaBlog 博客系统（Spring Boot 3 + Vue 3 + TypeScript + Element Plus）
> 读者：产品、前端开发、后端开发、测试

---

## 1. 文档概览

### 1.1 背景
NovaBlog 已在 Article 实体与前后端 DTO 中预留了 AI 相关字段（`aiSummary / aiKeywords / aiSuggestedTags / structuredData / entities / aiReadingTime / aiContentQualityScore`），并预留了 `AiGenerationLog` 实体。本次目标是在现有预留基础上，补齐一套「可用、可扩展、可观测」的博客 AI 功能。

### 1.2 目标
| 编号 | 目标 | 量化描述 |
| --- | --- | --- |
| G1 | 降低作者写作门槛 | 新文章从「空白」到「草稿 + 标题 + 摘要 + 推荐标签」的平均耗时 ≤ 2 分钟 |
| G2 | 提升读者阅读体验 | 长文打开后 3 秒内可看到 AI 速览，读者决策是否继续阅读 |
| G3 | 保证内容质量 | 发布前自动给出「质量分 + 问题清单 + 优化建议」，分数低于阈值的文章禁止发布（可选开关） |
| G4 | 可观测 & 易运营 | 管理后台 1 分钟内看到 24h AI 调用量、失败率、热门模型、Top 作者 |

### 1.3 范围与里程碑
本次交付分为两个里程碑，**V1 不开发读者端 AI**，先把作者端 + 管理后台做通；V2 再补齐读者端。

| 里程碑 | 范围 | 状态 |
| --- | --- | --- |
| V1 | 作者端写作辅助 + 质量评估 + 管理后台 AI 配置/监控 | 当前规划 |
| V2 | 读者端 AI 速览 / 实体解释 / 智能翻译 / 相关推荐 | 下一阶段 |
| 不在范围（Out of Scope） | 评论 AI 审核、全站 RAG 问答、AI 自动发文、额度/计费、国外模型（OpenAI 等） | ❌ 本次不做 |

### 1.4 已确认的关键决策
1. **功能范围**：写作辅助（核心）+ 阅读增强（V2）+ 质量评估
2. **模型接入**：仅国内大模型（通义千问、豆包、Kimi、DeepSeek、文心一言、智谱 GLM），管理后台可切换
3. **作者端 UI**：写文章页面「右侧 AI 侧边栏」
4. **优先级**：先作者端（V1），再读者端（V2）
5. **计费策略**：管理员统一填 Key，所有作者免费使用，无额度限制
6. **响应方式**：SSE 流式输出，前端打字机效果
7. **写入策略**：AI 生成结果先在前端预览，作者点「应用」才写入数据库字段
8. **读者端鉴权**：匿名可访问读者 AI 功能（V2 实施时补 rate limit）
9. **管理后台 AI 面板**：配置 + 监控双面板（连通性测试、调用日志、调用量图表）

---

## 2. 术语表

| 术语 | 含义 |
| --- | --- |
| Provider | 国内大模型供应商（如 dashscope=通义千问、volcengine=豆包、moonshot=Kimi、deepseek、qianfan=文心一言、zhipu=智谱） |
| TaskType | 一次 AI 生成的任务类型枚举（SUMMARY / TITLE / TAG / POLISH / CONTINUE / PROOFREAD / REPHRASE / QUALITY_SCORE / EXTRACT_ENTITIES / TRANSLATE / SPEED_READ） |
| SSE | Server-Sent Events，HTTP 长连接，服务端一边收 token 一边推给前端 |
| SseEmitter | Spring Web 原生 SSE 组件，后端流式返回 |
| Prompt 模板 | 每种 TaskType 对应的系统提示词（System Prompt）+ 变量槽位，可在管理后台编辑 |
| AiGenerationLog | 每次 AI 调用的审计日志，落数据库表 |
| 质量分（Quality Score） | 0-100 整数，多维度加权，发布前警告用 |

---

## 3. 角色与用例

### 3.1 角色
| 角色 | 身份 | 能做的 AI 操作 |
| --- | --- | --- |
| 作者（Author） | 已登录、有写文章权限 | 写文章页所有写作辅助 + 质量评估（V1）；阅读 AI（V2） |
| 读者（Reader） | 匿名或登录访客 | 仅 V2 的阅读增强 AI 功能 |
| 管理员（Admin） | 有管理后台权限 | 配置模型 Key、编辑 Prompt 模板、查看调用日志/统计、导出日志 CSV |

### 3.2 V1 用例清单（作者端）
| ID | 用例 | 触发路径 | 核心输出 | 写库 |
| --- | --- | --- | --- | --- |
| UC-W-01 | AI 生成标题 | 侧边栏 → 输入草稿/正文 → 生成标题（给 3 个候选） | 候选标题列表 | 用户点"设为标题" → `Article.title` |
| UC-W-02 | AI 生成摘要 | 侧边栏 → 生成摘要（要求 80-150 字、3 句以内） | 摘要正文 | 用户点"应用摘要" → `Article.aiSummary`，同步覆盖 `summary`（让摘要在列表也展示） |
| UC-W-03 | AI 推荐标签 & 分类 | 侧边栏 → 分析正文 → 最多 5 个标签 + 1 个分类建议 | 标签名列表（带置信度）+ 分类名 | 用户点"全部应用" → 写回 `Article.tagIds` 和 `categoryId`；同时如果标签名不存在，先自动创建 Tag（颜色随机） |
| UC-W-04 | AI 正文润色 | 选中一段正文（≥20 字）→ 润色（更流畅/更正式/更口语化） | 润色后的整段文字 | 用户点"替换选中文本" → Markdown 编辑器选区被替换 |
| UC-W-05 | AI 续写 | 光标位置 → 续写（200-500 字） | 续写文本（打字机流式） | 用户点"插入到光标位置" → 写进编辑器 |
| UC-W-06 | AI 纠错（错别字/病句） | 选一段 → 纠错 | 纠错结果 + 问题清单（原词 → 推荐词） | 用户可逐条应用或全部替换 |
| UC-W-07 | AI 改写（风格切换） | 选一段 → 选风格（技术 → 通俗 / 学术 → 博客 / 客观 → 有观点） | 改写文本 | 用户点"替换选中文本" |
| UC-W-08 | AI 质量评分（发布前体检） | 保存或发布前自动触发 / 手动点"体检" | 质量分 + 维度分 + 问题清单 + 建议 | 分数 → `aiContentQualityScore`；结构化报告存 JSON 到 `structuredData` 的 `qualityReport` 字段；发布时若低于「最小分」且开关开启则拦截 |

### 3.3 V1 用例清单（管理端）
| ID | 用例 | 触发路径 | 核心输出 |
| --- | --- | --- | --- |
| UC-A-01 | AI 模型配置 | 管理后台 → AI 设置 → 模型配置页 | 选择 Provider / 填 API Key / 默认模型 / 超时时间（默认 30s）/ 最大 token / 保存 & 连通性测试（测试对话一次"你好"，结果展示耗时） |
| UC-A-02 | Prompt 模板管理 | 管理后台 → AI 设置 → Prompt 模板 Tab | 按 TaskType 列表展示模板，可编辑变量、保存、"恢复默认" |
| UC-A-03 | 质量评估规则配置 | 管理后台 → AI 设置 → 质量评分 Tab | 权重（可读性 30% + 结构 20% + SEO 20% + 原创度 20% + 合规 10%）、"发布前强制体检"开关、「最小发布分」阈值（默认 60） |
| UC-A-04 | 调用日志查看 | 管理后台 → AI 监控 → 日志 | 可按 TaskType / Provider / 状态 / 时间 / 用户 筛选的表格；点击行展开完整 Prompt + 返回 + 错误栈 |
| UC-A-05 | 调用量图表 | 管理后台 → AI 监控 → 大盘 | 24h 折线图（总调用、成功、失败）、饼图（TaskType 分布）、饼图（Provider 分布）、Top 5 作者排行、失败率 Top 3 TaskType |
| UC-A-06 | 日志导出 CSV | 监控页 → 筛选后 → 导出 | CSV 文件下载，供审计 |

### 3.4 V2 用例清单（读者端）
| ID | 用例 | 触发路径 | 核心输出 |
| --- | --- | --- | --- |
| UC-R-01 | AI 速览（AI 摘要） | 详情页顶部「AI 速览」折叠卡片 | 3 点结论 + 关键数据 + 一句推荐语；存 `aiSummary`（如作者已经写了，就不重复调用） |
| UC-R-02 | 实体 / 术语高亮解释 | 正文出现 entities 里的词时鼠标悬浮或加角标 | 弹窗解释 + "相关文章"链接（可跳转）；entities 字段存抽取出的实体 JSON |
| UC-R-03 | 全文翻译 | 详情页右上角「切换语言：中 → 英 / 英 → 中」 | 整文翻译，前端用临时状态存（不写库，避免脏数据） |
| UC-R-04 | 相关文章智能推荐 | 详情页底部「AI 为你推荐」3 篇 | 基于 structuredData 里的关键词向量匹配（V2 简单版用 SQL 关键词 LIKE；后面再升级向量库） |
| UC-R-05 | 朗读时长预测 | 已预留字段 `aiReadingTime`，已有实现用 `Math.ceil(length/500)` 估算，V2 升级为按中文 400 字/分钟、英文 180 词/分钟智能判断 |

---

## 4. 功能需求详述（V1 必须完成）

### 4.1 作者端 — 写文章页右侧 AI 侧边栏

#### 4.1.1 交互结构
```
┌──────────────────────────────────────────────┬───────────┐
│ Markdown 编辑器 (2/3 宽度)                   │ AI 侧边栏 │
│                                              │           │
│  [标题输入]                                   │  Tab 切换 │
│                                              │ · 写作助手│
│  [正文]                                       │ · 体检    │
│     (选区高亮)                                │ · 历史    │
│                                              │           │
│  ...                                          │ [任务卡]  │
│                                              │  参数区   │
│                                              │  生成按钮 │
│                                              │  (结果区) │
│                                              │  应用按钮 │
└──────────────────────────────────────────────┴───────────┘
```

- **默认折叠**：侧边栏默认 360px 宽，可拖拽至 480px / 折叠为 0px
- **三 Tab**：写作助手（默认） / 质量体检 / 历史生成
- **选区联动**：当用户在 Markdown 编辑器选中文字时，写作助手 Tab 自动显示"当前选中：XX 字"，并默认选中润色/纠错/改写任务
- **任务卡模板**：每个 TaskType 一张卡片，描述 + 参数（如润色有"风格"下拉、续写有"字数"滑块）+「生成」按钮
- **结果区**：
  - 流式打字机逐 token 显示
  - 未完成时显示「停止生成」按钮
  - 完成后显示「应用 / 复制 / 重新生成」按钮
  - 生成标题任务：3 个候选并列显示，每个旁都有「设为标题」按钮
- **历史 Tab**：当前文章的本次会话内所有 AI 生成结果（按时间倒序），便于对比

#### 4.1.2 任务参数表
| TaskType | 参数 | 默认值 | 校验 |
| --- | --- | --- | --- |
| TITLE | 候选数量（1/3/5）、语气（正式/活泼/技术） | 3 个、技术 | 正文 ≥ 50 字才允许 |
| SUMMARY | 字数（80/150/300） | 150 | 正文 ≥ 200 字 |
| TAG | 数量上限（3/5/8） | 5 | 正文 ≥ 100 字 |
| POLISH | 风格（流畅/正式/口语化/简洁） | 流畅 | 必须有选区，且 20 ≤ len ≤ 5000 |
| CONTINUE | 字数（200/500/1000） | 500 | 光标必须在编辑器内 |
| PROOFREAD | / | / | 必须有选区 |
| REPHRASE | 目标风格（5 选 1） | 技术→通俗 | 必须有选区 |
| QUALITY_SCORE | / | / | 正文 ≥ 300 字才跑，少于则跳过评分并提示 |

#### 4.1.3 写入策略（已确认：作者点应用才落库）
- **SSE 响应过程**：后端 `SseEmitter` 一边推 token 一边在完成后推送 `[DONE]` 事件 + 最终完整文本
- **前端暂存**：结果完整收到后先存在 Pinia Store（`aiStore.generations[]`）里，关联 `articleId + taskType + timestamp`
- **作者点「应用」**：
  - 标题 → 直接调 `PATCH /api/admin/articles/{id}` 只改 `title`
  - 摘要 → 改 `aiSummary` **同时覆盖 `summary`**（保证首页摘要一致）
  - 标签 / 分类 → 改 `tagIds` + `categoryId`；标签名不存在的自动 `POST /api/admin/tag` 新建（颜色从预设色板顺序挑）
  - 润色 / 续写 / 纠错 / 改写 → 只在前端 Markdown 编辑器操作，不单独落库；文章整体「保存」时一并写入

### 4.2 质量评分模型（V1 规则版，V2 可升级为 LLM-as-Judge）
V1 先实现「规则 + 关键词 + 简单结构」的可解释评分，避免纯 LLM 打分不稳定且耗 token。

总分 100 = 可读性 30 + 结构 20 + SEO 20 + 原创感 20 + 合规 10

| 维度 | 权重 | V1 算法 | 输出示例 |
| --- | --- | --- | --- |
| 可读性（Readability） | 30 | 平均句长 / 段落长度分布 / 标点完整率 | 句长过长扣分项 + 总分 |
| 结构（Structure） | 20 | 是否有 H2/H3、列表、粗体、引用块占比 | "缺少小标题 -5" |
| SEO | 20 | 标题 8-30 字 ✓ / 摘要存在 ✓ / 关键词 ≥ 3 个 ✓ / slug 含关键词 ✓ / 图至少一张 ✓ | 每项 4 分 |
| 原创感（Originality） | 20 | 与本站已有 10 篇文章段落级 Jaccard 相似度 ≤ 0.2 满分；>0.5 打 0 | "与《XX》相似度 37%，建议改写" |
| 合规（Compliance） | 10 | 敏感词库（可在后台编辑）命中，命中 1 个直接 0 分 + 发布拦截 | "命中敏感词：XXX，请修改" |

**拦截规则**：当管理后台开启「发布前强制体检」且 `qualityScore < minPublishScore`（默认 60）时，点发布弹出体检报告并拒绝提交；作者可手动勾选"忽略仍发布"（是否允许忽略，管理后台可开关）。

### 4.3 管理后台 AI 设置页

#### 4.3.1 模型配置页
表单：
- **启用 Provider**（多选框）：dashscope / volcengine / moonshot / deepseek / qianfan / zhipu
- **当前默认 Provider**：单选下拉（只能从已启用里选）
- 每选中一个 Provider 展开子表单：`API Key`（密码输入，隐藏显示按钮） + `Endpoint`（一般可留空，走官方默认） + `默认模型名`（下拉：qwen-plus / doubao-pro / moonshot-v1-8k / deepseek-chat / ernie-4.0 / glm-4 等） + `超时(秒，默认30)` + `最大输出 token（默认 2048）`
- **测试连通性**按钮：调用一次 `你好，请只回复 pong`，成功显示「成功，耗时 XXX ms」；失败显示错误栈 JSON

#### 4.3.2 Prompt 模板页
列表（10 行 = 10 个 TaskType + 2 个 V2 预留）：
| 列 | 内容 |
| --- | --- |
| TaskType | 不可编辑 |
| 名称（中文） | 可编辑（如 SUMMARY = 摘要生成） |
| System Prompt | 多行文本框，变量用 `{占位符}`，支持 `{title}` `{content}` `{tags}` `{style}` `{wordCount}` |
| User Prompt 模板 | 同上，例如：`请基于以下文章内容生成 {wordCount} 字中文摘要：\n{content}` |
| 操作 | 保存 / 恢复默认 / 预览变量填充效果 |

默认模板版本号内置在代码里，"恢复默认"按钮能一键回到出厂模板。

#### 4.3.3 调用监控大盘
- **顶部指标卡（6 个）**：24h 调用量、成功数、失败数、失败率、平均耗时、token 消耗总计
- **折线图**：按小时粒度，最近 24h / 7d 切换
- **饼图 3 个**：TaskType 分布、Provider 分布、状态分布（成功 / 失败 / 超时 / 中断）
- **Top 榜**：Top 5 作者、Top 3 失败 TaskType + 失败原因
- **日志表格**：
  - 列：时间、作者、文章ID、TaskType、Provider、模型、状态（badge 彩色）、耗时 ms、输入 tokens、输出 tokens、操作
  - 操作列：查看详情（弹窗展示完整 system prompt / user prompt / output / 错误堆栈）
  - 导出：按当前筛选条件导出 CSV

---

## 5. 非功能需求

### 5.1 性能
- **P95 首 token 延迟**：国内模型 ≤ 3s（含网络 + 推理）
- **P95 总耗时**：500 字输出任务 ≤ 15s；质量评分任务（规则版）≤ 2s
- **SSE 连接保活**：未收到 token 超过 15s 发送 `:keep-alive` 注释心跳，防止中间层断链
- **并发**：单机 100 并发 SSE 连接不 OOM（限制单次生成最多 4096 token）

### 5.2 安全 & 合规
- **API Key 存储（优先级）**：按「请求级自定义 Key > 数据库密文 > 系统环境变量」三级兜底。数据库层面存储 AES-256-GCM 加密后的密文（AES key 从环境变量 `NOVABLOG_AI_KEY_ENCRYPT_KEY` 读取）。**V1 默认 Provider 为阿里云 DashScope，直接从环境变量 `ALIYUN_API_KEY` 读取 Key**（对应 `application.yml` 的 `novablog.ai.aliyun.api-key: ${ALIYUN_API_KEY:}`），数据库自定义 Key 作为 V2 升级项。前端接口永不返回任何明文 Key，仅返回掩码（如 `sk-a8***f2c1`）
- **敏感词/合规**：用户输入给 LLM 的 Prompt 在发出去前过一遍本地敏感词库（合规扣分项用同一词库），命中则直接拒绝并返回「内容可能违规，请修改后重试」
- **防刷（读者端 V2）**：IP 维度 + 会话维度 rate limit，1 分钟 10 次，超限返回 429；V1 作者端免费不限制
- **日志脱敏**：AiGenerationLog 表中 userPrompt / output 如果命中 email/手机号/身份证等模式，用 `***` 打码后再落库
- **审计**：所有 AI 调用必须写 `AiGenerationLog`，哪怕用户没点「应用」也要写（因为消耗了 Key 资源）

### 5.3 可用性 / 降级
- **降级开关**：管理后台一键「关闭全部 AI 功能」（全局 switch）。关闭后侧边栏显示「AI 功能暂未启用」占位图，所有 API 返回 503；监控仍可用
- **单 Provider 故障**：默认 Provider 返回 4xx/5xx 连续失败 5 次，自动切到下一个已启用的 Provider（轮转式）；仪表盘显示「主 Provider 异常告警」红点
- **超时重试**：5xx 错误自动重试 1 次（指数退避 1s + 2s）；4xx（Key 错、额度用完）不重试，直接给用户显示"Key 失效，请联系管理员"

### 5.4 可观测性
- **AiGenerationLog** 表自带所有关键字段（见第 7 章）
- 管理后台监控页（4.3.3）展示所有指标
- 关键事件打 `log.info` / `log.error`，包含 traceId（MDC 注入），方便排查
- 失败场景告警：日志 ERROR 级 + 记录失败原因字段，监控页失败率 > 10% 显示警告 banner

### 5.5 可扩展性（给未来升级留口子）
- Provider 抽象：后端实现 `LlmProvider` SPI 接口，新增模型只加一个实现类，不改动业务代码
- TaskType 抽象：后端实现 `AiTaskHandler` SPI 接口，新增任务只要加 Handler + 在 Prompt 模板加一行
- 向量存储：V2 相关文章推荐要升级向量库（Milvus / pgvector）时，只在 `RelatedArticleService` 替换实现

---

## 6. 接口设计（HTTP + SSE）

### 6.1 统一前缀
V1 全部：`/api/admin/ai/...`（作者端，需登录鉴权 `ROLE_AUTHOR` 以上）  
V2 读者端：`/api/ai/...`（匿名，IP 限流）

### 6.2 通用响应结构
```ts
// 普通同步接口（Result<T> 沿用项目已有 Result 包装）
interface Result<T> { code: number; message: string; data: T }
```

### 6.3 普通同步接口
| 方法 | 路径 | 鉴权 | 功能 |
| --- | --- | --- | --- |
| GET | `/api/admin/ai/config/task-types` | Author | 返回当前支持的 TaskType 及参数定义（前端侧边栏动态渲染任务卡） |
| POST | `/api/admin/ai/config/test-provider` | Admin | 连通性测试：入参 provider + key + model，出参 `{ ok: boolean, latencyMs: number, error?: string }` |
| GET | `/api/admin/ai/prompts` | Admin | 获取所有 Prompt 模板（按 TaskType 为 key 的 map） |
| PUT | `/api/admin/ai/prompts/:taskType` | Admin | 保存/恢复单个模板 |
| GET | `/api/admin/ai/quality-config` | Admin | 获取质量评分权重/阈值/开关 |
| PUT | `/api/admin/ai/quality-config` | Admin | 保存质量评分配置 |
| POST | `/api/admin/ai/apply/:articleId` | Author | 作者点「应用」后正式写回文章字段（标题/摘要/tagIds/categoryId/aiSummary） |
| POST | `/api/admin/ai/quality/:articleId` | Author | 质量评分（同步接口，2s 内返回结构化 JSON 报告） |
| GET | `/api/admin/ai/logs` | Admin | 日志分页列表（筛选 + page/size） |
| GET | `/api/admin/ai/logs/stats` | Admin | 监控大盘聚合指标（返回 JSON：指标卡 / 折线图数据点 / 饼图数据 / Top 榜） |
| GET | `/api/admin/ai/logs/export` | Admin | 导出当前筛选条件 CSV（文件流） |
| GET | `/api/admin/ai/logs/:id` | Admin | 单条日志详情（展开完整 prompt/output/error） |

### 6.4 SSE 流式生成接口（核心）

**`GET /api/admin/ai/generate?articleId&taskType&...params`**（或 POST + SSEEmitter，推荐 POST 因为 body 大）

> 推荐 POST：`POST /api/admin/ai/generate`  
> Content-Type: application/json  
> Body: `{ articleId, taskType, params: { style, wordCount, selectedText, cursorPosition... } }`  
> 返回：`text/event-stream; charset=utf-8`

SSE 事件格式（每条 `event:` + `data:`）：
```
event: token
data: {"text":"今天","seq":1}

event: token
data: {"text":"我们","seq":2}

event: progress
data: {"stage":"思考中","percent":30}

event: done
data: {"text":"今天我们聊一聊...","usage":{"promptTokens":342,"completionTokens":147},"latencyMs":4123,"logId":39281}

event: error
data: {"code":"KEY_INVALID","message":"API Key 无效","retryable":false}
```

前端处理规则：
- `event: token` 追加到结果区（做打字机光标闪烁）
- `event: done` 把完整结果存 Pinia 并解锁「应用/复制/重生成」按钮
- `event: error` 结果区显示红色错误 + 重试按钮；若 `retryable=true` 自动重试 1 次
- 超时：前端 60s 定时器，没收到 done/error 自动终止并提示"生成超时"

### 6.5 完整数据流时序（作者点「生成摘要」为例）
1. 前端侧边栏点击"生成摘要" → `POST /api/admin/ai/generate`（taskType=SUMMARY，携带正文）
2. 后端 `AiService` → 读取默认 Provider → 从加密 DB 解密 Key → 构建 Prompt（System + User）
3. 调用国内大模型 HTTP 流式接口（如通义千问 SSE）→ 每收到 token 转译后本地 `SseEmitter.send()` 给前端
4. 同时累积最终文本、记 start 时间、收 usage（tokens）
5. 全部 token 结束 → 写一条 `AiGenerationLog`（status=SUCCESS，或失败时=FAIL+reason）
6. 前端收到 `event: done` → 暂存结果 → 作者点「应用」→ `POST /apply` → ArticleService 更新 aiSummary + summary 字段

---

## 7. 数据库设计

### 7.1 Article 表（已存在，使用预留字段，不用改表结构）
V1 要写入/读取的字段：
```sql
-- 已在 Article.java/Article 实体里存在，直接用
ai_summary              TEXT            -- AI 生成摘要
ai_keywords             VARCHAR(500)    -- AI 提取关键词（逗号分隔，SEO 用）
ai_suggested_tags       VARCHAR(500)    -- AI 推荐标签（JSON 数组或逗号分，存原始候选）
ai_reading_time         INT             -- AI 预估阅读分钟
ai_content_quality_score INT            -- 质量分 0-100
structured_data         LONGTEXT        -- JSON，存 qualityReport / schemaOrg / 关键词权重
entities                LONGTEXT        -- JSON 数组，[{name,type,description,offset}] （V2 用）
```

### 7.2 AiGenerationLog 表（已预留，补齐字段确认）
实体已在代码里存在（搜索结果可见 `AiGenerationLog.java`）。字段规范如下（如与现有实体不一致，以本 spec 为准补齐）：

```sql
CREATE TABLE ai_generation_log (
  id                  BIGINT          PRIMARY KEY AUTO_INCREMENT,
  user_id             BIGINT          NOT NULL COMMENT '触发用户ID，匿名读者为 NULL',
  article_id          BIGINT          NULL COMMENT '关联文章ID，任务无关则 NULL',
  task_type           VARCHAR(32)     NOT NULL COMMENT 'TaskType 枚举值',
  provider            VARCHAR(32)     NOT NULL COMMENT 'Provider 枚举值',
  model_name          VARCHAR(64)     NOT NULL,
  status              VARCHAR(16)     NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/FAIL/TIMEOUT/ABORT',
  failure_reason      VARCHAR(255)    NULL,
  prompt_tokens       INT             NULL,
  completion_tokens   INT             NULL,
  total_tokens        INT             NULL,
  latency_ms          INT             NOT NULL COMMENT '端到端耗时（含网络）',
  system_prompt       LONGTEXT        NULL COMMENT '脱敏后',
  user_prompt         LONGTEXT        NULL COMMENT '脱敏后',
  output_text         MEDIUMTEXT      NULL COMMENT '脱敏后',
  error_stack         TEXT            NULL COMMENT '仅 status != SUCCESS',
  trace_id            VARCHAR(64)     NULL COMMENT 'MDC 注入，排查用',
  client_ip           VARCHAR(45)     NULL COMMENT 'IPv4/IPv6',
  created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_created (user_id, created_at DESC),
  INDEX idx_article (article_id),
  INDEX idx_task_created (task_type, created_at DESC),
  INDEX idx_status_created (status, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 生成调用审计日志';
```

### 7.3 新增表 1：AiProviderConfig（管理后台模型配置）
> ⚠️ V1 阶段此表非必建。V1 默认 Provider 固定为阿里云 DashScope，Key 从启动环境变量 `ALIYUN_API_KEY` 或 `application.yml` 的 `novablog.ai.aliyun.api-key` 读取，无需写入数据库。V2 多 Provider 切换后才启用此表，并以其中的自定义 Key 覆盖环境变量（优先级更高）。
```sql
CREATE TABLE ai_provider_config (
  id                  INT PRIMARY KEY AUTO_INCREMENT,
  provider            VARCHAR(32)     NOT NULL UNIQUE,
  enabled             TINYINT(1)      NOT NULL DEFAULT 0,
  is_default          TINYINT(1)      NOT NULL DEFAULT 0,
  api_key_encrypted   VARCHAR(1024)   NOT NULL COMMENT 'AES-256-GCM 密文 + base64。V1 未启用此表时走 ALIYUN_API_KEY 环境变量',
  endpoint            VARCHAR(255)    NULL COMMENT 'NULL=走官方默认',
  default_model       VARCHAR(64)     NOT NULL,
  timeout_seconds     INT             NOT NULL DEFAULT 30,
  max_output_tokens   INT             NOT NULL DEFAULT 2048,
  temperature         DECIMAL(3,2)    NOT NULL DEFAULT 0.7,
  updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Provider 配置（V2 多Provider时启用）';
```

### 7.4 新增表 2：AiPromptTemplate（Prompt 模板）
```sql
CREATE TABLE ai_prompt_template (
  id                  INT PRIMARY KEY AUTO_INCREMENT,
  task_type           VARCHAR(32)     NOT NULL UNIQUE,
  name_zh             VARCHAR(64)     NOT NULL,
  system_prompt       LONGTEXT        NOT NULL,
  user_prompt_tpl     LONGTEXT        NOT NULL,
  version             INT             NOT NULL DEFAULT 1,
  is_default          TINYINT(1)      NOT NULL DEFAULT 0,
  updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 任务 Prompt 模板';
```

### 7.5 新增表 3：AiQualityConfig（质量评分规则 + 阈值）
单表单行即可：
```sql
CREATE TABLE ai_quality_config (
  id                          INT PRIMARY KEY DEFAULT 1,
  weight_readability          INT NOT NULL DEFAULT 30,
  weight_structure            INT NOT NULL DEFAULT 20,
  weight_seo                  INT NOT NULL DEFAULT 20,
  weight_originality          INT NOT NULL DEFAULT 20,
  weight_compliance           INT NOT NULL DEFAULT 10,
  force_check_before_publish  TINYINT(1) NOT NULL DEFAULT 1,
  min_publish_score           INT NOT NULL DEFAULT 60,
  allow_ignore_low_score      TINYINT(1) NOT NULL DEFAULT 1,
  sensitive_words             LONGTEXT NULL COMMENT '逗号分隔或 JSON 数组',
  updated_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 质量评分配置';
```

---

## 8. 后端模块架构设计

```
com.slim.blogbackend.ai
├── AiController.java                  // 作者端 SSE + 普通接口
├── admin
│   └── AiAdminController.java         // 管理后台配置/监控接口
├── spi
│   ├── LlmProvider.java               // Provider SPI
│   └── AiTaskHandler.java             // 任务处理 SPI
├── provider
│   ├── DashScopeProvider.java         // 通义千问
│   ├── VolcengineProvider.java        // 豆包
│   ├── MoonshotProvider.java          // Kimi
│   ├── DeepSeekProvider.java
│   ├── QianfanProvider.java           // 文心一言
│   └── ZhipuProvider.java             // 智谱
├── task
│   ├── SummaryTaskHandler.java
│   ├── TitleTaskHandler.java
│   ├── TagTaskHandler.java
│   ├── PolishTaskHandler.java
│   ├── ContinueTaskHandler.java
│   ├── ProofreadTaskHandler.java
│   ├── RephraseTaskHandler.java
│   └── QualityScoreTaskHandler.java   // 规则评分，不调 LLM
├── service
│   ├── AiService.java                 // 核心：路由 -> Provider -> TaskHandler -> SSE
│   ├── AiCryptoService.java           // Key 加解密
│   ├── AiLogService.java              // 日志写入 + 查询 + 导出 + 大盘统计
│   ├── AiProviderConfigService.java
│   ├── AiPromptService.java
│   └── AiQualityService.java
├── entity
│   ├── AiProviderConfig.java
│   ├── AiPromptTemplate.java
│   └── AiQualityConfig.java
├── mapper
│   ├── AiProviderConfigMapper.java
│   ├── AiPromptTemplateMapper.java
│   └── AiQualityConfigMapper.java
├── dto
│   ├── SseEventDTO.java               // token/progress/done/error 统一事件
│   ├── ProviderConfigDTO.java
│   ├── QualityReportDTO.java          // 总分 + 维度分 + 问题清单
│   └── LogStatsDTO.java               // 监控大盘聚合结果
├── enums
│   ├── ProviderEnum.java
│   ├── TaskTypeEnum.java
│   └── AiStatusEnum.java
└── config
    ├── AiAutoConfig.java              // 启动时初始化 Prompt 默认值、Provider 默认值（若表空）
    └── SseMvcConfig.java              // SSE 长连接 timeout 调大
```

### 关键类职责摘录
- **`LlmProvider#streamChat(request, emitter)`**：调对应厂商 SSE HTTP，token 转发、累积 usage
- **`AiTaskHandler#buildPrompt(article, params)`**：读 Prompt 模板 + 填变量槽位，返回 System+User prompt 对
- **`AiService#streamGenerate(articleId, taskType, userId, params)`**：组合上面两个，管日志、失败回滚
- **`AiCryptoService#encryptKey(plain)/decryptKey(cipher)`**：AES-256-GCM，key 来自环境变量

---

## 9. 前端模块设计

### 9.1 新增 Pinia Store
```ts
// stores/ai.ts
interface AiStore {
  // 当前文章内的生成历史
  generations: {
    id: string
    taskType: TaskType
    createdAt: number
    params: Record<string, any>
    fullText: string           // 最终完整文本（SSE done 后赋值）
    applied: boolean
    provider: string
    latencyMs: number
  }[]
  // 侧边栏状态
  sidebar: {
    width: number            // 360 默认
    collapsed: boolean
    activeTab: 'assistant' | 'quality' | 'history'
    streaming: boolean
    currentTaskType: TaskType | null
  }
  // 方法
  startStream(taskType): void
  appendToken(text): void
  finishStream(fullResult): void
  apply(generationId, field: 'title' | 'summary' | 'tags' | 'replace-selection' | 'insert')
}
```

### 9.2 新增组件
| 路径 | 组件 | 说明 |
| --- | --- | --- |
| `components/ai/AiSidebar.vue` | AI 侧边栏外壳 | 三 Tab 切换、拖拽调整宽度、折叠按钮 |
| `components/ai/task-cards/*.vue` | TitleCard / SummaryCard / TagCard / PolishCard / ContinueCard / ProofreadCard / RephraseCardCard | 每种任务一张卡，参数表单 + 结果打字机区 + 操作按钮 |
| `components/ai/QualityReport.vue` | 质量报告组件 | 雷达图（5 维度）+ 问题列表（含「一键修复」入口跳对应位置） |
| `components/ai/SseResultBox.vue` | 流式结果容器 | 统一光标闪烁、停止、重生成、应用按钮 |
| `views/admin/AiConfig.vue` | 管理后台 AI 设置页 | 三 Tab：模型配置 / Prompt 模板 / 质量规则 |
| `views/admin/AiMonitor.vue` | 管理后台 AI 监控页 | 指标卡 + 图表 + 日志表 + 导出 |

### 9.3 样式规范
- AI 侧边栏卡片风格沿用现有 Element Plus + 项目渐变主色（`#667eea → #764ba2`）
- 生成中：主色渐变边框 + 右下方 4 个点 loading 动画
- 成功：绿色 badge「已完成 4123ms」
- 失败：红色 badge + 错误说明 + 重试按钮
- 流式打字机：光标 `|` 500ms 闪烁，token 追加时淡入动画 120ms

### 9.4 读者端 V2 组件（预留，不开发）
- `components/ai/ArticleAiSummary.vue` 详情页顶部 AI 速览
- `components/ai/EntityTooltip.vue` 实体悬浮解释
- `components/ai/SmartTranslateBar.vue` 顶部翻译切换条
- `components/ai/RelatedArticleAiList.vue` 底部相关推荐

---

## 10. 错误码与处理策略

| 错误码 | HTTP | 场景 | 前端处理 |
| --- | --- | --- | --- |
| `AI_DISABLED` | 503 | 管理员关闭了全局 AI 开关 | 侧边栏显示"AI 暂不可用"占位，全部任务卡禁用 |
| `NO_PROVIDER_ENABLED` | 503 | 没有启用任何 Provider | 提示"请管理员先在 AI 设置里启用并填 Key" |
| `KEY_INVALID` | 400 | Provider 返回 401/403 Key 错 | 红字提示"模型 Key 无效，请联系管理员"，不重试 |
| `QUOTA_EXHAUSTED` | 402 | Provider 返回额度用完 | 红字提示"模型额度已用尽，请联系管理员充值"，自动切下一个 Provider 重试 1 次 |
| `PROVIDER_TIMEOUT` | 504 | Provider 请求超时（默认 30s） | 提示"生成超时，已中断"，重试 1 次 |
| `PROVIDER_RATE_LIMIT` | 429 | Provider 侧限流 | 提示"模型忙，请 5s 后重试"，自动 sleep 5s 后重试 1 次 |
| `COMPLIANCE_BLOCKED` | 400 | 敏感词本地拦截 | 红字显示命中的敏感词（打码），让作者先修内容 |
| `BAD_PARAMS` | 400 | 正文太短 / 选区为空 / 参数错 | 在对应任务卡下提示具体参数问题 |
| `QUALITY_LOW_BLOCKED` | 422 | 发布体检分不足 + 不允许忽略 | 弹窗提示分数，并跳转到质量 Tab |
| `UNKNOWN` | 500 | 其他未分类错误 | 显示"生成失败，请稍后重试"并附 traceId，方便找管理员排查 |

---

## 11. 验收标准（V1）

### 11.1 功能验收
- [ ] F1：写文章页右侧有 AI 侧边栏，折叠/展开/拖拽宽度正常
- [ ] F2：8 个写作任务（标题/摘要/标签/润色/续写/纠错/改写 + 体检）都能从侧边栏触发，并通过 SSE 流式出结果
- [ ] F3：作者点「应用」后，对应字段真正写入 Article（需数据库验证：aiSummary/summary 同值、tagIds 落 article_tag、标题覆盖 title、质量分落 ai_content_quality_score）
- [ ] F4：管理后台 AI 设置页可启用 Provider、输入加密 Key 后连通性测试返回成功耗时
- [ ] F5：Prompt 模板可编辑并保存，"恢复默认"回到出厂模板
- [ ] F6：管理后台大盘能看到成功/失败调用日志，能按筛选条件导出 CSV
- [ ] F7：质量评分开启强制 + 分不够时，发布被拦截；关闭开关可正常发布

### 11.2 非功能验收
- [ ] NF1：500 字续写首 token ≤ 3s（局域网环境，使用正常 Key）
- [ ] NF2：管理员打开 `AiProviderConfig` 表，`api_key_encrypted` 字段不是明文，前端接口返回里也看不到明文
- [ ] NF3：用户输入包含本地敏感词，本地直接拦截，不调用 LLM
- [ ] NF4：默认 Provider 连续模拟 5 次 5xx，第 6 次调用自动切到下一个已启用 Provider（监控页显示异常告警）
- [ ] NF5：关闭全局 AI 开关后，所有 AI 接口 503 + 前端侧边栏占位提示正确

### 11.3 数据库验收
- [ ] DB1：`ai_generation_log` 每次调用有记录，token 用量、latency、脱敏后 output 齐全
- [ ] DB2：`ai_provider_config` 至少支持 3 个 Provider（dashscope、deepseek、moonshot）同时启用
- [ ] DB3：`ai_prompt_template` 出厂时有 10 条默认模板（8 个 V1 Task + 2 个 V2 占位）

---

## 12. V2 升级路线（读者端）
V2 在 V1 稳定后开启，关键事项：
1. 先上线 rate-limit 中间件（IP 限流 + Redis 计数器）防止匿名刷 Key
2. 部署读者端点 `/api/ai/read/speed-read/:articleId`、`/api/ai/read/translate/:articleId`
3. entities 实体抽取在文章发布后后台异步 worker 做（不要在读者请求时现抽，避免首屏慢）
4. 相关文章推荐从 SQL 关键词命中升级到向量检索（pgvector 或 Milvus），structuredData 的 embedding 字段加一列
5. 可选：加 AI 对话浮窗（仅针对当前文章上下文的问答，不是全站 RAG），单独写一份子 spec

---

## 13. 风险 & 开放问题

| 风险/问题 | 影响 | 应对 |
| --- | --- | --- |
| 国内大模型价格波动 / 突然涨价 | 运营成本上升 | Provider 层实现费用估算，监控页展示「今日预估花费」；超过阈值告警 |
| V1 不做额度限制，作者误操作狂刷 Key | 额度提前耗尽 | 加可选开关：按日维度每用户 100 次软上限（超了提示但不硬挡） |
| 质量评分规则模型误判严重 | 作者抱怨 | 开放「忽略低分可发布」默认开启；评分规则可调权重；差评样本汇总到 V2 升级 LLM-as-Judge |
| SSE 经过 Nginx 反向代理被缓冲导致非流式 | 前端看不到打字机 | 在部署文档里加 Nginx 配置：`proxy_buffering off;` + `X-Accel-Buffering: no` 响应头 |

> ⚠️ 文档中的「默认值」「阈值」如 30s / 60 分 / 30% 权重等，均已基于同类博客产品经验设定；若运营数据显示不符合使用习惯，可在管理后台动态调整无需改代码。

---

**文档结束**
