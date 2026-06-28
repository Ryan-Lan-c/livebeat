// 前端型別契約：以後端 DTO 為準（見 backend application/dto、api/dto）。
// id 一律為 UUID 字串；時間為 ISO-8601 字串（後端 Instant / LocalDate 序列化）。

export type UserRole = 'USER' | 'ORGANIZER' | 'STAFF' | 'ADMIN'
export type AuthProvider = 'LOCAL' | 'GOOGLE' | 'FACEBOOK'

export type ConcertCategory =
  | 'POP'
  | 'ROCK'
  | 'HIP_HOP'
  | 'ELECTRONIC'
  | 'CLASSICAL'
  | 'JAZZ'
  | 'OTHER'

export type ConcertStatus = 'DRAFT' | 'PUBLISHED' | 'ON_SALE' | 'CANCELLED' | 'ENDED'
export type SessionStatus = 'DRAFT' | 'ON_SALE' | 'SOLD_OUT' | 'CANCELLED' | 'ENDED'

/**
 * Spring Data Page 的預設（DIRECT）序列化形狀。
 * 目前頁碼為 0-based 的 `number`，是否最後一頁為 `last`。
 * 註：若後端改用 PagedModel（巢狀 page 物件）序列化，需同步調整此型別。
 */
export interface Page<T> {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  numberOfElements: number
  empty: boolean
}

/** POST /auth/login|register|refresh 的回應（refresh token 走 HttpOnly cookie，不在此）。 */
export interface TokenResponse {
  accessToken: string
  userId: string
  email: string
  username: string
  role: UserRole
}

/** 前端持有的登入使用者資訊（由 TokenResponse 取得）。 */
export interface User {
  userId: string
  email: string
  username: string
  role: UserRole
}

export interface ConcertSummary {
  id: string
  title: string
  artist: string
  venue: string
  city: string
  country: string
  category: ConcertCategory
  status: ConcertStatus
  imageUrl: string | null
  createdAt: string
}

export interface TicketZone {
  id: string
  sessionId: string
  zoneCode: string
  zoneName: string
  price: number
  totalSeats: number
  soldSeats: number
  lockedSeats: number
  availableSeats: number
}

export interface ConcertSession {
  id: string
  concertId: string
  sessionName: string
  eventDate: string
  status: SessionStatus
  hasAssignedSeats: boolean
  maxTicketsPerOrder: number
  saleStartAt: string | null
  saleEndAt: string | null
  zones: TicketZone[]
}

export interface ConcertDetail {
  id: string
  title: string
  artist: string
  description: string | null
  venue: string
  city: string
  country: string
  category: ConcertCategory
  status: ConcertStatus
  imageUrl: string | null
  organizerId: string
  sessions: ConcertSession[]
  createdAt: string
  updatedAt: string
}

/** 後端統一錯誤回應（GlobalExceptionHandler.ErrorResponse）。 */
export interface ApiError {
  code: string
  message: string
}

export type OrderStatus = 'PENDING' | 'PAID' | 'CANCELLED' | 'REFUNDED'

/** POST /orders 請求；idempotencyKey 防連點重複下單。 */
export interface CreateOrderRequest {
  sessionId: string
  zoneId: string
  quantity: number
  idempotencyKey?: string
}

/**
 * 訂單回應（後端 OrderResponse）。
 * 注意：不含 quantity 與票券明細；張數由下單頁透過 router state 帶入，
 * 票券查詢 endpoint 尚未提供。expiresAt 在已售出（PAID）時為 null。
 */
export interface OrderResponse {
  orderId: string
  orderNo: string
  sessionId: string
  status: OrderStatus
  totalAmount: number
  currency: string
  expiresAt: string | null
}
