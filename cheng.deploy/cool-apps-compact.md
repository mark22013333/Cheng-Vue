---
trigger: manual
description: 
globs: 
---

# CoolApps 專案開發規範

> **最後更新**: 2025-12-07（Vue 3 升級完成）  
> **完整文件**: `/docs/Development/` 目錄包含詳細規範

---

## 📦 技術棧

### 前端（Vue 3 生態系）
- **Vue 3.5.24** + Composition API | **Element Plus 2.11.8**（繁體中文）
- **Pinia 2.3.1**（狀態管理） | **Vue Router 4.6.3**（路由名稱必須唯一）
- **Vite 5.4.21**（環境變數：`import.meta.env.VITE_APP_*`）

### 後端
- **Spring Boot 3.5.4** + Java 17 | **MyBatis** + MySQL
- **執行緒池**（`ThreadPoolTaskExecutor` + `TraceTaskDecorator`）

---

## AI 協作規範（最優先）

- ❌ 不要產生 Git Commit Message
- ❌ 不要產生總結文件（除非用戶明確要求）
- ✅ 只需簡單說明修改內容

---

## ⭐⭐⭐ 核心規範（必須遵守）

### 1. 強制使用 Enum

```java
// ❌ 禁止
if (status.equals("0")) { ... }

// ✅ 必須
if (status == Status.NORMAL) { ... }

// Enum 標準範本
public enum Status {
    NORMAL("0", "正常"), DISABLE("1", "停用");
    private final String code;
    
    @JsonValue
    public String getCode() { return code; }
    
    public static Status getByCode(String code) {
        return Arrays.stream(values())
            .filter(e -> e.getCode().equals(code))
            .findFirst().orElse(null);
    }
}
```

---

### 2. 操作日誌記錄追溯資訊

**必須包含：人、物、單號、數量、時間**

```java
@Log(title = "遺失物品", businessType = BusinessType.UPDATE)
public AjaxResult lostItem(@RequestBody LostRequest request) {
  // 1. 先查詢資訊
  InvBorrow borrow = invBorrowService.selectById(request.getBorrowId());
  
  // 2. 執行邏輯
  int result = invBorrowService.lostItem(request);
  
  // 3. 記錄到 json_result
  AjaxResult ajaxResult = toAjax(result);
  ajaxResult.put("itemName", borrow.getItemName());      // 物品
  ajaxResult.put("borrowerName", borrow.getBorrowerName());  // 人
  ajaxResult.put("borrowNo", borrow.getBorrowNo());      // 單號
  ajaxResult.put("quantity", request.getQuantity());     // 數量
  
  return ajaxResult;
}
```

---

### 3. 冗餘欄位設計

**歷史記錄不可變更和刪除，物品刪除後仍需顯示**

```java
// ✅ 建立時儲存冗餘欄位
InvBorrow borrow = new InvBorrow();
borrow.setItemId(item.getItemId());
borrow.setItemName(item.getItemName());  // 冗餘
borrow.setItemCode(item.getItemCode());  // 冗餘
```

```xml
<!-- ✅ 優先查詢冗餘欄位（無需 JOIN） -->
<select id="selectBorrowList">
  SELECT borrow_id, item_name, item_code FROM inv_borrow
</select>

<!-- ⚠️ 需要最新資訊時使用 LEFT JOIN -->
<select id="selectWithLatest">
  SELECT b.*, COALESCE(i.item_name, b.item_name) AS current_name
  FROM inv_borrow b
  LEFT JOIN inv_item i ON b.item_id = i.item_id
</select>
```

---

## 🎯 Vue 3 前端規範

### 1. Composition API（主要開發方式）

```vue
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'

const loading = ref(false)
const form = reactive({ username: '', password: '' })

onMounted(() => {
  loadData()
})
</script>
```

---

### 2. Pinia 狀態管理（統一使用）

```javascript
// 定義 store
export const useUserStore = defineStore('user', {
  state: () => ({ token: '', userInfo: {} }),
  actions: {
    setToken(token) { this.token = token }
  }
})

// 使用 store
import { useUserStore } from '@/store/modules/user'
const userStore = useUserStore()
userStore.setToken('xxx')
```

---

### 3. 路由名稱必須唯一

```javascript
// ❌ 錯誤：衝突導致 404
{ name: 'User', path: '/system/user' }
{ name: 'User', path: '/line/user' }

// ✅ 正確：使用模組前綴
{ name: 'SystemUser', path: '/system/user' }
{ name: 'LineUser', path: '/line/user' }
```

---

### 4. 前端狀態參數處理

```javascript
// 後端返回 → 前端顯示
let statusValue = '1'
if (response.data.status) {
  const status = response.data.status
  statusValue = typeof status === 'object' ? String(status.code) : String(status)
}

// 前端提交 → 後端接收
const submitData = {
  statusCode: parseInt(form.status),
  isDefaultCode: form.isDefault ? 1 : 0,
  status: undefined  // 移除避免衝突
}
```

**Element Plus 元件規範**：
```vue
<!-- el-radio-group: label 必須是字串 -->
<el-radio-group v-model="form.status">
  <el-radio label="0">停用</el-radio>
  <el-radio label="1">正常</el-radio>
</el-radio-group>

<!-- el-switch: 使用 active-value -->
<el-switch v-model="form.isDefault" :active-value="true" :inactive-value="false" />
```

---

### 5. Vite 環境變數

```javascript
// ❌ Vue 2（已棄用）
process.env.VUE_APP_BASE_API

// ✅ Vue 3 + Vite
import.meta.env.VITE_APP_BASE_API
```

---

### 6. Element Plus 繁體中文

```javascript
// main.js
import locale from 'element-plus/es/locale/lang/zh-tw'
app.use(ElementPlus, { locale: locale })
```

---

## 🔧 後端進階規範

### 1. 執行緒池使用（必須設定 TraceTaskDecorator）

```java
// ✅ 使用現有執行緒池
@Autowired
@Qualifier("threadPoolTaskExecutor")
private ThreadPoolTaskExecutor taskExecutor;

taskExecutor.execute(() -> {
  // TraceTaskDecorator 自動傳遞 traceId
  log.info("traceId: {}", TraceUtils.getTraceId());
});

// ✅ 建立新執行緒池
@Bean(name = "customTaskExecutor")
public ThreadPoolTaskExecutor customTaskExecutor() {
  ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
  executor.setTaskDecorator(new TraceTaskDecorator());  // ⚠️ 必須
  return executor;
}
```

---

### 2. 備用方案優先級（Redis > DB > 預設值）

```java
private String getUsername() {
  try {
    return SecurityUtils.getUsername();  // 優先
  } catch (Exception e) {
    // 備用 1: Redis
    String username = redisCache.getCacheObject("current_user");
    if (StringUtils.isNotEmpty(username)) return username;
    
    // 備用 2: DB
    // ...
    
    // 最終：預設值
    return "system";
  }
}
```

---

### 3. 效能優化

```java
// ✅ 必須使用分頁
@GetMapping("/list")
public TableDataInfo list(SysUser user) {
    startPage();  // 必須
    return getDataTable(userService.selectUserList(user));
}

// ✅ 快取熱點資料
String value = redisCache.getCacheObject(cacheKey);
if (StringUtils.isEmpty(value)) {
    value = mapper.selectByKey(key);
    redisCache.setCacheObject(cacheKey, value, 30, TimeUnit.MINUTES);
}

// ✅ 防止重複提交
@PostMapping
@RepeatSubmit
public AjaxResult add(@RequestBody SysUser user) { ... }
```

---

### 4. 事務與驗證

```java
// ✅ 多表操作必須加事務
@Transactional(rollbackFor = Exception.class)
public int borrowItem(InvBorrow borrow) {
    borrowMapper.insert(borrow);
    stockMapper.decreaseQuantity(borrow.getItemId(), borrow.getQuantity());
    return 1;
}

// ✅ 明確的異常訊息
throw new ServiceException(
    String.format("分類「%s」正被 %d 個物品使用，無法刪除", categoryName, count)
);

// ✅ Controller 層驗證
@PostMapping
public AjaxResult add(@Valid @RequestBody SysUser user) { ... }
```

---

## ⚠️ 常見陷阱

### Vue 3 陷阱
```javascript
// ❌ 路由名稱重複 → 404
{ name: 'User', ... }, { name: 'User', ... }

// ❌ 環境變數錯誤
process.env.VUE_APP_BASE_API  // Vue 2

// ✅ 正確
import.meta.env.VITE_APP_BASE_API  // Vue 3
```

---

### 後端陷阱

```java
// ❌ 執行緒池無法取得 SecurityContext
ExecutorService executor = Executors.newFixedThreadPool(10);

// ✅ 使用 Spring 管理的執行緒池
@Autowired private ThreadPoolTaskExecutor taskExecutor;

// ❌ SSE 端點 401
@GetMapping("/subscribe")
public SseEmitter subscribe() { }

// ✅ 必須加 @Anonymous
@Anonymous
@GetMapping("/subscribe")
public SseEmitter subscribe() { }

// ❌ 圖片路徑錯誤
return "http://localhost:8080/profile/upload/xxx.jpg";

// ✅ 返回相對路徑
return "/profile/upload/xxx.jpg";

// ❌ Migration 版本號衝突
# 直接建立 V20，但當前已有 V22

// ✅ 先檢查當前最高版本
ls -la cheng-admin/src/main/resources/db/migration/
# 建立 V23__xxx.sql
```

---

### 其他陷阱

```java
// ❌ Long 比較
if (userId1 == userId2) { ... }
// ✅ 正確
if (Objects.equals(userId1, userId2)) { ... }

// ❌ Spring Bean 成員變數
@Service
public class UserService {
    private SysUser currentUser;  // 執行緒不安全
}
// ✅ 使用區域變數

// ❌ List 判空
if (list != null) { list.get(0); }
// ✅ 正確
if (StringUtils.isNotEmpty(list)) { list.get(0); }
```

```xml
<!-- ❌ MyBatis 動態 SQL -->
<if test="status == '0'">

<!-- ✅ 正確 -->
<if test="status != null and '0'.equals(status)">
```

---

## 🔐 環境配置

```bash
# Jasypt 加密（啟動必須）
mvn spring-boot:run \
  -Dspring-boot.run.profiles=local \
  -Djasypt.encryptor.password=diDsd]3FsGO@4dido

# 環境
# Local: /Users/cheng/cool-logs, /Users/cheng/uploadPath
# PROD: /opt/cool-apps/logs, /opt/cool-apps/uploadFile
```

---

## ✅ 開發前檢查清單

### 核心規範（必須）
- [ ] 使用 Enum 替代魔法數字
- [ ] 重要操作記錄追溯資訊（人、物、單號、數量）
- [ ] 歷史記錄儲存冗餘欄位
- [ ] 所有列表使用分頁
- [ ] 多表操作加上事務
- [ ] 異常訊息清晰明確

### Vue 3 前端
- [ ] 使用 Composition API
- [ ] 使用 Pinia 狀態管理
- [ ] 路由名稱唯一
- [ ] 環境變數使用 `import.meta.env`
- [ ] Element Plus 設定繁體中文
- [ ] el-radio-group label 為字串
- [ ] 提交使用 xxxCode 欄位

### 後端執行緒池
- [ ] 使用 Spring 管理的 ThreadPoolTaskExecutor
- [ ] 新執行緒池設定 TraceTaskDecorator
- [ ] 提供備用方案（Redis > DB > 預設值）

### 其他
- [ ] 熱點資料有快取
- [ ] 避免 N+1 查詢
- [ ] SSE 端點加 @Anonymous
- [ ] 圖片使用相對路徑
- [ ] Migration 版本號檢查

---

## 📖 完整文件

- `/docs/Development/RULE_00_INDEX.md` - 索引
- `/docs/Development/RULE_02_ENUM_STANDARDS.md` - Enum 規範 ⭐
- `/docs/Development/RULE_13_FRONTEND_STANDARDS.md` - 前端規範 ⭐
- `/docs/Development/RULE_04_PERFORMANCE.md` - 效能優化
- `/docs/Development/RULE_11_CODE_REVIEW.md` - 檢查清單

---

**記住**：這些規範基於實際開發經驗，務必遵守以確保程式碼品質！
