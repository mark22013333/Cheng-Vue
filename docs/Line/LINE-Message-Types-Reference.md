# LINE Messaging API - 訊息類型參考文件

> **來源**: https://developers.line.biz/en/docs/messaging-api/message-types/  
> **最後更新**: 2025-12-21  
> **用途**: 開發 LINE 訊息範本編輯器與發送功能的參考依據

---

## 訊息類型總覽

| 類型 | ContentType Enum | 說明 | 支援狀態 |
|------|------------------|------|----------|
| Text | `TEXT` | 純文字訊息 | ✅ 已支援 |
| Text (v2) | `TEXT` | 支援 mention 和 emoji 替換 | ⚠️ 待擴充 |
| Sticker | `STICKER` | 貼圖訊息 | ✅ 已支援 |
| Image | `IMAGE` | 圖片訊息 | ✅ 已支援 |
| Video | `VIDEO` | 影片訊息 | ✅ 已支援 |
| Audio | `AUDIO` | 音訊訊息 | ✅ 已支援 |
| Location | `LOCATION` | 位置訊息 | ❌ 待實作 |
| Coupon | `COUPON` | 優惠券訊息 | ❌ 待實作 |
| Imagemap | `IMAGEMAP` | 圖片地圖訊息 | ⚠️ 部分支援 |
| Template | `TEMPLATE` | 模板訊息 | ❌ 待實作 |
| Flex | `FLEX` | 彈性訊息 | ✅ 已支援 |

---

## 1. Text Message（文字訊息）

### JSON 結構

```json
{
  "type": "text",
  "text": "Hello, world",
  "emojis": [
    {
      "index": 0,
      "productId": "5ac1bfd5040ab15980c9b435",
      "emojiId": "001"
    }
  ],
  "quoteToken": "optional-quote-token"
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `type` | String | ✅ | 固定為 `"text"` |
| `text` | String | ✅ | 訊息文字（最多 5000 字元） |
| `emojis` | Array | ❌ | LINE emoji 列表 |
| `quoteToken` | String | ❌ | 引用訊息的 token |

### LINE SDK 對應

```java
import com.linecorp.bot.messaging.model.TextMessage;

TextMessage message = new TextMessage(text);
// 或帶 emoji
TextMessage message = new TextMessage(text, emojis, quoteToken);
```

### 前端預覽樣式

- 背景色：LINE 綠色 `#06c755`
- 文字色：白色 `#ffffff`
- 圓角氣泡框
- 支援換行顯示

---

## 2. Text Message v2（文字訊息 v2）

### JSON 結構

```json
{
  "type": "textV2",
  "text": "Hello {user}! Check out this {emoji}",
  "substitution": {
    "user": {
      "type": "mention",
      "mentionee": {
        "type": "user",
        "userId": "U1234567890abcdef"
      }
    },
    "emoji": {
      "type": "emoji",
      "productId": "5ac1bfd5040ab15980c9b435",
      "emojiId": "001"
    }
  }
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `type` | String | ✅ | 固定為 `"textV2"` |
| `text` | String | ✅ | 含佔位符的文字 |
| `substitution` | Object | ❌ | 佔位符替換對應 |

---

## 3. Sticker Message（貼圖訊息）

### JSON 結構

```json
{
  "type": "sticker",
  "packageId": "446",
  "stickerId": "1988"
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `type` | String | ✅ | 固定為 `"sticker"` |
| `packageId` | String | ✅ | 貼圖包 ID |
| `stickerId` | String | ✅ | 貼圖 ID |

### LINE SDK 對應

```java
import com.linecorp.bot.messaging.model.StickerMessage;

StickerMessage message = new StickerMessage(packageId, stickerId);
```

### 可用貼圖列表

參考：https://developers.line.biz/en/docs/messaging-api/sticker-list/

### 前端預覽

- 圖片來源：`https://stickershop.line-scdn.net/stickershop/v1/sticker/{stickerId}/iPhone/sticker.png`
- 建議尺寸：120x120px

---

## 4. Image Message（圖片訊息）

### JSON 結構

```json
{
  "type": "image",
  "originalContentUrl": "https://example.com/original.jpg",
  "previewImageUrl": "https://example.com/preview.jpg"
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `type` | String | ✅ | 固定為 `"image"` |
| `originalContentUrl` | String | ✅ | 原圖 URL（HTTPS，最大 10 MB） |
| `previewImageUrl` | String | ✅ | 預覽圖 URL（HTTPS，最大 1 MB） |

### 圖片規格

- **原圖**：最大 10 MB，格式 JPEG/PNG
- **預覽圖**：最大 1 MB，建議 240x240px
- **協定**：必須使用 HTTPS（TLS 1.2+）

### LINE SDK 對應

```java
import com.linecorp.bot.messaging.model.ImageMessage;

ImageMessage message = new ImageMessage(
    URI.create(originalContentUrl),
    URI.create(previewImageUrl)
);
```

### 前端預覽樣式

- 顯示預覽圖
- 點擊可查看原圖
- 圓角邊框
- 最大寬度限制

---

## 5. Video Message（影片訊息）

### JSON 結構

```json
{
  "type": "video",
  "originalContentUrl": "https://example.com/video.mp4",
  "previewImageUrl": "https://example.com/preview.jpg",
  "trackingId": "optional-tracking-id"
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `type` | String | ✅ | 固定為 `"video"` |
| `originalContentUrl` | String | ✅ | 影片 URL（HTTPS，最大 200 MB） |
| `previewImageUrl` | String | ✅ | 預覽圖 URL（HTTPS，最大 1 MB） |
| `trackingId` | String | ❌ | 追蹤用 ID |

### 影片規格

- **格式**：MP4
- **大小**：最大 200 MB
- **時長**：最長 1 分鐘
- **協定**：必須使用 HTTPS（TLS 1.2+）

### LINE SDK 對應

```java
import com.linecorp.bot.messaging.model.VideoMessage;

VideoMessage message = new VideoMessage(
    URI.create(originalContentUrl),
    URI.create(previewImageUrl),
    trackingId  // 可為 null
);
```

### 前端預覽樣式

- 顯示預覽圖 + 播放按鈕覆蓋
- 點擊播放影片
- 圓角邊框

---

## 6. Audio Message（音訊訊息）

### JSON 結構

```json
{
  "type": "audio",
  "originalContentUrl": "https://example.com/audio.m4a",
  "duration": 60000
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `type` | String | ✅ | 固定為 `"audio"` |
| `originalContentUrl` | String | ✅ | 音訊 URL（HTTPS，最大 200 MB） |
| `duration` | Number | ✅ | 音訊長度（毫秒） |

### 音訊規格

- **格式**：M4A
- **大小**：最大 200 MB
- **時長**：最長 1 分鐘
- **協定**：必須使用 HTTPS（TLS 1.2+）

### LINE SDK 對應

```java
import com.linecorp.bot.messaging.model.AudioMessage;

AudioMessage message = new AudioMessage(
    URI.create(originalContentUrl),
    duration  // 毫秒
);
```

### 前端預覽樣式

- 耳機圖示
- 顯示時長（MM:SS 格式）
- 播放按鈕

---

## 7. Location Message（位置訊息）

### JSON 結構

```json
{
  "type": "location",
  "title": "台北 101",
  "address": "台北市信義區信義路五段7號",
  "latitude": 25.0339639,
  "longitude": 121.5644722
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `type` | String | ✅ | 固定為 `"location"` |
| `title` | String | ✅ | 地點名稱（最多 100 字元） |
| `address` | String | ✅ | 地址（最多 100 字元） |
| `latitude` | Number | ✅ | 緯度（-90 ~ 90） |
| `longitude` | Number | ✅ | 經度（-180 ~ 180） |

### LINE SDK 對應

```java
import com.linecorp.bot.messaging.model.LocationMessage;

LocationMessage message = new LocationMessage(
    title,
    address,
    latitude,
    longitude
);
```

### 前端預覽樣式

- 地圖圖示
- 顯示標題和地址
- 可點擊開啟地圖

---

## 8. Coupon Message（優惠券訊息）

### JSON 結構

```json
{
  "type": "coupon",
  "couponId": "coupon-id-12345"
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `type` | String | ✅ | 固定為 `"coupon"` |
| `couponId` | String | ✅ | 優惠券 ID |

### 備註

需要在 LINE Official Account Manager 建立優惠券後取得 ID。

---

## 9. Imagemap Message（圖片地圖訊息）

### JSON 結構

```json
{
  "type": "imagemap",
  "baseUrl": "https://example.com/imagemap",
  "altText": "這是圖片地圖",
  "baseSize": {
    "width": 1040,
    "height": 1040
  },
  "actions": [
    {
      "type": "uri",
      "linkUri": "https://example.com",
      "area": {
        "x": 0,
        "y": 0,
        "width": 520,
        "height": 1040
      }
    },
    {
      "type": "message",
      "text": "Hello",
      "area": {
        "x": 520,
        "y": 0,
        "width": 520,
        "height": 1040
      }
    }
  ],
  "video": {
    "originalContentUrl": "https://example.com/video.mp4",
    "previewImageUrl": "https://example.com/preview.jpg",
    "area": {
      "x": 0,
      "y": 0,
      "width": 1040,
      "height": 585
    },
    "externalLink": {
      "linkUri": "https://example.com",
      "label": "查看更多"
    }
  }
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `type` | String | ✅ | 固定為 `"imagemap"` |
| `baseUrl` | String | ✅ | 圖片基底 URL |
| `altText` | String | ✅ | 替代文字（最多 400 字元） |
| `baseSize` | Object | ✅ | 圖片尺寸 |
| `actions` | Array | ✅ | 可點擊區域動作 |
| `video` | Object | ❌ | 影片播放設定 |

### 圖片規格

- 寬度固定 1040px
- 需提供多種解析度版本：240, 300, 460, 700, 1040

---

## 10. Template Message（模板訊息）

### 10.1 Buttons Template（按鈕模板）

```json
{
  "type": "template",
  "altText": "按鈕模板",
  "template": {
    "type": "buttons",
    "thumbnailImageUrl": "https://example.com/image.jpg",
    "imageAspectRatio": "rectangle",
    "imageSize": "cover",
    "imageBackgroundColor": "#FFFFFF",
    "title": "標題",
    "text": "請選擇",
    "defaultAction": {
      "type": "uri",
      "label": "查看詳情",
      "uri": "https://example.com"
    },
    "actions": [
      {
        "type": "postback",
        "label": "購買",
        "data": "action=buy&itemid=123"
      },
      {
        "type": "uri",
        "label": "查看更多",
        "uri": "https://example.com"
      }
    ]
  }
}
```

### 10.2 Confirm Template（確認模板）

```json
{
  "type": "template",
  "altText": "確認模板",
  "template": {
    "type": "confirm",
    "text": "確定要執行嗎？",
    "actions": [
      {
        "type": "postback",
        "label": "是",
        "data": "action=confirm"
      },
      {
        "type": "postback",
        "label": "否",
        "data": "action=cancel"
      }
    ]
  }
}
```

### 10.3 Carousel Template（輪播模板）

```json
{
  "type": "template",
  "altText": "輪播模板",
  "template": {
    "type": "carousel",
    "columns": [
      {
        "thumbnailImageUrl": "https://example.com/item1.jpg",
        "title": "商品 1",
        "text": "描述 1",
        "actions": [
          {
            "type": "postback",
            "label": "購買",
            "data": "action=buy&itemid=1"
          }
        ]
      },
      {
        "thumbnailImageUrl": "https://example.com/item2.jpg",
        "title": "商品 2",
        "text": "描述 2",
        "actions": [
          {
            "type": "postback",
            "label": "購買",
            "data": "action=buy&itemid=2"
          }
        ]
      }
    ]
  }
}
```

### 10.4 Image Carousel Template（圖片輪播模板）

```json
{
  "type": "template",
  "altText": "圖片輪播",
  "template": {
    "type": "image_carousel",
    "columns": [
      {
        "imageUrl": "https://example.com/image1.jpg",
        "action": {
          "type": "uri",
          "label": "查看",
          "uri": "https://example.com/1"
        }
      },
      {
        "imageUrl": "https://example.com/image2.jpg",
        "action": {
          "type": "uri",
          "label": "查看",
          "uri": "https://example.com/2"
        }
      }
    ]
  }
}
```

---

## 11. Flex Message（彈性訊息）

### Container 類型

| 類型 | 說明 |
|------|------|
| `bubble` | 單一氣泡 |
| `carousel` | 多個氣泡輪播（最多 12 個） |

### Bubble 結構

```json
{
  "type": "bubble",
  "size": "mega",
  "direction": "ltr",
  "header": { /* Box component */ },
  "hero": { /* Image/Video/Box component */ },
  "body": { /* Box component */ },
  "footer": { /* Box component */ },
  "styles": {
    "header": { "backgroundColor": "#FFFFFF" },
    "hero": { "backgroundColor": "#FFFFFF" },
    "body": { "backgroundColor": "#FFFFFF" },
    "footer": { "backgroundColor": "#FFFFFF" }
  }
}
```

### Carousel 結構

```json
{
  "type": "carousel",
  "contents": [
    { /* bubble 1 */ },
    { /* bubble 2 */ }
  ]
}
```

### 完整 Flex Message

```json
{
  "type": "flex",
  "altText": "Flex Message",
  "contents": {
    "type": "bubble",
    "body": {
      "type": "box",
      "layout": "vertical",
      "contents": [
        {
          "type": "text",
          "text": "Hello, World!"
        }
      ]
    }
  }
}
```

---

## Action Objects（動作物件）

### 動作類型

| 類型 | 說明 |
|------|------|
| `postback` | 發送 postback 事件 |
| `message` | 代替使用者發送訊息 |
| `uri` | 開啟 URL |
| `datetimepicker` | 日期時間選擇器 |
| `camera` | 開啟相機 |
| `cameraRoll` | 開啟相簿 |
| `location` | 開啟位置選擇 |
| `richmenuswitch` | 切換 Rich Menu |
| `clipboard` | 複製到剪貼簿 |

### Postback Action

```json
{
  "type": "postback",
  "label": "購買",
  "data": "action=buy&itemid=123",
  "displayText": "購買商品",
  "inputOption": "openKeyboard",
  "fillInText": "請輸入數量"
}
```

### URI Action

```json
{
  "type": "uri",
  "label": "查看網站",
  "uri": "https://example.com",
  "altUri": {
    "desktop": "https://example.com/desktop"
  }
}
```

### Message Action

```json
{
  "type": "message",
  "label": "說 Hello",
  "text": "Hello"
}
```

### Datetime Picker Action

```json
{
  "type": "datetimepicker",
  "label": "選擇日期",
  "data": "action=selectDate",
  "mode": "date",
  "initial": "2025-12-21",
  "max": "2026-12-31",
  "min": "2025-01-01"
}
```

---

## Quick Reply（快速回覆）

所有訊息類型都可以附加 Quick Reply：

```json
{
  "type": "text",
  "text": "請選擇",
  "quickReply": {
    "items": [
      {
        "type": "action",
        "imageUrl": "https://example.com/icon.png",
        "action": {
          "type": "message",
          "label": "選項 A",
          "text": "A"
        }
      },
      {
        "type": "action",
        "action": {
          "type": "camera",
          "label": "拍照"
        }
      }
    ]
  }
}
```

---

## 程式碼對應表

### ContentType Enum

```java
public enum ContentType {
    TEXT("純文字"),
    IMAGE("圖片訊息"),
    VIDEO("影片訊息"),
    AUDIO("音訊訊息"),
    STICKER("貼圖訊息"),
    LOCATION("位置訊息"),
    COUPON("優惠券訊息"),
    IMAGEMAP("圖片地圖"),
    TEMPLATE("模板訊息"),
    FLEX("Flex訊息");
}
```

### PushMessageDTO 欄位對應

| ContentType | 必要欄位 |
|-------------|----------|
| `TEXT` | `textMessage` |
| `IMAGE` | `imageUrl`, `previewImageUrl` |
| `VIDEO` | `videoUrl`, `videoPreviewImageUrl` |
| `AUDIO` | `audioUrl`, `audioDuration` |
| `STICKER` | `stickerPackageId`, `stickerId` |
| `LOCATION` | `locationTitle`, `locationAddress`, `latitude`, `longitude` |
| `FLEX` | `flexMessageJson`, `altText` |
| `TEMPLATE` | `templateMessageJson`, `altText` |
| `IMAGEMAP` | `imagemapMessageJson`, `altText` |

---

## 待實作項目

### 後端

1. **ContentType Enum** - 新增 `LOCATION`, `COUPON`, `IMAGEMAP`
2. **PushMessageDTO** - 新增 Location 相關欄位
3. **LineMessageServiceImpl** - 實作 `buildLocationMessage`
4. **LineMessageTemplateController** - 處理新訊息類型

### 前端

1. **MessagePreview.vue** - 優化各類型預覽樣式
2. **TemplateEditor.vue** - 新增各類型編輯表單
3. **新增貼圖選擇器元件**
4. **新增位置選擇器元件**

---

## 參考連結

- [LINE Messaging API - Message Types](https://developers.line.biz/en/docs/messaging-api/message-types/)
- [LINE Messaging API Reference](https://developers.line.biz/en/reference/messaging-api/)
- [Flex Message Simulator](https://developers.line.biz/flex-simulator/)
- [Sticker List](https://developers.line.biz/en/docs/messaging-api/sticker-list/)
- [LINE Emoji List](https://developers.line.biz/en/docs/messaging-api/emoji-list/)
