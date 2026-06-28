import http from './http'
import type { CreateOrderRequest, OrderResponse } from '@/types'

export const ordersApi = {
  /** 建立訂單（鎖票，回 PENDING + expiresAt）。需登入（USER）。 */
  create(body: CreateOrderRequest) {
    return http.post<OrderResponse>('/orders', body).then((r) => r.data)
  },

  /** 查詢本人訂單（非本人回 404）。 */
  get(id: string) {
    return http.get<OrderResponse>(`/orders/${id}`).then((r) => r.data)
  },

  /** Sandbox 付款，成功後狀態轉 PAID。 */
  pay(id: string) {
    return http.post<OrderResponse>(`/orders/${id}/pay`).then((r) => r.data)
  },
}
