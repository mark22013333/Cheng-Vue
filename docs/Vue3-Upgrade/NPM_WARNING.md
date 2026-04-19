# ⚠️ 重要提醒：請勿使用 npm install

## 正確的依賴安裝方式

```bash
# ✅ 使用 pnpm（推薦）
pnpm install

# ✅ 或使用 yarn
yarn install
```

## 為什麼不要使用 npm install？

1. **已設定 .npmrc**：專案已設定 `package-lock=false`，npm install 不會產生 lock 檔案
2. **版本鎖定**：專案使用 `pnpm-lock.yaml` 來鎖定依賴版本
3. **避免衝突**：混合使用不同的套件管理器會導致依賴版本不一致

## 如果意外執行了 npm install

```bash
# 刪除 node_modules 重新安裝
rm -rf node_modules
pnpm install
```

## 配置說明

- `.npmrc` 檔案設定 `package-lock=false`
- `package.json` 中 `install` 和 `i` scripts 會顯示警告
- `.gitignore` 已忽略 `package-lock.json`

---
**請務必使用 pnpm 或 yarn 進行依賴管理！**
