<template>
  <div class="rich-menu-editor">
    <el-row :gutter="20" type="flex">
      <!-- 左側：畫布預覽區 -->
      <el-col :span="14" style="flex-shrink: 0;">
        <div class="canvas-wrapper">
          <div class="canvas-header">
            <span>選單預覽</span>
            <el-tag v-if="selectedTemplate" size="small" type="info">
              {{ getTemplateName(selectedTemplate) }}
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
                  <i :class="getActionIcon(area.action.type)"></i>
                  <span class="action-text">{{ getActionLabel(area.action) }}</span>
                </div>
              </div>
              <!-- 拖曳調整大小的手柄（只在自訂版型且被選中時顯示） -->
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
            <el-alert type="info" :closable="false">
              <template slot="title">
                圖片尺寸：{{ imageSize }} | 區塊數量：{{ areas.length }}/20
              </template>
            </el-alert>
            <el-button
              v-if="templateType === 'CUSTOM'"
              type="primary"
              size="small"
              icon="el-icon-plus"
              @click="addCustomArea"
              style="width: 100%; margin-top: 10px"
            >
              新增區塊
            </el-button>
          </div>
        </div>
      </el-col>

      <!-- 右側：區塊設定面板 -->
      <el-col :span="10" style="flex-shrink: 0;">
        <div class="settings-panel">
          <div class="panel-header">
            <span>區塊設定</span>
            <el-button
              v-if="selectedAreaIndex !== null"
              type="text"
              size="mini"
              icon="el-icon-delete"
              @click="removeArea"
            >
              刪除此區塊
            </el-button>
          </div>

          <div v-if="selectedAreaIndex !== null && selectedArea" class="area-settings">
            <el-form :model="selectedArea" label-width="100px" size="small">
              <el-form-item label="區塊編號">
                <el-tag>{{ selectedAreaIndex + 1}}</el-tag>
              </el-form-item>

              <el-divider>區塊位置與尺寸</el-divider>

              <el-form-item label="X 座標">
                <el-input-number
                  v-model="selectedArea.bounds.x"
                  :min="0"
                  :max="maxWidth"
                  @change="updateArea"
                  style="width: 100%"
                />
              </el-form-item>

              <el-form-item label="Y 座標">
                <el-input-number
                  v-model="selectedArea.bounds.y"
                  :min="0"
                  :max="maxHeight"
                  @change="updateArea"
                  style="width: 100%"
                />
              </el-form-item>

              <el-form-item label="寬度">
                <el-input-number
                  v-model="selectedArea.bounds.width"
                  :min="1"
                  :max="maxWidth"
                  @change="updateArea"
                  style="width: 100%"
                />
              </el-form-item>

              <el-form-item label="高度">
                <el-input-number
                  v-model="selectedArea.bounds.height"
                  :min="1"
                  :max="maxHeight"
                  @change="updateArea"
                  style="width: 100%"
                />
              </el-form-item>

              <el-divider>動作設定</el-divider>

              <el-form-item label="動作類型">
                <el-select
                  v-model="selectedArea.action.type"
                  placeholder="請選擇動作類型"
                  @change="onActionTypeChange"
                  style="width: 100%"
                >
                  <el-option label="開啟網址" value="uri" />
                  <el-option label="傳送訊息" value="message" />
                  <el-option label="回傳資料" value="postback" />
                  <el-option label="切換選單" value="richmenuswitch" />
                  <el-option label="日期時間選擇" value="datetimepicker" />
                </el-select>
              </el-form-item>

              <!-- URI 動作 -->
              <el-form-item v-if="selectedArea.action.type === 'uri'" label="網址">
                <el-input
                  v-model="selectedArea.action.uri"
                  placeholder="https://example.com"
                  @change="updateArea"
                />
              </el-form-item>

              <!-- Message 動作 -->
              <el-form-item v-if="selectedArea.action.type === 'message'" label="訊息文字">
                <el-input
                  v-model="selectedArea.action.text"
                  type="textarea"
                  :rows="3"
                  placeholder="請輸入要傳送的訊息"
                  @change="updateArea"
                />
              </el-form-item>

              <!-- Postback 動作 -->
              <template v-if="selectedArea.action.type === 'postback'">
                <el-form-item label="回傳資料">
                  <el-input
                    v-model="selectedArea.action.data"
                    placeholder="action=buy&itemid=123"
                    @change="updateArea"
                  />
                </el-form-item>
                <el-form-item label="顯示文字">
                  <el-input
                    v-model="selectedArea.action.displayText"
                    placeholder="選用：點擊後顯示的文字"
                    @change="updateArea"
                  />
                </el-form-item>
              </template>

              <!-- Rich Menu Switch 動作 -->
              <template v-if="selectedArea.action.type === 'richmenuswitch'">
                <el-form-item label="目標選單 Alias" required>
                  <el-select
                    v-model="selectedArea.action.richMenuAliasId"
                    placeholder="請選擇 Rich Menu Alias"
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
                      <span style="float: left">{{ alias.aliasId }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px">{{ alias.richMenuName }}</span>
                    </el-option>
                  </el-select>
                  <span style="color: #999; font-size: 12px">
                    ⚠️ 請選擇或輸入目標 Rich Menu 的 <strong>Alias ID</strong>（只能包含小寫字母、數字和連字號，最多 32 字元）
                  </span>
                </el-form-item>
                <el-form-item label="回傳資料" required>
                  <el-input
                    v-model="selectedArea.action.data"
                    placeholder="richmenu-changed-to-a"
                    @change="updateArea"
                  />
                  <span style="color: #999; font-size: 12px">
                    用於追蹤切換事件的 postback 資料
                  </span>
                </el-form-item>
              </template>

              <!-- Datetime Picker 動作 -->
              <template v-if="selectedArea.action.type === 'datetimepicker'">
                <el-form-item label="回傳資料">
                  <el-input
                    v-model="selectedArea.action.data"
                    placeholder="storeId=12345"
                    @change="updateArea"
                  />
                </el-form-item>
                <el-form-item label="模式">
                  <el-select v-model="selectedArea.action.mode" @change="updateArea" style="width: 100%">
                    <el-option label="日期" value="date" />
                    <el-option label="時間" value="time" />
                    <el-option label="日期時間" value="datetime" />
                  </el-select>
                </el-form-item>
              </template>
            </el-form>
          </div>

          <div v-else class="empty-selection">
            <i class="el-icon-info"></i>
            <p>請點擊左側畫布中的區塊進行編輯</p>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listAllAliases } from '@/api/line/richMenuAlias'

export default {
  name: 'RichMenuEditor',
  props: {
    value: {
      type: Array,
      default: () => []
    },
    templateType: {
      type: String,
      default: null
    },
    imageSize: {
      type: String,
      default: '2500x1686'
    }
  },
  data() {
    return {
      areas: [],
      selectedAreaIndex: null,
      canvasWidth: 400,
      canvasHeight: 270,
      // 拖曳狀態
      isDragging: false,
      dragMode: null, // 'move', 'n', 's', 'w', 'e', 'nw', 'ne', 'sw', 'se'
      dragIndex: null,
      dragStartX: 0,
      dragStartY: 0,
      dragStartBounds: null,
      // Rich Menu Alias 選項
      aliasOptions: []
    }
  },
  computed: {
    selectedTemplate() {
      return this.templateType
    },
    selectedArea() {
      if (this.selectedAreaIndex !== null && this.areas[this.selectedAreaIndex]) {
        return this.areas[this.selectedAreaIndex]
      }
      return null
    },
    maxWidth() {
      if (!this.imageSize) return 2500
      const [width] = this.imageSize.split('x').map(Number)
      return width
    },
    maxHeight() {
      if (!this.imageSize) return 1686
      const [, height] = this.imageSize.split('x').map(Number)
      return height
    },
    canvasStyle() {
      if (!this.imageSize) {
        return {
          width: this.canvasWidth + 'px',
          height: (this.canvasWidth * 1686 / 2500) + 'px',
          position: 'relative',
          background: '#f5f5f5',
          border: '1px solid #ddd'
        }
      }
      const [width, height] = this.imageSize.split('x').map(Number)
      const ratio = height / width
      return {
        width: this.canvasWidth + 'px',
        height: (this.canvasWidth * ratio) + 'px',
        position: 'relative',
        background: '#f5f5f5',
        border: '1px solid #ddd'
      }
    }
  },
  watch: {
    value: {
      handler(newVal, oldVal) {
        // 只有當值真的改變時才更新（深度比較）
        if (newVal && Array.isArray(newVal)) {
          const newValStr = JSON.stringify(newVal)
          const oldValStr = JSON.stringify(this.areas)
          if (newValStr !== oldValStr) {
            this.areas = JSON.parse(JSON.stringify(newVal))
            // 如果有資料且被選中的索引超出範圍，重置選擇
            if (this.selectedAreaIndex !== null && this.selectedAreaIndex >= this.areas.length) {
              this.selectedAreaIndex = null
            }
          }
        } else if (newVal === null || (Array.isArray(newVal) && newVal.length === 0)) {
          // 如果是 null 或空陣列，清空 areas
          this.areas = []
          this.selectedAreaIndex = null
        }
      },
      immediate: true,
      deep: true
    },
    templateType: {
      handler(newVal, oldVal) {
        // 當版型切換時，如果是預設版型就初始化區塊
        if (newVal && newVal !== oldVal && newVal !== 'CUSTOM') {
          this.initTemplateAreas(newVal)
        }
      },
      immediate: false
    }
  },
  mounted() {
    // 載入所有可用的 Rich Menu Alias
    this.loadAliases()
    
    // 如果是新建（沒有 areas）且不是自訂版型，初始化預設版型
    // 必須確保 templateType 是有效值（不是 null 或 undefined）
    if ((!this.value || this.value.length === 0) && 
        this.templateType && 
        this.templateType !== 'CUSTOM') {
      this.initTemplateAreas(this.templateType)
    }
    // 添加全域拖曳事件監聽
    document.addEventListener('mousemove', this.onDrag)
    document.addEventListener('mouseup', this.endDrag)
  },
  beforeDestroy() {
    // 移除全域拖曳事件監聽
    document.removeEventListener('mousemove', this.onDrag)
    document.removeEventListener('mouseup', this.endDrag)
  },
  methods: {
    /** 載入所有可用的 Rich Menu Alias */
    loadAliases() {
      listAllAliases().then(response => {
        this.aliasOptions = response.rows || []
      }).catch(error => {
        console.error('載入 Rich Menu Alias 失敗:', error)
        this.aliasOptions = []
      })
    },
    initTemplateAreas(templateType) {
      const templates = {
        'TWO_COLS': this.createTwoColsTemplate(),
        'TWO_COLS_HIGH': this.createTwoColsTemplate(),
        'THREE_ROWS': this.createThreeRowsTemplate(),
        'THREE_COLS': this.createThreeColsTemplate(),
        'FOUR_GRID': this.createFourGridTemplate(),
        'SIX_GRID': this.createSixGridTemplate(),
        'SIX_GRID_ALT': this.createSixGridAltTemplate(),
        'ALIAS_SWITCH_GRID': this.createAliasSwitchGridTemplate()
      }

      this.areas = templates[templateType] || []
      this.emitChange()
    },
    createTwoColsTemplate() {
      if (!this.imageSize) return []
      const [width, height] = this.imageSize.split('x').map(Number)
      const halfWidth = Math.floor(width / 2)
      return [
        { bounds: { x: 0, y: 0, width: halfWidth, height }, action: { type: 'uri', uri: '' } },
        { bounds: { x: halfWidth, y: 0, width: halfWidth, height }, action: { type: 'uri', uri: '' } }
      ]
    },
    createThreeRowsTemplate() {
      if (!this.imageSize) return []
      const [width, height] = this.imageSize.split('x').map(Number)
      const rowHeight = Math.floor(height / 3)
      return [
        { bounds: { x: 0, y: 0, width, height: rowHeight }, action: { type: 'uri', uri: '' } },
        { bounds: { x: 0, y: rowHeight, width, height: rowHeight }, action: { type: 'uri', uri: '' } },
        { bounds: { x: 0, y: rowHeight * 2, width, height: height - rowHeight * 2 }, action: { type: 'uri', uri: '' } }
      ]
    },
    createThreeColsTemplate() {
      if (!this.imageSize) return []
      const [width, height] = this.imageSize.split('x').map(Number)
      const colWidth = Math.floor(width / 3)
      return [
        { bounds: { x: 0, y: 0, width: colWidth, height }, action: { type: 'uri', uri: '' } },
        { bounds: { x: colWidth, y: 0, width: colWidth, height }, action: { type: 'uri', uri: '' } },
        { bounds: { x: colWidth * 2, y: 0, width: width - colWidth * 2, height }, action: { type: 'uri', uri: '' } }
      ]
    },
    createFourGridTemplate() {
      if (!this.imageSize) return []
      const [width, height] = this.imageSize.split('x').map(Number)
      const halfWidth = Math.floor(width / 2)
      const halfHeight = Math.floor(height / 2)
      return [
        { bounds: { x: 0, y: 0, width: halfWidth, height: halfHeight }, action: { type: 'uri', uri: '' } },
        { bounds: { x: halfWidth, y: 0, width: halfWidth, height: halfHeight }, action: { type: 'uri', uri: '' } },
        { bounds: { x: 0, y: halfHeight, width: halfWidth, height: halfHeight }, action: { type: 'uri', uri: '' } },
        { bounds: { x: halfWidth, y: halfHeight, width: halfWidth, height: halfHeight }, action: { type: 'uri', uri: '' } }
      ]
    },
    createSixGridTemplate() {
      if (!this.imageSize) return []
      const [width, height] = this.imageSize.split('x').map(Number)
      const colWidth = Math.floor(width / 2)
      const rowHeight = Math.floor(height / 3)
      const areas = []
      for (let row = 0; row < 3; row++) {
        for (let col = 0; col < 2; col++) {
          areas.push({
            bounds: {
              x: col * colWidth,
              y: row * rowHeight,
              width: colWidth,
              height: rowHeight
            },
            action: { type: 'uri', uri: '' }
          })
        }
      }
      return areas
    },
    createSixGridAltTemplate() {
      if (!this.imageSize) return []
      const [width, height] = this.imageSize.split('x').map(Number)
      const colWidth = Math.floor(width / 3)
      const rowHeight = Math.floor(height / 2)
      const areas = []
      for (let row = 0; row < 2; row++) {
        for (let col = 0; col < 3; col++) {
          areas.push({
            bounds: {
              x: col * colWidth,
              y: row * rowHeight,
              width: colWidth,
              height: rowHeight
            },
            action: { type: 'uri', uri: '' }
          })
        }
      }
      return areas
    },
    createAliasSwitchGridTemplate() {
      if (!this.imageSize) return []
      const [width, height] = this.imageSize.split('x').map(Number)
      
      // 上方別名切換區域高度（佔總高度的 1/8）
      const topHeight = Math.floor(height / 8)
      const bottomHeight = height - topHeight
      
      // 上方兩個別名切換按鈕
      const halfWidth = Math.floor(width / 2)
      const areas = [
        // 上方左：ALIAS-A
        { 
          bounds: { x: 0, y: 0, width: halfWidth, height: topHeight }, 
          action: { type: 'richmenuswitch', richMenuAliasId: '', data: 'switch-alias-a' } 
        },
        // 上方右：ALIAS-B
        { 
          bounds: { x: halfWidth, y: 0, width: halfWidth, height: topHeight }, 
          action: { type: 'richmenuswitch', richMenuAliasId: '', data: 'switch-alias-b' } 
        }
      ]
      
      // 下方 2 列 × 3 行網格（6 個功能按鈕）
      const colWidth = Math.floor(width / 3)
      const rowHeight = Math.floor(bottomHeight / 2)
      
      for (let row = 0; row < 2; row++) {
        for (let col = 0; col < 3; col++) {
          areas.push({
            bounds: {
              x: col * colWidth,
              y: topHeight + row * rowHeight,
              width: colWidth,
              height: rowHeight
            },
            action: { type: 'uri', uri: '' }
          })
        }
      }
      
      return areas
    },
    getAreaStyle(area, index) {
      if (!this.imageSize) return {}
      const [imgWidth, imgHeight] = this.imageSize.split('x').map(Number)
      const scaleX = this.canvasWidth / imgWidth
      const scaleY = (this.canvasWidth * imgHeight / imgWidth) / imgHeight

      // 檢查是否有重疊（只在自訂版型時檢查）
      const isOverlapping = this.templateType === 'CUSTOM' && typeof index === 'number' && this.hasOverlap(index)

      return {
        position: 'absolute',
        left: (area.bounds.x * scaleX) + 'px',
        top: (area.bounds.y * scaleY) + 'px',
        width: (area.bounds.width * scaleX) + 'px',
        height: (area.bounds.height * scaleY) + 'px',
        border: isOverlapping ? '2px solid #F56C6C' : '2px solid #409EFF',
        background: isOverlapping ? 'rgba(245, 108, 108, 0.2)' : 'rgba(64, 158, 255, 0.1)',
        cursor: 'pointer',
        transition: 'all 0.2s'
      }
    },
    selectArea(index) {
      this.selectedAreaIndex = index
    },
    removeArea() {
      if (this.selectedAreaIndex !== null) {
        this.areas.splice(this.selectedAreaIndex, 1)
        this.selectedAreaIndex = null
        this.emitChange()
      }
    },
    addCustomArea() {
      if (this.areas.length >= 20) {
        this.$message.warning('最多只能新增 20 個區塊')
        return
      }
      // 建立一個預設大小的區塊（佔畫布的 1/4）
      if (!this.imageSize) {
        this.$message.error('請先選擇圖片尺寸')
        return
      }
      const [imgWidth, imgHeight] = this.imageSize.split('x').map(Number)
      const defaultWidth = Math.floor(imgWidth / 4)
      const defaultHeight = Math.floor(imgHeight / 4)
      
      // 計算新區塊的位置（避免重疊）
      const offsetX = (this.areas.length % 4) * defaultWidth
      const offsetY = Math.floor(this.areas.length / 4) * defaultHeight
      
      const newArea = {
        bounds: {
          x: offsetX,
          y: offsetY,
          width: defaultWidth,
          height: defaultHeight
        },
        action: { type: 'uri', uri: '' }
      }
      
      this.areas.push(newArea)
      this.selectedAreaIndex = this.areas.length - 1
      
      // 發出手動編輯事件，通知父組件將版型設為自訂
      this.$emit('manual-edit')
      
      this.emitChange()
      this.$message.success('已新增區塊')
    },
    updateArea() {
      this.emitChange()
    },
    // 拖曳相關方法
    startDrag(event, index, mode) {
      // 允許拖動，如果不是自訂版型會在 endDrag 時自動切換
      event.preventDefault()
      this.isDragging = true
      this.dragMode = mode
      this.dragIndex = index
      this.dragStartX = event.clientX
      this.dragStartY = event.clientY
      
      // 儲存初始邊界
      const area = this.areas[index]
      this.dragStartBounds = {
        x: area.bounds.x,
        y: area.bounds.y,
        width: area.bounds.width,
        height: area.bounds.height
      }
      
      // 選中該區塊
      this.selectedAreaIndex = index
    },
    onDrag(event) {
      if (!this.isDragging || this.dragIndex === null) return
      
      event.preventDefault()
      
      // 計算滑鼠移動距離（畫布像素）
      const deltaX = event.clientX - this.dragStartX
      const deltaY = event.clientY - this.dragStartY
      
      // 轉換為實際圖片座標
      if (!this.imageSize) return
      const [imgWidth, imgHeight] = this.imageSize.split('x').map(Number)
      const scaleX = imgWidth / this.canvasWidth
      const scaleY = imgHeight / (this.canvasWidth * imgHeight / imgWidth)
      
      const actualDeltaX = Math.round(deltaX * scaleX)
      const actualDeltaY = Math.round(deltaY * scaleY)
      
      const area = this.areas[this.dragIndex]
      const startBounds = this.dragStartBounds
      
      // 根據拖曳模式調整區塊
      let newBounds = { ...area.bounds }
      
      if (this.dragMode === 'move') {
        // 移動整個區塊
        newBounds.x = startBounds.x + actualDeltaX
        newBounds.y = startBounds.y + actualDeltaY
      } else {
        // 調整大小
        if (this.dragMode.includes('n')) {
          // 向上調整
          newBounds.y = startBounds.y + actualDeltaY
          newBounds.height = startBounds.height - actualDeltaY
        }
        if (this.dragMode.includes('s')) {
          // 向下調整
          newBounds.height = startBounds.height + actualDeltaY
        }
        if (this.dragMode.includes('w')) {
          // 向左調整
          newBounds.x = startBounds.x + actualDeltaX
          newBounds.width = startBounds.width - actualDeltaX
        }
        if (this.dragMode.includes('e')) {
          // 向右調整
          newBounds.width = startBounds.width + actualDeltaX
        }
      }
      
      // 應用邊界限制
      newBounds = this.applyBoundaryConstraints(newBounds, imgWidth, imgHeight)
      
      // 更新區塊
      area.bounds = newBounds
      this.$forceUpdate()
    },
    endDrag() {
      if (this.isDragging) {
        const draggedIndex = this.dragIndex
        this.isDragging = false
        this.dragMode = null
        this.dragIndex = null
        
        // 檢查是否有重疊
        if (draggedIndex !== null && this.hasOverlap(draggedIndex)) {
          this.$message.warning('警告：區塊與其他區塊重疊，請調整位置')
        }
        
        // 發出手動編輯事件，通知父組件將版型設為自訂
        this.$emit('manual-edit')
        
        this.emitChange()
      }
    },
    applyBoundaryConstraints(bounds, maxWidth, maxHeight) {
      const minSize = 50 // 最小尺寸
      
      // 確保尺寸不小於最小值
      let { x, y, width, height } = bounds
      width = Math.max(minSize, width)
      height = Math.max(minSize, height)
      
      // 確保不超出畫布
      x = Math.max(0, Math.min(x, maxWidth - width))
      y = Math.max(0, Math.min(y, maxHeight - height))
      
      // 確保右下角不超出畫布
      if (x + width > maxWidth) {
        width = maxWidth - x
      }
      if (y + height > maxHeight) {
        height = maxHeight - y
      }
      
      return { x, y, width, height }
    },
    // 檢查兩個區塊是否重疊
    checkOverlap(bounds1, bounds2) {
      return !(
        bounds1.x + bounds1.width <= bounds2.x ||
        bounds2.x + bounds2.width <= bounds1.x ||
        bounds1.y + bounds1.height <= bounds2.y ||
        bounds2.y + bounds2.height <= bounds1.y
      )
    },
    // 檢查當前區塊是否與其他區塊重疊（用於警告提示）
    hasOverlap(index) {
      if (this.templateType !== 'CUSTOM') return false
      
      const currentBounds = this.areas[index].bounds
      for (let i = 0; i < this.areas.length; i++) {
        if (i !== index) {
          if (this.checkOverlap(currentBounds, this.areas[i].bounds)) {
            return true
          }
        }
      }
      return false
    },
    onActionTypeChange() {
      // 清除其他動作類型的資料
      const type = this.selectedArea.action.type
      
      // 使用 $set 確保響應式，創建新的 action 物件
      const newAction = { type }
      
      // 根據類型初始化預設值
      if (type === 'uri') {
        newAction.uri = ''
      } else if (type === 'message') {
        newAction.text = ''
      } else if (type === 'postback') {
        newAction.data = ''
        newAction.displayText = ''
      } else if (type === 'richmenuswitch') {
        newAction.richMenuAliasId = ''
        newAction.data = ''
      } else if (type === 'datetimepicker') {
        newAction.data = ''
        newAction.mode = 'datetime'
      }
      
      // 使用 $set 確保 Vue 能追蹤到變化
      this.$set(this.selectedArea, 'action', newAction)
      this.updateArea()
    },
    emitChange() {
      this.$emit('input', this.areas)
      this.$emit('change', this.areas)
    },
    getTemplateName(type) {
      const names = {
        'TWO_COLS': '左右兩格',
        'TWO_COLS_HIGH': '左右兩格（高版）',
        'THREE_ROWS': '上下三格',
        'THREE_COLS': '左右三格',
        'FOUR_GRID': '四格（2x2）',
        'SIX_GRID': '六格（2x3）',
        'SIX_GRID_ALT': '六格（3x2）',
        'ALIAS_SWITCH_GRID': '別名切換網格',
        'CUSTOM': '自訂版型'
      }
      return names[type] || type
    },
    getActionIcon(type) {
      const icons = {
        'uri': 'el-icon-link',
        'message': 'el-icon-chat-dot-round',
        'postback': 'el-icon-s-promotion',
        'richmenuswitch': 'el-icon-switch-button',
        'datetimepicker': 'el-icon-date'
      }
      return icons[type] || 'el-icon-question'
    },
    getActionLabel(action) {
      if (action.type === 'uri' && action.uri) {
        return action.uri.substring(0, 20) + (action.uri.length > 20 ? '...' : '')
      }
      if (action.type === 'message' && action.text) {
        return action.text.substring(0, 15) + (action.text.length > 15 ? '...' : '')
      }
      if (action.type === 'postback' && action.data) {
        return action.data.substring(0, 15) + (action.data.length > 15 ? '...' : '')
      }
      return '未設定'
    }
  }
}
</script>

<style scoped>
.rich-menu-editor {
  padding: 10px;
  min-height: 400px;
}

.canvas-wrapper {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 15px;
  background: #fff;
}

.canvas-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.canvas-container {
  margin: 0 auto;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.menu-area {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  cursor: move; /* 所有區塊都可以拖曳（拖動後自動切換為自訂版型） */
}

.menu-area:hover {
  background: rgba(64, 158, 255, 0.2) !important;
  border-color: #66b1ff !important;
}

.menu-area.active {
  background: rgba(103, 194, 58, 0.2) !important;
  border-color: #67C23A !important;
}


/* 拖曳調整大小的手柄 */
.resize-handle {
  position: absolute;
  background: #409EFF;
  border: 1px solid #fff;
  box-shadow: 0 0 3px rgba(0, 0, 0, 0.3);
  z-index: 10;
}

.resize-handle:hover {
  background: #67C23A;
}

/* 上下左右手柄 */
.handle-n, .handle-s {
  width: 40px;
  height: 6px;
  left: 50%;
  transform: translateX(-50%);
  cursor: ns-resize;
}

.handle-n {
  top: -3px;
}

.handle-s {
  bottom: -3px;
}

.handle-w, .handle-e {
  width: 6px;
  height: 40px;
  top: 50%;
  transform: translateY(-50%);
  cursor: ew-resize;
}

.handle-w {
  left: -3px;
}

.handle-e {
  right: -3px;
}

/* 四個角手柄 */
.handle-nw, .handle-ne, .handle-sw, .handle-se {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.handle-nw {
  top: -5px;
  left: -5px;
  cursor: nwse-resize;
}

.handle-ne {
  top: -5px;
  right: -5px;
  cursor: nesw-resize;
}

.handle-sw {
  bottom: -5px;
  left: -5px;
  cursor: nesw-resize;
}

.handle-se {
  bottom: -5px;
  right: -5px;
  cursor: nwse-resize;
}

.area-label {
  text-align: center;
  font-size: 12px;
  color: #333;
}

.area-number {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  background: #409EFF;
  color: white;
  border-radius: 50%;
  margin-bottom: 5px;
  font-weight: bold;
}

.area-action {
  margin-top: 5px;
}

.action-text {
  font-size: 11px;
  display: block;
  margin-top: 3px;
}

.canvas-info {
  margin-top: 15px;
}

.settings-panel {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 15px;
  background: #fff;
  max-height: 600px;
  overflow-y: auto;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  font-weight: bold;
}

.area-settings {
  animation: fadeIn 0.3s;
}

.empty-selection {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-selection i {
  font-size: 48px;
  margin-bottom: 10px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
