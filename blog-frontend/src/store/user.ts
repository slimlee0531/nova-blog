import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api/user'
import type { User, LoginParams, RegisterParams, LoginResult, Result } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
    userInfo.value = null
  }

  const login = async (data: LoginParams) => {
    const res = await userApi.login(data) as Result<LoginResult>
    if (res.code === 200) {
      setToken(res.data.token)
      userInfo.value = res.data.user
      return true
    }
    return false
  }

  const register = async (data: RegisterParams) => {
    const res = await userApi.register(data) as Result<LoginResult>
    if (res.code === 200) {
      setToken(res.data.token)
      userInfo.value = res.data.user
      return true
    }
    return false
  }

  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      const res = await userApi.getInfo() as Result<User>
      if (res.code === 200) {
        userInfo.value = res.data
      }
    } catch (e) {
      clearToken()
    }
  }

  const logout = () => {
    clearToken()
  }

  return {
    token,
    userInfo,
    setToken,
    clearToken,
    login,
    register,
    fetchUserInfo,
    logout
  }
})
