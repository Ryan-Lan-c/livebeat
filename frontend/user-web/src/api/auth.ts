import http from './http'
import type { TokenResponse } from '@/types'

export interface LoginPayload {
  email: string
  password: string
}

export interface RegisterPayload {
  email: string
  username: string
  password: string
  phone?: string
}

export const authApi = {
  login(payload: LoginPayload) {
    return http.post<TokenResponse>('/auth/login', payload).then((r) => r.data)
  },

  register(payload: RegisterPayload) {
    return http.post<TokenResponse>('/auth/register', payload).then((r) => r.data)
  },

  /** 以 HttpOnly cookie 靜默換新 access token；無有效 cookie 時會 401。 */
  refresh() {
    return http.post<TokenResponse>('/auth/refresh').then((r) => r.data)
  },

  logout() {
    return http.post('/auth/logout')
  },
}
