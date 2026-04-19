# Vue 3 升級回滾方案

> **建立日期**：2025-11-22  
> **目的**：確保升級失敗時能快速恢復到穩定狀態  
> **重要性**：⭐⭐⭐⭐⭐

---

## 🆘 緊急回滾（任何階段皆可使用）

### 方案 1：Git 分支切換（最快）

**適用場景**：升級分支有問題，需立即回到穩定版本

```bash
# 1. 切回 main 分支
cd /Users/cheng/IdeaProjects/R/Cheng-Vue
git checkout main

# 2. 重新安裝依賴（如果切換後有問題）
cd cheng-ui
npm install

# 3. 啟動驗證
npm run dev
```

**恢復時間**：< 5 分鐘  
**資料損失**：無（升級分支保留）

---

### 方案 2：Git 標籤恢復

**適用場景**：需要恢復到特定備份點

```bash
# 1. 查看所有標籤
git tag -l

# 2. 恢復到備份標籤
git checkout vue2-backup-20251122

# 3. 建立新分支（保留當前狀態）
git checkout -b recovery-from-backup

# 4. 安裝依賴
cd cheng-ui
npm install

# 5. 啟動驗證
npm run dev
```

**恢復時間**：< 5 分鐘  
**資料損失**：無

---

### 方案 3：Git Reset（謹慎使用）

**適用場景**：想保留在升級分支，但回退到之前的 commit

```bash
# 1. 查看 commit 歷史
git log --oneline -10

# 2. 回退到特定 commit（保留變更）
git reset --soft <commit-hash>

# 或完全回退（捨棄變更）
git reset --hard <commit-hash>

# 3. 重新安裝依賴
cd cheng-ui
npm install

# 4. 啟動驗證
npm run dev
```

**恢復時間**：< 5 分鐘  
**資料損失**：--hard 會遺失未提交變更

---

## 📋 階段性回滾方案

### Phase 2 回滾：依賴升級失敗

**問題場景**：依賴安裝失敗或版本衝突

#### 回滾步驟

```bash
# 1. 恢復 package.json 和 package-lock.json
cd /Users/cheng/IdeaProjects/R/Cheng-Vue/cheng-ui
git checkout package.json package-lock.json

# 2. 清除 node_modules
rm -rf node_modules

# 3. 清除 npm 快取（如需要）
npm cache clean --force

# 4. 重新安裝
npm install

# 5. 驗證
npm run dev
```

**預防措施**：
```bash
# 升級前先備份
cp package.json package.json.backup
cp package-lock.json package-lock.json.backup
```

---

### Phase 3 回滾：核心重構失敗

**問題場景**：main.js 或 vite.config.js 設定錯誤導致無法啟動

#### 回滾步驟

```bash
# 1. 恢復特定檔案
git checkout src/main.js
git checkout vite.config.js
git checkout index.html
git checkout package.json

# 2. 移除可能建立的新檔案
rm -f vite.config.js  # 如果有建立

# 3. 恢復依賴
npm install

# 4. 驗證
npm run dev
```

**部分保留方案**：
```bash
# 只回退 main.js，保留其他變更
git checkout HEAD~1 -- src/main.js
```

---

### Phase 4 回滾：Element Plus 遷移失敗

**問題場景**：UI 元件大量報錯，樣式錯亂

#### 回滾步驟

```bash
# 1. 回退到 Phase 3 完成時的 commit
git log --oneline --grep="Phase 3"
git reset --hard <phase3-commit-hash>

# 2. 或只回退元件檔案
git checkout HEAD~1 -- src/views/
git checkout HEAD~1 -- src/components/

# 3. 重新安裝依賴
npm install

# 4. 驗證
npm run dev
```

**漸進式回滾**：
```bash
# 逐頁回退，保留已成功遷移的頁面
git checkout HEAD~1 -- src/views/system/user/index.vue
```

---

### Phase 5 回滾：路由與狀態管理失敗

**問題場景**：路由跳轉失敗，狀態管理報錯

#### 回滾步驟

```bash
# 1. 回退路由和 store
git checkout HEAD~1 -- src/router/
git checkout HEAD~1 -- src/store/

# 2. 如果已切換到 Pinia，降級回 Vuex
npm uninstall pinia
npm install vuex@3.6.0

# 3. 驗證
npm run dev
```

---

### Phase 6-7 回滾：元件遷移或測試階段問題

**問題場景**：部分功能失效，但核心可運作

#### 回滾步驟

```bash
# 1. 只回退有問題的檔案或模組
git checkout HEAD~1 -- src/views/inventory/

# 2. 或回退到上一個穩定 commit
git reset --hard HEAD~1

# 3. 驗證
npm run dev
```

---

## 🔧 依賴恢復腳本

### 快速恢復 Vue 2 依賴

建立 `restore-vue2-deps.sh`：

```bash
#!/bin/bash

echo "開始恢復 Vue 2 依賴..."

# 移除 Vue 3 相關依賴
npm uninstall vue vue-router pinia element-plus @element-plus/icons-vue
npm uninstall vite @vitejs/plugin-vue vite-plugin-svg-icons

# 安裝 Vue 2 依賴
npm install vue@2.6.12 vue-template-compiler@2.6.12
npm install vue-router@3.4.9
npm install vuex@3.6.0
npm install element-ui@2.15.14

# 安裝 Vue CLI
npm install -D @vue/cli-service@4.4.6 @vue/cli-plugin-babel@4.4.6

# 安裝舊版插件
npm install vue-clipboard2@^0.3.3
npm install @riophae/vue-treeselect@0.4.0
npm install vuedraggable@2.24.3

echo "Vue 2 依賴恢復完成！"
echo "請執行 npm install 確保所有依賴正確安裝"
```

**使用方式**：
```bash
chmod +x restore-vue2-deps.sh
./restore-vue2-deps.sh
```

---

## 📝 檔案備份清單

### 關鍵檔案備份（手動）

在開始升級前，備份以下檔案：

```bash
# 建立備份目錄
mkdir -p .backup/vue2-original

# 備份關鍵檔案
cp package.json .backup/vue2-original/
cp package-lock.json .backup/vue2-original/
cp src/main.js .backup/vue2-original/
cp vue.config.js .backup/vue2-original/
cp src/router/index.js .backup/vue2-original/
cp src/store/index.js .backup/vue2-original/

# 備份環境變數
cp .env.development .backup/vue2-original/
cp .env.production .backup/vue2-original/

echo "備份完成：.backup/vue2-original/"
```

### 恢復備份

```bash
# 從備份恢復
cp .backup/vue2-original/package.json ./
cp .backup/vue2-original/package-lock.json ./
cp .backup/vue2-original/src/main.js src/
cp .backup/vue2-original/vue.config.js ./

# 重新安裝依賴
rm -rf node_modules
npm install
```

---

## 🚨 常見問題與解決方案

### 問題 1：npm install 失敗

**症狀**：依賴安裝過程報錯

**解決方案**：
```bash
# 清除快取
npm cache clean --force
rm -rf node_modules package-lock.json

# 使用舊版 npm（如需要）
npm install -g npm@8

# 重新安裝
npm install
```

---

### 問題 2：Vite 建構失敗

**症狀**：`npm run dev` 報錯

**解決方案**：
```bash
# 刪除 Vite 快取
rm -rf node_modules/.vite

# 檢查 Node.js 版本
node -v  # 應該 >= 18

# 如果 vite.config.js 有問題，直接刪除
rm vite.config.js

# 恢復 Vue CLI 設定
git checkout vue.config.js
```

---

### 問題 3：Element Plus 樣式錯亂

**症狀**：頁面樣式完全跑版

**解決方案**：
```bash
# 方案 1：恢復 Element UI
npm uninstall element-plus @element-plus/icons-vue
npm install element-ui@2.15.14

# 方案 2：恢復樣式檔案
git checkout src/assets/styles/
git checkout src/main.js
```

---

### 問題 4：路由跳轉失敗

**症狀**：點擊選單無反應或報錯

**解決方案**：
```bash
# 恢復路由檔案
git checkout src/router/
git checkout src/permission.js

# 降級 Vue Router
npm uninstall vue-router
npm install vue-router@3.4.9
```

---

### 問題 5：Pinia 狀態管理問題

**症狀**：狀態讀取報錯

**解決方案**：
```bash
# 恢復 Vuex
npm uninstall pinia
npm install vuex@3.6.0

# 恢復 store 檔案
git checkout src/store/
```

---

## 📊 回滾決策樹

```
升級失敗？
│
├─ 依賴安裝失敗 → 方案 1: Git 切回 main 分支
│
├─ 應用無法啟動 → 方案 2: 恢復 main.js + vite.config.js
│
├─ UI 大量報錯 → 方案 3: 回退到 Phase 3 完成時
│
├─ 部分功能失效 → 方案 4: 只回退有問題的檔案
│
└─ 效能嚴重下降 → 方案 5: 完整回退，重新規劃
```

---

## ✅ 回滾後驗證清單

- [ ] 應用可正常啟動（`npm run dev`）
- [ ] 登入功能正常
- [ ] 主要頁面可開啟
- [ ] 無 console 錯誤
- [ ] 資料庫連線正常
- [ ] API 請求正常

---

## 📞 升級過程檢查點

建議在以下時間點建立 Git commit，方便回滾：

1. **Phase 1 完成後**
   ```bash
   git add .
   git commit -m "Phase 1: 基礎準備完成"
   ```

2. **Phase 2 完成後**
   ```bash
   git add .
   git commit -m "Phase 2: 依賴升級完成"
   ```

3. **Phase 3 完成後**
   ```bash
   git add .
   git commit -m "Phase 3: 核心重構完成 - 應用可啟動"
   ```

4. **Phase 4 完成後**
   ```bash
   git add .
   git commit -m "Phase 4: Element Plus 遷移完成"
   ```

5. **Phase 5 完成後**
   ```bash
   git add .
   git commit -m "Phase 5: 路由與狀態管理遷移完成"
   ```

6. **Phase 6 完成後**
   ```bash
   git add .
   git commit -m "Phase 6: 元件遷移完成"
   ```

7. **Phase 7 完成後**
   ```bash
   git add .
   git commit -m "Phase 7: 測試與優化完成 - 準備上線"
   ```

---

## 🔐 最終保險措施

### 完整專案壓縮備份

```bash
# 在開始升級前
cd /Users/cheng/IdeaProjects/R
tar -czf Cheng-Vue_vue2_backup_20251122.tar.gz Cheng-Vue/

# 驗證備份
ls -lh Cheng-Vue_vue2_backup_20251122.tar.gz
```

### 恢復完整備份

```bash
# 刪除當前專案（謹慎！）
cd /Users/cheng/IdeaProjects/R
rm -rf Cheng-Vue

# 解壓備份
tar -xzf Cheng-Vue_vue2_backup_20251122.tar.gz

# 重新安裝依賴
cd Cheng-Vue/cheng-ui
npm install
```

---

## 📋 回滾日誌模板

每次回滾請記錄：

```markdown
### 回滾記錄 #1

- **日期**：2025-11-XX
- **階段**：Phase X
- **原因**：_描述問題_
- **使用方案**：方案 X
- **恢復時間**：X 分鐘
- **資料損失**：有 / 無
- **經驗教訓**：_填寫_
```

---

## ⚠️ 重要提醒

1. **定期 commit**：每完成一個小功能就 commit
2. **不要強制推送**：避免使用 `git push --force`
3. **保留升級分支**：即使回滾，也保留 `feature/vue3-migration` 分支供分析
4. **記錄問題**：所有問題都記錄在 `VUE3_UPGRADE_PROGRESS.md` 的問題追蹤表
5. **測試後再 commit**：確保每個 commit 都是可運作的狀態

---

**建立時間**：2025-11-22 23:50  
**最後更新**：2025-11-22 23:50  
**下一步**：開始 Phase 2 - 依賴升級
