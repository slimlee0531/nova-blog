/**
 * NovaBlog 类型定义
 */

// ==================== 通用 ====================

export interface Result<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = any> {
  records: T[]
  total: number
  current: number
  size: number
}

// ==================== 用户 ====================

export interface User {
  id: number
  username: string
  email: string
  displayName?: string
  avatarUrl?: string
  bio?: string
  role: 'ADMIN' | 'EDITOR' | 'AUTHOR'
  isActive: boolean
  lastLoginAt?: string
  createdAt: string
  updatedAt: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  email: string
  password: string
  displayName?: string
}

export interface LoginResult {
  token: string
  user: User
}

// ==================== 文章 ====================

export interface TagInfo {
  id: number
  name: string
  color?: string
}

export interface Article {
  id: number
  title: string
  slug: string
  content: string
  contentHtml?: string
  summary?: string
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'SCHEDULED'
  visibility: 'PUBLIC' | 'PRIVATE' | 'PASSWORD_PROTECTED'
  viewCount: number
  likeCount: number
  commentCount: number
  bookmarkCount: number
  metaTitle?: string
  metaDescription?: string
  ogImage?: string
  authorId: number
  authorName?: string
  authorAvatar?: string
  categoryId?: number
  categoryName?: string
  featuredImage?: string
  tags?: string[]
  tagInfos?: TagInfo[]
  publishedAt?: string
  createdAt: string
  updatedAt: string
  // AI字段
  aiSummary?: string
  aiKeywords?: string
  aiReadingTime?: number
  aiContentQualityScore?: number
  aiSuggestedTags?: string
  structuredData?: string
  entities?: string
}

export interface ArticleCreateParams {
  title: string
  content: string
  categoryId?: number
  tags?: string[]
  status?: string
  visibility?: string
  metaTitle?: string
  metaDescription?: string
  featuredImage?: string
  summary?: string
}

export interface ArticleUpdateParams extends Partial<ArticleCreateParams> {}

// ==================== 分类 ====================

export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  parentId?: number
  sortOrder: number
  articleCount: number
  createdAt: string
  updatedAt: string
}

// ==================== 标签 ====================

export interface Tag {
  id: number
  name: string
  slug: string
  description?: string
  color?: string
  articleCount: number
  createdAt: string
}

// ==================== 评论 ====================

export interface Comment {
  id: number
  content: string
  userId?: number
  authorName?: string
  guestName?: string
  guestEmail?: string
  articleId: number
  parentId?: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SPAM'
  likeCount: number
  createdAt: string
  updatedAt: string
}

export interface CommentCreateParams {
  articleId: number
  content: string
  parentId?: number
}

export interface DashboardStats {
  totalArticles: number
  publishedArticles: number
  draftArticles: number
  totalCategories: number
  totalTags: number
  totalViews: number
  totalComments: number
  totalBookmarks: number
}

// ==================== AI 写作辅助 ====================

export type AiTaskType =
  | 'SUMMARY'
  | 'TITLE'
  | 'TAG'
  | 'POLISH'
  | 'CONTINUE'
  | 'PROOFREAD'
  | 'REPHRASE'
  | 'QUALITY_SCORE'
  | 'SEO_META'
  | 'SUGGEST_OUTLINE'
  | 'TRANSLATE'

export interface AiTaskMeta {
  icon: string
  label: string
  group: '写作' | '优化' | '质量' | 'SEO'
}

export type AiSidebarTab = 'tasks' | 'result' | 'quality' | 'settings'

export interface AiGenerationResult {
  id: string
  taskType: AiTaskType
  status: 'idle' | 'generating' | 'done' | 'error'
  appliedField?: keyof ArticleCreateParams | 'qualityScore'
  inputSnapshot?: Record<string, any>
  fullText: string
  streamingText: string
  tokens?: { promptTokens: number; completionTokens: number }
  latencyMs?: number
  errorCode?: string
  errorMessage?: string
  logId?: number
  createdAt: number
}

export interface AiConfigDTO {
  enabled: boolean
  defaultProvider: string
  providerName: string
  model: string
  apiKeyMasked: string
  apiKeySet: boolean
  timeoutSeconds: number
}

export interface AiTestConnectionResult {
  ok: boolean
  provider: string
  model: string
  latencyMs?: number
  text?: string
  tokens?: { promptTokens: number; completionTokens: number }
  error?: string
}

export interface AiGenerateParams {
  articleId?: number
  taskType: AiTaskType
  params?: Record<string, any>
}

export interface SseProgressEvent {
  seq: number
  text?: string
  stage?: string
  percent?: number
}

export interface SseDoneEvent {
  text: string
  tokens?: { promptTokens: number; completionTokens: number }
  latencyMs: number
  logId: number
}

export interface SseErrorEvent {
  code: string
  message: string
  retryable: boolean
}
