<template>
  <el-dialog
    title="匯入 LINE 使用者"
    :visible.sync="dialogVisible"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="LINE 頻道" prop="configId">
        <el-select
          v-model="form.configId"
          placeholder="請選擇 LINE 頻道"
          style="width: 100%"
          :loading="configLoading"
        >
          <el-option
            v-for="config in configList"
            :key="config.id"
            :label="config.channelName"
            :value="config.id"
          >
            <span style="float: left">{{ config.channelName }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ config.channelType }}</span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="上傳檔案" prop="file">
        <el-upload
          ref="upload"
          :limit="1"
          accept=".xlsx,.xls,.csv,.txt"
          :headers="upload.headers"
          :action="upload.url"
          :disabled="upload.isUploading"
          :on-progress="handleFileUploadProgress"
          :on-success="handleFileSuccess"
          :auto-upload="false"
          :on-change="handleFileChange"
          drag
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">將檔案拖曳至此，或<em>點擊上傳</em></div>
          <div class="el-upload__tip" slot="tip">
            <div style="color: #E6A23C; margin-bottom: 10px;">
              <i class="el-icon-warning"></i> 
              支援 .xlsx、.xls、.csv 或 .txt 格式
            </div>
            <div style="color: #909399; font-size: 12px;">
              <div>• Excel/CSV：第一列為標題，第一欄為 LINE User ID</div>
              <div>• TXT：每行一個 LINE User ID</div>
              <div>• 系統會自動去除空白和重複項目</div>
            </div>
          </div>
        </el-upload>
      </el-form-item>

      <el-alert
        title="說明"
        type="info"
        :closable="false"
        style="margin-bottom: 15px"
      >
        <div slot="default">
          <p style="margin: 5px 0;">1. 上傳包含 LINE User ID 的檔案</p>
          <p style="margin: 5px 0;">2. 系統會呼叫 LINE API 取得使用者資料</p>
          <p style="margin: 5px 0;">3. 成功的會新增或更新，失敗的會顯示詳細錯誤</p>
        </div>
      </el-alert>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="upload.isUploading">
        {{ upload.isUploading ? '匯入中...' : '開始匯入' }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { listConfig } from '@/api/line/config'
import { getToken } from '@/utils/auth'

export default {
  name: 'ImportDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      form: {
        configId: null,
        file: null
      },
      rules: {
        configId: [
          { required: true, message: '請選擇 LINE 頻道', trigger: 'change' }
        ]
      },
      configList: [],
      configLoading: false,
      upload: {
        isUploading: false,
        url: process.env.VUE_APP_BASE_API + '/line/user/import',
        headers: { Authorization: 'Bearer ' + getToken() }
      }
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
    visible(val) {
      if (val) {
        this.getConfigList()
      }
    }
  },
  methods: {
    /** 取得頻道設定列表 */
    getConfigList() {
      this.configLoading = true
      listConfig({}).then(response => {
        this.configList = response.rows.filter(item => item.status === '1')
        this.configLoading = false
      }).catch(() => {
        this.configLoading = false
      })
    },
    /** 檔案變更 */
    handleFileChange(file, fileList) {
      this.form.file = file
    },
    /** 檔案上傳進度 */
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true
    },
    /** 檔案上傳成功 */
    handleFileSuccess(response, file, fileList) {
      this.upload.isUploading = false
      this.$refs.upload.clearFiles()
      
      if (response.code === 200) {
        this.$emit('success', response.data)
        this.handleClose()
      } else {
        this.$modal.msgError(response.msg)
      }
    },
    /** 確定匯入 */
    handleConfirm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (!this.form.file) {
            this.$modal.msgWarning('請先選擇要上傳的檔案')
            return
          }

          // 建立 FormData
          const formData = new FormData()
          formData.append('file', this.form.file.raw)
          formData.append('configId', this.form.configId)

          // 使用 upload 組件的上傳功能
          this.$refs.upload.submit()
        }
      })
    },
    /** 關閉對話框 */
    handleClose() {
      this.form.configId = null
      this.form.file = null
      this.$refs.upload.clearFiles()
      this.$refs.form.resetFields()
      this.$emit('update:visible', false)
    }
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-upload-dragger {
  width: 100%;
}
</style>
