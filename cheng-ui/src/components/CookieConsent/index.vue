<template>
  <transition name="cookie-slide">
    <div v-if="visible" class="cookie-consent">
      <div class="cookie-consent-inner">
        <div class="cookie-content">
          <div class="cookie-main">
            <div class="cookie-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2a10 10 0 1 0 10 10 4 4 0 0 1-5-5 4 4 0 0 1-5-5"/>
                <circle cx="8.5" cy="8.5" r="1"/><circle cx="10.5" cy="15.5" r="1"/><circle cx="15.5" cy="12.5" r="1"/>
              </svg>
            </div>
            <div class="cookie-text">
              <p class="cookie-title">我們使用 Cookie 來改善您的購物體驗</p>
              <p class="cookie-desc">
                透過 Cookie 與本地儲存，我們能為您提供更流暢、更個人化的服務。
              </p>
              <button class="cookie-detail-toggle" @click="showDetail = !showDetail">
                {{ showDetail ? '收起說明' : '了解更多' }}
                <svg :class="{ 'rotate': showDetail }" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg>
              </button>
            </div>
          </div>
          <div class="cookie-actions">
            <button class="cookie-btn-accept" @click="handleAcceptAll">全部接受</button>
            <button class="cookie-btn-essential" @click="handleEssentialOnly">僅必要</button>
          </div>
        </div>

        <transition name="detail-expand">
          <div v-if="showDetail" class="cookie-detail">
            <div class="cookie-categories">
              <div class="cookie-category">
                <div class="category-icon">
                  <!-- 盾牌 icon -->
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  </svg>
                </div>
                <div class="category-text">
                  <p class="category-name">必要 Cookie</p>
                  <p class="category-desc">確保網站正常運作與安全登入，這些是網站運行不可或缺的基礎。</p>
                </div>
              </div>
              <div class="cookie-category">
                <div class="category-icon">
                  <!-- 齒輪/設定 icon -->
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="3"/>
                    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
                  </svg>
                </div>
                <div class="category-text">
                  <p class="category-name">功能性 Cookie</p>
                  <p class="category-desc">記住您的偏好設定，如主題配色、帳號資訊與購物車內容，讓每次造訪都更便利。</p>
                </div>
              </div>
              <div class="cookie-category">
                <div class="category-icon">
                  <!-- 愛心 icon -->
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                </div>
                <div class="category-text">
                  <p class="category-name">行銷 Cookie</p>
                  <p class="category-desc">分析您的瀏覽行為，提供個人化商品推薦，幫助您更快找到喜愛的商品。</p>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useConsentStore } from '@/store/modules/consent'

const consentStore = useConsentStore()
const visible = ref(false)
const showDetail = ref(false)

function handleAcceptAll() {
  consentStore.acceptAll()
  visible.value = false
}

function handleEssentialOnly() {
  consentStore.acceptEssentialOnly()
  visible.value = false
}

onMounted(() => {
  consentStore.init()
  if (!consentStore.hasDecided) {
    setTimeout(() => {
      visible.value = true
    }, 1500)
  }
})
</script>

<style scoped>
.cookie-consent {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  padding: 0 16px 16px;
  pointer-events: none;
}

.cookie-consent-inner {
  max-width: 960px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  pointer-events: auto;
}

.cookie-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
}

.cookie-main {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.cookie-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--mall-primary-light, #EEF2F4);
  color: var(--mall-primary, #4A6B7C);
  border-radius: 10px;
}

.cookie-text {
  flex: 1;
  min-width: 0;
}

.cookie-title {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.4;
}

.cookie-desc {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.cookie-detail-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 6px;
  padding: 0;
  border: none;
  background: none;
  color: var(--mall-primary, #4A6B7C);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
  min-height: 44px;
  min-width: 44px;
}

.cookie-detail-toggle:hover {
  opacity: 0.7;
}

.cookie-detail-toggle svg {
  transition: transform 0.3s ease;
}

.cookie-detail-toggle svg.rotate {
  transform: rotate(180deg);
}

.cookie-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.cookie-btn-accept {
  padding: 10px 28px;
  border: none;
  border-radius: 10px;
  background: var(--mall-primary, #4A6B7C);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  min-height: 44px;
  min-width: 44px;
}

.cookie-btn-accept:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 107, 124, 0.3);
}

.cookie-btn-accept:active {
  transform: translateY(0);
}

.cookie-btn-essential {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: none;
  color: #888;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s;
  white-space: nowrap;
  min-height: 44px;
  min-width: 44px;
  text-align: center;
}

.cookie-btn-essential:hover {
  color: var(--mall-primary, #4A6B7C);
}

/* 展開區域：類別說明 */
.cookie-detail {
  padding: 0 24px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.cookie-categories {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 16px;
}

.cookie-category {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.category-icon {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--mall-primary-light, #EEF2F4);
  color: var(--mall-primary, #4A6B7C);
  border-radius: 8px;
}

.category-text {
  flex: 1;
  min-width: 0;
}

.category-name {
  margin: 0 0 2px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.category-desc {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

/* 過場動畫 */
.cookie-slide-enter-active {
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.cookie-slide-leave-active {
  transition: all 0.35s cubic-bezier(0.55, 0, 1, 0.45);
}

.cookie-slide-enter-from {
  opacity: 0;
  transform: translateY(100%);
}

.cookie-slide-leave-to {
  opacity: 0;
  transform: translateY(24px);
}

.detail-expand-enter-active {
  transition: all 0.3s ease;
}

.detail-expand-leave-active {
  transition: all 0.2s ease;
}

.detail-expand-enter-from,
.detail-expand-leave-to {
  opacity: 0;
  max-height: 0;
}

/* 手機版 (<=640px) */
@media (max-width: 640px) {
  .cookie-consent {
    padding: 0 8px 8px;
  }

  .cookie-consent-inner {
    border-radius: 12px;
  }

  .cookie-content {
    flex-direction: column;
    padding: 16px;
    gap: 14px;
  }

  .cookie-main {
    gap: 10px;
  }

  .cookie-icon {
    width: 34px;
    height: 34px;
  }

  .cookie-icon svg {
    width: 18px;
    height: 18px;
  }

  .cookie-title {
    font-size: 14px;
  }

  .cookie-desc {
    font-size: 12px;
  }

  .cookie-actions {
    width: 100%;
    flex-direction: column;
  }

  .cookie-btn-accept {
    width: 100%;
    padding: 12px;
    border-radius: 8px;
  }

  .cookie-btn-essential {
    width: 100%;
    padding: 10px;
  }

  .cookie-detail {
    padding: 0 16px 16px;
  }

  .category-icon {
    width: 30px;
    height: 30px;
  }

  .category-icon svg {
    width: 16px;
    height: 16px;
  }

  .category-name {
    font-size: 13px;
  }

  .category-desc {
    font-size: 12px;
  }
}
</style>
