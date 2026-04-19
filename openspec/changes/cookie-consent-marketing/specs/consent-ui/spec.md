## ADDED Requirements

### Requirement: 簡潔的 Cookie 提示橫幅
系統 SHALL 在使用者未做過同意選擇時，於頁面底部顯示 Cookie 提示橫幅。橫幅 SHALL 以自然語言簡要說明 Cookie 用途，不列出技術參數名稱。

#### Scenario: 首次訪問顯示提示
- **WHEN** 使用者首次造訪商城且 localStorage 無 `cookie_consent` key
- **THEN** 頁面載入 1.5 秒後，底部滑入 Cookie 提示橫幅

#### Scenario: 已做過選擇不顯示
- **WHEN** 使用者已做過同意選擇（localStorage 有 `cookie_consent` key）
- **THEN** 不顯示 Cookie 提示橫幅

### Requirement: 雙按鈕選項
橫幅 SHALL 提供兩個操作選項：「全部接受」（主要按鈕，實色）與「僅必要」（次要按鈕，文字風格）。兩個選項皆 SHALL 清晰可見可點擊。

#### Scenario: 點選全部接受
- **WHEN** 使用者點選「全部接受」按鈕
- **THEN** 橫幅以動畫收起，同意管理器記錄三層皆接受

#### Scenario: 點選僅必要
- **WHEN** 使用者點選「僅必要」按鈕
- **THEN** 橫幅以動畫收起，同意管理器記錄僅必要層級，清除已存在的非必要資料

### Requirement: 可展開的分類說明
橫幅 SHALL 提供「了解更多」展開區域，以自然語言描述三個類別的用途：必要（登入驗證）、功能性（記住偏好設定）、行銷（個人化推薦）。不列出具體的 Cookie 名稱或技術參數。

#### Scenario: 展開分類說明
- **WHEN** 使用者點選「了解更多」
- **THEN** 橫幅下方展開顯示三個類別的簡要說明文案

#### Scenario: 收起分類說明
- **WHEN** 使用者再次點選「了解更多」（或「收起」）
- **THEN** 展開區域收起

### Requirement: 響應式設計
橫幅 SHALL 在桌面（>640px）與手機（<=640px）上都有良好的顯示效果。手機上按鈕 SHALL 全寬排列。

#### Scenario: 手機版面配置
- **WHEN** 螢幕寬度 <= 640px
- **THEN** 按鈕改為全寬垂直排列，文案字體適當縮小

### Requirement: 主題適配
橫幅 SHALL 使用 CSS 變數 `--mall-primary` 作為主色調，自動適配商城當前主題配色。

#### Scenario: 切換主題後橫幅配色
- **WHEN** 商城主題為深色系（如 dark 主題）
- **THEN** 橫幅的按鈕色、icon 色隨主題變化
