<template>
  <div class="imagemap-preview-container">
    <div v-if="data && data.baseUrl" class="preview-wrapper" :style="previewStyle">
      <!-- 熱區標記 -->
      <div
        v-for="(action, index) in data.actions"
        :key="index"
        class="action-area"
        :style="getActionStyle(action)"
        @click="handleActionClick(action)"
      >
        <div class="area-label">
          <span class="area-number">{{ index + 1 }}</span>
          <el-icon v-if="action.type === 'uri'"><Link /></el-icon>
          <el-icon v-else><ChatDotRound /></el-icon>
        </div>
      </div>
    </div>
    <div v-else class="empty-preview">
      <el-icon :size="48" color="#dcdfe6"><Grid /></el-icon>
      <p>暫無預覽</p>
    </div>
    
    <!-- 動作詳情提示 -->
    <div v-if="showTooltip" class="action-tooltip" :style="tooltipStyle">
      <div class="tooltip-content">
        <div class="tooltip-type">
          <el-tag :type="currentAction?.type === 'uri' ? 'primary' : 'success'" size="small">
            {{ currentAction?.type === 'uri' ? '開啟連結' : '發送訊息' }}
          </el-tag>
        </div>
        <div class="tooltip-value">
          {{ currentAction?.type === 'uri' ? currentAction?.linkUri : currentAction?.text }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Link, ChatDotRound, Grid } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/image'

const props = defineProps({
  data: { type: Object, default: null },
  width: { type: Number, default: 400 }
})

const PREVIEW_WIDTH = computed(() => props.width)

const showTooltip = ref(false)
const tooltipStyle = ref({})
const currentAction = ref(null)

const previewStyle = computed(() => {
  if (!props.data?.baseSize) return {}
  
  const { width, height } = props.data.baseSize
  const ratio = height / width
  const previewHeight = PREVIEW_WIDTH.value * ratio
  
  // baseUrl 是目錄路徑（如 /profile/imagemap/xxx），需要加上尺寸才能訪問實際圖片
  let baseUrl = props.data.baseUrl || ''
  // 如果 baseUrl 不是以 /數字 結尾，則加上 /700（預覽尺寸）
  if (baseUrl && !baseUrl.match(/\/\d+$/)) {
    baseUrl = baseUrl + '/700'
  }
  const imageUrl = getImageUrl(baseUrl)
  
  return {
    width: PREVIEW_WIDTH.value + 'px',
    height: previewHeight + 'px',
    backgroundImage: `url(${imageUrl})`,
    backgroundSize: 'contain',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat',
    position: 'relative',
    border: '1px solid #ddd',
    borderRadius: '8px',
    overflow: 'hidden'
  }
})

const getActionStyle = (action) => {
  if (!props.data?.baseSize || !action?.area) return {}
  
  const { width: baseWidth, height: baseHeight } = props.data.baseSize
  const scaleX = PREVIEW_WIDTH.value / baseWidth
  const scaleY = scaleX  // 保持等比例縮放
  
  return {
    position: 'absolute',
    left: (action.area.x * scaleX) + 'px',
    top: (action.area.y * scaleY) + 'px',
    width: (action.area.width * scaleX) + 'px',
    height: (action.area.height * scaleY) + 'px',
    border: '2px solid rgba(64, 158, 255, 0.8)',
    backgroundColor: 'rgba(64, 158, 255, 0.15)',
    cursor: 'pointer',
    transition: 'all 0.2s'
  }
}

const handleActionClick = (action) => {
  currentAction.value = action
  showTooltip.value = true
  setTimeout(() => {
    showTooltip.value = false
  }, 3000)
}
</script>

<style scoped>
.imagemap-preview-container {
  position: relative;
}

.preview-wrapper {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.empty-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 400px;
  height: 300px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #909399;
}

.action-area {
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-area:hover {
  background-color: rgba(64, 158, 255, 0.3) !important;
  border-color: rgba(64, 158, 255, 1) !important;
}

.area-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  pointer-events: none;
}

.area-number {
  width: 24px;
  height: 24px;
  background: #409EFF;
  color: white;
  border-radius: 50%;
  text-align: center;
  line-height: 24px;
  font-weight: bold;
  font-size: 12px;
}

.action-tooltip {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
}

.tooltip-content {
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 12px 16px;
  border-radius: 8px;
  max-width: 300px;
}

.tooltip-type {
  margin-bottom: 8px;
}

.tooltip-value {
  font-size: 13px;
  word-break: break-all;
}
</style>
