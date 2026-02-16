# HTTP 路徑處理規範

## 核心原則

**路徑匹配、快取鍵建立等業務邏輯，必須使用不含 context path 的路徑。**

## 背景

專案有兩種部署方式：
- **localhost**：內嵌 Tomcat，context path = `/`
- **VM**：外部 Tomcat，WAR 名稱 `apps.war`，context path = `/apps`

使用 `getRequestURI()`（包含 context path）會導致不同環境下行為不一致。

## API 選擇指南

| 方法 | 用途 | localhost | VM (apps.war) |
|------|------|-----------|---------------|
| `PathUtils.getServletPath()` | 路徑匹配、快取鍵 | `/shop/front` | `/shop/front` |
| `PathUtils.getFullRequestURI()` | 日誌記錄 | `/shop/front` | `/apps/shop/front` |
| `request.getRequestURI()` | **禁止用於匹配** | `/shop/front` | `/apps/shop/front` |

## 正確用法

### Filter shouldNotFilter()

```java
// ❌ 錯誤
String uri = request.getRequestURI();
return !uri.startsWith("/shop/");  // VM 環境：/apps/shop/... → 匹配失敗

// ✅ 正確
return !PathUtils.pathStartsWith(request, "/shop/");
```

### 快取鍵建立

```java
// ❌ 錯誤
String cacheKey = "key:" + request.getRequestURI();  // 不同環境 key 不一致

// ✅ 正確
String cacheKey = "key:" + PathUtils.getServletPath(request);
```

### 路徑白名單

```java
// ✅ 正確
private static final Set<String> PATHS = Set.of("/captchaImage", "/login");
if (PathUtils.isPathInWhitelist(request, PATHS)) { ... }
```

## 自動化檢查

ArchUnit 架構測試（`PathHandlingArchitectureTest`）在 CI 中自動偵測：
- Filter 類別禁止呼叫 `getRequestURI()`
- Interceptor 類別禁止呼叫 `getRequestURI()`

違反規則會導致建置失敗。

## 工具類位置

`com.cheng.common.utils.http.PathUtils`
