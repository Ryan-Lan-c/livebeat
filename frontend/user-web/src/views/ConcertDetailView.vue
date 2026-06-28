<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useQuery } from '@tanstack/vue-query'
import { AxiosError } from 'axios'
import dayjs from 'dayjs'
import { concertsApi } from '@/api/concerts'
import { ordersApi } from '@/api/orders'
import { useAuthStore } from '@/stores/auth'
import { Button } from '@/components/ui/button'
import type { ConcertSession, TicketZone } from '@/types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const id = computed(() => String(route.params.id))

const { data: concert, isLoading, isError } = useQuery({
  queryKey: ['concert', id],
  queryFn: () => concertsApi.detail(id.value),
})

function formatDateTime(iso: string | null) {
  return iso ? dayjs(iso).format('YYYY-MM-DD HH:mm') : '—'
}

// 購買：就地展開數量選擇，一次只展開一個票區。
const buyingZoneId = ref<string | null>(null)
const qty = ref(1)
const submitting = ref(false)
const buyError = ref<string | null>(null)

function canBuy(session: ConcertSession, zone: TicketZone) {
  return session.status === 'ON_SALE' && zone.availableSeats > 0
}

function maxQty(session: ConcertSession, zone: TicketZone) {
  return Math.min(session.maxTicketsPerOrder, zone.availableSeats)
}

function openBuy(zone: TicketZone) {
  buyingZoneId.value = zone.id
  qty.value = 1
  buyError.value = null
}

async function confirmBuy(session: ConcertSession, zone: TicketZone) {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  submitting.value = true
  buyError.value = null
  try {
    const order = await ordersApi.create({
      sessionId: session.id,
      zoneId: zone.id,
      quantity: qty.value,
      idempotencyKey: crypto.randomUUID(),
    })
    router.push({
      name: 'order',
      params: { id: order.orderId },
      // 訂單頁第一眼即有完整摘要（OrderResponse 不含這些欄位）；重整後 history.state 仍在。
      state: {
        summary: JSON.stringify({
          concertTitle: concert.value?.title ?? '',
          sessionName: session.sessionName,
          zoneName: zone.zoneName,
          unitPrice: zone.price,
          quantity: qty.value,
        }),
      },
    })
  } catch (e) {
    const code = e instanceof AxiosError ? e.response?.data?.code : undefined
    const key = `errors.${code}`
    buyError.value = t(key) === key ? t('errors.default') : t(key)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="space-y-6">
    <RouterLink to="/" class="text-sm text-muted-foreground hover:underline">
      {{ t('concert.backToList') }}
    </RouterLink>

    <p v-if="isLoading" class="text-muted-foreground">{{ t('concert.loading') }}</p>
    <p v-else-if="isError || !concert" class="text-destructive">{{ t('concert.loadError') }}</p>

    <template v-else>
      <header class="space-y-2">
        <div class="flex items-center gap-3">
          <h1 class="text-3xl font-bold">{{ concert.title }}</h1>
          <span class="rounded-full bg-secondary px-2 py-0.5 text-xs text-secondary-foreground">
            {{ t(`status.${concert.status}`) }}
          </span>
        </div>
        <p class="text-lg text-muted-foreground">{{ concert.artist }}</p>
        <p class="text-sm text-muted-foreground">{{ concert.venue }} · {{ concert.city }}</p>
      </header>

      <img
        v-if="concert.imageUrl"
        :src="concert.imageUrl"
        :alt="concert.title"
        class="max-h-96 w-full rounded-xl object-cover"
      >

      <p v-if="concert.description" class="whitespace-pre-line text-foreground/90">
        {{ concert.description }}
      </p>

      <section class="space-y-4">
        <h2 class="text-xl font-semibold">{{ t('concert.sessions') }}</h2>
        <p v-if="concert.sessions.length === 0" class="text-muted-foreground">
          {{ t('concert.noSessions') }}
        </p>

        <div
          v-for="session in concert.sessions"
          :key="session.id"
          class="space-y-3 rounded-xl border p-4"
        >
          <div class="flex items-center justify-between gap-2">
            <h3 class="font-semibold">{{ session.sessionName }}</h3>
            <span class="rounded-full bg-secondary px-2 py-0.5 text-xs text-secondary-foreground">
              {{ t(`status.${session.status}`) }}
            </span>
          </div>
          <p class="text-sm text-muted-foreground">
            {{ t('concert.eventDate') }}：{{ formatDateTime(session.eventDate) }}
          </p>
          <p class="text-sm text-muted-foreground">
            {{ t('concert.saleWindow') }}：{{ formatDateTime(session.saleStartAt) }} ~
            {{ formatDateTime(session.saleEndAt) }}
          </p>

          <ul v-if="session.zones.length" class="divide-y rounded-lg border">
            <li
              v-for="zone in session.zones"
              :key="zone.id"
              class="px-3 py-2 text-sm"
            >
              <div class="flex items-center justify-between gap-2">
                <span class="font-medium">{{ zone.zoneName }}</span>
                <span class="flex items-center gap-3">
                  <span>NT$ {{ zone.price.toLocaleString() }}</span>
                  <span v-if="zone.availableSeats > 0" class="text-muted-foreground">
                    {{ t('concert.available', { count: zone.availableSeats }) }}
                  </span>
                  <span v-else class="text-destructive">{{ t('concert.soldOut') }}</span>
                  <Button
                    v-if="canBuy(session, zone) && buyingZoneId !== zone.id"
                    size="sm"
                    @click="openBuy(zone)"
                  >
                    {{ t('concert.buy') }}
                  </Button>
                  <span
                    v-else-if="zone.availableSeats > 0 && session.status !== 'ON_SALE'"
                    class="text-muted-foreground"
                  >
                    {{ t('concert.notOnSale') }}
                  </span>
                </span>
              </div>

              <!-- 就地數量選擇 -->
              <div
                v-if="buyingZoneId === zone.id"
                class="mt-2 flex flex-wrap items-center gap-3 rounded-md bg-muted/50 p-2"
              >
                <span>{{ t('concert.quantity') }}</span>
                <div class="flex items-center gap-2">
                  <Button variant="outline" size="sm" :disabled="qty <= 1" @click="qty--">−</Button>
                  <span class="w-8 text-center tabular-nums">{{ qty }}</span>
                  <Button
                    variant="outline"
                    size="sm"
                    :disabled="qty >= maxQty(session, zone)"
                    @click="qty++"
                  >
                    +
                  </Button>
                </div>
                <span class="text-muted-foreground">
                  {{ t('concert.subtotal') }}：NT$ {{ (zone.price * qty).toLocaleString() }}
                </span>
                <Button size="sm" :disabled="submitting" @click="confirmBuy(session, zone)">
                  {{ t('concert.confirmPurchase') }}
                </Button>
                <Button variant="ghost" size="sm" @click="buyingZoneId = null">
                  {{ t('concert.cancel') }}
                </Button>
                <p v-if="buyError" class="w-full text-destructive">{{ buyError }}</p>
              </div>
            </li>
          </ul>
        </div>
      </section>
    </template>
  </div>
</template>
