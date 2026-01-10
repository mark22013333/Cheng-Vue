<template>
  <div class="message-preview-wrapper" :class="{ 'full-size': fullSize, 'with-phone-frame': showPhoneFrame }">
    <!-- iPhone 外觀框架（使用 mockup 圖片） -->
    <div v-if="showPhoneFrame" class="iphone-mockup-container">
      <img :src="iphoneMockupImg" class="iphone-mockup-img" alt="iPhone" />
      <div class="iphone-screen-content">
        <!-- 狀態列 -->
        <div class="status-bar">
          <span class="status-time">{{ currentTime }}</span>
          <div class="status-icons">
            <svg class="signal-icon" viewBox="0 0 17 10.7" width="17" height="11">
              <path d="M15.5 10.7h-1c-.8 0-1.5-.7-1.5-1.5v-7c0-.8.7-1.5 1.5-1.5h1c.8 0 1.5.7 1.5 1.5v7c0 .8-.7 1.5-1.5 1.5z"/>
              <path d="M10.5 10.7h-1c-.8 0-1.5-.7-1.5-1.5v-5c0-.8.7-1.5 1.5-1.5h1c.8 0 1.5.7 1.5 1.5v5c0 .8-.7 1.5-1.5 1.5z"/>
              <path d="M5.5 10.7h-1c-.8 0-1.5-.7-1.5-1.5v-3c0-.8.7-1.5 1.5-1.5h1c.8 0 1.5.7 1.5 1.5v3c0 .8-.7 1.5-1.5 1.5z"/>
              <path d="M.5 10.7h-1c-.8 0-1.5-.7-1.5-1.5v-1c0-.8.7-1.5 1.5-1.5h1c.8 0 1.5.7 1.5 1.5v1c0 .8-.7 1.5-1.5 1.5z" opacity=".3"/>
            </svg>
            <svg class="battery-icon" viewBox="0 0 25 12" width="25" height="12">
              <rect x="0" y="0" width="22" height="12" rx="3" ry="3" fill="none" stroke="currentColor" stroke-width="1"/>
              <rect x="23" y="3.5" width="2" height="5" rx="1" ry="1" fill="currentColor"/>
              <rect x="2" y="2" width="18" height="8" rx="1" ry="1" fill="currentColor"/>
            </svg>
          </div>
        </div>
        <!-- LINE 聊天室內容 -->
        <div class="line-chat-content">
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
                    <div class="text-bubble">
                      <template v-if="getTextWithEmojis(msg)">
                        <template v-for="(part, idx) in getTextWithEmojis(msg)" :key="idx">
                          <span v-if="part.type === 'text'">{{ part.content }}</span>
                          <img v-else-if="part.type === 'emoji'" :src="part.url" class="inline-emoji" :alt="'emoji'" />
                        </template>
                      </template>
                      <template v-else>{{ msg.text }}</template>
                    </div>
                  </div>
                  <!-- IMAGE -->
                  <div v-else-if="msg.type === 'image'" class="preview-image">
                    <el-image :src="msg.previewImageUrl || msg.originalContentUrl" fit="contain" />
                  </div>
                  <!-- IMAGEMAP -->
                  <div v-else-if="msg.type === 'imagemap'" class="preview-imagemap">
                    <div v-if="getImagemapUrl(msg)" class="imagemap-image-wrapper">
                      <el-image :src="getImagemapUrl(msg)" fit="contain" :style="getImagemapStyle(msg)" />
                    </div>
                    <div v-else class="imagemap-placeholder">
                      <el-icon :size="32"><Grid /></el-icon>
                      <span>圖文訊息</span>
                    </div>
                  </div>
                  <!-- FLEX -->
                  <div v-else-if="msg.type === 'flex'" class="preview-flex">
                    <FlexPreview v-if="msg.contents" :json-content="typeof msg.contents === 'string' ? msg.contents : JSON.stringify(msg.contents)" :width="200" :show-header="false" />
                  </div>
                  <!-- AUDIO -->
                  <div v-else-if="msg.type === 'audio'" class="preview-audio">
                    <div class="audio-bubble">
                      <el-icon :size="24"><Headset /></el-icon>
                      <span>音訊訊息</span>
                    </div>
                  </div>
                  <!-- STICKER -->
                  <div v-else-if="msg.type === 'sticker'" class="preview-sticker">
                    <img :src="getStickerUrl(msg.packageId, msg.stickerId)" alt="sticker" />
                  </div>
                  <!-- TEMPLATE -->
                  <div v-else-if="msg.type === 'template'" class="preview-template">
                    <TemplateMessagePreview
                      v-if="getTemplatePreviewData(msg)"
                      :template-type="getTemplatePreviewData(msg).templateType"
                      :template-data="getTemplatePreviewData(msg).templateData"
                      :alt-text="msg.altText"
                      :compact="true"
                    />
                    <div v-else class="template-placeholder">
                      <el-icon :size="24"><Grid /></el-icon>
                      <span>模板訊息</span>
                    </div>
                  </div>
                  <!-- OTHER -->
                  <div v-else class="preview-unknown">
                    <span>{{ msg.type || '未知類型' }}</span>
                  </div>
                </div>
              </div>
              <!-- Quick Reply 預覽（多訊息模式：只取最後一則訊息的 quickReply，顯示在底部） -->
              <div v-if="lastMessageQuickReply && lastMessageQuickReply.length > 0" class="quick-reply-preview quick-reply-bottom">
                <div class="quick-reply-container">
                  <div v-for="(item, idx) in lastMessageQuickReply" :key="idx" class="quick-reply-item">
                    <img v-if="item.imageUrl" :src="item.imageUrl" class="quick-reply-icon" />
                    <span class="quick-reply-label">{{ item.action?.label || '按鈕' }}</span>
                  </div>
                </div>
              </div>
            </template>

            <!-- 單一訊息模式 -->
            <template v-else>
              <div class="single-message-row">
                <!-- TEXT -->
                <div v-if="msgType === 'TEXT'" class="preview-text">
                  <div class="text-bubble">
                    <template v-if="displayTextWithEmojis">
                      <template v-for="(part, idx) in displayTextWithEmojis" :key="idx">
                        <span v-if="part.type === 'text'">{{ part.content }}</span>
                        <img v-else-if="part.type === 'emoji'" :src="part.url" class="inline-emoji" :alt="'emoji'" />
                      </template>
                    </template>
                    <template v-else>{{ displayContent }}</template>
                  </div>
                  <!-- Quick Reply 預覽 -->
                  <div v-if="quickReplyItems && quickReplyItems.length > 0" class="quick-reply-preview">
                    <div class="quick-reply-container">
                      <div v-for="(item, idx) in quickReplyItems" :key="idx" class="quick-reply-item">
                        <img v-if="item.imageUrl" :src="item.imageUrl" class="quick-reply-icon" />
                        <span class="quick-reply-label">{{ item.action?.label || '按鈕' }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              <!-- IMAGE -->
              <div v-else-if="msgType === 'IMAGE'" class="preview-image">
                <el-image :src="imageUrl" fit="contain" />
              </div>
              <!-- IMAGEMAP -->
              <div v-else-if="msgType === 'IMAGEMAP'" class="preview-imagemap">
                <div v-if="imagemapPreviewUrl" class="imagemap-image-wrapper">
                  <el-image :src="imagemapPreviewUrl" fit="contain" :style="imagemapStyle" />
                </div>
                <div v-else class="imagemap-placeholder">
                  <el-icon :size="32"><Grid /></el-icon>
                  <span>圖文訊息</span>
                </div>
              </div>
              <!-- FLEX -->
              <div v-else-if="msgType === 'FLEX'" class="preview-flex">
                <FlexPreview v-if="isValidFlexJson" :json-content="flexJsonContent" :width="200" :show-header="false" />
              </div>
              <!-- TEMPLATE -->
              <div v-else-if="msgType === 'TEMPLATE'" class="preview-template">
                <TemplateMessagePreview
                  v-if="templatePreviewData"
                  :template-type="templatePreviewData.templateType"
                  :template-data="templatePreviewData.templateData"
                  :alt-text="templatePreviewData.altText"
                  :compact="true"
                />
                <div v-else class="template-placeholder">
                  <el-icon :size="24"><Grid /></el-icon>
                  <span>模板訊息</span>
                </div>
              </div>
              <!-- OTHER -->
              <div v-else class="preview-unknown">
                <span>{{ msgType || '未知類型' }}</span>
              </div>
              </div>
            </template>
          </div>

        </div>
      </div>
    </div>

    <!-- 無 iPhone 框架模式 -->
    <div v-else class="line-chat-container">
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
            <div class="text-bubble">
              <template v-if="getTextWithEmojis(msg)">
                <template v-for="(part, idx) in getTextWithEmojis(msg)" :key="idx">
                  <span v-if="part.type === 'text'">{{ part.content }}</span>
                  <img v-else-if="part.type === 'emoji'" :src="part.url" class="inline-emoji" :alt="'emoji'" />
                </template>
              </template>
              <template v-else>{{ msg.text }}</template>
            </div>
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
            <div v-if="getImagemapUrl(msg)" class="imagemap-image-wrapper">
              <el-image :src="getImagemapUrl(msg)" fit="contain" :style="getImagemapStyle(msg)">
                <template #error>
                  <div class="imagemap-placeholder">
                    <el-icon :size="32"><Grid /></el-icon>
                    <span>圖片載入失敗</span>
                  </div>
                </template>
              </el-image>
              <div class="imagemap-info">
                <el-tag size="small" type="info">{{ msg.actions?.length || 0 }} 個熱區</el-tag>
              </div>
            </div>
            <div v-else class="imagemap-placeholder">
              <el-icon :size="32"><Grid /></el-icon>
              <span>圖文訊息</span>
            </div>
          </div>

          <!-- TEMPLATE -->
          <div v-else-if="msg.type === 'template'" class="preview-template">
            <TemplateMessagePreview
              v-if="getTemplatePreviewData(msg)"
              :template-type="getTemplatePreviewData(msg).templateType"
              :template-data="getTemplatePreviewData(msg).templateData"
              :alt-text="msg.altText"
              :compact="true"
            />
            <div v-else class="template-placeholder">
              <el-icon :size="24"><Grid /></el-icon>
              <span>模板訊息</span>
            </div>
          </div>

          <!-- UNKNOWN -->
          <div v-else class="preview-unknown">
            <el-icon :size="24"><QuestionFilled /></el-icon>
            <span>{{ msg.type || '未知類型' }}</span>
          </div>
        </div>
      </div>
      <!-- Quick Reply 預覽（多訊息模式：只取最後一則訊息的 quickReply，顯示在底部） -->
      <div v-if="lastMessageQuickReply && lastMessageQuickReply.length > 0" class="quick-reply-preview quick-reply-bottom">
        <div class="quick-reply-container">
          <div v-for="(item, idx) in lastMessageQuickReply" :key="idx" class="quick-reply-item">
            <img v-if="item.imageUrl" :src="item.imageUrl" class="quick-reply-icon" />
            <span class="quick-reply-label">{{ item.action?.label || '按鈕' }}</span>
          </div>
        </div>
      </div>
    </template>

    <!-- 單一訊息模式 -->
    <template v-else>
      <div class="single-message-row">
        <!-- TEXT -->
        <div v-if="msgType === 'TEXT'" class="preview-text">
          <div class="text-bubble">
            <template v-if="displayTextWithEmojis">
              <template v-for="(part, idx) in displayTextWithEmojis" :key="idx">
                <span v-if="part.type === 'text'">{{ part.content }}</span>
                <img v-else-if="part.type === 'emoji'" :src="part.url" class="inline-emoji" :alt="'emoji'" />
              </template>
            </template>
            <template v-else>{{ displayContent }}</template>
          </div>
          <!-- Quick Reply 預覽 -->
          <div v-if="quickReplyItems && quickReplyItems.length > 0" class="quick-reply-preview">
            <div class="quick-reply-container">
              <div v-for="(item, idx) in quickReplyItems" :key="idx" class="quick-reply-item">
                <img v-if="item.imageUrl" :src="item.imageUrl" class="quick-reply-icon" />
                <span class="quick-reply-label">{{ item.action?.label || '按鈕' }}</span>
              </div>
            </div>
          </div>
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
        <div v-if="imagemapPreviewUrl" class="imagemap-image-wrapper">
          <el-image :src="imagemapPreviewUrl" fit="contain" :style="imagemapStyle">
            <template #error>
              <div class="imagemap-placeholder">
                <el-icon :size="fullSize ? 48 : 32"><Grid /></el-icon>
                <span>圖片載入失敗</span>
              </div>
            </template>
          </el-image>
          <div class="imagemap-info">
            <el-tag size="small" type="info">{{ imagemapActionsCount }} 個熱區</el-tag>
          </div>
        </div>
        <div v-else class="imagemap-placeholder">
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

      <!-- TEMPLATE -->
      <div v-else-if="msgType === 'TEMPLATE'" class="preview-template">
        <TemplateMessagePreview
          v-if="templatePreviewData"
          :template-type="templatePreviewData.templateType"
          :template-data="templatePreviewData.templateData"
          :alt-text="templatePreviewData.altText"
          :compact="!fullSize"
        />
        <div v-else class="template-placeholder">
          <el-icon :size="fullSize ? 48 : 24"><Grid /></el-icon>
          <span>模板訊息</span>
        </div>
      </div>

      <!-- UNKNOWN -->
      <div v-else class="preview-unknown">
        <el-icon :size="32"><QuestionFilled /></el-icon>
        <span>未知類型</span>
      </div>
      </div>
    </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Picture, VideoCamera, VideoPlay, Headset, Location, PriceTag, Grid, Document, QuestionFilled, User } from '@element-plus/icons-vue'
import FlexPreview from '@/components/Line/FlexPreview.vue'
import TemplateMessagePreview from './template/TemplateMessagePreview.vue'
import iphoneMockupImg from '@/assets/images/iphone-14-mockup-with-transparent.png'
import { getImageUrl } from '@/utils/image'

const props = defineProps({
  msgType: { type: String, required: true },
  content: { type: String, default: '' },
  previewImg: { type: String, default: '' },
  fullSize: { type: Boolean, default: false },
  botName: { type: String, default: '' },
  botAvatar: { type: String, default: '' },
  showBotInfo: { type: Boolean, default: true },
  showTimestamp: { type: Boolean, default: true },
  showPhoneFrame: { type: Boolean, default: false }
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

// 取得貼圖 URL（使用 android 路徑）
const getStickerUrl = (packageId, stickerId) => {
  if (packageId && stickerId) {
    return `https://stickershop.line-scdn.net/stickershop/v1/sticker/${stickerId}/android/sticker.png`
  }
  return ''
}

const displayContent = computed(() => {
  if (props.msgType === 'TEXT') {
    const content = props.content || ''
    // 嘗試解析 JSON 格式（包含 emojis）
    if (content.startsWith('{') && content.includes('"emojis"')) {
      try {
        const obj = JSON.parse(content)
        return obj.text || content
      } catch {
        return content
      }
    }
    return content || '(無內容)'
  }
  return ''
})

// 解析 TEXT 訊息中的 emojis
const textEmojis = computed(() => {
  if (props.msgType !== 'TEXT' || !props.content) return []
  const content = props.content
  if (content.startsWith('{') && content.includes('"emojis"')) {
    try {
      const obj = JSON.parse(content)
      return obj.emojis || []
    } catch {
      return []
    }
  }
  return []
})

// 解析 TEXT 訊息中的 quickReply（單一訊息模式）
const quickReplyItems = computed(() => {
  if (props.msgType !== 'TEXT' || !props.content) return []
  const content = props.content
  if (content.startsWith('{') && content.includes('"quickReply"')) {
    try {
      const obj = JSON.parse(content)
      return obj.quickReply?.items || []
    } catch {
      return []
    }
  }
  return []
})

// 多訊息模式：取得最後一則訊息的 quickReply（LINE 只會顯示最後一則的 Quick Reply）
const lastMessageQuickReply = computed(() => {
  if (!hasMultipleMessages.value) return []
  const messages = messagesList.value
  if (!messages || messages.length === 0) return []
  
  // 從最後一則訊息開始往前找有 quickReply 的訊息
  for (let i = messages.length - 1; i >= 0; i--) {
    const msg = messages[i]
    if (msg.quickReply?.items && msg.quickReply.items.length > 0) {
      return msg.quickReply.items
    }
  }
  return []
})

// 將文字中的 $ 替換為 emoji 圖片
const displayTextWithEmojis = computed(() => {
  const text = displayContent.value
  const emojis = textEmojis.value
  if (!emojis || emojis.length === 0) return null
  
  // 按 index 排序
  const sortedEmojis = [...emojis].sort((a, b) => a.index - b.index)
  
  let result = []
  let lastIndex = 0
  
  for (const emoji of sortedEmojis) {
    // 添加 $ 之前的文字
    if (emoji.index > lastIndex) {
      result.push({ type: 'text', content: text.substring(lastIndex, emoji.index) })
    }
    // 添加 emoji 圖片
    result.push({
      type: 'emoji',
      url: `https://stickershop.line-scdn.net/sticonshop/v1/sticon/${emoji.productId}/android/${emoji.emojiId}.png`,
      productId: emoji.productId,
      emojiId: emoji.emojiId
    })
    lastIndex = emoji.index + 1 // 跳過 $ 符號
  }
  
  // 添加剩餘的文字
  if (lastIndex < text.length) {
    result.push({ type: 'text', content: text.substring(lastIndex) })
  }
  
  return result
})

// 多訊息模式下解析單一訊息的 emoji
const getTextWithEmojis = (msg) => {
  if (!msg || !msg.text || !msg.emojis || msg.emojis.length === 0) return null
  
  const text = msg.text
  const emojis = msg.emojis
  
  // 按 index 排序
  const sortedEmojis = [...emojis].sort((a, b) => a.index - b.index)
  
  let result = []
  let lastIndex = 0
  
  for (const emoji of sortedEmojis) {
    // 添加 $ 之前的文字
    if (emoji.index > lastIndex) {
      result.push({ type: 'text', content: text.substring(lastIndex, emoji.index) })
    }
    // 添加 emoji 圖片
    result.push({
      type: 'emoji',
      url: `https://stickershop.line-scdn.net/sticonshop/v1/sticon/${emoji.productId}/android/${emoji.emojiId}.png`,
      productId: emoji.productId,
      emojiId: emoji.emojiId
    })
    lastIndex = emoji.index + 1 // 跳過 $ 符號
  }
  
  // 添加剩餘的文字
  if (lastIndex < text.length) {
    result.push({ type: 'text', content: text.substring(lastIndex) })
  }
  
  return result
}

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
    // 使用 android 路徑而非 iPhone（根據 LINE API 文件）
    return `https://stickershop.line-scdn.net/stickershop/v1/sticker/${stickerId}/android/sticker.png`
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

// Imagemap 預覽相關
const imagemapPreviewUrl = computed(() => {
  if (props.previewImg) {
    // 處理相對路徑
    if (props.previewImg.startsWith('/profile/')) {
      return getImageUrl(props.previewImg)
    }
    return props.previewImg
  }
  // 從 content 解析 baseUrl
  let baseUrl = parsedContent.value.baseUrl
  
  // 如果 parsedContent 是多訊息格式，嘗試從 messages 中取得 imagemap
  if (!baseUrl && parsedContent.value.messages) {
    const imagemapMsg = parsedContent.value.messages.find(m => m.type === 'imagemap')
    if (imagemapMsg) {
      baseUrl = imagemapMsg.baseUrl || ''
    }
  }
  
  if (baseUrl) {
    // 確保有尺寸後綴
    if (!baseUrl.match(/\/\d+$/)) {
      baseUrl = baseUrl + '/700'
    }
    if (baseUrl.startsWith('/profile/')) {
      return getImageUrl(baseUrl)
    }
    return baseUrl
  }
  return ''
})

const imagemapActionsCount = computed(() => {
  return parsedContent.value.actions?.length || 0
})

const imagemapStyle = computed(() => {
  const baseSize = parsedContent.value.baseSize
  if (baseSize && baseSize.width && baseSize.height) {
    const ratio = baseSize.height / baseSize.width
    const width = props.fullSize ? 280 : 180
    return {
      width: width + 'px',
      height: (width * ratio) + 'px'
    }
  }
  return { maxWidth: '280px', maxHeight: '400px' }
})

const formatDuration = (ms) => {
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 多訊息模式下取得 Imagemap 圖片 URL
const getImagemapUrl = (msg) => {
  // imagemap 訊息可能直接有 baseUrl，或在 contents 中
  let baseUrl = msg.baseUrl || ''
  
  // 如果沒有直接的 baseUrl，嘗試從 contents 解析
  if (!baseUrl && msg.contents) {
    let imagemapData = msg.contents
    if (typeof imagemapData === 'string') {
      try {
        imagemapData = JSON.parse(imagemapData)
      } catch {
        return ''
      }
    }
    baseUrl = imagemapData?.baseUrl || ''
  }
  
  if (!baseUrl) return ''
  
  // 確保 baseUrl 有尺寸後綴
  if (!baseUrl.match(/\/\d+$/)) {
    baseUrl = baseUrl + '/700'
  }
  
  // 處理相對路徑
  if (baseUrl.startsWith('/profile/')) {
    return getImageUrl(baseUrl)
  }
  return baseUrl
}

// 多訊息模式下取得 Imagemap 樣式
const getImagemapStyle = (msg) => {
  // 嘗試直接從 msg 取得 baseSize
  let baseSize = msg.baseSize
  
  // 如果沒有，嘗試從 contents 解析
  if (!baseSize && msg.contents) {
    let imagemapData = msg.contents
    if (typeof imagemapData === 'string') {
      try {
        imagemapData = JSON.parse(imagemapData)
      } catch {
        return { maxWidth: '200px', maxHeight: '300px' }
      }
    }
    baseSize = imagemapData?.baseSize
  }
  
  if (baseSize && baseSize.width && baseSize.height) {
    const ratio = baseSize.height / baseSize.width
    const width = props.fullSize ? 220 : 180
    return {
      width: width + 'px',
      height: (width * ratio) + 'px'
    }
  }
  return { maxWidth: '200px', maxHeight: '300px' }
}

const handleStickerError = () => {
  stickerError.value = true
}

// Template Message 預覽資料（單一訊息模式）
const templatePreviewData = computed(() => {
  if (props.msgType !== 'TEMPLATE') return null
  return parseTemplateData(parsedContent.value)
})

// 從訊息物件解析 Template 資料（多訊息模式）
const getTemplatePreviewData = (msg) => {
  if (!msg || msg.type !== 'template') return null
  return parseTemplateData(msg)
}

// 解析 Template 資料的通用函數
const parseTemplateData = (data) => {
  if (!data) return null
  
  // 檢查是否有 template 物件
  const template = data.template
  if (!template) return null
  
  const templateType = template.type
  if (!templateType) return null
  
  return {
    templateType: templateType,
    templateData: template,
    altText: data.altText || '模板訊息'
  }
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

  &.with-phone-frame {
    display: flex;
    justify-content: center;
    padding: 20px;
  }
}

// iPhone Mockup 容器（使用圖片作為外框）
.iphone-mockup-container {
  position: relative;
  width: 340px;
  height: 115%;
  
  .iphone-mockup-img {
    width: 100%;
    height: auto;
    display: block;
    pointer-events: none;
  }
  
  .iphone-screen-content {
    position: absolute;
    top: 2.5%;
    left: 5.5%;
    right: 5.5%;
    bottom: 2.5%;
    border-radius: 32px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    background: linear-gradient(180deg, #7494c0 0%, #8ea8c9 100%);
  }
  
  .status-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 20px;
    background: rgba(0, 0, 0, 0.1);
    
    .status-time {
      font-size: 14px;
      font-weight: 600;
      color: #000;
    }
    
    .status-icons {
      display: flex;
      align-items: center;
      gap: 6px;
      
      .signal-icon, .battery-icon {
        fill: #000;
      }
    }
  }
  
  .line-chat-content {
    flex: 1;
    padding: 12px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
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

// 時間戳記（訊息左下角）
.bubble-timestamp {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  align-self: flex-end;
  margin-right: 4px;
  white-space: nowrap;
}

// 單一訊息行（包含時間戳記和訊息泡泡）
.single-message-row {
  display: flex;
  align-items: flex-end;
  gap: 4px;
}

.message-preview {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  flex-direction: column;
  flex: 1;
  min-height: 180px;

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
    align-items: flex-end;
    gap: 4px;
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

    .inline-emoji {
      width: 18px;
      height: 18px;
      vertical-align: middle;
      margin: 0 1px;
    }
  }
}

// Quick Reply 預覽樣式
.quick-reply-preview {
  margin-top: 12px;
  width: 100%;
  
  .quick-reply-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .quick-reply-item {
    display: flex;
    align-items: center;
    gap: 6px;
    background: rgba(255, 255, 255, 0.95);
    border: 1px solid rgba(0, 0, 0, 0.1);
    border-radius: 20px;
    padding: 6px 14px;
    font-size: 13px;
    color: #06c755;
    font-weight: 500;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #f0f0f0;
      transform: translateY(-1px);
    }
    
    .quick-reply-icon {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      object-fit: cover;
    }
    
    .quick-reply-label {
      white-space: nowrap;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  // 多訊息模式底部固定樣式
  &.quick-reply-bottom {
    margin-top: auto;
    padding-top: 12px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
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
  .imagemap-image-wrapper {
    position: relative;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    
    :deep(.el-image) {
      display: block;
    }
    
    .imagemap-info {
      position: absolute;
      bottom: 8px;
      right: 8px;
    }
  }
  
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
