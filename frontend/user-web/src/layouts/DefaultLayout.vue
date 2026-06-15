<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { Button } from '@/components/ui/button'

const auth = useAuthStore()
const router = useRouter()
const { t } = useI18n()

async function logout() {
  try {
    // 呼叫後端撤銷 server 端 refresh token 並清除 cookie
    await authApi.logout()
  } finally {
    auth.clearAuth()
    await router.push({ name: 'login' })
  }
}
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <header class="border-b">
      <nav class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <RouterLink to="/" class="text-xl font-bold">{{ t('app.name') }}</RouterLink>
        <div class="flex items-center gap-4">
          <template v-if="auth.isAuthenticated">
            <span class="text-sm text-muted-foreground">{{ auth.user?.username }}</span>
            <Button variant="ghost" size="sm" @click="logout">{{ t('nav.logout') }}</Button>
          </template>
          <template v-else>
            <RouterLink to="/login">
              <Button variant="ghost" size="sm">{{ t('nav.login') }}</Button>
            </RouterLink>
            <RouterLink to="/register">
              <Button size="sm">{{ t('nav.register') }}</Button>
            </RouterLink>
          </template>
        </div>
      </nav>
    </header>

    <main class="mx-auto max-w-7xl px-4 py-8">
      <RouterView />
    </main>
  </div>
</template>
