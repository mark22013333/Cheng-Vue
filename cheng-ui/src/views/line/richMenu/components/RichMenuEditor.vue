<template>
  <div class="rich-menu-editor" ref="editorRoot">
    <div class="editor-container">

      <div class="editor-left">
        <div class="canvas-wrapper">
          <div class="canvas-header">
            <span>選單預覽</span>
            <el-tag v-if="templateType" size="small" type="info">
              {{ getTemplateName(templateType) }}
            </el-tag>
          </div>

          <div class="canvas-container" :style="canvasStyle">
            <div
              v-for="(area, index) in areas"
              :key="index"
              class="menu-area"
              :class="{ active: selectedAreaIndex === index, 'is-custom': templateType === 'CUSTOM' }"
              :style="getAreaStyle(area, index)"
              @click="selectArea(index)"
              @mousedown="startDrag($event, index, 'move')"
            >
              <div class="area-label">
                <span class="area-number">{{ index + 1 }}</span>
                <div class="area-action" v-if="area.action">
                  <el-icon>
                    <component :is="getActionIcon(area.action.type)"/>
                  </el-icon>
                  <span class="action-text">{{ getActionLabel(area.action) }}</span>
                </div>
              </div>

              <template v-if="templateType === 'CUSTOM' && selectedAreaIndex === index">
                <div class="resize-handle handle-n" @mousedown.stop="startDrag($event, index, 'n')"></div>
                <div class="resize-handle handle-s" @mousedown.stop="startDrag($event, index, 's')"></div>
                <div class="resize-handle handle-w" @mousedown.stop="startDrag($event, index, 'w')"></div>
                <div class="resize-handle handle-e" @mousedown.stop="startDrag($event, index, 'e')"></div>
                <div class="resize-handle handle-nw" @mousedown.stop="startDrag($event, index, 'nw')"></div>
                <div class="resize-handle handle-ne" @mousedown.stop="startDrag($event, index, 'ne')"></div>
                <div class="resize-handle handle-sw" @mousedown.stop="startDrag($event, index, 'sw')"></div>
                <div class="resize-handle handle-se" @mousedown.stop="startDrag($event, index, 'se')"></div>
              </template>
            </div>
          </div>

          <div class="canvas-info">
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                圖片: {{ imageSize }} | 區塊: {{ areas.length }}/20
              </template>
            </el-alert>
            <el-button
              v-if="templateType === 'CUSTOM'"
              type="primary"
              size="small"
              :icon="Plus"
              @click="addCustomArea"
              style="width: 100%; margin-top: 10px"
            >
              新增區塊
            </el-button>
          </div>
        </div>
      </div>

      <div class="editor-right">
        <div class="settings-panel">
          <div class="panel-header">
            <span>區塊設定</span>
            <el-button
              v-if="selectedAreaIndex !== null"
              type="primary"
              link
              size="small"
              :icon="Delete"
              @click="removeArea"
            >
              刪除此區塊
            </el-button>
          </div>

          <div v-if="selectedAreaIndex !== null && selectedArea" class="area-settings">
            <el-form :model="selectedArea" label-width="90px" size="small">
              <el-form-item label="區塊編號">
                <el-tag>{{ selectedAreaIndex + 1 }}</el-tag>
              </el-form-item>

              <el-divider content-position="left">位置與尺寸</el-divider>

              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="X 座標">
                    <el-input-number v-model="selectedArea.bounds.x" :min="0" :max="maxWidth" controls-position="right"
                                     style="width: 100%" @change="updateArea"/>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Y 座標">
                    <el-input-number v-model="selectedArea.bounds.y" :min="0" :max="maxHeight" controls-position="right"
                                     style="width: 100%" @change="updateArea"/>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="寬度">
                    <el-input-number v-model="selectedArea.bounds.width" :min="1" :max="maxWidth"
                                     controls-position="right" style="width: 100%" @change="updateArea"/>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="高度">
                    <el-input-number v-model="selectedArea.bounds.height" :min="1" :max="maxHeight"
                                     controls-position="right" style="width: 100%" @change="updateArea"/>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-divider content-position="left">動作設定</el-divider>

              <el-form-item label="動作類型">
                <el-select
                  v-model="selectedArea.action.type"
                  placeholder="請選擇"
                  @change="onActionTypeChange"
                  style="width: 100%"
                >
                  <el-option label="開啟網址 (URI)" value="uri"/>
                  <el-option label="傳送訊息 (Message)" value="message"/>
                  <el-option label="回傳資料 (Postback)" value="postback"/>
                  <el-option label="切換選單 (Switch)" value="richmenuswitch"/>
                  <el-option label="日期時間 (Datetime)" value="datetimepicker"/>
                  <el-option label="開啟相機 (Camera)" value="camera"/>
                  <el-option label="開啟相簿 (Camera Roll)" value="cameraRoll"/>
                  <el-option label="傳送位置 (Location)" value="location"/>
                  <el-option label="複製文字 (Clipboard)" value="clipboard"/>
                </el-select>
              </el-form-item>

              <div class="action-details">
                <el-form-item v-if="selectedArea.action.type === 'uri'" label="網址">
                  <el-input v-model="selectedArea.action.uri" placeholder="https://..." @change="updateArea"/>
                </el-form-item>

                <el-form-item v-if="selectedArea.action.type === 'message'" label="訊息文字">
                  <el-input v-model="selectedArea.action.text" type="textarea" :rows="3" placeholder="請輸入訊息"
                            @change="updateArea"/>
                </el-form-item>

                <template v-if="selectedArea.action.type === 'postback'">
                  <el-form-item label="回傳資料">
                    <el-input v-model="selectedArea.action.data" placeholder="例如: action=buy" @change="updateArea"/>
                  </el-form-item>
                  <el-form-item label="顯示文字">
                    <el-input v-model="selectedArea.action.displayText" placeholder="使用者看到的文字"
                              @change="updateArea"/>
                  </el-form-item>
                </template>

                <template v-if="selectedArea.action.type === 'richmenuswitch'">
                  <el-form-item label="Alias ID" required>
                    <el-select
                      v-model="selectedArea.action.richMenuAliasId"
                      placeholder="選擇 Alias"
                      filterable
                      allow-create
                      style="width: 100%"
                      @change="updateArea"
                    >
                      <el-option
                        v-for="alias in aliasOptions"
                        :key="alias.aliasId"
                        :label="alias.aliasId"
                        :value="alias.aliasId"
                      >
                        <span>{{ alias.aliasId }}</span>
                        <span style="float:right; color:#999; font-size:12px">{{ alias.richMenuName }}</span>
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="回傳資料" required>
                    <el-input v-model="selectedArea.action.data" placeholder="richmenu-changed" @change="updateArea"/>
                  </el-form-item>
                </template>

                <template v-if="selectedArea.action.type === 'datetimepicker'">
                  <el-form-item label="回傳資料">
                    <el-input v-model="selectedArea.action.data" placeholder="key=value" @change="updateArea"/>
                  </el-form-item>
                  <el-form-item label="模式">
                    <el-select v-model="selectedArea.action.mode" @change="updateArea" style="width: 100%">
                      <el-option label="日期" value="date"/>
                      <el-option label="時間" value="time"/>
                      <el-option label="日期時間" value="datetime"/>
                    </el-select>
                  </el-form-item>
                </template>

                <!-- Camera / CameraRoll / Location 無需額外欄位 -->
                <el-alert
                  v-if="selectedArea.action.type === 'camera'"
                  title="點擊後開啟使用者的相機"
                  type="info"
                  :closable="false"
                  show-icon
                />
                <el-alert
                  v-if="selectedArea.action.type === 'cameraRoll'"
                  title="點擊後開啟使用者的相簿"
                  type="info"
                  :closable="false"
                  show-icon
                />
                <el-alert
                  v-if="selectedArea.action.type === 'location'"
                  title="點擊後讓使用者傳送目前位置"
                  type="info"
                  :closable="false"
                  show-icon
                />

                <!-- Clipboard Action -->
                <el-form-item v-if="selectedArea.action.type === 'clipboard'" label="複製文字" required>
                  <el-input
                    v-model="selectedArea.action.clipboardText"
                    type="textarea"
                    :rows="3"
                    placeholder="點擊後複製到剪貼簿的文字"
                    maxlength="1000"
                    show-word-limit
                    @change="updateArea"
                  />
                </el-form-item>
              </div>
            </el-form>
          </div>

          <div v-else class="empty-selection">
            <el-icon :size="48" color="#dcdfe6">
              <InfoFilled/>
            </el-icon>
            <p>請點擊左側畫布區塊</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, watch, onMounted, onBeforeUnmount} from 'vue'
import {listAllAliases} from '@/api/line/richMenuAlias'
import {getImageUrl} from '@/utils/image'
import {ElMessage} from 'element-plus'
import {Plus, Delete, InfoFilled, Link, ChatDotRound, Promotion, SwitchButton, Calendar, Camera, Picture, Location, DocumentCopy} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {type: Array, default: () => []},
  templateType: {type: String, default: null},
  imageSize: {type: String, default: '2500x1686'},
  imageUrl: {type: String, default: ''}
})

const emit = defineEmits(['update:modelValue', 'change', 'manual-edit'])

// Data
const editorRoot = ref(null)
const areas = ref([])
const selectedAreaIndex = ref(null)
const canvasWidth = ref(600)
const aliasOptions = ref([])

// Dragging
let isDragging = false
let dragMode = null
let dragIndex = null
let dragStartX = 0
let dragStartY = 0
let dragStartBounds = null

// Computed
const selectedArea = computed(() => {
  if (selectedAreaIndex.value !== null && areas.value[selectedAreaIndex.value]) {
    return areas.value[selectedAreaIndex.value]
  }
  return null
})

const maxWidth = computed(() => {
  if (!props.imageSize) return 2500
  const [width] = props.imageSize.split('x').map(Number)
  return width
})

const maxHeight = computed(() => {
  if (!props.imageSize) return 1686
  const [, height] = props.imageSize.split('x').map(Number)
  return height
})

const canvasStyle = computed(() => {
  if (!props.imageSize) {
    return {
      width: canvasWidth.value + 'px',
      height: '405px',
      position: 'relative',
      background: '#f5f5f5',
      border: '1px solid #ddd'
    }
  }
  const [width, height] = props.imageSize.split('x').map(Number)
  const ratio = height / width
  const style = {
    width: canvasWidth.value + 'px',
    height: (canvasWidth.value * ratio) + 'px',
    position: 'relative',
    border: '1px solid #ddd'
  }
  if (props.imageUrl) {
    const imageFullUrl = getImageUrl(props.imageUrl)
    style.background = `linear-gradient(rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0.3)), url(${imageFullUrl})`
    style.backgroundSize = 'contain'
    style.backgroundPosition = 'center'
    style.backgroundRepeat = 'no-repeat'
  } else {
    style.background = '#f5f5f5'
  }
  return style
})

// Watchers
watch(() => props.modelValue, (newVal) => {
  if (newVal && Array.isArray(newVal)) {
    const newValStr = JSON.stringify(newVal)
    const oldValStr = JSON.stringify(areas.value)
    if (newValStr !== oldValStr) {
      areas.value = JSON.parse(JSON.stringify(newVal))
      if (selectedAreaIndex.value !== null && selectedAreaIndex.value >= areas.value.length) {
        selectedAreaIndex.value = null
      }
    }
  } else {
    areas.value = []
    selectedAreaIndex.value = null
  }
}, {immediate: true, deep: true})

watch(() => props.templateType, (newVal, oldVal) => {
  if (newVal && newVal !== oldVal && newVal !== 'CUSTOM') {
    initTemplateAreas(newVal)
  }
})

watch(() => props.imageSize, (newVal, oldVal) => {
  if (newVal && oldVal && newVal !== oldVal) {
    adjustAreasToNewSize(newVal, oldVal)
  }
})

onMounted(() => {
  loadAliases()
  if ((!props.modelValue || props.modelValue.length === 0) && props.templateType && props.templateType !== 'CUSTOM') {
    initTemplateAreas(props.templateType)
  }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
})

// Helper Functions
const loadAliases = async () => {
  try {
    const response = await listAllAliases()
    aliasOptions.value = response.rows || []
  } catch (error) {
    console.error('載入 Alias 失敗:', error)
  }
}

const getTemplateName = (type) => {
  const names = {
    'TWO_COLS': '左右兩格',
    'TWO_COLS_HIGH': '左右兩格(高)',
    'THREE_ROWS': '上下三格',
    'THREE_COLS': '左右三格',
    'FOUR_GRID': '四格',
    'SIX_GRID': '六格',
    'SIX_GRID_ALT': '六格(橫)',
    'ALIAS_SWITCH_GRID': '別名切換網格',
    'CUSTOM': '自訂'
  }
  return names[type] || type
}

const getActionIcon = (type) => {
  const icons = {
    'uri': Link,
    'message': ChatDotRound,
    'postback': Promotion,
    'richmenuswitch': SwitchButton,
    'datetimepicker': Calendar,
    'camera': Camera,
    'cameraRoll': Picture,
    'location': Location,
    'clipboard': DocumentCopy
  }
  return icons[type] || Link
}

const getActionLabel = (action) => {
  if (action.type === 'uri') return action.uri || '未設定'
  if (action.type === 'message') return action.text || '未設定'
  if (action.type === 'postback') return action.data || '未設定'
  if (action.type === 'richmenuswitch') return action.richMenuAliasId || '未設定'
  if (action.type === 'datetimepicker') return action.mode || 'date'
  if (action.type === 'camera') return '開啟相機'
  if (action.type === 'cameraRoll') return '開啟相簿'
  if (action.type === 'location') return '傳送位置'
  if (action.type === 'clipboard') return action.clipboardText?.substring(0, 20) || '未設定'
  return ''
}

// Logic: Init Template
const initTemplateAreas = (templateType) => {
  const templates = {
    'TWO_COLS': createTwoColsTemplate(),
    'TWO_COLS_HIGH': createTwoColsTemplate(),
    'THREE_ROWS': createThreeRowsTemplate(),
    'THREE_COLS': createThreeColsTemplate(),
    'FOUR_GRID': createFourGridTemplate(),
    'SIX_GRID': createSixGridTemplate(),
    'SIX_GRID_ALT': createSixGridAltTemplate(),
    'ALIAS_SWITCH_GRID': createAliasSwitchGridTemplate()
  }
  areas.value = templates[templateType] || []
  emitChange()
}

const createTwoColsTemplate = () => {
  if (!props.imageSize) return []
  const [width, height] = props.imageSize.split('x').map(Number)
  const halfWidth = Math.floor(width / 2)
  return [{bounds: {x: 0, y: 0, width: halfWidth, height}, action: {type: 'uri', uri: ''}}, {
    bounds: {
      x: halfWidth,
      y: 0,
      width: halfWidth,
      height
    }, action: {type: 'uri', uri: ''}
  }]
}
const createThreeRowsTemplate = () => {
  if (!props.imageSize) return []
  const [width, height] = props.imageSize.split('x').map(Number)
  const h = Math.floor(height / 3)
  return [{bounds: {x: 0, y: 0, width, height: h}, action: {type: 'uri'}}, {
    bounds: {x: 0, y: h, width, height: h},
    action: {type: 'uri'}
  }, {bounds: {x: 0, y: h * 2, width, height: height - h * 2}, action: {type: 'uri'}}]
}
const createThreeColsTemplate = () => {
  if (!props.imageSize) return []
  const [width, height] = props.imageSize.split('x').map(Number)
  const w = Math.floor(width / 3)
  return [{bounds: {x: 0, y: 0, width: w, height}, action: {type: 'uri'}}, {
    bounds: {x: w, y: 0, width: w, height},
    action: {type: 'uri'}
  }, {bounds: {x: w * 2, y: 0, width: width - w * 2, height}, action: {type: 'uri'}}]
}
const createFourGridTemplate = () => {
  if (!props.imageSize) return []
  const [width, height] = props.imageSize.split('x').map(Number)
  const w = Math.floor(width / 2), h = Math.floor(height / 2)
  return [{bounds: {x: 0, y: 0, width: w, height: h}, action: {type: 'uri'}}, {
    bounds: {
      x: w,
      y: 0,
      width: w,
      height: h
    }, action: {type: 'uri'}
  }, {bounds: {x: 0, y: h, width: w, height: h}, action: {type: 'uri'}}, {
    bounds: {x: w, y: h, width: w, height: h},
    action: {type: 'uri'}
  }]
}
const createSixGridTemplate = () => { // 2x3
  if (!props.imageSize) return []
  const [width, height] = props.imageSize.split('x').map(Number)
  const w = Math.floor(width / 2), h = Math.floor(height / 3)
  let arr = []
  for (let r = 0; r < 3; r++) for (let c = 0; c < 2; c++) arr.push({
    bounds: {x: c * w, y: r * h, width: w, height: h},
    action: {type: 'uri'}
  })
  return arr
}
const createSixGridAltTemplate = () => { // 3x2
  if (!props.imageSize) return []
  const [width, height] = props.imageSize.split('x').map(Number)
  const w = Math.floor(width / 3), h = Math.floor(height / 2)
  let arr = []
  for (let r = 0; r < 2; r++) for (let c = 0; c < 3; c++) arr.push({
    bounds: {x: c * w, y: r * h, width: w, height: h},
    action: {type: 'uri'}
  })
  return arr
}
// [新增] 別名切換網格版型 (2 + 6 格)
const createAliasSwitchGridTemplate = () => {
  if (!props.imageSize) return []
  const [width, height] = props.imageSize.split('x').map(Number)

  // 上方控制列高度 (假設佔 1/8 或固定比例，這裡設為 height / 8，約 210px)
  // 如果要精確像截圖那樣，可以微調比例。
  const topHeight = Math.floor(height * 0.125)
  const bottomHeight = height - topHeight
  const topWidth = Math.floor(width / 2)
  const bottomWidth = Math.floor(width / 3)
  const bottomRowHeight = Math.floor(bottomHeight / 2)

  let areas = []

  // 上方兩個按鈕
  areas.push({
    bounds: {x: 0, y: 0, width: topWidth, height: topHeight},
    action: {type: 'richmenuswitch', data: 'switch-left'}
  })
  areas.push({
    bounds: {x: topWidth, y: 0, width: topWidth, height: topHeight},
    action: {type: 'richmenuswitch', data: 'switch-right'}
  })

  // 下方 2x3 網格
  for (let row = 0; row < 2; row++) {
    for (let col = 0; col < 3; col++) {
      areas.push({
        bounds: {
          x: col * bottomWidth,
          y: topHeight + (row * bottomRowHeight),
          width: bottomWidth,
          height: bottomRowHeight
        },
        action: {type: 'uri', uri: ''}
      })
    }
  }

  return areas
}

// Styling & Interactions
const getAreaStyle = (area, index) => {
  if (!props.imageSize) return {}
  const [imgWidth, imgHeight] = props.imageSize.split('x').map(Number)
  const scaleX = canvasWidth.value / imgWidth
  const scaleY = (canvasWidth.value * imgHeight / imgWidth) / imgHeight

  const isOverlapping = props.templateType === 'CUSTOM' && hasOverlap(index)

  return {
    position: 'absolute',
    left: (area.bounds.x * scaleX) + 'px',
    top: (area.bounds.y * scaleY) + 'px',
    width: (area.bounds.width * scaleX) + 'px',
    height: (area.bounds.height * scaleY) + 'px',
    border: isOverlapping ? '2px solid #F56C6C' : '2px solid #409EFF',
    backgroundColor: isOverlapping ? 'rgba(245, 108, 108, 0.3)' : 'rgba(64, 158, 255, 0.2)',
    cursor: 'pointer'
  }
}

const selectArea = (index) => {
  selectedAreaIndex.value = index
}

const removeArea = () => {
  if (selectedAreaIndex.value !== null) {
    areas.value.splice(selectedAreaIndex.value, 1)
    selectedAreaIndex.value = null
    emitChange()
  }
}

const addCustomArea = () => {
  if (areas.value.length >= 20) return
  const [w, h] = props.imageSize.split('x').map(Number)
  const newW = Math.floor(w / 4), newH = Math.floor(h / 4)
  const offX = (areas.value.length % 4) * newW
  const offY = Math.floor(areas.value.length / 4) * newH

  areas.value.push({bounds: {x: offX, y: offY, width: newW, height: newH}, action: {type: 'uri', uri: ''}})
  selectedAreaIndex.value = areas.value.length - 1
  emit('manual-edit')
  emitChange()
}

const updateArea = () => emitChange()

const startDrag = (event, index, mode) => {
  event.preventDefault()
  isDragging = true
  dragMode = mode
  dragIndex = index
  dragStartX = event.clientX
  dragStartY = event.clientY
  dragStartBounds = {...areas.value[index].bounds}
  selectedAreaIndex.value = index
}

const onDrag = (event) => {
  if (!isDragging || dragIndex === null) return
  event.preventDefault()

  const [imgW, imgH] = props.imageSize.split('x').map(Number)
  const scaleX = imgW / canvasWidth.value
  const scaleY = imgH / (canvasWidth.value * imgH / imgW)

  const deltaX = (event.clientX - dragStartX) * scaleX
  const deltaY = (event.clientY - dragStartY) * scaleY

  const area = areas.value[dragIndex]
  let b = {...dragStartBounds}

  if (dragMode === 'move') {
    b.x += deltaX;
    b.y += deltaY
  } else {
    if (dragMode.includes('e')) b.width += deltaX
    if (dragMode.includes('s')) b.height += deltaY
    if (dragMode.includes('w')) {
      b.x += deltaX;
      b.width -= deltaX;
    }
    if (dragMode.includes('n')) {
      b.y += deltaY;
      b.height -= deltaY;
    }
  }

  // Boundary checks
  b = applyBoundaryConstraints(b, imgW, imgH)
  area.bounds = b
}

const endDrag = () => {
  if (isDragging) {
    isDragging = false
    dragIndex = null
    emitChange()
  }
}

const applyBoundaryConstraints = (b, maxW, maxH) => {
  b.x = Math.max(0, Math.min(b.x, maxW - 50))
  b.y = Math.max(0, Math.min(b.y, maxH - 50))
  b.width = Math.max(50, Math.min(b.width, maxW - b.x))
  b.height = Math.max(50, Math.min(b.height, maxH - b.y))
  return b
}

const hasOverlap = (index) => {
  const t = areas.value[index].bounds
  return areas.value.some((a, i) => i !== index && !(t.x + t.width <= a.bounds.x || a.bounds.x + a.bounds.width <= t.x || t.y + t.height <= a.bounds.y || a.bounds.y + a.bounds.height <= t.y))
}

const onActionTypeChange = () => {
  selectedArea.value.action = {type: selectedArea.value.action.type, data: '', text: '', uri: ''} // Reset properties
  emitChange()
}

const emitChange = () => {
  emit('update:modelValue', areas.value)
  emit('change', areas.value)
}

const adjustAreasToNewSize = (newSize, oldSize) => {
  if (!areas.value.length) return
  const [nW, nH] = newSize.split('x').map(Number)
  const [oW, oH] = oldSize.split('x').map(Number)
  const sX = nW / oW, sY = nH / oH

  areas.value.forEach(area => {
    area.bounds.x = Math.round(area.bounds.x * sX)
    area.bounds.y = Math.round(area.bounds.y * sY)
    area.bounds.width = Math.round(area.bounds.width * sX)
    area.bounds.height = Math.round(area.bounds.height * sY)
  })
  emitChange()
}
</script>

<style scoped>
.rich-menu-editor {
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  width: 100%;
}

/* 使用 Grid 佈局解決右側空白問題 */
.editor-container {
  display: grid;
  grid-template-columns: 630px 1fr; /* 左固定，右填滿 */
  gap: 20px;
  align-items: start;
}

.editor-left {
  width: 100%;
  overflow: hidden;
}

.editor-right {
  width: 100%;
  min-width: 0;
  height: 100%;
}

/* 響應式處理 */
@media (max-width: 1200px) {
  .editor-container {
    grid-template-columns: 1fr;
  }

  .editor-left {
    width: 630px;
    margin: 0 auto;
  }
}

.settings-panel {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fff;
  height: 100%;
  min-height: 500px;
  display: flex;
  flex-direction: column;
}

/* 增加表單項目的間距 */
.area-settings .el-form-item {
  margin-bottom: 18px; /* 增加底部間距 */
}

.empty-selection {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.canvas-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fff;
  height: 100%;
}

.canvas-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.canvas-container {
  margin: 0 auto;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.menu-area {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.area-label {
  display: flex;
  flex-direction: column;
  align-items: center;
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
  margin-bottom: 4px;
}

.area-action {
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 4px;
  max-width: 100px;
}

.action-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resize-handle {
  position: absolute;
  background: #fff;
  border: 1px solid #409EFF;
  width: 8px;
  height: 8px;
  z-index: 10;
}

.handle-se {
  bottom: -4px;
  right: -4px;
  cursor: se-resize;
}

.handle-sw {
  bottom: -4px;
  left: -4px;
  cursor: sw-resize;
}

.handle-ne {
  top: -4px;
  right: -4px;
  cursor: ne-resize;
}

.handle-nw {
  top: -4px;
  left: -4px;
  cursor: nw-resize;
}

.handle-n {
  top: -4px;
  left: 50%;
  margin-left: -4px;
  cursor: n-resize;
}

.handle-s {
  bottom: -4px;
  left: 50%;
  margin-left: -4px;
  cursor: s-resize;
}

.handle-e {
  right: -4px;
  top: 50%;
  margin-top: -4px;
  cursor: e-resize;
}

.handle-w {
  left: -4px;
  top: 50%;
  margin-top: -4px;
  cursor: w-resize;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}
</style>
