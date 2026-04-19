<template>
  <div
    class="accordion-section"
    :class="{
      'is-collapsed': collapsed,
      'has-error': hasError,
      'has-changes': isDirty
    }"
  >
    <!-- 標題列（永遠可見）-->
    <div
      class="accordion-section__header"
      role="button"
      :aria-expanded="!collapsed"
      :aria-controls="'section-' + sectionKey"
      tabindex="0"
      @click="toggle"
      @keydown.enter.space.prevent="toggle"
    >
      <span class="accordion-section__bar" />

      <div class="accordion-section__heading">
        <h3 class="accordion-section__title">
          <span
            v-if="hasError"
            class="accordion-section__indicator accordion-section__indicator--error"
          />
          <span
            v-else-if="isDirty"
            class="accordion-section__indicator accordion-section__indicator--dirty"
          />
          {{ title }}
        </h3>

        <span v-if="collapsed" class="accordion-section__summary">
          <slot name="summary" />
        </span>
        <span v-else class="accordion-section__desc">
          {{ description }}
        </span>
      </div>

      <el-icon class="accordion-section__chevron">
        <ArrowRight />
      </el-icon>
    </div>

    <!-- 內容區（可展開/收合）-->
    <el-collapse-transition>
      <div
        v-show="!collapsed"
        :id="'section-' + sectionKey"
        class="accordion-section__body"
      >
        <slot />
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  collapsed: { type: Boolean, default: false },
  title: { type: String, required: true },
  description: { type: String, default: '' },
  sectionKey: { type: String, required: true },
  hasError: { type: Boolean, default: false },
  isDirty: { type: Boolean, default: false }
})

const emit = defineEmits(['update:collapsed'])

function toggle() {
  emit('update:collapsed', !props.collapsed)
}
</script>

<style scoped>
/* === 容器 === */
.accordion-section {
  margin-bottom: var(--sp-md, 16px);
  border: 1px solid var(--color-border, #E5E6EB);
  border-radius: var(--radius-md, 10px);
  background: var(--color-bg-1, #FFFFFF);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: box-shadow 220ms ease, border-color var(--transition-fast, 150ms ease-out);
  overflow: hidden;
}
.accordion-section:hover {
  box-shadow: 0 4px 12px -4px rgba(15, 23, 42, 0.1);
}

/* 錯誤狀態 */
.accordion-section.has-error {
  border-color: var(--color-error, #F53F3F);
}
.accordion-section.has-error .accordion-section__bar {
  background: linear-gradient(180deg, var(--color-error, #F53F3F) 0%, #FF7875 100%);
}

/* === 標題列 === */
.accordion-section__header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 24px;
  cursor: pointer;
  user-select: none;
  background: var(--color-bg-2, #F7F8FA);
  transition: background var(--transition-fast, 150ms ease-out);
}
.accordion-section__header:hover {
  background: var(--color-bg-3, #F2F3F5);
}
.accordion-section__header:focus-visible {
  outline: 2px solid var(--color-primary, #409EFF);
  outline-offset: -2px;
  border-radius: var(--radius-md, 10px);
}

/* 展開時底部分隔線 */
.accordion-section:not(.is-collapsed) .accordion-section__header {
  border-bottom: 1px solid var(--color-border, #E5E6EB);
}

/* === 左側色條 === */
.accordion-section__bar {
  width: 4px;
  height: 28px;
  border-radius: 4px;
  background: linear-gradient(180deg, var(--color-primary, #409EFF) 0%, var(--color-primary-light, #79BBFF) 100%);
  flex-shrink: 0;
  transition: background var(--transition-fast, 150ms ease-out);
}

/* === 標題文字區 === */
.accordion-section__heading {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}
.accordion-section__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1, #1D2129);
  letter-spacing: 0.3px;
  display: flex;
  align-items: center;
  gap: var(--sp-sm, 8px);
}
.accordion-section__summary {
  font-size: 13px;
  color: var(--color-text-3, #86909C);
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.accordion-section__desc {
  font-size: 12px;
  color: var(--color-text-3, #86909C);
}

/* === 狀態指示器（圓點）=== */
.accordion-section__indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.accordion-section__indicator--error {
  background: var(--color-error, #F53F3F);
  box-shadow: 0 0 0 3px rgba(245, 63, 63, 0.2);
  animation: pulse-error 1.5s ease-in-out infinite;
}
.accordion-section__indicator--dirty {
  background: var(--color-primary, #409EFF);
}

@keyframes pulse-error {
  0%, 100% { box-shadow: 0 0 0 3px rgba(245, 63, 63, 0.2); }
  50%      { box-shadow: 0 0 0 6px rgba(245, 63, 63, 0.1); }
}

/* === 箭頭 === */
.accordion-section__chevron {
  font-size: 16px;
  color: var(--color-text-4, #C9CDD4);
  flex-shrink: 0;
  transition: transform var(--transition-normal, 250ms ease-out);
}
.accordion-section:not(.is-collapsed) .accordion-section__chevron {
  transform: rotate(90deg);
}

/* === 內容區 === */
.accordion-section__body {
  padding: 24px 28px 8px;
}

/* el-collapse-transition duration 覆寫 */
.accordion-section :deep(.el-collapse-transition) {
  transition-duration: 250ms !important;
  transition-timing-function: ease-out !important;
}

/* === Reduced Motion === */
@media (prefers-reduced-motion: reduce) {
  .accordion-section,
  .accordion-section__header,
  .accordion-section__chevron,
  .accordion-section :deep(.el-collapse-transition) {
    transition: none !important;
  }
  .accordion-section__indicator--error {
    animation: none;
    box-shadow: 0 0 0 3px rgba(245, 63, 63, 0.2);
  }
}

/* === 響應式 === */
@media (max-width: 768px) {
  .accordion-section__header {
    padding: 14px var(--sp-md, 16px);
    gap: 10px;
  }
  .accordion-section__body {
    padding: var(--sp-md, 16px) var(--sp-md, 16px) var(--sp-sm, 8px);
  }
  .accordion-section__bar {
    height: 22px;
  }
  .accordion-section__title {
    font-size: 14px;
  }
}
</style>
