# NovaBlog 数据库设计文档

## 项目概述

NovaBlog 是一个面向AI时代的个人博客系统，不仅服务于人类读者，也为AI Agent提供结构化、可机器读取的内容。系统支持AI辅助创作、Agent API访问、知识库等功能。

## 设计理念

1. **人机共读**：文章内容同时优化给人类和AI阅读
2. **AI辅助创作**：集成AI能力，帮助作者提升创作效率
3. **Agent友好**：提供标准化API，让AI Agent能够理解和交互博客内容
4. **可扩展性**：模块化设计，便于未来添加新的AI功能

---

## 数据库表结构

### 1. 用户表 (users)

存储博主和管理员信息。

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    display_name VARCHAR(100) COMMENT '显示名称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    bio TEXT COMMENT '个人简介',
    role ENUM('ADMIN', 'EDITOR', 'AUTHOR') DEFAULT 'AUTHOR' COMMENT '用户角色',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

### 2. 分类表 (categories)

文章分类，支持层级结构。

```sql
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    slug VARCHAR(100) NOT NULL UNIQUE COMMENT 'URL友好的标识',
    description TEXT COMMENT '分类描述',
    parent_id BIGINT COMMENT '父分类ID，支持多级分类',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    article_count INT DEFAULT 0 COMMENT '文章数量（冗余字段，便于查询）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_slug (slug),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';
```

### 3. 标签表 (tags)

文章标签，用于内容标记和检索。

```sql
CREATE TABLE tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    slug VARCHAR(50) NOT NULL UNIQUE COMMENT 'URL友好的标识',
    description VARCHAR(200) COMMENT '标签描述',
    color VARCHAR(7) COMMENT '标签颜色（十六进制）',
    article_count INT DEFAULT 0 COMMENT '文章数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_slug (slug),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';
```

### 4. 文章表 (articles)

核心表，存储博客文章内容，包含AI增强字段。

```sql
CREATE TABLE articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    slug VARCHAR(200) NOT NULL UNIQUE COMMENT 'URL友好的标识',
    content LONGTEXT NOT NULL COMMENT '文章内容（Markdown格式）',
    content_html LONGTEXT COMMENT '渲染后的HTML内容（缓存）',
    summary TEXT COMMENT '文章摘要',
    
    -- AI增强字段
    ai_summary TEXT COMMENT 'AI生成的摘要',
    ai_keywords JSON COMMENT 'AI提取的关键词',
    ai_reading_time INT COMMENT 'AI估算的阅读时间（分钟）',
    ai_content_quality_score DECIMAL(3,2) COMMENT 'AI内容质量评分（0-1）',
    ai_suggested_tags JSON COMMENT 'AI建议的标签',
    ai_suggested_category VARCHAR(100) COMMENT 'AI建议的分类',
    
    -- 结构化数据（便于Agent理解）
    structured_data JSON COMMENT '结构化元数据（用于Schema.org等）',
    entities JSON COMMENT 'AI识别的实体（人物、地点、概念等）',
    
    -- 状态和元数据
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED', 'SCHEDULED') DEFAULT 'DRAFT' COMMENT '文章状态',
    visibility ENUM('PUBLIC', 'PRIVATE', 'PASSWORD_PROTECTED') DEFAULT 'PUBLIC' COMMENT '可见性',
    password VARCHAR(100) COMMENT '访问密码（如果需要）',
    
    -- 统计字段
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞次数',
    comment_count INT DEFAULT 0 COMMENT '评论数量',
    bookmark_count INT DEFAULT 0 COMMENT '收藏次数',
    
    -- SEO字段
    meta_title VARCHAR(200) COMMENT 'SEO标题',
    meta_description VARCHAR(300) COMMENT 'SEO描述',
    og_image VARCHAR(500) COMMENT 'Open Graph图片',
    
    -- 关联字段
    author_id BIGINT NOT NULL COMMENT '作者ID',
    category_id BIGINT COMMENT '分类ID',
    featured_image VARCHAR(500) COMMENT '特色图片URL',
    
    -- 时间字段
    published_at TIMESTAMP NULL COMMENT '发布时间',
    scheduled_at TIMESTAMP NULL COMMENT '计划发布时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_slug (slug),
    INDEX idx_status (status),
    INDEX idx_author (author_id),
    INDEX idx_category (category_id),
    INDEX idx_published (published_at),
    FULLTEXT INDEX idx_content_search (title, content, summary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';
```

### 5. 文章标签关联表 (article_tags)

```sql
CREATE TABLE article_tags (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';
```

### 6. 评论表 (comments)

支持人类和AI Agent的评论。

```sql
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL COMMENT '评论内容',
    
    -- 评论者信息
    user_id BIGINT COMMENT '用户ID（如果是登录用户）',
    guest_name VARCHAR(100) COMMENT '访客名称（未登录用户）',
    guest_email VARCHAR(100) COMMENT '访客邮箱（未登录用户）',
    guest_website VARCHAR(200) COMMENT '访客网站（未登录用户）',
    
    -- Agent评论标识
    is_agent_comment BOOLEAN DEFAULT FALSE COMMENT '是否为Agent评论',
    agent_id VARCHAR(100) COMMENT 'Agent标识（如：openai-gpt4, claude-3）',
    agent_metadata JSON COMMENT 'Agent元数据',
    
    -- 评论关系
    article_id BIGINT NOT NULL COMMENT '文章ID',
    parent_id BIGINT COMMENT '父评论ID（支持多级评论）',
    
    -- 状态
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'SPAM') DEFAULT 'PENDING' COMMENT '审核状态',
    
    -- 统计
    like_count INT DEFAULT 0 COMMENT '点赞数量',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_article (article_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
```

### 7. 媒体文件表 (media)

存储上传的图片、视频等媒体文件。

```sql
CREATE TABLE media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL COMMENT '文件名',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    mime_type VARCHAR(100) NOT NULL COMMENT 'MIME类型',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    url VARCHAR(500) NOT NULL COMMENT '访问URL',
    
    -- 图片特有字段
    width INT COMMENT '图片宽度',
    height INT COMMENT '图片高度',
    alt_text VARCHAR(200) COMMENT '替代文本（SEO和无障碍）',
    
    -- AI分析字段
    ai_description TEXT COMMENT 'AI生成的图片描述',
    ai_tags JSON COMMENT 'AI识别的标签',
    
    -- 元数据
    metadata JSON COMMENT '其他元数据',
    
    -- 关联
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    article_id BIGINT COMMENT '关联文章ID（可选）',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE SET NULL,
    INDEX idx_uploader (uploader_id),
    INDEX idx_article (article_id),
    INDEX idx_mime_type (mime_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体文件表';
```

---

## AI/Agent功能相关表

### 8. AI生成日志表 (ai_generation_logs)

记录所有AI辅助创作的操作日志。

```sql
CREATE TABLE ai_generation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- 操作信息
    operation_type ENUM(
        'SUMMARY_GENERATION',
        'KEYWORD_EXTRACTION',
        'TAG_SUGGESTION',
        'CATEGORY_SUGGESTION',
        'CONTENT_IMPROVEMENT',
        'TITLE_SUGGESTION',
        'CONTENT_EXPANSION',
        'GRAMMAR_CHECK',
        'READABILITY_ANALYSIS',
        'ENTITY_EXTRACTION',
        'STRUCTURED_DATA_GENERATION',
        'TRANSLATION',
        'CUSTOM_PROMPT'
    ) NOT NULL COMMENT '操作类型',
    
    -- 输入输出
    input_data JSON COMMENT '输入数据（如文章内容、提示词等）',
    output_data JSON COMMENT '输出结果',
    
    -- AI模型信息
    model_name VARCHAR(100) NOT NULL COMMENT 'AI模型名称',
    model_version VARCHAR(50) COMMENT '模型版本',
    prompt_template TEXT COMMENT '使用的提示词模板',
    
    -- 性能指标
    tokens_used INT COMMENT '消耗的token数量',
    processing_time_ms INT COMMENT '处理时间（毫秒）',
    cost_usd DECIMAL(10,6) COMMENT '成本（美元）',
    
    -- 关联
    user_id BIGINT COMMENT '触发操作的用户',
    article_id BIGINT COMMENT '关联的文章',
    
    -- 状态
    status ENUM('SUCCESS', 'FAILED', 'TIMEOUT', 'CANCELLED') DEFAULT 'SUCCESS' COMMENT '执行状态',
    error_message TEXT COMMENT '错误信息（如果失败）',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_article (article_id),
    INDEX idx_operation (operation_type),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成日志表';
```

### 9. Agent API密钥表 (agent_api_keys)

管理AI Agent的API访问权限。

```sql
CREATE TABLE agent_api_keys (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Agent信息
    agent_name VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    agent_description TEXT COMMENT 'Agent描述',
    agent_type ENUM('AI_MODEL', 'SEARCH_ENGINE', 'CONTENT_AGGREGATOR', 'CUSTOM') NOT NULL COMMENT 'Agent类型',
    
    -- API密钥
    api_key_hash VARCHAR(255) NOT NULL UNIQUE COMMENT 'API密钥哈希',
    api_key_prefix VARCHAR(10) NOT NULL COMMENT 'API密钥前缀（用于显示）',
    
    -- 权限控制
    permissions JSON NOT NULL COMMENT '权限列表（如：read, write, analyze）',
    rate_limit INT DEFAULT 100 COMMENT '每分钟请求限制',
    daily_limit INT DEFAULT 10000 COMMENT '每日请求限制',
    
    -- 状态
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    last_used_at TIMESTAMP NULL COMMENT '最后使用时间',
    total_requests INT DEFAULT 0 COMMENT '总请求次数',
    
    -- 关联
    user_id BIGINT NOT NULL COMMENT '创建者ID',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_api_key (api_key_hash),
    INDEX idx_user (user_id),
    INDEX idx_agent_name (agent_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent API密钥表';
```

### 10. Agent访问日志表 (agent_access_logs)

记录Agent对博客的访问，用于分析和审计。

```sql
CREATE TABLE agent_access_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Agent信息
    agent_api_key_id BIGINT COMMENT '使用的API密钥ID',
    agent_name VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    agent_ip VARCHAR(45) COMMENT 'Agent IP地址',
    user_agent VARCHAR(500) COMMENT 'User-Agent字符串',
    
    -- 请求信息
    endpoint VARCHAR(200) NOT NULL COMMENT '访问的端点',
    method VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
    query_params JSON COMMENT '查询参数',
    request_body JSON COMMENT '请求体',
    
    -- 响应信息
    response_status INT COMMENT '响应状态码',
    response_time_ms INT COMMENT '响应时间（毫秒）',
    response_size_bytes INT COMMENT '响应大小（字节）',
    
    -- 内容访问
    resource_type ENUM('ARTICLE', 'COMMENT', 'MEDIA', 'API', 'OTHER') COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    
    -- 分析
    purpose ENUM(
        'CONTENT_READ',
        'CONTENT_ANALYZE',
        'SEARCH',
        'INDEX',
        'SUMMARIZE',
        'COMMENT',
        'OTHER'
    ) COMMENT '访问目的',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (agent_api_key_id) REFERENCES agent_api_keys(id) ON DELETE SET NULL,
    INDEX idx_agent (agent_api_key_id),
    INDEX idx_endpoint (endpoint),
    INDEX idx_created (created_at),
    INDEX idx_resource (resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent访问日志表';
```

### 11. 知识库配置表 (knowledge_base_configs)

配置博客作为AI知识库的参数。

```sql
CREATE TABLE knowledge_base_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- 配置信息
    name VARCHAR(100) NOT NULL COMMENT '知识库名称',
    description TEXT COMMENT '知识库描述',
    
    -- 内容范围
    include_categories JSON COMMENT '包含的分类ID列表',
    include_tags JSON COMMENT '包含的标签ID列表',
    include_statuses JSON DEFAULT '["PUBLISHED"]' COMMENT '包含的文章状态',
    date_range_start DATE COMMENT '开始日期',
    date_range_end DATE COMMENT '结束日期',
    
    -- 处理选项
    chunk_size INT DEFAULT 1000 COMMENT '文本分块大小（字符数）',
    chunk_overlap INT DEFAULT 200 COMMENT '分块重叠大小',
    include_metadata BOOLEAN DEFAULT TRUE COMMENT '是否包含元数据',
    include_comments BOOLEAN DEFAULT FALSE COMMENT '是否包含评论',
    
    -- 向量化选项
    embedding_model VARCHAR(100) DEFAULT 'text-embedding-ada-002' COMMENT '嵌入模型',
    embedding_dimensions INT DEFAULT 1536 COMMENT '嵌入维度',
    
    -- 访问控制
    access_level ENUM('PUBLIC', 'PRIVATE', 'RESTRICTED') DEFAULT 'PRIVATE' COMMENT '访问级别',
    allowed_agents JSON COMMENT '允许访问的Agent列表',
    
    -- 状态
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    last_synced_at TIMESTAMP NULL COMMENT '最后同步时间',
    total_documents INT DEFAULT 0 COMMENT '文档总数',
    
    -- 关联
    user_id BIGINT NOT NULL COMMENT '创建者ID',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库配置表';
```

### 12. 知识库文档表 (knowledge_base_documents)

存储知识库中的文档分块。

```sql
CREATE TABLE knowledge_base_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- 文档内容
    content TEXT NOT NULL COMMENT '文档内容',
    content_hash VARCHAR(64) NOT NULL COMMENT '内容哈希（用于去重）',
    
    -- 元数据
    metadata JSON COMMENT '元数据（标题、作者、标签等）',
    chunk_index INT COMMENT '分块索引',
    token_count INT COMMENT 'token数量',
    
    -- 向量嵌入
    embedding BLOB COMMENT '向量嵌入（用于相似度搜索）',
    embedding_model VARCHAR(100) COMMENT '使用的嵌入模型',
    
    -- 来源
    source_type ENUM('ARTICLE', 'COMMENT', 'CUSTOM') NOT NULL COMMENT '来源类型',
    source_id BIGINT COMMENT '来源ID',
    
    -- 状态
    status ENUM('ACTIVE', 'DELETED', 'UPDATED') DEFAULT 'ACTIVE' COMMENT '状态',
    
    -- 关联
    config_id BIGINT NOT NULL COMMENT '知识库配置ID',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (config_id) REFERENCES knowledge_base_configs(id) ON DELETE CASCADE,
    INDEX idx_config (config_id),
    INDEX idx_source (source_type, source_id),
    INDEX idx_content_hash (content_hash),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';
```

---

## 辅助表

### 13. 点赞表 (likes)

记录用户对文章和评论的点赞。

```sql
CREATE TABLE likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID（未登录用户用IP）',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    
    likeable_type ENUM('ARTICLE', 'COMMENT') NOT NULL COMMENT '点赞类型',
    likeable_id BIGINT NOT NULL COMMENT '点赞对象ID',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user (user_id),
    INDEX idx_likeable (likeable_type, likeable_id),
    UNIQUE INDEX idx_unique_like (user_id, likeable_type, likeable_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';
```

### 14. 收藏表 (bookmarks)

```sql
CREATE TABLE bookmarks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    note TEXT COMMENT '收藏备注',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_user_article (user_id, article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
```

### 15. 系统配置表 (system_configs)

存储系统配置项。

```sql
CREATE TABLE system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING' COMMENT '配置类型',
    description VARCHAR(200) COMMENT '配置描述',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开（前端可访问）',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';
```

---

## 初始数据

### 默认分类

```sql
INSERT INTO categories (name, slug, description, sort_order) VALUES
('技术', 'technology', '技术相关文章', 1),
('生活', 'life', '生活随笔', 2),
('AI与未来', 'ai-and-future', 'AI技术和未来趋势', 3),
('教程', 'tutorials', '各种教程', 4),
('思考', 'thoughts', '思考和感悟', 5);
```

### 默认标签

```sql
INSERT INTO tags (name, slug, description, color) VALUES
('Java', 'java', 'Java编程语言', '#007396'),
('Spring Boot', 'spring-boot', 'Spring Boot框架', '#6DB33F'),
('Vue.js', 'vuejs', 'Vue.js前端框架', '#4FC08D'),
('AI', 'ai', '人工智能', '#FF6B6B'),
('机器学习', 'machine-learning', '机器学习', '#4ECDC4'),
('ChatGPT', 'chatgpt', 'ChatGPT相关', '#10A37F'),
('Prompt工程', 'prompt-engineering', '提示词工程', '#9B59B6'),
('Agent', 'agent', 'AI Agent', '#3498DB');
```

### 默认系统配置

```sql
INSERT INTO system_configs (config_key, config_value, config_type, description, is_public) VALUES
('site_name', 'NovaBlog', 'STRING', '网站名称', TRUE),
('site_description', '面向AI时代的个人博客', 'STRING', '网站描述', TRUE),
('site_url', 'https://novablog.example.com', 'STRING', '网站URL', TRUE),
('ai_enabled', 'true', 'BOOLEAN', '是否启用AI功能', TRUE),
('agent_api_enabled', 'true', 'BOOLEAN', '是否启用Agent API', TRUE),
('default_embedding_model', 'text-embedding-ada-002', 'STRING', '默认嵌入模型', FALSE),
('max_upload_size', '10485760', 'NUMBER', '最大上传大小（字节）', FALSE);
```

---

## ER关系图（文本描述）

```
users (1) ──── (N) articles
users (1) ──── (N) comments
users (1) ──── (N) media
users (1) ──── (N) agent_api_keys
users (1) ──── (N) knowledge_base_configs

categories (1) ──── (N) articles
categories (1) ──── (N) categories (自关联，父子分类)

tags (N) ──── (N) articles (通过 article_tags)

articles (1) ──── (N) comments
articles (1) ──── (N) likes
articles (1) ──── (N) bookmarks

comments (1) ──── (N) comments (自关联，多级评论)
comments (1) ──── (N) likes

agent_api_keys (1) ──── (N) agent_access_logs

knowledge_base_configs (1) ──── (N) knowledge_base_documents
```

---

## 设计特点

### 1. AI增强字段
- 文章表包含AI生成的摘要、关键词、质量评分等字段
- 支持AI建议的标签和分类
- 实体识别和结构化数据存储

### 2. Agent友好设计
- 专用的Agent API密钥管理
- 详细的访问日志记录
- 支持Agent评论（标识来源）

### 3. 知识库支持
- 灵活的知识库配置
- 文档分块和向量化存储
- 支持多种嵌入模型

### 4. 扩展性
- JSON字段用于存储灵活的元数据
- 模块化的AI功能日志
- 可配置的系统参数

---

## 下一步

1. 创建SQL迁移文件
2. 实现JPA实体类
3. 设计RESTful API（包括Agent API）
4. 实现AI辅助创作功能
5. 构建知识库系统

---

## 更新日志

- 2026-07-13: 初始版本，包含所有核心表设计
