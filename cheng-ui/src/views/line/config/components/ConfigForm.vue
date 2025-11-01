<template>
  <el-dialog
    :title="title"
    :visible.sync="dialogVisible"
    width="900px"
    append-to-body
    :close-on-click-modal="false"
    @close="cancel"
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="頻道名稱" prop="channelName">
        <el-input
          v-model="form.channelName"
          placeholder="請輸入頻道名稱"
          maxlength="50"
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Channel ID" prop="channelId">
            <el-input
              v-model="form.channelId"
              placeholder="請輸入 Channel ID"
              maxlength="20"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Bot Basic ID" prop="botBasicId">
            <el-input
              v-model="form.botBasicId"
              placeholder="@botid"
              maxlength="50"
            >
              <template slot="prepend">@</template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="Channel Secret" prop="channelSecret">
        <el-input
          v-model="form.channelSecret"
          placeholder="請輸入 Messaging API Channel Secret"
          type="password"
          show-password
          maxlength="100"
        />
        <div class="form-tip">
          <i class="el-icon-info"></i>
          用於 Messaging API（發送訊息）
        </div>
      </el-form-item>

      <el-divider content-position="left">LINE Login 設定（選填）</el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="Login Channel ID">
            <el-input
              v-model="form.loginChannelId"
              placeholder="選填，用於 LINE Login"
              maxlength="20"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Login Channel Secret">
            <el-input
              v-model="form.loginChannelSecret"
              placeholder="選填，用於 LINE Login"
              type="password"
              show-password
              maxlength="100"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="form-tip" style="margin-top: -10px; margin-bottom: 15px;">
        <i class="el-icon-info"></i>
        LINE 區分 Messaging API 頻道和 LINE Login 頻道，兩者的 Channel ID 和 Secret 不同。如需使用 LINE Login 功能，請填寫 Login 頻道資訊。
      </div>

      <el-divider content-position="left"></el-divider>

      <el-form-item label="Channel Access Token" prop="channelAccessToken">
        <el-input
          v-model="form.channelAccessToken"
          type="textarea"
          :rows="3"
          placeholder="請輸入 Channel Access Token（長期有效）"
          maxlength="500"
        />
        <div class="form-tip">
          <i class="el-icon-info"></i>
          請使用長期有效的 Channel Access Token
        </div>
      </el-form-item>

      <el-divider content-position="left">Webhook 設定</el-divider>

      <el-form-item label="Webhook 基礎 URL" prop="webhookBaseUrl">
        <el-input
          v-model="form.webhookBaseUrl"
          placeholder="選填，留空則使用系統預設值"
          maxlength="255"
        >
          <template slot="prepend">https://</template>
        </el-input>
        <div class="form-tip" v-if="defaultWebhookBaseUrl">
          <i class="el-icon-info"></i>
          系統預設值：{{ defaultWebhookBaseUrl }}
        </div>
        <div class="form-tip">
          <i class="el-icon-info"></i>
          LINE 平台回呼使用的網域，必須是公開可存取的 URL（如有應用程式存取路徑，請一併填入，例如：domain.com/cool-apps）。留空則使用系統預設值。
        </div>
      </el-form-item>

      <el-form-item label="Webhook URL" v-if="webhookUrl">
        <el-input v-model="webhookUrl" readonly>
          <el-button slot="append" icon="el-icon-document-copy" @click="copyWebhookUrl">複製</el-button>
        </el-input>
        <div class="form-tip">
          <i class="el-icon-info"></i>
          自動產生的 Webhook URL，請將此 URL 設定至 LINE Developer Console
        </div>
        <el-button
          v-if="webhookUrl"
          type="success"
          size="small"
          icon="el-icon-link"
          :loading="settingWebhook"
          @click="handleSetLineWebhook"
          style="margin-top: 10px;"
        >
          設定 LINE Webhook URL
        </el-button>
        <div class="form-tip">
          <i class="el-icon-warning"></i>
          點擊按鈕將自動呼叫 LINE API 設定 Webhook URL，無需手動至 LINE Developer Console 設定
        </div>
      </el-form-item>

      <el-divider content-position="left">進階設定</el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="是否啟用" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio label="1">啟用</el-radio>
              <el-radio label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="設為預設頻道" prop="isDefault">
            <el-switch
              v-model="form.isDefault"
              :active-value="true"
              :inactive-value="false"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="備註">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="請輸入備註"
          maxlength="200"
        />
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="cancel">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="submitting">確 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getConfig, addConfig, updateConfig, checkChannelType, setLineWebhook, setLineWebhookWithParams, getDefaultWebhookBaseUrl } from '@/api/line/config'

export default {
  name: 'ConfigForm',
  dicts: ['line_channel_type'],
  data() {
    return {
      // 對話框標題
      title: '',
      // 是否顯示對話框
      dialogVisible: false,
      // 是否為新增
      isAdd: true,
      // 提交中
      submitting: false,
      // 設定 Webhook 中
      settingWebhook: false,
      // Webhook URL
      webhookUrl: '',
      // 系統預設的 Webhook 基礎 URL
      defaultWebhookBaseUrl: '',
      // 表單參數
      form: {
        configId: null,
        channelName: null,
        channelId: null,
        botBasicId: null,
        channelSecret: null,
        loginChannelId: null,
        loginChannelSecret: null,
        channelAccessToken: null,
        webhookBaseUrl: null,
        channelType: 'SUB',
        status: '1',
        isDefault: false,
        remark: null
      },
      // 表單校驗
      rules: {
        channelName: [
          { required: true, message: '頻道名稱不能為空', trigger: 'blur' }
        ],
        channelId: [
          { required: true, message: 'Channel ID 不能為空', trigger: 'blur' },
          { pattern: /^\d+$/, message: 'Channel ID 格式不正確', trigger: 'blur' }
        ],
        channelSecret: [
          { required: true, message: 'Channel Secret 不能為空', trigger: 'blur' }
        ],
        channelAccessToken: [
          { required: true, message: 'Channel Access Token 不能為空', trigger: 'blur' }
        ],
        channelType: [
          { required: true, message: '請選擇頻道類型', trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    /** 開啟對話框 */
    open(configId, channelType = 'SUB') {
      this.reset()
      
      // 取得系統預設的 Webhook 基礎 URL
      getDefaultWebhookBaseUrl().then(response => {
        this.defaultWebhookBaseUrl = response.data || ''
        console.log('載入預設 Webhook 基礎 URL:', this.defaultWebhookBaseUrl)
        
        // 如果是新增模式，設定預設值
        if (!configId) {
          this.form.webhookBaseUrl = this.defaultWebhookBaseUrl
          console.log('新增模式：設定預設 webhookBaseUrl =', this.form.webhookBaseUrl)
          // 觸發 Webhook URL 產生
          this.$nextTick(() => {
            this.generateWebhookUrl()
          })
        }
      })
      
      if (configId) {
        this.isAdd = false
        this.title = '修改頻道設定'
        getConfig(configId).then(response => {
          console.log('=== ConfigForm 載入資料 ===')
          console.log('原始回應:', response.data)
          console.log('status 欄位:', response.data.status)
          console.log('status 類型:', typeof response.data.status)
          
          // 後端 Status 枚舉轉換為前端字串
          // 後端可能返回：
          // 1. 枚舉物件：{code: 1, description: "啟用"}
          // 2. 數字：1 或 0
          // 3. 字串：'ENABLE' 或 'DISABLE'
          let statusValue = '1' // 預設啟用
          
          if (response.data.status) {
            const status = response.data.status
            
            if (typeof status === 'object' && status.code !== undefined) {
              // 枚舉物件
              statusValue = String(status.code)
              console.log('解析為枚舉物件，code:', status.code)
            } else if (typeof status === 'number') {
              // 數字
              statusValue = String(status)
              console.log('解析為數字:', status)
            } else if (typeof status === 'string') {
              // 字串：'ENABLE' -> '1', 'DISABLE' -> '0'
              if (status === 'ENABLE' || status === '1') {
                statusValue = '1'
              } else if (status === 'DISABLE' || status === '0') {
                statusValue = '0'
              }
              console.log('解析為字串:', status, '-> 轉換為:', statusValue)
            }
          }
          
          this.form = {
            ...response.data,
            // 將 YES/NO 枚舉轉換為 Boolean
            isDefault: response.data.isDefault === 'YES',
            // 轉換 status
            status: statusValue
          }
          console.log('設定後的 form.status:', this.form.status)
          console.log('=== ConfigForm 載入完成 ===')
          // 使用後端返回的 webhookUrl
          this.webhookUrl = response.data.webhookUrl || ''
          // 重新產生 Webhook URL（以防後端返回的不正確）
          this.$nextTick(() => {
            this.generateWebhookUrl()
          })
        })
      } else {
        this.isAdd = true
        this.title = '新增頻道設定'
        // 設定頻道類型（從外部傳入）
        this.form.channelType = channelType
      }
      this.dialogVisible = true
    },
    /** 表單重置 */
    reset() {
      this.form = {
        configId: null,
        channelName: null,
        channelId: null,
        botBasicId: null,
        channelSecret: null,
        loginChannelId: null,
        loginChannelSecret: null,
        channelAccessToken: null,
        webhookBaseUrl: null,
        channelType: 'SUB',
        status: '1',
        isDefault: false,
        remark: null
      }
      this.webhookUrl = ''
      this.resetForm('form')
    },
    /** 取消按鈕 */
    cancel() {
      this.dialogVisible = false
      this.reset()
    },
    /** 提交按鈕 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          console.log('=== 提交表單 ===')
          console.log('表單 status 原始值:', this.form.status)
          console.log('表單 status 類型:', typeof this.form.status)
          
          // 轉換資料格式
          // 前端和後端保持一致：'1'=啟用, '0'=停用
          const statusCode = parseInt(this.form.status)
          console.log('轉換後的 statusCode:', statusCode)
          
          const submitData = {
            ...this.form,
            channelTypeCode: this.form.channelType,  // 使用 channelTypeCode 對應後端的 setChannelTypeCode() 方法
            isDefaultCode: this.form.isDefault ? 1 : 0,  // 使用 isDefaultCode 對應後端的 setIsDefaultCode() 方法
            statusCode: statusCode,  // 使用 statusCode 對應後端的 setStatusCode() 方法
            channelType: undefined,  // 移除 channelType 避免衝突
            isDefault: undefined,  // 移除 isDefault 避免衝突
            status: undefined  // 移除 status 避免衝突
          }
          console.log('提交的資料:', submitData)
          console.log('=== 提交資料完成 ===')

          // 如果是修改主頻道，顯示警告
          if (this.form.configId != null && this.form.channelType === 'MAIN') {
            this.$confirm(
              '您即將修改主頻道設定，此操作將影響系統預設的 LINE 頻道。是否確定要修改？',
              '警告',
              {
                confirmButtonText: '確定修改',
                cancelButtonText: '取消',
                type: 'warning'
              }
            ).then(() => {
              this.performUpdate(submitData)
            })
          } else if (this.form.configId != null) {
            this.performUpdate(submitData)
          } else {
            this.performAdd(submitData)
          }
        }
      })
    },
    /** 執行新增 */
    performAdd(submitData) {
      this.submitting = true
      addConfig(submitData).then(response => {
        this.$modal.msgSuccess('新增成功')
        this.dialogVisible = false
        this.$emit('success')
      }).finally(() => {
        this.submitting = false
      })
    },
    /** 執行修改 */
    performUpdate(submitData) {
      this.submitting = true
      updateConfig(submitData).then(response => {
        this.$modal.msgSuccess('修改成功')
        this.dialogVisible = false
        this.$emit('success')
      }).finally(() => {
        this.submitting = false
      })
    },
    /** 產生 Webhook URL */
    generateWebhookUrl() {
      const botId = this.form.botBasicId || this.form.channelId
      console.log('產生 Webhook URL，botId:', botId)
      
      if (!botId) {
        this.webhookUrl = ''
        return
      }

      // 決定使用哪個 Base URL
      let baseUrl = this.form.webhookBaseUrl || this.defaultWebhookBaseUrl
      console.log('使用的 baseUrl:', baseUrl)
      
      if (!baseUrl) {
        // 如果都沒有，使用瀏覽器的 origin
        baseUrl = window.location.origin
        console.log('使用瀏覽器 origin:', baseUrl)
      } else {
        // 確保有 protocol
        if (!baseUrl.startsWith('http://') && !baseUrl.startsWith('https://')) {
          baseUrl = 'https://' + baseUrl
          console.log('加上 https 協議:', baseUrl)
        }
      }

      // 移除結尾的斜線
      if (baseUrl.endsWith('/')) {
        baseUrl = baseUrl.substring(0, baseUrl.length - 1)
      }

      // 移除 Bot ID 開頭的 @ 符號
      let pathParam = botId
      if (pathParam.startsWith('@')) {
        pathParam = pathParam.substring(1)
      }

      // 組裝 Webhook URL（使用後端定義的路徑：/webhook/line）
      const webhookPath = 'webhook/line'
      this.webhookUrl = `${baseUrl}/${webhookPath}/${pathParam}`
      console.log('最終產生的 Webhook URL:', this.webhookUrl)
    },
    /** 複製 Webhook URL */
    copyWebhookUrl() {
      const input = document.createElement('input')
      input.value = this.webhookUrl
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
      this.$modal.msgSuccess('已複製到剪貼簿')
    },
    /** 檢查頻道類型是否已存在 */
    async checkExistingChannel(channelType) {
      try {
        const response = await checkChannelType(channelType)
        
        // 編輯模式下：直接清空表單（保留頻道類型）
        if (!this.isAdd) {
          this.clearFormExceptChannelType()
          return
        }
        
        // 新增模式下：檢查是否已有該類型的資料
        if (response.data) {
          // 已存在該類型的頻道
          this.$confirm(
            `已存在一個「${this.getChannelTypeName(channelType)}」頻道設定，是否要載入現有設定進行編輯？`,
            '提示',
            {
              confirmButtonText: '載入編輯',
              cancelButtonText: '繼續新增',
              type: 'warning'
            }
          ).then(() => {
            // 載入現有資料
            this.form = {
              ...response.data,
              isDefault: response.data.isDefault === 'YES'
            }
            this.isAdd = false
            this.title = '修改頻道設定'
            this.generateWebhookUrl()
          }).catch(() => {
            // 用戶選擇繼續新增，清空表單（保留頻道類型）
            this.clearFormExceptChannelType()
          })
        } else {
          // 該類型頻道不存在，清空表單（保留頻道類型）
          this.clearFormExceptChannelType()
        }
      } catch (error) {
        console.error('檢查頻道類型失敗', error)
      }
    },
    /** 清空表單（保留頻道類型） */
    clearFormExceptChannelType() {
      const currentChannelType = this.form.channelType
      this.reset()
      this.form.channelType = currentChannelType
    },
    /** 取得頻道類型名稱 */
    getChannelTypeName(type) {
      const typeMap = {
        'MAIN': '主頻道',
        'SUB': '副頻道',
        'TEST': '測試頻道'
      }
      return typeMap[type] || type
    },
    /** 設定 LINE Webhook URL */
    handleSetLineWebhook() {
      // 驗證必要欄位
      if (!this.form.channelAccessToken) {
        this.$modal.msgWarning('請填寫 Channel Access Token')
        return
      }
      if (!this.webhookUrl) {
        this.$modal.msgWarning('Webhook URL 未產生')
        return
      }

      this.$confirm(
        '即將使用當前表單的設定呼叫 LINE API 設定 Webhook URL，確定要繼續嗎？',
        '確認',
        {
          confirmButtonText: '確定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        this.settingWebhook = true
        
        // 使用表單當前的值
        const params = {
          webhookUrl: this.webhookUrl,
          channelAccessToken: this.form.channelAccessToken,
          configId: this.form.configId || null
        }
        
        setLineWebhookWithParams(params).then(response => {
          this.$modal.msgSuccess(response.msg || 'Webhook URL 設定成功')
          this.$emit('success')
        }).catch(error => {
          this.$modal.msgError('設定失敗：' + (error.msg || error.message || '未知錯誤'))
        }).finally(() => {
          this.settingWebhook = false
        })
      })
    }
  },
  watch: {
    'form.botBasicId'() {
      this.generateWebhookUrl()
    },
    'form.channelId'() {
      this.generateWebhookUrl()
    },
    'form.webhookBaseUrl'() {
      this.generateWebhookUrl()
    },
  }
}
</script>

<style lang="scss" scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;

  i {
    margin-right: 4px;
  }
}

::v-deep .el-divider__text {
  font-weight: 600;
  color: #606266;
}

// Webhook URL 唯讀欄位游標樣式
::v-deep .el-input__inner[readonly] {
  cursor: not-allowed;
  background-color: #f5f7fa;
}

// 頻道類型下拉選單顯示正常指針游標
::v-deep .el-select:not(.is-disabled) .el-input__inner {
  cursor: pointer;
}
</style>
