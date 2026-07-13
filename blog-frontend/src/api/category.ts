import request from './request'
import type { Category, Result } from '@/types'

export const categoryApi = {
  getList() {
    return request.get('/api/admin/category/list') as Promise<Result<Category[]>>
  },
  getById(id: number) {
    return request.get(`/api/admin/category/${id}`) as Promise<Result<Category>>
  },
  create(data: Partial<Category>) {
    return request.post('/api/admin/category', data) as Promise<Result<Category>>
  },
  update(id: number, data: Partial<Category>) {
    return request.put(`/api/admin/category/${id}`, data) as Promise<Result<Category>>
  },
  delete(id: number) {
    return request.delete(`/api/admin/category/${id}`) as Promise<Result<void>>
  }
}
