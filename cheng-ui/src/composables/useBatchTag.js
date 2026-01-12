import { ref, reactive } from 'vue'

/**
 * 通用批次貼標 Composable
 * 支援 LINE 使用者和物品庫存貼標頁面
 */
export function useBatchTag(options = {}) {
  const {
    parseExcelFn,
    batchBindApiFn,
    idField = 'id',
    templateHeaders = ['ID', '額外標籤'],
    templateSampleData = [],
    templateFilename = '貼標範本'
  } = options

  // 狀態
  const batchBindVisible = ref(false)
  const batchBindLoading = ref(false)
  const uploadRef = ref(null)
  const uploadFile = ref(null)
  const batchBindForm = reactive({
    tagIds: [],
    inputIds: '',
    inputType: 'excel'
  })

  // 上傳進度
  const uploadProgress = reactive({
    visible: false,
    percent: 0,
    status: '',
    message: ''
  })

  // 上次上傳結果
  const lastUploadIds = ref([])
  const lastUploadTagCount = ref(0)

  /**
   * 開啟批次貼標對話框
   */
  function openBatchBind() {
    batchBindForm.tagIds = []
    batchBindForm.inputIds = ''
    batchBindForm.inputType = 'excel'
    uploadFile.value = null
    uploadProgress.visible = false
    uploadProgress.percent = 0
    uploadProgress.status = ''
    uploadProgress.message = ''
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
    batchBindVisible.value = true
  }

  /**
   * 關閉批次貼標對話框
   */
  function closeBatchBind() {
    batchBindVisible.value = false
  }

  /**
   * 處理檔案選擇
   */
  function handleFileChange(file) {
    uploadFile.value = file.raw
  }

  /**
   * 處理檔案移除
   */
  function handleFileRemove() {
    uploadFile.value = null
  }

  /**
   * 下載範本
   */
  async function downloadTemplate() {
    const { exportTemplate } = await import('@/utils/excel')
    exportTemplate(templateHeaders, templateSampleData, templateFilename)
  }

  /**
   * 重置上次上傳結果
   */
  function resetLastUpload() {
    lastUploadIds.value = []
    lastUploadTagCount.value = 0
  }

  /**
   * 提交批次貼標
   * @param {Object} callbacks - 回調函數
   * @param {Function} callbacks.onSuccess - 成功回調
   * @param {Function} callbacks.onError - 失敗回調
   * @param {Function} callbacks.msgWarning - 警告訊息
   * @param {Function} callbacks.msgSuccess - 成功訊息
   * @param {Function} callbacks.msgError - 錯誤訊息
   */
  async function submitBatchBind(callbacks = {}) {
    const { onSuccess, onError, msgWarning, msgSuccess, msgError } = callbacks

    if (!batchBindForm.tagIds || batchBindForm.tagIds.length === 0) {
      msgWarning?.('請選擇至少一個預設標籤')
      return
    }

    let records = []

    if (batchBindForm.inputType === 'excel') {
      if (!uploadFile.value) {
        msgWarning?.('請上傳 Excel 檔案')
        return
      }

      try {
        uploadProgress.visible = true
        uploadProgress.percent = 0
        uploadProgress.status = ''
        uploadProgress.message = '正在解析 Excel 檔案...'

        records = await parseExcelFn(uploadFile.value)

        if (!records || records.length === 0) {
          msgWarning?.('Excel 檔案中沒有有效資料')
          uploadProgress.visible = false
          return
        }

        uploadProgress.message = `解析完成，共 ${records.length} 筆資料，準備上傳...`
      } catch (error) {
        msgError?.('解析 Excel 檔案失敗：' + error.message)
        uploadProgress.visible = false
        return
      }
    } else {
      if (!batchBindForm.inputIds.trim()) {
        msgWarning?.('請輸入 ID')
        return
      }

      const ids = [...new Set(
        batchBindForm.inputIds
          .split('\n')
          .map(id => id.trim())
          .filter(id => id.length > 0)
      )]
      records = ids.map(id => ({ [idField]: id, extraTagName: null }))
    }

    if (records.length === 0) {
      msgWarning?.('請輸入有效的 ID')
      return
    }

    // 分批上傳
    batchBindLoading.value = true
    uploadProgress.visible = true

    const BATCH_SIZE = 5000
    const totalBatches = Math.ceil(records.length / BATCH_SIZE)
    let totalSuccess = 0
    let totalFailed = 0
    const failedRecords = []
    const newTagsCreated = []

    try {
      for (let i = 0; i < totalBatches; i++) {
        const batchRecords = records.slice(i * BATCH_SIZE, (i + 1) * BATCH_SIZE)
        const batchNo = i + 1

        uploadProgress.percent = Math.round((i / totalBatches) * 100)
        uploadProgress.message = `正在處理第 ${batchNo}/${totalBatches} 批（共 ${batchRecords.length} 筆）...`

        const response = await batchBindApiFn({
          records: batchRecords,
          defaultTagIds: batchBindForm.tagIds,
          batchNo: batchNo,
          totalBatches: totalBatches
        })

        if (response.data) {
          totalSuccess += response.data.successCount || 0
          totalFailed += response.data.failedCount || 0
          if (response.data.failedRecords) {
            failedRecords.push(...response.data.failedRecords.slice(0, 10))
          }
          if (response.data.newTagsCreated) {
            newTagsCreated.push(...response.data.newTagsCreated)
          }
        }
      }

      uploadProgress.percent = 100
      uploadProgress.status = 'success'
      uploadProgress.message = `上傳完成！成功 ${totalSuccess} 筆，失敗 ${totalFailed} 筆`

      // 顯示結果
      let resultMsg = `成功貼標 ${totalSuccess} 筆`
      if (totalFailed > 0) {
        resultMsg += `，失敗 ${totalFailed} 筆`
      }
      if (newTagsCreated.length > 0) {
        resultMsg += `，新建標籤：${newTagsCreated.join('、')}`
      }
      msgSuccess?.(resultMsg)

      // 儲存當次上傳的結果
      lastUploadIds.value = records.map(r => r[idField])
      lastUploadTagCount.value = batchBindForm.tagIds.length

      // 延遲關閉對話框
      setTimeout(() => {
        batchBindVisible.value = false
        uploadProgress.visible = false
        onSuccess?.({ totalSuccess, totalFailed, newTagsCreated })
      }, 1500)

    } catch (error) {
      uploadProgress.status = 'exception'
      uploadProgress.message = `上傳失敗：${error.message || '未知錯誤'}`
      msgError?.('批次貼標失敗：' + (error.message || '未知錯誤'))
      onError?.(error)
    } finally {
      batchBindLoading.value = false
    }
  }

  return {
    // 狀態
    batchBindVisible,
    batchBindLoading,
    uploadRef,
    uploadFile,
    batchBindForm,
    uploadProgress,
    lastUploadIds,
    lastUploadTagCount,
    // 方法
    openBatchBind,
    closeBatchBind,
    handleFileChange,
    handleFileRemove,
    downloadTemplate,
    resetLastUpload,
    submitBatchBind
  }
}
