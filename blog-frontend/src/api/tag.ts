import request from './request'
import type { Tag, Result } from '@/types'

export const tagApi = {
  getList() {
    return request.get('/api/admin/tag/list') as Promise<Result<Tag[]>>
  },
  getTagList() {
    return request.get('/api/admin/tag/list') as Promise<Result<Tag[]>>
  },
  getById(id: number) {
    return request.get(`/api/admin/tag/${id}`) as Promise<Result<Tag>>
  },
  create(data: Partial<Tag>) {
    return request.post('/api/admin/tag', data) as Promise<Result<Tag>>
  },
  update(id: number, data: Partial<Tag>) {
    return request.put(`/api/admin/tag/${id}`, data) as Promise<Result<Tag>>
  },
  delete(id: number) {
    return request.delete(`/api/admin/tag/${id}`) as Promise<Result<void>>
  }
}
