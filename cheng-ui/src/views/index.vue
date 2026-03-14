<template>
  <div class="dashboard">
    <!-- Hero Greeting -->
    <section class="hero" :class="greetingTimeClass">
      <div class="hero-inner">
        <div class="hero-left">
          <h1 class="hero-greeting">{{ greetingEmoji }} {{ greetingText }}，{{ userName }}</h1>
          <p class="hero-date">{{ currentDateString }}</p>
        </div>
        <div class="hero-right">
          <span class="hero-badge">{{ version }}</span>
          <span class="hero-separator">·</span>
          <span class="hero-uptime">穩定運行 <strong>{{ daysSinceLaunch }}</strong> 天</span>
        </div>
      </div>
    </section>

    <!-- Stats Grid -->
    <section class="stats-grid">
      <div v-for="(stat, index) in statCards" :key="index" class="stat-card" :class="'stat-' + stat.theme">
        <div class="stat-icon-wrap">
          <el-icon :size="20"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-value" :class="{ 'no-perm': stat.noPermission }">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </section>

    <!-- Main Content -->
    <section class="content-grid">
      <!-- Announcements -->
      <div class="panel announcements-panel" v-if="announcements.length > 0">
        <div class="panel-header">
          <div class="panel-title">
            <el-icon :size="18"><Bell /></el-icon>
            <span>系統公告</span>
            <el-tag type="warning" size="small" round>{{ announcements.length }}</el-tag>
          </div>
          <el-tag v-if="totalAnnouncementPages > 1" type="info" size="small">
            {{ announcementCurrentPage }} / {{ totalAnnouncementPages }}
          </el-tag>
        </div>
        <div class="announcement-list">
          <div
            v-for="notice in paginatedAnnouncements"
            :key="notice.noticeId"
            class="announcement-item"
            @click="showAnnouncementDetail(notice)"
          >
            <div class="announcement-left">
              <span class="announcement-dot" :class="{
                'dot-new': isNewAnnouncement(notice),
                'dot-recent': isRecentAnnouncement(notice)
              }"></span>
              <span class="announcement-title">{{ notice.noticeTitle }}</span>
              <el-tag v-if="isNewAnnouncement(notice)" type="danger" size="small" effect="dark" round>最新</el-tag>
              <el-tag v-else-if="isRecentAnnouncement(notice)" type="warning" size="small" round>近期</el-tag>
            </div>
            <span class="announcement-time">{{ notice.createTime }}</span>
          </div>
        </div>
        <div class="panel-footer" v-if="totalAnnouncementPages > 1">
          <el-pagination
            small
            background
            layout="prev, pager, next"
            :total="announcements.length"
            :page-size="announcementPageSize"
            v-model:current-page="announcementCurrentPage"
          />
        </div>
      </div>

      <!-- Info Panel -->
      <div class="panel info-panel">
        <div class="panel-header">
          <div class="panel-title">
            <el-icon :size="18"><InfoFilled /></el-icon>
            <span>專案資訊</span>
          </div>
        </div>
        <div class="info-list">
          <div class="info-row">
            <span class="info-label">開發者</span>
            <span class="info-value">Mark Cheng</span>
          </div>
          <div class="info-row">
            <span class="info-label">地點</span>
            <span class="info-value">台灣 (Taiwan)</span>
          </div>
          <div class="info-row">
            <span class="info-label">發布日</span>
            <span class="info-value">2025-09-22</span>
          </div>
          <div class="info-row">
            <span class="info-label">版本數</span>
            <span class="info-value highlight">{{ totalVersions }}</span>
          </div>
        </div>
        <el-button class="info-btn" type="primary" plain round @click="goTarget('https://mark22013333.github.io/')">
          <el-icon><Promotion /></el-icon>造訪部落格
        </el-button>
      </div>
    </section>

    <!-- Changelog -->
    <section class="panel changelog-panel">
      <div class="panel-header">
        <div class="panel-title">
          <el-icon :size="18"><Document /></el-icon>
          <span>系統更新日誌</span>
          <el-tag type="info" size="small" round>{{ versionLogs.length }} 個版本</el-tag>
        </div>
        <el-button link type="primary" @click="showAllLogs = !showAllLogs">
          {{ showAllLogs ? '收合' : '展開全部' }}
          <el-icon class="expand-icon" :class="{ 'is-expanded': showAllLogs }"><ArrowDown /></el-icon>
        </el-button>
      </div>
      <div class="changelog-body">
        <el-timeline>
          <el-timeline-item
            v-for="(log, index) in displayedLogs"
            :key="index"
            :timestamp="log.date"
            :type="index === 0 ? 'success' : 'primary'"
            :icon="log.icon"
            :color="log.color"
            placement="top"
          >
            <div class="tl-card">
              <div class="tl-header">
                <span class="tl-version">{{ log.version }}</span>
                <el-tag :type="log.tagType" effect="dark" size="small" round>{{ log.tag }}</el-tag>
              </div>
              <ul class="tl-list">
                <li v-for="(item, idx) in log.items" :key="idx">{{ item }}</li>
              </ul>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </section>

    <!-- Announcement Detail Dialog -->
    <el-dialog
      title="公告詳情"
      v-model="announcementDialogVisible"
      width="800px"
      class="announcement-dialog"
      @opened="setupAnnouncementImagePreview"
    >
      <div class="dialog-body">
        <div class="dialog-title-row">
          <h3>{{ currentAnnouncement.noticeTitle }}</h3>
          <el-tag v-if="isNewAnnouncement(currentAnnouncement)" type="danger" size="small" effect="dark" round>最新</el-tag>
          <el-tag v-else-if="isRecentAnnouncement(currentAnnouncement)" type="warning" size="small" round>近期</el-tag>
        </div>
        <div class="dialog-content" v-html="currentAnnouncement.noticeContent" ref="announcementBodyRef"></div>
        <div class="dialog-meta">
          <el-icon :size="14"><Timer /></el-icon>
          <span>{{ currentAnnouncement.createTime }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- Image Preview Dialog -->
    <el-dialog v-model="announcementImagePreviewVisible" width="90%" top="5vh">
      <img :src="announcementPreviewImageUrl" style="width: 100%; display: block;" alt="圖片預覽" />
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import { ref, computed, onMounted, onActivated, watch, nextTick, getCurrentInstance } from 'vue'
import useUserStore from '@/store/modules/user'
import request from '@/utils/request'
import { versionLogs, getLatestVersion } from '@/data/changelog'
import { checkPermi } from '@/utils/permission'
import { MONITOR_ONLINE_LIST, SYSTEM_USER_LIST } from '@/constants/permissions'
import {
  DataLine, Timer, User, UserFilled,
  Bell, InfoFilled, Document, Promotion, ArrowDown
} from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const route = useRoute()

// ====== State ======
const launchDate = new Date('2025-09-22')
const onlineUsers = ref(0)
const totalUsers = ref(0)
const announcements = ref([])
const announcementDialogVisible = ref(false)
const currentAnnouncement = ref({})
const announcementCurrentPage = ref(1)
const announcementPageSize = ref(10)
const announcementBodyRef = ref(null)
const announcementImagePreviewVisible = ref(false)
const announcementPreviewImageUrl = ref('')
const showAllLogs = ref(false)

// ====== Computed ======
const version = computed(() => getLatestVersion())
const totalVersions = computed(() => versionLogs.length)

const userName = computed(() => userStore.nickName || userStore.name || '管理員')

const greetingText = computed(() => {
  const h = new Date().getHours()
  if (h >= 5 && h < 12) return '早安'
  if (h >= 12 && h < 18) return '午安'
  return '晚安'
})

const greetingEmoji = computed(() => {
  const h = new Date().getHours()
  if (h >= 5 && h < 12) return '☀️'
  if (h >= 12 && h < 18) return '🌤️'
  return '🌙'
})

const greetingTimeClass = computed(() => {
  const h = new Date().getHours()
  if (h >= 5 && h < 12) return 'morning'
  if (h >= 12 && h < 18) return 'afternoon'
  return 'evening'
})

const currentDateString = computed(() => {
  const now = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${weekDays[now.getDay()]}`
})

const daysSinceLaunch = computed(() => {
  return Math.ceil(Math.abs(new Date() - launchDate) / 86400000)
})

const statCards = computed(() => {
  const hasOnline = checkPermi([MONITOR_ONLINE_LIST])
  const hasUser = checkPermi([SYSTEM_USER_LIST])
  return [
    { label: '版本迭代', value: totalVersions.value, icon: DataLine, theme: 'blue' },
    { label: '執行天數', value: daysSinceLaunch.value, icon: Timer, theme: 'green' },
    { label: '線上人數', value: hasOnline ? onlineUsers.value : '—', icon: User, theme: 'amber', noPermission: !hasOnline },
    { label: '註冊帳號', value: hasUser ? totalUsers.value : '—', icon: UserFilled, theme: 'violet', noPermission: !hasUser }
  ]
})

const paginatedAnnouncements = computed(() => {
  const start = (announcementCurrentPage.value - 1) * announcementPageSize.value
  return announcements.value.slice(start, start + announcementPageSize.value)
})

const totalAnnouncementPages = computed(() => {
  return Math.ceil(announcements.value.length / announcementPageSize.value)
})

const displayedLogs = computed(() => {
  return showAllLogs.value ? versionLogs : versionLogs.slice(0, 5)
})

// ====== Methods ======
function goTarget(href) {
  window.open(href, '_blank')
}

function getOnlineUsers() {
  if (checkPermi([MONITOR_ONLINE_LIST])) {
    request({ url: '/monitor/online/list', method: 'get' })
      .then(res => { onlineUsers.value = res.total || 0 })
      .catch(() => { onlineUsers.value = 0 })
  }
}

function getTotalUsers() {
  if (checkPermi([SYSTEM_USER_LIST])) {
    request({ url: '/system/user/list', method: 'get', params: { pageNum: 1, pageSize: 1 } })
      .then(res => { totalUsers.value = res.total || 0 })
      .catch(() => { totalUsers.value = 0 })
  }
}

function getAnnouncements() {
  request({ url: '/system/notice/announcements', method: 'get' })
    .then(res => { announcements.value = res.data || [] })
    .catch(() => { announcements.value = [] })
}

function showAnnouncementDetail(notice) {
  currentAnnouncement.value = notice
  announcementDialogVisible.value = true
}

function isNewAnnouncement(notice) {
  if (!notice.createTime) return false
  return (new Date() - new Date(notice.createTime)) / 86400000 <= 3
}

function isRecentAnnouncement(notice) {
  if (!notice.createTime) return false
  const days = (new Date() - new Date(notice.createTime)) / 86400000
  return days > 3 && days <= 7
}

function setupAnnouncementImagePreview() {
  nextTick(() => {
    if (announcementBodyRef.value) {
      announcementBodyRef.value.querySelectorAll('img').forEach(img => {
        img.style.cursor = 'pointer'
        img.onclick = () => {
          announcementPreviewImageUrl.value = img.src
          announcementImagePreviewVisible.value = true
        }
      })
    }
  })
}

// ====== Lifecycle ======
onMounted(() => {
  getOnlineUsers()
  getTotalUsers()
  getAnnouncements()
})

onActivated(() => {
  getAnnouncements()
})

watch(() => route.path, (path) => {
  if (path === '/index') {
    getAnnouncements()
  }
})
</script>

<style scoped lang="scss">
/* ====== Design Tokens ====== */
.dashboard {
  --d-bg: #f3f4f6;
  --d-surface: #ffffff;
  --d-surface-hover: #f9fafb;
  --d-border: rgba(0, 0, 0, 0.06);
  --d-text-1: #111827;
  --d-text-2: #6b7280;
  --d-text-3: #9ca3af;
  --d-radius: 16px;
  --d-radius-sm: 12px;
  --d-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  --d-shadow-hover: 0 4px 12px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04);
  --d-transition: 0.2s ease;

  /* Hero gradients */
  --hero-morning: linear-gradient(135deg, #FFF7ED 0%, #FFFBEB 50%, #FEF9C3 100%);
  --hero-afternoon: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 50%, #BFDBFE 100%);
  --hero-evening: linear-gradient(135deg, #EDE9FE 0%, #E0E7FF 50%, #C7D2FE 100%);

  /* Stat themes */
  --stat-blue-bg: #EFF6FF;
  --stat-blue-icon: #3B82F6;
  --stat-green-bg: #ECFDF5;
  --stat-green-icon: #10B981;
  --stat-amber-bg: #FFFBEB;
  --stat-amber-icon: #F59E0B;
  --stat-violet-bg: #F5F3FF;
  --stat-violet-icon: #8B5CF6;

  padding: 24px;
  min-height: 100vh;
  background: var(--d-bg);
  display: flex;
  flex-direction: column;
  gap: 20px;

  /* ====== Dark Mode ====== */
  html.dark & {
    --d-bg: var(--el-bg-color, #141414);
    --d-surface: var(--el-bg-color-overlay, #1d1e1f);
    --d-surface-hover: #252627;
    --d-border: rgba(255, 255, 255, 0.08);
    --d-text-1: #f0f0f0;
    --d-text-2: #a0a0a0;
    --d-text-3: #666666;
    --d-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
    --d-shadow-hover: 0 4px 12px rgba(0, 0, 0, 0.4);

    --hero-morning: linear-gradient(135deg, #1C1208 0%, #1A150A 100%);
    --hero-afternoon: linear-gradient(135deg, #0C1220 0%, #0A1628 100%);
    --hero-evening: linear-gradient(135deg, #130D1E 0%, #0E0A1A 100%);

    --stat-blue-bg: rgba(59, 130, 246, 0.1);
    --stat-green-bg: rgba(16, 185, 129, 0.1);
    --stat-amber-bg: rgba(245, 158, 11, 0.1);
    --stat-violet-bg: rgba(139, 92, 246, 0.1);
  }
}

/* ====== Hero Section ====== */
.hero {
  border-radius: var(--d-radius);
  padding: 28px 32px;
  transition: background var(--d-transition);

  &.morning { background: var(--hero-morning); }
  &.afternoon { background: var(--hero-afternoon); }
  &.evening { background: var(--hero-evening); }
}

.hero-inner {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.hero-greeting {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  color: var(--d-text-1);
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.hero-date {
  margin: 0;
  font-size: 14px;
  color: var(--d-text-2);
  font-weight: 500;
}

.hero-right {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--d-text-2);
  flex-shrink: 0;
}

.hero-badge {
  display: inline-flex;
  padding: 3px 10px;
  border-radius: 20px;
  background: var(--el-color-primary, #409EFF);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.hero-separator {
  color: var(--d-text-3);
}

.hero-uptime {
  strong {
    color: var(--d-text-1);
    font-weight: 700;
  }
}

/* ====== Stats Grid ====== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: var(--d-surface);
  border-radius: var(--d-radius);
  padding: 24px 20px;
  text-align: center;
  box-shadow: var(--d-shadow);
  transition: transform var(--d-transition), box-shadow var(--d-transition);
  cursor: default;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--d-shadow-hover);
  }

  html.dark & {
    box-shadow: none;
    border: 1px solid var(--d-border);
  }
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  color: #fff;
}

.stat-blue .stat-icon-wrap { background: var(--stat-blue-icon); }
.stat-green .stat-icon-wrap { background: var(--stat-green-icon); }
.stat-amber .stat-icon-wrap { background: var(--stat-amber-icon); }
.stat-violet .stat-icon-wrap { background: var(--stat-violet-icon); }

.stat-blue { background: var(--stat-blue-bg); }
.stat-green { background: var(--stat-green-bg); }
.stat-amber { background: var(--stat-amber-bg); }
.stat-violet { background: var(--stat-violet-bg); }

.stat-value {
  font-size: 30px;
  font-weight: 800;
  color: var(--d-text-1);
  line-height: 1.2;
  margin-bottom: 4px;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;

  &.no-perm {
    font-size: 20px;
    font-weight: 500;
    color: var(--d-text-3);
  }
}

.stat-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--d-text-2);
}

/* ====== Content Grid ====== */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
}

/* ====== Panels ====== */
.panel {
  background: var(--d-surface);
  border-radius: var(--d-radius);
  box-shadow: var(--d-shadow);
  overflow: hidden;

  html.dark & {
    box-shadow: none;
    border: 1px solid var(--d-border);
  }
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid var(--d-border);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--d-text-1);
}

.panel-footer {
  display: flex;
  justify-content: center;
  padding: 14px;
  border-top: 1px solid var(--d-border);
}

/* ====== Announcements ====== */
.announcement-list {
  padding: 4px 0;
}

.announcement-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 24px;
  cursor: pointer;
  transition: background var(--d-transition);

  &:hover {
    background: var(--d-surface-hover);
  }
}

.announcement-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.announcement-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
  background: var(--d-text-3);

  &.dot-new { background: #EF4444; }
  &.dot-recent { background: #F59E0B; }
}

.announcement-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--d-text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}

.announcement-time {
  font-size: 12px;
  color: var(--d-text-3);
  white-space: nowrap;
  flex-shrink: 0;
}

/* ====== Info Panel ====== */
.info-panel {
  display: flex;
  flex-direction: column;
}

.info-list {
  padding: 8px 24px;
  flex: 1;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;

  & + & {
    border-top: 1px solid var(--d-border);
  }
}

.info-label {
  font-size: 13px;
  color: var(--d-text-2);
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: var(--d-text-1);
  font-weight: 600;

  &.highlight {
    color: var(--el-color-primary, #409EFF);
  }
}

.info-btn {
  margin: 12px 24px 20px;
}

/* ====== Changelog ====== */
.changelog-panel {
  .panel-header {
    .el-button {
      font-size: 13px;
    }
  }
}

.expand-icon {
  transition: transform 0.25s ease;
  margin-left: 2px;

  &.is-expanded {
    transform: rotate(180deg);
  }
}

.changelog-body {
  padding: 20px 24px 8px;
}

.tl-card {
  background: var(--d-surface-hover);
  border-radius: var(--d-radius-sm);
  padding: 16px 20px;
  border: 1px solid var(--d-border);
  transition: border-color var(--d-transition);

  &:hover {
    border-color: var(--el-color-primary-light-5, #a0cfff);
  }
}

.tl-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.tl-version {
  font-size: 16px;
  font-weight: 700;
  color: var(--d-text-1);
  letter-spacing: -0.01em;
}

.tl-list {
  padding-left: 18px;
  margin: 0;

  li {
    font-size: 13px;
    line-height: 1.7;
    color: var(--d-text-2);
    margin-bottom: 2px;
  }
}

/* ====== Announcement Dialog ====== */
.dialog-body {
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.dialog-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;

  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: var(--d-text-1);
    flex: 1;
    min-width: 0;
  }
}

.dialog-content {
  font-size: 14px;
  color: var(--d-text-2);
  line-height: 1.8;
  min-height: 100px;
  margin-bottom: 16px;

  :deep(img) {
    max-width: 100% !important;
    width: 100% !important;
    height: auto !important;
    display: block !important;
    margin: 10px 0;
    border-radius: 8px;
    object-fit: contain;
  }

  :deep(*) {
    max-width: 100%;
    box-sizing: border-box;
  }
}

.dialog-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-top: 12px;
  border-top: 1px solid var(--d-border);
  font-size: 12px;
  color: var(--d-text-3);
}

/* ====== Responsive ====== */
@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr 280px;
  }
}

@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
    gap: 14px;
  }

  .hero {
    padding: 20px;
  }

  .hero-inner {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-greeting {
    font-size: 22px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 18px 14px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-icon-wrap {
    width: 38px;
    height: 38px;
    margin-bottom: 10px;
  }

  .panel-header {
    padding: 14px 16px;
  }

  .announcement-item {
    padding: 10px 16px;
    flex-wrap: wrap;
  }

  .announcement-left {
    flex: 1 1 100%;
    margin-bottom: 4px;
  }

  .announcement-time {
    margin-left: 17px;
  }

  .info-list {
    padding: 8px 16px;
  }

  .info-btn {
    margin: 12px 16px 16px;
  }

  .changelog-body {
    padding: 16px;
  }

  .tl-card {
    padding: 12px 14px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  .stat-card {
    padding: 16px 12px;
  }

  .stat-value {
    font-size: 22px;
  }
}

/* ====== Dialog RWD ====== */
:deep(.announcement-dialog) {
  @media (max-width: 768px) {
    width: 95% !important;

    .el-dialog__body {
      padding: 16px;
      max-height: calc(100vh - 150px);
      overflow-y: auto;
    }

    .dialog-title-row h3 {
      font-size: 16px;
    }

    .dialog-content {
      font-size: 13px;
      line-height: 1.6;
    }
  }
}
</style>
