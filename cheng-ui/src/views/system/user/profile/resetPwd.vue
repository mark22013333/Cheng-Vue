<template>
  <el-form ref="form" :model="user" :rules="rules" label-width="80px">
    <el-form-item label="舊密碼" prop="oldPassword">
      <el-input v-model="user.oldPassword" placeholder="請輸入舊密碼" show-password type="password"/>
    </el-form-item>
    <el-form-item label="新密碼" prop="newPassword">
      <el-input v-model="user.newPassword" placeholder="請輸入新密碼" show-password type="password"/>
    </el-form-item>
    <el-form-item label="確認密碼" prop="confirmPassword">
      <el-input v-model="user.confirmPassword" placeholder="請確認新密碼" show-password type="password"/>
    </el-form-item>
    <el-form-item>
      <el-button size="mini" type="primary" @click="submit">儲存</el-button>
      <el-button size="mini" type="danger" @click="close">關閉</el-button>
    </el-form-item>
  </el-form>
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
  methods: {
    submit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          updateUserPwd(this.user.oldPassword, this.user.newPassword).then(response => {
            this.$modal.msgSuccess("修改成功")
          })
        }
      })
    },
    close() {
      this.$tab.closePage()
    }
  }
}
</script>
