<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { useNow } from '@vueuse/core'
import { AxiosError } from 'axios'
import { ordersApi } from '@/api/orders'
import { Button } from '@/components/ui/button'
import type { OrderResponse } from '@/types'

const { t } = useI18n()
const route = useRoute()
const queryClient = useQueryClient()
const id = computed(() => String(route.params.id))

// 下單頁透過 router state 帶來的摘要（OrderResponse 不含這些欄位）。
// 直接開連結 / 新分頁時為 null，退化為僅顯示金額與狀態。
interface OrderSummary {
  concertTitle: string
  sessionName: string
  zoneName: string
  unitPrice: number
  quantity: number
}
const summary = ((): OrderSummary | null => {
  try {
    const raw = history.state?.summary
    return raw ? (JSON.parse(raw) as OrderSummary) : null
  } catch {
    return null
  }
})()

const { data: order, isLoading, isError } = useQuery({
  queryKey: ['order', id],
  queryFn: () => ordersApi.get(id.value),
})

// 倒數：以後端 expiresAt 為準，每秒重算。歸零即視為過期（OrderExpiryJob 會稍後回補庫存）。
const now = useNow({ interval: 1000 })
const secondsLeft = computed(() => {
  if (order.value?.status !== 'PENDING' || !order.value.expiresAt) return 0
  return Math.max(
    0,
    Math.floor((new Date(order.value.expiresAt).getTime() - now.value.getTime()) / 1000),
  )
})
const isExpired = computed(() => order.value?.status === 'PENDING' && secondsLeft.value <= 0)
const countdown = computed(() => {
  const s = secondsLeft.value
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`
})

const payError = ref<string | null>(null)
const { mutate: payNow, isPending: paying } = useMutation({
  mutationFn: () => ordersApi.pay(id.value),
  onSuccess: (updated) => {
    queryClient.setQueryData<OrderResponse>(['order', id.value], updated)
    payError.value = null
  },
  onError: (e) => {
    const code = e instanceof AxiosError ? e.response?.data?.code : undefined
    const key = `errors.${code}`
    payError.value = t(key) === key ? t('errors.default') : t(key)
  },
})
</script>

<template>
  <div class="mx-auto max-w-lg space-y-6">
    <p v-if="isLoading" class="text-muted-foreground">{{ t('order.loading') }}</p>
    <p v-else-if="isError || !order" class="text-destructive">{{ t('order.loadError') }}</p>

    <template v-else>
      <!-- 付款成功 -->
      <div v-if="order.status === 'PAID'" class="space-y-3 rounded-xl border p-6 text-center">
        <div class="text-5xl">✅</div>
        <h1 class="text-2xl font-bold">{{ t('order.paidTitle') }}</h1>
        <p class="text-muted-foreground">{{ t('order.orderNo') }}：{{ order.orderNo }}</p>
        <p class="text-sm text-muted-foreground">{{ t('order.paidHint') }}</p>
        <p class="font-semibold">{{ t('order.total') }}：NT$ {{ order.totalAmount.toLocaleString() }}</p>
      </div>

      <!-- 已取消 / 退款 -->
      <div
        v-else-if="order.status === 'CANCELLED' || order.status === 'REFUNDED'"
        class="space-y-2 rounded-xl border p-6 text-center"
      >
        <h1 class="text-xl font-bold">{{ t('order.cancelledTitle') }}</h1>
        <p class="text-muted-foreground">{{ t(`orderStatus.${order.status}`) }}</p>
      </div>

      <!-- 待付款 -->
      <div v-else class="space-y-4 rounded-xl border p-6">
        <h1 class="text-2xl font-bold">{{ t('order.title') }}</h1>
        <p class="text-sm text-muted-foreground">{{ t('order.orderNo') }}：{{ order.orderNo }}</p>

        <div v-if="!isExpired" class="rounded-lg bg-muted/50 p-3 text-center">
          <p class="text-sm text-muted-foreground">{{ t('order.timeLeft') }}</p>
          <p
            class="text-3xl font-bold tabular-nums"
            :class="secondsLeft <= 60 ? 'text-destructive' : ''"
          >
            {{ countdown }}
          </p>
        </div>
        <p v-else class="rounded-lg bg-destructive/10 p-3 text-center text-destructive">
          {{ t('order.expired') }}
        </p>

        <dl class="space-y-1 text-sm">
          <div v-if="summary" class="flex justify-between">
            <dt class="text-muted-foreground">{{ t('order.concert') }}</dt>
            <dd>{{ summary.concertTitle }}</dd>
          </div>
          <div v-if="summary" class="flex justify-between">
            <dt class="text-muted-foreground">{{ t('order.session') }}</dt>
            <dd>{{ summary.sessionName }}</dd>
          </div>
          <div v-if="summary" class="flex justify-between">
            <dt class="text-muted-foreground">{{ t('order.zone') }}</dt>
            <dd>{{ summary.zoneName }}</dd>
          </div>
          <div v-if="summary" class="flex justify-between">
            <dt class="text-muted-foreground">{{ t('order.quantity') }}</dt>
            <dd>{{ summary.quantity }}</dd>
          </div>
          <div class="flex justify-between border-t pt-1 font-semibold">
            <dt>{{ t('order.total') }}</dt>
            <dd>NT$ {{ order.totalAmount.toLocaleString() }}</dd>
          </div>
        </dl>

        <Button class="w-full" :disabled="isExpired || paying" @click="payNow()">
          {{ paying ? t('order.paying') : t('order.payNow') }}
        </Button>
        <p v-if="payError" class="text-sm text-destructive">{{ payError }}</p>
      </div>

      <RouterLink to="/" class="block text-center text-sm text-muted-foreground hover:underline">
        {{ t('order.backToConcerts') }}
      </RouterLink>
    </template>
  </div>
</template>
