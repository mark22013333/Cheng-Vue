# LINE 訊息範本重構設計文檔

## 目標

重新設計 LINE 訊息範本功能，實現：
1. 雙欄式 UI（左側列表 + 右側編輯器）
2. 視覺化 Flex Message 編輯器（類似 LINE Flex Simulator）
3. 支援 1~5 個訊息物件組合（符合 LINE Push Message API 規範）
4. 預設範本庫，使用者可選擇後自訂
5. 拖拉式元件建立

---

## LINE Messaging API 訊息類型

### 支援的訊息類型

| 類型 | 說明 | 複雜度 |
|------|------|--------|
| TEXT | 純文字訊息 | 簡單 |
| IMAGE | 圖片訊息 | 簡單 |
| VIDEO | 影片訊息 | 簡單 |
| AUDIO | 音訊訊息 | 簡單 |
| LOCATION | 位置訊息 | 簡單 |
| STICKER | 貼圖訊息 | 簡單 |
| IMAGEMAP | 圖片地圖訊息 | 中等 |
| TEMPLATE | 範本訊息（Buttons/Confirm/Carousel） | 中等 |
| FLEX | Flex Message（最彈性） | 複雜 |

### Push Message API 限制

- 單次推播最多 5 個訊息物件
- 每個訊息物件獨立，可混合不同類型

---

## Flex Message 結構分析

### Container 類型

```
Flex Message
├── bubble（單一泡泡框）
│   ├── header（頭部區塊）
│   ├── hero（主視覺區塊）
│   ├── body（內容區塊）
│   └── footer（底部區塊）
└── carousel（輪播，最多 12 個 bubbles）
    ├── bubble[0]
    ├── bubble[1]
    └── ...
```

### 元件類型

| 元件 | 說明 | 可用區塊 |
|------|------|----------|
| box | 容器，可嵌套 | 所有區塊 |
| text | 文字 | 所有區塊 |
| image | 圖片 | 所有區塊 |
| icon | 圖示 | box (baseline layout) |
| button | 按鈕 | 所有區塊 |
| filler | 填充空間 | box |
| separator | 分隔線 | box (vertical/horizontal) |
| spacer | 間距（已棄用） | box |

### Box Layout 類型

- `vertical`：垂直排列
- `horizontal`：水平排列
- `baseline`：基線對齊（用於 icon + text）

---

## 資料庫結構調整

### 現有結構 (line_message_template)

```sql
CREATE TABLE line_message_template (
    template_id     BIGINT PRIMARY KEY,
    template_name   VARCHAR(100),
    template_code   VARCHAR(50) UNIQUE,
    msg_type        VARCHAR(20),      -- TEXT/IMAGE/VIDEO/AUDIO/LOCATION/STICKER/IMAGEMAP/TEMPLATE/FLEX
    content         TEXT,             -- JSON 格式內容
    alt_text        VARCHAR(400),
    preview_img     VARCHAR(500),
    status          CHAR(1),
    sort_order      INT,
    use_count       INT,
    last_used_at    DATETIME,
    ...
);
```

### 新增結構 (支援多訊息組合)

方案：使用 JSON 陣列儲存多個訊息物件

```sql
-- 保持現有結構，調整 content 欄位儲存格式
-- content 欄位改為儲存 JSON 陣列：
-- [
--   { "type": "text", "text": "Hello" },
--   { "type": "flex", "altText": "...", "contents": {...} }
-- ]

-- 新增欄位
ALTER TABLE line_message_template 
ADD COLUMN message_count INT DEFAULT 1 COMMENT '訊息物件數量（1-5）';
```

### 訊息物件 JSON 結構

```json
{
  "messages": [
    {
      "type": "text",
      "text": "歡迎光臨！"
    },
    {
      "type": "flex",
      "altText": "商品資訊",
      "contents": {
        "type": "bubble",
        "body": {
          "type": "box",
          "layout": "vertical",
          "contents": [...]
        }
      }
    }
  ]
}
```

---

## 前端 UI 設計

### 頁面布局

```
┌─────────────────────────────────────────────────────────────┐
│ 訊息範本管理                                    [+ 新增範本] │
├───────────────────┬─────────────────────────────────────────┤
│                   │                                         │
│   範本列表        │           編輯器 / 預覽區域              │
│                   │                                         │
│  ┌─────────────┐  │  ┌─────────────┐  ┌──────────────────┐  │
│  │ 📄 範本 A   │  │  │ 編輯模式    │  │   即時預覽       │  │
│  │    TEXT     │  │  │             │  │                  │  │
│  ├─────────────┤  │  │ [訊息 1]    │  │  ┌────────────┐  │  │
│  │ 📄 範本 B   │  │  │ [訊息 2]    │  │  │ 泡泡框預覽 │  │  │
│  │    FLEX     │  │  │ [+ 新增]    │  │  └────────────┘  │  │
│  ├─────────────┤  │  │             │  │                  │  │
│  │ 📄 範本 C   │  │  │             │  │                  │  │
│  │   CAROUSEL  │  │  │             │  │                  │  │
│  └─────────────┘  │  └─────────────┘  └──────────────────┘  │
│                   │                                         │
│  [搜尋...       ] │  [儲存] [取消] [預覽]                    │
│                   │                                         │
└───────────────────┴─────────────────────────────────────────┘
```

### 編輯器組件架構

```
MessageTemplateEditor/
├── index.vue                    # 主編輯器容器
├── components/
│   ├── MessageList.vue          # 訊息物件列表（1-5個）
│   ├── MessageTypeSelector.vue  # 訊息類型選擇器
│   ├── TemplateGallery.vue      # 預設範本庫
│   │
│   ├── editors/                 # 各類型編輯器
│   │   ├── TextEditor.vue       # 純文字編輯
│   │   ├── ImageEditor.vue      # 圖片編輯
│   │   ├── VideoEditor.vue      # 影片編輯
│   │   ├── AudioEditor.vue      # 音訊編輯
│   │   ├── LocationEditor.vue   # 位置編輯
│   │   ├── StickerEditor.vue    # 貼圖編輯
│   │   └── FlexEditor/          # Flex Message 編輯器
│   │       ├── index.vue        # Flex 編輯器主容器
│   │       ├── BubbleEditor.vue # 泡泡框編輯器
│   │       ├── ComponentTree.vue# 元件樹狀結構
│   │       ├── PropertyPanel.vue# 屬性面板
│   │       └── ComponentPalette.vue # 元件拖拉面板
│   │
│   ├── preview/                 # 預覽組件
│   │   ├── MessagePreview.vue   # 訊息預覽（已存在）
│   │   └── FlexPreview.vue      # Flex 預覽
│   │
│   └── common/                  # 共用組件
│       ├── ColorPicker.vue      # 顏色選擇器
│       ├── MediaSelector.vue    # 素材庫選擇器
│       └── ActionEditor.vue     # Action 編輯器
```

---

## Flex 編輯器設計

### 編輯模式

1. **範本模式**：選擇預設範本，填寫對應欄位
2. **視覺模式**：拖拉元件，即時預覽
3. **JSON 模式**：直接編輯 JSON（進階用戶）

### 預設範本庫

基於用戶提供的範本：

| 範本名稱 | 類型 | 用途 |
|----------|------|------|
| 服飾商品 | carousel | 服飾/商品展示 |
| 飯店資訊 | bubble | 飯店/民宿介紹 |
| 房地產 | bubble | 房屋/不動產資訊 |
| 餐廳介紹 | bubble | 餐廳/店家資訊 |
| 地點搜尋 | carousel (micro) | 多地點卡片 |

### 元件拖拉

```
┌─────────────────┐    ┌─────────────────────────────────┐
│  元件面板       │    │           編輯區域               │
├─────────────────┤    ├─────────────────────────────────┤
│ 📦 Box         │    │  ┌─────────────────────────────┐ │
│ 📝 Text        │ => │  │         header              │ │
│ 🖼️ Image       │    │  ├─────────────────────────────┤ │
│ 🔘 Button      │    │  │         hero                │ │
│ ⎯ Separator    │    │  ├─────────────────────────────┤ │
│ 🎨 Icon        │    │  │         body                │ │
│                │    │  │   [拖放元件到這裡]          │ │
│                │    │  ├─────────────────────────────┤ │
│                │    │  │         footer              │ │
└─────────────────┘    │  └─────────────────────────────┘ │
                       └─────────────────────────────────┘
```

---

## 屬性面板設計

### 元件屬性

選中元件後，右側顯示對應屬性面板：

**Text 元件屬性**
- text：文字內容
- size：字體大小（xxs/xs/sm/md/lg/xl/xxl）
- weight：粗細（regular/bold）
- color：顏色
- align：對齊（start/center/end）
- wrap：是否換行
- maxLines：最大行數

**Image 元件屬性**
- url：圖片網址 / 素材庫選擇
- size：尺寸（xxs~full）
- aspectRatio：比例
- aspectMode：模式（cover/fit）

**Box 元件屬性**
- layout：排列（vertical/horizontal/baseline）
- spacing：間距
- margin：外距
- padding：內距
- backgroundColor：背景色
- borderWidth/borderColor：邊框

**Button 元件屬性**
- action：動作設定
- style：樣式（primary/secondary/link）
- height：高度（sm/md）
- color：文字顏色

---

## Action 類型

```
Action
├── uri          # 開啟網址
├── message      # 發送訊息
├── postback     # 發送 Postback
├── datetimepicker # 日期時間選擇
├── camera       # 開啟相機
├── cameraRoll   # 開啟相簿
├── location     # 發送位置
└── richmenuswitch # 切換 Rich Menu
```

---

## 實作順序

### Phase 1：基礎架構
1. 調整頁面為雙欄式布局
2. 實作訊息列表組件（支援 1-5 個訊息）
3. 實作各類型基礎編輯器

### Phase 2：Flex 編輯器
1. 實作預設範本庫
2. 實作元件樹狀結構
3. 實作屬性面板
4. 實作即時預覽

### Phase 3：進階功能
1. 拖拉式元件建立
2. 素材庫整合
3. Action 編輯器
4. JSON 匯入/匯出

### Phase 4：後端調整
1. API 支援多訊息結構
2. 範本驗證邏輯
3. 預覽功能調整

---

## 技術選型

### 拖拉功能
- **vuedraggable@4.x**：Vue 3 拖拉組件

### 預覽渲染
- 自訂 Vue 組件模擬 LINE 樣式
- 使用 CSS 模擬 LINE 泡泡框外觀

### 狀態管理
- 使用 Vue 3 Composition API
- 複雜狀態考慮使用 Pinia

---

## 檔案清單

### 前端新增檔案

```
cheng-ui/src/views/line/template/
├── index.vue                    # 主頁面（改為雙欄式）
├── components/
│   ├── TemplateList.vue         # 左側範本列表
│   ├── TemplateEditor.vue       # 右側編輯器容器
│   ├── MessageList.vue          # 訊息物件列表
│   ├── TypeSelector.vue         # 訊息類型選擇
│   ├── TemplateGallery.vue      # 預設範本庫
│   │
│   ├── editors/
│   │   ├── TextEditor.vue
│   │   ├── ImageEditor.vue
│   │   ├── VideoEditor.vue
│   │   ├── AudioEditor.vue
│   │   ├── LocationEditor.vue
│   │   ├── StickerEditor.vue
│   │   └── flex/
│   │       ├── FlexEditor.vue
│   │       ├── BubbleEditor.vue
│   │       ├── ComponentTree.vue
│   │       ├── PropertyPanel.vue
│   │       └── ComponentPalette.vue
│   │
│   └── preview/
│       ├── MessagePreview.vue   # 已存在，需調整
│       └── FlexRenderer.vue     # Flex 渲染組件
```

### 後端調整

```
cheng-line/src/main/java/com/cheng/line/
├── domain/
│   └── LineMessageTemplate.java  # 新增 messageCount 欄位
├── service/
│   └── impl/LineMessageTemplateServiceImpl.java  # 調整驗證邏輯
└── controller/
    └── LineMessageTemplateController.java  # 調整 API
```
