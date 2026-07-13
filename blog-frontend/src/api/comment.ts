import request from './request'

export const commentApi = {
  getByArticle(articleId: number) {
    return request.get(`/api/comments/article/${articleId}`)
  },
  create(data: any) {
    return request.post('/api/comments', data)
  }
}
