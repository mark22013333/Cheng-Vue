---
trigger: manual
description: 
globs: 
---

# CoolApps 專案開發規範

> **最後更新**: 2025-12-07（Vue 3 升級完成）  
> **完整文件**: `/docs/Development/` 目錄包含 13 份以上的詳細規範

---

## 📦 技術棧概覽

### 前端技術棧（Vue 3 生態系）
- ✅ **Vue 3.5.24** + **Composition API**（主要開發方式）
- ✅ **Element Plus 2.11.8**（預設繁體中文 `zh-tw`）
- ✅ **Vue Router 4.6.3**（路由名稱必須唯一）
- ✅ **Pinia 2.3.1**（統一狀態管理）
- ✅ **Vite 5.4.21**（環境變數：`import.meta.env.VITE_APP_*`）
- ✅ **Composables**（可重用邏輯，如 `useTableConfig`）

### 後端技術棧
- ✅ **Spring Boot 3.5.4** + **Java 17**
- ✅ **MyBatis 3.0.4** + **MySQL 8.2.0**
- ✅ **Redis** + **Flyway** + **Jasypt**
- ✅ **執行緒池**（`ThreadPoolTaskExecutor` + `TraceTaskDecorator`）

---

## AI 協作規範（最優先遵守）

### 回應格式
- ❌ **不要產生** Git Commit Message
- ❌ **不要產生**總結文件（除非用戶明確要求）
- ✅ **只需簡單說明**做了哪些修改即可

### 原則
- 完成修改後，簡潔說明修改內容即可
- 避免產生未經測試的總結文件
- 等用戶確認功能正常後，再由用戶決定是否需要文件

---

## ⭐⭐⭐ 核心規範（必須遵守）

### 1. 強制使用 Enum（最重要！）

❌ **禁止使用魔法數字或字串**:
```java
if (status.equals("0")) { ... }  // 不要這樣寫
if (type == 1) { ... }
```

✅ **必須使用 Enum**:
```java
if (status == Status.NORMAL) { ... }  // 清晰明確
if (channelType == ChannelType.MAIN) { ... }
```

**Enum 標準範本**:
```java
public enum Status {
    NORMAL("0", "正常"),
    DISABLE("1", "停用");

    private final String code;
    private final String description;

    Status(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static Status getByCode(String code) {
        return Arrays.stream(values())
            .filter(e -> e.getCode().equals(code))
            .findFirst()
            .orElse(null);
    }
}
```

---

### 2. 操作日誌記錄追溯資訊（極其重要！）

**重要操作必須記錄完整追溯資訊**：

```java
@Log(title = "遺失物品", businessType = BusinessType.UPDATE)
public AjaxResult lostItem(@RequestBody LostRequest request) {
  // 1. 先查詢資訊用於日誌
  InvBorrow borrow = invBorrowService.selectById(request.getBorrowId());
  String itemName = borrow != null ? borrow.getItemName() : "未知物品";
  String borrowerName = borrow != null ? borrow.getBorrowerName() : "未知借用人";
  String borrowNo = borrow != null ? borrow.getBorrowNo() : "未知單號";
  
  // 2. 執行業務邏輯
  int result = invBorrowService.lostItem(request);
  
  // 3. 記錄到 json_result（@Log 會自動記錄）
  AjaxResult ajaxResult = toAjax(result);
  ajaxResult.put("itemName", itemName);          // ✅ 物品名稱
  ajaxResult.put("quantity", request.getQuantity());  // ✅ 數量
  ajaxResult.put("borrowerName", borrowerName);  // ✅ 借用人
  ajaxResult.put("borrowNo", borrowNo);          // ✅ 關聯單號
  
  log.info("遺失物品：{}，數量：{}，借用人：{}", itemName, request.getQuantity(), borrowerName);
  
  return ajaxResult;
}
```

**追溯資訊必須包含：**
- ✅ **人**：操作人、借用人
- ✅ **物**：物品名稱、物品編號
- ✅ **單號**：借出單號（方便追蹤）
- ✅ **數量**：操作數量
- ✅ **時間**：操作時間（@Log 自動記錄）

---

### 3. 冗餘欄位設計（歷史記錄不可變更和刪除）

**歷史記錄表必須儲存冗餘欄位**：

```java
// ✅ 建立借出記錄時，儲存物品冗餘欄位
public void createBorrow(BorrowRequest request) {
  // 查詢物品資訊
  InvItem item = invItemService.selectById(request.getItemId());
  
  InvBorrow borrow = new InvBorrow();
  borrow.setItemId(item.getItemId());
  borrow.setItemName(item.getItemName());  // ✅ 冗餘欄位
  borrow.setItemCode(item.getItemCode());  // ✅ 冗餘欄位
  
  invBorrowMapper.insert(borrow);
}

// 📌 目的：即使物品被刪除，借出記錄仍能顯示物品名稱和編號
```

**查詢時的策略**：

```xml
<!-- ✅ 優先：直接查詢冗餘欄位（無需 JOIN，效能更好） -->
<select id="selectBorrowList" resultMap="BorrowResult">
  SELECT 
    borrow_id,
    item_name,     <!-- 冗餘欄位 -->
    item_code,     <!-- 冗餘欄位 -->
    borrower_name
  FROM inv_borrow
  WHERE del_flag = '0'
</select>

<!-- ⚠️ 如果需要物品最新資訊，則使用 LEFT JOIN + COALESCE -->
<select id="selectBorrowWithItemInfo" resultMap="BorrowResult">
  SELECT 
    b.borrow_id,
    b.item_name,           <!-- 冗餘欄位（刪除後顯示） -->
    COALESCE(i.item_name, b.item_name) AS current_item_name,  <!-- 最新名稱 -->
    b.borrower_name
  FROM inv_borrow b
  LEFT JOIN inv_item i ON b.item_id = i.item_id AND i.del_flag = '0'
  WHERE b.del_flag = '0'
</select>
```

**重點理念：**
- ✅ 冗餘欄位是為了**物品刪除後仍能顯示歷史記錄**
- ✅ 歷史記錄**不允許被改變和刪除**
- ✅ 如果需要**統一查詢來源**（顯示最新資訊），則使用 **LEFT JOIN + COALESCE**
- ✅ 如果只是**顯示歷史**，直接查詢冗餘欄位即可（效能更好）

---

## 🎯 Vue 3 前端開發規範

### 1. Composition API（主要開發方式）

```vue
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'

// ✅ 使用 ref 定義響應式變數
const loading = ref(false)
const total = ref(0)

// ✅ 使用 reactive 定義響應式物件
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null
})

const form = reactive({
  username: '',
  password: ''
})

// ✅ 計算屬性
const filteredList = computed(() => {
  return dataList.value.filter(item => item.status === '1')
})

// ✅ 生命週期鉤子
onMounted(() => {
  loadData()
})

// ✅ 方法定義
function handleQuery() {
  queryParams.pageNum = 1
  loadData()
}
</script>
```

**與 Vue 2 Options API 對照**：

| Vue 2 Options API | Vue 3 Composition API |
|-------------------|----------------------|
| `data()` | `ref()`, `reactive()` |
| `computed` | `computed()` |
| `methods` | 直接定義函數 |
| `mounted()` | `onMounted()` |
| `watch` | `watch()`, `watchEffect()` |
| `this.$refs` | `ref()` + `template ref` |

---

### 2. Pinia 狀態管理（統一使用）

```javascript
// ✅ 定義 store (src/store/modules/user.js)
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: {},
    roles: [],
    permissions: []
  }),
  
  getters: {
    isAdmin: (state) => state.roles.includes('admin')
  },
  
  actions: {
    setToken(token) {
      this.token = token
    },
    
    setUserInfo(userInfo) {
      this.userInfo = userInfo
    },
    
    async getUserInfo() {
      const res = await getInfo()
      this.userInfo = res.user
      this.roles = res.roles
      this.permissions = res.permissions
    }
  }
})

// ✅ 使用 store（在組件中）
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// 讀取狀態
console.log(userStore.token)
console.log(userStore.isAdmin)

// 修改狀態
userStore.setToken('xxx')

// 呼叫 action
await userStore.getUserInfo()
```

---

### 3. Composables（可重用邏輯）

```javascript
// ✅ 定義 composable (src/composables/useTableConfig.js)
import { getTableConfig, saveTableConfig } from '@/api/system/tableConfig'

export function useTableConfig() {
  async function loadConfig(pageKey, defaultColumns) {
    try {
      const response = await getTableConfig(pageKey)
      if (!response.data) {
        return { ...defaultColumns }
      }
      const savedConfig = JSON.parse(response.data)
      return mergeConfig(defaultColumns, savedConfig)
    } catch (error) {
      console.error('載入表格欄位配置失敗：', error)
      return { ...defaultColumns }
    }
  }

  async function saveConfig(pageKey, columns) {
    try {
      await saveTableConfig(pageKey, columns)
    } catch (error) {
      console.error('儲存表格欄位配置失敗：', error)
      throw error
    }
  }

  function mergeConfig(defaultColumns, savedConfig) {
    const merged = {}
    for (const key in defaultColumns) {
      if (savedConfig.hasOwnProperty(key)) {
        merged[key] = {
          ...defaultColumns[key],
          visible: savedConfig[key].visible
        }
      } else {
        merged[key] = {
          ...defaultColumns[key],
          visible: true
        }
      }
    }
    return merged
  }

  return {
    loadConfig,
    saveConfig,
    mergeConfig
  }
}

// ✅ 使用 composable（在組件中）
import { useTableConfig } from '@/composables/useTableConfig'

const { loadConfig, saveConfig } = useTableConfig()

onMounted(async () => {
  const savedConfig = await loadConfig('system_user', defaultColumns)
  Object.assign(columns, savedConfig)
})
```

---

### 4. 路由名稱必須唯一（避免衝突）

```javascript
// ❌ 錯誤：重複名稱會導致 404
const routes = [
  {
    path: '/system/user',
    name: 'User',  // 衝突！
    component: () => import('@/views/system/user/index')
  },
  {
    path: '/line/user',
    name: 'User',  // 衝突！
    component: () => import('@/views/line/user/index')
  }
]

// ✅ 正確：使用模組前綴或完整路徑命名
const routes = [
  {
    path: '/system/user',
    name: 'SystemUser',  // ✅ 唯一
    component: () => import('@/views/system/user/index')
  },
  {
    path: '/line/user',
    name: 'LineUser',  // ✅ 唯一
    component: () => import('@/views/line/user/index')
  }
]
```

---

### 5. 前端狀態參數處理（極其重要！）

#### 後端返回 → 前端顯示
```javascript
// 處理多種可能的格式
let statusValue = '1'
if (response.data.status) {
  const status = response.data.status
  if (typeof status === 'object' && status.code !== undefined) {
    statusValue = String(status.code)  // 枚舉物件
  } else if (typeof status === 'number') {
    statusValue = String(status)  // 數字
  } else if (typeof status === 'string') {
    statusValue = status === 'ENABLE' ? '1' : '0'  // 字串
  }
}
this.form.status = statusValue
```

#### 前端提交 → 後端接收
```javascript
const submitData = {
  ...this.form,
  statusCode: parseInt(this.form.status),  // 字串 → 數字
  isDefaultCode: this.form.isDefault ? 1 : 0,  // 布林 → 數字
  status: undefined,  // 移除避免衝突
  isDefault: undefined
}
```

#### Element Plus 元件規範
- `el-radio-group`: label 必須是字串 `label="1"`
- `el-switch`: 綁定布林值 `:active-value="true"`
- `el-select`: value 使用 Enum code `value="MAIN"`

**完整範例**：

```vue
<template>
  <!-- ✅ el-radio-group: label 必須是字串 -->
  <el-radio-group v-model="form.status">
    <el-radio label="0">停用</el-radio>
    <el-radio label="1">正常</el-radio>
  </el-radio-group>

  <!-- ✅ el-switch: 使用 active-value / inactive-value -->
  <el-switch 
    v-model="form.isDefault"
    :active-value="true"
    :inactive-value="false"
  />

  <!-- ✅ el-select: value 使用 Enum code -->
  <el-select v-model="form.channelType">
    <el-option label="主頻道" value="MAIN" />
    <el-option label="測試頻道" value="TEST" />
  </el-select>

  <!-- ✅ el-checkbox: true-label / false-label -->
  <el-checkbox 
    v-model="form.isPublic"
    true-label="1"
    false-label="0"
  >
    公開
  </el-checkbox>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({
  status: '0',        // 字串
  isDefault: false,   // 布林
  channelType: 'MAIN',  // 字串
  isPublic: '0'       // 字串
})

// 提交時轉換
function handleSubmit() {
  const submitData = {
    statusCode: parseInt(form.status),
    isDefaultCode: form.isDefault ? 1 : 0,
    channelType: form.channelType,
    isPublicCode: parseInt(form.isPublic)
  }
  // 發送請求...
}
</script>
```

---

### 6. Vite 環境變數

```javascript
// ❌ Vue 2 + Vue CLI（已棄用）
const baseURL = process.env.VUE_APP_BASE_API
const isDev = process.env.NODE_ENV === 'development'

// ✅ Vue 3 + Vite
const baseURL = import.meta.env.VITE_APP_BASE_API
const isDev = import.meta.env.DEV  // Vite 內建
const isProd = import.meta.env.PROD  // Vite 內建

// ✅ 實際使用範例
// src/utils/request.js
import axios from 'axios'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,  // ✅
  timeout: 10000
})
```

**環境變數檔案**：
- `.env.development` → `VITE_APP_BASE_API=/dev-api`
- `.env.production` → `VITE_APP_BASE_API=/prod-api`

---

### 7. Element Plus 預設繁體中文

```javascript
// src/main.js
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import locale from 'element-plus/es/locale/lang/zh-tw'  // ✅ 繁體中文

const app = createApp(App)

// ✅ 統一預設繁體中文
app.use(ElementPlus, {
  locale: locale,  // ✅ 必須設定
  size: 'default'
})

app.mount('#app')
```

---

## 🔧 後端進階規範

### 1. 執行緒池使用規範（必須設定 TraceTaskDecorator）

**使用現有執行緒池**：

```java
// ✅ 正確：使用 Spring Bean
@Autowired
@Qualifier("threadPoolTaskExecutor")
private ThreadPoolTaskExecutor taskExecutor;

public void asyncTask() {
  taskExecutor.execute(() -> {
    // TraceTaskDecorator 會自動傳遞 traceId
    String traceId = TraceUtils.getTraceId();
    log.info("非同步任務執行，traceId: {}", traceId);
    
    // 執行業務邏輯...
  });
}
```

**建立新執行緒池（不同用途）**：

```java
// ✅ 如果需要建立不同用途的執行緒池
@Configuration
public class CustomThreadPoolConfig {
  
  @Bean(name = "emailTaskExecutor")
  public ThreadPoolTaskExecutor emailTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("email-task-");
    
    // ⚠️ 必須設定 TraceId 裝飾器
    executor.setTaskDecorator(new TraceTaskDecorator());
    
    executor.initialize();
    return executor;
  }
}
```

---

### 2. 備用方案優先級（Redis > DB > 預設值）

```java
/**
 * 取得使用者名稱（含備用方案）
 * 優先級：SecurityContext > Redis > DB > 預設值
 */
private String getUsername() {
  try {
    // 優先：從 SecurityContext 取得
    return SecurityUtils.getUsername();
  } catch (Exception e) {
    log.warn("無法從 SecurityContext 取得使用者，嘗試從 Redis 取得");
    
    try {
      // 備用 1：從 Redis 取得
      String username = redisCache.getCacheObject("current_user:" + getRequestId());
      if (StringUtils.isNotEmpty(username)) {
        log.info("從 Redis 取得使用者：{}", username);
        return username;
      }
    } catch (Exception ex) {
      log.warn("無法從 Redis 取得使用者，嘗試從 DB 取得", ex);
    }
    
    try {
      // 備用 2：從 DB 取得（如果有 userId 或其他識別）
      Long userId = getCurrentUserId();
      if (userId != null) {
        SysUser user = userMapper.selectUserById(userId);
        if (user != null) {
          log.info("從 DB 取得使用者：{}", user.getUserName());
          return user.getUserName();
        }
      }
    } catch (Exception ex) {
      log.warn("無法從 DB 取得使用者", ex);
    }
    
    // 最終：使用系統帳號
    log.warn("所有方式都無法取得使用者，使用系統帳號");
    return "system";
  }
}
```

---

### 3. 效能優化（支援 500 人線上、100 人並發）

#### 必須使用分頁
```java
@GetMapping("/list")
public TableDataInfo list(SysUser user) {
    startPage();  // 必須！
    List<SysUser> list = userService.selectUserList(user);
    return getDataTable(list);
}
```

#### 快取熱點資料
```java
// 查詢前先查快取
String cacheKey = "config:" + configKey;
String value = redisCache.getCacheObject(cacheKey);
if (StringUtils.isNotEmpty(value)) {
    return value;
}
// 查詢資料庫並寫入快取
value = mapper.selectByKey(configKey);
redisCache.setCacheObject(cacheKey, value, 30, TimeUnit.MINUTES);
```

#### 防止重複提交
```java
@PostMapping
@RepeatSubmit  // 5 秒內防止重複提交
public AjaxResult add(@RequestBody SysUser user) {
    return toAjax(userService.insertUser(user));
}
```

---

### 4. 異常處理規範

✅ **統一使用 ServiceException**:
```java
// 明確的異常訊息
throw new ServiceException(
    String.format("分類「%s」正被 %d 個物品使用，無法刪除", 
        categoryName, itemCount)
);
```

❌ **不要使用模糊訊息**:
```java
throw new ServiceException("刪除失敗");  // 太模糊
```

---

### 5. 事務管理規範

✅ **多表操作必須加事務**:
```java
@Transactional(rollbackFor = Exception.class)
public int borrowItem(InvBorrow borrow) {
    borrowMapper.insert(borrow);
    stockMapper.decreaseQuantity(borrow.getItemId(), borrow.getQuantity());
    stockRecordMapper.insert(buildRecord(borrow));
    return 1;
}
```

⚠️ **注意事項**:
- @Transactional 只對 public 方法有效
- 同類別內部呼叫事務不生效（需注入自己）
- 捕獲異常後要重新拋出或手動回滾

---

### 6. 資料驗證規範

#### Controller 層
```java
@PostMapping
public AjaxResult add(@Valid @RequestBody SysUser user) {
    return toAjax(userService.insertUser(user));
}
```

#### Service 層
```java
// 檢查唯一性
if (userMapper.selectByUserName(user.getUserName()) != null) {
    throw new ServiceException("用戶名稱已存在");
}

// 檢查關聯資料
int itemCount = itemMapper.countByCategoryId(categoryId);
if (itemCount > 0) {
    throw new ServiceException(
        String.format("分類正被 %d 個物品使用，無法刪除", itemCount)
    );
}
```

---

### 7. API 設計規範

#### RESTful 規範
- GET: 查詢 `/system/user/list`
- POST: 新增 `/system/user`
- PUT: 修改 `/system/user`
- DELETE: 刪除 `/system/user/{ids}`

#### 統一回應格式
```java
@GetMapping("/{userId}")
public AjaxResult getInfo(@PathVariable Long userId) {
    SysUser user = userService.selectUserById(userId);
    return AjaxResult.success(user);
}
```

#### 時間格式
```java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
private Date createTime;
```

---

## ⚠️ 常見陷阱

### 1. Vue Router 4 路由名稱衝突
```javascript
// ❌ 錯誤：導致 404
{ name: 'User', path: '/system/user' }
{ name: 'User', path: '/line/user' }

// ✅ 正確：使用唯一名稱
{ name: 'SystemUser', path: '/system/user' }
{ name: 'LineUser', path: '/line/user' }
```

### 2. Vite 環境變數錯誤
```javascript
// ❌ 錯誤：Vue 2 語法
process.env.VUE_APP_BASE_API

// ✅ 正確：Vue 3 + Vite
import.meta.env.VITE_APP_BASE_API
```

### 3. 執行緒池無法取得 SecurityContext
```java
// ❌ 錯誤：直接使用 ExecutorService
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.execute(() -> {
  String username = SecurityUtils.getUsername();  // null!
});

// ✅ 正確：使用 Spring 管理的 ThreadPoolTaskExecutor
@Autowired
private ThreadPoolTaskExecutor taskExecutor;  // 已配置 TraceTaskDecorator

taskExecutor.execute(() -> {
  String username = SecurityUtils.getUsername();  // ✅ 正常
});
```

### 4. MyBatis 動態 SQL
```xml
<!-- ❌ 錯誤 -->
<if test="status == '0'">  <!-- 物件比較 -->

<!-- ✅ 正確 -->
<if test="status != null and '0'.equals(status)">
```

### 5. Long 比較
```java
// ❌ 錯誤
if (userId1 == userId2) { ... }

// ✅ 正確
if (Objects.equals(userId1, userId2)) { ... }
```

### 6. 執行緒安全
```java
// ❌ 危險：Spring Bean 成員變數
@Service
public class UserService {
    private SysUser currentUser;  // 多執行緒會互相覆蓋
}

// ✅ 正確：使用區域變數
public void processUser(Long userId) {
    SysUser currentUser = userMapper.selectById(userId);
}
```

### 7. List 判空
```java
// ❌ 錯誤
if (list != null) {
    list.get(0);  // 可能 IndexOutOfBoundsException
}

// ✅ 正確
if (StringUtils.isNotEmpty(list)) {
    list.get(0);
}
```

### 8. SSE 端點 401 錯誤
```java
// ❌ 錯誤：EventSource 無法攜帶自訂 header
@GetMapping("/subscribe")
public SseEmitter subscribe() { }  // 401 Unauthorized

// ✅ 正確：必須加上 @Anonymous
@Anonymous  // ✅ 必須
@GetMapping(value = "/subscribe/{taskId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter subscribe(@PathVariable String taskId) { }
```

### 9. 圖片路徑錯誤
```java
// ❌ 錯誤：返回完整 URL（外部無法存取）
String imageUrl = "http://localhost:8080/profile/upload/xxx.jpg";

// ✅ 正確：返回相對路徑
String imageUrl = "/profile/upload/xxx.jpg";
```

### 10. Migration 版本號衝突
```bash
# ❌ 錯誤：沒有檢查當前最高版本
# 直接建立 V20__xxx.sql，但實際上已經有 V22

# ✅ 正確：先檢查當前最高版本
ls -la cheng-admin/src/main/resources/db/migration/
# 當前最高：V22__xxx.sql
# 新增：V23__add_new_table.sql
```

---

## 🔐 環境配置

### Jasypt 加密
啟動時必須加上解密密碼：
```bash
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido
```

### 環境切換
- **Local**: `spring.profiles.active=local`
  - LOG: `/Users/cheng/cool-logs`
  - 上傳: `/Users/cheng/uploadPath`

- **PROD**: `spring.profiles.active=prod`
  - LOG: `/opt/cool-apps/logs`
  - 上傳: `/opt/cool-apps/uploadFile`

---

## ✅ 開發前檢查清單

### 核心規範檢查（最重要）
- [ ] ⭐ 是否使用 Enum 替代魔法數字？
- [ ] ⭐ 重要操作是否記錄完整追溯資訊（人、物、單號、數量）？
- [ ] ⭐ 歷史記錄表是否儲存冗餘欄位（item_name, item_code）？
- [ ] ⭐ 所有列表查詢都使用分頁？
- [ ] ⭐ 異常訊息是否清晰明確？
- [ ] ⭐ 多表操作是否加上事務？

### Vue 3 前端檢查
- [ ] 是否使用 **Composition API**（`<script setup>`）？
- [ ] 是否使用 **Pinia** 管理狀態（而非 Vuex）？
- [ ] 路由名稱是否**唯一**（避免衝突）？
- [ ] 環境變數是否使用 `import.meta.env.VITE_APP_*`？
- [ ] Element Plus 是否設定**繁體中文**（`zh-tw`）？
- [ ] el-radio-group 的 label 是否為字串？
- [ ] 後端返回的枚舉物件是否提取 code？
- [ ] 提交時是否使用 xxxCode 欄位名？

### 後端執行緒池檢查
- [ ] 是否使用 Spring 管理的 `ThreadPoolTaskExecutor`？
- [ ] 新建執行緒池是否設定 **TraceTaskDecorator**？
- [ ] 是否提供備用方案（Redis > DB > 預設值）？

### 操作日誌檢查
- [ ] `json_result` 是否包含**關鍵資訊**（人、物、單號）？
- [ ] 日誌訊息是否清晰可追溯？

### 冗餘欄位檢查
- [ ] 查詢是否優先使用**冗餘欄位**（避免 JOIN）？
- [ ] 如需最新資訊，是否使用 **LEFT JOIN + COALESCE**？

### 效能檢查
- [ ] 熱點資料是否有快取？
- [ ] 是否避免 N+1 查詢？
- [ ] 是否有適當的資料庫索引？
- [ ] 慢查詢數是否為 0（< 1 秒）？

### 安全檢查
- [ ] 是否使用參數化查詢（防 SQL 注入）？
- [ ] 密碼是否使用 BCrypt 加密？
- [ ] 日誌是否記錄敏感資訊？
- [ ] SSE 端點是否加上 @Anonymous？

---

## 📖 完整文件位置

詳細規範請查閱：
- `/docs/Development/RULE_00_INDEX.md` - 索引
- `/docs/Development/RULE_02_ENUM_STANDARDS.md` - Enum 規範 ⭐
- `/docs/Development/RULE_13_FRONTEND_STANDARDS.md` - 前端規範 ⭐
- `/docs/Development/RULE_04_PERFORMANCE.md` - 效能優化
- `/docs/Development/RULE_11_CODE_REVIEW.md` - 檢查清單

---

**⭐注意**: 不需要每一次做完都產出總結的文件，應該產出前要先問我，我同意再產出文件，務必記住這件事情，不要浪費Token產出一堆功能沒有測試過後的總結文件，那是沒有意義的動作！！！

**⭐注意**: 除非使用者有特別要求，否則不要建立新的 Markdown 文件來記錄每項變更或總結您的工作。

**記住**: 這些規範基於實際開發經驗總結，務必遵守以確保程式碼品質！
