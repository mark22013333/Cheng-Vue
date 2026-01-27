import { defineStore } from 'pinia'

/**
 * 預設主題配色
 */
export const PRESET_THEMES = {
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

export const useMallThemeStore = defineStore('mallTheme', {
  state: () => ({
    currentTheme: 'purple',
    customTheme: null
  }),

  getters: {
    theme: (state) => {
      if (state.customTheme) {
        return state.customTheme
      }
      return PRESET_THEMES[state.currentTheme] || PRESET_THEMES.purple
    },
    themeList: () => {
      return Object.entries(PRESET_THEMES).map(([key, value]) => ({
        key,
        ...value
      }))
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

      // 設定文字顏色
      const isDark = theme.bodyBg === '#121212'
      root.style.setProperty('--mall-text-primary', isDark ? '#ffffff' : '#303133')
      root.style.setProperty('--mall-text-secondary', isDark ? '#a0a0a0' : '#606266')
      root.style.setProperty('--mall-text-muted', isDark ? '#666666' : '#909399')
    },

    saveToStorage() {
      const data = {
        currentTheme: this.currentTheme,
        customTheme: this.customTheme
      }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    },

    loadFromStorage() {
      try {
        const saved = localStorage.getItem(STORAGE_KEY)
        if (saved) {
          const data = JSON.parse(saved)
          this.currentTheme = data.currentTheme || 'purple'
          this.customTheme = data.customTheme || null
        }
      } catch (e) {
        console.error('載入主題失敗', e)
      }
      this.applyTheme()
    },

    init() {
      this.loadFromStorage()
    }
  }
})
