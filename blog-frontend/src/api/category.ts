import request from './request'

export const categoryApi = {
  getList() {
    return request.get('/api/admin/category/list')
  },
  getById(id: number) {
    return request.get(`/api/admin/category/${id}`)
  },
  create(data: any) {
    return request.post('/api/admin/category', data)
  },
  update(id: number, data: any) {
    return request.put(`/api/admin/category/${id}`, data)
  },
  delete(id: number) {
    return request.delete(`/api/admin/category/${id}`)
  }
}
