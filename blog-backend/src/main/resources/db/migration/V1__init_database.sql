-- ============================================
-- NovaBlog 数据库初始化脚本
-- 版本: 1.0
-- 创建时间: 2026-07-13
-- 描述: 创建博客系统核心表结构
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 用户表 (users)
-- 存储博主和管理员信息
-- ============================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `display_name` VARCHAR(100) NULL DEFAULT NULL COMMENT '显示名称',
    `avatar_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '头像URL',
    `bio` TEXT NULL COMMENT '个人简介',
    `role` ENUM('ADMIN', 'EDITOR', 'AUTHOR') NOT NULL DEFAULT 'AUTHOR' COMMENT '用户角色',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    INDEX `idx_email` (`email`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 分类表 (categories)
-- 文章分类，支持层级结构
-- ============================================
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `slug` VARCHAR(100) NOT NULL COMMENT 'URL友好的标识',
    `description` TEXT NULL COMMENT '分类描述',
    `parent_id` BIGINT NULL DEFAULT NULL COMMENT '父分类ID，支持多级分类',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `article_count` INT NOT NULL DEFAULT 0 COMMENT '文章数量（冗余字段，便于查询）',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`),
    INDEX `idx_slug` (`slug`),
    INDEX `idx_parent` (`parent_id`),
    CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ============================================
-- 3. 标签表 (tags)
-- 文章标签，用于内容标记和检索
-- ============================================
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `slug` VARCHAR(50) NOT NULL COMMENT 'URL友好的标识',
    `description` VARCHAR(200) NULL DEFAULT NULL COMMENT '标签描述',
    `color` VARCHAR(7) NULL DEFAULT NULL COMMENT '标签颜色（十六进制）',
    `article_count` INT NOT NULL DEFAULT 0 COMMENT '文章数量',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_slug` (`slug`),
    INDEX `idx_slug` (`slug`),
    INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================
-- 4. 文章表 (articles)
-- 核心表，存储博客文章内容，包含AI增强字段
-- ============================================
DROP TABLE IF EXISTS `articles`;
CREATE TABLE `articles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
    `slug` VARCHAR(200) NOT NULL COMMENT 'URL友好的标识',
    `content` LONGTEXT NOT NULL COMMENT '文章内容（Markdown格式）',
    `content_html` LONGTEXT NULL COMMENT '渲染后的HTML内容（缓存）',
    `summary` TEXT NULL COMMENT '文章摘要',

    -- AI增强字段
    `ai_summary` TEXT NULL COMMENT 'AI生成的摘要',
    `ai_keywords` JSON NULL COMMENT 'AI提取的关键词',
    `ai_reading_time` INT NULL COMMENT 'AI估算的阅读时间（分钟）',
    `ai_content_quality_score` DECIMAL(3,2) NULL COMMENT 'AI内容质量评分（0-1）',
    `ai_suggested_tags` JSON NULL COMMENT 'AI建议的标签',
    `ai_suggested_category` VARCHAR(100) NULL COMMENT 'AI建议的分类',

    -- 结构化数据（便于Agent理解）
    `structured_data` JSON NULL COMMENT '结构化元数据（用于Schema.org等）',
    `entities` JSON NULL COMMENT 'AI识别的实体（人物、地点、概念等）',

    -- 状态和元数据
    `status` ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED', 'SCHEDULED') NOT NULL DEFAULT 'DRAFT' COMMENT '文章状态',
    `visibility` ENUM('PUBLIC', 'PRIVATE', 'PASSWORD_PROTECTED') NOT NULL DEFAULT 'PUBLIC' COMMENT '可见性',
    `password` VARCHAR(100) NULL DEFAULT NULL COMMENT '访问密码（如果需要）',

    -- 统计字段
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞次数',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数量',
    `bookmark_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',

    -- SEO字段
    `meta_title` VARCHAR(200) NULL DEFAULT NULL COMMENT 'SEO标题',
    `meta_description` VARCHAR(300) NULL DEFAULT NULL COMMENT 'SEO描述',
    `og_image` VARCHAR(500) NULL DEFAULT NULL COMMENT 'Open Graph图片',

    -- 关联字段
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `category_id` BIGINT NULL DEFAULT NULL COMMENT '分类ID',
    `featured_image` VARCHAR(500) NULL DEFAULT NULL COMMENT '特色图片URL',

    -- 时间字段
    `published_at` TIMESTAMP NULL DEFAULT NULL COMMENT '发布时间',
    `scheduled_at` TIMESTAMP NULL DEFAULT NULL COMMENT '计划发布时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`),
    INDEX `idx_slug` (`slug`),
    INDEX `idx_status` (`status`),
    INDEX `idx_author` (`author_id`),
    INDEX `idx_category` (`category_id`),
    INDEX `idx_published` (`published_at`),
    FULLTEXT INDEX `idx_content_search` (`title`, `content`, `summary`),
    CONSTRAINT `fk_article_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- ============================================
-- 5. 文章标签关联表 (article_tags)
-- ============================================
DROP TABLE IF EXISTS `article_tags`;
CREATE TABLE `article_tags` (
    `article_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`article_id`, `tag_id`),
    CONSTRAINT `fk_at_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_at_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- ============================================
-- 6. 评论表 (comments)
-- 支持人类和AI Agent的评论
-- ============================================
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `content` TEXT NOT NULL COMMENT '评论内容',

    -- 评论者信息
    `user_id` BIGINT NULL DEFAULT NULL COMMENT '用户ID（如果是登录用户）',
    `guest_name` VARCHAR(100) NULL DEFAULT NULL COMMENT '访客名称（未登录用户）',
    `guest_email` VARCHAR(100) NULL DEFAULT NULL COMMENT '访客邮箱（未登录用户）',
    `guest_website` VARCHAR(200) NULL DEFAULT NULL COMMENT '访客网站（未登录用户）',

    -- Agent评论标识
    `is_agent_comment` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为Agent评论',
    `agent_id` VARCHAR(100) NULL DEFAULT NULL COMMENT 'Agent标识（如：openai-gpt4, claude-3）',
    `agent_metadata` JSON NULL COMMENT 'Agent元数据',

    -- 评论关系
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `parent_id` BIGINT NULL DEFAULT NULL COMMENT '父评论ID（支持多级评论）',

    -- 状态
    `status` ENUM('PENDING', 'APPROVED', 'REJECTED', 'SPAM') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',

    -- 统计
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数量',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_article` (`article_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_parent` (`parent_id`),
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_comment_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================
-- 7. 媒体文件表 (media)
-- 存储上传的图片、视频等媒体文件
-- ============================================
DROP TABLE IF EXISTS `media`;
CREATE TABLE `media` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `filename` VARCHAR(255) NOT NULL COMMENT '文件名',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `mime_type` VARCHAR(100) NOT NULL COMMENT 'MIME类型',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `url` VARCHAR(500) NOT NULL COMMENT '访问URL',

    -- 图片特有字段
    `width` INT NULL DEFAULT NULL COMMENT '图片宽度',
    `height` INT NULL DEFAULT NULL COMMENT '图片高度',
    `alt_text` VARCHAR(200) NULL DEFAULT NULL COMMENT '替代文本（SEO和无障碍）',

    -- AI分析字段
    `ai_description` TEXT NULL COMMENT 'AI生成的图片描述',
    `ai_tags` JSON NULL COMMENT 'AI识别的标签',

    -- 元数据
    `metadata` JSON NULL COMMENT '其他元数据',

    -- 关联
    `uploader_id` BIGINT NOT NULL COMMENT '上传者ID',
    `article_id` BIGINT NULL DEFAULT NULL COMMENT '关联文章ID（可选）',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_uploader` (`uploader_id`),
    INDEX `idx_article` (`article_id`),
    INDEX `idx_mime_type` (`mime_type`),
    CONSTRAINT `fk_media_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_media_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体文件表';

-- ============================================
-- 8. AI生成日志表 (ai_generation_logs)
-- 记录所有AI辅助创作的操作日志
-- ============================================
DROP TABLE IF EXISTS `ai_generation_logs`;
CREATE TABLE `ai_generation_logs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,

    -- 操作信息
    `operation_type` ENUM(
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
    `input_data` JSON NULL COMMENT '输入数据（如文章内容、提示词等）',
    `output_data` JSON NULL COMMENT '输出结果',

    -- AI模型信息
    `model_name` VARCHAR(100) NOT NULL COMMENT 'AI模型名称',
    `model_version` VARCHAR(50) NULL DEFAULT NULL COMMENT '模型版本',
    `prompt_template` TEXT NULL COMMENT '使用的提示词模板',

    -- 性能指标
    `tokens_used` INT NULL DEFAULT NULL COMMENT '消耗的token数量',
    `processing_time_ms` INT NULL DEFAULT NULL COMMENT '处理时间（毫秒）',
    `cost_usd` DECIMAL(10,6) NULL DEFAULT NULL COMMENT '成本（美元）',

    -- 关联
    `user_id` BIGINT NULL DEFAULT NULL COMMENT '触发操作的用户',
    `article_id` BIGINT NULL DEFAULT NULL COMMENT '关联的文章',

    -- 状态
    `status` ENUM('SUCCESS', 'FAILED', 'TIMEOUT', 'CANCELLED') NOT NULL DEFAULT 'SUCCESS' COMMENT '执行状态',
    `error_message` TEXT NULL COMMENT '错误信息（如果失败）',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_article` (`article_id`),
    INDEX `idx_operation` (`operation_type`),
    INDEX `idx_created` (`created_at`),
    CONSTRAINT `fk_ai_log_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_ai_log_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成日志表';

-- ============================================
-- 9. Agent API密钥表 (agent_api_keys)
-- 管理AI Agent的API访问权限
-- ============================================
DROP TABLE IF EXISTS `agent_api_keys`;
CREATE TABLE `agent_api_keys` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,

    -- Agent信息
    `agent_name` VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    `agent_description` TEXT NULL COMMENT 'Agent描述',
    `agent_type` ENUM('AI_MODEL', 'SEARCH_ENGINE', 'CONTENT_AGGREGATOR', 'CUSTOM') NOT NULL COMMENT 'Agent类型',

    -- API密钥
    `api_key_hash` VARCHAR(255) NOT NULL COMMENT 'API密钥哈希',
    `api_key_prefix` VARCHAR(10) NOT NULL COMMENT 'API密钥前缀（用于显示）',

    -- 权限控制
    `permissions` JSON NOT NULL COMMENT '权限列表（如：read, write, analyze）',
    `rate_limit` INT NOT NULL DEFAULT 100 COMMENT '每分钟请求限制',
    `daily_limit` INT NOT NULL DEFAULT 10000 COMMENT '每日请求限制',

    -- 状态
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
    `expires_at` TIMESTAMP NULL DEFAULT NULL COMMENT '过期时间',
    `last_used_at` TIMESTAMP NULL DEFAULT NULL COMMENT '最后使用时间',
    `total_requests` INT NOT NULL DEFAULT 0 COMMENT '总请求次数',

    -- 关联
    `user_id` BIGINT NOT NULL COMMENT '创建者ID',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_key_hash` (`api_key_hash`),
    INDEX `idx_api_key` (`api_key_hash`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_agent_name` (`agent_name`),
    CONSTRAINT `fk_api_key_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent API密钥表';

-- ============================================
-- 10. Agent访问日志表 (agent_access_logs)
-- 记录Agent对博客的访问，用于分析和审计
-- ============================================
DROP TABLE IF EXISTS `agent_access_logs`;
CREATE TABLE `agent_access_logs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,

    -- Agent信息
    `agent_api_key_id` BIGINT NULL DEFAULT NULL COMMENT '使用的API密钥ID',
    `agent_name` VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    `agent_ip` VARCHAR(45) NULL DEFAULT NULL COMMENT 'Agent IP地址',
    `user_agent` VARCHAR(500) NULL DEFAULT NULL COMMENT 'User-Agent字符串',

    -- 请求信息
    `endpoint` VARCHAR(200) NOT NULL COMMENT '访问的端点',
    `method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
    `query_params` JSON NULL COMMENT '查询参数',
    `request_body` JSON NULL COMMENT '请求体',

    -- 响应信息
    `response_status` INT NULL DEFAULT NULL COMMENT '响应状态码',
    `response_time_ms` INT NULL DEFAULT NULL COMMENT '响应时间（毫秒）',
    `response_size_bytes` INT NULL DEFAULT NULL COMMENT '响应大小（字节）',

    -- 内容访问
    `resource_type` ENUM('ARTICLE', 'COMMENT', 'MEDIA', 'API', 'OTHER') NULL DEFAULT NULL COMMENT '资源类型',
    `resource_id` BIGINT NULL DEFAULT NULL COMMENT '资源ID',

    -- 分析
    `purpose` ENUM(
        'CONTENT_READ',
        'CONTENT_ANALYZE',
        'SEARCH',
        'INDEX',
        'SUMMARIZE',
        'COMMENT',
        'OTHER'
    ) NULL DEFAULT NULL COMMENT '访问目的',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_agent` (`agent_api_key_id`),
    INDEX `idx_endpoint` (`endpoint`),
    INDEX `idx_created` (`created_at`),
    INDEX `idx_resource` (`resource_type`, `resource_id`),
    CONSTRAINT `fk_access_log_api_key` FOREIGN KEY (`agent_api_key_id`) REFERENCES `agent_api_keys` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent访问日志表';

-- ============================================
-- 11. 知识库配置表 (knowledge_base_configs)
-- 配置博客作为AI知识库的参数
-- ============================================
DROP TABLE IF EXISTS `knowledge_base_configs`;
CREATE TABLE `knowledge_base_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,

    -- 配置信息
    `name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
    `description` TEXT NULL COMMENT '知识库描述',

    -- 内容范围
    `include_categories` JSON NULL COMMENT '包含的分类ID列表',
    `include_tags` JSON NULL COMMENT '包含的标签ID列表',
    `include_statuses` JSON NOT NULL DEFAULT '["PUBLISHED"]' COMMENT '包含的文章状态',
    `date_range_start` DATE NULL DEFAULT NULL COMMENT '开始日期',
    `date_range_end` DATE NULL DEFAULT NULL COMMENT '结束日期',

    -- 处理选项
    `chunk_size` INT NOT NULL DEFAULT 1000 COMMENT '文本分块大小（字符数）',
    `chunk_overlap` INT NOT NULL DEFAULT 200 COMMENT '分块重叠大小',
    `include_metadata` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否包含元数据',
    `include_comments` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否包含评论',

    -- 向量化选项
    `embedding_model` VARCHAR(100) NOT NULL DEFAULT 'text-embedding-ada-002' COMMENT '嵌入模型',
    `embedding_dimensions` INT NOT NULL DEFAULT 1536 COMMENT '嵌入维度',

    -- 访问控制
    `access_level` ENUM('PUBLIC', 'PRIVATE', 'RESTRICTED') NOT NULL DEFAULT 'PRIVATE' COMMENT '访问级别',
    `allowed_agents` JSON NULL COMMENT '允许访问的Agent列表',

    -- 状态
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
    `last_synced_at` TIMESTAMP NULL DEFAULT NULL COMMENT '最后同步时间',
    `total_documents` INT NOT NULL DEFAULT 0 COMMENT '文档总数',

    -- 关联
    `user_id` BIGINT NOT NULL COMMENT '创建者ID',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_active` (`is_active`),
    CONSTRAINT `fk_kb_config_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库配置表';

-- ============================================
-- 12. 知识库文档表 (knowledge_base_documents)
-- 存储知识库中的文档分块
-- ============================================
DROP TABLE IF EXISTS `knowledge_base_documents`;
CREATE TABLE `knowledge_base_documents` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,

    -- 文档内容
    `content` TEXT NOT NULL COMMENT '文档内容',
    `content_hash` VARCHAR(64) NOT NULL COMMENT '内容哈希（用于去重）',

    -- 元数据
    `metadata` JSON NULL COMMENT '元数据（标题、作者、标签等）',
    `chunk_index` INT NULL DEFAULT NULL COMMENT '分块索引',
    `token_count` INT NULL DEFAULT NULL COMMENT 'token数量',

    -- 向量嵌入
    `embedding` BLOB NULL COMMENT '向量嵌入（用于相似度搜索）',
    `embedding_model` VARCHAR(100) NULL DEFAULT NULL COMMENT '使用的嵌入模型',

    -- 来源
    `source_type` ENUM('ARTICLE', 'COMMENT', 'CUSTOM') NOT NULL COMMENT '来源类型',
    `source_id` BIGINT NULL DEFAULT NULL COMMENT '来源ID',

    -- 状态
    `status` ENUM('ACTIVE', 'DELETED', 'UPDATED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',

    -- 关联
    `config_id` BIGINT NOT NULL COMMENT '知识库配置ID',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_config` (`config_id`),
    INDEX `idx_source` (`source_type`, `source_id`),
    INDEX `idx_content_hash` (`content_hash`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_kb_doc_config` FOREIGN KEY (`config_id`) REFERENCES `knowledge_base_configs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

-- ============================================
-- 13. 点赞表 (likes)
-- 记录用户对文章和评论的点赞
-- ============================================
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NULL DEFAULT NULL COMMENT '用户ID（未登录用户用IP）',
    `ip_address` VARCHAR(45) NULL DEFAULT NULL COMMENT 'IP地址',

    `likeable_type` ENUM('ARTICLE', 'COMMENT') NOT NULL COMMENT '点赞类型',
    `likeable_id` BIGINT NOT NULL COMMENT '点赞对象ID',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_likeable` (`likeable_type`, `likeable_id`),
    UNIQUE KEY `idx_unique_like` (`user_id`, `likeable_type`, `likeable_id`),
    CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- ============================================
-- 14. 收藏表 (bookmarks)
-- ============================================
DROP TABLE IF EXISTS `bookmarks`;
CREATE TABLE `bookmarks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `note` TEXT NULL COMMENT '收藏备注',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_article` (`user_id`, `article_id`),
    CONSTRAINT `fk_bookmark_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bookmark_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================
-- 15. 系统配置表 (system_configs)
-- 存储系统配置项
-- ============================================
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT NULL COMMENT '配置值',
    `config_type` ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') NOT NULL DEFAULT 'STRING' COMMENT '配置类型',
    `description` VARCHAR(200) NULL DEFAULT NULL COMMENT '配置描述',
    `is_public` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否公开（前端可访问）',

    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    INDEX `idx_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================
-- 初始数据
-- ============================================

-- 默认分类
INSERT INTO `categories` (`name`, `slug`, `description`, `sort_order`) VALUES
('技术', 'technology', '技术相关文章', 1),
('生活', 'life', '生活随笔', 2),
('AI与未来', 'ai-and-future', 'AI技术和未来趋势', 3),
('教程', 'tutorials', '各种教程', 4),
('思考', 'thoughts', '思考和感悟', 5);

-- 默认标签
INSERT INTO `tags` (`name`, `slug`, `description`, `color`) VALUES
('Java', 'java', 'Java编程语言', '#007396'),
('Spring Boot', 'spring-boot', 'Spring Boot框架', '#6DB33F'),
('Vue.js', 'vuejs', 'Vue.js前端框架', '#4FC08D'),
('AI', 'ai', '人工智能', '#FF6B6B'),
('机器学习', 'machine-learning', '机器学习', '#4ECDC4'),
('ChatGPT', 'chatgpt', 'ChatGPT相关', '#10A37F'),
('Prompt工程', 'prompt-engineering', '提示词工程', '#9B59B6'),
('Agent', 'agent', 'AI Agent', '#3498DB');

-- 默认系统配置
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `is_public`) VALUES
('site_name', 'NovaBlog', 'STRING', '网站名称', 1),
('site_description', '面向AI时代的个人博客', 'STRING', '网站描述', 1),
('site_url', 'https://novablog.example.com', 'STRING', '网站URL', 1),
('ai_enabled', 'true', 'BOOLEAN', '是否启用AI功能', 1),
('agent_api_enabled', 'true', 'BOOLEAN', '是否启用Agent API', 1),
('default_embedding_model', 'text-embedding-ada-002', 'STRING', '默认嵌入模型', 0),
('max_upload_size', '10485760', 'NUMBER', '最大上传大小（字节）', 0);

SET FOREIGN_KEY_CHECKS = 1;
