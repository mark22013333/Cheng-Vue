<template>
  <div class="user-info-form">
    <el-form ref="form" :model="form" :rules="rules" label-width="100px" label-position="top">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="使用者暱稱" prop="nickName">
            <el-input
              v-model="form.nickName"
              maxlength="30"
              placeholder="請輸入暱稱"
              show-word-limit
              size="large">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性別">
            <el-radio-group v-model="form.sex" size="large">
              <el-radio-button label="0">
                <span style="display: inline-flex; align-items: center; gap: 6px;">
                  <el-icon :size="18"><Male /></el-icon>
                  男
                </span>
              </el-radio-button>
              <el-radio-button label="1">
                <span style="display: inline-flex; align-items: center; gap: 6px;">
                  <el-icon :size="18"><Female /></el-icon>
                  女
                </span>
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="手機號碼" prop="phonenumber">
        <el-input
          v-model="form.phonenumber"
          maxlength="11"
          placeholder="請輸入手機號碼"
          size="large">
          <template #prefix>
            <el-icon><Iphone /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="電子信箱" prop="email">
        <el-input
          v-model="form.email"
          maxlength="50"
          placeholder="請輸入電子信箱"
          size="large">
          <template #prefix>
            <el-icon><Message /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item class="form-actions">
        <el-button type="primary" @click="submit" :loading="loading" size="large">
          <el-icon v-if="!loading" style="margin-right: 6px;"><Check /></el-icon>
          儲存變更
        </el-button>
        <el-button @click="resetForm" size="large">
          <el-icon style="margin-right: 6px;"><RefreshLeft /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {updateUserProfile} from "@/api/system/user"
import {User, Iphone, Message, Check, RefreshLeft, Male, Female} from '@element-plus/icons-vue'

export default {
  components: {User, Iphone, Message, Check, RefreshLeft, Male, Female},
  props: {
    user: {
      type: Object
    }
  },
  data() {
    return {
      form: {},
      loading: false,
      // 表單校驗
      rules: {
        nickName: [
          {required: true, message: "使用者暱稱不能為空", trigger: "blur"}
        ],
        email: [
          {required: true, message: "信箱位置不能為空", trigger: "blur"},
          {
            type: "email",
            message: "請輸入正確的信箱",
            trigger: ["blur", "change"]
          }
        ],
        phonenumber: [
          {required: true, message: "手機號碼不能為空", trigger: "blur"},
          {
            pattern: /^09\d{8}$/,
            message: "請輸入正確的手機號碼",
            trigger: "blur"
          }
        ]
      }
    }
  },
  watch: {
    user: {
      handler(user) {
        if (user) {
          this.form = { nickName: user.nickName, phonenumber: user.phonenumber, email: user.email, sex: user.sex }
        }
      },
      immediate: true
    }
  },
  methods: {
    submit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.loading = true
          updateUserProfile(this.form).then(response => {
            this.$modal.msgSuccess("資料更新成功")
            this.user.phonenumber = this.form.phonenumber
            this.user.email = this.form.email
            this.user.nickName = this.form.nickName
            this.user.sex = this.form.sex
            this.$emit('refresh')
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        }
      })
    },
    resetForm() {
      this.form = {
        nickName: this.user.nickName,
        phonenumber: this.user.phonenumber,
        email: this.user.email,
        sex: this.user.sex
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.user-info-form {
  :deep(.el-form-item__label) {
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
    font-size: 15px;
  }

  // 優化輸入框樣式
  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 8px;
      padding: 8px 15px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      transition: all 0.3s;
      background: #fff;

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }

      &.is-focus {
        border-color: #409eff;
        box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1), 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }

    .el-input__prefix {
      color: #909399;
      font-size: 18px;

      .el-icon {
        font-size: 18px;
      }
    }

    .el-input__inner {
      font-size: 14px;
      color: #303133;

      &::placeholder {
        color: #c0c4cc;
        font-size: 14px;
      }
    }

    // 字數統計樣式
    .el-input__count {
      color: #909399;
      font-size: 12px;
    }
  }

  :deep(.el-radio-group) {
    display: flex;
    gap: 12px;
    width: 100%;
  }

  :deep(.el-radio-button) {
    flex: 1;

    .el-radio-button__inner {
      border-radius: 8px !important;
      padding: 12px 20px;
      border: 2px solid #dcdfe6;
      width: 100%;
      transition: all 0.3s;
      font-size: 14px;
      font-weight: 500;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

      &:hover {
        border-color: #409eff;
        background: #ecf5ff;
      }
    }

    // 第一個按鈕（男）- 藍色
    &:first-child {
      .el-radio-button__inner {
        border-radius: 8px !important;
        border-right: 2px solid #dcdfe6 !important;

        &:hover {
          border-color: #409eff;
          background: #ecf5ff;
        }
      }

      &.is-active .el-radio-button__inner {
        background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%) !important;
        border-color: #409eff !important;
        color: #fff !important;
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
      }
    }

    // 第二個按鈕（女）- 粉色
    &:last-child {
      .el-radio-button__inner {
        border-radius: 8px !important;

        &:hover {
          border-color: #f56c6c;
          background: #fef0f0;
        }
      }

      &.is-active .el-radio-button__inner {
        background: linear-gradient(135deg, #ff69b4 0%, #ff1493 100%) !important;
        border-color: #ff69b4 !important;
        color: #fff !important;
        box-shadow: 0 4px 12px rgba(255, 105, 180, 0.3);
      }
    }
  }

  .form-actions {
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid #f0f2f5;

    :deep(.el-form-item__content) {
      display: flex;
      gap: 12px;
    }

    :deep(.el-button) {
      border-radius: 8px;
      padding: 12px 28px;
      font-weight: 500;
      transition: all 0.3s;
      display: inline-flex;
      align-items: center;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &:active {
        transform: translateY(0);
      }

      &.el-button--primary {
        background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
        border: none;

        &:hover {
          background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
        }
      }

      .el-icon {
        font-size: 16px;
      }
    }
  }
}
</style>
