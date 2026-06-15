<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const username = ref('')
const password = ref('')
const phone = ref('')
const error = ref<string | null>(null)
const submitting = ref(false)

async function onSubmit() {
  error.value = null
  if (!email.value.includes('@')) {
    error.value = t('auth.emailRequired')
    return
  }
  if (username.value.length < 3) {
    error.value = t('auth.usernameRequired')
    return
  }
  if (password.value.length < 8) {
    error.value = t('auth.passwordRequired')
    return
  }
  submitting.value = true
  try {
    auth.setAuth(
      await authApi.register({
        email: email.value,
        username: username.value,
        password: password.value,
        phone: phone.value || undefined,
      }),
    )
    await router.push({ name: 'concert-list' })
  } catch {
    error.value = t('auth.registerFailed')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="flex min-h-[calc(100vh-8rem)] items-center justify-center">
    <form class="w-full max-w-sm space-y-4" @submit.prevent="onSubmit">
      <h1 class="text-2xl font-bold">{{ t('auth.registerTitle') }}</h1>

      <div class="space-y-2">
        <label class="text-sm font-medium" for="email">{{ t('auth.email') }}</label>
        <Input id="email" v-model="email" type="email" autocomplete="email" />
      </div>

      <div class="space-y-2">
        <label class="text-sm font-medium" for="username">{{ t('auth.username') }}</label>
        <Input id="username" v-model="username" autocomplete="username" />
      </div>

      <div class="space-y-2">
        <label class="text-sm font-medium" for="password">{{ t('auth.password') }}</label>
        <Input id="password" v-model="password" type="password" autocomplete="new-password" />
      </div>

      <div class="space-y-2">
        <label class="text-sm font-medium" for="phone">{{ t('auth.phone') }}</label>
        <Input id="phone" v-model="phone" type="tel" autocomplete="tel" />
      </div>

      <p v-if="error" class="text-sm text-destructive">{{ error }}</p>

      <Button type="submit" class="w-full" :disabled="submitting">{{ t('auth.submitRegister') }}</Button>

      <RouterLink
        to="/login"
        class="block text-center text-sm text-muted-foreground hover:underline"
      >
        {{ t('auth.toLogin') }}
      </RouterLink>
    </form>
  </div>
</template>
