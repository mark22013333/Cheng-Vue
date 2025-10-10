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
              prefix-icon="el-icon-user"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性別">
            <el-radio-group v-model="form.sex">
              <el-radio-button label="0">
                <i class="el-icon-male"></i> 男
              </el-radio-button>
              <el-radio-button label="1">
                <i class="el-icon-female"></i> 女
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
          prefix-icon="el-icon-phone"
        />
      </el-form-item>
      
      <el-form-item label="電子信箱" prop="email">
        <el-input 
          v-model="form.email" 
          maxlength="50" 
          placeholder="請輸入信箱地址"
          prefix-icon="el-icon-message"
        />
      </el-form-item>
      
      <el-form-item class="form-actions">
        <el-button type="primary" @click="submit" :loading="loading">
          <i class="el-icon-check" v-if="!loading"></i>
          儲存變更
        </el-button>
        <el-button @click="resetForm">
          <i class="el-icon-refresh-left"></i>
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {updateUserProfile} from "@/api/system/user"

export default {
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
          {required: true, message: "信箱地址不能為空", trigger: "blur"},
          {
            type: "email",
            message: "請輸入正確的信箱地址",
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

  ::v-deep .el-radio-button {
    .el-radio-button__inner {
      border-radius: 6px;
      padding: 12px 20px;
      border: 1px solid #dcdfe6;
      
      i {
        margin-right: 4px;
      }
    }

    &.is-active .el-radio-button__inner {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-color: #667eea;
    }

    &:first-child .el-radio-button__inner {
      border-right: 1px solid #dcdfe6;
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
</style>
