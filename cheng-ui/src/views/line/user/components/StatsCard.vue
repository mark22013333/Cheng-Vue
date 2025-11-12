<template>
  <div class="stats-card-container" v-loading="loading">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <div class="stat-card total">
          <div class="stat-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">總使用者數</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <div class="stat-card following">
          <div class="stat-icon">
            <i class="el-icon-star-on"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.followingCount || 0 }}</div>
            <div class="stat-label">關注中</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <div class="stat-card bound">
          <div class="stat-icon">
            <i class="el-icon-link"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.boundCount || 0 }}</div>
            <div class="stat-label">已綁定</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <div class="stat-card unfollowed">
          <div class="stat-icon">
            <i class="el-icon-circle-close"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.unfollowedCount || 0 }}</div>
            <div class="stat-label">已封鎖</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <div class="stat-card blacklist">
          <div class="stat-icon">
            <i class="el-icon-remove-outline"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.blacklistedCount || 0 }}</div>
            <div class="stat-label">黑名單</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
        <div class="stat-card new-week">
          <div class="stat-icon">
            <i class="el-icon-plus"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.weekNewCount || 0 }}</div>
            <div class="stat-label">本週新增</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'StatsCard',
  props: {
    stats: {
      type: Object,
      default: () => ({})
    },
    loading: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style lang="scss" scoped>
.stats-card-container {
  margin-bottom: 20px;
}

.stats-row {
  margin: 0 -10px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  cursor: pointer;
  margin-bottom: 15px;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;

    i {
      font-size: 28px;
      color: #fff;
    }
  }

  .stat-content {
    flex: 1;

    .stat-value {
      font-size: 28px;
      font-weight: bold;
      line-height: 1;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }

  &.total {
    .stat-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    .stat-value {
      color: #667eea;
    }
  }

  &.following {
    .stat-icon {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
    .stat-value {
      color: #f5576c;
    }
  }

  &.bound {
    .stat-icon {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
    .stat-value {
      color: #4facfe;
    }
  }

  &.unfollowed {
    .stat-icon {
      background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
    }
    .stat-value {
      color: #ff6b6b;
    }
  }

  &.blacklist {
    .stat-icon {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    }
    .stat-value {
      color: #fa709a;
    }
  }

  &.new-week {
    .stat-icon {
      background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
    }
    .stat-value {
      color: #30cfd0;
    }
  }
}

@media (max-width: 768px) {
  .stat-card {
    padding: 15px;

    .stat-icon {
      width: 50px;
      height: 50px;
      margin-right: 10px;

      i {
        font-size: 24px;
      }
    }

    .stat-content {
      .stat-value {
        font-size: 24px;
      }

      .stat-label {
        font-size: 12px;
      }
    }
  }
}
</style>
