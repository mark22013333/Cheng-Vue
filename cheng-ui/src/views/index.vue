<template>
  <div class="app-container home">

    <el-row :gutter="20" class="mb-20">
      <el-col :xs="24" :sm="24" :md="16" :lg="17">
        <el-card shadow="hover" class="welcome-card">
          <div class="welcome-content">
            <div class="logo-wrapper">
              <svg class="welcome-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                <path fill="#A0CFFF"
                      d="M266.7 128h576c70.7 0 128 57.3 128 128v576c0 70.7-57.3 128-128 128h-576c-70.7 0-128-57.3-128-128v-576c0-70.7 57.3-128 128-128z"/>
                <path fill="#79BBFF"
                      d="M192 213.3h576c70.7 0 128 57.3 128 128v576c0 70.7-57.3 128-128 128h-576c-70.7 0-128-57.3-128-128v-576c0-70.7 57.3-128 128-128z"/>
                <path fill="#409EFF"
                      d="M117.3 298.7h576c70.7 0 128 57.3 128 128v576c0 70.7-57.3 128-128 128h-576c-70.7 0-128-57.3-128-128v-576c0-70.7 57.3-128 128-128z"/>
              </svg>
            </div>
            <div class="welcome-text">
              <h2 class="welcome-title">CoolApps 後台管理系統</h2>
              <p class="welcome-subtitle">歡迎回來，高效管理您的應用程式與資料。</p>
              <div class="version-tags">
                <el-tag type="primary" effect="dark">
                  <i class="el-icon-price-tag"></i> {{ version }}
                </el-tag>
                <el-tag type="danger" effect="plain" class="ml-10">
                  <i class="el-icon-warning-outline"></i> 測試環境
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8" :lg="7">
        <el-card shadow="hover" class="info-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-user-solid"></i> 開發資訊</span>
          </div>
          <div class="contact-body">
            <div class="contact-row">
              <span class="label">開發者</span>
              <span class="value">Mark Cheng</span>
            </div>
            <div class="contact-row">
              <span class="label">地點</span>
              <span class="value">台灣 (Taiwan)</span>
            </div>
            <div class="contact-row">
              <span class="label">發布日</span>
              <span class="value">2025-09-22</span>
            </div>
            <el-button
              class="blog-btn"
              type="primary"
              plain
              icon="el-icon-s-promotion"
              @click="goTarget('https://mark22013333.github.io/')">
              造訪部落格
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-20">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" v-for="(stat, index) in statCards" :key="index">
        <el-card shadow="hover" class="stat-card-item" :body-style="{ padding: '20px' }">
          <div class="stat-wrapper">
            <div class="stat-icon-box" :class="stat.colorClass">
              <i :class="stat.icon"></i>
            </div>
            <div class="stat-info">
              <div class="stat-text">{{ stat.label }}</div>
              <div class="stat-number">{{ stat.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系統公告區塊 -->
    <el-row :gutter="20" class="mb-20" v-if="announcements.length > 0">
      <el-col :span="24">
        <el-card shadow="hover" class="announcement-card">
          <div slot="header" class="card-header">
            <i class="el-icon-bell"></i>
            <span>系統公告</span>
            <el-tag type="warning" size="small">{{ announcements.length }} 則</el-tag>
            <el-tag v-if="announcements.length > announcementPageSize" type="info" size="small">第
              {{ announcementCurrentPage }} 頁
            </el-tag>
          </div>
          <div class="announcement-list">
            <div
              v-for="notice in paginatedAnnouncements"
              :key="notice.noticeId"
              class="announcement-item"
              @click="showAnnouncementDetail(notice)">
              <div class="announcement-item-content">
                <i class="el-icon-document"></i>
                <span class="announcement-item-title">{{ notice.noticeTitle }}</span>
                <!-- 公告標籤 -->
                <el-tag v-if="isNewAnnouncement(notice)" type="danger" size="small" effect="dark">最新</el-tag>
                <el-tag v-else-if="isRecentAnnouncement(notice)" type="warning" size="small">近期</el-tag>
              </div>
              <div class="announcement-item-time">
                <i class="el-icon-time"></i>
                {{ notice.createTime }}
              </div>
            </div>
          </div>
          <!-- 分頁組件 -->
          <div class="announcement-pagination" v-show="announcements.length > announcementPageSize">
            <el-pagination
              small
              background
              layout="prev, pager, next"
              :total="announcements.length"
              :page-size="announcementPageSize"
              v-model:current-page="announcementCurrentPage"
              @current-change="handleAnnouncementPageChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告詳情對話框 (RWD 支援) -->
    <el-dialog
      title="公告詳情"
      v-model="announcementDialogVisible"
      width="800px"
      class="announcement-detail-dialog"
      @opened="setupAnnouncementImagePreview">
      <div class="announcement-dialog-content">
        <div class="announcement-dialog-header">
          <h3>{{ currentAnnouncement.noticeTitle }}</h3>
          <el-tag v-if="isNewAnnouncement(currentAnnouncement)" type="danger" size="small" effect="dark">最新</el-tag>
          <el-tag v-else-if="isRecentAnnouncement(currentAnnouncement)" type="warning" size="small">近期</el-tag>
        </div>
        <div class="announcement-dialog-body" v-html="currentAnnouncement.noticeContent"
             ref="announcementBodyRef"></div>
        <div class="announcement-dialog-footer">
          <span class="announcement-dialog-time">
            <i class="el-icon-time"></i> {{ currentAnnouncement.createTime }}
          </span>
        </div>
      </div>
    </el-dialog>

    <!-- 公告圖片預覽對話框 -->
    <el-dialog v-model="announcementImagePreviewVisible" width="90%" top="5vh">
      <img :src="announcementPreviewImageUrl" style="width: 100%; display: block;" alt="圖片預覽"/>
    </el-dialog>

    <el-row>
      <el-col :span="24">
        <el-card shadow="never" class="changelog-card">
          <div slot="header" class="card-header">
            <div class="header-title">
              <i class="el-icon-notebook-2"></i>
              <span>系統更新日誌</span>
            </div>
            <el-tag type="info" effect="plain">共 {{ versionLogs.length }} 個版本紀錄</el-tag>
          </div>

          <div class="changelog-container">
            <el-timeline>
              <el-timeline-item
                v-for="(log, index) in versionLogs"
                :key="index"
                :timestamp="log.date"
                :type="index === 0 ? 'success' : 'primary'"
                :icon="log.icon"
                :color="log.color"
                placement="top">

                <el-card shadow="hover" class="timeline-card">
                  <div class="timeline-header">
                    <div class="version-title">
                      <span class="v-num">{{ log.version }}</span>
                      <el-tag :type="log.tagType" effect="dark">{{ log.tag }}</el-tag>
                    </div>
                  </div>
                  <ul class="version-list">
                    <li v-for="(item, idx) in log.items" :key="idx">{{ item }}</li>
                  </ul>
                </el-card>

              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script>
import request from "@/utils/request"
import {versionLogs, getLatestVersion} from "@/data/changelog"
import {checkPermi} from "@/utils/permission"
import { MONITOR_ONLINE_LIST, SYSTEM_USER_LIST } from '@/constants/permissions'

export default {
  name: "Index",
  data() {
    return {
      launchDate: new Date("2025-09-22"),
      onlineUsers: 0,
      totalUsers: 0,
      versionLogs,  // 從 changelog.js 引入的版本日誌資料
      announcements: [],  // 系統公告列表
      announcementDialogVisible: false,  // 公告詳情對話框
      currentAnnouncement: {},  // 當前查看的公告
      announcementCurrentPage: 1,  // 公告當前頁碼
      announcementPageSize: 10,  // 每頁顯示數量
      announcementBodyRef: null,  // 公告內容 ref
      announcementImagePreviewVisible: false,  // 公告圖片預覽對話框
      announcementPreviewImageUrl: ''  // 預覽圖片 URL
    }
  },
  computed: {
    // 自動取得最新版本號（從 versionLogs 第一筆資料）
    version() {
      return getLatestVersion()
    },
    totalVersions() {
      return this.versionLogs.length
    },
    // 分頁後的公告列表
    paginatedAnnouncements() {
      const start = (this.announcementCurrentPage - 1) * this.announcementPageSize
      const end = start + this.announcementPageSize
      return this.announcements.slice(start, end)
    },
    daysSinceLaunch() {
      const today = new Date()
      const diffTime = Math.abs(today - this.launchDate)
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    },
    // 產生統計卡片資料結構，方便 v-for
    statCards() {
      // 使用 checkPermi 函數檢查權限（支援萬用字元權限）
      const hasOnlinePermission = checkPermi([MONITOR_ONLINE_LIST])
      const hasUserPermission = checkPermi([SYSTEM_USER_LIST])

      return [
        {label: "版本迭代", value: this.totalVersions, icon: "el-icon-data-line", colorClass: "primary"},
        {label: "執行天數", value: this.daysSinceLaunch, icon: "el-icon-time", colorClass: "success"},
        {
          label: "線上人數",
          value: hasOnlinePermission ? this.onlineUsers : "無權限查看",
          icon: "el-icon-user",
          colorClass: "warning",
          noPermission: !hasOnlinePermission
        },
        {
          label: "註冊帳號",
          value: hasUserPermission ? this.totalUsers : "無權限查看",
          icon: "el-icon-s-custom",
          colorClass: "info",
          noPermission: !hasUserPermission
        }
      ]
    }
  },
  mounted() {
    this.getOnlineUsers()
    this.getTotalUsers()
    this.getAnnouncements()
  },
  methods: {
    goTarget(href) {
      window.open(href, "_blank")
    },
    getOnlineUsers() {
      // 有權限才呼叫 API，避免不必要的請求
      if (checkPermi([MONITOR_ONLINE_LIST])) {
        request({
          url: "/monitor/online/list",
          method: "get"
        }).then(response => {
          this.onlineUsers = response.total || 0
        }).catch(() => {
          this.onlineUsers = 0
        })
      }
    },
    getTotalUsers() {
      // 有權限才呼叫 API，避免不必要的請求
      if (checkPermi([SYSTEM_USER_LIST])) {
        request({
          url: "/system/user/list",
          method: "get",
          params: {pageNum: 1, pageSize: 1}
        }).then(response => {
          this.totalUsers = response.total || 0
        }).catch(() => {
          this.totalUsers = 0
        })
      }
    },
    getAnnouncements() {
      request({
        url: "/system/notice/announcements",
        method: "get"
      }).then(response => {
        this.announcements = response.data || []
      }).catch(() => {
        this.announcements = []
      })
    },
    showAnnouncementDetail(notice) {
      this.currentAnnouncement = notice
      this.announcementDialogVisible = true
    },
    // 處理公告分頁變化
    handleAnnouncementPageChange(page) {
      this.announcementCurrentPage = page
    },
    // 判斷是否為最新公告（3天內）
    isNewAnnouncement(notice) {
      if (!notice.createTime) return false
      const createTime = new Date(notice.createTime)
      const now = new Date()
      const diffDays = (now - createTime) / (1000 * 60 * 60 * 24)
      return diffDays <= 3
    },
    // 判斷是否為近期公告（7天內，排除最新3天）
    isRecentAnnouncement(notice) {
      if (!notice.createTime) return false
      const createTime = new Date(notice.createTime)
      const now = new Date()
      const diffDays = (now - createTime) / (1000 * 60 * 60 * 24)
      return diffDays > 3 && diffDays <= 7
    },
    // 設定公告圖片點擊預覽
    setupAnnouncementImagePreview() {
      // 等待 DOM 更新
      this.$nextTick(() => {
        const bodyElement = this.$refs.announcementBodyRef
        if (bodyElement) {
          const images = bodyElement.querySelectorAll('img')
          images.forEach(img => {
            img.style.cursor = 'pointer'
            img.onclick = () => {
              this.announcementPreviewImageUrl = img.src
              this.announcementImagePreviewVisible = true
            }
          })
        }
      })
    }
  },
  // 使用 activated 生命週期鉤子，在路由切換回首頁時自動重新整理
  activated() {
    this.getAnnouncements()
  },
  // 監聽路由變化，當路由是首頁時重新整理公告
  watch: {
    '$route'() {
      if (this.$route.path === '/index') {
        this.getAnnouncements()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.home {
  padding: 20px;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
  background-color: #f5f7f9;
  min-height: 100vh;

  .mb-20 {
    margin-bottom: 20px;
  }

  .ml-10 {
    margin-left: 10px;
  }

  // 1. 歡迎卡片優化
  .welcome-card {
    height: 100%;
    border: none;

    .welcome-content {
      display: flex;
      align-items: center;

      .logo-wrapper {
        padding-right: 25px;
        border-right: 1px solid #eee;
        margin-right: 25px;

        // Icon 縮小
        .welcome-icon {
          width: 64px;
          height: 64px;
          color: #409EFF;
        }
      }

      .welcome-text {
        .welcome-title {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 8px 0;
        }

        .welcome-subtitle {
          font-size: 14px;
          color: #909399;
          margin: 0 0 15px 0;
        }
      }
    }
  }

  // 2. 聯絡資訊卡片優化
  .info-card {
    height: 100%;
    border: none;

    .card-header {
      font-weight: bold;
      color: #303133;
    }

    .contact-body {
      .contact-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 12px;
        font-size: 14px;

        .label {
          color: #909399;
        }

        .value {
          color: #606266;
          font-weight: 500;
        }
      }

      .blog-btn {
        width: 100%;
        margin-top: 10px;
      }
    }
  }

  // 3. 統計卡片 (獨立分離)
  .stat-card-item {
    border: none;
    margin-bottom: 10px;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-4px);
    }

    .stat-wrapper {
      display: flex;
      align-items: center;

      .stat-icon-box {
        width: 48px; // 原本 60px，縮小
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        flex-shrink: 0;

        i {
          font-size: 24px; // Icon 字體縮小
          color: #fff;
        }

        // 顏色定義
        &.primary {
          background: linear-gradient(135deg, #667eea, #764ba2);
        }

        &.success {
          background: linear-gradient(135deg, #42e695, #3bb2b8);
        }

        &.warning {
          background: linear-gradient(135deg, #f6d365, #fda085);
        }

        &.info {
          background: linear-gradient(135deg, #84fab0, #8fd3f4);
        }
      }

      .stat-info {
        .stat-text {
          color: #909399;
          font-size: 13px;
          margin-bottom: 4px;
        }

        .stat-number {
          color: #303133;
          font-size: 24px;
          font-weight: bold;
        }
      }
    }
  }

  // 4. 更新日誌 (改用 Timeline 樣式，更清晰)
  .changelog-card {
    border: none;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-title {
        font-size: 16px;
        font-weight: 600;

        i {
          margin-right: 6px;
          color: #409EFF;
        }
      }
    }

    .changelog-container {
      padding: 10px 0;

      // 替換原本的 collapse 為 timeline，解決擠壓
      .timeline-card {
        border: 1px solid #EBEEF5;

        .timeline-header {
          display: flex;
          align-items: center;
          margin-bottom: 12px;

          .version-title {
            display: flex;
            align-items: center;

            .v-num {
              font-size: 16px;
              font-weight: bold;
              margin-right: 10px;
              color: #303133;
            }
          }
        }

        .version-list {
          padding-left: 18px;
          margin: 0;

          li {
            font-size: 14px;
            line-height: 1.6;
            color: #606266;
            margin-bottom: 4px;
          }
        }
      }
    }
  }

  // 系統公告卡片
  .announcement-card {
    border: none;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: bold;
      color: #303133;
      flex-wrap: wrap;

      i {
        color: #F56C6C;
      }

      span {
        flex: 1;
      }

      .el-tag {
        margin-left: 4px;
      }
    }

    .announcement-list {
      .announcement-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
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

        .announcement-item-content {
          display: flex;
          align-items: center;
          flex: 1;
          gap: 8px;
          min-width: 0;

          i {
            color: #409EFF;
            font-size: 16px;
            flex-shrink: 0;
          }

          .announcement-item-title {
            font-size: 14px;
            color: #303133;
            font-weight: 500;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex: 1;
            min-width: 0;
          }

          .el-tag {
            flex-shrink: 0;
            margin-left: 8px;
          }
        }

        .announcement-item-time {
          font-size: 12px;
          color: #909399;
          white-space: nowrap;
          margin-left: 16px;

          i {
            margin-right: 4px;
          }
        }
      }
    }

    .announcement-pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 16px 0;
      border-top: 1px solid #f0f0f0;
      margin-top: 8px;
      min-height: 60px;

      :deep(.el-pagination) {
        display: flex;
        justify-content: center;
      }
    }
  }

  // 公告詳情對話框
  .announcement-dialog-content {
    max-width: 100%;
    overflow: hidden;
    word-wrap: break-word;
    word-break: break-all;

    .announcement-dialog-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
      flex-wrap: wrap;

      h3 {
        font-size: 18px;
        color: #303133;
        margin: 0;
        flex: 1;
        min-width: 0;
      }

      .el-tag {
        flex-shrink: 0;
      }
    }

    .announcement-dialog-body {
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
      min-height: 100px;
      margin-bottom: 16px;
      max-width: 100%;
      overflow: hidden;
      word-wrap: break-word;

      // 限制圖片寬度
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

    .announcement-dialog-footer {
      border-top: 1px solid #f0f0f0;
      padding-top: 12px;

      .announcement-dialog-time {
        font-size: 12px;
        color: #909399;

        i {
          margin-right: 4px;
        }
      }
    }
  }

  // RWD 手機適配
  @media (max-width: 768px) {
    .welcome-card {
      .welcome-content {
        flex-direction: column;
        text-align: center;

        .logo-wrapper {
          border-right: none;
          border-bottom: 1px solid #eee;
          margin-right: 0;
          margin-bottom: 15px;
          padding-right: 0;
          padding-bottom: 15px;
        }
      }
    }

    .stat-card-item {
      margin-bottom: 15px;
    }

    // 公告卡片手機端優化
    .announcement-card {
      .card-header {
        font-size: 14px;
        padding: 12px;

        .el-tag {
          font-size: 11px;
        }
      }

      .announcement-list {
        .announcement-item {
          padding: 10px 12px;
          flex-wrap: wrap;

          .announcement-item-content {
            flex: 1 1 100%;
            margin-bottom: 6px;

            .announcement-item-title {
              font-size: 13px;
            }

            i {
              font-size: 14px;
            }

            .el-tag {
              font-size: 10px;
              padding: 0 4px;
              height: 18px;
              line-height: 18px;
            }
          }

          .announcement-item-time {
            flex: 1 1 100%;
            margin-left: 22px;
            font-size: 11px;
          }
        }
      }

      .announcement-pagination {
        padding: 12px 0;

        :deep(.el-pagination) {
          .btn-prev,
          .btn-next,
          .el-pager li {
            min-width: 28px;
            height: 28px;
            line-height: 28px;
            font-size: 12px;
          }
        }
      }
    }
  }
}

// 公告對話框 RWD (使用全局樣式以覆蓋 Element Plus)
@media (max-width: 768px) {
  :deep(.announcement-detail-dialog) {
    width: 95% !important;
    margin: 0 auto;

    .el-dialog__header {
      padding: 15px;
    }

    .el-dialog__body {
      padding: 15px;
      max-height: calc(100vh - 150px);
      overflow-y: auto;
    }

    .el-dialog__footer {
      padding: 10px 15px;
    }

    .announcement-dialog-header {
      flex-wrap: wrap;
      gap: 8px;

      h3 {
        font-size: 16px !important;
        flex: 1 1 100%;
      }

      .el-tag {
        font-size: 11px;
      }
    }

    .announcement-dialog-body {
      font-size: 13px !important;
      line-height: 1.6 !important;

      img {
        margin: 8px 0 !important;
      }
    }

    .announcement-dialog-footer {
      .announcement-dialog-time {
        font-size: 11px;
      }
    }
  }
}
</style>
