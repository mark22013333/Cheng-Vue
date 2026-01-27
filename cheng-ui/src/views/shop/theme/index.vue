<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商城主題配置</span>
          <el-button type="primary" @click="handleSave">儲存設定</el-button>
        </div>
      </template>

      <el-form :model="themeForm" label-width="120px">
        <el-form-item label="選擇預設主題">
          <div class="theme-grid">
            <div
              v-for="(theme, key) in presetThemes"
              :key="key"
              class="theme-card"
              :class="{ active: themeForm.themeKey === key }"
              @click="selectPreset(key)"
            >
              <div class="theme-preview" :style="{ background: theme.headerBg }"></div>
              <div class="theme-info">
                <span class="theme-name">{{ theme.name }}</span>
                <el-icon v-if="themeForm.themeKey === key" class="check-icon"><Check /></el-icon>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-divider>或自訂配色</el-divider>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="主色調">
              <el-color-picker v-model="themeForm.primary" show-alpha />
              <span class="color-value">{{ themeForm.primary }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主色調漸變">
              <el-color-picker v-model="themeForm.primaryEnd" show-alpha />
              <span class="color-value">{{ themeForm.primaryEnd }}</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="強調色">
              <el-color-picker v-model="themeForm.accent" show-alpha />
              <span class="color-value">{{ themeForm.accent }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="頁尾背景">
              <el-color-picker v-model="themeForm.footerBg" show-alpha />
              <span class="color-value">{{ themeForm.footerBg }}</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="卡片背景">
              <el-color-picker v-model="themeForm.cardBg" show-alpha />
              <span class="color-value">{{ themeForm.cardBg }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="頁面背景">
              <el-color-picker v-model="themeForm.bodyBg" show-alpha />
              <span class="color-value">{{ themeForm.bodyBg }}</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="preview-card">
      <template #header>
        <span>預覽效果</span>
      </template>
      <div class="preview-container">
        <div class="preview-header" :style="{ background: previewHeaderBg }">
          <span class="preview-logo">CoolApps 商城</span>
          <div class="preview-nav">
            <span>首頁</span>
            <span>全部商品</span>
            <span>商品分類</span>
          </div>
        </div>
        <div class="preview-body" :style="{ background: themeForm.bodyBg }">
          <div class="preview-product-card" :style="{ background: themeForm.cardBg }">
            <div class="preview-product-img"></div>
            <div class="preview-product-info">
              <div class="preview-product-title">商品名稱範例</div>
              <div class="preview-product-price" :style="{ color: themeForm.accent }">NT$ 999</div>
            </div>
          </div>
        </div>
        <div class="preview-footer" :style="{ background: themeForm.footerBg }">
          <span>© 2025 CoolApps</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { PRESET_THEMES } from '@/store/modules/mallTheme'

const presetThemes = PRESET_THEMES

const themeForm = reactive({
  themeKey: 'purple',
  primary: '#667eea',
  primaryEnd: '#764ba2',
  accent: '#f56c6c',
  footerBg: '#2c3e50',
  cardBg: '#ffffff',
  bodyBg: '#f5f5f5'
})

const previewHeaderBg = computed(() => {
  return `linear-gradient(135deg, ${themeForm.primary} 0%, ${themeForm.primaryEnd} 100%)`
})

function selectPreset(key) {
  const theme = presetThemes[key]
  themeForm.themeKey = key
  themeForm.primary = theme.primary
  themeForm.primaryEnd = theme.primaryEnd
  themeForm.accent = theme.accent
  themeForm.footerBg = theme.footerBg
  themeForm.cardBg = theme.cardBg
  themeForm.bodyBg = theme.bodyBg
}

function handleSave() {
  const themeData = {
    themeKey: themeForm.themeKey,
    primary: themeForm.primary,
    primaryEnd: themeForm.primaryEnd,
    accent: themeForm.accent,
    headerBg: previewHeaderBg.value,
    footerBg: themeForm.footerBg,
    cardBg: themeForm.cardBg,
    bodyBg: themeForm.bodyBg
  }
  
  localStorage.setItem('mall_admin_theme', JSON.stringify(themeData))
  ElMessage.success('主題設定已儲存，將套用至商城前台')
}

function loadSavedTheme() {
  try {
    const saved = localStorage.getItem('mall_admin_theme')
    if (saved) {
      const data = JSON.parse(saved)
      Object.assign(themeForm, data)
    }
  } catch (e) {
    console.error('載入主題設定失敗', e)
  }
}

loadSavedTheme()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.theme-card {
  cursor: pointer;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.theme-card:hover {
  border-color: #c0c4cc;
}

.theme-card.active {
  border-color: #409eff;
}

.theme-preview {
  height: 60px;
}

.theme-info {
  padding: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
}

.theme-name {
  font-size: 12px;
  color: #606266;
}

.check-icon {
  color: #409eff;
}

.color-value {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
  font-family: monospace;
}

.preview-card {
  margin-top: 20px;
}

.preview-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.preview-header {
  padding: 16px 24px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-logo {
  font-size: 18px;
  font-weight: 600;
}

.preview-nav {
  display: flex;
  gap: 24px;
  font-size: 14px;
}

.preview-body {
  padding: 24px;
  min-height: 150px;
  display: flex;
  justify-content: center;
}

.preview-product-card {
  width: 180px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-product-img {
  height: 120px;
  background: linear-gradient(135deg, #e0e0e0, #f5f5f5);
}

.preview-product-info {
  padding: 12px;
}

.preview-product-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
}

.preview-product-price {
  font-size: 18px;
  font-weight: 600;
}

.preview-footer {
  padding: 16px 24px;
  color: #bdc3c7;
  font-size: 13px;
  text-align: center;
}

@media (max-width: 1200px) {
  .theme-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
