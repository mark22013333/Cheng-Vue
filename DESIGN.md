# Design System — CoolApps

## Product Context
- **What this is:** 綜合電商管理平台，包含後台管理系統和消費者端電商前台
- **Who it's for:** 小型商家（店主、管理員、消費者）
- **Space/industry:** 電商 + LINE 行銷 + 庫存管理
- **Project type:** Hybrid — 後台管理系統 (internal tool) + 電商前台 (consumer web app)

## Aesthetic Direction
- **Direction:** Warm Professional — 親切但不隨便，專業但不冰冷
- **Decoration level:** intentional — 微妙的圓角和陰影讓介面有溫度
- **Mood:** 想像一家設計很好的社區商店。專業、值得信賴，但走進去讓人覺得舒服、受歡迎
- **Reference sites:** N/A (based on design knowledge)

## Typography
- **Display/Hero:** Plus Jakarta Sans Bold/ExtraBold — 溫暖的幾何無襯線體，比 Inter 有個性，適合品牌標題
- **Body:** system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif — 中文可讀性最佳、零載入成本
- **UI/Labels:** same as body
- **Data/Tables:** DM Sans (tabular-nums) — 數字等寬對齊，表格和金額清晰
- **Code:** JetBrains Mono
- **Loading:** Google Fonts CDN
  ```html
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=DM+Sans:opsz,wght@9..40,400;9..40,500;9..40,600&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
  ```
- **Scale:**
  - xs: 12px — 輔助文字、標籤
  - sm: 13px — 次要 UI 文字
  - base: 14px — 預設 body
  - md: 16px — 正文段落
  - lg: 20px — 小標題
  - xl: 24px — 區塊標題
  - 2xl: 32px — 頁面標題
  - 3xl: 48px — Hero 標題

## Color
- **Approach:** balanced — primary + accent + 語義色系
- **Primary (admin):** #409EFF — Element Plus 藍，後台所有操作的主色
  - Light: #79BBFF
  - Lighter: #D9ECFF
- **Accent (shop):** #FF6B4A — 珊瑚橘，前台強調色、CTA、價格
  - Light: #FF9A85
  - Lighter: #FFE8E2
- **Neutrals (warm gray):**
  - Text 1: #1D2129 — 主要文字
  - Text 2: #4E5969 — 次要文字
  - Text 3: #86909C — 輔助文字、placeholder
  - Text 4: #C9CDD4 — 禁用文字
  - Border: #E5E6EB — 邊框、分隔線
  - BG 3: #F2F3F5 — 區塊背景
  - BG 2: #F7F8FA — 頁面背景
  - BG 1: #FFFFFF — 卡片/白色背景
- **Semantic:**
  - Success: #00B42A — 成功操作、已完成
  - Warning: #FF7D00 — 警告、待處理
  - Error: #F53F3F — 錯誤、失敗、刪除
  - Info: #86909C — 資訊提示
- **Dark mode:** redesign surfaces, reduce saturation 10-20%
  - BG 1: #17171A / BG 2: #232324 / BG 3: #2A2A2B
  - Border: #3D3D3D
  - Text 1: #F6F6F6 / Text 2: #C9CDD4

## Spacing
- **Base unit:** 4px
- **Density:**
  - 後台: comfortable (md:16px 為主)
  - 前台: spacious (lg:24px / xl:32px 為主)
- **Scale:**
  - 2xs: 2px
  - xs: 4px
  - sm: 8px
  - md: 16px
  - lg: 24px
  - xl: 32px
  - 2xl: 48px
  - 3xl: 64px

## Layout
- **Approach:** hybrid — 後台用嚴格網格（grid-disciplined），前台用更自由的編輯式佈局
- **Grid:**
  - Desktop (≥1200px): 24 columns (Element Plus grid)
  - Tablet (768-1199px): 12 columns
  - Mobile (<768px): 4 columns
- **Max content width:** 1200px (後台), 1280px (前台)
- **Border radius (3 levels only):**
  - sm: 6px — 按鈕、輸入框、Tag、小元件
  - md: 10px — 卡片、Dialog、範本項目
  - lg: 16px — 大卡片、模組面板、Hero 區塊

## Motion
- **Approach:** intentional — 有意義的狀態轉換和入場動畫，不做裝飾性動畫
- **Easing:** enter(ease-out) exit(ease-in) move(ease-in-out)
- **Duration:**
  - micro: 50-100ms — tooltip, ripple
  - short: 150ms — hover, focus, toggle（狀態轉換）
  - medium: 250ms — expand/collapse, fade in（入場動畫）
  - long: 300ms — slide, page transition（滾動觸發）

## CSS Variables Template

```css
:root {
  /* Colors */
  --color-primary: #409EFF;
  --color-primary-light: #79BBFF;
  --color-primary-lighter: #D9ECFF;
  --color-accent: #FF6B4A;
  --color-accent-light: #FF9A85;
  --color-accent-lighter: #FFE8E2;
  --color-success: #00B42A;
  --color-warning: #FF7D00;
  --color-error: #F53F3F;
  --color-info: #86909C;
  --color-text-1: #1D2129;
  --color-text-2: #4E5969;
  --color-text-3: #86909C;
  --color-text-4: #C9CDD4;
  --color-border: #E5E6EB;
  --color-bg-1: #FFFFFF;
  --color-bg-2: #F7F8FA;
  --color-bg-3: #F2F3F5;

  /* Spacing */
  --sp-2xs: 2px;
  --sp-xs: 4px;
  --sp-sm: 8px;
  --sp-md: 16px;
  --sp-lg: 24px;
  --sp-xl: 32px;
  --sp-2xl: 48px;
  --sp-3xl: 64px;

  /* Radius */
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;

  /* Typography */
  --font-display: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
  --font-body: system-ui, -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-data: 'DM Sans', system-ui, sans-serif;
  --font-code: 'JetBrains Mono', monospace;

  /* Motion */
  --transition-fast: 150ms ease-out;
  --transition-normal: 250ms ease-out;
  --transition-slow: 300ms ease-in-out;
}
```

## Decisions Log
| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-03-28 | Initial design system created | Created by /design-consultation. Warm Professional aesthetic for small business e-commerce + admin platform. |
| 2026-03-28 | Plus Jakarta Sans for display | Warmer geometric sans-serif than Inter/Roboto, gives CoolApps personality while staying professional. |
| 2026-03-28 | Coral accent #FF6B4A | Warmer and more modern than typical e-commerce red/orange. Differentiates from competition. |
| 2026-03-28 | 3-level border radius (6/10/16) | Simplifies from previous 5 different radius values. Clear hierarchy: small elements, cards, large panels. |
| 2026-03-28 | Warm gray neutrals | Warmer than pure gray (#1D2129 vs #000). Reinforces "friendly but professional" positioning. |
