<template>
  <el-dialog
    v-model="visible"
    title="選擇 LINE Emoji"
    width="750px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="emoji-selector">
      <!-- 分類選擇（換行顯示） -->
      <div class="category-tabs">
        <div class="tab-list">
          <div
            v-for="cat in emojiCategories"
            :key="cat.productId"
            class="tab-item"
            :class="{ active: selectedProductId === cat.productId }"
            @click="selectCategory(cat.productId)"
          >
            <span class="tab-name">{{ cat.name }}</span>
            <span v-if="cat.loaded" class="tab-count">({{ cat.emojis.length }})</span>
          </div>
        </div>
      </div>

      <!-- Emoji 列表 -->
      <div class="emoji-grid" v-loading="loading">
        <div
          v-for="emoji in currentEmojis"
          :key="emoji.emojiId"
          class="emoji-item"
          :class="{ selected: isEmojiSelected(selectedProductId, emoji.emojiId) }"
          @click="toggleEmoji(emoji)"
        >
          <img
            :src="getEmojiUrl(selectedProductId, emoji.emojiId)"
            :alt="`Emoji ${emoji.emojiId}`"
            @error="handleEmojiError($event)"
          />
          <span v-if="isEmojiSelected(selectedProductId, emoji.emojiId)" class="selected-badge">
            {{ getEmojiIndex(selectedProductId, emoji.emojiId) }}
          </span>
        </div>
        <div v-if="!loading && currentEmojis.length === 0" class="empty-hint">
          正在載入 Emoji...
        </div>
      </div>

      <!-- 已選擇的 Emoji 列表 -->
      <div class="selected-preview" v-if="selectedEmojis.length > 0">
        <div class="preview-label">已選擇 {{ selectedEmojis.length }} 個：</div>
        <div class="preview-list">
          <div v-for="(emoji, index) in selectedEmojis" :key="index" class="preview-item">
            <img :src="getEmojiUrl(emoji.productId, emoji.emojiId)" alt="Selected emoji" />
            <el-button type="danger" :icon="Close" size="small" circle @click="removeEmoji(index)" />
          </div>
        </div>
        <el-button size="small" @click="clearAllEmojis" style="margin-top: 8px;">清除全部</el-button>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :disabled="selectedEmojis.length === 0" @click="handleConfirm">
        插入 {{ selectedEmojis.length }} 個 Emoji
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const selectedProductId = ref('5ac1bfd5040ab15980c9b435')
const selectedEmojis = ref([]) // 多選模式

// LINE Emoji 官方列表（使用官方文檔確認的正確 productId）
// 參考: https://developers.line.biz/en/docs/messaging-api/emoji-list/
// Emoji 數量動態載入，從編號 1 開始直到 404
const emojiCategories = reactive([
  { productId: '5ac1bfd5040ab15980c9b435', name: 'Brown & Friends', emojis: [], loaded: false },
  { productId: '5ac21a8c040ab15980c9b43f', name: 'CHOCO & Friends', emojis: [], loaded: false },
  { productId: '5ac2213e040ab15980c9b447', name: '表情符號', emojis: [], loaded: false },
  { productId: '5ac21cce040ab15980c9b443', name: '生活日常', emojis: [], loaded: false },
  { productId: '5ac21e6c040ab15980c9b444', name: '動物', emojis: [], loaded: false },
  { productId: '5ac22e85040ab15980c9b44f', name: '食物', emojis: [], loaded: false },
  { productId: '5ac22775040ab15980c9b44c', name: '運動', emojis: [], loaded: false },
  { productId: '5ac2197b040ab15980c9b43d', name: '旅行', emojis: [], loaded: false },
  { productId: '5ac21d59031a6752fb806d5d', name: '節日', emojis: [], loaded: false },
  { productId: '5ac221ca040ab15980c9b449', name: '工作', emojis: [], loaded: false },
  { productId: '5ac21fda040ab15980c9b446', name: '天氣自然', emojis: [], loaded: false },
  { productId: '5ac22224031a6752fb806d62', name: '愛心符號', emojis: [], loaded: false },
  { productId: '5ac222bf031a6752fb806d64', name: '手勢動作', emojis: [], loaded: false },
  { productId: '5ac21c4e031a6752fb806d5b', name: '符號', emojis: [], loaded: false },
  { productId: '5ac2211e031a6752fb806d61', name: '其他', emojis: [], loaded: false },
  // 新增更多 emoji 分類
  { productId: '670e0cce840a8236ddd4ee4c', name: 'Emoji 新系列', emojis: [], loaded: false }
])

// 動態載入指定分類的 emoji（透過圖片請求測試哪些編號存在）
const loadCategoryEmojis = async (productId) => {
  const cat = emojiCategories.find(c => c.productId === productId)
  if (!cat || cat.loaded) return

  loading.value = true
  const emojis = []
  let consecutiveErrors = 0
  const maxConsecutiveErrors = 2 // 連續 2 次失敗就停止
  const maxEmojis = 500 // 最大探測數量

  for (let i = 1; i <= maxEmojis && consecutiveErrors < maxConsecutiveErrors; i++) {
    const emojiId = String(i).padStart(3, '0')
    const url = getEmojiUrl(productId, emojiId)

    try {
      const exists = await checkImageExists(url)
      if (exists) {
        emojis.push({ emojiId })
        consecutiveErrors = 0
      } else {
        consecutiveErrors++
      }
    } catch {
      consecutiveErrors++
    }
  }

  cat.emojis = emojis
  cat.loaded = true
  loading.value = false
}

// 檢查圖片是否存在
const checkImageExists = (url) => {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => resolve(true)
    img.onerror = () => resolve(false)
    img.src = url
  })
}

// 選擇分類時載入 emoji
const selectCategory = (productId) => {
  selectedProductId.value = productId
  loadCategoryEmojis(productId)
}

const currentEmojis = computed(() => {
  const cat = emojiCategories.find(c => c.productId === selectedProductId.value)
  return cat ? cat.emojis : []
})

// 使用 android 路徑而非 iPhone（根據 LINE API 文件）
const getEmojiUrl = (productId, emojiId) => {
  return `https://stickershop.line-scdn.net/sticonshop/v1/sticon/${productId}/android/${emojiId}.png`
}

// 切換 emoji 選擇狀態（多選）
const toggleEmoji = (emoji) => {
  const productId = selectedProductId.value
  const emojiId = emoji.emojiId
  const index = selectedEmojis.value.findIndex(
    e => e.productId === productId && e.emojiId === emojiId
  )
  if (index >= 0) {
    selectedEmojis.value.splice(index, 1)
  } else {
    selectedEmojis.value.push({ productId, emojiId })
  }
}

// 檢查 emoji 是否已選擇
const isEmojiSelected = (productId, emojiId) => {
  return selectedEmojis.value.some(
    e => e.productId === productId && e.emojiId === emojiId
  )
}

// 取得 emoji 在已選列表中的順序
const getEmojiIndex = (productId, emojiId) => {
  return selectedEmojis.value.findIndex(
    e => e.productId === productId && e.emojiId === emojiId
  ) + 1
}

// 移除單個 emoji
const removeEmoji = (index) => {
  selectedEmojis.value.splice(index, 1)
}

// 清除全部
const clearAllEmojis = () => {
  selectedEmojis.value = []
}

const handleEmojiError = (event) => {
  event.target.style.opacity = '0.3'
}

const handleClose = () => {
  visible.value = false
}

const handleConfirm = () => {
  if (selectedEmojis.value.length > 0) {
    // 批次發送所有選擇的 emoji
    emit('select', selectedEmojis.value)
    visible.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    selectedEmojis.value = []
    // 開啟時載入當前選中分類的 emoji
    loadCategoryEmojis(selectedProductId.value)
  }
})

// 元件掛載時預載入第一個分類
onMounted(() => {
  loadCategoryEmojis(selectedProductId.value)
})
</script>

<style scoped lang="scss">
.emoji-selector {
  .category-tabs {
    margin-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    padding-bottom: 12px;

    .tab-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .tab-item {
      padding: 6px 14px;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.2s;
      text-align: center;
      background: #f5f7fa;
      border: 1px solid #e4e7ed;

      &:hover {
        background: #ecf5ff;
        border-color: #409eff;
      }

      &.active {
        background: #409eff;
        color: #fff;
        border-color: #409eff;
      }

      .tab-name {
        font-size: 13px;
        font-weight: 500;
      }

      .tab-count {
        font-size: 11px;
        color: #909399;
        margin-left: 4px;
      }

      &.active .tab-count {
        color: #fff;
        opacity: 0.8;
      }
    }
  }

  .empty-hint {
    grid-column: 1 / -1;
    text-align: center;
    color: #909399;
    padding: 40px 0;
  }

  .emoji-grid {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 8px;
    max-height: 350px;
    overflow-y: auto;
    padding: 8px;
    background: #f5f7fa;
    border-radius: 8px;
  }

  .emoji-item {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 6px;
    background: #fff;
    border: 2px solid transparent;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
    aspect-ratio: 1;
    position: relative;

    &:hover {
      border-color: #409eff;
      transform: scale(1.1);
    }

    &.selected {
      border-color: #67c23a;
      background: #f0f9eb;
    }

    img {
      width: 36px;
      height: 36px;
      object-fit: contain;
    }

    .selected-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      width: 18px;
      height: 18px;
      background: #67c23a;
      color: #fff;
      border-radius: 50%;
      font-size: 11px;
      font-weight: bold;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .selected-preview {
    margin-top: 16px;
    padding: 12px;
    background: #f0f9eb;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    gap: 8px;

    .preview-label {
      font-weight: 500;
      color: #67c23a;
    }

    .preview-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .preview-item {
        display: flex;
        align-items: center;
        gap: 4px;
        background: #fff;
        padding: 4px 8px;
        border-radius: 6px;
        border: 1px solid #e4e7ed;

        img {
          width: 24px;
          height: 24px;
          object-fit: contain;
        }

        .el-button {
          margin-left: 4px;
        }
      }
    }
  }
}
</style>
