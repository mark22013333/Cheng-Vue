<template>
  <div class="template-message-editor">
    <!-- 模板類型選擇 -->
    <el-form-item label="模板類型" required>
      <el-radio-group v-model="templateType" @change="handleTemplateTypeChange">
        <el-radio-button value="buttons">
          <el-icon><Grid /></el-icon>
          按鈕模板
        </el-radio-button>
        <el-radio-button value="confirm">
          <el-icon><QuestionFilled /></el-icon>
          確認模板
        </el-radio-button>
        <el-radio-button value="carousel">
          <el-icon><Tickets /></el-icon>
          輪播模板
        </el-radio-button>
        <el-radio-button value="image_carousel">
          <el-icon><PictureFilled /></el-icon>
          圖片輪播
        </el-radio-button>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="替代文字" required>
      <el-input 
        v-model="altText" 
        placeholder="不支援模板訊息時顯示的替代文字（必填）" 
        maxlength="400"
        show-word-limit
        @input="emitChange"
      />
      <div class="form-tip">※ 在通知或不支援模板的裝置上會顯示此文字</div>
    </el-form-item>

    <el-divider />

    <!-- Buttons 模板編輯 -->
    <template v-if="templateType === 'buttons'">
      <ButtonsTemplateEditor 
        v-model="buttonsData" 
        @change="emitChange"
      />
    </template>

    <!-- Confirm 模板編輯 -->
    <template v-else-if="templateType === 'confirm'">
      <ConfirmTemplateEditor 
        v-model="confirmData" 
        @change="emitChange"
      />
    </template>

    <!-- Carousel 模板編輯 -->
    <template v-else-if="templateType === 'carousel'">
      <CarouselTemplateEditor 
        v-model="carouselData" 
        @change="emitChange"
      />
    </template>

    <!-- Image Carousel 模板編輯 -->
    <template v-else-if="templateType === 'image_carousel'">
      <ImageCarouselTemplateEditor 
        v-model="imageCarouselData" 
        @change="emitChange"
      />
    </template>

    <!-- 即時預覽 -->
    <el-divider content-position="left">預覽</el-divider>
    <div class="template-preview">
      <TemplateMessagePreview 
        :template-type="templateType"
        :template-data="currentTemplateData"
        :alt-text="altText"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Grid, QuestionFilled, Tickets, PictureFilled } from '@element-plus/icons-vue'
import ButtonsTemplateEditor from './template/ButtonsTemplateEditor.vue'
import ConfirmTemplateEditor from './template/ConfirmTemplateEditor.vue'
import CarouselTemplateEditor from './template/CarouselTemplateEditor.vue'
import ImageCarouselTemplateEditor from './template/ImageCarouselTemplateEditor.vue'
import TemplateMessagePreview from './template/TemplateMessagePreview.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const templateType = ref('buttons')
const altText = ref('')

// 各模板類型的數據
const buttonsData = ref({
  thumbnailImageUrl: '',
  imageAspectRatio: 'rectangle',
  imageSize: 'cover',
  imageBackgroundColor: '#FFFFFF',
  title: '',
  text: '',
  defaultAction: null,
  actions: []
})

const confirmData = ref({
  text: '',
  actions: [
    { type: 'message', label: '是', text: '是' },
    { type: 'message', label: '否', text: '否' }
  ]
})

const carouselData = ref({
  imageAspectRatio: 'rectangle',
  imageSize: 'cover',
  columns: []
})

const imageCarouselData = ref({
  columns: []
})

// 當前模板數據
const currentTemplateData = computed(() => {
  switch (templateType.value) {
    case 'buttons': return buttonsData.value
    case 'confirm': return confirmData.value
    case 'carousel': return carouselData.value
    case 'image_carousel': return imageCarouselData.value
    default: return {}
  }
})

// 建構完整的 Template Message JSON
const buildTemplateJson = () => {
  const template = {
    type: templateType.value,
    ...currentTemplateData.value
  }

  return {
    type: 'template',
    altText: altText.value || '模板訊息',
    template
  }
}

// 發射變更事件
const emitChange = () => {
  const data = {
    templateType: templateType.value,
    altText: altText.value,
    templateData: currentTemplateData.value,
    content: JSON.stringify(buildTemplateJson())
  }
  emit('update:modelValue', data)
  emit('change', data)
}

// 模板類型切換
const handleTemplateTypeChange = () => {
  emitChange()
}

// 解析傳入的數據
const parseModelValue = (value) => {
  if (!value) return

  try {
    let tpl = null
    let parsedAltText = ''

    // 優先從 content 解析
    if (value.content) {
      const parsed = typeof value.content === 'string' 
        ? JSON.parse(value.content) 
        : value.content

      if (parsed.type === 'template' && parsed.template) {
        // 完整格式: { type: 'template', altText: '...', template: {...} }
        parsedAltText = parsed.altText || ''
        tpl = parsed.template
      } else if (parsed.template) {
        // 簡化格式: { altText: '...', template: {...} }
        parsedAltText = parsed.altText || ''
        tpl = parsed.template
      }
    }
    
    // 如果沒有從 content 解析到，嘗試從 templateData 和 templateType 恢復
    if (!tpl && value.templateType && value.templateData) {
      tpl = { type: value.templateType, ...value.templateData }
      parsedAltText = value.altText || ''
    }

    if (!tpl) return

    altText.value = parsedAltText

    switch (tpl.type) {
        case 'buttons':
          templateType.value = 'buttons'
          buttonsData.value = {
            thumbnailImageUrl: tpl.thumbnailImageUrl || '',
            imageAspectRatio: tpl.imageAspectRatio || 'rectangle',
            imageSize: tpl.imageSize || 'cover',
            imageBackgroundColor: tpl.imageBackgroundColor || '#FFFFFF',
            title: tpl.title || '',
            text: tpl.text || '',
            defaultAction: tpl.defaultAction || null,
            actions: tpl.actions || []
          }
          break
        case 'confirm':
          templateType.value = 'confirm'
          confirmData.value = {
            text: tpl.text || '',
            actions: tpl.actions || [
              { type: 'message', label: '是', text: '是' },
              { type: 'message', label: '否', text: '否' }
            ]
          }
          break
        case 'carousel':
          templateType.value = 'carousel'
          carouselData.value = {
            imageAspectRatio: tpl.imageAspectRatio || 'rectangle',
            imageSize: tpl.imageSize || 'cover',
            columns: tpl.columns || []
          }
          break
        case 'image_carousel':
          templateType.value = 'image_carousel'
          imageCarouselData.value = {
            columns: tpl.columns || []
          }
          break
      }
  } catch (e) {
    console.warn('解析模板數據失敗:', e)
  }
}

// 監聽 modelValue 變化
watch(() => props.modelValue, (newVal) => {
  parseModelValue(newVal)
}, { immediate: true, deep: true })

onMounted(() => {
  if (!props.modelValue?.content) {
    // 初始化時發射一次
    emitChange()
  }
})
</script>

<style scoped lang="scss">
.template-message-editor {
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .template-preview {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    justify-content: center;
    min-height: 200px;
  }

  :deep(.el-radio-button__inner) {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}
</style>
