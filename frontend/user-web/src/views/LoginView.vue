<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref<string | null>(null)
const submitting = ref(false)

async function onSubmit() {
  error.value = null
  if (!email.value.includes('@')) {
    error.value = t('auth.emailRequired')
    return
  }
  if (!password.value) {
    error.value = t('auth.passwordRequired')
    return
  }
  submitting.value = true
  try {
    auth.setAuth(await authApi.login({ email: email.value, password: password.value }))
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
    await router.push(redirect ?? { name: 'concert-list' })
  } catch {
    error.value = t('auth.loginFailed')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="flex min-h-[calc(100vh-8rem)] items-center justify-center">
    <form class="w-full max-w-sm space-y-4" @submit.prevent="onSubmit">
      <h1 class="text-2xl font-bold">{{ t('auth.loginTitle') }}</h1>

      <div class="space-y-2">
        <label class="text-sm font-medium" for="email">{{ t('auth.email') }}</label>
        <Input id="email" v-model="email" type="email" autocomplete="email" />
      </div>

      <div class="space-y-2">
        <label class="text-sm font-medium" for="password">{{ t('auth.password') }}</label>
        <Input id="password" v-model="password" type="password" autocomplete="current-password" />
      </div>

      <p v-if="error" class="text-sm text-destructive">{{ error }}</p>

      <Button type="submit" class="w-full" :disabled="submitting">{{ t('auth.submitLogin') }}</Button>

      <RouterLink
        to="/register"
        class="block text-center text-sm text-muted-foreground hover:underline"
      >
        {{ t('auth.toRegister') }}
      </RouterLink>
    </form>
  </div>
</template>
