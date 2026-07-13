import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<any>(null)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
    userInfo.value = null
  }

  const login = async (data: any) => {
    const res: any = await userApi.login(data)
    if (res.code === 200) {
      setToken(res.data.token)
      userInfo.value = res.data.user
      return true
    }
    return false
  }

  const register = async (data: any) => {
    const res: any = await userApi.register(data)
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
      const res: any = await userApi.getInfo()
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
