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
