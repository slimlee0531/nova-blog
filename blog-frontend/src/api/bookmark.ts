import request from './request'
import type { PageResult, Result } from '@/types'

export interface BookmarkItem {
  id: number
  articleId: number
  title: string
  slug: string
  summary?: string
  viewCount: number
  likeCount: number
  commentCount: number
  bookmarkCount: number
  featuredImage?: string
  publishedAt?: string
  categoryName?: string
  createdAt: string
}

export interface BookmarkToggleResult {
  bookmarked: boolean
  bookmarkCount: number
}

export const bookmarkApi = {
  // 切换收藏状态
  toggleBookmark(articleId: number) {
    return request.post('/api/bookmarks/toggle', { articleId }) as Promise<Result<BookmarkToggleResult>>
  },

  // 检查收藏状态
  checkBookmark(articleId: number) {
    return request.get(`/api/bookmarks/check/${articleId}`) as Promise<Result<{ bookmarked: boolean; bookmarkCount: number }>>
  },

  // 获取收藏列表
  getBookmarks(params: { page: number; size: number }) {
    return request.get('/api/bookmarks', { params }) as Promise<Result<PageResult<BookmarkItem>>>
  },

  // 获取收藏数量
  getBookmarkCount() {
    return request.get('/api/bookmarks/count') as Promise<Result<number>>
  }
}
