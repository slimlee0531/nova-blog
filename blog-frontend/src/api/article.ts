import request from './request'

export const articleApi = {
  // 前台接口
  getArticles(params: any) {
    return request.get('/api/articles', { params })
  },
  getArticleBySlug(slug: string) {
    return request.get(`/api/articles/${slug}`)
  },
  getArticleById(id: number) {
    return request.get(`/api/articles/${id}`)
  },
  getArticlesByCategory(categorySlug: string) {
    return request.get(`/api/articles/category/${categorySlug}`)
  },
  getArticlesByTag(tagSlug: string) {
    return request.get(`/api/articles/tag/${tagSlug}`)
  },

  // 后台接口
  adminGetArticles(params: any) {
    return request.get('/api/admin/article/list', { params })
  },
  adminGetArticle(id: number) {
    return request.get(`/api/admin/article/${id}`)
  },
  adminCreateArticle(data: any) {
    return request.post('/api/admin/article', data)
  },
  adminUpdateArticle(id: number, data: any) {
    return request.put(`/api/admin/article/${id}`, data)
  },
  adminDeleteArticle(id: number) {
    return request.delete(`/api/admin/article/${id}`)
  }
}
