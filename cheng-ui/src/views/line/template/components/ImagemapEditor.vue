<template>
  <div class="imagemap-editor">
    <!-- 步驟一：版型選擇 -->
    <div class="editor-section">
      <div class="section-header">
        <span class="section-title">1. 選擇版型</span>
      </div>
      <div class="template-selector">
        <div
          v-for="tpl in templateOptions"
          :key="tpl.value"
          :class="['template-item', { active: selectedTemplate === tpl.value }]"
          @click="selectTemplate(tpl.value)"
        >
          <div class="template-preview">
            <div
              v-for="(area, idx) in tpl.preview"
              :key="idx"
              class="preview-area"
              :style="getPreviewAreaStyle(area)"
            >
              <span>{{ String.fromCharCode(65 + idx) }}</span>
            </div>
          </div>
          <div class="template-name">{{ tpl.label }}</div>
        </div>
      </div>
    </div>

    <!-- 步驟二：上傳背景圖片 -->
    <div class="editor-section">
      <div class="section-header">
        <span class="section-title">2. 背景圖片</span>
        <el-tag v-if="imageInfo.finalSize" size="small" type="success">
          {{ imageInfo.finalSize }}
        </el-tag>
      </div>

      <el-row :gutter="20">
        <el-col :span="16">
          <!-- 圖片上傳區域 -->
          <div v-if="!imageUrl" class="upload-area" v-loading="uploading" element-loading-text="圖片上傳中...">
            <el-upload
              ref="uploadRef"
              drag
              :auto-upload="false"
              :show-file-list="false"
              accept="image/jpeg,image/png,image/jpg"
              :on-change="handleFileChange"
            >
              <el-icon class="el-icon--upload" :size="48"><Upload /></el-icon>
              <div class="el-upload__text">
                拖曳圖片到此處，或 <em>點擊上傳</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支援 JPG、PNG 格式，系統會自動調整至最佳尺寸
                </div>
              </template>
            </el-upload>
          </div>

          <!-- 圖片預覽畫布 -->
          <div v-else class="canvas-wrapper" v-loading="uploading" element-loading-text="圖片上傳中...">
            <div class="canvas-toolbar">
              <el-button size="small" :icon="RefreshRight" @click="handleReupload">
                重新上傳
              </el-button>
              <el-popconfirm title="確定要刪除圖片嗎？" @confirm="handleDeleteImage">
                <template #reference>
                  <el-button size="small" type="danger" :icon="Delete">刪除圖片</el-button>
                </template>
              </el-popconfirm>
            </div>

            <div class="canvas-container" :style="canvasStyle">
              <!-- 熱區 -->
              <div
                v-for="(action, index) in actions"
                :key="index"
                class="action-area"
                :class="{ active: selectedIndex === index, overlapping: isAreaOverlapping(index) }"
                :style="getActionStyle(action)"
                @mousedown.stop="startDrag($event, index, 'move')"
                @click.stop="selectAction(index)"
              >
                <div class="area-label">
                  <span class="area-number">{{ index + 1 }}</span>
                  <el-icon v-if="action.type === 'uri'" class="action-icon"><Link /></el-icon>
                  <el-icon v-else class="action-icon"><ChatDotRound /></el-icon>
                </div>

                <!-- 調整大小的控制點 -->
                <template v-if="selectedIndex === index && selectedTemplate === 'CUSTOM'">
                  <div class="resize-handle handle-nw" @mousedown.stop="startDrag($event, index, 'nw')"></div>
                  <div class="resize-handle handle-ne" @mousedown.stop="startDrag($event, index, 'ne')"></div>
                  <div class="resize-handle handle-sw" @mousedown.stop="startDrag($event, index, 'sw')"></div>
                  <div class="resize-handle handle-se" @mousedown.stop="startDrag($event, index, 'se')"></div>
                </template>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 圖片資訊面板 -->
        <el-col :span="8">
          <div class="image-info-panel">
            <div class="info-title">圖片規格</div>
            <div v-if="imageInfo.originalSize" class="info-item">
              <span class="info-label">原始尺寸：</span>
              <el-tag size="small" type="info">{{ imageInfo.originalSize }}</el-tag>
            </div>
            <div v-if="imageInfo.finalSize" class="info-item">
              <span class="info-label">調整後：</span>
              <el-tag v-if="imageInfo.resized" size="small" type="warning">{{ imageInfo.finalSize }}</el-tag>
              <el-tag v-else size="small" type="success">無需調整</el-tag>
            </div>
            <div class="info-hint">
              <el-icon><InfoFilled /></el-icon>
              <span>LINE Imagemap 圖片寬度固定為 1040px，高度依圖片比例自動調整</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 步驟三：可點選區域設定 -->
    <div class="editor-section">
      <div class="section-header">
        <span class="section-title">3. 可點選區域</span>
        <div class="header-actions">
          <el-button
            v-if="selectedTemplate === 'CUSTOM'"
            size="small"
            :icon="Plus"
            @click="addAction"
            :disabled="actions.length >= 20"
          >
            新增區域
          </el-button>
          <el-tag size="small" type="info">{{ actions.length }}/20</el-tag>
        </div>
      </div>

      <div class="actions-panel">
        <!-- 區域列表 -->
        <div class="actions-list">
          <div
            v-for="(action, index) in actions"
            :key="index"
            :class="['action-item', { active: selectedIndex === index }]"
            @click="selectAction(index)"
          >
            <div class="action-header">
              <div class="action-title">
                <el-icon v-if="action.type === 'uri'" color="#409EFF"><Link /></el-icon>
                <el-icon v-else color="#67C23A"><ChatDotRound /></el-icon>
                <span>按鈕 {{ index + 1 }}</span>
                <el-tag size="small" type="info">{{ getActionTypeName(action.type) }}</el-tag>
              </div>
              <el-button
                v-if="selectedTemplate === 'CUSTOM'"
                type="danger"
                link
                :icon="Delete"
                @click.stop="removeActionAt(index)"
              />
            </div>
            <div class="action-content">
              <span class="content-value">{{ getActionContentPreview(action) }}</span>
            </div>
            <div class="action-position">
              X: {{ action.area.x }}, Y: {{ action.area.y }} |
              W: {{ action.area.width }} × H: {{ action.area.height }}
            </div>
          </div>

          <el-empty v-if="actions.length === 0" description="請先選擇版型或新增區域" :image-size="60" />
        </div>

        <!-- 區域設定面板 -->
        <div class="action-settings" v-if="selectedAction">
          <div class="settings-title">區域設定 - 按鈕 {{ selectedIndex + 1 }}</div>

          <el-form :model="selectedAction" label-width="90px" size="small">
            <el-form-item label="動作類型">
              <el-select v-model="selectedAction.type" style="width: 100%" @change="onActionTypeChange">
                <el-option label="開啟連結 (URI)" value="uri">
                  <el-icon style="margin-right: 8px"><Link /></el-icon>
                  <span>開啟連結 (URI)</span>
                </el-option>
                <el-option label="發送文字 (Message)" value="message">
                  <el-icon style="margin-right: 8px"><ChatDotRound /></el-icon>
                  <span>發送文字 (Message)</span>
                </el-option>
                <el-option label="複製文字 (Clipboard)" value="clipboard">
                  <el-icon style="margin-right: 8px"><DocumentCopy /></el-icon>
                  <span>複製文字 (Clipboard)</span>
                </el-option>
              </el-select>
              <div class="form-tip">複製文字功能需要 LINE 14.0.0 以上版本</div>
            </el-form-item>

            <!-- URI Action -->
            <el-form-item v-if="selectedAction.type === 'uri'" label="連結網址" required>
              <el-input
                v-model="selectedAction.linkUri"
                placeholder="https://example.com"
                @change="emitChange"
              >
                <template #prepend>
                  <el-icon><Link /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- Message Action -->
            <el-form-item v-if="selectedAction.type === 'message'" label="訊息文字" required>
              <el-input
                v-model="selectedAction.text"
                type="textarea"
                :rows="3"
                placeholder="點擊此區域時發送的文字"
                maxlength="300"
                show-word-limit
                @change="emitChange"
              />
            </el-form-item>

            <!-- Clipboard Action -->
            <el-form-item v-if="selectedAction.type === 'clipboard'" label="複製內容" required>
              <el-input
                v-model="selectedAction.clipboardText"
                type="textarea"
                :rows="3"
                placeholder="點擊此區域時複製到剪貼簿的文字"
                maxlength="1000"
                show-word-limit
                @change="emitChange"
              />
            </el-form-item>

            <el-form-item label="標籤">
              <el-input
                v-model="selectedAction.label"
                placeholder="選填，用於無障礙輔助"
                maxlength="50"
                @change="emitChange"
              />
            </el-form-item>

            <el-divider content-position="left">位置與尺寸</el-divider>

            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="X">
                  <el-input-number
                    v-model="selectedAction.area.x"
                    :min="0"
                    :max="baseWidth - selectedAction.area.width"
                    :disabled="selectedTemplate !== 'CUSTOM'"
                    controls-position="right"
                    style="width: 100%"
                    @change="emitChange"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Y">
                  <el-input-number
                    v-model="selectedAction.area.y"
                    :min="0"
                    :max="baseHeight - selectedAction.area.height"
                    :disabled="selectedTemplate !== 'CUSTOM'"
                    controls-position="right"
                    style="width: 100%"
                    @change="emitChange"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="寬度">
                  <el-input-number
                    v-model="selectedAction.area.width"
                    :min="1"
                    :max="baseWidth - selectedAction.area.x"
                    :disabled="selectedTemplate !== 'CUSTOM'"
                    controls-position="right"
                    style="width: 100%"
                    @change="emitChange"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="高度">
                  <el-input-number
                    v-model="selectedAction.area.height"
                    :min="1"
                    :max="baseHeight - selectedAction.area.y"
                    :disabled="selectedTemplate !== 'CUSTOM'"
                    controls-position="right"
                    style="width: 100%"
                    @change="emitChange"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div v-else class="empty-settings">
          <el-icon :size="48" color="#dcdfe6"><Pointer /></el-icon>
          <p>點擊左側區域進行設定</p>
        </div>
      </div>
    </div>

    <!-- 步驟四：基本設定 -->
    <div class="editor-section">
      <div class="section-header">
        <span class="section-title">4. 基本設定</span>
      </div>
      <el-form label-width="100px" size="small">
        <el-form-item label="替代文字" required>
          <el-input
            v-model="altText"
            placeholder="圖片無法顯示時的替代文字（必填）"
            maxlength="400"
            show-word-limit
            @change="emitChange"
          />
          <div class="form-tip">當圖片無法載入時，會顯示此文字</div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Upload, RefreshRight, Link, ChatDotRound, InfoFilled, Pointer, Position, Camera, Picture, DocumentCopy, Calendar } from '@element-plus/icons-vue'
import { uploadImagemapImage, deleteImagemapImage } from '@/api/line/template'
import { getImageUrl } from '@/utils/image'

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'change'])

// 基準寬度（LINE Imagemap 規格）
const BASE_WIDTH = 1040
const CANVAS_WIDTH = 480

// 版型選項
const templateOptions = [
  {
    value: 'A',
    label: '單格',
    preview: [{ x: 0, y: 0, w: 100, h: 100 }],
    areas: [{ x: 0, y: 0, width: 1040, heightRatio: 1 }]
  },
  {
    value: 'AB',
    label: '左右兩格',
    preview: [{ x: 0, y: 0, w: 50, h: 100 }, { x: 50, y: 0, w: 50, h: 100 }],
    areas: [
      { x: 0, y: 0, width: 520, heightRatio: 1 },
      { x: 520, y: 0, width: 520, heightRatio: 1 }
    ]
  },
  {
    value: 'A_BC',
    label: '上一下二',
    preview: [
      { x: 0, y: 0, w: 100, h: 50 },
      { x: 0, y: 50, w: 50, h: 50 },
      { x: 50, y: 50, w: 50, h: 50 }
    ],
    areas: [
      { x: 0, y: 0, width: 1040, heightRatio: 0.5 },
      { x: 0, yRatio: 0.5, width: 520, heightRatio: 0.5 },
      { x: 520, yRatio: 0.5, width: 520, heightRatio: 0.5 }
    ]
  },
  {
    value: 'AB_C',
    label: '上二下一',
    preview: [
      { x: 0, y: 0, w: 50, h: 50 },
      { x: 50, y: 0, w: 50, h: 50 },
      { x: 0, y: 50, w: 100, h: 50 }
    ],
    areas: [
      { x: 0, y: 0, width: 520, heightRatio: 0.5 },
      { x: 520, y: 0, width: 520, heightRatio: 0.5 },
      { x: 0, yRatio: 0.5, width: 1040, heightRatio: 0.5 }
    ]
  },
  {
    value: 'ABC_V',
    label: '上下三格',
    preview: [
      { x: 0, y: 0, w: 100, h: 33.33 },
      { x: 0, y: 33.33, w: 100, h: 33.33 },
      { x: 0, y: 66.66, w: 100, h: 33.34 }
    ],
    areas: [
      { x: 0, y: 0, width: 1040, heightRatio: 0.3333 },
      { x: 0, yRatio: 0.3333, width: 1040, heightRatio: 0.3333 },
      { x: 0, yRatio: 0.6666, width: 1040, heightRatio: 0.3334 }
    ]
  },
  {
    value: 'ABC_H',
    label: '左右三格',
    preview: [
      { x: 0, y: 0, w: 33.33, h: 100 },
      { x: 33.33, y: 0, w: 33.33, h: 100 },
      { x: 66.66, y: 0, w: 33.34, h: 100 }
    ],
    areas: [
      { x: 0, y: 0, width: 347, heightRatio: 1 },
      { x: 347, y: 0, width: 346, heightRatio: 1 },
      { x: 693, y: 0, width: 347, heightRatio: 1 }
    ]
  },
  {
    value: 'AB_CD',
    label: '上二下二',
    preview: [
      { x: 0, y: 0, w: 50, h: 50 },
      { x: 50, y: 0, w: 50, h: 50 },
      { x: 0, y: 50, w: 50, h: 50 },
      { x: 50, y: 50, w: 50, h: 50 }
    ],
    areas: [
      { x: 0, y: 0, width: 520, heightRatio: 0.5 },
      { x: 520, y: 0, width: 520, heightRatio: 0.5 },
      { x: 0, yRatio: 0.5, width: 520, heightRatio: 0.5 },
      { x: 520, yRatio: 0.5, width: 520, heightRatio: 0.5 }
    ]
  },
  {
    value: 'A_BCD',
    label: '上一下三',
    preview: [
      { x: 0, y: 0, w: 100, h: 50 },
      { x: 0, y: 50, w: 33.33, h: 50 },
      { x: 33.33, y: 50, w: 33.33, h: 50 },
      { x: 66.66, y: 50, w: 33.34, h: 50 }
    ],
    areas: [
      { x: 0, y: 0, width: 1040, heightRatio: 0.5 },
      { x: 0, yRatio: 0.5, width: 347, heightRatio: 0.5 },
      { x: 347, yRatio: 0.5, width: 346, heightRatio: 0.5 },
      { x: 693, yRatio: 0.5, width: 347, heightRatio: 0.5 }
    ]
  },
  {
    value: 'ABC_DE',
    label: '上三下二',
    preview: [
      { x: 0, y: 0, w: 33.33, h: 50 },
      { x: 33.33, y: 0, w: 33.33, h: 50 },
      { x: 66.66, y: 0, w: 33.34, h: 50 },
      { x: 0, y: 50, w: 50, h: 50 },
      { x: 50, y: 50, w: 50, h: 50 }
    ],
    areas: [
      { x: 0, y: 0, width: 347, heightRatio: 0.5 },
      { x: 347, y: 0, width: 346, heightRatio: 0.5 },
      { x: 693, y: 0, width: 347, heightRatio: 0.5 },
      { x: 0, yRatio: 0.5, width: 520, heightRatio: 0.5 },
      { x: 520, yRatio: 0.5, width: 520, heightRatio: 0.5 }
    ]
  },
  {
    value: 'AB_CDE',
    label: '上二下三',
    preview: [
      { x: 0, y: 0, w: 50, h: 50 },
      { x: 50, y: 0, w: 50, h: 50 },
      { x: 0, y: 50, w: 33.33, h: 50 },
      { x: 33.33, y: 50, w: 33.33, h: 50 },
      { x: 66.66, y: 50, w: 33.34, h: 50 }
    ],
    areas: [
      { x: 0, y: 0, width: 520, heightRatio: 0.5 },
      { x: 520, y: 0, width: 520, heightRatio: 0.5 },
      { x: 0, yRatio: 0.5, width: 347, heightRatio: 0.5 },
      { x: 347, yRatio: 0.5, width: 346, heightRatio: 0.5 },
      { x: 693, yRatio: 0.5, width: 347, heightRatio: 0.5 }
    ]
  },
  {
    value: 'ABCD_EF',
    label: '上四下二',
    preview: [
      { x: 0, y: 0, w: 25, h: 50 },
      { x: 25, y: 0, w: 25, h: 50 },
      { x: 50, y: 0, w: 25, h: 50 },
      { x: 75, y: 0, w: 25, h: 50 },
      { x: 0, y: 50, w: 50, h: 50 },
      { x: 50, y: 50, w: 50, h: 50 }
    ],
    areas: [
      { x: 0, y: 0, width: 260, heightRatio: 0.5 },
      { x: 260, y: 0, width: 260, heightRatio: 0.5 },
      { x: 520, y: 0, width: 260, heightRatio: 0.5 },
      { x: 780, y: 0, width: 260, heightRatio: 0.5 },
      { x: 0, yRatio: 0.5, width: 520, heightRatio: 0.5 },
      { x: 520, yRatio: 0.5, width: 520, heightRatio: 0.5 }
    ]
  },
  {
    value: 'ABC_DEF',
    label: '六格 (3x2)',
    preview: [
      { x: 0, y: 0, w: 33.33, h: 50 },
      { x: 33.33, y: 0, w: 33.33, h: 50 },
      { x: 66.66, y: 0, w: 33.34, h: 50 },
      { x: 0, y: 50, w: 33.33, h: 50 },
      { x: 33.33, y: 50, w: 33.33, h: 50 },
      { x: 66.66, y: 50, w: 33.34, h: 50 }
    ],
    areas: [
      { x: 0, y: 0, width: 347, heightRatio: 0.5 },
      { x: 347, y: 0, width: 346, heightRatio: 0.5 },
      { x: 693, y: 0, width: 347, heightRatio: 0.5 },
      { x: 0, yRatio: 0.5, width: 347, heightRatio: 0.5 },
      { x: 347, yRatio: 0.5, width: 346, heightRatio: 0.5 },
      { x: 693, yRatio: 0.5, width: 347, heightRatio: 0.5 }
    ]
  },
  {
    value: 'ABCDEF',
    label: '六格 (2x3)',
    preview: [
      { x: 0, y: 0, w: 50, h: 33.33 },
      { x: 50, y: 0, w: 50, h: 33.33 },
      { x: 0, y: 33.33, w: 50, h: 33.33 },
      { x: 50, y: 33.33, w: 50, h: 33.33 },
      { x: 0, y: 66.66, w: 50, h: 33.34 },
      { x: 50, y: 66.66, w: 50, h: 33.34 }
    ],
    areas: [
      { x: 0, y: 0, width: 520, heightRatio: 0.3333 },
      { x: 520, y: 0, width: 520, heightRatio: 0.3333 },
      { x: 0, yRatio: 0.3333, width: 520, heightRatio: 0.3333 },
      { x: 520, yRatio: 0.3333, width: 520, heightRatio: 0.3333 },
      { x: 0, yRatio: 0.6666, width: 520, heightRatio: 0.3334 },
      { x: 520, yRatio: 0.6666, width: 520, heightRatio: 0.3334 }
    ]
  },
  {
    value: 'ABC_DEF_GHI',
    label: '九格 (3x3)',
    preview: [
      { x: 0, y: 0, w: 33.33, h: 33.33 },
      { x: 33.33, y: 0, w: 33.33, h: 33.33 },
      { x: 66.66, y: 0, w: 33.34, h: 33.33 },
      { x: 0, y: 33.33, w: 33.33, h: 33.33 },
      { x: 33.33, y: 33.33, w: 33.33, h: 33.33 },
      { x: 66.66, y: 33.33, w: 33.34, h: 33.33 },
      { x: 0, y: 66.66, w: 33.33, h: 33.34 },
      { x: 33.33, y: 66.66, w: 33.33, h: 33.34 },
      { x: 66.66, y: 66.66, w: 33.34, h: 33.34 }
    ],
    areas: [
      { x: 0, y: 0, width: 347, heightRatio: 0.3333 },
      { x: 347, y: 0, width: 346, heightRatio: 0.3333 },
      { x: 693, y: 0, width: 347, heightRatio: 0.3333 },
      { x: 0, yRatio: 0.3333, width: 347, heightRatio: 0.3333 },
      { x: 347, yRatio: 0.3333, width: 346, heightRatio: 0.3333 },
      { x: 693, yRatio: 0.3333, width: 347, heightRatio: 0.3333 },
      { x: 0, yRatio: 0.6666, width: 347, heightRatio: 0.3334 },
      { x: 347, yRatio: 0.6666, width: 346, heightRatio: 0.3334 },
      { x: 693, yRatio: 0.6666, width: 347, heightRatio: 0.3334 }
    ]
  },
  {
    value: 'CUSTOM',
    label: '自訂',
    preview: [],
    areas: []
  }
]

// State
const uploadRef = ref(null)
const selectedTemplate = ref('AB')
const imageUrl = ref('')
const imageUuid = ref('')
const baseWidth = ref(BASE_WIDTH)
const baseHeight = ref(700)
const altText = ref('圖片訊息')
const actions = ref([])
const selectedIndex = ref(null)
const uploading = ref(false)

// Image Info
const imageInfo = ref({
  originalSize: '',
  finalSize: '',
  resized: false
})

// Dragging
let isDragging = false
let dragMode = null
let dragIndex = null
let dragStartX = 0
let dragStartY = 0
let dragStartArea = null

// Computed
const selectedAction = computed(() => {
  if (selectedIndex.value !== null && actions.value[selectedIndex.value]) {
    return actions.value[selectedIndex.value]
  }
  return null
})

// 計算每個區域是否與其他區域重疊
const overlappingIndices = computed(() => {
  const indices = new Set()
  for (let i = 0; i < actions.value.length; i++) {
    for (let j = i + 1; j < actions.value.length; j++) {
      if (isOverlapping(actions.value[i].area, actions.value[j].area)) {
        indices.add(i)
        indices.add(j)
      }
    }
  }
  return indices
})

const isAreaOverlapping = (index) => {
  return overlappingIndices.value.has(index)
}

const canvasStyle = computed(() => {
  const ratio = baseHeight.value / baseWidth.value
  const height = CANVAS_WIDTH * ratio
  const style = {
    width: CANVAS_WIDTH + 'px',
    height: height + 'px',
    position: 'relative',
    border: '1px solid #e4e7ed',
    borderRadius: '4px'
  }
  if (imageUrl.value) {
    const fullUrl = getImageUrl(imageUrl.value)
    style.backgroundImage = `url(${fullUrl})`
    style.backgroundSize = 'cover'
    style.backgroundPosition = 'center'
    style.backgroundRepeat = 'no-repeat'
  } else {
    style.background = '#f5f7fa'
  }
  return style
})

// Methods
const getPreviewAreaStyle = (area) => {
  return {
    position: 'absolute',
    left: area.x + '%',
    top: area.y + '%',
    width: area.w + '%',
    height: area.h + '%',
    border: '1px solid #409EFF',
    background: 'rgba(64, 158, 255, 0.15)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '10px',
    color: '#409EFF',
    fontWeight: 'bold',
    boxSizing: 'border-box'
  }
}

const selectTemplate = (value) => {
  // 如果已經是自訂模式且有區域，不要重置
  if (selectedTemplate.value === 'CUSTOM' && value === 'CUSTOM' && actions.value.length > 0) {
    return
  }
  selectedTemplate.value = value
  applyTemplate(value)
}

const applyTemplate = (templateValue) => {
  const template = templateOptions.find(t => t.value === templateValue)
  if (!template || templateValue === 'CUSTOM') {
    if (templateValue === 'CUSTOM' && actions.value.length === 0) {
      // 自訂模式下預設新增一個區域
      addAction()
    }
    return
  }

  // 根據版型產生區域
  const newActions = template.areas.map((areaDef, index) => {
    const y = areaDef.yRatio ? Math.round(baseHeight.value * areaDef.yRatio) : (areaDef.y || 0)
    const height = Math.round(baseHeight.value * areaDef.heightRatio)

    return {
      type: 'uri',
      linkUri: '',
      text: '',
      label: '',
      area: {
        x: areaDef.x,
        y: y,
        width: areaDef.width,
        height: height
      }
    }
  })

  actions.value = newActions
  selectedIndex.value = 0
  emitChange()
}

const getActionTypeName = (type) => {
  const map = {
    uri: '連結',
    message: '文字',
    clipboard: '複製'
  }
  return map[type] || type
}

const getActionContentPreview = (action) => {
  switch (action.type) {
    case 'uri':
      return action.linkUri || '(未設定連結)'
    case 'message':
      return action.text || '(未設定訊息)'
    case 'clipboard':
      return action.clipboardText || '(未設定複製內容)'
    default:
      return '(未知動作)'
  }
}

const detectTemplate = (forceDetect = false) => {
  // 如果已經是 CUSTOM 模式且不是強制檢測，保持 CUSTOM 模式
  if (selectedTemplate.value === 'CUSTOM' && !forceDetect) {
    return
  }
  // 簡單判斷版型
  const count = actions.value.length
  if (count === 1) selectedTemplate.value = 'A'
  else if (count === 2) selectedTemplate.value = 'AB'
  else if (count === 3) selectedTemplate.value = 'ABC_V'
  else if (count === 4) selectedTemplate.value = 'ABCD'
  else if (count === 6) selectedTemplate.value = 'ABCDEF'
  else selectedTemplate.value = 'CUSTOM'
}

// Watch props
watch(() => props.modelValue, (newVal) => {
  console.log('[ImagemapEditor] watch modelValue:', newVal)
  if (newVal) {
    if (newVal.baseUrl) {
      // 從 baseUrl 提取 UUID
      const match = newVal.baseUrl.match(/\/imagemap\/([^/]+)$/)
      if (match) {
        imageUuid.value = match[1]
      }
      // 預覽時使用 700px 尺寸的圖片（baseUrl 是目錄，需要加上尺寸才能存取檔案）
      imageUrl.value = newVal.baseUrl + '/700'
    }
    if (newVal.altText) {
      altText.value = newVal.altText
    }
    if (newVal.baseSize) {
      baseWidth.value = newVal.baseSize.width || BASE_WIDTH
      baseHeight.value = newVal.baseSize.height || 700
    }
    if (newVal.actions && Array.isArray(newVal.actions)) {
      // 只在內容不同時才更新（避免 emitChange 觸發的循環更新）
      const newActions = JSON.parse(JSON.stringify(newVal.actions))
      const currentActionsStr = JSON.stringify(actions.value.map(a => a.area))
      const newActionsStr = JSON.stringify(newActions.map(a => a.area))
      if (currentActionsStr !== newActionsStr || actions.value.length !== newActions.length) {
        actions.value = newActions
        // 只在初始載入時根據區域數量判斷版型
        if (selectedTemplate.value === 'AB') {
          detectTemplate(true)
        }
      }
    }
  }
}, { immediate: true, deep: true })

// Lifecycle
onMounted(() => {
  console.log('[ImagemapEditor] onMounted, props.modelValue:', props.modelValue)
  console.log('[ImagemapEditor] onMounted, actions.value.length:', actions.value.length)
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
  // 初始化版型 - 只有在沒有傳入資料時才應用預設版型
  if (actions.value.length === 0 && !props.modelValue?.baseUrl) {
    console.log('[ImagemapEditor] 應用預設版型')
    applyTemplate(selectedTemplate.value)
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
})

// Methods
const handleFileChange = async (uploadFile) => {
  if (!uploadFile.raw) return

  const file = uploadFile.raw
  if (!file.type.startsWith('image/')) {
    ElMessage.error('請上傳圖片檔案')
    return
  }

  uploading.value = true
  try {
    const res = await uploadImagemapImage(file)
    if (res.code === 200 && res.data) {
      imageUrl.value = res.data.previewUrl
      imageUuid.value = res.data.uuid
      baseWidth.value = res.data.baseWidth || BASE_WIDTH
      baseHeight.value = res.data.baseHeight || 700

      // 更新圖片資訊
      imageInfo.value = {
        originalSize: res.data.originalSize || '',
        finalSize: `${res.data.baseWidth}x${res.data.baseHeight}`,
        resized: res.data.resized || false
      }

      ElMessage.success(`圖片上傳成功${res.data.resized ? '（已自動調整尺寸）' : ''}`)

      // 重新套用版型以更新區域高度
      applyTemplate(selectedTemplate.value)
    } else {
      ElMessage.error(res.msg || '上傳失敗')
    }
  } catch (e) {
    ElMessage.error('上傳失敗：' + (e.message || '未知錯誤'))
  } finally {
    uploading.value = false
  }
}

const handleReupload = () => {
  uploadRef.value?.$el.querySelector('input')?.click()
}

const handleDeleteImage = async () => {
  if (imageUuid.value) {
    try {
      await deleteImagemapImage(imageUuid.value)
    } catch (e) {
      console.warn('刪除圖片失敗', e)
    }
  }
  imageUrl.value = ''
  imageUuid.value = ''
  imageInfo.value = { originalSize: '', finalSize: '', resized: false }
  emitChange()
}

const addAction = () => {
  if (actions.value.length >= 20) {
    ElMessage.warning('最多只能新增 20 個區域')
    return
  }

  const defaultWidth = Math.floor(baseWidth.value / 4)
  const defaultHeight = Math.floor(baseHeight.value / 4)
  const offsetX = (actions.value.length % 4) * defaultWidth
  const offsetY = Math.floor(actions.value.length / 4) * defaultHeight

  actions.value.push({
    type: 'uri',
    linkUri: '',
    text: '',
    label: '',
    area: {
      x: Math.min(offsetX, baseWidth.value - defaultWidth),
      y: Math.min(offsetY, baseHeight.value - defaultHeight),
      width: defaultWidth,
      height: defaultHeight
    }
  })
  selectedIndex.value = actions.value.length - 1
  emitChange()
}

const removeActionAt = (index) => {
  actions.value.splice(index, 1)
  if (selectedIndex.value === index) {
    selectedIndex.value = null
  } else if (selectedIndex.value > index) {
    selectedIndex.value--
  }
  emitChange()
}

const selectAction = (index) => {
  selectedIndex.value = index
}

const onActionTypeChange = () => {
  if (selectedAction.value) {
    selectedAction.value.linkUri = ''
    selectedAction.value.text = ''
    emitChange()
  }
}

const getActionStyle = (action) => {
  const scaleX = CANVAS_WIDTH / baseWidth.value
  const scaleY = (CANVAS_WIDTH * baseHeight.value / baseWidth.value) / baseHeight.value

  return {
    position: 'absolute',
    left: (action.area.x * scaleX) + 'px',
    top: (action.area.y * scaleY) + 'px',
    width: (action.area.width * scaleX) + 'px',
    height: (action.area.height * scaleY) + 'px',
    border: '2px solid #409EFF',
    backgroundColor: 'rgba(64, 158, 255, 0.2)',
    cursor: selectedTemplate.value === 'CUSTOM' ? 'move' : 'pointer',
    boxSizing: 'border-box',
    transition: 'border-color 0.2s, background-color 0.2s'
  }
}

// Dragging (only for CUSTOM template)
const startDrag = (event, index, mode) => {
  if (selectedTemplate.value !== 'CUSTOM' && mode !== 'move') return
  if (selectedTemplate.value !== 'CUSTOM') {
    selectAction(index)
    return
  }

  event.preventDefault()
  isDragging = true
  dragMode = mode
  dragIndex = index
  dragStartX = event.clientX
  dragStartY = event.clientY
  dragStartArea = { ...actions.value[index].area }
  selectedIndex.value = index
}

const onDrag = (event) => {
  if (!isDragging || dragIndex === null || selectedTemplate.value !== 'CUSTOM') return
  event.preventDefault()

  const scaleX = baseWidth.value / CANVAS_WIDTH
  const scaleY = baseHeight.value / (CANVAS_WIDTH * baseHeight.value / baseWidth.value)

  const deltaX = Math.round((event.clientX - dragStartX) * scaleX)
  const deltaY = Math.round((event.clientY - dragStartY) * scaleY)

  const action = actions.value[dragIndex]
  let area = { ...dragStartArea }

  if (dragMode === 'move') {
    area.x = Math.max(0, Math.min(area.x + deltaX, baseWidth.value - area.width))
    area.y = Math.max(0, Math.min(area.y + deltaY, baseHeight.value - area.height))
  } else {
    if (dragMode.includes('e')) {
      area.width = Math.max(10, Math.min(area.width + deltaX, baseWidth.value - area.x))
    }
    if (dragMode.includes('s')) {
      area.height = Math.max(10, Math.min(area.height + deltaY, baseHeight.value - area.y))
    }
    if (dragMode.includes('w')) {
      const newX = Math.max(0, area.x + deltaX)
      const newWidth = area.width - (newX - area.x)
      if (newWidth >= 10) {
        area.x = newX
        area.width = newWidth
      }
    }
    if (dragMode.includes('n')) {
      const newY = Math.max(0, area.y + deltaY)
      const newHeight = area.height - (newY - area.y)
      if (newHeight >= 10) {
        area.y = newY
        area.height = newHeight
      }
    }
  }

  action.area = area
}

const endDrag = () => {
  if (isDragging) {
    isDragging = false
    dragIndex = null
    emitChange()
  }
}

const emitChange = () => {
  const baseUrl = imageUuid.value ? `/profile/imagemap/${imageUuid.value}` : ''

  const imagemapData = {
    baseUrl,
    altText: altText.value,
    baseSize: {
      width: baseWidth.value,
      height: baseHeight.value
    },
    actions: actions.value.map(a => {
      const action = {
        type: a.type,
        area: { ...a.area }
      }

      // 根據動作類型添加對應欄位
      if (a.label) action.label = a.label

      // 根據動作類型添加對應欄位
      switch (a.type) {
        case 'uri':
          if (a.linkUri) action.linkUri = a.linkUri
          break
        case 'message':
          if (a.text) action.text = a.text
          break
        case 'clipboard':
          if (a.clipboardText) action.clipboardText = a.clipboardText
          break
      }

      return action
    })
  }

  emit('update:modelValue', imagemapData)
  emit('change', imagemapData)
}

/**
 * 檢測兩個矩形是否重疊（包含 2% 的寬容值）
 */
const isOverlapping = (rect1, rect2) => {
  // 1. 計算寬容值 buffer
  // 取兩者中較小的邊長來計算 2%，這樣可以避免小物件對上大物件時寬容值過大
  // 如果你希望寬容值固定，也可以直接寫死一個數值（例如 5px）
  const toleranceX = Math.min(rect1.width, rect2.width) * 0.02;
  const toleranceY = Math.min(rect1.height, rect2.height) * 0.02;

  // 2. 進行判斷
  // 邏輯：原本是 (右邊界 < 對方左邊界) 表示沒重疊
  // 現在我們把右邊界「扣掉」寬容值，讓它更難碰到對方，藉此產生「允許稍微重疊」的效果
  return !(
    rect1.x + rect1.width - toleranceX < rect2.x + 1 || // R1 在 R2 左側 (但在寬容值內不算)
    rect2.x + rect2.width - toleranceX < rect1.x + 1 || // R2 在 R1 左側
    rect1.y + rect1.height - toleranceY < rect2.y + 1 || // R1 在 R2 上方
    rect2.y + rect2.height - toleranceY < rect1.y + 1    // R2 在 R1 上方
  );
}

/**
 * 檢測所有區域是否有重疊
 * @returns {Object} { hasOverlap: boolean, overlappingPairs: Array<[number, number]> }
 */
const checkOverlap = () => {
  const overlappingPairs = []

  for (let i = 0; i < actions.value.length; i++) {
    for (let j = i + 1; j < actions.value.length; j++) {
      if (isOverlapping(actions.value[i].area, actions.value[j].area)) {
        overlappingPairs.push([i + 1, j + 1])
      }
    }
  }

  return {
    hasOverlap: overlappingPairs.length > 0,
    overlappingPairs
  }
}

/**
 * 驗證圖文訊息設定
 * @returns {Object} { valid: boolean, message: string }
 */
const validate = () => {
  if (!imageUrl.value) {
    return { valid: false, message: '請上傳圖片' }
  }

  if (!altText.value) {
    return { valid: false, message: '請輸入替代文字' }
  }

  if (actions.value.length === 0) {
    return { valid: false, message: '請至少設定一個可點選區域' }
  }

  // 檢測重疊
  const { hasOverlap, overlappingPairs } = checkOverlap()
  if (hasOverlap) {
    const pairsStr = overlappingPairs.map(p => `按鈕${p[0]}與按鈕${p[1]}`).join('、')
    return { valid: false, message: `區域重疊：${pairsStr}，請調整區域位置或大小` }
  }

  // 檢查每個區域的動作設定
  for (let i = 0; i < actions.value.length; i++) {
    const action = actions.value[i]
    if (action.type === 'uri' && !action.linkUri) {
      return { valid: false, message: `按鈕 ${i + 1} 未設定連結網址` }
    }
    if (action.type === 'message' && !action.text) {
      return { valid: false, message: `按鈕 ${i + 1} 未設定發送文字` }
    }
    if (action.type === 'clipboard' && !action.clipboardText) {
      return { valid: false, message: `按鈕 ${i + 1} 未設定複製內容` }
    }
  }

  return { valid: true, message: '' }
}

// 暴露方法給父組件
defineExpose({
  validate,
  checkOverlap
})
</script>

<style scoped lang="scss">
.imagemap-editor {
  background: #fff;
}

.editor-section {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  background: #fff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;

  .section-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

/* 版型選擇器 */
.template-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.template-item {
  width: 80px;
  cursor: pointer;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 8px;
  transition: all 0.2s;

  &:hover {
    border-color: #c0c4cc;
  }

  &.active {
    border-color: #409EFF;
    background: #ecf5ff;
  }

  .template-preview {
    position: relative;
    width: 100%;
    height: 50px;
    background: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 6px;
    overflow: hidden;
  }

  .template-name {
    text-align: center;
    font-size: 12px;
    color: #606266;
  }

  .preview-area {
    position: absolute;
    background: #409EFF;
    opacity: 0.6;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #fff;
    font-weight: bold;
  }
}

/* 圖片上傳區域 */
.upload-area {
  padding: 20px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  background: #fafafa;
}

.canvas-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.canvas-toolbar {
  display: flex;
  gap: 8px;
}

.canvas-container {
  margin: 0 auto;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 圖片資訊面板 */
.image-info-panel {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  height: 100%;

  .info-title {
    font-weight: 600;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e4e7ed;
  }

  .info-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;

    .info-label {
      color: #606266;
      font-size: 13px;
      margin-right: 8px;
    }
  }

  .info-hint {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    margin-top: 16px;
    padding: 10px;
    background: #fff;
    border-radius: 4px;
    font-size: 12px;
    color: #909399;
    line-height: 1.5;

    .el-icon {
      margin-top: 2px;
      flex-shrink: 0;
    }
  }
}

/* 區域面板 */
.actions-panel {
  display: flex;
  gap: 16px;
  min-height: 300px;
}

.actions-list {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  max-height: 400px;
  overflow-y: auto;
  background: #fafafa;
}

.action-item {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    border-color: #c0c4cc;
  }

  &.active {
    border-color: #409EFF;
    background: #ecf5ff;
  }

  .action-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
  }

  .action-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 500;
  }

  .action-content {
    font-size: 13px;
    color: #606266;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    .content-value {
      color: #909399;
    }
  }

  .action-position {
    font-size: 11px;
    color: #909399;
  }
}

.action-settings {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fff;

  .settings-title {
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }
}

.empty-settings {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
  color: #909399;

  p {
    margin-top: 12px;
  }
}

/* 畫布上的熱區 */
.action-area {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  transition: border-color 0.2s, background-color 0.2s;

  &.active {
    border-color: #67C23A !important;
    background-color: rgba(103, 194, 58, 0.25) !important;
  }
}

.area-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
  gap: 2px;

  .area-number {
    width: 22px;
    height: 22px;
    background: #409EFF;
    color: white;
    border-radius: 50%;
    text-align: center;
    line-height: 22px;
    font-weight: bold;
    font-size: 11px;
  }

  .action-icon {
    color: #fff;
    font-size: 12px;
  }
}

.action-area.active .area-label .area-number {
  background: #67C23A;
}

.action-area.overlapping {
  border-color: #F56C6C !important;
  background-color: rgba(245, 108, 108, 0.3) !important;

  .area-label .area-number {
    background: #F56C6C;
  }
}

.action-area.overlapping.active {
  border-color: #F56C6C !important;
  background-color: rgba(245, 108, 108, 0.4) !important;
}

.resize-handle {
  position: absolute;
  background: #fff;
  border: 2px solid #67C23A;
  width: 10px;
  height: 10px;
  border-radius: 2px;
  z-index: 10;
}

.handle-nw { top: -5px; left: -5px; cursor: nw-resize; }
.handle-ne { top: -5px; right: -5px; cursor: ne-resize; }
.handle-sw { bottom: -5px; left: -5px; cursor: sw-resize; }
.handle-se { bottom: -5px; right: -5px; cursor: se-resize; }

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
