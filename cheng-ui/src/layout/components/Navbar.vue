<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container"
               @toggleClick="toggleSideBar"/>
    <breadcrumb v-if="!settingsStore.topNav" id="breadcrumb-container" class="breadcrumb-container"/>
    <top-nav v-if="settingsStore.topNav" id="topmenu-container" class="topmenu-container"/>

    <div class="right-menu">
      <template v-if="appStore.device !== 'mobile'">
        <header-search id="header-search" class="right-menu-item"/>

        <screenfull id="screenfull" class="right-menu-item hover-effect"/>

        <el-tooltip content="主題模式" effect="dark" placement="bottom">
          <div class="right-menu-item hover-effect theme-switch-wrapper" @click="toggleTheme">
            <svg-icon v-if="settingsStore.isDark" icon-class="sunny"/>
            <svg-icon v-if="!settingsStore.isDark" icon-class="moon"/>
          </div>
        </el-tooltip>

        <el-tooltip content="版面大小" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect"/>
        </el-tooltip>
      </template>

      <!-- 通知訊息 (支援所有設備包括手機) -->
      <el-popover placement="bottom" :width="appStore.device === 'mobile' ? 'auto' : 360" trigger="click" @show="getUnreadNotifications">
        <template #reference>
          <div class="right-menu-item hover-effect notification-bell">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </el-badge>
          </div>
        </template>

        <div class="notification-panel">
          <div class="panel-header">
            <span>通知訊息 ({{ unreadCount }})</span>
          </div>
          <el-scrollbar :max-height="appStore.device === 'mobile' ? '60vh' : '400px'">
            <div v-if="notifications.length === 0" class="empty-notice">
              <i class="el-icon-check" style="font-size: 48px; color: #67C23A;"></i>
              <p>目前沒有未讀通知</p>
            </div>
            <div
              v-for="notice in notifications"
              :key="notice.noticeId"
              class="notice-item"
              @click="handleNoticeClick(notice)">
              <div class="notice-title">{{ notice.noticeTitle }}</div>
              <div class="notice-time">{{ notice.createTime }}</div>
            </div>
          </el-scrollbar>
          <div class="panel-footer">
            <router-link to="/system/notice" class="view-all-link">
              查看全部公告
            </router-link>
          </div>
        </div>
      </el-popover>


      <el-dropdown @command="handleCommand" class="avatar-container right-menu-item hover-effect" trigger="hover">
        <div class="avatar-wrapper">
          <img :src="userStore.avatar" class="user-avatar"/>
          <span class="user-nickname"> {{ userStore.nickName }} </span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <router-link to="/user/profile">
              <el-dropdown-item>個人中心</el-dropdown-item>
            </router-link>
            <el-dropdown-item command="setLayout" v-if="settingsStore.showSettings">
              <span>版面設定</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <span>登出</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>

  <!-- 通知詳情對話框 (RWD 支援) -->
  <el-dialog
    title="通知詳情"
    v-model="noticeDialogVisible"
    :width="appStore.device === 'mobile' ? '95%' : '800px'"
    :close-on-click-modal="false"
    :fullscreen="appStore.device === 'mobile'"
    @opened="setupImagePreview">
    <div class="notice-dialog-content">
      <h3>{{ currentNotice.noticeTitle }}</h3>
      <div class="notice-body" v-html="currentNotice.noticeContent" ref="noticeBodyRef"></div>
      <div class="notice-footer">
        <span class="notice-time">
          <i class="el-icon-time"></i> {{ currentNotice.createTime }}
        </span>
      </div>
    </div>
    <template #footer>
      <el-button type="primary" @click="confirmRead" :style="appStore.device === 'mobile' ? 'width: 100%' : ''">
        我已閱讀
      </el-button>
    </template>
  </el-dialog>

  <!-- 圖片預覽對話框 -->
  <el-dialog v-model="imagePreviewVisible" width="90%" top="5vh">
    <img :src="previewImageUrl" style="width: 100%; display: block;" alt="圖片預覽"/>
  </el-dialog>
</template>

<script setup>
import {ElMessageBox} from 'element-plus'
import {ref, onMounted, onUnmounted} from 'vue'
import {ChatDotRound} from '@element-plus/icons-vue'
import request from '@/utils/request'
import eventBus from '@/utils/eventBus'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import HeaderSearch from '@/components/HeaderSearch'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'

const appStore = useAppStore()
const userStore = useUserStore()
const settingsStore = useSettingsStore()

// 通知相關狀態
const unreadCount = ref(0)
const notifications = ref([])
const noticeDialogVisible = ref(false)
const currentNotice = ref({})
const noticeBodyRef = ref(null)
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
let pollTimer = null
let noticeRefreshHandler = null

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleCommand(command) {
  switch (command) {
    case "setLayout":
      setLayout()
      break
    case "logout":
      logout()
      break
    default:
      break
  }
}

function logout() {
  ElMessageBox.confirm('確定登出系統嗎？', '提示', {
    confirmButtonText: '確定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/cadm/login'
    })
  }).catch(() => {
  })
}

const emits = defineEmits(['setLayout'])

function setLayout() {
  emits('setLayout')
}

function toggleTheme() {
  settingsStore.toggleTheme()
}

// 取得未讀通知數量
function getUnreadCount() {
  request({
    url: '/system/notice/unread/count',
    method: 'get'
  }).then(response => {
    unreadCount.value = response.data || 0
  }).catch(() => {
    unreadCount.value = 0
  })
}

// 取得未讀通知列表
function getUnreadNotifications() {
  request({
    url: '/system/notice/unread',
    method: 'get'
  }).then(response => {
    notifications.value = response.data || []
  }).catch(() => {
    notifications.value = []
  })
}

// 點擊通知項目
function handleNoticeClick(notice) {
  currentNotice.value = notice
  noticeDialogVisible.value = true
}

// 確認已讀
function confirmRead() {
  request({
    url: `/system/notice/read/${currentNotice.value.noticeId}`,
    method: 'post'
  }).then(() => {
    noticeDialogVisible.value = false
    // 重新整理未讀列表
    getUnreadNotifications()
    getUnreadCount()
  }).catch((error) => {
    console.error('標記已讀失敗:', error)
  })
}

// 設定圖片點擊預覽
function setupImagePreview() {
  // 等待 DOM 更新
  setTimeout(() => {
    if (noticeBodyRef.value) {
      const images = noticeBodyRef.value.querySelectorAll('img')
      images.forEach(img => {
        img.style.cursor = 'pointer'
        img.onclick = () => {
          previewImageUrl.value = img.src
          imagePreviewVisible.value = true
        }
      })
    }
  }, 100)
}

// 啟動輪詢
onMounted(() => {
  getUnreadCount()
  getUnreadNotifications()

  // 其他頁面（例如新增公告）完成後，立即刷新紅點數字
  noticeRefreshHandler = () => {
    getUnreadCount()
  }
  eventBus.on('notice:refresh-unread-count', noticeRefreshHandler)

  // 每 30 秒輪詢一次
  pollTimer = setInterval(() => {
    getUnreadCount()
  }, 30000)
})

// 清理輪詢
onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }

  if (noticeRefreshHandler) {
    eventBus.off('notice:refresh-unread-count', noticeRefreshHandler)
  }
})
</script>

<style lang='scss' scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: var(--navbar-bg);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;
    display: flex;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }

      &.theme-switch-wrapper {
        display: flex;
        align-items: center;

        svg {
          transition: transform 0.3s;

          &:hover {
            transform: scale(1.15);
          }
        }
      }
    }

    .avatar-container {
      margin-right: 0px;
      padding-right: 0px;

      .avatar-wrapper {
        margin-top: 10px;
        right: 8px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 30px;
          height: 30px;
          margin-right: 8px;
          border-radius: 50%;
        }

        .user-nickname {
          position: relative;
          left: 0px;
          bottom: 10px;
          font-size: 14px;
          font-weight: bold;
        }

        i {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }

  // 通知鈴鐺
  .notification-bell {
    display: flex;
    align-items: center;
    justify-content: center;

    // 調整紅點位置到圖示右上角
    :deep(.el-badge__content) {
      transform: translateY(-50%) translateX(50%);
      top: 8px;
      right: 0px;
    }
  }
}

// 通知面板樣式
.notification-panel {
  .panel-header {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    font-weight: bold;
    color: #303133;
  }

  .empty-notice {
    padding: 60px 20px;
    text-align: center;
    color: #909399;

    i {
      display: block;
      margin-bottom: 16px;
    }

    p {
      margin: 0;
      font-size: 14px;
    }
  }

  .notice-item {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
    }

    &:last-child {
      border-bottom: none;
    }

    .notice-title {
      font-size: 14px;
      color: #303133;
      font-weight: 500;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .notice-time {
      font-size: 12px;
      color: #909399;
    }
  }

  .panel-footer {
    padding: 12px 16px;
    border-top: 1px solid #f0f0f0;
    text-align: center;

    .view-all-link {
      color: #409eff;
      font-size: 14px;
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 通知詳情對話框
.notice-dialog-content {
  max-width: 100%;
  overflow: hidden;
  word-wrap: break-word;
  word-break: break-all;

  h3 {
    font-size: 18px;
    color: #303133;
    margin: 0 0 16px 0;
  }

  .notice-body {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
    min-height: 100px;
    margin-bottom: 16px;
    max-width: 100%;
    overflow: hidden;
    word-wrap: break-word;

    // 限制圖片寬度，避免原圖過大
    img {
      max-width: 100% !important;
      width: 100% !important;
      height: auto !important;
      display: block !important;
      margin: 10px 0;
      object-fit: contain;
      box-sizing: border-box;
    }

    // 處理可能超出的其他元素
    * {
      max-width: 100%;
      box-sizing: border-box;
    }
  }

  .notice-footer {
    border-top: 1px solid #f0f0f0;
    padding-top: 12px;

    .notice-time {
      font-size: 12px;
      color: #909399;

      i {
        margin-right: 4px;
      }
    }
  }
}

// 手機端樣式優化
@media (max-width: 768px) {
  .notification-panel {
    min-width: 280px;
    max-width: 90vw;

    .panel-header {
      padding: 10px 12px;
      font-size: 14px;
    }

    .notice-item {
      padding: 10px 12px;

      .notice-title {
        font-size: 13px;
      }

      .notice-time {
        font-size: 11px;
      }
    }
  }

  .notice-dialog-content {
    h3 {
      font-size: 16px !important;
    }

    .notice-body {
      font-size: 13px !important;
      line-height: 1.6 !important;

      img {
        margin: 8px 0 !important;
      }
    }

    .notice-footer {
      .notice-time {
        font-size: 11px;
      }
    }
  }

  // 調整紅點在手機端的位置
  .notification-bell {
    :deep(.el-badge__content) {
      transform: translateY(-50%) translateX(40%);
      top: 6px;
      right: -2px;
    }
  }
}
</style>
