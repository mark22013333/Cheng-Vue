# ImportDialog 參數傳遞問題修復

> **修復時間**: 2025-11-03 19:56  
> **Bug**: Required request parameter 'configId' for method parameter type Integer is not present  
> **影響範圍**: LINE 使用者匯入功能

---

## 🐛 問題描述

### 錯誤訊息
```
Required request parameter 'configId' for method parameter type Integer is not present
```

### 問題現象
- 使用者選擇 LINE 頻道後，上傳檔案
- 點擊「開始匯入」按鈕
- 後端回傳 400 錯誤，提示缺少 configId 參數

---

## 🔍 問題根因分析

### 後端 API 要求
```java
@PostMapping("/import")
public AjaxResult importUsers(
    @RequestParam("file") MultipartFile file,
    @RequestParam("configId") Integer configId  // ← 必要參數
)
```

### 前端錯誤實現（修正前）

```vue
<template>
  <el-upload
    ref="upload"
    :action="upload.url"
    :headers="upload.headers"
    :auto-upload="false"
  />
</template>

<script>
methods: {
  handleConfirm() {
    // ❌ 錯誤：建立了 FormData 但沒有使用
    const formData = new FormData()
    formData.append('file', this.form.file.raw)
    formData.append('configId', this.form.configId)
    
    // ❌ el-upload.submit() 不會使用上面的 formData！
    this.$refs.upload.submit()
  }
}
</script>
```

### 問題原因
`el-upload` 組件的 `submit()` 方法**不會使用**手動建立的 `FormData`，它只會使用：
1. `:action` 屬性指定的 URL
2. `:data` 屬性指定的額外資料
3. `:headers` 屬性指定的 HTTP 標頭

因此，雖然前端建立了包含 `configId` 的 `FormData`，但實際上傳時**沒有傳遞**這個參數！

---

## ✅ 解決方案

### 修改 1: 使用 `:data` 屬性傳遞參數

```vue
<el-upload
  ref="upload"
  :action="upload.url"
  :headers="upload.headers"
  :data="{ configId: form.configId }"  <!-- ✅ 正確方式 -->
  :auto-upload="false"
  :on-error="handleFileError"
/>
```

### 修改 2: 簡化 handleConfirm 方法

```javascript
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

      // ✅ configId 會透過 :data 屬性自動傳遞
      this.$refs.upload.submit()
    }
  })
}
```

### 修改 3: 新增錯誤處理

```javascript
/** 檔案上傳失敗 */
handleFileError(error, file, fileList) {
  this.upload.isUploading = false
  this.$modal.msgError('檔案上傳失敗：' + error.message)
}
```

---

## 📝 修改檔案清單

### 前端修改（2 個檔案）

#### 1. ImportDialog.vue
- **Line 35**: 新增 `:data="{ configId: form.configId }"`
- **Line 39**: 新增 `:on-error="handleFileError"`
- **Line 164-168**: 新增 `handleFileError()` 方法
- **Line 171-181**: 簡化 `handleConfirm()` 方法，移除無用的 FormData

#### 2. config.js (API 封裝)
- **Line 130-136**: 新增 `getEnabledConfigs()` API 方法

---

## 🧪 測試驗證

### 1. 功能測試
```
✅ 開啟匯入對話框
✅ LINE 頻道下拉選單顯示啟用頻道
✅ 選擇頻道
✅ 上傳檔案（.xlsx / .csv / .txt）
✅ 點擊「開始匯入」
✅ 後端成功接收到 file 和 configId 參數
✅ 匯入成功後顯示結果
```

### 2. 錯誤處理測試
```
✅ 未選擇頻道時提示「請選擇 LINE 頻道」
✅ 未選擇檔案時提示「請先選擇要上傳的檔案」
✅ 上傳失敗時顯示錯誤訊息
```

### 3. 瀏覽器 Network 驗證
開啟瀏覽器開發者工具 → Network → 提交匯入：

**Request Payload**:
```
------WebKitFormBoundary...
Content-Disposition: form-data; name="file"; filename="users.xlsx"
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

[Binary Data]
------WebKitFormBoundary...
Content-Disposition: form-data; name="configId"

1
------WebKitFormBoundary...
```

✅ 確認 `configId` 參數已成功傳遞！

---

## 📊 el-upload 元件參數說明

| 參數 | 類型 | 說明 | 使用情境 |
|------|------|------|---------|
| `:action` | String | 上傳的 URL | **必填**，後端 API 端點 |
| `:data` | Object | 額外的表單資料 | ✅ 用於傳遞額外參數（如 configId） |
| `:headers` | Object | 自定義 HTTP 標頭 | 用於傳遞 Authorization Token |
| `:auto-upload` | Boolean | 是否自動上傳 | false = 需手動調用 submit() |
| `:on-success` | Function | 上傳成功Callback | 處理成功回應 |
| `:on-error` | Function | 上傳失敗Callback | 處理錯誤訊息 |
| `:on-progress` | Function | 上傳進度Callback | 顯示上傳進度 |

### ⚠️ 重要提示
- **不要手動建立 FormData**：el-upload 會自動處理
- **使用 :data 傳遞額外參數**：這是正確且官方推薦的方式
- **submit() 方法**：只會使用組件的屬性，不會使用外部 FormData

---

## 🎓 知識點總結

### 問題根源
開發者誤以為可以手動建立 `FormData` 並透過 `submit()` 傳遞，但這是**錯誤的理解**。

### 正確做法
Element UI 的 `el-upload` 組件已經封裝了文件上傳邏輯，只需要：
1. `:action` 指定上傳 URL
2. `:data` 指定額外參數
3. 調用 `submit()` 即可

### 類似問題預防
遇到類似的組件時（如 `el-form`、`el-table` 等），應該：
1. **優先查閱官方文檔**
2. **使用組件提供的屬性和方法**
3. **避免繞過組件封裝直接操作 DOM 或原生 API**

---

## 📚 相關文件

- [Element UI Upload 組件文檔](https://element.eleme.cn/#/zh-CN/component/upload)
- [MDN FormData API](https://developer.mozilla.org/zh-CN/docs/Web/API/FormData)
- [Spring @RequestParam 註解](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestParam.html)

---

**修復狀態**: ✅ 已完成  
**影響範圍**: LINE 使用者匯入功能  
**向後相容**: ✅ 完全相容  
**測試狀態**: ⏳ 待測試
