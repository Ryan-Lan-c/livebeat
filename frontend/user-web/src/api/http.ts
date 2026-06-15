import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'
import type { TokenResponse } from '@/types'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 10000,
  // 帶上 HttpOnly refresh cookie（Path=/api/v1/auth）；後端 CORS allowCredentials=true 對應
  withCredentials: true,
})

const REFRESH_URL = '/auth/refresh'

// 附上 access token（存於記憶體）
http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

// 並發的 401 共用同一個進行中的 refresh，避免重複呼叫
let refreshing: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const auth = useAuthStore()
  // 用 http 呼叫 refresh；其本身的 401 由下方 isRefreshCall 判斷略過攔截，不會遞迴
  const { data } = await http.post<TokenResponse>(REFRESH_URL)
  auth.setAuth(data)
  return data.accessToken
}

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    const isRefreshCall = original?.url?.includes(REFRESH_URL) ?? false

    if (error.response?.status === 401 && original && !original._retry && !isRefreshCall) {
      original._retry = true
      try {
        refreshing ??= refreshAccessToken().finally(() => {
          refreshing = null
        })
        const token = await refreshing
        original.headers.Authorization = `Bearer ${token}`
        return http(original)
      } catch (refreshError) {
        // refresh 失敗：登出並導回登入頁（用 router，不做 hard reload）
        useAuthStore().clearAuth()
        const current = router.currentRoute.value
        if (current.name !== 'login') {
          await router.push({ name: 'login', query: { redirect: current.fullPath } })
        }
        return Promise.reject(refreshError)
      }
    }
    return Promise.reject(error)
  },
)

export default http
