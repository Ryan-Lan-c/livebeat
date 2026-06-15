import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'
import { createI18n } from 'vue-i18n'

import App from './App.vue'
import router from './router'
import { DEFAULT_LOCALE, messages } from './locales'
import { useAuthStore } from './stores/auth'
import { authApi } from './api/auth'

const i18n = createI18n({
  legacy: false,
  locale: DEFAULT_LOCALE,
  fallbackLocale: DEFAULT_LOCALE,
  messages,
})

const pinia = createPinia()
const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(VueQueryPlugin)
app.use(i18n)

// 啟動時嘗試以 HttpOnly refresh cookie 靜默還原登入（access token 僅存記憶體、不持久化）。
async function bootstrap() {
  const auth = useAuthStore()
  try {
    auth.setAuth(await authApi.refresh())
  } catch {
    // 無有效 refresh cookie：維持未登入狀態
  } finally {
    app.mount('#app')
  }
}

void bootstrap()
