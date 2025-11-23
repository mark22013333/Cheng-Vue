<template>
  <div class="profile-container">
    <!-- 版型切換工具列 -->
    <div class="layout-toolbar">
      <el-button-group>
        <el-tooltip content="左右分欄" placement="top">
          <el-button 
            :type="layoutMode === 'side' ? 'primary' : ''" 
            icon="el-icon-menu" 
            @click="layoutMode = 'side'">
          </el-button>
        </el-tooltip>
        <el-tooltip content="上下堆疊" placement="top">
          <el-button 
            :type="layoutMode === 'stack' ? 'primary' : ''" 
            icon="el-icon-s-unfold" 
            @click="layoutMode = 'stack'">
          </el-button>
        </el-tooltip>
        <el-tooltip content="全寬模式" placement="top">
          <el-button 
            :type="layoutMode === 'wide' ? 'primary' : ''" 
            icon="el-icon-full-screen" 
            @click="layoutMode = 'wide'">
          </el-button>
        </el-tooltip>
      </el-button-group>

      <div class="theme-selector">
        <el-select v-model="cardTheme" style="width: 140px;">
          <el-option label="🎨 預設主題" value="default"></el-option>
          <el-option label="💙 藍色主題" value="blue"></el-option>
          <el-option label="💚 綠色主題" value="green"></el-option>
          <el-option label="🧡 橙色主題" value="orange"></el-option>
          <el-option label="💜 紫色主題" value="purple"></el-option>
        </el-select>
      </div>
    </div>

    <!-- 頂部背景卡片 -->
    <div class="profile-header" :class="'theme-' + cardTheme">
      <div class="header-content">
        <div class="avatar-section">
          <userAvatar />
          <div class="user-title">
            <h2>{{ user.nickName || user.userName }}</h2>
            <p class="user-subtitle">
              <el-tag type="success" effect="plain">{{ roleGroup }}</el-tag>
              <span class="dept-info" v-if="user.dept">
                <i class="el-icon-office-building"></i>
                {{ user.dept.deptName }}
              </span>
            </p>
          </div>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <div class="stat-value">{{ getDaysJoined() }}</div>
            <div class="stat-label">加入天數</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value">{{ user.loginDate ? '已登入' : '未登入' }}</div>
            <div class="stat-label">登入狀態</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要內容區 -->
    <el-row :gutter="20" class="content-row" :class="`layout-${layoutMode}`">
      <!-- 左側資訊卡片 -->
      <el-col 
        :span="layoutMode === 'wide' ? 24 : layoutMode === 'stack' ? 24 : 8" 
        :xs="24">
        <el-card class="info-card" shadow="hover" :class="'theme-' + cardTheme">
          <div slot="header" class="card-header">
            <i class="el-icon-user"></i>
            <span>個人資訊</span>
          </div>
          <div class="info-list">
            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-user-solid"></i>
                使用者名稱
              </div>
              <div class="info-value">{{ user.userName }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-phone"></i>
                手機號碼
              </div>
              <div class="info-value">{{ user.phonenumber || '未設定' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-message"></i>
                電子信箱
              </div>
              <div class="info-value">{{ user.email || '未設定' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-male"></i>
                性別
              </div>
              <div class="info-value">
                <el-tag :type="user.sex === '0' ? 'primary' : 'danger'">
                  {{ user.sex === '0' ? '男' : user.sex === '1' ? '女' : '未設定' }}
                </el-tag>
              </div>
            </div>
            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-office-building"></i>
                所屬部門
              </div>
              <div class="info-value" v-if="user.dept">{{ user.dept.deptName }}</div>
              <div class="info-value" v-else>未分配</div>
            </div>
            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-postcard"></i>
                職位
              </div>
              <div class="info-value">{{ postGroup || '未設定' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">
                <i class="el-icon-date"></i>
                建立時間
              </div>
              <div class="info-value">{{ parseTime(user.createTime, '{y}-{m}-{d}') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右側編輯區 -->
      <el-col 
        :span="layoutMode === 'wide' ? 24 : layoutMode === 'stack' ? 24 : 16" 
        :xs="24">
        <el-card class="edit-card" shadow="hover" :class="'theme-' + cardTheme">
          <el-tabs v-model="selectedTab" class="profile-tabs">
            <el-tab-pane name="userinfo">
              <span slot="label">
                <i class="el-icon-edit"></i>
                編輯資料
              </span>
              <userInfo :user="user" @refresh="getUser" />
            </el-tab-pane>
            <el-tab-pane name="resetPwd">
              <span slot="label">
                <i class="el-icon-lock"></i>
                修改密碼
              </span>
              <resetPwd />
            </el-tab-pane>
            <el-tab-pane name="security">
              <span slot="label">
                <i class="el-icon-shield"></i>
                安全設定
              </span>
              <div class="security-content">
                <el-alert
                  title="安全提示"
                  type="info"
                  description="為了您的帳號安全，建議定期修改密碼，並啟用雙因素認證。"
                  :closable="false"
                  show-icon>
                </el-alert>
                <div class="security-items">
                  <div class="security-item">
                    <div class="security-item-info">
                      <i class="el-icon-key"></i>
                      <div>
                        <div class="security-item-title">登入密碼</div>
                        <div class="security-item-desc">定期修改密碼可以提高帳號安全性</div>
                      </div>
                    </div>
                    <el-button @click="selectedTab = 'resetPwd'">修改密碼</el-button>
                  </div>
                  <div class="security-item">
                    <div class="security-item-info">
                      <i class="el-icon-phone"></i>
                      <div>
                        <div class="security-item-title">手機綁定</div>
                        <div class="security-item-desc">{{ user.phonenumber ? '已綁定: ' + user.phonenumber : '未綁定' }}</div>
                      </div>
                    </div>
                    <el-button @click="selectedTab = 'userinfo'">{{ user.phonenumber ? '修改' : '綁定' }}</el-button>
                  </div>
                  <div class="security-item">
                    <div class="security-item-info">
                      <i class="el-icon-message"></i>
                      <div>
                        <div class="security-item-title">信箱綁定</div>
                        <div class="security-item-desc">{{ user.email ? '已綁定: ' + user.email : '未綁定' }}</div>
                      </div>
                    </div>
                    <el-button @click="selectedTab = 'userinfo'">{{ user.email ? '修改' : '綁定' }}</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import userAvatar from "./userAvatar"
import userInfo from "./userInfo"
import resetPwd from "./resetPwd"
import {getUserProfile} from "@/api/system/user"

export default {
  name: "Profile",
  components: { userAvatar, userInfo, resetPwd },
  data() {
    return {
      user: {},
      roleGroup: {},
      postGroup: {},
      selectedTab: "userinfo",
      layoutMode: 'side', // 'side', 'stack', 'wide'
      cardTheme: 'default' // 卡片主題
    }
  },
  created() {
    const activeTab = this.$route.params && this.$route.params.activeTab
    if (activeTab) {
      this.selectedTab = activeTab
    }
    this.loadPreferences()
    this.getUser()
  },
  watch: {
    layoutMode(val) {
      this.savePreferences()
    },
    cardTheme(val) {
      this.savePreferences()
    }
  },
  methods: {
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data
        this.roleGroup = response.roleGroup
        this.postGroup = response.postGroup
      })
    },
    getDaysJoined() {
      if (!this.user.createTime) return 0
      const createDate = new Date(this.user.createTime)
      const today = new Date()
      const diff = today - createDate
      return Math.floor(diff / (1000 * 60 * 60 * 24))
    },
    // 儲存偏好設定到 localStorage
    savePreferences() {
      const preferences = {
        layoutMode: this.layoutMode,
        cardTheme: this.cardTheme
      }
      localStorage.setItem('profilePreferences', JSON.stringify(preferences))
    },
    // 從 localStorage 載入偏好設定
    loadPreferences() {
      const saved = localStorage.getItem('profilePreferences')
      if (saved) {
        try {
          const preferences = JSON.parse(saved)
          this.layoutMode = preferences.layoutMode || 'side'
          this.cardTheme = preferences.cardTheme || 'default'
        } catch (e) {
          console.error('載入偏好設定失敗:', e)
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
}

// 版型切換工具列
.layout-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

  .theme-selector {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 40px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;

  // 主題顏色
  &.theme-blue {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    box-shadow: 0 4px 20px rgba(79, 172, 254, 0.4);
  }

  &.theme-green {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    box-shadow: 0 4px 20px rgba(67, 233, 123, 0.4);
  }

  &.theme-orange {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    box-shadow: 0 4px 20px rgba(250, 112, 154, 0.4);
  }

  &.theme-purple {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 30px;
  }

  .avatar-section {
    display: flex;
    align-items: center;
    gap: 24px;

    .user-title {
      color: white;

      h2 {
        margin: 0 0 8px 0;
        font-size: 28px;
        font-weight: 600;
      }

      .user-subtitle {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 14px;
        opacity: 0.9;

        .dept-info {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }

  .header-stats {
    display: flex;
    gap: 30px;
    align-items: center;

    .stat-item {
      text-align: center;
      color: white;

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 13px;
        opacity: 0.85;
      }
    }

    .stat-divider {
      width: 1px;
      height: 40px;
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.content-row {
  margin-top: 20px;

  // 堆疊模式
  &.layout-stack {
    .info-card {
      margin-bottom: 20px;
    }
  }

  // 全寬模式
  &.layout-wide {
    .info-card,
    .edit-card {
      margin-bottom: 20px;
    }

    .info-card {
      .info-list {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 16px;

        .info-item {
          border-bottom: none;
          padding: 16px;
          background: #fafafa;
          border-radius: 8px;

          &:hover {
            margin: 0;
            padding: 16px;
          }
        }
      }
    }
  }

}

.info-card,
.edit-card {
  border-radius: 12px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
  
  :deep(.el-card__header) {
    border-bottom: 2px solid #f0f2f5;
    padding: 20px;
  }

  :deep(.el-card__body) {
    padding: 24px;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;

    i {
      font-size: 18px;
      color: #667eea;
    }
  }

  // 主題顏色
  &.theme-blue {
    .card-header i {
      color: #4facfe;
    }

    :deep(.el-card__header) {
      border-bottom-color: rgba(79, 172, 254, 0.2);
    }
  }

  &.theme-green {
    .card-header i {
      color: #43e97b;
    }

    :deep(.el-card__header) {
      border-bottom-color: rgba(67, 233, 123, 0.2);
    }
  }

  &.theme-orange {
    .card-header i {
      color: #fa709a;
    }

    :deep(.el-card__header) {
      border-bottom-color: rgba(250, 112, 154, 0.2);
    }
  }

  &.theme-purple {
    .card-header i {
      color: #667eea;
    }
  }
}

.info-list {
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f0f2f5;
    transition: all 0.3s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #fafafa;
      margin: 0 -12px;
      padding: 16px 12px;
      border-radius: 6px;
    }

    .info-label {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #606266;
      font-size: 14px;

      i {
        color: #909399;
        font-size: 16px;
      }
    }

    .info-value {
      color: #303133;
      font-weight: 500;
      font-size: 14px;
    }
  }
}

.profile-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
    padding: 0 24px;
    height: 44px;
    line-height: 44px;

    i {
      margin-right: 6px;
    }
  }

  :deep(.el-tabs__active-bar) {
    height: 3px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  }

  // 主題色標籤列
  .edit-card.theme-blue & :deep(.el-tabs__active-bar) {
    background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  }

  .edit-card.theme-green & :deep(.el-tabs__active-bar) {
    background: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%);
  }

  .edit-card.theme-orange & :deep(.el-tabs__active-bar) {
    background: linear-gradient(90deg, #fa709a 0%, #fee140 100%);
  }

  .edit-card.theme-purple & :deep(.el-tabs__active-bar) {
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  }

  :deep(.el-tabs__nav-wrap::after) {
    background: #e4e7ed;
  }
}

.security-content {
  .el-alert {
    margin-bottom: 24px;
  }

  .security-items {
    .security-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      margin-bottom: 16px;
      background: #fafafa;
      border-radius: 8px;
      transition: all 0.3s;

      &:hover {
        background: #f5f7fa;
        transform: translateY(-2px);
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      }

      .security-item-info {
        display: flex;
        align-items: center;
        gap: 16px;

        i {
          font-size: 32px;
          color: #667eea;
        }

        .security-item-title {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .security-item-desc {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .profile-header {
    padding: 24px;

    .header-content {
      flex-direction: column;
      align-items: flex-start;
    }

    .avatar-section {
      flex-direction: column;
      align-items: center;
      text-align: center;

      .user-title .user-subtitle {
        flex-direction: column;
      }
    }

    .header-stats {
      width: 100%;
      justify-content: space-around;
    }
  }

  .content-row {
    .el-col {
      margin-bottom: 20px;
    }
  }
}
</style>
