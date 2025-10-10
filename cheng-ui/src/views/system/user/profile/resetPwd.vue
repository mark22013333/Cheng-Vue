<template>
  <div class="reset-pwd-form">
    <el-alert
      title="密碼安全提示"
      type="warning"
      :closable="false"
      show-icon
      class="security-tip">
      <ul>
        <li>密碼長度需介於 6 至 20 個字元</li>
        <li>不可包含非法字元：&lt; &gt; " ' \ |</li>
        <li>建議使用字母、數字和特殊符號組合</li>
      </ul>
    </el-alert>

    <el-form ref="form" :model="user" :rules="rules" label-width="100px" label-position="top" class="pwd-form">
      <el-form-item label="目前密碼" prop="oldPassword">
        <el-input 
          v-model="user.oldPassword" 
          placeholder="請輸入目前密碼" 
          show-password 
          type="password"
          prefix-icon="el-icon-lock"
        />
      </el-form-item>
      
      <el-form-item label="新密碼" prop="newPassword">
        <el-input 
          v-model="user.newPassword" 
          placeholder="請輸入新密碼（6-20個字元）" 
          show-password 
          type="password"
          prefix-icon="el-icon-key"
          @input="checkPasswordStrength"
        />
        <div class="password-strength" v-if="user.newPassword">
          <div class="strength-bar">
            <div class="strength-fill" :class="strengthClass" :style="{width: strengthWidth}"></div>
          </div>
          <span class="strength-text" :style="{color: strengthColor}">{{ strengthText }}</span>
        </div>
      </el-form-item>
      
      <el-form-item label="確認新密碼" prop="confirmPassword">
        <el-input 
          v-model="user.confirmPassword" 
          placeholder="請再次輸入新密碼" 
          show-password 
          type="password"
          prefix-icon="el-icon-check"
        />
      </el-form-item>
      
      <el-form-item class="form-actions">
        <el-button type="primary" @click="submit" :loading="loading">
          <i class="el-icon-check" v-if="!loading"></i>
          確認修改
        </el-button>
        <el-button @click="resetForm">
          <i class="el-icon-refresh-left"></i>
          清空
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {updateUserPwd} from "@/api/system/user"

export default {
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.user.newPassword !== value) {
        callback(new Error("兩次輸入的密碼不一致"))
      } else {
        callback()
      }
    }
    return {
      user: {
        oldPassword: undefined,
        newPassword: undefined,
        confirmPassword: undefined
      },
      loading: false,
      passwordStrength: 0,
      // 表單校驗
      rules: {
        oldPassword: [
          {required: true, message: "舊密碼不能為空", trigger: "blur"}
        ],
        newPassword: [
          {required: true, message: "新密碼不能為空", trigger: "blur"},
          {min: 6, max: 20, message: "長度在 6 到 20 個字串", trigger: "blur"},
          {pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字串：< > \" ' \\\ |", trigger: "blur"}
        ],
        confirmPassword: [
          {required: true, message: "確認密碼不能為空", trigger: "blur"},
          { required: true, validator: equalToPassword, trigger: "blur" }
        ]
      }
    }
  },
  computed: {
    strengthClass() {
      const classes = ['weak', 'medium', 'strong', 'very-strong']
      return classes[Math.min(this.passwordStrength, 3)]
    },
    strengthWidth() {
      return ((this.passwordStrength + 1) * 25) + '%'
    },
    strengthText() {
      const texts = ['弱', '中等', '強', '非常強']
      return texts[this.passwordStrength] || ''
    },
    strengthColor() {
      const colors = ['#F56C6C', '#E6A23C', '#67C23A', '#409EFF']
      return colors[this.passwordStrength] || ''
    }
  },
  methods: {
    checkPasswordStrength() {
      const pwd = this.user.newPassword
      if (!pwd) {
        this.passwordStrength = 0
        return
      }
      
      let strength = 0
      // 長度檢查
      if (pwd.length >= 8) strength++
      if (pwd.length >= 12) strength++
      // 複雜度檢查
      if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++
      if (/\\d/.test(pwd)) strength++
      if (/[^a-zA-Z0-9]/.test(pwd)) strength++
      
      this.passwordStrength = Math.min(Math.floor(strength / 2), 3)
    },
    submit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.loading = true
          updateUserPwd(this.user.oldPassword, this.user.newPassword).then(response => {
            this.$modal.msgSuccess("密碼修改成功，請重新登入")
            this.resetForm()
            this.loading = false
            // 可以選擇在這裡登出使用者
            // this.$store.dispatch('LogOut').then(() => {
            //   location.href = '/login'
            // })
          }).catch(() => {
            this.loading = false
          })
        }
      })
    },
    resetForm() {
      this.user = {
        oldPassword: undefined,
        newPassword: undefined,
        confirmPassword: undefined
      }
      this.passwordStrength = 0
      this.$refs.form.clearValidate()
    }
  }
}
</script>

<style lang="scss" scoped>
.reset-pwd-form {
  .security-tip {
    margin-bottom: 24px;

    ::v-deep .el-alert__content {
      ul {
        margin: 8px 0 0 0;
        padding-left: 20px;

        li {
          font-size: 13px;
          line-height: 2;
          color: #E6A23C;
        }
      }
    }
  }

  .pwd-form {
    ::v-deep .el-form-item__label {
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
    }

    ::v-deep .el-input__inner {
      border-radius: 6px;
      transition: all 0.3s;

      &:focus {
        border-color: #667eea;
        box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
      }
    }

    .password-strength {
      margin-top: 8px;
      display: flex;
      align-items: center;
      gap: 12px;

      .strength-bar {
        flex: 1;
        height: 4px;
        background: #f0f2f5;
        border-radius: 2px;
        overflow: hidden;

        .strength-fill {
          height: 100%;
          transition: all 0.3s;
          border-radius: 2px;

          &.weak {
            background: #F56C6C;
          }

          &.medium {
            background: #E6A23C;
          }

          &.strong {
            background: #67C23A;
          }

          &.very-strong {
            background: #409EFF;
          }
        }
      }

      .strength-text {
        font-size: 12px;
        font-weight: 600;
        min-width: 60px;
      }
    }

    .form-actions {
      margin-top: 32px;
      padding-top: 24px;
      border-top: 1px solid #f0f2f5;

      ::v-deep .el-form-item__content {
        display: flex;
        gap: 12px;
      }

      .el-button {
        i {
          margin-right: 4px;
        }
      }
    }
  }
}
</style>
