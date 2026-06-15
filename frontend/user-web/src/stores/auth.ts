import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { TokenResponse, User, UserRole } from '@/types'

/**
 * 認證狀態。
 *
 * Access token 僅存於記憶體（不落 localStorage，符合 docs/06-security 的「Access Token 不存
 * localStorage」原則，避免 XSS 竊取）。重新整理後由 /auth/refresh（HttpOnly cookie）靜默換新還原。
 */
export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!accessToken.value)
  const role = computed<UserRole | null>(() => user.value?.role ?? null)

  function setAuth(token: TokenResponse) {
    accessToken.value = token.accessToken
    user.value = {
      userId: token.userId,
      email: token.email,
      username: token.username,
      role: token.role,
    }
  }

  function clearAuth() {
    accessToken.value = null
    user.value = null
  }

  return { accessToken, user, isAuthenticated, role, setAuth, clearAuth }
})
