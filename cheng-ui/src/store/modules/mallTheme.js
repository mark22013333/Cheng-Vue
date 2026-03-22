import { defineStore } from 'pinia'
import { useConsentStore } from '@/store/modules/consent'

/**
 * 預設主題配色
 */
export const PRESET_THEMES = {
  natural: {
    name: '自然質感',
    primary: '#4A6B7C',
    primaryEnd: '#5A8A9A',
    accent: '#A5635C',
    headerBg: '#FFFFFF',
    footerBg: '#3D3D3D',
    cardBg: '#FFFFFF',
    bodyBg: '#FAF8F5'
  },
  purple: {
    name: '典雅紫',
    primary: '#667eea',
    primaryEnd: '#764ba2',
    accent: '#f56c6c',
    headerBg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    footerBg: '#2c3e50',
    cardBg: '#ffffff',
    bodyBg: '#f5f5f5'
  },
  ocean: {
    name: '海洋藍',
    primary: '#0077b6',
    primaryEnd: '#00b4d8',
    accent: '#ff6b6b',
    headerBg: 'linear-gradient(135deg, #0077b6 0%, #00b4d8 100%)',
    footerBg: '#023e8a',
    cardBg: '#ffffff',
    bodyBg: '#f0f4f8'
  },
  sunset: {
    name: '夕陽橘',
    primary: '#ff6b35',
    primaryEnd: '#f7931e',
    accent: '#2ec4b6',
    headerBg: 'linear-gradient(135deg, #ff6b35 0%, #f7931e 100%)',
    footerBg: '#3d3d3d',
    cardBg: '#ffffff',
    bodyBg: '#fff8f5'
  },
  forest: {
    name: '森林綠',
    primary: '#2d6a4f',
    primaryEnd: '#40916c',
    accent: '#e76f51',
    headerBg: 'linear-gradient(135deg, #2d6a4f 0%, #40916c 100%)',
    footerBg: '#1b4332',
    cardBg: '#ffffff',
    bodyBg: '#f0f7f4'
  },
  rose: {
    name: '玫瑰粉',
    primary: '#e63946',
    primaryEnd: '#f4a261',
    accent: '#457b9d',
    headerBg: 'linear-gradient(135deg, #e63946 0%, #f4a261 100%)',
    footerBg: '#1d3557',
    cardBg: '#ffffff',
    bodyBg: '#fff5f5'
  },
  dark: {
    name: '暗黑模式',
    primary: '#6366f1',
    primaryEnd: '#8b5cf6',
    accent: '#f59e0b',
    headerBg: 'linear-gradient(135deg, #1e1e2e 0%, #2d2d44 100%)',
    footerBg: '#0f0f1a',
    cardBg: '#1e1e2e',
    bodyBg: '#121212'
  }
}

const STORAGE_KEY = 'mall_theme'

/**
 * 判斷 headerBg 是否為白色系（非漸層且為淺色）
 */
function isLightBg(bg) {
  if (!bg || bg.includes('gradient')) return false
  const hex = bg.replace('#', '')
  if (hex.length !== 6) return false
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  return (r * 299 + g * 587 + b * 114) / 1000 > 186
}

export const useMallThemeStore = defineStore('mallTheme', {
  state: () => ({
    currentTheme: 'natural',
    customTheme: null
  }),

  getters: {
    theme: (state) => {
      if (state.customTheme) {
        return state.customTheme
      }
      return PRESET_THEMES[state.currentTheme] || PRESET_THEMES.natural
    },
    themeList: () => {
      return Object.entries(PRESET_THEMES).map(([key, value]) => ({
        key,
        ...value
      }))
    },
    isLightHeader() {
      return isLightBg(this.theme.headerBg)
    }
  },

  actions: {
    setTheme(themeKey) {
      if (PRESET_THEMES[themeKey]) {
        this.currentTheme = themeKey
        this.customTheme = null
        this.applyTheme()
        this.saveToStorage()
      }
    },

    setCustomTheme(theme) {
      this.customTheme = theme
      this.applyTheme()
      this.saveToStorage()
    },

    applyTheme() {
      const theme = this.theme
      const root = document.documentElement

      root.style.setProperty('--mall-primary', theme.primary)
      root.style.setProperty('--mall-primary-end', theme.primaryEnd)
      root.style.setProperty('--mall-accent', theme.accent)
      root.style.setProperty('--mall-header-bg', theme.headerBg)
      root.style.setProperty('--mall-footer-bg', theme.footerBg)
      root.style.setProperty('--mall-card-bg', theme.cardBg)
      root.style.setProperty('--mall-body-bg', theme.bodyBg)

      // Header 文字顏色
      const lightHeader = isLightBg(theme.headerBg)
      root.style.setProperty('--mall-header-text', lightHeader ? '#303133' : '#ffffff')
      root.style.setProperty('--mall-header-text-secondary', lightHeader ? '#606266' : 'rgba(255, 255, 255, 0.9)')

      // 邊框顏色
      const isDark = theme.bodyBg === '#121212'
      root.style.setProperty('--mall-border-color', isDark ? '#333333' : '#E8E4DF')

      // 設定文字顏色
      root.style.setProperty('--mall-text-primary', isDark ? '#ffffff' : '#303133')
      root.style.setProperty('--mall-text-secondary', isDark ? '#a0a0a0' : '#606266')
      root.style.setProperty('--mall-text-muted', isDark ? '#666666' : '#909399')
    },

    saveToStorage() {
      const consentStore = useConsentStore()
      if (!consentStore.isAllowed('functional')) return

      const data = {
        currentTheme: this.currentTheme,
        customTheme: this.customTheme
      }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    },

    loadFromStorage() {
      const consentStore = useConsentStore()
      if (consentStore.isAllowed('functional')) {
        try {
          const saved = localStorage.getItem(STORAGE_KEY)
          if (saved) {
            const data = JSON.parse(saved)
            this.currentTheme = data.currentTheme || 'natural'
            this.customTheme = data.customTheme || null
          }
        } catch (e) {
          console.error('載入主題失敗', e)
        }
      }
      this.applyTheme()
    },

    init() {
      this.loadFromStorage()
    }
  }
})
