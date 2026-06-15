import http from './http'
import type { ConcertDetail, ConcertSummary, Page } from '@/types'

export interface ConcertListParams {
  q?: string
  category?: string
  city?: string
  page?: number
  size?: number
  sort?: string
}

export const concertsApi = {
  /** 公開列表搜尋：支援 q / category / city / 分頁 / 排序。 */
  list(params: ConcertListParams) {
    return http
      .get<Page<ConcertSummary>>('/concerts', { params })
      .then((r) => r.data)
  },

  /** 公開詳情（含場次與票區）。 */
  detail(id: string) {
    return http.get<ConcertDetail>(`/concerts/${id}`).then((r) => r.data)
  },
}
