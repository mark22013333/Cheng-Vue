<template>
  <div class="flex-preview-container" :style="containerStyle">
    <div class="flex-preview-header" v-if="showHeader">
      <span class="preview-title">LINE 預覽</span>
      <el-tag size="small" type="info">{{ deviceLabel }}</el-tag>
    </div>
    <div class="flex-preview-body" :style="bodyStyle">
      <!-- Carousel 類型：支援左右滑動 -->
      <div v-if="isCarousel && renderedHtml && !errorMessage" class="carousel-wrapper">
        <div class="carousel-scroll-container" ref="carouselRef">
          <div class="preview-content carousel-content" v-html="renderedHtml"></div>
        </div>
        <div class="carousel-controls" v-if="bubbleCount > 1">
          <el-button circle size="small" :icon="ArrowLeft" @click="scrollCarousel('left')" :disabled="!canScrollLeft" />
          <span class="carousel-indicator">{{ currentBubble }} / {{ bubbleCount }}</span>
          <el-button circle size="small" :icon="ArrowRight" @click="scrollCarousel('right')" :disabled="!canScrollRight" />
        </div>
      </div>
      <!-- Bubble 類型：一般顯示 -->
      <div v-else-if="renderedHtml && !errorMessage" class="preview-content" v-html="renderedHtml"></div>
      <div v-if="errorMessage" class="preview-error">
        <el-icon><WarningFilled /></el-icon>
        <span>{{ errorMessage }}</span>
      </div>
      <div v-if="!jsonContent && !errorMessage" class="preview-empty">
        <el-icon :size="48"><Document /></el-icon>
        <p>請輸入 Flex Message JSON</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, nextTick, onMounted } from 'vue'
import { WarningFilled, Document, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { render } from 'flex-render'
import 'flex-render/css'

const props = defineProps({
  jsonContent: {
    type: String,
    default: ''
  },
  width: {
    type: [Number, String],
    default: 400
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  backgroundColor: {
    type: String,
    default: '#849ebf'
  }
})

const emit = defineEmits(['error', 'success'])

const errorMessage = ref('')
const renderedHtml = ref('')
const carouselRef = ref(null)
const isCarousel = ref(false)
const bubbleCount = ref(0)
const currentBubble = ref(1)
const canScrollLeft = ref(false)
const canScrollRight = ref(true)

const containerStyle = computed(() => ({
  width: typeof props.width === 'number' ? `${props.width}px` : props.width
}))

const bodyStyle = computed(() => ({
  backgroundColor: props.backgroundColor
}))

const deviceLabel = computed(() => 'LINE App 預覽')

const parseAndRender = () => {
  errorMessage.value = ''
  renderedHtml.value = ''
  isCarousel.value = false
  bubbleCount.value = 0
  currentBubble.value = 1

  if (!props.jsonContent) {
    return
  }

  try {
    const jsonObj = JSON.parse(props.jsonContent)

    if (!jsonObj.type || !['bubble', 'carousel'].includes(jsonObj.type)) {
      errorMessage.value = '無效的 Flex Container 類型，必須是 bubble 或 carousel'
      emit('error', errorMessage.value)
      return
    }

    // 檢查是否為 carousel 類型
    if (jsonObj.type === 'carousel' && Array.isArray(jsonObj.contents)) {
      isCarousel.value = true
      bubbleCount.value = jsonObj.contents.length
    }

    renderedHtml.value = render(jsonObj)
    emit('success')
    
    // 初始化捲動狀態
    nextTick(() => {
      updateScrollState()
    })
  } catch (e) {
    errorMessage.value = 'JSON 格式錯誤：' + e.message
    emit('error', errorMessage.value)
  }
}

// 更新捲動狀態
const updateScrollState = () => {
  if (!carouselRef.value) return
  const el = carouselRef.value
  canScrollLeft.value = el.scrollLeft > 0
  canScrollRight.value = el.scrollLeft < el.scrollWidth - el.clientWidth - 1
  
  // 計算當前顯示的 bubble
  if (bubbleCount.value > 0) {
    const bubbleWidth = el.scrollWidth / bubbleCount.value
    currentBubble.value = Math.round(el.scrollLeft / bubbleWidth) + 1
  }
}

// 捲動 carousel
const scrollCarousel = (direction) => {
  if (!carouselRef.value) return
  const el = carouselRef.value
  const bubbleWidth = el.scrollWidth / bubbleCount.value
  const scrollAmount = direction === 'left' ? -bubbleWidth : bubbleWidth
  
  el.scrollBy({ left: scrollAmount, behavior: 'smooth' })
  
  // 延遲更新狀態（等待捲動動畫完成）
  setTimeout(updateScrollState, 350)
}

watch(() => props.jsonContent, () => {
  parseAndRender()
}, { immediate: true })

const refresh = () => {
  parseAndRender()
}

// 監聽捲動事件
onMounted(() => {
  if (carouselRef.value) {
    carouselRef.value.addEventListener('scroll', updateScrollState)
  }
})

defineExpose({
  refresh
})
</script>

<style scoped>
.flex-preview-container {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.flex-preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.preview-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.flex-preview-body {
  min-height: 300px;
  padding: 16px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.preview-content {
  width: 100%;
  max-width: 360px;
}

.preview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #f56c6c;
  text-align: center;
}

.preview-error .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.preview-error span {
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
}

.preview-empty .el-icon {
  margin-bottom: 12px;
  color: #c0c4cc;
}

.preview-empty p {
  margin: 0;
  font-size: 14px;
}

/* Carousel 相關樣式 */
.carousel-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.carousel-scroll-container {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  
  /* 隱藏滾動條但保持功能 */
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
}

.carousel-scroll-container::-webkit-scrollbar {
  height: 6px;
}

.carousel-scroll-container::-webkit-scrollbar-track {
  background: transparent;
}

.carousel-scroll-container::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.carousel-scroll-container::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.3);
}

.carousel-content {
  display: flex;
  max-width: none;
  width: max-content;
}

.carousel-content :deep(.LySlider) {
  display: flex;
  gap: 8px;
}

.carousel-content :deep(.T1) {
  scroll-snap-align: start;
  flex-shrink: 0;
}

.carousel-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 8px 0;
}

.carousel-indicator {
  font-size: 13px;
  color: #606266;
  min-width: 50px;
  text-align: center;
}
</style>
