<template>
  <div class="message-preview-wrapper" :class="{ 'full-size': fullSize }">
    <!-- LINE 聊天室風格背景 -->
    <div class="line-chat-container">
      <!-- Bot 資訊區 -->
      <div v-if="showBotInfo && (botName || botAvatar)" class="bot-info">
        <img v-if="botAvatar" :src="botAvatar" class="bot-avatar" alt="Bot Avatar" @error="handleAvatarError" />
        <div v-else class="bot-avatar-placeholder">
          <el-icon :size="20"><User /></el-icon>
        </div>
        <span class="bot-name">{{ botName || 'LINE Bot' }}</span>
      </div>

      <!-- 訊息區域 -->
      <div class="message-preview" :class="{ 'full-size': fullSize }">
        <!-- 多訊息模式：渲染 messages 陣列 -->
        <template v-if="hasMultipleMessages">
          <div class="messages-container">
            <div v-for="(msg, index) in messagesList" :key="index" class="message-item">
          <!-- TEXT -->
          <div v-if="msg.type === 'text'" class="preview-text">
            <div class="text-bubble">{{ msg.text }}</div>
          </div>

          <!-- IMAGE -->
          <div v-else-if="msg.type === 'image'" class="preview-image">
            <el-image 
              :src="msg.previewImageUrl || msg.originalContentUrl" 
              fit="contain" 
              :preview-src-list="fullSize ? [msg.originalContentUrl] : []"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>圖片載入失敗</span>
                </div>
              </template>
            </el-image>
          </div>

          <!-- VIDEO -->
          <div v-else-if="msg.type === 'video'" class="preview-video">
            <div class="video-thumbnail" v-if="!fullSize">
              <el-image :src="msg.previewImageUrl" fit="cover">
                <template #error>
                  <div class="video-placeholder">
                    <el-icon :size="40"><VideoCamera /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="play-icon">
                <el-icon :size="32"><VideoPlay /></el-icon>
              </div>
            </div>
            <video v-else :src="msg.originalContentUrl" controls class="video-player" />
          </div>

          <!-- AUDIO -->
          <div v-else-if="msg.type === 'audio'" class="preview-audio">
            <div class="audio-bubble">
              <div class="audio-icon">
                <el-icon :size="24"><Headset /></el-icon>
              </div>
              <div class="audio-info">
                <span class="audio-label">音訊訊息</span>
                <span v-if="msg.duration" class="duration">{{ formatDuration(msg.duration) }}</span>
              </div>
            </div>
            <audio v-if="fullSize" :src="msg.originalContentUrl" controls class="audio-player" />
          </div>

          <!-- LOCATION -->
          <div v-else-if="msg.type === 'location'" class="preview-location">
            <div class="location-icon">
              <el-icon :size="28"><Location /></el-icon>
            </div>
            <div class="location-info">
              <div class="location-title">{{ msg.title || '位置訊息' }}</div>
              <div class="location-address" v-if="msg.address">{{ msg.address }}</div>
            </div>
          </div>

          <!-- STICKER -->
          <div v-else-if="msg.type === 'sticker'" class="preview-sticker">
            <img :src="getStickerUrl(msg.packageId, msg.stickerId)" alt="sticker" />
          </div>

          <!-- FLEX -->
          <div v-else-if="msg.type === 'flex'" class="preview-flex">
            <FlexPreview 
              v-if="msg.contents" 
              :json-content="typeof msg.contents === 'string' ? msg.contents : JSON.stringify(msg.contents)" 
              :width="fullSize ? 300 : 200" 
              :show-header="false" 
            />
            <div v-else class="flex-placeholder">
              <el-icon :size="32"><Document /></el-icon>
              <span>Flex 訊息</span>
            </div>
          </div>

          <!-- IMAGEMAP -->
          <div v-else-if="msg.type === 'imagemap'" class="preview-imagemap">
            <div class="imagemap-placeholder">
              <el-icon :size="32"><Grid /></el-icon>
              <span>圖片地圖</span>
            </div>
          </div>

          <!-- UNKNOWN -->
          <div v-else class="preview-unknown">
            <el-icon :size="24"><QuestionFilled /></el-icon>
            <span>{{ msg.type || '未知類型' }}</span>
          </div>
        </div>
      </div>
    </template>

    <!-- 單一訊息模式 -->
    <template v-else>
      <!-- TEXT -->
      <div v-if="msgType === 'TEXT'" class="preview-text">
        <div class="text-bubble">{{ displayContent }}</div>
      </div>

      <!-- IMAGE -->
      <div v-else-if="msgType === 'IMAGE'" class="preview-image">
        <el-image :src="imageUrl" fit="contain" :preview-src-list="fullSize ? [imageUrl] : []">
          <template #error>
            <div class="image-error">
              <el-icon><Picture /></el-icon>
              <span>圖片載入失敗</span>
            </div>
          </template>
        </el-image>
      </div>

      <!-- VIDEO -->
      <div v-else-if="msgType === 'VIDEO'" class="preview-video">
        <div class="video-thumbnail" v-if="!fullSize">
          <el-image :src="videoPreviewUrl" fit="cover">
            <template #error>
              <div class="video-placeholder">
                <el-icon :size="40"><VideoCamera /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="play-icon">
            <el-icon :size="32"><VideoPlay /></el-icon>
          </div>
        </div>
        <video v-else :src="videoUrl" controls class="video-player" />
      </div>

      <!-- AUDIO -->
      <div v-else-if="msgType === 'AUDIO'" class="preview-audio">
        <div class="audio-bubble">
          <div class="audio-icon">
            <el-icon :size="24"><Headset /></el-icon>
          </div>
          <div class="audio-info">
            <span class="audio-label">音訊訊息</span>
            <span v-if="audioDuration" class="duration">{{ formatDuration(audioDuration) }}</span>
          </div>
        </div>
        <audio v-if="fullSize" :src="audioUrl" controls class="audio-player" />
      </div>

      <!-- LOCATION -->
      <div v-else-if="msgType === 'LOCATION'" class="preview-location">
        <div class="location-icon">
          <el-icon :size="fullSize ? 40 : 28"><Location /></el-icon>
        </div>
        <div class="location-info">
          <div class="location-title">{{ locationTitle || '位置訊息' }}</div>
          <div class="location-address" v-if="locationAddress">{{ locationAddress }}</div>
        </div>
      </div>

      <!-- STICKER -->
      <div v-else-if="msgType === 'STICKER'" class="preview-sticker">
        <img :src="stickerUrl" alt="sticker" @error="handleStickerError" />
        <div v-if="stickerError" class="sticker-error">
          <el-icon><PriceTag /></el-icon>
          <span>貼圖</span>
        </div>
      </div>

      <!-- IMAGEMAP -->
      <div v-else-if="msgType === 'IMAGEMAP'" class="preview-imagemap">
        <div class="imagemap-placeholder">
          <el-icon :size="fullSize ? 48 : 32"><Grid /></el-icon>
          <span>圖片地圖</span>
        </div>
      </div>

      <!-- FLEX -->
      <div v-else-if="msgType === 'FLEX'" class="preview-flex">
        <FlexPreview v-if="isValidFlexJson" :json-content="flexJsonContent" :width="fullSize ? 300 : 200" :show-header="false" />
        <div v-else class="flex-placeholder">
          <el-icon :size="fullSize ? 48 : 32"><Document /></el-icon>
          <span>Flex 訊息</span>
        </div>
      </div>

      <!-- UNKNOWN -->
      <div v-else class="preview-unknown">
        <el-icon :size="32"><QuestionFilled /></el-icon>
        <span>未知類型</span>
      </div>
    </template>
      </div>

      <!-- 時間戳記 -->
      <div v-if="showTimestamp" class="message-timestamp">{{ currentTime }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Picture, VideoCamera, VideoPlay, Headset, Location, PriceTag, Grid, Document, QuestionFilled, User } from '@element-plus/icons-vue'
import FlexPreview from '@/components/Line/FlexPreview.vue'

const props = defineProps({
  msgType: { type: String, required: true },
  content: { type: String, default: '' },
  previewImg: { type: String, default: '' },
  fullSize: { type: Boolean, default: false },
  botName: { type: String, default: '' },
  botAvatar: { type: String, default: '' },
  showBotInfo: { type: Boolean, default: true },
  showTimestamp: { type: Boolean, default: true }
})

// 當前時間（HH:MM 格式）
const currentTime = computed(() => {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
})

// 處理頭像載入錯誤
const handleAvatarError = (e) => {
  e.target.style.display = 'none'
}

const stickerError = ref(false)

const parsedContent = computed(() => {
  if (!props.content) return {}
  try {
    return JSON.parse(props.content)
  } catch {
    return {}
  }
})

// 檢查是否為多訊息格式（包含 messages 陣列）
const hasMultipleMessages = computed(() => {
  try {
    const obj = JSON.parse(props.content)
    return obj.messages && Array.isArray(obj.messages) && obj.messages.length > 0
  } catch {
    return false
  }
})

// 取得訊息陣列
const messagesList = computed(() => {
  try {
    const obj = JSON.parse(props.content)
    if (obj.messages && Array.isArray(obj.messages)) {
      return obj.messages
    }
    return []
  } catch {
    return []
  }
})

// 取得貼圖 URL
const getStickerUrl = (packageId, stickerId) => {
  if (packageId && stickerId) {
    return `https://stickershop.line-scdn.net/stickershop/v1/sticker/${stickerId}/iPhone/sticker.png`
  }
  return ''
}

const displayContent = computed(() => {
  if (props.msgType === 'TEXT') {
    return props.content || '(無內容)'
  }
  return ''
})

const imageUrl = computed(() => {
  if (props.previewImg) return props.previewImg
  return parsedContent.value.originalContentUrl || parsedContent.value.previewImageUrl || ''
})

const videoUrl = computed(() => parsedContent.value.originalContentUrl || '')
const videoPreviewUrl = computed(() => parsedContent.value.previewImageUrl || '')

const audioUrl = computed(() => parsedContent.value.originalContentUrl || '')
const audioDuration = computed(() => parsedContent.value.duration || 0)

const locationTitle = computed(() => parsedContent.value.title || '')
const locationAddress = computed(() => parsedContent.value.address || '')

const stickerUrl = computed(() => {
  const { packageId, stickerId } = parsedContent.value
  if (packageId && stickerId) {
    return `https://stickershop.line-scdn.net/stickershop/v1/sticker/${stickerId}/iPhone/sticker.png`
  }
  return ''
})

// 取得有效的 Flex JSON（處理單一 FLEX 訊息）
const flexJsonContent = computed(() => {
  try {
    const obj = JSON.parse(props.content)
    // 直接是 bubble/carousel 格式
    if (['bubble', 'carousel'].includes(obj.type)) {
      return props.content
    }
    // 如果是完整的 flex message 格式
    if (obj.contents && ['bubble', 'carousel'].includes(obj.contents?.type)) {
      return JSON.stringify(obj.contents)
    }
    return null
  } catch {
    return null
  }
})

const isValidFlexJson = computed(() => {
  return flexJsonContent.value !== null
})

const formatDuration = (ms) => {
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const handleStickerError = () => {
  stickerError.value = true
}
</script>

<style scoped lang="scss">
// LINE 聊天室風格包裝容器
.message-preview-wrapper {
  width: 100%;
  height: 100%;

  &.full-size {
    min-height: 280px;
  }
}

// LINE 聊天室容器（藍底背景）
.line-chat-container {
  background: linear-gradient(180deg, #7494c0 0%, #8ea8c9 100%);
  border-radius: 12px;
  padding: 16px;
  min-height: 200px;
  position: relative;
}

// Bot 資訊區
.bot-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);

  .bot-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid rgba(255, 255, 255, 0.5);
  }

  .bot-avatar-placeholder {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .bot-name {
    font-size: 14px;
    font-weight: 500;
    color: #fff;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
}

// 時間戳記
.message-timestamp {
  text-align: right;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 12px;
  padding-top: 8px;
}

.message-preview {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  flex-direction: column;

  &.full-size {
    min-height: 120px;
  }
}

// 多訊息容器
.messages-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 320px;

  .message-item {
    display: flex;
    justify-content: flex-start;
  }
}

.preview-text {
  width: 100%;

  .text-bubble {
    background: #06c755;
    color: white;
    padding: 10px 14px;
    border-radius: 18px;
    border-bottom-left-radius: 4px;
    font-size: 14px;
    line-height: 1.5;
    max-width: 100%;
    word-break: break-word;
    white-space: pre-wrap;
    display: inline-block;
  }
}

.preview-image {
  width: 100%;
  max-width: 240px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

  :deep(.el-image) {
    width: 100%;
    display: block;
  }

  .image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #909399;
    height: 150px;
    background: #f5f7fa;
  }
}

.preview-video {
  width: 100%;
  max-width: 240px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

  .video-thumbnail {
    position: relative;
    width: 100%;
    aspect-ratio: 16 / 9;

    :deep(.el-image) {
      width: 100%;
      height: 100%;
    }

    .video-placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      background: #1a1a1a;
      color: #fff;
      height: 100%;
    }

    .play-icon {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: rgba(255, 255, 255, 0.9);
      border-radius: 50%;
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #303133;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    }
  }

  .video-player {
    width: 100%;
    max-height: 300px;
    border-radius: 12px;
  }
}

.preview-audio {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .audio-bubble {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    background: #06c755;
    border-radius: 18px;
    border-bottom-left-radius: 4px;
    min-width: 160px;
  }

  .audio-icon {
    background: rgba(255, 255, 255, 0.25);
    border-radius: 50%;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  .audio-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    color: #fff;

    .audio-label {
      font-size: 13px;
      font-weight: 500;
    }

    .duration {
      color: rgba(255, 255, 255, 0.85);
      font-size: 12px;
    }
  }

  .audio-player {
    width: 100%;
    max-width: 280px;
    height: 40px;
  }
}

.preview-location {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 260px;

  .location-icon {
    color: #f56c6c;
    flex-shrink: 0;
  }

  .location-info {
    .location-title {
      font-weight: 500;
      color: #303133;
      font-size: 14px;
    }

    .location-address {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.preview-sticker {
  padding: 4px;

  img {
    max-width: 140px;
    max-height: 140px;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  }

  .sticker-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #909399;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 12px;
  }
}

.preview-imagemap {
  .imagemap-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #909399;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 12px;
  }
}

.preview-flex {
  width: 100%;
  display: flex;
  justify-content: flex-start;

  .flex-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #909399;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 12px;
  }

  :deep(.flex-preview-container) {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  }
}

.preview-unknown {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #909399;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
}
</style>
