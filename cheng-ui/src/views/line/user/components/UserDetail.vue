<template>
  <el-drawer
    :visible.sync="drawerVisible"
    :with-header="false"
    size="600px"
    @close="handleClose"
  >
    <div class="user-detail-drawer" v-loading="loading">
      <!-- 標題列 -->
      <div class="drawer-header">
        <h3>使用者詳情</h3>
        <el-button type="text" icon="el-icon-close" @click="handleClose"></el-button>
      </div>

      <div class="drawer-content" v-if="userInfo">
        <!-- 基本資料區 -->
        <div class="section">
          <div class="section-title">
            <i class="el-icon-user"></i>
            基本資料
          </div>
          <div class="user-header">
            <el-avatar :src="userInfo.linePictureUrl" :size="80">
              <i class="el-icon-user-solid"></i>
            </el-avatar>
            <div class="user-info">
              <h2>{{ userInfo.lineDisplayName }}</h2>
              <p class="user-id">{{ userInfo.lineUserId }}</p>
              <p class="status-message" v-if="userInfo.lineStatusMessage">
                <i class="el-icon-chat-line-round"></i>
                {{ userInfo.lineStatusMessage }}
              </p>
            </div>
          </div>

          <div class="info-grid">
            <div class="info-item">
              <span class="label">語言設定</span>
              <span class="value">{{ userInfo.lineLanguage || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">關注狀態</span>
              <el-tag v-if="userInfo.followStatus === 'FOLLOWING'" type="success" size="small">關注中</el-tag>
              <el-tag v-else-if="userInfo.followStatus === 'BLACKLISTED'" type="danger" size="small">黑名單</el-tag>
              <el-tag v-else type="info" size="small">已取消</el-tag>
            </div>
            <div class="info-item">
              <span class="label">綁定狀態</span>
              <el-tag v-if="userInfo.bindStatus === 'BOUND'" type="primary" size="small">已綁定</el-tag>
              <el-tag v-else type="warning" size="small">未綁定</el-tag>
            </div>
            <div class="info-item">
              <span class="label">綁定次數</span>
              <span class="value">{{ userInfo.bindCount || 0 }} 次</span>
            </div>
          </div>
        </div>

        <!-- 關注歷程 -->
        <div class="section">
          <div class="section-title">
            <i class="el-icon-time"></i>
            關注歷程
          </div>
          <el-timeline>
            <el-timeline-item
              v-if="userInfo.firstFollowTime"
              timestamp=""
              placement="top"
              color="#67C23A"
            >
              <div class="timeline-content">
                <div class="timeline-title">初次關注</div>
                <div class="timeline-time">{{ parseTime(userInfo.firstFollowTime) }}</div>
              </div>
            </el-timeline-item>
            <el-timeline-item
              v-if="userInfo.latestFollowTime"
              timestamp=""
              placement="top"
              color="#409EFF"
            >
              <div class="timeline-content">
                <div class="timeline-title">最近關注</div>
                <div class="timeline-time">{{ parseTime(userInfo.latestFollowTime) }}</div>
              </div>
            </el-timeline-item>
            <el-timeline-item
              v-if="userInfo.unfollowTime"
              timestamp=""
              placement="top"
              color="#909399"
            >
              <div class="timeline-content">
                <div class="timeline-title">取消關注</div>
                <div class="timeline-time">{{ parseTime(userInfo.unfollowTime) }}</div>
              </div>
            </el-timeline-item>
            <el-timeline-item
              v-if="userInfo.blockTime"
              timestamp=""
              placement="top"
              color="#F56C6C"
            >
              <div class="timeline-content">
                <div class="timeline-title">加入黑名單</div>
                <div class="timeline-time">{{ parseTime(userInfo.blockTime) }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 綁定歷程 -->
        <div class="section" v-if="userInfo.bindCount > 0">
          <div class="section-title">
            <i class="el-icon-link"></i>
            綁定歷程
          </div>
          <el-timeline>
            <el-timeline-item
              v-if="userInfo.firstBindTime"
              timestamp=""
              placement="top"
              color="#67C23A"
            >
              <div class="timeline-content">
                <div class="timeline-title">初次綁定</div>
                <div class="timeline-time">{{ parseTime(userInfo.firstBindTime) }}</div>
              </div>
            </el-timeline-item>
            <el-timeline-item
              v-if="userInfo.latestBindTime"
              timestamp=""
              placement="top"
              color="#409EFF"
            >
              <div class="timeline-content">
                <div class="timeline-title">最近綁定</div>
                <div class="timeline-time">{{ parseTime(userInfo.latestBindTime) }}</div>
              </div>
            </el-timeline-item>
            <el-timeline-item
              v-if="userInfo.unbindTime"
              timestamp=""
              placement="top"
              color="#909399"
            >
              <div class="timeline-content">
                <div class="timeline-title">解除綁定</div>
                <div class="timeline-time">{{ parseTime(userInfo.unbindTime) }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 互動統計 -->
        <div class="section">
          <div class="section-title">
            <i class="el-icon-data-line"></i>
            互動統計
          </div>
          <div class="stats-grid">
            <div class="stats-item">
              <div class="stats-icon send">
                <i class="el-icon-s-promotion"></i>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ userInfo.totalMessagesSent || 0 }}</div>
                <div class="stats-label">發送訊息</div>
              </div>
            </div>
            <div class="stats-item">
              <div class="stats-icon receive">
                <i class="el-icon-s-comment"></i>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ userInfo.totalMessagesReceived || 0 }}</div>
                <div class="stats-label">接收訊息</div>
              </div>
            </div>
            <div class="stats-item full-width">
              <div class="stats-icon interaction">
                <i class="el-icon-time"></i>
              </div>
              <div class="stats-info">
                <div class="stats-value" v-if="userInfo.lastInteractionTime">
                  {{ parseTime(userInfo.lastInteractionTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
                </div>
                <div class="stats-value" v-else>無</div>
                <div class="stats-label">最後互動時間</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 備註 -->
        <div class="section" v-if="userInfo.remark">
          <div class="section-title">
            <i class="el-icon-document"></i>
            備註
          </div>
          <p class="remark-text">{{ userInfo.remark }}</p>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import { getUser } from '@/api/line/user'

export default {
  name: 'UserDetail',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    userId: {
      type: Number,
      default: null
    }
  },
  data() {
    return {
      loading: false,
      userInfo: null
    }
  },
  computed: {
    drawerVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    }
  },
  watch: {
    userId: {
      handler(val) {
        if (val) {
          this.getUserDetail()
        }
      },
      immediate: true
    }
  },
  methods: {
    getUserDetail() {
      if (!this.userId) return
      
      this.loading = true
      getUser(this.userId).then(response => {
        this.userInfo = response.data
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleClose() {
      this.$emit('close')
    }
  }
}
</script>

<style lang="scss" scoped>
.user-detail-drawer {
  height: 100%;
  display: flex;
  flex-direction: column;

  .drawer-header {
    padding: 20px;
    border-bottom: 1px solid #EBEEF5;
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: bold;
    }
  }

  .drawer-content {
    flex: 1;
    overflow-y: auto;
    padding: 20px;

    .section {
      margin-bottom: 30px;

      .section-title {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 15px;
        padding-bottom: 10px;
        border-bottom: 2px solid #E4E7ED;
        color: #303133;

        i {
          margin-right: 8px;
          color: #409EFF;
        }
      }

      .user-header {
        display: flex;
        align-items: center;
        margin-bottom: 20px;
        padding: 20px;
        background: #F5F7FA;
        border-radius: 8px;

        .user-info {
          margin-left: 20px;
          flex: 1;

          h2 {
            margin: 0 0 8px 0;
            font-size: 20px;
          }

          .user-id {
            color: #909399;
            font-size: 14px;
            margin: 0 0 8px 0;
          }

          .status-message {
            color: #606266;
            font-size: 14px;
            font-style: italic;
            margin: 0;

            i {
              margin-right: 5px;
            }
          }
        }
      }

      .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 15px;

        .info-item {
          display: flex;
          flex-direction: column;
          padding: 15px;
          background: #F5F7FA;
          border-radius: 6px;

          .label {
            font-size: 12px;
            color: #909399;
            margin-bottom: 8px;
          }

          .value {
            font-size: 14px;
            color: #303133;
            font-weight: 500;
          }
        }
      }

      .timeline-content {
        .timeline-title {
          font-weight: bold;
          margin-bottom: 5px;
        }

        .timeline-time {
          color: #909399;
          font-size: 13px;
        }
      }

      .stats-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 15px;

        .stats-item {
          display: flex;
          align-items: center;
          padding: 15px;
          background: #F5F7FA;
          border-radius: 8px;

          &.full-width {
            grid-column: 1 / -1;
          }

          .stats-icon {
            width: 50px;
            height: 50px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 15px;

            i {
              font-size: 24px;
              color: #fff;
            }

            &.send {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            }

            &.receive {
              background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            }

            &.interaction {
              background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            }
          }

          .stats-info {
            flex: 1;

            .stats-value {
              font-size: 20px;
              font-weight: bold;
              margin-bottom: 5px;
            }

            .stats-label {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }

      .remark-text {
        padding: 15px;
        background: #F5F7FA;
        border-radius: 6px;
        color: #606266;
        line-height: 1.6;
      }
    }
  }
}
</style>
