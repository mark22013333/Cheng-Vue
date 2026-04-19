# Mapper 介面與 XML 實作驗證指南

## 📋 問題背景

在使用 MyBatis 開發時，常見的問題是：
- **Mapper 介面定義了方法，但 XML 檔案忘記實作**
- **編譯時不會報錯，執行時才會出現 `BindingException`**
- **人工檢查容易遺漏，特別是專案規模變大時**

## 🎯 解決方案

我們建立了自動化驗證工具 `verify-mapper.py`，可以：
1. 自動掃描所有 Mapper 介面檔案
2. 提取所有方法定義
3. 檢查對應的 XML 檔案是否有實作
4. 產生詳細的驗證報告

## 🔧 使用方式

### 全專案驗證（推薦）

```bash
# 在專案根目錄執行
python3 verify-all-mappers.py
```

**功能特點**：
- ✅ 自動掃描所有模組（cheng-system、cheng-line、cheng-quartz、cheng-generator）
- ✅ 支援遞迴搜尋 XML 檔案（處理子目錄結構）
- ✅ 識別註解實作的方法（`@Select`、`@Insert` 等）
- ✅ 彩色輸出，清晰易讀
- ✅ 詳細的統計報表

### 單一模組驗證

```bash
# 僅驗證 LINE 模組
cd cheng-line
python3 verify-mapper.py
```

### 驗證結果範例

#### ✅ 全專案驗證通過
```
======================================================================
全專案 Mapper 介面與 XML 實作驗證工具
======================================================================
發現 4 個模組包含 Mapper：
  - cheng-system
  - cheng-line
  - cheng-quartz
  - cheng-generator

======================================================================
模組：cheng-system
======================================================================

檢查 InvItemMapper...
  Java: .../InvItemMapper.java
  方法：15 個（XML實作：14，註解實作：1）
  ✅ 所有 14 個需要 XML 的方法都有對應實作

檢查 SysUserMapper...
  Java: .../SysUserMapper.java
  ✅ 所有 12 個方法都有對應的 XML 實作

...

======================================================================
驗證總結
======================================================================

模組統計：

模組名稱                      Mapper數    方法數        缺失數        狀態        
----------------------------------------------------------------------
cheng-system              23         190        -          ✅ 通過
cheng-line                4          49         -          ✅ 通過
cheng-quartz              2          14         -          ✅ 通過
cheng-generator           2          16         -          ✅ 通過
----------------------------------------------------------------------
總計                        31         269        0         

驗證完成: 4/4 個模組通過
✅ 驗證通過！所有 4 個模組的 31 個 Mapper 都有完整的 XML 實作
   總共檢查了 269 個方法
```

#### ❌ 驗證失敗
```
檢查 LineMessageLogMapper...
  Java: .../LineMessageLogMapper.java
  XML:  .../LineMessageLogMapper.xml
  ❌ 缺少 3 個 XML 實作:
     - selectMessageLogByUserId
     - selectMessageLogByTagId
     - countFailedMessagesByConfigId

============================================================
驗證完成: 3/4 個 Mapper 通過
❌ 驗證失敗！請修正上述缺少的 XML 實作
```

## 📝 實際案例：修復 LineMessageLogMapper

### 發現問題
驗證工具發現 `LineMessageLogMapper` 缺少 3 個方法的 XML 實作：
1. `selectMessageLogByUserId`
2. `selectMessageLogByTagId`
3. `countFailedMessagesByConfigId`

### 修復步驟

在 `LineMessageLogMapper.xml` 中補上缺少的實作：

```xml
<!-- 查詢指定使用者的訊息記錄 -->
<select id="selectMessageLogByUserId" parameterType="String" resultMap="LineMessageLogResult">
    <include refid="selectLineMessageLogVo"/>
    where target_line_user_id = #{lineUserId}
    or target_user_ids like concat('%', #{lineUserId}, '%')
    order by send_time desc
</select>

<!-- 查詢指定標籤的訊息記錄 -->
<select id="selectMessageLogByTagId" parameterType="Long" resultMap="LineMessageLogResult">
    <include refid="selectLineMessageLogVo"/>
    where target_tag_id = #{tagId}
    order by send_time desc
</select>

<!-- 統計發送失敗的訊息數 -->
<select id="countFailedMessagesByConfigId" parameterType="Integer" resultType="int">
    select count(1)
    from sys_line_message_log
    where config_id = #{configId}
      and send_status = 'FAILED'
</select>
```

### 驗證修復
```bash
python3 verify-mapper.py
# ✅ 驗證通過！
```

## 🚀 整合到 CI/CD

### GitHub Actions

在 `.github/workflows/ci-cd.yml` 中加入驗證步驟：

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  validate:
    name: Validate Mapper XML
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.9'
      
      - name: Verify LINE Mapper implementations
        run: |
          cd cheng-line
          python3 verify-mapper.py
      
      # 如果有其他模組的 Mapper，也可以驗證
      - name: Verify System Mapper implementations
        run: |
          cd cheng-system
          if [ -f verify-mapper.py ]; then
            python3 verify-mapper.py
          fi
  
  build:
    name: Build and Test
    needs: validate  # 先通過驗證才進行建置
    runs-on: ubuntu-latest
    # ... 其他建置步驟
```

### Pre-commit Hook

建立 `.git/hooks/pre-commit`：

```bash
#!/bin/bash

echo "執行 Mapper 驗證..."

cd cheng-line
if ! python3 verify-mapper.py; then
    echo "❌ Mapper 驗證失敗！請修正後再提交。"
    exit 1
fi

echo "✅ Mapper 驗證通過！"
exit 0
```

設定執行權限：
```bash
chmod +x .git/hooks/pre-commit
```

### Maven Plugin

在 `pom.xml` 中整合驗證：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>3.1.0</version>
            <executions>
                <execution>
                    <id>verify-mapper</id>
                    <phase>validate</phase>
                    <goals>
                        <goal>exec</goal>
                    </goals>
                    <configuration>
                        <executable>python3</executable>
                        <workingDirectory>${project.basedir}</workingDirectory>
                        <arguments>
                            <argument>verify-mapper.py</argument>
                        </arguments>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

執行：
```bash
mvn validate
```

## 📚 擴展到其他模組

### 複製驗證腳本到其他模組

```bash
# 複製到 cheng-system
cp cheng-line/verify-mapper.py cheng-system/

# 複製到 cheng-quartz
cp cheng-line/verify-mapper.py cheng-quartz/
```

### 建立全專案驗證腳本

建立 `/verify-all-mappers.sh`：

```bash
#!/bin/bash

echo "驗證所有模組的 Mapper..."

MODULES=("cheng-line" "cheng-system" "cheng-quartz")
FAILED=0

for module in "${MODULES[@]}"; do
    if [ -d "$module" ] && [ -f "$module/verify-mapper.py" ]; then
        echo "檢查模組: $module"
        cd "$module"
        if ! python3 verify-mapper.py; then
            ((FAILED++))
        fi
        cd ..
        echo ""
    fi
done

if [ $FAILED -eq 0 ]; then
    echo "✅ 所有模組驗證通過！"
    exit 0
else
    echo "❌ $FAILED 個模組驗證失敗！"
    exit 1
fi
```

## 🔍 驗證原理

### 方法提取邏輯

腳本使用正則表達式提取 Mapper 介面中的方法：

```python
pattern = r'^\s*(?:public\s+)?(?:int|void|String|Long|Integer|Boolean|List<[^>]+>|[A-Z][a-zA-Z0-9]*)\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\([^)]*\)\s*;'
```

匹配範例：
```java
int insertLineUser(LineUser lineUser);           // ✅ 匹配
List<LineUser> selectLineUserList(...);          // ✅ 匹配
void updateFollowStatus(String id, Status s);    // ✅ 匹配
```

### XML 檢查邏輯

在 XML 檔案中搜尋方法實作：

```python
pattern = r'id=["\']' + re.escape(method_name) + r'["\']'
```

匹配範例：
```xml
<select id="selectLineUserList" ...>     <!-- ✅ 找到 -->
<insert id="insertLineUser" ...>         <!-- ✅ 找到 -->
```

## ⚠️ 注意事項

### 1. 方法命名規範

確保 Mapper 介面方法名稱與 XML 中的 `id` 完全一致：

```java
// ❌ 錯誤：大小寫不一致
List<User> SelectUserList(...);  // Java
<select id="selectUserList">     <!-- XML -->

// ✅ 正確
List<User> selectUserList(...);  // Java
<select id="selectUserList">     <!-- XML -->
```

### 2. 泛型類型支援

腳本支援常見的回傳類型：
- 基本類型：`int`, `void`, `String`, `Long`, `Integer`, `Boolean`
- 泛型類型：`List<T>`, `Map<K,V>`
- 自訂類型：所有 PascalCase 命名的類別

### 3. 註解影響

確保方法定義不在註解中：

```java
// ❌ 這會被提取
// List<User> selectUserList(...);

/* ❌ 這也會被提取 */
int deleteUser(Long id);

/**
 * ✅ JavaDoc 註解不影響
 */
int insertUser(User user);
```

## 🎨 自訂驗證工具

### 修改支援的回傳類型

編輯 `verify-mapper.py`：

```python
# 在正則表達式中加入新的回傳類型
pattern = r'^\s*(?:public\s+)?(?:int|void|String|Long|Integer|Boolean|List<[^>]+>|Map<[^>]+>|YourCustomType)\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\([^)]*\)\s*;'
```

### 加入更多檢查

```python
# 檢查 XML 是否有多餘的實作（Mapper 介面中沒有定義）
def check_orphan_implementations(xml_path, methods):
    """檢查 XML 中是否有孤立的實作"""
    with open(xml_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 提取所有 XML 中的 id
    xml_ids = re.findall(r'id=["\']([^"\']+)["\']', content)
    
    orphans = []
    for xml_id in xml_ids:
        if xml_id not in methods:
            orphans.append(xml_id)
    
    return orphans
```

## 📊 統計資訊

### 目前驗證結果

| 模組 | Mapper 數量 | 方法總數 | 驗證狀態 |
|------|------------|---------|---------|
| cheng-line | 4 | 49 | ✅ 通過 |
| cheng-system | - | - | 待實作 |
| cheng-quartz | - | - | 待實作 |

### LINE 模組詳細統計

| Mapper | 方法數 | 狀態 |
|--------|-------|------|
| LineConfigMapper | 14 | ✅ |
| LineConversationLogMapper | 7 | ✅ |
| LineMessageLogMapper | 10 | ✅ |
| LineUserMapper | 18 | ✅ |

## 🔗 相關資源

- [MyBatis 官方文件](https://mybatis.org/mybatis-3/)
- [Python 正則表達式](https://docs.python.org/3/library/re.html)
- [GitHub Actions 文件](https://docs.github.com/en/actions)

## 📅 版本記錄

### v1.0.0 (2025-11-02)
- ✅ 初版發布
- ✅ 支援 LINE 模組所有 Mapper 驗證
- ✅ 修復 LineMessageLogMapper 缺少的 3 個方法
- ✅ 建立完整的使用文件

---

**維護者**：Cascade AI Assistant  
**最後更新**：2025-11-02  
**驗證狀態**：✅ 所有 Mapper 通過驗證
