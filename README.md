# NovaBlog - 面向AI时代的个人博客系统

## 项目简介

NovaBlog 是一个现代化的个人博客系统，融合了AI和Agent时代的特性。它不仅服务于人类读者，也为AI Agent提供结构化、可机器读取的内容。项目采用前后端分离架构，后端基于 Java 17 + Spring Boot 3.5，前端基于 Vue 3 + TypeScript + Vite。

## 核心特性

### 🤖 AI写作助手
- **13种AI任务类型**：摘要生成、标题建议、标签推荐、正文润色、续写、校对、重写、质量评分、SEO优化、大纲建议、实体提取、速读总结、翻译
- **阿里云通义千问集成**：支持文本模型和多模态模型（qwen3.7-plus、qwen-vl等）
- **实时流式输出**：通过SSE（Server-Sent Events）实现逐字生成
- **一键应用结果**：AI生成的内容可直接应用到文章标题、摘要、标签、正文等字段
- **质量评分系统**：AI对文章进行多维度质量评估（100分制）
- **可扩展架构**：基于SPI设计，易于添加新的AI提供商

### 📝 博客核心功能
- **Markdown编辑器**：支持实时预览、代码高亮
- **分类和标签系统**：支持层级分类、标签颜色、文章计数
- **多级评论系统**：支持嵌套评论、评论审核、访客评论
- **文章收藏**：用户可收藏文章，支持备注
- **仪表盘统计**：文章数、浏览量、评论数、收藏数等实时统计

### 🔄 Agent友好设计
- **结构化内容输出**：Schema.org兼容的JSON-LD格式
- **Agent评论支持**：可标识Agent来源和元数据
- **Agent API密钥管理**：专用API密钥、权限控制、速率限制
- **访问日志记录**：完整的Agent访问追踪（已设计，待实现）

### 🎨 现代化UI
- **响应式设计**：适配桌面和移动端
- **暗黑模式**：一键切换主题
- **Element Plus**：基于Vue 3的UI组件库
- **Pinia状态管理**：轻量级状态管理方案

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | LTS版本 |
| Spring Boot | 3.5 | 主框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0 | 关系型数据库 |
| Maven | 3.8+ | 项目构建 |
| DashScope SDK | 2.18.0 | 阿里云AI SDK |
| JWT | 0.12.6 | 身份认证 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3 | 渐进式JavaScript框架 |
| TypeScript | 5 | 类型安全 |
| Vite | 8 | 构建工具 |
| Element Plus | - | UI组件库 |
| Pinia | - | 状态管理 |
| Vue Router | - | 路由管理 |
| Axios | - | HTTP客户端 |
| markdown-it | - | Markdown渲染 |
| highlight.js | - | 代码高亮 |

## 项目结构

```
novaBlog/
├── blog-backend/                          # Spring Boot后端
│   ├── src/main/java/com/slim/blogbackend/
│   │   ├── ai/                            # AI功能模块
│   │   │   ├── dto/                       # AI数据传输对象
│   │   │   │   ├── LlmChatRequest.java    # LLM请求DTO
│   │   │   │   ├── LlmChatResponse.java   # LLM响应DTO
│   │   │   │   └── BuiltInPromptTemplate.java  # 内置提示词模板
│   │   │   ├── enums/                     # AI枚举类
│   │   │   │   ├── AiProviderEnum.java    # AI提供商枚举
│   │   │   │   └── AiTaskTypeEnum.java    # AI任务类型枚举（13种）
│   │   │   ├── provider/                  # AI提供商实现
│   │   │   │   └── AliyunDashScopeProvider.java  # 阿里云实现
│   │   │   ├── service/                   # AI服务层
│   │   │   │   ├── LlmService.java        # LLM服务（管理多Provider）
│   │   │   │   └── TaskPromptService.java # 任务提示词路由
│   │   │   └── spi/                       # 服务提供者接口
│   │   │       └── LlmProvider.java       # LLM Provider接口
│   │   ├── config/                        # 配置类
│   │   │   ├── WebMvcConfig.java          # Web配置（CORS、拦截器）
│   │   │   ├── MybatisPlusConfig.java     # MyBatis-Plus配置
│   │   │   ├── AiProperties.java          # AI配置属性
│   │   │   └── AiAutoConfig.java          # AI自动配置
│   │   ├── controller/                    # 控制器层
│   │   │   ├── api/                       # 前台API
│   │   │   │   ├── ArticleApiController.java
│   │   │   │   ├── CommentApiController.java
│   │   │   │   └── BookmarkApiController.java
│   │   │   └── admin/                     # 后台管理API
│   │   │       ├── ArticleController.java
│   │   │       ├── CategoryController.java
│   │   │       ├── TagController.java
│   │   │       ├── CommentController.java
│   │   │       ├── DashboardController.java
│   │   │       └── AiAdminController.java # AI管理（SSE流式接口）
│   │   ├── entity/                        # 实体类（14个）
│   │   ├── mapper/                        # MyBatis Mapper接口
│   │   ├── service/                       # 业务逻辑层
│   │   ├── dto/                           # 数据传输对象
│   │   ├── vo/                            # 视图对象
│   │   ├── util/                          # 工具类
│   │   │   └── JwtUtil.java               # JWT工具类
│   │   ├── interceptor/                   # 拦截器
│   │   │   └── JwtInterceptor.java        # JWT认证拦截器
│   │   └── exception/                     # 异常处理
│   ├── src/main/resources/
│   │   ├── db/migration/                  # 数据库迁移脚本
│   │   │   └── V1__init_database.sql      # 初始化SQL
│   │   ├── mapper/                        # MyBatis映射文件
│   │   └── application.yml                # 配置文件（不提交到git）
│   └── pom.xml                            # Maven配置
│
├── blog-frontend/                         # Vue前端
│   └── src/
│       ├── api/                           # API接口层
│       │   ├── request.ts                 # Axios封装
│       │   ├── user.ts                    # 用户API
│       │   ├── article.ts                 # 文章API
│       │   ├── category.ts                # 分类API
│       │   ├── tag.ts                     # 标签API
│       │   ├── comment.ts                 # 评论API
│       │   ├── bookmark.ts                # 收藏API
│       │   └── ai.ts                      # AI API（SSE流式）
│       ├── components/                    # Vue组件
│       │   ├── ai/
│       │   │   └── AiSidebar.vue          # AI写作助手侧边栏
│       │   └── ThemeToggle.vue            # 主题切换组件
│       ├── views/                         # 页面视图
│       │   ├── Home.vue                   # 首页
│       │   ├── ArticleDetail.vue          # 文章详情
│       │   ├── WriteArticle.vue           # 写文章
│       │   ├── Login.vue / Register.vue   # 登录/注册
│       │   ├── Bookmarks.vue              # 收藏列表
│       │   └── admin/                     # 管理后台
│       │       ├── Dashboard.vue          # 仪表盘
│       │       ├── ArticleList.vue        # 文章列表
│       │       ├── ArticleEdit.vue        # 文章编辑（集成AI助手）
│       │       ├── CategoryList.vue       # 分类管理
│       │       ├── TagList.vue            # 标签管理
│       │       └── CommentList.vue        # 评论管理
│       ├── store/                         # Pinia状态管理
│       │   ├── user.ts                    # 用户状态
│       │   └── ai.ts                      # AI状态
│       ├── router/                        # 路由配置
│       ├── types/                         # TypeScript类型定义
│       └── utils/                         # 工具函数
│           ├── format.ts                  # 格式化工具
│           └── markdown.ts                # Markdown渲染
│
├── docs/                                  # 项目文档
│   └── database-design.md                 # 数据库设计文档
│
└── README.md                              # 项目说明
```

## 数据库设计

数据库包含 **15个核心表**，分为三大类：

### 核心博客表（7张）
| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `users` | 用户表 | id, username, email, password_hash, role(ADMIN/EDITOR/AUTHOR) |
| `articles` | 文章表 | id, title, slug, content(Markdown), status(DRAFT/PUBLISHED/ARCHIVED), AI增强字段 |
| `categories` | 分类表 | id, name, slug, parent_id(支持层级), article_count |
| `tags` | 标签表 | id, name, slug, color(十六进制), article_count |
| `article_tags` | 文章标签关联 | article_id, tag_id (多对多) |
| `comments` | 评论表 | id, content, parent_id(多级), is_agent_comment, status(PENDING/APPROVED/REJECTED) |
| `media` | 媒体文件表 | id, file_name, file_path, mime_type |

### AI/Agent功能表（5张）
| 表名 | 说明 |
|------|------|
| `ai_generation_logs` | AI生成日志：记录操作类型、输入输出、token消耗、处理时间 |
| `agent_api_keys` | Agent API密钥管理：密钥哈希、权限控制、速率限制 |
| `agent_access_logs` | Agent访问日志：请求/响应信息、访问目的 |
| `knowledge_base_configs` | 知识库配置：分块参数、向量化选项、访问控制 |
| `knowledge_base_documents` | 知识库文档分块：内容、向量嵌入(JSON)、来源 |

### 辅助表（3张）
| 表名 | 说明 |
|------|------|
| `likes` | 点赞表：支持文章和评论点赞，支持未登录用户(IP+指纹去重) |
| `bookmarks` | 收藏表：用户对文章的收藏，含备注 |
| `system_configs` | 系统配置表：key-value存储 |

**数据库特性**：
- 6个触发器自动维护冗余计数字段
- 初始数据：1个管理员账户（admin/admin123）、5个默认分类、8个默认标签

详细设计请查看 [数据库设计文档](docs/database-design.md)

## AI功能详解

### 13种AI任务类型

| 分组 | 任务 | 说明 |
|------|------|------|
| **写作** | SUMMARY | 生成文章摘要 |
| | TITLE | 生成标题建议 |
| | TAG | 推荐标签 |
| | CONTINUE | 续写内容 |
| | SUGGEST_OUTLINE | 生成大纲建议 |
| **优化** | POLISH | 正文润色 |
| | PROOFREAD | 校对纠错 |
| | REPHRASE | 重写表达 |
| | TRANSLATE | 翻译 |
| **质量** | QUALITY_SCORE | 质量评分（100分制） |
| **SEO** | SEO_META | 生成SEO元数据 |
| **分析** | EXTRACT_ENTITIES | 实体提取 |
| | SPEED_READ | 速读总结 |

### AI架构设计

```
┌─────────────────────────────────────────────────────────┐
│                    AiAdminController                     │
│                    (SSE流式接口)                          │
└─────────────┬───────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────┐
│                    TaskPromptService                     │
│                    (任务提示词路由)                       │
└─────────────┬───────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────┐
│                      LlmService                          │
│                    (多Provider管理)                       │
└─────────────┬───────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────┐
│                   LlmProvider (SPI)                      │
│  ┌─────────────────────────────────────────────────┐   │
│  │          AliyunDashScopeProvider                 │   │
│  │      (阿里云通义千问 - 文本/多模态)              │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 前端AI集成

**AiSidebar组件**：
- 4个Tab：任务列表、生成结果、质量评分、设置
- 支持拖拽调整宽度（280-640px）
- 流式输出展示
- 一键应用到文章表单

**SSE事件处理**：
- `meta` - 任务元数据
- `progress` - 进度百分比
- `token` - 逐字输出
- `done` - 完成（含usage统计）
- `error` - 错误处理

## 快速开始

### 环境要求

- **Java** 17+
- **Node.js** 22+
- **MySQL** 8.0+
- **Maven** 3.8+

### 1. 克隆项目

```bash
git clone https://github.com/slimlee0531/nova-blog.git
cd nova-blog
```

### 2. 数据库配置

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE novablog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入初始数据（可选）
mysql -u root -p novablog < blog-backend/src/main/resources/db/migration/V1__init_database.sql
```

### 3. 后端配置

```bash
cd blog-backend

# 复制配置文件模板
cp src/main/resources/application.yml.example src/main/resources/application.yml

# 编辑配置文件，修改以下配置：
# - spring.datasource.password (MySQL密码)
# - jwt.secret (JWT密钥，至少32字符)
# - novablog.ai.aliyun.api-key (阿里云API Key，可选)
```

**application.yml 关键配置**：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/novablog?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的MySQL密码

jwt:
  secret: your_jwt_secret_key_here  # 修改为随机字符串（至少32字符）
  expiration: 86400

novablog:
  ai:
    enabled: true
    default-provider: aliyun
    aliyun:
      api-key: ${ALIYUN_API_KEY:}  # 设置环境变量或直接填写
      model: qwen3.7-plus
      endpoint: https://llm-jbyc85e0hmzvu5ib.cn-beijing.maas.aliyuncs.com/api/v1
      timeout-seconds: 30
      max-output-tokens: 2048
      temperature: 0.7
```

### 4. 启动后端

```bash
# 编译并启动
mvn spring-boot:run

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/blog-backend-0.0.1-SNAPSHOT.jar
```

后端默认运行在 `http://localhost:8080`

### 5. 启动前端

```bash
cd blog-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 6. AI功能配置（可选）

```bash
# 设置阿里云通义千问API Key
export ALIYUN_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxx

# 启动后端服务
mvn spring-boot:run
```

## API接口文档

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/register` | 用户注册 |
| POST | `/api/user/login` | 用户登录（返回JWT Token） |
| GET | `/api/articles` | 获取已发布文章列表（分页） |
| GET | `/api/articles/detail/{slug}` | 按slug获取文章详情 |
| GET | `/api/articles/category/{slug}` | 按分类获取文章 |
| GET | `/api/articles/tag/{slug}` | 按标签获取文章 |

### 需认证接口

**请求头**：`Authorization: Bearer <token>`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/info` | 获取当前用户信息 |
| GET | `/api/user/list` | 获取用户列表（分页） |
| POST | `/api/comments` | 发表评论 |
| GET | `/api/comments/article/{id}` | 获取文章评论 |
| POST | `/api/bookmarks/toggle` | 切换收藏状态 |
| GET | `/api/bookmarks/check/{articleId}` | 检查收藏状态 |
| GET | `/api/bookmarks` | 获取收藏列表 |
| GET | `/api/bookmarks/count` | 获取收藏数量 |

### 后台管理接口（需ADMIN角色）

**文章管理**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/article/list` | 文章列表（分页，可按状态筛选） |
| GET | `/api/admin/article/{id}` | 获取文章详情 |
| POST | `/api/admin/article` | 创建文章 |
| PUT | `/api/admin/article/{id}` | 更新文章 |
| DELETE | `/api/admin/article/{id}` | 删除文章 |

**分类管理**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/category/list` | 分类列表 |
| POST | `/api/admin/category` | 创建分类 |
| PUT | `/api/admin/category/{id}` | 更新分类 |
| DELETE | `/api/admin/category/{id}` | 删除分类 |

**标签管理**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/tag/list` | 标签列表 |
| POST | `/api/admin/tag` | 创建标签 |
| PUT | `/api/admin/tag/{id}` | 更新标签 |
| DELETE | `/api/admin/tag/{id}` | 删除标签 |

**评论管理**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/comments/list` | 评论列表（分页，可按状态/文章筛选） |
| PUT | `/api/admin/comments/{id}/approve` | 批准评论 |
| PUT | `/api/admin/comments/{id}/reject` | 拒绝评论 |
| DELETE | `/api/admin/comments/{id}` | 删除评论 |

**仪表盘**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard/stats` | 获取统计数据 |

**AI管理**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/ai/config` | 获取AI配置（脱敏） |
| POST | `/api/admin/ai/test` | 测试AI连通性 |
| GET | `/api/admin/ai/generate` | AI流式生成（SSE） |

### AI流式生成接口

**请求**：
```bash
GET /api/admin/ai/generate?taskType=SUMMARY&content=你的文章内容...
Accept: text/event-stream
```

**响应事件**：
```
event: meta
data: {"taskType":"SUMMARY","label":"生成摘要","model":"qwen3.7-plus"}

event: progress
data: {"percent":25}

event: token
data: {"text":"这"}

event: token
data: {"text":"是"}

event: done
data: {"usage":{"promptTokens":100,"completionTokens":50},"latencyMs":1234}

event: error
data: {"code":"API_ERROR","message":"请求失败","retryable":true}
```

### 响应格式

所有API响应使用统一格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

## 开发规范

### Git Commit规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范，中文描述：

```
<type>(<scope>): <subject>

类型（type）：
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式（不影响功能）
- refactor: 重构
- test: 测试相关
- chore: 构建/工具相关
```

示例：
```bash
git commit -m "feat(ai): 集成阿里云通义千问AI写作助手"
git commit -m "fix(auth): 修复登录token过期问题"
git commit -m "docs: 更新数据库设计文档"
git commit -m "style(frontend): 全面重新设计前端页面"
```

### 代码规范

**后端**
- 使用 Lombok 简化代码（@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor）
- 使用 MyBatis-Plus 注解（@TableName, @TableId）
- JSON字段使用 String 类型，在应用层处理转换
- 时间字段使用 LocalDateTime
- Controller → Service → Mapper → Database 分层

**前端**
- 使用 TypeScript 严格模式
- 组件使用 Composition API（setup语法糖）
- 状态管理使用 Pinia
- API调用统一通过 api/ 层

## 开发计划

### Phase 1: 基础架构 ✅
- [x] 项目初始化
- [x] 数据库设计（15张表 + 触发器）
- [x] 后端基础架构（Spring Boot + MyBatis-Plus）
- [x] 前端基础架构（Vue 3 + TypeScript + Vite）
- [x] JWT认证机制

### Phase 2: 核心功能 ✅
- [x] 用户注册/登录
- [x] 文章CRUD（Markdown支持）
- [x] 分类和标签管理
- [x] 评论系统（多级评论 + 审核）
- [x] 收藏功能
- [x] 仪表盘统计

### Phase 3: AI功能 ✅
- [x] AI写作助手架构（SPI设计）
- [x] 阿里云通义千问集成
- [x] 13种AI任务类型
- [x] SSE流式输出
- [x] 一键应用AI结果
- [x] 质量评分系统
- [x] 多模态模型支持

### Phase 4: Agent API 🚧
- [x] Agent API密钥管理（数据库 + 实体类）
- [ ] Agent访问日志实现
- [ ] 知识库系统实现
- [ ] 结构化内容输出优化
- [ ] Agent评论功能

### Phase 5: 高级功能 ⏳
- [ ] 媒体文件上传
- [ ] 点赞功能实现
- [ ] 系统配置管理
- [ ] 文章搜索（全文检索）
- [ ] RSS订阅

## 常见问题

### 1. 启动时报错 "ALIYUN_API_KEY 环境变量未设置"

这是警告信息，不影响启动。AI功能需要阿里云API Key才能使用：
```bash
export ALIYUN_API_KEY=sk-xxx
```

### 2. 数据库连接失败

确保：
- MySQL服务已启动
- 数据库 `novablog` 已创建
- `application.yml` 中的数据库配置正确

### 3. JWT认证失败

确保：
- `jwt.secret` 配置了足够长的随机字符串（至少32字符）
- 前端请求头包含正确的 `Authorization: Bearer <token>`

### 4. AI功能不响应

检查：
- `novablog.ai.enabled` 是否为 `true`
- 阿里云API Key是否正确配置
- 网络是否能访问阿里云服务

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat(amazing-feature): 添加 amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目链接: https://github.com/slimlee0531/nova-blog
- Issue Tracker: https://github.com/slimlee0531/nova-blog/issues

## 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架
- [Vue.js](https://vuejs.org/) - 前端框架
- [Element Plus](https://element-plus.org/) - UI组件库
- [阿里云通义千问](https://www.aliyun.com/product/dashscope) - AI能力支持
- [MyBatis-Plus](https://baomidou.com/) - ORM框架
- 所有贡献者和开源社区
