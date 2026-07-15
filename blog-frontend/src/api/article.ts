import request from './request'
import type { Article, ArticleCreateParams, ArticleUpdateParams, PageResult, Result, DashboardStats } from '@/types'

export const articleApi = {
  // 前台接口
  getArticles(params: { page: number; size: number }) {
    return request.get('/api/articles', { params }) as Promise<Result<PageResult<Article>>>
  },
  getArticleBySlug(slug: string) {
    return request.get(`/api/articles/detail/${slug}`) as Promise<Result<Article>>
  },
  getArticleById(id: number) {
    return request.get(`/api/articles/${id}`) as Promise<Result<Article>>
  },
  getArticlesByCategory(categorySlug: string) {
    return request.get(`/api/articles/category/${categorySlug}`) as Promise<Result<Article[]>>
  },
  getArticlesByTag(tagSlug: string) {
    return request.get(`/api/articles/tag/${tagSlug}`) as Promise<Result<Article[]>>
  },

  // 后台接口
  adminGetArticles(params: { page: number; size: number; status?: string }) {
    return request.get('/api/admin/article/list', { params }) as Promise<Result<PageResult<Article>>>
  },
  adminGetArticle(id: number) {
    return request.get(`/api/admin/article/${id}`) as Promise<Result<Article>>
  },
  adminCreateArticle(data: ArticleCreateParams) {
    return request.post('/api/admin/article', data) as Promise<Result<Article>>
  },
  adminUpdateArticle(id: number, data: ArticleUpdateParams) {
    return request.put(`/api/admin/article/${id}`, data) as Promise<Result<Article>>
  },
  adminDeleteArticle(id: number) {
    return request.delete(`/api/admin/article/${id}`) as Promise<Result<void>>
  },

  adminGetDashboardStats() {
    return request.get('/api/admin/dashboard/stats') as Promise<Result<DashboardStats>>
  }
}
