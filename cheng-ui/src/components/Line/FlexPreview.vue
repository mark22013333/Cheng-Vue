<template>
  <div class="flex-preview-container" :style="containerStyle">
    <div class="flex-preview-header" v-if="showHeader">
      <span class="preview-title">LINE 預覽</span>
      <el-tag size="small" type="info">{{ deviceLabel }}</el-tag>
    </div>
    <div class="flex-preview-body" :style="bodyStyle">
      <div class="preview-content" v-if="renderedHtml && !errorMessage" v-html="renderedHtml"></div>
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
import { ref, watch, computed } from 'vue'
import { WarningFilled, Document } from '@element-plus/icons-vue'
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

    renderedHtml.value = render(jsonObj)
    emit('success')
  } catch (e) {
    errorMessage.value = 'JSON 格式錯誤：' + e.message
    emit('error', errorMessage.value)
  }
}

watch(() => props.jsonContent, () => {
  parseAndRender()
}, { immediate: true })

const refresh = () => {
  parseAndRender()
}

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
</style>
