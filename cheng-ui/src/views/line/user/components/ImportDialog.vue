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
            :key="config.configId"
            :label="config.channelName"
            :value="config.configId"
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
          :data="{ configId: form.configId }"
          :disabled="upload.isUploading"
          :on-progress="handleFileUploadProgress"
          :on-success="handleFileSuccess"
          :on-error="handleFileError"
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
import { getEnabledConfigs } from '@/api/line/config'
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
      // 使用新的專用 API 取得啟用的頻道列表
      getEnabledConfigs().then(response => {
        this.configList = response.data
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
        const result = response.data
        
        // 顯示匯入結果
        let message = `<div style="text-align: left; line-height: 1.8;">
          <p style="font-size: 14px; margin-bottom: 15px;"><strong>📊 匯入統計</strong></p>
          <p style="margin: 5px 0;">📁 總共：<strong>${result.totalCount || 0}</strong> 筆</p>
          <p style="margin: 5px 0; color: #67C23A;">✅ 成功：<strong>${result.successCount || 0}</strong> 筆</p>
          <p style="margin: 5px 0; color: #409EFF;">　├ 新增：${result.newCount || 0} 筆</p>
          <p style="margin: 5px 0; color: #409EFF;">　└ 更新：${result.updateCount || 0} 筆</p>
        `
        
        if (result.failCount > 0) {
          message += `<p style="margin: 5px 0; color: #F56C6C;">❌ 失敗：<strong>${result.failCount}</strong> 筆</p>`
        }
        
        if (result.failCount > 0 && result.failDetails && result.failDetails.length > 0) {
          message += `<div style="margin-top: 20px; padding: 15px; background: #FEF0F0; border-radius: 4px; border-left: 4px solid #F56C6C;">
            <p style="font-size: 14px; margin-bottom: 10px; color: #F56C6C;"><strong>⚠️ 失敗項目詳情</strong></p>
            <div style="max-height: 300px; overflow-y: auto;">`
          
          result.failDetails.forEach((detail, index) => {
            message += `<div style="margin: 10px 0; padding: 10px; background: white; border-radius: 4px; font-size: 13px;">
              <p style="margin: 3px 0;"><strong>第 ${detail.rowNumber} 行</strong></p>
              <p style="margin: 3px 0; color: #606266;">User ID: <code style="background: #f5f5f5; padding: 2px 6px; border-radius: 3px;">${detail.lineUserId}</code></p>
              <p style="margin: 3px 0; color: #F56C6C;">原因: ${detail.reason || '未知錯誤'}</p>
            </div>`
          })
          
          message += `</div></div>`
        }
        
        message += '</div>'
        
        this.$alert(message, '匯入結果', {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '確定',
          type: result.failCount > 0 ? 'warning' : 'success',
          customClass: 'import-result-dialog'
        })
        
        this.$emit('success', result)
        this.handleClose()
      } else {
        // 顯示錯誤訊息（使用可滾動的對話框）
        this.$alert(
          `<div style="text-align: left; max-height: 400px; overflow-y: auto; word-break: break-all; line-height: 1.6; padding: 10px;">
            <p style="color: #F56C6C; font-size: 14px; margin-bottom: 10px;"><strong>❌ 匯入失敗</strong></p>
            <p style="color: #606266;">${response.msg || '未知錯誤，請聯繫系統管理員'}</p>
          </div>`,
          '錯誤訊息',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '確定',
            type: 'error',
            customClass: 'import-error-dialog'
          }
        )
      }
    },
    /** 檔案上傳失敗 */
    handleFileError(error, file, fileList) {
      this.upload.isUploading = false
      
      let errorMessage = '檔案上傳失敗'
      let errorDetails = ''
      
      try {
        // 嘗試解析錯誤訊息
        if (error.message) {
          try {
            const errorObj = JSON.parse(error.message)
            errorMessage = errorObj.msg || errorObj.message || error.message
          } catch (e) {
            errorMessage = error.message
          }
        }
        
        // 檢查是否有額外的錯誤詳情
        if (error.response) {
          errorDetails = `<p style="margin-top: 10px; color: #909399; font-size: 12px;">HTTP 狀態碼: ${error.response.status}</p>`
        }
      } catch (e) {
        errorMessage = '未知錯誤，請聯繫系統管理員'
      }
      
      // 使用可滾動的對話框顯示錯誤
      this.$alert(
        `<div style="text-align: left; max-height: 400px; overflow-y: auto; word-break: break-all; line-height: 1.6; padding: 10px;">
          <p style="color: #F56C6C; font-size: 14px; margin-bottom: 10px;"><strong>❌ 上傳失敗</strong></p>
          <div style="padding: 10px; background: #FEF0F0; border-radius: 4px; border-left: 4px solid #F56C6C;">
            <p style="color: #606266; margin: 5px 0;">${errorMessage}</p>
            ${errorDetails}
          </div>
          <p style="margin-top: 15px; color: #909399; font-size: 12px;">
            <strong>可能的原因：</strong><br>
            • 檔案格式不正確（僅支援 .xlsx, .xls, .csv, .txt）<br>
            • 檔案內容格式錯誤<br>
            • 網路連線問題<br>
            • 伺服器暫時無法回應
          </p>
        </div>`,
        '錯誤訊息',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '確定',
          type: 'error',
          customClass: 'import-error-dialog'
        }
      )
    },
    /** 確定匯入 */
    handleConfirm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (!this.form.file) {
            this.$modal.msgWarning('請先選擇要上傳的檔案')
            return
          }

          // 驗證是否選擇頻道
          if (!this.form.configId) {
            this.$modal.msgWarning('請選擇 LINE 頻道')
            return
          }

          // 使用 upload 組件的上傳功能（configId 會透過 :data 屬性傳遞）
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

<style lang="scss">
// 匯入結果對話框樣式
.import-result-dialog {
  .el-message-box {
    width: 650px;
    max-width: 90%;
    
    // 確保在螢幕中央
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    margin: 0 !important;
  }
  
  .el-message-box__content {
    max-height: 60vh;
    overflow-y: auto;
    padding: 20px 25px;
  }
  
  .el-message-box__message {
    line-height: 1.6;
  }
  
  // 美化滾動條
  .el-message-box__content::-webkit-scrollbar {
    width: 8px;
  }
  
  .el-message-box__content::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  .el-message-box__content::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
  }
  
  .el-message-box__content::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
}

// 匯入錯誤對話框樣式
.import-error-dialog {
  .el-message-box {
    width: 800px;  // 從 600px 增加到 800px
    max-width: 95%;  // 從 90% 增加到 95%
    
    // 確保在螢幕中央
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    margin: 0 !important;
  }
  
  .el-message-box__content {
    max-height: 60vh;
    overflow-y: auto;
    padding: 20px 25px;
  }
  
  .el-message-box__message {
    line-height: 1.6;
  }
  
  // 美化滾動條
  .el-message-box__content::-webkit-scrollbar {
    width: 8px;
  }
  
  .el-message-box__content::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  .el-message-box__content::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
  }
  
  .el-message-box__content::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
}
</style>
