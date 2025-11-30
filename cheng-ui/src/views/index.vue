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

export default {
  name: "Index",
  data() {
    return {
      launchDate: new Date("2025-09-22"),
      onlineUsers: 0,
      totalUsers: 0,
      versionLogs  // 從 changelog.js 引入的版本日誌資料
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
    daysSinceLaunch() {
      const today = new Date()
      const diffTime = Math.abs(today - this.launchDate)
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    },
    // 產生統計卡片資料結構，方便 v-for
    statCards() {
      return [
        {label: "版本迭代", value: this.totalVersions, icon: "el-icon-data-line", colorClass: "primary"},
        {label: "執行天數", value: this.daysSinceLaunch, icon: "el-icon-time", colorClass: "success"},
        {label: "線上人數", value: this.onlineUsers, icon: "el-icon-user", colorClass: "warning"},
        {label: "註冊帳號", value: this.totalUsers, icon: "el-icon-s-custom", colorClass: "info"}
      ]
    }
  },
  mounted() {
    this.getOnlineUsers()
    this.getTotalUsers()
  },
  methods: {
    goTarget(href) {
      window.open(href, "_blank")
    },
    getOnlineUsers() {
      request({
        url: "/monitor/online/list",
        method: "get"
      }).then(response => {
        this.onlineUsers = response.total || 0
      }).catch(() => {
        this.onlineUsers = 0
      })
    },
    getTotalUsers() {
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
  }
}
</style>
