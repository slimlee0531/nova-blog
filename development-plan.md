# NovaBlog 开发计划

## 一、项目架构设计

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      客户端层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   浏览器     │  │   Agent      │  │   AI模型     │      │
│  │  (Vue前端)   │  │  (API调用)   │  │  (知识库)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      网关/认证层                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  JWT认证     │  │  API Key    │  │  权限控制    │      │
│  │  (用户)      │  │  (Agent)    │  │  (RBAC)     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      服务层 (Spring Boot)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Controller  │  │  Service     │  │  Mapper      │      │
│  │  (接口层)    │  │  (业务逻辑)  │  │  (数据访问)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  AI服务      │  │  Agent服务   │  │  知识库服务  │      │
│  │  (创作辅助)  │  │  (API管理)   │  │  (向量搜索)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据层                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   MySQL      │  │   Redis      │  │   文件存储   │      │
│  │  (主数据库)  │  │  (缓存)      │  │  (媒体文件)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 后端分层架构

```
com.slim.blogbackend/
├── config/                 # 配置类
│   ├── MybatisPlusConfig.java
│   ├── WebMvcConfig.java
│   └── SecurityConfig.java
├── controller/             # 控制器层
│   ├── admin/              # 后台管理接口
│   │   ├── ArticleController.java
│   │   ├── CategoryController.java
│   │   └── UserController.java
│   ├── api/                # 前台接口
│   │   ├── ArticleApiController.java
│   │   └── CommentApiController.java
│   └── agent/              # Agent API接口
│       └── AgentApiController.java
├── entity/                 # 实体类 (已完成)
│   └── ...
├── mapper/                 # MyBatis-Plus Mapper
│   └── ...
├── service/                # 服务层
│   ├── ArticleService.java
│   ├── CategoryService.java
│   ├── UserService.java
│   ├── AiService.java
│   ├── AgentService.java
│   └── KnowledgeBaseService.java
├── dto/                    # 数据传输对象
│   ├── request/            # 请求DTO
│   └── response/           # 响应DTO
├── vo/                     # 视图对象
├── util/                   # 工具类
│   ├── JwtUtil.java
│   ├── MarkdownUtil.java
│   └── JsonUtil.java
├── exception/              # 异常处理
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
└── interceptor/            # 拦截器
    ├── JwtInterceptor.java
    └── AgentApiKeyInterceptor.java
```

### 1.3 前端架构

```
blog-frontend/src/
├── api/                    # API接口
│   ├── article.ts
│   ├── user.ts
│   └── agent.ts
├── components/             # 组件
│   ├── common/             # 公共组件
│   ├── article/            # 文章相关组件
│   └── admin/              # 后台管理组件
├── views/                  # 页面
│   ├── Home.vue
│   ├── ArticleDetail.vue
│   └── admin/
│       ├── Dashboard.vue
│       ├── ArticleList.vue
│       └── ArticleEdit.vue
├── router/                 # 路由
│   └── index.ts
├── store/                  # 状态管理 (Pinia)
│   └── user.ts
├── utils/                  # 工具函数
│   ├── request.ts          # Axios封装
│   └── auth.ts             # 认证工具
└── types/                  # TypeScript类型
    └── index.ts
```

---

## 二、分阶段开发计划

### Phase 1: 基础架构搭建 (第1周)

#### 1.1 后端配置
**目标**: 配置Spring Boot基础环境

**任务清单**:
- [ ] 1.1.1 配置application.yml
  - MySQL连接信息
  - MyBatis-Plus配置
  - 日志配置
  - 服务器端口

- [ ] 1.1.2 创建MybatisPlusConfig
  - 分页插件
  - 自动填充处理器
  - 逻辑删除配置

- [ ] 1.1.3 创建WebMvcConfig
  - 跨域配置
  - 静态资源映射
  - 拦截器注册

- [ ] 1.1.4 创建全局异常处理器
  - BusinessException
  - GlobalExceptionHandler

**产出文件**:
```
config/
├── MybatisPlusConfig.java
├── WebMvcConfig.java
└── exception/
    ├── BusinessException.java
    └── GlobalExceptionHandler.java
```

#### 1.2 Mapper层
**目标**: 为所有实体创建Mapper接口

**任务清单**:
- [ ] 1.2.1 创建基础Mapper接口
  - UserMapper
  - ArticleMapper
  - CategoryMapper
  - TagMapper
  - CommentMapper
  - MediaMapper

- [ ] 1.2.2 创建AI/Agent相关Mapper
  - AiGenerationLogMapper
  - AgentApiKeyMapper
  - AgentAccessLogMapper
  - KnowledgeBaseConfigMapper
  - KnowledgeBaseDocumentMapper

- [ ] 1.2.3 创建辅助功能Mapper
  - LikeMapper
  - BookmarkMapper
  - SystemConfigMapper

**产出文件**:
```
mapper/
├── UserMapper.java
├── ArticleMapper.java
├── CategoryMapper.java
├── TagMapper.java
├── CommentMapper.java
├── MediaMapper.java
├── AiGenerationLogMapper.java
├── AgentApiKeyMapper.java
├── AgentAccessLogMapper.java
├── KnowledgeBaseConfigMapper.java
├── KnowledgeBaseDocumentMapper.java
├── LikeMapper.java
├── BookmarkMapper.java
└── SystemConfigMapper.java
```

#### 1.3 DTO/VO层
**目标**: 定义数据传输对象

**任务清单**:
- [ ] 1.3.1 创建请求DTO
  - ArticleCreateDTO
  - ArticleUpdateDTO
  - UserLoginDTO
  - UserRegisterDTO
  - CommentCreateDTO

- [ ] 1.3.2 创建响应DTO
  - ArticleResponseDTO
  - UserResponseDTO
  - CommentResponseDTO
  - AiGenerateResponseDTO

- [ ] 1.3.3 创建分页VO
  - PageResult<T>
  - PageRequest

**产出文件**:
```
dto/
├── request/
│   ├── ArticleCreateDTO.java
│   ├── ArticleUpdateDTO.java
│   ├── UserLoginDTO.java
│   ├── UserRegisterDTO.java
│   └── CommentCreateDTO.java
└── response/
    ├── ArticleResponseDTO.java
    ├── UserResponseDTO.java
    ├── CommentResponseDTO.java
    └── AiGenerateResponseDTO.java

vo/
├── PageResult.java
└── PageRequest.java
```

---

### Phase 2: 核心功能实现 (第2-3周)

#### 2.1 用户认证模块
**目标**: 实现用户注册、登录、JWT认证

**任务清单**:
- [ ] 2.1.1 实现UserService
  - register(): 用户注册
  - login(): 用户登录
  - getUserById(): 获取用户信息
  - updateUser(): 更新用户信息

- [ ] 2.1.2 实现JwtUtil
  - generateToken(): 生成JWT
  - parseToken(): 解析JWT
  - validateToken(): 验证JWT

- [ ] 2.1.3 实现JwtInterceptor
  - 拦截请求验证JWT
  - 将用户信息放入ThreadLocal

- [ ] 2.1.4 创建UserController
  - POST /api/user/register
  - POST /api/user/login
  - GET /api/user/info
  - PUT /api/user/update

**API设计**:
```yaml
# 注册
POST /api/user/register
Request:
  {
    "username": "string",
    "email": "string",
    "password": "string"
  }
Response:
  {
    "code": 200,
    "message": "success",
    "data": {
      "token": "jwt_token",
      "user": {...}
    }
  }

# 登录
POST /api/user/login
Request:
  {
    "username": "string",
    "password": "string"
  }
Response:
  {
    "code": 200,
    "message": "success",
    "data": {
      "token": "jwt_token",
      "user": {...}
    }
  }
```

#### 2.2 文章管理模块
**目标**: 实现文章的CRUD操作

**任务清单**:
- [ ] 2.2.1 实现ArticleService
  - createArticle(): 创建文章
  - updateArticle(): 更新文章
  - deleteArticle(): 删除文章
  - getArticleById(): 获取文章详情
  - getArticleList(): 获取文章列表
  - publishArticle(): 发布文章

- [ ] 2.2.2 实现CategoryService
  - createCategory(): 创建分类
  - updateCategory(): 更新分类
  - deleteCategory(): 删除分类
  - getCategoryTree(): 获取分类树

- [ ] 2.2.3 实现TagService
  - createTag(): 创建标签
  - getTags(): 获取标签列表
  - getArticlesByTag(): 获取标签下的文章

- [ ] 2.2.4 创建ArticleController
  - POST /api/admin/article
  - PUT /api/admin/article/{id}
  - DELETE /api/admin/article/{id}
  - GET /api/admin/article/{id}
  - GET /api/admin/articles

- [ ] 2.2.5 创建ArticleApiController
  - GET /api/articles
  - GET /api/articles/{slug}
  - GET /api/articles/category/{categorySlug}
  - GET /api/articles/tag/{tagSlug}

**API设计**:
```yaml
# 创建文章
POST /api/admin/article
Headers: Authorization: Bearer {token}
Request:
  {
    "title": "string",
    "content": "string (markdown)",
    "categoryId": "number",
    "tags": ["string"],
    "status": "DRAFT|PUBLISHED",
    "visibility": "PUBLIC|PRIVATE",
    "metaTitle": "string",
    "metaDescription": "string"
  }
Response:
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": "number",
      "slug": "string",
      ...
    }
  }

# 获取文章列表
GET /api/articles?page=1&size=10&category=technology
Response:
  {
    "code": 200,
    "message": "success",
    "data": {
      "records": [...],
      "total": 100,
      "page": 1,
      "size": 10
    }
  }
```

#### 2.3 评论模块
**目标**: 实现评论的CRUD和多级评论

**任务清单**:
- [ ] 2.3.1 实现CommentService
  - createComment(): 创建评论
  - deleteComment(): 删除评论
  - getCommentsByArticle(): 获取文章评论
  - approveComment(): 审核评论

- [ ] 2.3.2 创建CommentController
  - POST /api/comment
  - DELETE /api/comment/{id}
  - GET /api/comments/article/{articleId}

**API设计**:
```yaml
# 创建评论
POST /api/comment
Request:
  {
    "articleId": "number",
    "parentId": "number (可选)",
    "content": "string",
    "guestName": "string (未登录时)",
    "guestEmail": "string (未登录时)"
  }
Response:
  {
    "code": 200,
    "message": "success",
    "data": {...}
  }
```

---

### Phase 3: AI功能集成 (第4-5周)

#### 3.1 AI辅助创作服务
**目标**: 集成AI能力，辅助文章创作

**任务清单**:
- [ ] 3.1.1 实现AiService接口
  ```java
  public interface AiService {
      // 生成文章摘要
      String generateSummary(String content);
      
      // 提取关键词
      List<String> extractKeywords(String content);
      
      // 建议标签
      List<String> suggestTags(String content);
      
      // 建议分类
      String suggestCategory(String content);
      
      // 内容质量评分
      BigDecimal analyzeQuality(String content);
      
      // 识别实体
      List<String> extractEntities(String content);
      
      // 生成结构化数据
      Object generateStructuredData(Article article);
  }
  ```

- [ ] 3.1.2 实现OpenAiServiceImpl (使用OpenAI API)
  - 配置API密钥
  - 实现各AI功能
  - 处理API调用异常
  - 记录调用日志

- [ ] 3.1.3 实现AiGenerationLogService
  - 记录AI操作日志
  - 统计token消耗
  - 计算成本

- [ ] 3.1.4 创建AiController
  - POST /api/ai/summarize
  - POST /api/ai/keywords
  - POST /api/ai/tags
  - POST /api/ai/category
  - POST /api/ai/quality
  - POST /api/ai/entities

**API设计**:
```yaml
# 生成摘要
POST /api/ai/summarize
Headers: Authorization: Bearer {token}
Request:
  {
    "content": "string (文章内容)",
    "maxLength": 200
  }
Response:
  {
    "code": 200,
    "message": "success",
    "data": {
      "summary": "string",
      "tokensUsed": 150,
      "processingTimeMs": 1200
    }
  }

# 提取关键词
POST /api/ai/keywords
Request:
  {
    "content": "string",
    "count": 10
  }
Response:
  {
    "code": 200,
    "message": "success",
    "data": {
      "keywords": ["AI", "博客", "技术"],
      "tokensUsed": 100
    }
  }
```

#### 3.2 Agent API服务
**目标**: 为AI Agent提供标准化API

**任务清单**:
- [ ] 3.2.1 实现AgentApiKeyService
  - createApiKey(): 创建API密钥
  - validateApiKey(): 验证API密钥
  - revokeApiKey(): 吊销API密钥
  - getApiKeyUsage(): 获取使用统计

- [ ] 3.2.2 实现AgentAccessLogService
  - logAccess(): 记录访问日志
  - getAccessStats(): 获取访问统计

- [ ] 3.2.3 实现AgentApiKeyInterceptor
  - 验证API Key
  - 检查权限
  - 检查速率限制
  - 记录访问日志

- [ ] 3.2.4 创建AgentApiController
  - GET /api/agent/articles
  - GET /api/agent/articles/{id}
  - GET /api/agent/articles/{id}/structured
  - GET /api/agent/categories
  - GET /api/agent/tags

**API设计**:
```yaml
# Agent获取文章（结构化数据）
GET /api/agent/articles?limit=10
Headers: X-API-Key: {api_key}
Response:
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "title": "文章标题",
        "slug": "article-slug",
        "content": "文章内容",
        "summary": "文章摘要",
        "keywords": ["AI", "技术"],
        "structuredData": {
          "@type": "Article",
          "headline": "文章标题",
          "datePublished": "2026-07-13"
        },
        "metadata": {
          "author": "作者",
          "category": "技术",
          "tags": ["AI", "Java"],
          "readingTime": 5
        }
      }
    ]
  }
```

---

### Phase 4: 知识库系统 (第6周)

#### 4.1 知识库服务
**目标**: 实现知识库的创建、更新和查询

**任务清单**:
- [ ] 4.1.1 实现KnowledgeBaseConfigService
  - createConfig(): 创建知识库配置
  - updateConfig(): 更新配置
  - syncKnowledgeBase(): 同步知识库

- [ ] 4.1.2 实现KnowledgeBaseDocumentService
  - addDocument(): 添加文档
  - updateDocument(): 更新文档
  - deleteDocument(): 删除文档
  - searchDocuments(): 相似度搜索

- [ ] 4.1.3 实现文档分块逻辑
  - 按段落分块
  - 按固定长度分块
  - 保持语义完整性

- [ ] 4.1.4 实现向量化存储
  - 调用embedding模型
  - 存储向量到数据库
  - 实现相似度搜索

**API设计**:
```yaml
# 查询知识库
POST /api/agent/knowledge-base/{configId}/search
Headers: X-API-Key: {api_key}
Request:
  {
    "query": "string",
    "topK": 5
  }
Response:
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "content": "相关文档内容",
        "score": 0.95,
        "metadata": {...}
      }
    ]
  }
```

---

### Phase 5: 前端开发 (第7-9周)

#### 5.1 前端基础架构
**目标**: 搭建Vue项目基础架构

**任务清单**:
- [ ] 5.1.1 安装依赖
  - Vue Router
  - Pinia (状态管理)
  - Axios (HTTP客户端)
  - Element Plus (UI组件库)

- [ ] 5.1.2 配置路由
  - 首页路由
  - 文章详情路由
  - 后台管理路由
  - 登录注册路由

- [ ] 5.1.3 封装Axios
  - 请求拦截器 (添加token)
  - 响应拦截器 (处理错误)
  - API接口封装

- [ ] 5.1.4 实现状态管理
  - 用户状态
  - 文章状态

#### 5.2 前台页面
**目标**: 实现博客前台页面

**任务清单**:
- [ ] 5.2.1 首页
  - 文章列表
  - 分类导航
  - 标签云
  - 分页

- [ ] 5.2.2 文章详情页
  - Markdown渲染
  - 评论列表
  - 点赞/收藏
  - 相关文章推荐

- [ ] 5.2.3 分类/标签页
  - 按分类查看文章
  - 按标签查看文章

#### 5.3 后台管理
**目标**: 实现后台管理界面

**任务清单**:
- [ ] 5.3.1 仪表盘
  - 统计数据
  - 最近文章
  - 最近评论

- [ ] 5.3.2 文章管理
  - 文章列表
  - 创建/编辑文章 (Markdown编辑器)
  - 文章状态管理

- [ ] 5.3.3 分类/标签管理
  - 分类CRUD
  - 标签CRUD

- [ ] 5.3.4 评论管理
  - 评论列表
  - 审核评论

- [ ] 5.3.5 媒体管理
  - 文件上传
  - 图片预览

---

### Phase 6: 测试和优化 (第10周)

#### 6.1 单元测试
**任务清单**:
- [ ] Service层测试
- [ ] Mapper层测试
- [ ] Controller层测试

#### 6.2 集成测试
**任务清单**:
- [ ] API接口测试
- [ ] 认证流程测试
- [ ] AI功能测试

#### 6.3 性能优化
**任务清单**:
- [ ] 数据库索引优化
- [ ] 缓存策略 (Redis)
- [ ] 接口响应时间优化

#### 6.4 安全加固
**任务清单**:
- [ ] SQL注入防护
- [ ] XSS防护
- [ ] CSRF防护
- [ ] 速率限制

---

## 三、技术实现细节

### 3.1 MyBatis-Plus配置

```java
@Configuration
@MapperScan("com.slim.blogbackend.mapper")
public class MybatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
    
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
            
            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

### 3.2 JWT认证实现

```java
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
```

### 3.3 AI服务接口

```java
@Service
@Slf4j
public class OpenAiServiceImpl implements AiService {
    
    @Value("${openai.api-key}")
    private String apiKey;
    
    @Value("${openai.model}")
    private String model;
    
    private final RestTemplate restTemplate;
    private final AiGenerationLogService logService;
    
    @Override
    public String generateSummary(String content) {
        long startTime = System.currentTimeMillis();
        
        String prompt = String.format(
            "请为以下文章生成一个简洁的摘要（不超过200字）：\n\n%s", 
            content
        );
        
        String response = callOpenAiApi(prompt);
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        // 记录日志
        logService.logOperation(
            OperationType.SUMMARY_GENERATION,
            content, response, model, 
            estimateTokens(response), processingTime
        );
        
        return response;
    }
    
    private String callOpenAiApi(String prompt) {
        // 调用OpenAI API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        Map<String, Object> body = Map.of(
            "model", model,
            "messages", List.of(Map.of("role", "user", "content", prompt)),
            "temperature", 0.7
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://api.openai.com/v1/chat/completions",
            request,
            Map.class
        );
        
        return (String) response.getBody()
            .get("choices")
            .get(0)
            .get("message")
            .get("content");
    }
}
```

### 3.4 Agent API密钥验证

```java
@Component
public class AgentApiKeyInterceptor implements HandlerInterceptor {
    
    private final AgentApiKeyService apiKeyService;
    private final AgentAccessLogService logService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        String apiKey = request.getHeader("X-API-Key");
        
        if (StringUtils.isBlank(apiKey)) {
            throw new BusinessException("Missing API Key");
        }
        
        AgentApiKey validKey = apiKeyService.validateApiKey(apiKey);
        
        if (validKey == null || !validKey.getIsActive()) {
            throw new BusinessException("Invalid API Key");
        }
        
        // 检查速率限制
        if (!apiKeyService.checkRateLimit(validKey)) {
            throw new BusinessException("Rate limit exceeded");
        }
        
        // 记录访问日志
        logService.logAccess(validKey, request);
        
        // 将API Key信息放入请求属性
        request.setAttribute("agentApiKey", validKey);
        
        return true;
    }
}
```

---

## 四、数据库扩展

### 4.1 Redis缓存表设计

```sql
-- 缓存热点文章
-- Key: article:{id}
-- Value: Article JSON
-- TTL: 1 hour

-- 缓存文章列表
-- Key: articles:page:{page}:size:{size}
-- Value: List<Article> JSON
-- TTL: 10 minutes

-- 缓存用户Token
-- Key: token:{userId}
-- Value: token string
-- TTL: 24 hours

-- 速率限制
-- Key: rate_limit:{apiKeyId}:{minute}
-- Value: request count
-- TTL: 1 minute
```

### 4.2 定时任务

```java
@Component
public class ScheduledTasks {
    
    // 每天凌晨2点清理过期的API密钥
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredApiKeys() {
        agentApiKeyService.cleanExpired();
    }
    
    // 每小时同步知识库
    @Scheduled(cron = "0 0 * * * ?")
    public void syncKnowledgeBase() {
        knowledgeBaseService.syncAll();
    }
    
    // 每天统计访问日志
    @Scheduled(cron = "0 0 1 * * ?")
    public void aggregateAccessLogs() {
        logService.aggregateDailyStats();
    }
}
```

---

## 五、部署方案

### 5.1 Docker部署

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: novablog
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./blog-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MYSQL_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis

  frontend:
    build: ./blog-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

### 5.2 环境配置

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST}:3306/novablog
    username: root
    password: ${MYSQL_PASSWORD}
  
  redis:
    host: ${REDIS_HOST}
    port: 6379

openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-4

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400
```

---

## 六、开发时间表

| 阶段 | 任务 | 时间 | 产出 |
|------|------|------|------|
| Phase 1 | 基础架构搭建 | 第1周 | 配置、Mapper、DTO |
| Phase 2 | 核心功能实现 | 第2-3周 | 用户、文章、评论模块 |
| Phase 3 | AI功能集成 | 第4-5周 | AI服务、Agent API |
| Phase 4 | 知识库系统 | 第6周 | 知识库服务 |
| Phase 5 | 前端开发 | 第7-9周 | 前台、后台页面 |
| Phase 6 | 测试和优化 | 第10周 | 测试、优化、部署 |

**总计**: 10周

---

## 七、风险评估

### 7.1 技术风险
| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| AI API调用失败 | AI功能不可用 | 实现降级策略，使用本地模型 |
| 数据库性能问题 | 响应慢 | 优化索引，使用Redis缓存 |
| 前端构建失败 | 无法部署 | 固定依赖版本，CI/CD |

### 7.2 进度风险
| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 需求变更 | 开发周期延长 | 预留20%缓冲时间 |
| 技术难点 | 开发阻塞 | 提前调研，准备备选方案 |
| 人员变动 | 知识断层 | 完善文档，代码评审 |

---

## 八、质量保证

### 8.1 代码规范
- 使用Lombok简化代码
- 遵循命名规范
- 添加必要的注释
- 代码评审

### 8.2 测试覆盖
- 单元测试覆盖率 > 80%
- 集成测试覆盖核心流程
- API接口测试

### 8.3 性能指标
- API响应时间 < 200ms (95%)
- 并发用户 > 100
- 数据库查询 < 50ms

---

## 九、文档要求

### 9.1 技术文档
- API接口文档 (Swagger)
- 数据库设计文档
- 架构设计文档

### 9.2 用户文档
- 部署文档
- 使用手册
- 开发指南

---

## 十、后续扩展

### 10.1 功能扩展
- 多语言支持
- 付费内容
- 邮件订阅
- 社交登录

### 10.2 技术升级
- 微服务架构
- 事件驱动
- 消息队列
- 容器编排

---

**文档版本**: 1.0
**创建时间**: 2026-07-13
**最后更新**: 2026-07-13
