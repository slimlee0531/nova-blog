import request from './request'
import type { Comment, CommentCreateParams, Result } from '@/types'

export const commentApi = {
  getCommentsByArticle(articleId: number) {
    return request.get(`/api/comments/article/${articleId}`) as Promise<Result<Comment[]>>
  },
  createComment(data: CommentCreateParams) {
    return request.post('/api/comments', data) as Promise<Result<Comment>>
  }
}
