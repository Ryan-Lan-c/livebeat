<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useQuery } from '@tanstack/vue-query'
import dayjs from 'dayjs'
import { concertsApi } from '@/api/concerts'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

const { t } = useI18n()

const PAGE_SIZE = 20
const searchInput = ref('')
const query = ref('') // 已套用的搜尋字（按下搜尋才生效）
const page = ref(0)

const { data, isLoading, isError } = useQuery({
  queryKey: ['concerts', query, page],
  queryFn: () => concertsApi.list({ q: query.value || undefined, page: page.value, size: PAGE_SIZE }),
})

const concerts = computed(() => data.value?.content ?? [])
const totalPages = computed(() => data.value?.totalPages ?? 0)
const isLast = computed(() => data.value?.last ?? true)

function applySearch() {
  query.value = searchInput.value.trim()
  page.value = 0
}
function prevPage() {
  if (page.value > 0) page.value -= 1
}
function nextPage() {
  if (!isLast.value) page.value += 1
}
function formatDate(iso: string) {
  return dayjs(iso).format('YYYY-MM-DD')
}
</script>

<template>
  <div class="space-y-6">
    <h1 class="text-2xl font-bold">{{ t('concert.listTitle') }}</h1>

    <form class="flex gap-2" @submit.prevent="applySearch">
      <Input v-model="searchInput" :placeholder="t('concert.searchPlaceholder')" />
      <Button type="submit">{{ t('concert.search') }}</Button>
    </form>

    <p v-if="isLoading" class="text-muted-foreground">{{ t('concert.loading') }}</p>
    <p v-else-if="isError" class="text-destructive">{{ t('concert.loadError') }}</p>
    <p v-else-if="concerts.length === 0" class="text-muted-foreground">{{ t('concert.empty') }}</p>

    <ul v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <li v-for="c in concerts" :key="c.id">
        <RouterLink
          :to="{ name: 'concert-detail', params: { id: c.id } }"
          class="block h-full overflow-hidden rounded-xl border transition-colors hover:bg-muted/50"
        >
          <img
            v-if="c.imageUrl"
            :src="c.imageUrl"
            :alt="c.title"
            class="h-40 w-full object-cover"
          >
          <div v-else class="flex h-40 w-full items-center justify-center bg-muted text-muted-foreground">
            {{ c.title }}
          </div>
          <div class="space-y-1 p-4">
            <div class="flex items-center justify-between gap-2">
              <h2 class="truncate font-semibold">{{ c.title }}</h2>
              <span class="shrink-0 rounded-full bg-secondary px-2 py-0.5 text-xs text-secondary-foreground">
                {{ t(`status.${c.status}`) }}
              </span>
            </div>
            <p class="truncate text-sm text-muted-foreground">{{ c.artist }}</p>
            <p class="truncate text-sm text-muted-foreground">{{ c.venue }} · {{ c.city }}</p>
            <p class="text-xs text-muted-foreground">{{ formatDate(c.createdAt) }}</p>
          </div>
        </RouterLink>
      </li>
    </ul>

    <div v-if="totalPages > 1" class="flex items-center justify-center gap-4">
      <Button variant="outline" size="sm" :disabled="page === 0" @click="prevPage">
        {{ t('concert.prev') }}
      </Button>
      <span class="text-sm text-muted-foreground">
        {{ t('concert.pageInfo', { current: page + 1, total: totalPages }) }}
      </span>
      <Button variant="outline" size="sm" :disabled="isLast" @click="nextPage">
        {{ t('concert.next') }}
      </Button>
    </div>
  </div>
</template>
