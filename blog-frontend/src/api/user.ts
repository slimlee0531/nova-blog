import request from './request'
import type { User, LoginParams, RegisterParams, LoginResult, Result } from '@/types'

export const userApi = {
  register(data: RegisterParams) {
    return request.post('/api/user/register', data) as Promise<Result<LoginResult>>
  },
  login(data: LoginParams) {
    return request.post('/api/user/login', data) as Promise<Result<LoginResult>>
  },
  getInfo() {
    return request.get('/api/user/info') as Promise<Result<User>>
  },
  getUserList(params: { page: number; size: number }) {
    return request.get('/api/user/list', { params }) as Promise<Result<User[]>>
  }
}
