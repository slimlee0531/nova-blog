# NovaBlog - 面向AI时代的个人博客系统

## 项目简介

NovaBlog 是一个现代化的个人博客系统，融合了AI和Agent时代的特性。它不仅服务于人类读者，也为AI Agent提供结构化、可机器读取的内容。

## 核心特性

### 🤖 AI辅助创作
- **AI写作助手** - 集成阿里云通义千问，提供实时写作辅助
- AI生成文章摘要和关键词
- AI建议标签和分类
- AI内容质量评分
- AI辅助标题优化
- 实体识别和结构化数据生成
- 多模型支持（通义千问、GPT-4等）
- 多模态模型支持（视觉理解）

### 🔄 Agent友好设计
- 标准化RESTful API
- 专用Agent API密钥管理
- 结构化内容输出（JSON-LD, Schema.org）
- Agent访问日志和分析
- 支持Agent评论（标识来源）

### 📚 知识库支持
- 灵活的知识库配置
- 文档分块和向量化存储
- 支持多种嵌入模型
- 相似度搜索

### 📝 传统博客功能
- Markdown编辑器
- 分类和标签系统
- 多级评论
- 文章点赞和收藏
- SEO优化
- 仪表盘统计
- 标签颜色支持

## 技术栈

### 后端
- Java 17
- Spring Boot 3.5
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Maven
- 阿里云 DashScope SDK

### 前端
- Vue 3
- TypeScript
- Vite 8
- Pinia（状态管理）

## 项目结构

```
novaBlog/
├── blog-backend/           # Spring Boot后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/slim/blogbackend/
│   │   │   │       ├── ai/           # AI功能模块
│   │   │   │       │   ├── dto/      # AI数据传输对象
│   │   │   │       │   ├── enums/    # AI枚举类
│   │   │   │       │   ├── provider/ # AI提供商实现
│   │   │   │       │   ├── service/  # AI服务
│   │   │   │       │   └── spi/      # AI服务提供者接口
│   │   │   │       ├── config/       # 配置类
│   │   │   │       ├── controller/   # 控制器
│   │   │   │       ├── entity/       # 实体类
│   │   │   │       ├── mapper/       # Mapper接口
│   │   │   │       ├── service/      # 服务层
│   │   │   │       └── util/         # 工具类
│   │   │   └── resources/
│   │   │       ├── db/migration/     # 数据库迁移脚本
│   │   │       ├── mapper/           # MyBatis映射文件
│   │   │       └── application.yml   # 配置文件（不提交到git）
│   │   └── test/                     # 测试代码
│   └── pom.xml
├── blog-frontend/          # Vue前端
│   └── src/
│       ├── api/            # API接口
│       ├── components/     # Vue组件
│       │   └── ai/         # AI相关组件
│       ├── store/          # Pinia状态管理
│       ├── views/          # 页面视图
│       └── utils/          # 工具函数
├── docs/                   # 项目文档
│   └── database-design.md  # 数据库设计文档
└── README.md
```

## 数据库设计

数据库包含15个核心表，分为以下几类：

### 核心表
1. `users` - 用户表
2. `articles` - 文章表（包含AI增强字段）
3. `categories` - 分类表
4. `tags` - 标签表
5. `article_tags` - 文章标签关联表
6. `comments` - 评论表（支持Agent评论）
7. `media` - 媒体文件表

### AI/Agent功能表
8. `ai_generation_logs` - AI生成日志表
9. `agent_api_keys` - Agent API密钥表
10. `agent_access_logs` - Agent访问日志表
11. `knowledge_base_configs` - 知识库配置表
12. `knowledge_base_documents` - 知识库文档表

### 辅助表
13. `likes` - 点赞表
14. `bookmarks` - 收藏表
15. `system_configs` - 系统配置表

详细设计请查看 [数据库设计文档](docs/database-design.md)

## 开发计划

### Phase 1: 基础架构 ✅
- [x] 项目初始化
- [x] 数据库设计
- [x] 后端基础架构
- [x] 前端基础架构

### Phase 2: 核心功能 ✅
- [x] 用户认证
- [x] 文章管理
- [x] 分类和标签
- [x] 评论系统
- [x] 点赞和收藏
- [x] 仪表盘统计

### Phase 3: AI功能 ✅
- [x] AI写作助手集成
- [x] 阿里云通义千问支持
- [x] AI辅助创作
- [x] 智能摘要
- [x] 标签推荐
- [x] 内容质量分析
- [x] 多模型支持
- [x] 多模态模型支持

### Phase 4: Agent API 🚧
- [x] API密钥管理
- [x] 结构化内容输出
- [ ] Agent访问日志
- [ ] 知识库系统

## 快速开始

### 环境要求
- Java 17+
- Node.js 22+
- MySQL 8.0+
- Maven 3.8+

### 1. 克隆项目
```bash
git clone https://github.com/slimlee0531/nova-blog.git
cd nova-blog
```

### 2. 数据库配置
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE novablog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本（如果有）
# mysql -u root -p novablog < docs/database.sql
```

### 3. 后端启动
```bash
cd blog-backend

# 复制配置文件
cp src/main/resources/application.yml.example src/main/resources/application.yml

# 修改配置文件（数据库密码、JWT密钥等）
# 编辑 src/main/resources/application.yml

# 启动服务
mvn spring-boot:run
```

### 4. 前端启动
```bash
cd blog-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 5. AI功能配置（可选）
```bash
# 设置阿里云通义千问API Key
export ALIYUN_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxx

# 启动后端服务
mvn spring-boot:run
```

## 配置说明

### application.yml 配置示例
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/novablog?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

jwt:
  secret: your_jwt_secret_key_here
  expiration: 86400

novablog:
  ai:
    enabled: true
    default-provider: aliyun
    aliyun:
      api-key: ${ALIYUN_API_KEY:}
      model: qwen3.7-plus
      endpoint: https://llm-jbyc85e0hmzvu5ib.cn-beijing.maas.aliyuncs.com/api/v1
      timeout-seconds: 30
      max-output-tokens: 2048
      temperature: 0.7
```

## API文档

### Agent API

Agent API 使用 API Key 进行认证：

```bash
curl -H "X-API-Key: your-api-key" \
     -H "Content-Type: application/json" \
     https://api.novablog.com/v1/articles
```

### AI写作助手API

```bash
# 生成文章摘要
curl -X POST http://localhost:8080/api/ai/generate/summary \
  -H "Content-Type: application/json" \
  -d '{
    "content": "你的文章内容...",
    "model": "qwen3.7-plus"
  }'

# 生成标签建议
curl -X POST http://localhost:8080/api/ai/generate/tags \
  -H "Content-Type: application/json" \
  -d '{
    "content": "你的文章内容...",
    "model": "qwen3.7-plus"
  }'
```

### 文章结构化数据

文章API返回包含Schema.org兼容的结构化数据：

```json
{
  "id": 1,
  "title": "文章标题",
  "content": "...",
  "structuredData": {
    "@type": "Article",
    "headline": "文章标题",
    "author": {
      "@type": "Person",
      "name": "作者"
    },
    "datePublished": "2026-07-16",
    "keywords": ["AI", "博客"]
  }
}
```

## 开发规范

### Git Commit规范
使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

类型（type）：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

示例：
```bash
git commit -m "feat(ai): 集成阿里云通义千问AI写作助手"
git commit -m "fix(auth): 修复登录token过期问题"
git commit -m "docs: 更新数据库设计文档"
git commit -m "style(frontend): 全面重新设计前端页面"
```

### 代码规范
- 后端：遵循Spring Boot最佳实践
- 前端：使用TypeScript严格模式
- 提交前进行代码检查

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

- Spring Boot - 后端框架
- Vue.js - 前端框架
- 阿里云通义千问 - AI能力支持
- 所有贡献者和开源社区
