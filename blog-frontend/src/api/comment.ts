import request from './request'
import type { Comment, CommentCreateParams, PageResult, Result } from '@/types'

export const commentApi = {
  getCommentsByArticle(articleId: number) {
    return request.get(`/api/comments/article/${articleId}`) as Promise<Result<Comment[]>>
  },
  createComment(data: CommentCreateParams) {
    return request.post('/api/comments', data) as Promise<Result<Comment>>
  },

  // 管理接口
  adminGetComments(params: { page: number; size: number; status?: string; articleId?: number }) {
    return request.get('/api/admin/comments/list', { params }) as Promise<Result<PageResult<Comment>>>
  },
  adminApproveComment(id: number) {
    return request.put(`/api/admin/comments/${id}/approve`) as Promise<Result<void>>
  },
  adminRejectComment(id: number) {
    return request.put(`/api/admin/comments/${id}/reject`) as Promise<Result<void>>
  },
  adminDeleteComment(id: number) {
    return request.delete(`/api/admin/comments/${id}`) as Promise<Result<void>>
  }
}
