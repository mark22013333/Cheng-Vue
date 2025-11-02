<template>
  <el-dialog
    title="綁定系統使用者"
    :visible.sync="dialogVisible"
    width="500px"
    @close="handleClose"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="LINE使用者" prop="lineUserId">
        <el-input v-model="form.lineUserId" disabled></el-input>
      </el-form-item>
      <el-form-item label="系統使用者" prop="sysUserId">
        <el-select
          v-model="form.sysUserId"
          placeholder="請選擇系統使用者"
          filterable
          style="width: 100%"
          :loading="userLoading"
        >
          <el-option
            v-for="user in sysUserList"
            :key="user.userId"
            :label="`${user.userName} (${user.nickName})`"
            :value="user.userId"
          >
            <span style="float: left">{{ user.nickName }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ user.userName }}</span>
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="submitting">確定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { listUser as listSysUser } from '@/api/system/user'
import { bindUser } from '@/api/line/user'

export default {
  name: 'BindDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    lineUserId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      form: {
        lineUserId: '',
        sysUserId: null
      },
      rules: {
        sysUserId: [
          { required: true, message: '請選擇系統使用者', trigger: 'change' }
        ]
      },
      sysUserList: [],
      userLoading: false,
      submitting: false
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    }
  },
  watch: {
    lineUserId: {
      handler(val) {
        this.form.lineUserId = val
      },
      immediate: true
    },
    visible(val) {
      if (val) {
        this.getSysUserList()
      }
    }
  },
  methods: {
    /** 取得系統使用者列表 */
    getSysUserList() {
      this.userLoading = true
      listSysUser({ status: '0' }).then(response => {
        this.sysUserList = response.rows
        this.userLoading = false
      }).catch(() => {
        this.userLoading = false
      })
    },
    /** 確定綁定 */
    handleConfirm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitting = true
          bindUser(this.form.lineUserId, this.form.sysUserId).then(() => {
            this.$modal.msgSuccess('綁定成功')
            this.$emit('success')
            this.handleClose()
          }).finally(() => {
            this.submitting = false
          })
        }
      })
    },
    /** 關閉對話框 */
    handleClose() {
      this.form.sysUserId = null
      this.$refs.form.resetFields()
      this.$emit('update:visible', false)
    }
  }
}
</script>
