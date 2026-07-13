# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

NovaBlog 是一个面向AI时代的个人博客系统，不仅服务于人类读者，也为AI Agent提供结构化、可机器读取的内容。

## 技术栈

### 后端 (blog-backend)
- Java 17 + Spring Boot 3.5
- MyBatis-Plus 3.5.7 (ORM框架)
- MySQL 8.0
- Maven

### 前端 (blog-frontend)
- Vue 3 + TypeScript
- Vite 8

## 常用命令

### 后端开发
```bash
# 启动后端
cd blog-backend
mvn spring-boot:run

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=BlogBackendApplicationTests

# 打包
mvn clean package
```

### 前端开发
```bash
# 安装依赖
cd blog-frontend
npm install

# 启动开发服务器
npm run dev

# 类型检查
npm run type-check

# 构建生产版本
npm run build
```

## 项目结构

```
novaBlog/
├── blog-backend/           # Spring Boot后端
│   ├── src/main/java/com/slim/blogbackend/
│   │   ├── entity/         # 实体类 (MyBatis-Plus)
│   │   ├── mapper/         # Mapper接口
│   │   ├── service/        # 服务层
│   │   └── controller/     # 控制器层
│   └── src/main/resources/
│       └── db/migration/   # 数据库迁移脚本
├── blog-frontend/          # Vue前端
│   └── src/
├── docs/                   # 项目文档
└── CLAUDE.md
```

## 数据库设计

数据库包含15个核心表：
- **核心博客表**: users, articles, categories, tags, article_tags, comments, media
- **AI/Agent功能表**: ai_generation_logs, agent_api_keys, agent_access_logs, knowledge_base_configs, knowledge_base_documents
- **辅助表**: likes, bookmarks, system_configs

详细设计请查看 `docs/database-design.md`

## 开发规范

### Git Commit规范
使用 Conventional Commits 规范，中文描述：
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

### 实体类规范
- 使用 Lombok 简化代码 (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- 使用 MyBatis-Plus 注解 (@TableName, @TableId)
- JSON字段使用 String 类型，在应用层处理转换
- 时间字段使用 LocalDateTime

### 代码分层
```
Controller → Service → Mapper → Database
    ↓           ↓         ↓
  参数校验    业务逻辑    数据访问
```

## AI/Agent特性

### 实体类中的AI增强字段
Article表包含以下AI字段：
- `ai_summary`: AI生成的摘要
- `ai_keywords`: AI提取的关键词 (JSON)
- `ai_content_quality_score`: AI内容质量评分
- `ai_suggested_tags`: AI建议的标签 (JSON)
- `structured_data`: 结构化数据 (Schema.org)
- `entities`: AI识别的实体 (JSON)

### Agent API支持
- API密钥管理 (agent_api_keys)
- 访问日志记录 (agent_access_logs)
- 权限控制和速率限制

## 环境要求

- Java 17+
- Node.js 22+
- MySQL 8.0+
- Maven 3.8+

## 配置文件

### application.properties
需要配置MySQL连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/novablog
spring.datasource.username=root
spring.datasource.password=your_password
```
