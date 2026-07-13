import request from './request'

export const userApi = {
  register(data: any) {
    return request.post('/api/user/register', data)
  },
  login(data: any) {
    return request.post('/api/user/login', data)
  },
  getInfo() {
    return request.get('/api/user/info')
  },
  getUserList(params: any) {
    return request.get('/api/user/list', { params })
  }
}
