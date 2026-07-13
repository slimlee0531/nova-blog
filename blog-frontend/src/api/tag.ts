import request from './request'

export const tagApi = {
  getList() {
    return request.get('/api/admin/tag/list')
  },
  getById(id: number) {
    return request.get(`/api/admin/tag/${id}`)
  },
  create(data: any) {
    return request.post('/api/admin/tag', data)
  },
  update(id: number, data: any) {
    return request.put(`/api/admin/tag/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/api/admin/tag/${id}`)
  }
}
