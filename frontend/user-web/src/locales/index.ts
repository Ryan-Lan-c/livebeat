import zhTW from './zh-TW'

export const SUPPORTED_LOCALES = ['zh-TW'] as const
export type AppLocale = (typeof SUPPORTED_LOCALES)[number]

export const DEFAULT_LOCALE: AppLocale = 'zh-TW'

// v1 聚焦台灣，僅提供繁體中文；i18n 架構保留，未來新增語系時於此擴充 messages。
export const messages = {
  'zh-TW': zhTW,
}

export type MessageSchema = typeof zhTW
