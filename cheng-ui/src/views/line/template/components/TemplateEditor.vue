<template>
  <div class="template-editor">
    <!-- 頂部工具列 -->
    <div class="editor-header">
      <div class="header-title">
        <template v-if="isNew">新增訊息範本</template>
        <template v-else>編輯：{{ form.templateName }}</template>
      </div>
      <div class="header-actions">
        <el-button @click="openTestDialog" :disabled="isNew">推播測試</el-button>
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">儲存</el-button>
      </div>
    </div>

    <!-- 推播測試對話框 -->
    <el-dialog v-model="testDialogVisible" title="推播測試" width="500px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="選擇使用者" required>
          <el-select
            v-model="testLineUserId"
            filterable
            remote
            placeholder="搜尋 LINE 使用者"
            :remote-method="searchLineUsers"
            :loading="lineUsersLoading"
            style="width: 100%"
          >
            <el-option
              v-for="user in lineUserList"
              :key="user.lineUserId"
              :label="user.lineDisplayName || user.lineUserId"
              :value="user.lineUserId"
            >
              <div class="line-user-option">
                <el-avatar :size="24" :src="user.linePictureUrl" />
                <span>{{ user.lineDisplayName || '未知使用者' }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon>
          測試訊息將發送給選擇的 LINE 使用者
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="testDialogVisible = false">取消</el-button>
        <el-button v-hasPermi="[LINE_TEMPLATE_SEND]" type="primary" :loading="testSending" :disabled="!testLineUserId" @click="sendTestMessage">
          發送測試
        </el-button>
      </template>
    </el-dialog>

    <!-- 匯入 JSON 對話框 -->
    <el-dialog v-model="importJsonDialogVisible" title="匯入 Flex JSON" width="700px" destroy-on-close>
      <el-tabs v-model="importJsonTab">
        <el-tab-pane label="貼上 JSON" name="paste">
          <el-input
            v-model="importJsonText"
            type="textarea"
            :rows="15"
            placeholder="請貼上 Flex Message JSON 內容..."
            style="font-family: monospace;"
          />
          <div v-if="importJsonError" class="import-error">
            <el-alert :title="importJsonError" type="error" :closable="false" show-icon />
          </div>
        </el-tab-pane>
        <el-tab-pane label="上傳檔案" name="file">
          <el-upload
            ref="jsonUploadRef"
            drag
            multiple
            :auto-upload="false"
            accept=".json,application/json"
            :file-list="importJsonFiles"
            :on-change="handleJsonFileChange"
            :on-remove="handleJsonFileRemove"
          >
            <el-icon class="el-icon--upload"><Upload /></el-icon>
            <div class="el-upload__text">
              拖曳 JSON 檔案到此處，或 <em>點擊上傳</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支援多個 .json 檔案，每個檔案會成為一個獨立訊息
              </div>
            </template>
          </el-upload>
          <div v-if="importJsonError" class="import-error">
            <el-alert :title="importJsonError" type="error" :closable="false" show-icon />
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="importJsonDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmImportJson" :loading="importJsonLoading">
          確認匯入
        </el-button>
      </template>
    </el-dialog>

    <!-- 編輯區域 -->
    <div class="editor-body">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
        <!-- 基本資訊 -->
        <div class="section">
          <div class="section-title">基本資訊</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="範本名稱" prop="templateName">
                <el-input v-model="form.templateName" placeholder="請輸入範本名稱" maxlength="100" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="範本代碼" prop="templateCode">
                <el-input v-model="form.templateCode" placeholder="選填，用於程式引用" maxlength="50" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="狀態">
                <el-radio-group v-model="form.status">
                  <el-radio :value="1">啟用</el-radio>
                  <el-radio :value="0">停用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="排序">
                <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 訊息內容 -->
        <div class="section">
          <div class="section-title">
            訊息內容
            <el-tag type="info" size="small" style="margin-left: 8px">
              {{ messages.length }} / 5 個訊息
            </el-tag>
          </div>

          <!-- 訊息列表（支援拖曳排序） -->
          <div class="message-list">
            <draggable
              v-model="messages"
              item-key="_dragKey"
              handle=".drag-handle"
              animation="200"
              ghost-class="ghost-item"
              @end="onDragEnd"
              class="message-list-inner"
            >
              <template #item="{ element: msg, index }">
                <div
                  :class="['message-item', { active: activeMessageIndex === index }]"
                  @click="activeMessageIndex = index"
                >
                  <div class="msg-header">
                    <el-icon class="drag-handle" :size="14" title="拖曳移動"><Rank /></el-icon>
                    <el-icon :size="16"><component :is="getMsgTypeIcon(msg.type)" /></el-icon>
                    <span class="msg-type">{{ getMsgTypeLabel(msg.type) }}</span>
                    <el-button
                      link
                      type="danger"
                      :icon="Delete"
                      size="small"
                      @click.stop="removeMessage(index)"
                    />
                  </div>
                  <div class="msg-preview">
                    <template v-if="msg.type === 'TEXT'">{{ msg.text?.substring(0, 30) || '...' }}</template>
                    <template v-else-if="msg.type === 'FLEX'">Flex Message</template>
                    <template v-else-if="msg.type === 'STICKER'">貼圖</template>
                    <template v-else-if="msg.type === 'TEMPLATE'">{{ msg.templateData?.templateType || '模板訊息' }}</template>
                    <template v-else>{{ getMsgTypeLabel(msg.type) }}</template>
                  </div>
                </div>
              </template>
            </draggable>

            <!-- 新增訊息按鈕 -->
            <el-dropdown v-if="messages.length < 5" trigger="click" @command="addMessage">
              <div class="add-message-btn">
                <el-icon><Plus /></el-icon>
                <span>新增訊息</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="item in msgTypeOptions" :key="item.value" :command="item.value">
                    <div style="display: flex; flex-direction: column;">
                      <div style="display: flex; align-items: center; gap: 8px;">
                        <el-icon><component :is="item.icon" /></el-icon>
                        {{ item.label }}
                      </div>
                      <span v-if="item.hint" style="font-size: 11px; color: #909399; margin-left: 24px;">{{ item.hint }}</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 當前訊息編輯器 -->
          <div v-if="currentMessage" class="message-editor">
            <!-- TEXT 編輯器 -->
            <template v-if="currentMessage.type === 'TEXT'">
              <!-- 即時預覽（綠色對話框） -->
              <div class="text-live-preview" v-if="currentMessage.text || (currentMessage.emojis && currentMessage.emojis.length > 0) || (currentMessage.enableQuickReply && currentMessage.quickReply?.items?.length > 0)">
                <div class="preview-label">預覽效果：</div>
                <div class="preview-bubble">
                  <template v-for="(part, idx) in textPreviewParts" :key="idx">
                    <span v-if="part.type === 'text'">{{ part.content }}</span>
                    <img v-else-if="part.type === 'emoji'" :src="part.url" class="preview-emoji" />
                  </template>
                  <span v-if="!currentMessage.text && (!currentMessage.emojis || currentMessage.emojis.length === 0)" class="empty-text">(無內容)</span>
                </div>
                <!-- Quick Reply 預覽 -->
                <div v-if="currentMessage.enableQuickReply && currentMessage.quickReply?.items?.length > 0" class="quick-reply-preview">
                  <div 
                    v-for="(item, idx) in currentMessage.quickReply.items" 
                    :key="idx" 
                    class="quick-reply-btn-preview"
                  >
                    <img v-if="item.imageUrl" :src="item.imageUrl" class="quick-reply-icon" />
                    <span>{{ item.action?.label || '(未設定)' }}</span>
                  </div>
                </div>
              </div>
              <el-form-item label="文字內容" prop="content">
                <el-input
                  ref="textInputRef"
                  v-model="currentMessage.text"
                  type="textarea"
                  :rows="6"
                  placeholder="請輸入文字內容，使用 $ 作為 LINE Emoji 佔位符"
                  maxlength="5000"
                  show-word-limit
                />
              </el-form-item>
              <!-- 已插入的 Emoji 列表 -->
              <div v-if="currentMessage.emojis && currentMessage.emojis.length > 0" class="emoji-list-preview">
                <div class="emoji-list-label">已插入的 Emoji（對應文字中的 $ 符號）：</div>
                <div class="emoji-list-items">
                  <div v-for="(emoji, index) in currentMessage.emojis" :key="index" class="emoji-list-item">
                    <span class="emoji-index">{{ index + 1 }}</span>
                    <img :src="getEmojiUrl(emoji.productId, emoji.emojiId)" class="emoji-thumbnail" />
                    <el-button type="danger" size="small" circle :icon="Close" @click="removeEmojiAt(index)" />
                  </div>
                </div>
                <el-button size="small" type="warning" plain @click="clearAllEmojisFromMessage">清除全部 Emoji</el-button>
              </div>
              <div class="text-actions">
                <el-button type="primary" plain @click="openEmojiSelector">
                  <el-icon><Star /></el-icon>
                  插入 LINE Emoji
                </el-button>
                <span class="action-tip">使用 $ 作為 Emoji 佔位符，例如：「Hello $ World $」</span>
              </div>

              <!-- Quick Reply 設定 -->
              <el-divider content-position="left">
                <el-checkbox v-model="currentMessage.enableQuickReply">啟用快速回覆按鈕</el-checkbox>
              </el-divider>
              <div v-if="currentMessage.enableQuickReply" class="quick-reply-section">
                <div class="quick-reply-header">
                  <span class="section-label">快速回覆按鈕（最多 13 個）</span>
                  <el-button type="primary" size="small" @click="addQuickReplyItem" :disabled="(currentMessage.quickReply?.items?.length || 0) >= 13">
                    <el-icon><Plus /></el-icon>
                    新增按鈕
                  </el-button>
                </div>
                <div class="form-tip">※ 快速回覆按鈕會顯示在訊息下方，用戶點擊後可快速觸發動作</div>
                
                <el-row v-if="currentMessage.quickReply?.items?.length > 0" :gutter="16" class="quick-reply-layout">
                  <!-- 左側：按鈕列表 -->
                  <el-col :span="8">
                    <div class="quick-reply-list">
                      <div 
                        v-for="(item, idx) in currentMessage.quickReply.items" 
                        :key="idx"
                        class="quick-reply-list-item"
                        :class="{ active: selectedQuickReplyIndex === idx }"
                        @click="selectedQuickReplyIndex = idx"
                      >
                        <div class="item-info">
                          <span class="item-index">{{ idx + 1 }}</span>
                          <span class="item-label">{{ item.action.label || '(未設定)' }}</span>
                        </div>
                        <el-button 
                          type="danger" 
                          size="small" 
                          circle 
                          :icon="Delete" 
                          @click.stop="removeQuickReplyItem(idx)" 
                        />
                      </div>
                    </div>
                  </el-col>
                  
                  <!-- 右側：編輯區域 -->
                  <el-col :span="16">
                    <div v-if="selectedQuickReplyItem" class="quick-reply-editor">
                      <div class="editor-title">編輯按鈕 {{ selectedQuickReplyIndex + 1 }}</div>
                      
                      <el-form-item label="動作類型" label-width="80px">
                        <el-select v-model="selectedQuickReplyItem.action.type" placeholder="選擇動作類型" style="width: 100%">
                          <el-option label="發送訊息" value="message" />
                          <el-option label="開啟連結" value="uri" />
                          <el-option label="Postback" value="postback" />
                          <el-option label="日期選擇" value="datetimepicker" />
                          <el-option label="開啟相機" value="camera" />
                          <el-option label="開啟相簿" value="cameraRoll" />
                          <el-option label="傳送位置" value="location" />
                          <el-option label="複製文字" value="clipboard" />
                        </el-select>
                      </el-form-item>
                      
                      <el-form-item label="按鈕文字" label-width="80px" required>
                        <el-input v-model="selectedQuickReplyItem.action.label" placeholder="顯示在按鈕上的文字" maxlength="20" show-word-limit />
                      </el-form-item>
                      
                      <!-- 依動作類型顯示不同欄位 -->
                      <template v-if="selectedQuickReplyItem.action.type === 'message'">
                        <el-form-item label="訊息內容" label-width="80px" required>
                          <el-input v-model="selectedQuickReplyItem.action.text" placeholder="用戶點擊後發送的訊息" maxlength="300" show-word-limit />
                        </el-form-item>
                      </template>
                      
                      <template v-else-if="selectedQuickReplyItem.action.type === 'uri'">
                        <el-form-item label="連結網址" label-width="80px" required>
                          <el-input v-model="selectedQuickReplyItem.action.uri" placeholder="https://..." />
                        </el-form-item>
                      </template>
                      
                      <template v-else-if="selectedQuickReplyItem.action.type === 'postback'">
                        <el-form-item label="Postback" label-width="80px" required>
                          <el-input v-model="selectedQuickReplyItem.action.data" placeholder="回傳資料（後端處理用）" maxlength="300" />
                        </el-form-item>
                        <el-form-item label="顯示文字" label-width="80px">
                          <el-input v-model="selectedQuickReplyItem.action.displayText" placeholder="選填，點擊後在聊天室顯示的文字" maxlength="300" />
                        </el-form-item>
                      </template>
                      
                      <template v-else-if="selectedQuickReplyItem.action.type === 'datetimepicker'">
                        <el-form-item label="Postback" label-width="80px" required>
                          <el-input v-model="selectedQuickReplyItem.action.data" placeholder="回傳資料（後端處理用）" maxlength="300" />
                        </el-form-item>
                        <el-form-item label="模式" label-width="80px">
                          <el-select v-model="selectedQuickReplyItem.action.mode" style="width: 100%">
                            <el-option label="日期時間" value="datetime" />
                            <el-option label="僅日期" value="date" />
                            <el-option label="僅時間" value="time" />
                          </el-select>
                        </el-form-item>
                      </template>
                      
                      <template v-else-if="selectedQuickReplyItem.action.type === 'clipboard'">
                        <el-form-item label="複製內容" label-width="80px" required>
                          <el-input v-model="selectedQuickReplyItem.action.clipboardText" placeholder="點擊後複製到剪貼簿的文字" maxlength="1000" />
                        </el-form-item>
                      </template>
                      
                      <!-- 按鈕圖示（所有類型都可設定） -->
                      <el-form-item label="按鈕圖示" label-width="80px">
                        <el-input v-model="selectedQuickReplyItem.imageUrl" placeholder="選填，PNG 圖片 URL（1:1 比例）" />
                      </el-form-item>
                    </div>
                    <el-empty v-else description="請選擇左側按鈕進行編輯" :image-size="60" />
                  </el-col>
                </el-row>
                <el-empty v-else description="尚未新增快速回覆按鈕" :image-size="60" />
              </div>
            </template>

            <!-- IMAGE 編輯器 -->
            <template v-else-if="currentMessage.type === 'IMAGE'">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="原圖網址">
                    <el-input v-model="currentMessage.originalContentUrl" placeholder="https://...">
                      <template #append>
                        <el-button @click="openMediaSelector('image', 'originalContentUrl')">選擇素材</el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="預覽圖網址">
                    <el-input v-model="currentMessage.previewImageUrl" placeholder="選填，預設同原圖">
                      <template #append>
                        <el-button @click="openMediaSelector('image', 'previewImageUrl')">選擇素材</el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <div class="media-preview">
                    <el-image v-if="currentMessage.originalContentUrl" :src="currentMessage.originalContentUrl" fit="contain" />
                    <el-empty v-else description="輸入網址後顯示預覽" :image-size="60" />
                  </div>
                </el-col>
              </el-row>
            </template>

            <!-- VIDEO 編輯器 -->
            <template v-else-if="currentMessage.type === 'VIDEO'">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="影片網址">
                    <el-input v-model="currentMessage.originalContentUrl" placeholder="https://...">
                      <template #append>
                        <el-button @click="openMediaSelector('video', 'originalContentUrl')">選擇素材</el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                  <el-form-item label="預覽圖網址">
                    <el-input v-model="currentMessage.previewImageUrl" placeholder="https://...">
                      <template #append>
                        <el-button @click="openMediaSelector('image', 'previewImageUrl')">選擇素材</el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <div class="media-preview">
                    <video v-if="currentMessage.originalContentUrl" :src="currentMessage.originalContentUrl" controls />
                    <el-empty v-else description="輸入網址後顯示預覽" :image-size="60" />
                  </div>
                </el-col>
              </el-row>
            </template>

            <!-- AUDIO 編輯器 -->
            <template v-else-if="currentMessage.type === 'AUDIO'">
              <el-form-item label="音訊網址">
                <el-input v-model="currentMessage.originalContentUrl" placeholder="https://...">
                  <template #append>
                    <el-button @click="openMediaSelector('audio', 'originalContentUrl')">選擇素材</el-button>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="時長（毫秒）">
                <el-input-number v-model="currentMessage.duration" :min="1" :max="60000" />
              </el-form-item>
              <div v-if="currentMessage.originalContentUrl" class="audio-preview">
                <audio :src="currentMessage.originalContentUrl" controls />
              </div>
            </template>

            <!-- LOCATION 編輯器 -->
            <template v-else-if="currentMessage.type === 'LOCATION'">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="地點名稱">
                    <el-input v-model="currentMessage.title" placeholder="例如：台北 101" maxlength="100" />
                  </el-form-item>
                  <el-form-item label="地址">
                    <el-input v-model="currentMessage.address" placeholder="例如：台北市信義區" maxlength="100" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="緯度">
                    <el-input-number v-model="currentMessage.latitude" :precision="6" :step="0.000001" controls-position="right" style="width: 100%" />
                  </el-form-item>
                  <el-form-item label="經度">
                    <el-input-number v-model="currentMessage.longitude" :precision="6" :step="0.000001" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </template>

            <!-- STICKER 編輯器 -->
            <template v-else-if="currentMessage.type === 'STICKER'">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="貼圖包 ID">
                    <el-input v-model="currentMessage.packageId" placeholder="例如：446" />
                  </el-form-item>
                  <el-form-item label="貼圖 ID">
                    <el-input v-model="currentMessage.stickerId" placeholder="例如：1988" />
                  </el-form-item>
                  <div class="sticker-actions">
                    <el-button type="primary" @click="openStickerSelector">
                      <el-icon><PriceTag /></el-icon>
                      從貼圖庫選擇
                    </el-button>
                    <el-link type="info" href="https://developers.line.biz/en/docs/messaging-api/sticker-list/" target="_blank">
                      查看完整列表
                    </el-link>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="media-preview">
                    <template v-if="stickerPreviewUrl">
                      <img 
                        v-if="!stickerError" 
                        :src="stickerPreviewUrl" 
                        alt="sticker" 
                        @error="handleStickerError"
                      />
                      <div v-else class="sticker-error">
                        <el-icon :size="32" color="#F56C6C"><WarningFilled /></el-icon>
                        <div>貼圖載入失敗</div>
                        <div class="error-hint">請確認貼圖包 ID 和貼圖 ID 是否正確</div>
                      </div>
                    </template>
                    <el-empty v-else description="請選擇或輸入貼圖 ID" :image-size="60" />
                  </div>
                </el-col>
              </el-row>
            </template>

            <!-- FLEX 編輯器 -->
            <template v-else-if="currentMessage.type === 'FLEX'">
              <el-row :gutter="16">
                <el-col :span="14">
                  <!-- 範本選擇 -->
                  <el-form-item label="選擇範本">
                    <div class="flex-template-selectors">
                      <!-- 系統範本 -->
                      <el-select 
                        v-model="selectedFlexPreset" 
                        placeholder="系統範本" 
                        :loading="flexPresetsLoading" 
                        clearable
                        filterable
                        class="flex-template-select"
                        @change="handleFlexPresetChange"
                      >
                        <el-option v-for="preset in flexPresets" :key="preset.name" :label="preset.label" :value="preset.name">
                          <div class="preset-option">
                            <span class="preset-label">{{ preset.label }}</span>
                            <span class="preset-desc">{{ preset.description }}</span>
                          </div>
                        </el-option>
                      </el-select>
                      <!-- 我的範本 -->
                      <el-select 
                        v-model="selectedMyTemplate" 
                        placeholder="我的範本" 
                        clearable
                        filterable
                        class="flex-template-select"
                        popper-class="flex-template-dropdown"
                        @change="handleMyTemplateChange"
                      >
                        <el-option 
                          v-for="tpl in myFlexTemplates" 
                          :key="tpl.flexTemplateId" 
                          :label="tpl.templateName" 
                          :value="tpl.flexTemplateId"
                        >
                          <div class="preset-option-with-action">
                            <div class="preset-info">
                              <span class="preset-label">{{ tpl.templateName }}</span>
                              <span class="preset-desc">{{ tpl.description || '私人' }}</span>
                            </div>
                            <el-button 
                              type="danger" 
                              link 
                              size="small" 
                              @click.stop="handleDeleteMyTemplate(tpl)"
                              title="刪除此範本"
                            >
                              <el-icon><Delete /></el-icon>
                            </el-button>
                          </div>
                        </el-option>
                      </el-select>
                      <!-- 共用範本 -->
                      <el-select 
                        v-model="selectedSharedTemplate" 
                        placeholder="共用範本" 
                        clearable
                        filterable
                        class="flex-template-select"
                        @change="handleSharedTemplateChange"
                      >
                        <el-option 
                          v-for="tpl in sharedFlexTemplates" 
                          :key="tpl.flexTemplateId" 
                          :label="tpl.templateName" 
                          :value="tpl.flexTemplateId"
                        >
                          <div class="preset-option">
                            <span class="preset-label">{{ tpl.templateName }}</span>
                            <span class="preset-desc">{{ tpl.creatorName || '共用' }}</span>
                          </div>
                        </el-option>
                      </el-select>
                    </div>
                  </el-form-item>

                  <el-form-item label="替代文字" required>
                    <el-input v-model="currentMessage.altText" placeholder="不支援 Flex 時顯示的文字（必填）" maxlength="400" />
                    <div class="form-tip">※ LINE APP 通知或聊天列表預覽時顯示的文字</div>
                  </el-form-item>

                  <!-- 可編輯欄位（從範本解析的 {{變數}} ） -->
                  <template v-if="flexEditableFields.length > 0">
                    <div class="editable-fields-section">
                      <div class="section-subtitle">
                        可編輯變數
                        <el-tag type="info" size="small" style="margin-left: 8px">
                          共 {{ flexEditableFields.length }} 個
                        </el-tag>
                      </div>
                      <el-form-item v-for="field in flexEditableFields" :key="field.name" :label="field.label">
                        <!-- 顏色類型 -->
                        <div v-if="field.type === 'color'" class="color-input-wrapper">
                          <el-color-picker
                            v-model="flexVariableValues[field.name]"
                            @change="updateFlexVariable(field.name, $event, 'color')"
                            show-alpha
                          />
                          <el-input
                            v-model="flexVariableValues[field.name]"
                            :placeholder="field.placeholder"
                            @input="updateFlexVariable(field.name, $event, 'color')"
                            style="flex: 1; margin-left: 8px;"
                          />
                        </div>
                        <!-- URL/圖片類型 -->
                        <el-input
                          v-else-if="field.type === 'url' || field.type === 'image'"
                          v-model="flexVariableValues[field.name]"
                          :placeholder="field.placeholder"
                          @input="updateFlexVariable(field.name, $event)"
                        >
                          <template #prepend>{{ field.type === 'image' ? '圖片' : 'URL' }}</template>
                          <template #append v-if="field.type === 'image'">
                            <el-button @click="openMediaSelectorForVariable(field.name)">選擇素材</el-button>
                          </template>
                        </el-input>
                        <!-- 一般文字類型 -->
                        <el-input
                          v-else
                          v-model="flexVariableValues[field.name]"
                          :placeholder="field.placeholder"
                          @input="updateFlexVariable(field.name, $event)"
                        />
                      </el-form-item>
                    </div>
                  </template>

                  <!-- JSON 編輯模式切換 -->
                  <el-form-item>
                    <div class="flex-actions">
                      <el-checkbox v-model="showFlexJsonEditor">顯示 JSON 編輯器（進階）</el-checkbox>
                      <el-button size="small" type="primary" plain @click="openImportJsonDialog">
                        <el-icon><Upload /></el-icon>
                        匯入 JSON
                      </el-button>
                      <el-button size="small" type="success" plain @click="openSaveFlexTemplateDialog" :disabled="!flexTemplateRaw">
                        <el-icon><FolderAdd /></el-icon>
                        儲存為範本
                      </el-button>
                    </div>
                  </el-form-item>

                  <el-form-item v-if="showFlexJsonEditor" label="JSON 內容（含 {{變數}} 佔位符）">
                    <div class="flex-toolbar">
                      <el-button size="small" @click="formatFlexJson">格式化</el-button>
                      <el-button size="small" @click="validateFlexJson">驗證</el-button>
                      <el-button size="small" @click="reloadTemplateVariables">重新解析變數</el-button>
                      <el-link type="primary" href="https://developers.line.biz/flex-simulator/" target="_blank" style="margin-left: auto">
                        Flex Simulator
                      </el-link>
                    </div>
                    <el-input
                      v-model="flexTemplateRaw"
                      type="textarea"
                      :rows="12"
                      placeholder="請貼上 Flex Message JSON，使用 {{變數名}} 或 {{變數名:預設值}} 定義可編輯欄位"
                      class="code-textarea"
                      @blur="reloadTemplateVariables"
                    />
                    <div class="form-tip">※ 使用 <code v-text="'{{變數名}}'"></code> 或 <code v-text="'{{變數名:預設值}}'"></code> 語法定義可編輯欄位</div>
                  </el-form-item>
                </el-col>
                <el-col :span="10">
                  <div class="flex-preview-panel">
                    <div class="preview-title">即時預覽</div>
                    <FlexPreview :json-content="currentMessage.contents" :width="280" :show-header="true" />
                  </div>
                </el-col>
              </el-row>
            </template>

            <!-- TEMPLATE 編輯器（模板訊息：Buttons/Confirm/Carousel/Image Carousel） -->
            <template v-else-if="currentMessage.type === 'TEMPLATE'">
              <TemplateMessageEditor
                v-model="currentMessage.templateData"
                @change="handleTemplateMessageChange"
              />
            </template>

            <!-- IMAGEMAP 編輯器 -->
            <template v-else-if="currentMessage.type === 'IMAGEMAP'">
              <!-- 選擇已建立的圖文訊息範本 -->
              <div class="imagemap-template-selector" style="margin-bottom: 16px;">
                <el-form-item label="選擇已建立的圖文訊息" style="margin-bottom: 8px;">
                  <el-select
                    v-model="selectedImagemapTemplateId"
                    placeholder="選擇範本後自動載入內容"
                    clearable
                    filterable
                    style="width: 100%;"
                    :loading="imagemapTemplatesLoading"
                    @change="handleSelectImagemapTemplate"
                    @focus="loadImagemapTemplates"
                  >
                    <el-option
                      v-for="tpl in imagemapTemplateList"
                      :key="tpl.templateId"
                      :label="tpl.templateName"
                      :value="tpl.templateId"
                    >
                      <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span>{{ tpl.templateName }}</span>
                        <span style="color: #909399; font-size: 12px;">{{ tpl.templateCode || '' }}</span>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
                <div style="color: #909399; font-size: 12px; margin-bottom: 12px;">
                  💡 可從「圖文訊息管理」建立的範本中選擇，或在下方自行編輯
                </div>
              </div>
              <ImagemapEditor
                v-model="currentMessage.imagemapData"
                @change="onImagemapChange"
              />
            </template>
          </div>
        </div>

        <!-- 備註 -->
        <div class="section">
          <el-form-item label="備註">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="選填" maxlength="500" />
          </el-form-item>
        </div>
      </el-form>
    </div>

    <!-- 素材選擇器 -->
    <MediaSelector
      v-model="mediaSelectorVisible"
      :media-type="mediaSelectorType"
      @select="handleMediaSelect"
    />

    <!-- 貼圖選擇器 -->
    <StickerSelector
      v-model="stickerSelectorVisible"
      :current-package-id="currentMessage?.packageId"
      :current-sticker-id="currentMessage?.stickerId"
      @select="handleStickerSelect"
    />

    <!-- Emoji 選擇器 -->
    <EmojiSelector
      v-model="emojiSelectorVisible"
      @select="handleEmojiSelect"
    />

    <!-- 儲存 Flex 範本對話框 -->
    <el-dialog
      v-model="saveFlexTemplateDialogVisible"
      title="儲存為 Flex 範本"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="saveFlexTemplateForm" label-width="100px">
        <el-form-item label="範本名稱" required>
          <el-input v-model="saveFlexTemplateForm.templateName" placeholder="請輸入範本名稱" maxlength="100" />
        </el-form-item>
        <el-form-item label="替代文字">
          <el-input v-model="saveFlexTemplateForm.altText" placeholder="不支援時顯示的文字" maxlength="400" />
        </el-form-item>
        <el-form-item label="範本說明">
          <el-input v-model="saveFlexTemplateForm.description" type="textarea" :rows="2" placeholder="選填，描述此範本用途" maxlength="500" />
        </el-form-item>
        <el-form-item label="公開範本">
          <el-switch v-model="saveFlexTemplateForm.isPublic" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 8px; color: #909399; font-size: 12px;">公開後其他使用者也可選用此範本</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveFlexTemplateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveFlexTemplateLoading" @click="handleSaveFlexTemplate">儲存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  LINE_TEMPLATE_SEND
} from '@/constants/permissions'
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, ChatLineSquare, Picture, VideoCamera, Headset, Location, PriceTag, Grid, Document, ArrowDown, Upload, WarningFilled, Star, Close, Rank, Postcard, FolderAdd } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'
import draggable from 'vuedraggable'
import FlexPreview from '@/components/Line/FlexPreview.vue'
import MediaSelector from './MediaSelector.vue'
import ImagemapEditor from './ImagemapEditor.vue'
import StickerSelector from './StickerSelector.vue'
import EmojiSelector from './EmojiSelector.vue'
import TemplateMessageEditor from './TemplateMessageEditor.vue'
import { getFlexPresets, getFlexPresetContent, sendTestMessage as sendTestApi, listTemplate, getTemplate } from '@/api/line/template'
import { getAvailableFlexTemplates, addFlexTemplate, updateFlexTemplate, delFlexTemplate } from '@/api/line/flexTemplate'
import { listUser as listLineUser } from '@/api/line/user'

const props = defineProps({
  template: { type: Object, default: null },
  flexTemplates: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['save', 'cancel'])

const msgTypeOptions = [
  { value: 'TEXT', label: '文字', icon: ChatLineSquare, hint: 'text' },
  { value: 'IMAGE', label: '圖片', icon: Picture, hint: 'image' },
  { value: 'VIDEO', label: '影片', icon: VideoCamera, hint: 'video' },
  { value: 'AUDIO', label: '音訊', icon: Headset, hint: 'audio' },
  { value: 'LOCATION', label: '位置', icon: Location, hint: 'location' },
  { value: 'STICKER', label: '貼圖', icon: PriceTag, hint: 'sticker' },
  { value: 'TEMPLATE', label: '模板訊息', icon: Postcard, hint: 'buttons/carousel' },
  { value: 'IMAGEMAP', label: '圖文訊息', icon: Grid, hint: 'imagemap' },
  { value: 'FLEX', label: 'Flex 訊息', icon: Document, hint: 'flex' }
]

const formRef = ref(null)
const saving = ref(false)
const activeMessageIndex = ref(0)

// 推播測試相關
const testDialogVisible = ref(false)
const testLineUserId = ref('')
const testSending = ref(false)
const lineUserList = ref([])
const lineUsersLoading = ref(false)

// 圖文訊息範本選擇相關
const imagemapTemplateList = ref([])
const imagemapTemplatesLoading = ref(false)
const selectedImagemapTemplateId = ref(null)

const loadImagemapTemplates = async () => {
  imagemapTemplatesLoading.value = true
  try {
    const res = await listTemplate({ msgType: 'IMAGEMAP', status: 1, pageNum: 1, pageSize: 100 })
    imagemapTemplateList.value = res.rows || []
  } catch (e) {
    console.error('載入圖文訊息範本失敗', e)
  } finally {
    imagemapTemplatesLoading.value = false
  }
}

const handleSelectImagemapTemplate = async (templateId) => {
  if (!templateId || !currentMessage.value) return
  
  try {
    const res = await getTemplate(templateId)
    if (res.data && res.data.content) {
      const imagemapData = JSON.parse(res.data.content)
      currentMessage.value.imagemapData = imagemapData
      currentMessage.value.contents = JSON.stringify(imagemapData, null, 2)
      currentMessage.value.altText = imagemapData.altText || '圖片訊息'
      // 記錄引用的圖文範本 ID
      currentMessage.value.imagemapSourceId = templateId
      ElMessage.success('已載入圖文訊息範本')
    }
  } catch (e) {
    ElMessage.error('載入範本失敗：' + (e.message || '未知錯誤'))
  }
}

const openTestDialog = () => {
  testLineUserId.value = ''
  lineUserList.value = []
  testDialogVisible.value = true
  // 預載入使用者列表
  searchLineUsers('')
}

const searchLineUsers = async (query) => {
  lineUsersLoading.value = true
  try {
    const res = await listLineUser({ lineDisplayName: query, pageNum: 1, pageSize: 20 })
    lineUserList.value = res.rows || []
  } catch (e) {
    console.error('搜尋 LINE 使用者失敗', e)
  } finally {
    lineUsersLoading.value = false
  }
}

const sendTestMessage = async () => {
  if (!testLineUserId.value || !form.templateId) return

  testSending.value = true
  try {
    await sendTestApi(form.templateId, testLineUserId.value)
    ElMessage.success('測試訊息已發送')
    testDialogVisible.value = false
  } catch (e) {
    ElMessage.error('發送失敗：' + (e.message || '未知錯誤'))
  } finally {
    testSending.value = false
  }
}

const form = reactive({
  templateId: null,
  templateName: '',
  templateCode: '',
  status: 1,
  sortOrder: 0,
  remark: ''
})

const messages = ref([])

const isNew = computed(() => !form.templateId)
const currentMessage = computed(() => messages.value[activeMessageIndex.value])

// 貼圖預覽相關
const stickerError = ref(false)

const stickerPreviewUrl = computed(() => {
  if (currentMessage.value?.type === 'STICKER' && currentMessage.value.packageId && currentMessage.value.stickerId) {
    // 重置錯誤狀態
    stickerError.value = false
    // 使用 android 路徑而非 iPhone（根據 LINE API 文件）
    return `https://stickershop.line-scdn.net/stickershop/v1/sticker/${currentMessage.value.stickerId}/android/sticker.png`
  }
  return ''
})

const handleStickerError = () => {
  stickerError.value = true
}

// 貼圖選擇器
const stickerSelectorVisible = ref(false)

const openStickerSelector = () => {
  stickerSelectorVisible.value = true
}

const handleStickerSelect = (sticker) => {
  if (currentMessage.value) {
    currentMessage.value.packageId = sticker.packageId
    currentMessage.value.stickerId = sticker.stickerId
    stickerError.value = false
  }
}

// Emoji 選擇器
const emojiSelectorVisible = ref(false)
const textInputRef = ref(null)

const openEmojiSelector = () => {
  emojiSelectorVisible.value = true
}

const handleEmojiSelect = (emojis) => {
  if (currentMessage.value && currentMessage.value.type === 'TEXT') {
    // emojis 現在是陣列（多選模式）
    const emojiList = Array.isArray(emojis) ? emojis : [emojis]
    
    // 初始化 emojis 陣列
    if (!currentMessage.value.emojis) {
      currentMessage.value.emojis = []
    }
    
    // 批次插入所有選擇的 emoji
    let currentText = currentMessage.value.text || ''
    for (const emoji of emojiList) {
      // 在文字末尾插入 $ 佔位符
      currentText = currentText + (currentText ? '' : '') + '$'
      
      // 添加 emoji 資訊（不計算 index，將在保存時根據 $ 位置重新計算）
      currentMessage.value.emojis.push({
        productId: emoji.productId,
        emojiId: emoji.emojiId
      })
    }
    currentMessage.value.text = currentText
    
    ElMessage.success(`已插入 ${emojiList.length} 個 Emoji`)
  }
}

// 取得 emoji 圖片 URL
const getEmojiUrl = (productId, emojiId) => {
  return `https://stickershop.line-scdn.net/sticonshop/v1/sticon/${productId}/android/${emojiId}.png`
}

// 移除指定位置的 emoji（同時移除對應的 $ 符號）
const removeEmojiAt = (index) => {
  if (!currentMessage.value || !currentMessage.value.emojis) return
  
  // 移除 emoji
  currentMessage.value.emojis.splice(index, 1)
  
  // 移除對應的 $ 符號
  const text = currentMessage.value.text || ''
  let dollarCount = 0
  let newText = ''
  for (let i = 0; i < text.length; i++) {
    if (text[i] === '$') {
      if (dollarCount !== index) {
        newText += text[i]
      }
      dollarCount++
    } else {
      newText += text[i]
    }
  }
  currentMessage.value.text = newText
  ElMessage.success('已移除 Emoji')
}

// 清除全部 emoji
const clearAllEmojisFromMessage = () => {
  if (!currentMessage.value) return
  currentMessage.value.emojis = []
  // 移除所有 $ 符號
  currentMessage.value.text = (currentMessage.value.text || '').replace(/\$/g, '')
  ElMessage.success('已清除全部 Emoji')
}

// Quick Reply 相關函數
const selectedQuickReplyIndex = ref(0)

const selectedQuickReplyItem = computed(() => {
  if (!currentMessage.value?.quickReply?.items?.length) return null
  if (selectedQuickReplyIndex.value < 0 || selectedQuickReplyIndex.value >= currentMessage.value.quickReply.items.length) {
    return null
  }
  return currentMessage.value.quickReply.items[selectedQuickReplyIndex.value]
})

const addQuickReplyItem = () => {
  if (!currentMessage.value) return
  
  // 初始化 quickReply 結構
  if (!currentMessage.value.quickReply) {
    currentMessage.value.quickReply = { items: [] }
  }
  if (!currentMessage.value.quickReply.items) {
    currentMessage.value.quickReply.items = []
  }
  
  // 檢查是否已達上限
  if (currentMessage.value.quickReply.items.length >= 13) {
    ElMessage.warning('快速回覆按鈕最多 13 個')
    return
  }
  
  // 新增預設項目
  currentMessage.value.quickReply.items.push({
    type: 'action',
    imageUrl: '',
    action: {
      type: 'message',
      label: '',
      text: ''
    }
  })
  
  // 自動選中新增的項目
  selectedQuickReplyIndex.value = currentMessage.value.quickReply.items.length - 1
}

const removeQuickReplyItem = (index) => {
  if (!currentMessage.value?.quickReply?.items) return
  currentMessage.value.quickReply.items.splice(index, 1)
  
  // 調整選中索引
  if (currentMessage.value.quickReply.items.length === 0) {
    selectedQuickReplyIndex.value = 0
  } else if (selectedQuickReplyIndex.value >= currentMessage.value.quickReply.items.length) {
    selectedQuickReplyIndex.value = currentMessage.value.quickReply.items.length - 1
  }
}

// 監聽 Quick Reply action 類型變化，初始化必要欄位
let previousActionType = null
watch(
  () => selectedQuickReplyItem.value?.action?.type,
  (newType, oldType) => {
    if (!selectedQuickReplyItem.value || !newType || newType === oldType) return
    
    const action = selectedQuickReplyItem.value.action
    
    // 根據新類型初始化必要欄位
    switch (newType) {
      case 'message':
        if (!action.text) action.text = ''
        break
      case 'uri':
        if (!action.uri) action.uri = ''
        break
      case 'postback':
        if (!action.data) action.data = ''
        break
      case 'datetimepicker':
        if (!action.data) action.data = ''
        if (!action.mode) action.mode = 'datetime'
        break
      case 'clipboard':
        if (!action.clipboardText) action.clipboardText = ''
        break
      // camera, cameraRoll, location 只需要 type 和 label，無需額外初始化
    }
  },
  { immediate: false }
)

// 即時預覽：將文字中的 $ 替換為 emoji 圖片
const textPreviewParts = computed(() => {
  if (!currentMessage.value || currentMessage.value.type !== 'TEXT') return []
  
  const text = currentMessage.value.text || ''
  const emojis = currentMessage.value.emojis || []
  
  if (!text) return []
  
  let result = []
  let emojiIndex = 0
  let lastPos = 0
  
  for (let i = 0; i < text.length; i++) {
    if (text[i] === '$') {
      // 添加 $ 之前的文字
      if (i > lastPos) {
        result.push({ type: 'text', content: text.substring(lastPos, i) })
      }
      // 如果有對應的 emoji，顯示 emoji；否則顯示 $
      if (emojiIndex < emojis.length) {
        const emoji = emojis[emojiIndex]
        result.push({
          type: 'emoji',
          url: getEmojiUrl(emoji.productId, emoji.emojiId)
        })
        emojiIndex++
      } else {
        result.push({ type: 'text', content: '$' })
      }
      lastPos = i + 1
    }
  }
  
  // 添加剩餘的文字
  if (lastPos < text.length) {
    result.push({ type: 'text', content: text.substring(lastPos) })
  }
  
  return result
})

// 重新計算文字中所有 $ 的 index 並匹配對應的 emoji
const recalculateEmojiIndices = (text, emojis) => {
  if (!text || !emojis || emojis.length === 0) return []
  
  // 找出所有 $ 的位置
  const dollarIndices = []
  for (let i = 0; i < text.length; i++) {
    if (text[i] === '$') {
      dollarIndices.push(i)
    }
  }
  
  // 如果 $ 數量與 emoji 數量不匹配，只處理較少的那個
  const count = Math.min(dollarIndices.length, emojis.length)
  const result = []
  
  for (let i = 0; i < count; i++) {
    result.push({
      index: dollarIndices[i],
      productId: emojis[i].productId,
      emojiId: emojis[i].emojiId
    })
  }
  
  return result
}

const rules = {
  templateName: [{ required: true, message: '請輸入範本名稱', trigger: 'blur' }]
}

// 素材選擇器
const mediaSelectorVisible = ref(false)
const mediaSelectorType = ref('image')
const mediaSelectorTargetField = ref('originalContentUrl')
const mediaSelectorTargetVariable = ref('')  // 用於 Flex 變數的素材選擇

const openMediaSelector = (type, targetField = 'originalContentUrl') => {
  mediaSelectorType.value = type
  mediaSelectorTargetField.value = targetField
  mediaSelectorTargetVariable.value = ''  // 清除變數目標
  mediaSelectorVisible.value = true
}

// 為 Flex 變數開啟素材選擇器
const openMediaSelectorForVariable = (varName) => {
  mediaSelectorType.value = 'image'
  mediaSelectorTargetField.value = ''
  mediaSelectorTargetVariable.value = varName
  mediaSelectorVisible.value = true
}

const handleMediaSelect = (media) => {
  // 處理 Flex 變數的素材選擇
  if (mediaSelectorTargetVariable.value) {
    flexVariableValues.value[mediaSelectorTargetVariable.value] = media.url
    updateFlexPreview()
    mediaSelectorTargetVariable.value = ''
    return
  }

  if (!currentMessage.value) return
  const targetField = mediaSelectorTargetField.value || 'originalContentUrl'
  currentMessage.value[targetField] = media.url

  if (currentMessage.value.type === 'IMAGE' && targetField === 'originalContentUrl' && !currentMessage.value.previewImageUrl) {
    currentMessage.value.previewImageUrl = media.url
  }

  if (currentMessage.value.type === 'VIDEO' && targetField === 'originalContentUrl' && !currentMessage.value.previewImageUrl) {
    ElMessage.warning('影片預覽圖需要圖片網址，請按「選擇素材」挑一張圖片')
  }

  if (mediaSelectorType.value === 'audio' && media.durationMs) {
    currentMessage.value.duration = media.durationMs
  }
}

// JSON 匯入對話框
const importJsonDialogVisible = ref(false)
const importJsonTab = ref('paste')
const importJsonText = ref('')
const importJsonFiles = ref([])
const importJsonError = ref('')
const importJsonLoading = ref(false)
const jsonUploadRef = ref(null)

const openImportJsonDialog = () => {
  importJsonText.value = ''
  importJsonFiles.value = []
  importJsonError.value = ''
  importJsonTab.value = 'paste'
  importJsonDialogVisible.value = true
}

const handleJsonFileChange = (file, fileList) => {
  importJsonFiles.value = fileList
  importJsonError.value = ''
}

const handleJsonFileRemove = (file, fileList) => {
  importJsonFiles.value = fileList
}

/**
 * 驗證 Flex JSON 格式
 * @param {string} jsonStr - JSON 字串
 * @returns {{ valid: boolean, error: string, json: object }} 驗證結果
 */
const validateFlexContent = (jsonStr) => {
  try {
    const json = JSON.parse(jsonStr)

    // 檢查是否為有效的 Flex Message 結構
    if (!json.type) {
      return { valid: false, error: '缺少 type 欄位', json: null }
    }

    const validTypes = ['bubble', 'carousel']
    if (!validTypes.includes(json.type)) {
      return { valid: false, error: `type 必須是 bubble 或 carousel，目前是「${json.type}」`, json: null }
    }

    // carousel 類型需要有 contents 陣列
    if (json.type === 'carousel') {
      if (!Array.isArray(json.contents) || json.contents.length === 0) {
        return { valid: false, error: 'carousel 類型必須包含 contents 陣列', json: null }
      }
      // 檢查每個 bubble
      for (let i = 0; i < json.contents.length; i++) {
        const bubble = json.contents[i]
        if (bubble.type !== 'bubble') {
          return { valid: false, error: `carousel.contents[${i}].type 必須是 bubble`, json: null }
        }
      }
    }

    // bubble 類型檢查基本結構
    if (json.type === 'bubble') {
      const validSections = ['header', 'hero', 'body', 'footer', 'styles', 'size', 'direction']
      const keys = Object.keys(json).filter(k => k !== 'type')
      if (keys.length === 0) {
        return { valid: false, error: 'bubble 必須至少包含 header、hero、body 或 footer 其中之一', json: null }
      }
    }

    return { valid: true, error: '', json }
  } catch (err) {
    return { valid: false, error: 'JSON 格式錯誤：' + err.message, json: null }
  }
}

const confirmImportJson = async () => {
  importJsonError.value = ''
  importJsonLoading.value = true

  try {
    if (importJsonTab.value === 'paste') {
      // 貼上模式
      const text = importJsonText.value.trim()
      if (!text) {
        importJsonError.value = '請輸入 JSON 內容'
        return
      }

      const result = validateFlexContent(text)
      if (!result.valid) {
        importJsonError.value = result.error
        return
      }

      // 格式化並儲存
      const formatted = JSON.stringify(result.json, null, 2)
      flexTemplateRaw.value = formatted
      parseFlexVariables(formatted)
      updateFlexPreview()

      importJsonDialogVisible.value = false

      if (flexEditableFields.value.length > 0) {
        ElMessage.success(`已匯入 JSON，共解析出 ${flexEditableFields.value.length} 個可編輯變數`)
      } else {
        ElMessage.info('已匯入 JSON，未發現 {{變數}} 佔位符')
      }

    } else {
      // 檔案模式
      if (importJsonFiles.value.length === 0) {
        importJsonError.value = '請選擇至少一個 JSON 檔案'
        return
      }

      // 讀取所有檔案
      const readPromises = importJsonFiles.value.map(fileItem => {
        return new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = (e) => {
            const result = validateFlexContent(e.target.result)
            resolve({
              name: fileItem.name,
              ...result,
              content: e.target.result
            })
          }
          reader.onerror = () => reject(new Error(`讀取 ${fileItem.name} 失敗`))
          reader.readAsText(fileItem.raw)
        })
      })

      const results = await Promise.all(readPromises)

      // 檢查是否有錯誤
      const errors = results.filter(r => !r.valid)
      if (errors.length > 0) {
        importJsonError.value = errors.map(e => `${e.name}: ${e.error}`).join('\n')
        return
      }

      // 如果只有一個檔案，直接匯入到當前訊息
      if (results.length === 1) {
        const formatted = JSON.stringify(results[0].json, null, 2)
        flexTemplateRaw.value = formatted
        parseFlexVariables(formatted)
        updateFlexPreview()

        importJsonDialogVisible.value = false

        if (flexEditableFields.value.length > 0) {
          ElMessage.success(`已匯入 ${results[0].name}，共解析出 ${flexEditableFields.value.length} 個可編輯變數`)
        } else {
          ElMessage.info(`已匯入 ${results[0].name}，未發現 {{變數}} 佔位符`)
        }
      } else {
        // 多個檔案：第一個匯入到當前訊息，其餘新增為新訊息
        const first = results[0]
        const formatted = JSON.stringify(first.json, null, 2)
        flexTemplateRaw.value = formatted
        parseFlexVariables(formatted)
        updateFlexPreview()

        // 新增其他訊息
        for (let i = 1; i < results.length; i++) {
          if (messages.value.length >= 5) {
            ElMessage.warning(`已達訊息上限 5 個，剩餘 ${results.length - i} 個檔案未匯入`)
            break
          }
          const msg = {
            type: 'FLEX',
            altText: results[i].name.replace('.json', ''),
            contents: JSON.stringify(results[i].json, null, 2)
          }
          messages.value.push(msg)
        }

        importJsonDialogVisible.value = false
        ElMessage.success(`已匯入 ${Math.min(results.length, 5)} 個 JSON 檔案`)
      }
    }
  } catch (err) {
    importJsonError.value = err.message
  } finally {
    importJsonLoading.value = false
  }
}

// 重新解析範本中的變數
const reloadTemplateVariables = () => {
  if (!flexTemplateRaw.value) {
    ElMessage.warning('請先載入或匯入範本')
    return
  }

  parseFlexVariables(flexTemplateRaw.value)
  updateFlexPreview()

  if (flexEditableFields.value.length > 0) {
    ElMessage.success(`已重新解析，共 ${flexEditableFields.value.length} 個可編輯變數`)
  } else {
    ElMessage.info('未發現 {{變數}} 佔位符')
  }
}

const getMsgTypeLabel = (type) => msgTypeOptions.find(o => o.value === type)?.label || type
const getMsgTypeIcon = (type) => msgTypeOptions.find(o => o.value === type)?.icon || Document

// 產生唯一的拖曳 key
let dragKeyCounter = 0
const generateDragKey = () => `msg_${Date.now()}_${dragKeyCounter++}`

const addMessage = (type) => {
  const newMsg = { type, _dragKey: generateDragKey() }
  switch (type) {
    case 'TEXT':
      newMsg.text = ''
      break
    case 'IMAGE':
    case 'VIDEO':
      newMsg.originalContentUrl = ''
      newMsg.previewImageUrl = ''
      break
    case 'AUDIO':
      newMsg.originalContentUrl = ''
      newMsg.duration = 60000
      break
    case 'LOCATION':
      newMsg.title = ''
      newMsg.address = ''
      newMsg.latitude = 25.033976
      newMsg.longitude = 121.564472
      break
    case 'STICKER':
      newMsg.packageId = ''
      newMsg.stickerId = ''
      break
    case 'TEMPLATE':
      newMsg.altText = ''
      newMsg.templateData = {}
      newMsg.contents = ''
      break
    case 'FLEX':
    case 'IMAGEMAP':
      newMsg.altText = ''
      newMsg.contents = ''
      break
  }
  messages.value.push(newMsg)
  activeMessageIndex.value = messages.value.length - 1
}

const removeMessage = (index) => {
  messages.value.splice(index, 1)
  if (activeMessageIndex.value >= messages.value.length) {
    activeMessageIndex.value = Math.max(0, messages.value.length - 1)
  }
}

// 拖曳結束後的處理
const onDragEnd = (evt) => {
  // 更新選中的訊息索引，跟隨拖曳後的位置
  if (evt.oldIndex === activeMessageIndex.value) {
    activeMessageIndex.value = evt.newIndex
  } else if (evt.oldIndex < activeMessageIndex.value && evt.newIndex >= activeMessageIndex.value) {
    activeMessageIndex.value--
  } else if (evt.oldIndex > activeMessageIndex.value && evt.newIndex <= activeMessageIndex.value) {
    activeMessageIndex.value++
  }
}

// Template Message 變更處理
const handleTemplateMessageChange = (data) => {
  if (!currentMessage.value) return
  currentMessage.value.templateData = data
  currentMessage.value.altText = data.altText || '模板訊息'
  currentMessage.value.contents = data.content || ''
}

const formatFlexJson = () => {
  if (!currentMessage.value) return
  try {
    currentMessage.value.contents = JSON.stringify(JSON.parse(currentMessage.value.contents), null, 2)
    ElMessage.success('格式化成功')
  } catch (e) {
    ElMessage.error('JSON 格式錯誤')
  }
}

const validateFlexJson = () => {
  if (!currentMessage.value) return
  try {
    const obj = JSON.parse(currentMessage.value.contents)
    if (!['bubble', 'carousel'].includes(obj.type)) {
      ElMessage.warning('Flex Container 類型必須是 bubble 或 carousel')
      return
    }
    ElMessage.success('JSON 格式正確')
  } catch (e) {
    ElMessage.error('JSON 格式錯誤：' + e.message)
  }
}

// Flex 範本相關
const flexPresets = ref([])
const customFlexTemplates = ref([])  // 使用者自訂範本（包含我的和共用）
const flexPresetsLoading = ref(false)
const selectedFlexPreset = ref('')  // 系統範本選擇
const selectedMyTemplate = ref(null)  // 我的範本選擇
const selectedSharedTemplate = ref(null)  // 共用範本選擇
const flexEditableFields = ref([])  // 變數欄位列表
const showFlexJsonEditor = ref(false)
const flexTemplateRaw = ref('')  // 原始範本 JSON（含 {{變數}} 佔位符）
const flexVariableValues = ref({})  // 變數值對應表 { 變數名: 值 }
const isRestoringFlexPreset = ref(false)  // 恢復下拉選單時的標記，避免重複載入
const currentUserId = ref(null)  // 當前使用者 ID

const myFlexTemplates = computed(() => {
  return customFlexTemplates.value.filter(
    t => Number(t.creatorId) === Number(currentUserId.value) && Number(t.isPublic) !== 1
  )
})

const sharedFlexTemplates = computed(() => {
  return customFlexTemplates.value.filter(t => Number(t.isPublic) === 1)
})

// 儲存 Flex 範本相關
const saveFlexTemplateDialogVisible = ref(false)
const saveFlexTemplateLoading = ref(false)
const saveFlexTemplateForm = reactive({
  templateName: '',
  altText: '訊息通知',
  description: '',
  isPublic: 0
})

const loadFlexPresets = async () => {
  flexPresetsLoading.value = true
  try {
    const res = await getFlexPresets()
    flexPresets.value = res.data || []
  } catch (e) {
    console.error('載入 Flex 範本列表失敗', e)
  } finally {
    flexPresetsLoading.value = false
  }
}

// 載入使用者自訂 Flex 範本
const loadCustomFlexTemplates = async () => {
  try {
    const res = await getAvailableFlexTemplates()
    customFlexTemplates.value = res.data || []
  } catch (e) {
    console.error('載入自訂 Flex 範本失敗', e)
  }
}

const clearOtherTemplateSelections = (keepType) => {
  if (keepType !== 'system') selectedFlexPreset.value = ''
  if (keepType !== 'my') selectedMyTemplate.value = null
  if (keepType !== 'shared') selectedSharedTemplate.value = null
}

const handleFlexPresetChange = async (val) => {
  if (!val) return
  clearOtherTemplateSelections('system')
  await loadFlexTemplate(val)
}

const handleMyTemplateChange = async (val) => {
  if (!val) return
  clearOtherTemplateSelections('my')
  await loadFlexTemplateById(val)
}

const handleSharedTemplateChange = async (val) => {
  if (!val) return
  clearOtherTemplateSelections('shared')
  await loadFlexTemplateById(val)
}

const loadFlexTemplateById = async (templateId) => {
  const tpl = customFlexTemplates.value.find(t => Number(t.flexTemplateId) === Number(templateId))
  if (!tpl) {
    ElMessage.error('找不到指定的範本')
    return
  }
  
  if (!currentMessage.value) {
    ElMessage.warning('請先選擇訊息類型')
    return
  }
  
  ElMessage.info('正在載入範本...')
  
  try {
    const content = tpl.flexJson
    if (!content) {
      ElMessage.error('範本內容為空')
      return
    }
    
    // 設定替代文字
    if (tpl.altText) {
      currentMessage.value.altText = tpl.altText
    }
    
    // 儲存原始範本
    flexTemplateRaw.value = content
    
    // 解析變數
    parseFlexVariables(content)
    
    // 更新預覽
    updateFlexPreview()
    
    // 儲存範本名稱
    currentMessage.value.flexPresetName = 'custom_' + templateId
    
    if (flexEditableFields.value.length === 0) {
      ElMessage.info('此範本沒有可編輯的變數，您可以直接編輯 JSON')
    } else {
      ElMessage.success(`已載入範本，共 ${flexEditableFields.value.length} 個可編輯變數`)
    }
  } catch (e) {
    console.error('載入範本失敗', e)
    ElMessage.error('載入範本失敗')
  }
}

const handleDeleteMyTemplate = async (tpl) => {
  try {
    await ElMessageBox.confirm(
      `確定要刪除範本「${tpl.templateName}」嗎？此操作無法復原。`,
      '確認刪除',
      { type: 'warning' }
    )
    
    const res = await delFlexTemplate(tpl.flexTemplateId)
    if (res.code === 200) {
      ElMessage.success('範本已刪除')
      // 如果刪除的是當前選中的範本，清除選擇
      if (selectedMyTemplate.value === tpl.flexTemplateId) {
        selectedMyTemplate.value = null
      }
      // 重新載入範本列表
      await loadCustomFlexTemplates()
    } else {
      ElMessage.error(res.msg || '刪除失敗')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('刪除範本失敗', e)
      ElMessage.error('刪除失敗')
    }
  }
}

const openSaveFlexTemplateDialog = () => {
  if (!flexTemplateRaw.value) {
    ElMessage.warning('請先載入或匯入 Flex JSON')
    return
  }
  // 重置表單
  saveFlexTemplateForm.templateName = ''
  saveFlexTemplateForm.altText = currentMessage.value?.altText || '訊息通知'
  saveFlexTemplateForm.description = ''
  saveFlexTemplateForm.isPublic = 0
  saveFlexTemplateDialogVisible.value = true
}

const handleSaveFlexTemplate = async () => {
  if (!saveFlexTemplateForm.templateName?.trim()) {
    ElMessage.warning('請輸入範本名稱')
    return
  }
  
  const templateName = saveFlexTemplateForm.templateName.trim()
  
  const existingTemplate = customFlexTemplates.value.find(
    t => t.templateName === templateName && Number(t.creatorId) === Number(currentUserId.value)
  )
  
  if (existingTemplate) {
    try {
      await ElMessageBox.confirm(
        `已存在名稱為「${templateName}」的範本，是否要覆蓋原有的 JSON 內容？`,
        '範本名稱重複',
        { 
          type: 'warning',
          confirmButtonText: '覆蓋',
          cancelButtonText: '取消'
        }
      )
      await doSaveFlexTemplate(templateName, existingTemplate.flexTemplateId)
    } catch (e) {
      return
    }
  } else {
    await doSaveFlexTemplate(templateName, null)
  }
}

const doSaveFlexTemplate = async (templateName, existingId) => {
  saveFlexTemplateLoading.value = true
  try {
    const data = {
      templateName: templateName,
      flexJson: flexTemplateRaw.value,
      altText: saveFlexTemplateForm.altText || '訊息通知',
      description: saveFlexTemplateForm.description,
      isPublic: saveFlexTemplateForm.isPublic
    }
    
    let res
    if (existingId) {
      data.flexTemplateId = existingId
      res = await updateFlexTemplate(data)
    } else {
      res = await addFlexTemplate(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(existingId ? '範本已更新' : '範本儲存成功')
      saveFlexTemplateDialogVisible.value = false
      // 重新載入自訂範本列表
      await loadCustomFlexTemplates()
    } else {
      ElMessage.error(res.msg || '儲存失敗')
    }
  } catch (e) {
    console.error('儲存 Flex 範本失敗', e)
    ElMessage.error('儲存失敗：' + (e.message || '未知錯誤'))
  } finally {
    saveFlexTemplateLoading.value = false
  }
}

const loadFlexTemplate = async (templateName) => {
  console.log('[Flex] loadFlexTemplate called, templateName:', templateName)
  console.log('[Flex] currentMessage.value:', currentMessage.value)

  if (!currentMessage.value) {
    console.warn('[Flex] Early return: currentMessage is falsy')
    ElMessage.warning('請先選擇訊息類型')
    return
  }

  if (!templateName) {
    console.warn('[Flex] Early return: templateName is falsy')
    return
  }

  ElMessage.info('正在載入範本...')

  try {
    let content = ''
    
    // 判斷是自訂範本還是系統範本
    if (templateName.startsWith('custom_')) {
      // 自訂範本：從 customFlexTemplates 中查找
      const templateId = parseInt(templateName.replace('custom_', ''))
      const customTpl = customFlexTemplates.value.find(t => t.flexTemplateId === templateId)
      if (customTpl) {
        content = customTpl.flexJson
        // 同時設定替代文字
        if (customTpl.altText && currentMessage.value) {
          currentMessage.value.altText = customTpl.altText
        }
      } else {
        ElMessage.error('找不到指定的自訂範本')
        return
      }
    } else {
      // 系統範本：呼叫 API 取得
      console.log('[Flex] Calling getFlexPresetContent for:', templateName)
      const res = await getFlexPresetContent(templateName)
      console.log('[Flex] API response:', res)

      // 後端 AjaxResult.success(string) 會將字串放在 msg 欄位
      const responseData = res.data || res.msg
      if (responseData) {
        content = typeof responseData === 'string' ? responseData : JSON.stringify(responseData, null, 2)
      }
    }
    
    if (content) {
      console.log('[Flex] Parsed content length:', content.length)
      console.log('[Flex] Content preview:', content.substring(0, 200))

      // 儲存原始範本（含變數佔位符）
      flexTemplateRaw.value = content
      console.log('[Flex] flexTemplateRaw updated')

      // 解析變數並產生輸入框
      parseFlexVariables(content)
      console.log('[Flex] After parseFlexVariables, fields:', flexEditableFields.value.length)
      console.log('[Flex] flexVariableValues:', flexVariableValues.value)

      // 立即用預設值替換並更新預覽
      updateFlexPreview()
      console.log('[Flex] After updateFlexPreview, currentMessage.contents:', currentMessage.value?.contents?.substring(0, 200))

      // 儲存選擇的範本名稱，以便編輯時恢復下拉選單
      currentMessage.value.flexPresetName = templateName

      if (flexEditableFields.value.length === 0) {
        ElMessage.info('此範本沒有可編輯的變數，您可以直接編輯 JSON')
      } else {
        ElMessage.success(`已載入範本，共 ${flexEditableFields.value.length} 個可編輯變數`)
      }
    } else {
      console.warn('[Flex] content is empty')
      ElMessage.error('範本內容為空')
    }
  } catch (e) {
    console.error('[Flex] 載入範本失敗', e)
    ElMessage.error('載入範本失敗')
  }
}

/**
 * 解析 Flex JSON 中的 {{變數名}} 或 {{變數名:預設值}} 格式
 * @param {string} jsonContent - 原始 JSON 內容
 */
const parseFlexVariables = (jsonContent) => {
  if (!jsonContent) {
    flexEditableFields.value = []
    flexVariableValues.value = {}
    return
  }

  // 正則表達式匹配 {{變數名}} 或 {{變數名:預設值}}
  // 支援中文、英文、數字、底線作為變數名
  const variablePattern = /\{\{([^}:]+)(?::([^}]*))?\}\}/g
  const variableMap = new Map()  // 用於去重，保留第一次出現的預設值

  let match
  while ((match = variablePattern.exec(jsonContent)) !== null) {
    const varName = match[1].trim()
    const defaultValue = match[2] !== undefined ? match[2] : ''

    // 只保留第一次出現的預設值（去重）
    if (!variableMap.has(varName)) {
      variableMap.set(varName, {
        name: varName,
        defaultValue: defaultValue,
        value: defaultValue,  // 初始值設為預設值
        type: guessVariableType(varName, defaultValue)
      })
    }
  }

  // 轉換為陣列
  const fields = []
  const values = {}
  variableMap.forEach((variable, name) => {
    fields.push({
      name: variable.name,
      label: variable.name,
      type: variable.type,
      defaultValue: variable.defaultValue,
      placeholder: variable.defaultValue || `請輸入${variable.name}`
    })
    values[name] = variable.value
  })

  flexEditableFields.value = fields
  flexVariableValues.value = values
}

/**
 * 根據變數名和預設值猜測變數類型
 */
const guessVariableType = (varName, defaultValue) => {
  const lowerName = varName.toLowerCase()
  const lowerValue = (defaultValue || '').toLowerCase()

  // 判斷是否為顏色類型（變數名包含「顏色」或「color」，或預設值是 hex 色碼）
  if (lowerName.includes('顏色') || lowerName.includes('color') || lowerName.includes('背景')) {
    return 'color'
  }
  // 檢查是否為 hex 色碼格式 (#RGB, #RRGGBB, #RRGGBBAA)
  if (/^#[0-9a-f]{3,8}$/i.test(defaultValue)) {
    return 'color'
  }

  // 判斷是否為 URL 類型
  if (lowerName.includes('網址') || lowerName.includes('連結') || lowerName.includes('url') || lowerName.includes('link')) {
    return 'url'
  }
  if (lowerValue.startsWith('http://') || lowerValue.startsWith('https://')) {
    return 'url'
  }

  // 判斷是否為圖片類型
  if (lowerName.includes('圖片') || lowerName.includes('image') || lowerName.includes('photo')) {
    return 'image'
  }

  return 'text'
}

/**
 * 將 rgba 顏色轉換為 hex 格式（LINE Flex 需要）
 * @param {string} color - 顏色值（可能是 rgba 或 hex）
 * @returns {string} hex 格式顏色
 */
const rgbaToHex = (color) => {
  if (!color) return color

  // 如果已經是 hex 格式，直接返回
  if (color.startsWith('#')) return color

  // 解析 rgba(r, g, b, a) 或 rgb(r, g, b)
  const match = color.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/)
  if (!match) return color

  const r = parseInt(match[1]).toString(16).padStart(2, '0')
  const g = parseInt(match[2]).toString(16).padStart(2, '0')
  const b = parseInt(match[3]).toString(16).padStart(2, '0')

  // 如果有 alpha 值且不是 1，加上 alpha
  if (match[4] && parseFloat(match[4]) < 1) {
    const a = Math.round(parseFloat(match[4]) * 255).toString(16).padStart(2, '0')
    return `#${r}${g}${b}${a}`.toUpperCase()
  }

  return `#${r}${g}${b}`.toUpperCase()
}

/**
 * 更新變數值並重新產生預覽 JSON
 * @param {string} varName - 變數名
 * @param {string} value - 新值
 * @param {string} fieldType - 欄位類型（可選）
 */
const updateFlexVariable = (varName, value, fieldType) => {
  // 如果是顏色類型，轉換為 hex 格式
  if (fieldType === 'color' || (value && value.toString().startsWith('rgba'))) {
    value = rgbaToHex(value)
  }
  flexVariableValues.value[varName] = value
  updateFlexPreview()
}

/**
 * 用變數值替換範本中的佔位符，產生預覽 JSON
 */
const updateFlexPreview = () => {
  console.log('[Flex] updateFlexPreview called')
  console.log('[Flex] flexTemplateRaw.value exists:', !!flexTemplateRaw.value)
  console.log('[Flex] currentMessage.value exists:', !!currentMessage.value)

  if (!flexTemplateRaw.value || !currentMessage.value) {
    console.warn('[Flex] updateFlexPreview early return')
    return
  }

  let result = flexTemplateRaw.value

  // 替換所有變數
  // 匹配 {{變數名}} 或 {{變數名:預設值}}
  result = result.replace(/\{\{([^}:]+)(?::[^}]*)?\}\}/g, (match, varName) => {
    const trimmedName = varName.trim()
    const value = flexVariableValues.value[trimmedName]
    // 如果有值就用值，否則用空字串（或可以選擇保留預設值）
    return value !== undefined && value !== '' ? value : ''
  })

  console.log('[Flex] Replaced result preview:', result.substring(0, 200))

  // 更新 contents 以觸發預覽更新
  currentMessage.value.contents = result
}

/**
 * 從 JSON 編輯器內容解析變數（當使用者手動編輯 JSON 時）
 */
const parseFlexEditableFields = () => {
  if (!currentMessage.value?.contents) {
    flexEditableFields.value = []
    return
  }

  // 如果沒有原始範本，使用當前內容作為範本
  if (!flexTemplateRaw.value) {
    flexTemplateRaw.value = currentMessage.value.contents
  }

  parseFlexVariables(flexTemplateRaw.value)
}

/**
 * 更新 Flex 欄位（保留舊方法名稱以相容現有程式碼）
 */
const updateFlexField = (index) => {
  const field = flexEditableFields.value[index]
  if (!field) return

  const value = flexVariableValues.value[field.name]
  updateFlexVariable(field.name, value)
}

const userStore = useUserStore()
watch(
  () => userStore.id,
  (val) => {
    currentUserId.value = val
  },
  { immediate: true }
)

loadFlexPresets()
loadCustomFlexTemplates()

// Imagemap 編輯器變更處理
const onImagemapChange = (imagemapData) => {
  console.log('[TemplateEditor] onImagemapChange:', imagemapData)
  if (!currentMessage.value) return

  // 將 imagemapData 轉換為 JSON 字串存入 contents
  currentMessage.value.contents = JSON.stringify(imagemapData, null, 2)
  currentMessage.value.altText = imagemapData.altText || '圖片訊息'
}

const handleCancel = () => {
  emit('cancel')
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    // 表單驗證失敗，滾動到第一個錯誤欄位
    nextTick(() => {
      const errorEl = document.querySelector('.el-form-item.is-error')
      if (errorEl) {
        errorEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    })
    return
  }

  // 驗證訊息內容
  for (let i = 0; i < messages.value.length; i++) {
    const msg = messages.value[i]
    switch (msg.type) {
      case 'TEXT':
        if (!msg.text?.trim()) {
          ElMessage.warning(`訊息 ${i + 1}：請輸入文字內容`)
          return
        }
        break
      case 'IMAGE':
        if (!msg.originalContentUrl) {
          ElMessage.warning(`訊息 ${i + 1}：請輸入圖片網址`)
          return
        }
        break
      case 'VIDEO':
        if (!msg.originalContentUrl || !msg.previewImageUrl) {
          ElMessage.warning(`訊息 ${i + 1}：請輸入影片網址和預覽圖網址`)
          return
        }
        break
      case 'AUDIO':
        if (!msg.originalContentUrl) {
          ElMessage.warning(`訊息 ${i + 1}：請輸入音訊網址`)
          return
        }
        break
      case 'LOCATION':
        if (!msg.title || !msg.address) {
          ElMessage.warning(`訊息 ${i + 1}：請輸入地點名稱和地址`)
          return
        }
        break
      case 'STICKER':
        if (!msg.packageId || !msg.stickerId) {
          ElMessage.warning(`訊息 ${i + 1}：請輸入貼圖包 ID 和貼圖 ID`)
          return
        }
        break
      case 'FLEX':
        if (!msg.contents?.trim() || !msg.altText?.trim()) {
          ElMessage.warning(`訊息 ${i + 1}：請輸入 JSON 內容和替代文字`)
          return
        }
        try {
          JSON.parse(msg.contents)
        } catch (e) {
          ElMessage.error(`訊息 ${i + 1}：JSON 格式錯誤`)
          return
        }
        break
      case 'IMAGEMAP':
        // 檢查 imagemapData 或直接在 msg 上的屬性（扁平結構）
        const imagemapData = msg.imagemapData || (msg.baseUrl ? msg : null)
        if (imagemapData) {
          if (!imagemapData.baseUrl) {
            ElMessage.warning(`訊息 ${i + 1}：請先上傳圖片`)
            return
          }
          if (!imagemapData.altText?.trim()) {
            ElMessage.warning(`訊息 ${i + 1}：請輸入替代文字`)
            return
          }
          if (!imagemapData.actions || imagemapData.actions.length === 0) {
            ElMessage.warning(`訊息 ${i + 1}：請至少新增一個熱區`)
            return
          }

          // 驗證熱區內容
          const validSchemes = ['http', 'https', 'tel', 'mailto', 'line', 'linemusic']
          for (let j = 0; j < imagemapData.actions.length; j++) {
            const action = imagemapData.actions[j]
            if (action.type === 'uri') {
              if (!action.linkUri?.trim()) {
                ElMessage.warning(`訊息 ${i + 1}：熱區 ${j + 1} 請輸入連結網址`)
                return
              }
              const schemeMatch = action.linkUri.trim().match(/^([a-zA-Z]+):/)
              if (!schemeMatch || !validSchemes.includes(schemeMatch[1].toLowerCase())) {
                ElMessage.warning(`訊息 ${i + 1}：熱區 ${j + 1} 連結網址格式錯誤，必須以 http, https, tel, mailto, line, linemusic 開頭`)
                return
              }
            } else if (action.type === 'message') {
              if (!action.text?.trim()) {
                ElMessage.warning(`訊息 ${i + 1}：熱區 ${j + 1} 請輸入訊息文字`)
                return
              }
            } else if (action.type === 'clipboard') {
              if (!action.clipboardText?.trim()) {
                ElMessage.warning(`訊息 ${i + 1}：熱區 ${j + 1} 請輸入複製內容`)
                return
              }
            }
          }

          // 資料同步：將編輯器中的 imagemapData 轉回 JSON 字串存入 contents
          console.log('[handleSave] Syncing Imagemap data:', imagemapData)
          msg.contents = JSON.stringify(imagemapData, null, 2)
          console.log('[handleSave] Updated contents:', msg.contents)
          msg.altText = imagemapData.altText

          // 移除可能存在的扁平結構欄位，確保儲存時優先使用 contents
          // 因為 index.vue 的 buildMessageObject 會優先讀取 baseUrl 等扁平欄位，若不移除會導致儲存到舊資料
          if (msg.baseUrl) msg.baseUrl = undefined
          if (msg.baseSize) msg.baseSize = undefined
          if (msg.actions) msg.actions = undefined
        } else if (!msg.contents?.trim()) {
          ElMessage.warning(`訊息 ${i + 1}：請設定圖片地圖內容`)
          return
        }
        break
      case 'TEMPLATE':
        // 驗證 TEMPLATE 訊息：檢查 templateData 是否已設定
        if (!msg.templateData) {
          ElMessage.warning(`訊息 ${i + 1}：請設定模板內容`)
          return
        }
        // 確保 contents 已同步（從 templateData.content 取得）
        if (msg.templateData.content) {
          msg.contents = msg.templateData.content
        }
        if (!msg.contents) {
          ElMessage.warning(`訊息 ${i + 1}：模板內容為空`)
          return
        }
        // 驗證模板內容是否完整（避免 LINE API 400 錯誤）
        try {
          const tplContent = typeof msg.contents === 'string' ? JSON.parse(msg.contents) : msg.contents
          const tpl = tplContent.template || tplContent
          const tplType = tpl.type || msg.templateData.templateType
          
          if (tplType === 'buttons') {
            if (!tpl.text?.trim()) {
              ElMessage.warning(`訊息 ${i + 1}：按鈕模板的內文不能為空`)
              return
            }
            if (!tpl.actions || tpl.actions.length === 0) {
              ElMessage.warning(`訊息 ${i + 1}：按鈕模板至少需要一個動作按鈕`)
              return
            }
          } else if (tplType === 'confirm') {
            if (!tpl.text?.trim()) {
              ElMessage.warning(`訊息 ${i + 1}：確認模板的文字不能為空`)
              return
            }
          } else if (tplType === 'carousel') {
            if (!tpl.columns || tpl.columns.length === 0) {
              ElMessage.warning(`訊息 ${i + 1}：輪播模板至少需要一張卡片`)
              return
            }
            for (let j = 0; j < tpl.columns.length; j++) {
              const col = tpl.columns[j]
              if (!col.text?.trim()) {
                ElMessage.warning(`訊息 ${i + 1}：輪播卡片 ${j + 1} 的內文不能為空`)
                return
              }
              if (!col.actions || col.actions.length === 0) {
                ElMessage.warning(`訊息 ${i + 1}：輪播卡片 ${j + 1} 至少需要一個動作按鈕`)
                return
              }
            }
          } else if (tplType === 'image_carousel') {
            if (!tpl.columns || tpl.columns.length === 0) {
              ElMessage.warning(`訊息 ${i + 1}：圖片輪播至少需要一張圖片`)
              return
            }
            for (let j = 0; j < tpl.columns.length; j++) {
              const col = tpl.columns[j]
              if (!col.imageUrl?.trim()) {
                ElMessage.warning(`訊息 ${i + 1}：圖片輪播第 ${j + 1} 張圖片網址不能為空`)
                return
              }
            }
          }
        } catch (e) {
          ElMessage.warning(`訊息 ${i + 1}：模板格式錯誤`)
          return
        }
        break
    }
  }

  saving.value = true
  try {
    // 收集所有 IMAGEMAP 訊息的引用資訊（包含訊息索引）
    const imagemapRefs = messages.value
      .map((msg, index) => ({ msg, index }))
      .filter(({ msg }) => msg.type === 'IMAGEMAP' && msg.imagemapSourceId)
      .map(({ msg, index }) => ({
        imagemapId: msg.imagemapSourceId,
        messageIndex: index
      }))
    
    emit('save', {
      ...form,
      messages: messages.value,
      imagemapRefs // 傳遞引用的圖文範本資訊（包含訊息索引）
    })
  } finally {
    saving.value = false
  }
}

// 初始化
const initForm = () => {
  if (props.template) {
    Object.assign(form, {
      templateId: props.template.templateId,
      templateName: props.template.templateName,
      templateCode: props.template.templateCode,
      status: props.template.status,
      sortOrder: props.template.sortOrder,
      remark: props.template.remark
    })

    // 解析訊息內容
    try {
      const content = JSON.parse(props.template.content)
      if (content.messages && Array.isArray(content.messages)) {
        // 新格式：處理 FLEX/IMAGEMAP 的 contents 欄位
        // 注意：儲存時 type 轉為小寫，載入時需轉回大寫
        messages.value = content.messages.map(msg => {
          const normalizedType = msg.type.toUpperCase()
          const normalizedMsg = { ...msg, type: normalizedType, _dragKey: generateDragKey() }
          
          // IMAGEMAP 處理：支援扁平結構和 contents 結構
          if (normalizedType === 'IMAGEMAP') {
            let imagemapData = null
            let imagemapSourceId = msg.imagemapSourceId || null
            
            // 優先檢查扁平結構（baseUrl 直接在 msg 上）
            if (msg.baseUrl) {
              imagemapData = {
                baseUrl: msg.baseUrl,
                altText: msg.altText,
                baseSize: msg.baseSize,
                actions: msg.actions || []
              }
            } else if (msg.contents) {
              // 從 contents 解析
              try {
                imagemapData = typeof msg.contents === 'string' 
                  ? JSON.parse(msg.contents) 
                  : msg.contents
              } catch (e) {
                console.error('[initForm] IMAGEMAP parse error:', e)
              }
            }
            
            if (imagemapData) {
              const contentsStr = JSON.stringify(imagemapData, null, 2)
              return { ...normalizedMsg, contents: contentsStr, imagemapData, imagemapSourceId }
            }
            return { ...normalizedMsg, imagemapSourceId }
          }
          
          // FLEX 處理：保留 flexPresetName
          if (normalizedType === 'FLEX' && msg.contents) {
            const contentsStr = typeof msg.contents === 'string'
              ? msg.contents
              : JSON.stringify(msg.contents, null, 2)
            return { 
              ...normalizedMsg, 
              contents: contentsStr,
              flexPresetName: msg.flexPresetName || ''
            }
          }
          
          // TEXT 處理：恢復 Quick Reply 設定
          if (normalizedType === 'TEXT' && msg.quickReply?.items?.length > 0) {
            return {
              ...normalizedMsg,
              enableQuickReply: true,
              quickReply: msg.quickReply
            }
          }
          
          // TEMPLATE 處理：恢復 templateData
          if (normalizedType === 'TEMPLATE') {
            let templateData = null
            let contentsStr = ''
            try {
              // 情況 1：msg.template 直接存在（從 buildMessageObject 儲存的格式）
              if (msg.template) {
                const tpl = msg.template
                // 建構完整的 template message JSON
                const fullTemplateJson = {
                  type: 'template',
                  altText: msg.altText || '模板訊息',
                  template: tpl
                }
                contentsStr = JSON.stringify(fullTemplateJson)
                templateData = {
                  content: contentsStr,
                  altText: msg.altText || '模板訊息',
                  templateType: tpl.type || 'buttons',
                  templateData: tpl
                }
              }
              // 情況 2：msg.contents 存在（舊格式或直接設定）
              else if (msg.contents) {
                const parsed = typeof msg.contents === 'string' 
                  ? JSON.parse(msg.contents) 
                  : msg.contents
                contentsStr = typeof msg.contents === 'string' 
                  ? msg.contents 
                  : JSON.stringify(msg.contents)
                templateData = {
                  content: contentsStr,
                  altText: parsed.altText || msg.altText || '模板訊息',
                  templateType: parsed.template?.type || 'buttons',
                  templateData: parsed.template || parsed
                }
              }
              // 情況 3：msg.templateData 存在（編輯中的暫存格式）
              else if (msg.templateData) {
                templateData = msg.templateData
                contentsStr = msg.templateData.content || ''
              }
            } catch (e) {
              console.error('[initForm] TEMPLATE parse error:', e)
            }
            return { 
              ...normalizedMsg, 
              templateData,
              contents: contentsStr,
              altText: templateData?.altText || msg.altText || '模板訊息',
              text: undefined  // 清除 text 欄位，避免顯示在預覽中
            }
          }
          
          return normalizedMsg
        })
      } else {
        // 舊格式：單一訊息
        const parsedMsg = parseOldFormat(props.template.msgType, props.template.content, props.template.altText)
        parsedMsg._dragKey = generateDragKey()
        messages.value = [parsedMsg]
      }
    } catch (e) {
      // 純文字或 JSON 解析失敗，嘗試用 parseOldFormat 解析
      const parsedMsg = parseOldFormat(props.template.msgType, props.template.content, props.template.altText)
      parsedMsg._dragKey = generateDragKey()
      messages.value = [parsedMsg]
    }
  } else {
    // 新增範本時，不預設任何訊息，讓使用者自行選擇
    Object.assign(form, {
      templateId: null,
      templateName: '',
      templateCode: '',
      status: 1,
      sortOrder: 0,
      remark: ''
    })
    messages.value = []
  }
  activeMessageIndex.value = 0
  
  // 初始化當前訊息的相關狀態
  nextTick(async () => {
    // FLEX 類型：初始化 flexTemplateRaw 和解析變數，並恢復下拉選單選擇
    if (currentMessage.value?.type === 'FLEX' && currentMessage.value.contents) {
      console.log('[initForm] FLEX message detected, initializing flexTemplateRaw')
      flexTemplateRaw.value = currentMessage.value.contents
      parseFlexVariables(currentMessage.value.contents)
      console.log('[initForm] FLEX initialized, fields:', flexEditableFields.value.length)
      
      // 恢復下拉選單選擇的範本名稱（設置標記避免 watch 重複載入）
      if (currentMessage.value.flexPresetName) {
        console.log('[initForm] Restoring flexPresetName:', currentMessage.value.flexPresetName)
        isRestoringFlexPreset.value = true
        selectedFlexPreset.value = currentMessage.value.flexPresetName
      }
    }
    
    // IMAGEMAP 類型：載入範本列表並設置選中值
    if (currentMessage.value?.type === 'IMAGEMAP') {
      await loadImagemapTemplates()
      const sourceId = currentMessage.value.imagemapSourceId
      console.log('[initForm] IMAGEMAP sourceId:', sourceId, 'type:', typeof sourceId)
      console.log('[initForm] imagemapTemplateList:', imagemapTemplateList.value.map(t => ({ id: t.templateId, name: t.templateName })))
      if (sourceId) {
        // 確保類型一致（轉為 Number）
        selectedImagemapTemplateId.value = Number(sourceId)
        console.log('[initForm] set selectedImagemapTemplateId:', selectedImagemapTemplateId.value)
      }
    }
  })
}

const parseOldFormat = (msgType, content, altText) => {
  try {
    const obj = JSON.parse(content)
    switch (msgType) {
      case 'TEXT':
        // 處理 JSON 格式的 TEXT（包含 emojis 和 quickReply）
        if (obj.type === 'text' && obj.text) {
          const result = { type: 'TEXT', text: obj.text, emojis: obj.emojis || [] }
          // 恢復 Quick Reply 設定
          if (obj.quickReply?.items?.length > 0) {
            result.enableQuickReply = true
            result.quickReply = obj.quickReply
          }
          return result
        }
        return { type: 'TEXT', text: content }
      case 'IMAGE':
        return { type: 'IMAGE', originalContentUrl: obj.originalContentUrl, previewImageUrl: obj.previewImageUrl }
      case 'VIDEO':
        return { type: 'VIDEO', originalContentUrl: obj.originalContentUrl, previewImageUrl: obj.previewImageUrl }
      case 'AUDIO':
        return { type: 'AUDIO', originalContentUrl: obj.originalContentUrl, duration: obj.duration }
      case 'LOCATION':
        return { type: 'LOCATION', title: obj.title, address: obj.address, latitude: obj.latitude, longitude: obj.longitude }
      case 'STICKER':
        return { type: 'STICKER', packageId: obj.packageId, stickerId: obj.stickerId }
      case 'FLEX':
        // 格式化 JSON 以便於編輯，並恢復 flexPresetName
        return { 
          type: 'FLEX', 
          altText: altText || '', 
          contents: JSON.stringify(obj, null, 2),
          flexPresetName: obj.flexPresetName || ''
        }
      case 'IMAGEMAP':
        return { type: 'IMAGEMAP', altText: altText || '', contents: JSON.stringify(obj, null, 2), imagemapData: obj }
      case 'TEMPLATE':
        // 解析 Template Message（Buttons/Confirm/Carousel/Image Carousel）
        const contentsStr = JSON.stringify(obj, null, 2)
        const templateData = {
          content: contentsStr,  // TemplateMessageEditor 期望的格式
          altText: obj.altText || altText || '模板訊息',
          templateType: obj.template?.type || 'buttons',
          templateData: obj.template || obj
        }
        return { 
          type: 'TEMPLATE', 
          altText: templateData.altText, 
          templateData,
          contents: contentsStr
        }
      default:
        return { type: 'TEXT', text: content }
    }
  } catch (e) {
    // 如果解析失敗，可能 content 已經是格式化的字串
    if (msgType === 'FLEX' || msgType === 'IMAGEMAP' || msgType === 'TEMPLATE') {
      return { type: msgType, altText: altText || '', contents: content }
    }
    return { type: 'TEXT', text: content }
  }
}

watch(() => props.template, initForm, { immediate: true })

watch(
  () => activeMessageIndex.value,
  async () => {
    if (currentMessage.value?.type === 'FLEX') {
      parseFlexEditableFields()
      // 恢復下拉選單選擇的範本名稱（設置標記避免 watch 重複載入）
      if (currentMessage.value.flexPresetName) {
        isRestoringFlexPreset.value = true
        selectedFlexPreset.value = currentMessage.value.flexPresetName
      } else {
        selectedFlexPreset.value = ''
      }
    } else {
      flexEditableFields.value = []
      flexTemplateRaw.value = ''
      flexVariableValues.value = {}
      selectedFlexPreset.value = ''
    }
    
    // IMAGEMAP 類型：載入範本列表並設置已選擇的範本
    if (currentMessage.value?.type === 'IMAGEMAP') {
      await loadImagemapTemplates()
      
      // 優先使用 imagemapSourceId（記錄的引用 ID）
      const sourceId = currentMessage.value.imagemapSourceId
      if (sourceId) {
        // 確保類型一致（轉為 Number）
        selectedImagemapTemplateId.value = Number(sourceId)
      } else {
        selectedImagemapTemplateId.value = null
      }
    } else {
      selectedImagemapTemplateId.value = null
    }
  },
  { immediate: true }
)

// 監聽範本選擇變化（僅用於恢復選擇，一般選擇由 @change 事件處理）
watch(
  () => selectedFlexPreset.value,
  async (newVal) => {
    console.log('[Flex Watch] selectedFlexPreset changed to:', newVal, 'isRestoring:', isRestoringFlexPreset.value)
    if (!newVal || !currentMessage.value) return
    
    // 僅處理恢復選擇的情況（切換訊息時恢復之前選擇的範本）
    if (isRestoringFlexPreset.value) {
      isRestoringFlexPreset.value = false
      // 保存當前的 contents（已替換變數的值）
      const savedContents = currentMessage.value.contents
      // 載入範本以取得可編輯欄位結構
      await loadFlexTemplate(newVal)
      // 恢復 contents（不使用範本的預設值）
      if (savedContents) {
        currentMessage.value.contents = savedContents
        // 嘗試從已儲存的內容中提取變數值
        extractVariableValuesFromContent(savedContents)
      }
    }
    // 一般選擇由 handleFlexPresetChange 處理，不在此重複載入
  }
)

/**
 * 從已儲存的 JSON 內容中提取變數值
 * 透過比對範本原始內容和已儲存內容來推測變數值
 */
const extractVariableValuesFromContent = (savedContent) => {
  if (!savedContent || !flexTemplateRaw.value || flexEditableFields.value.length === 0) return
  
  try {
    const savedObj = JSON.parse(savedContent)
    const templateObj = JSON.parse(flexTemplateRaw.value)
    
    // 遍歷可編輯欄位，嘗試從已儲存內容中提取對應值
    flexEditableFields.value.forEach(field => {
      const varName = field.name
      const pattern = new RegExp(`\\{\\{${varName}(?::[^}]*)?\\}\\}`)
      
      // 在範本中找到該變數的位置，然後在已儲存內容中找對應值
      const value = findValueInContent(templateObj, savedObj, pattern, varName)
      if (value !== null) {
        flexVariableValues.value[varName] = value
      }
    })
    
    console.log('[extractVariableValuesFromContent] Extracted values:', flexVariableValues.value)
  } catch (e) {
    console.warn('[extractVariableValuesFromContent] Failed to extract values:', e)
  }
}

/**
 * 在 JSON 物件中遞迴尋找變數對應的值
 */
const findValueInContent = (templateNode, savedNode, pattern, varName) => {
  if (templateNode === null || savedNode === null) return null
  
  if (typeof templateNode === 'string') {
    if (pattern.test(templateNode)) {
      return typeof savedNode === 'string' ? savedNode : null
    }
    return null
  }
  
  if (Array.isArray(templateNode) && Array.isArray(savedNode)) {
    for (let i = 0; i < Math.min(templateNode.length, savedNode.length); i++) {
      const result = findValueInContent(templateNode[i], savedNode[i], pattern, varName)
      if (result !== null) return result
    }
    return null
  }
  
  if (typeof templateNode === 'object' && typeof savedNode === 'object') {
    for (const key of Object.keys(templateNode)) {
      if (savedNode.hasOwnProperty(key)) {
        const result = findValueInContent(templateNode[key], savedNode[key], pattern, varName)
        if (result !== null) return result
      }
    }
  }
  
  return null
}
</script>

<style scoped lang="scss">
.template-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;

  .header-title {
    font-size: 16px;
    font-weight: 500;
  }
}

.editor-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.section {
  margin-bottom: 24px;

  .section-title {
    font-weight: 500;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }
}

.message-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  align-items: flex-start;

  .message-list-inner {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  .message-item {
    width: 130px;
    padding: 12px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    background: #fff;

    &:hover {
      border-color: #409eff;
    }

    &.active {
      border-color: #409eff;
      background: #ecf5ff;
    }

    .msg-header {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-bottom: 8px;

      .drag-handle {
        cursor: grab;
        color: #c0c4cc;
        margin-right: 2px;
        
        &:hover {
          color: #409eff;
        }
        
        &:active {
          cursor: grabbing;
        }
      }

      .msg-type {
        flex: 1;
        font-size: 12px;
        font-weight: 500;
      }
    }

    .msg-preview {
      font-size: 11px;
      color: #909399;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  // 拖曳時的幽靈效果
  .ghost-item {
    opacity: 0.5;
    background: #e6f1fc;
    border: 2px dashed #409eff;
  }

  .add-message-btn {
    width: 130px;
    height: 80px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border: 1px dashed #dcdfe6;
    border-radius: 8px;
    color: #909399;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #409eff;
      color: #409eff;
    }
  }
}

.message-editor {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.media-preview {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;

  img, video {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }
  
  .sticker-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #F56C6C;
    font-size: 14px;
    
    .error-hint {
      font-size: 12px;
      color: #909399;
    }
  }
}

.audio-preview {
  margin-top: 12px;
  text-align: center;
}

.sticker-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
}

// 即時預覽對話框
.text-live-preview {
  margin-bottom: 16px;
  
  .preview-label {
    font-size: 12px;
    color: #909399;
    margin-bottom: 8px;
  }
  
  .preview-bubble {
    background: #06c755;
    color: white;
    padding: 12px 16px;
    border-radius: 18px;
    border-bottom-left-radius: 4px;
    font-size: 14px;
    line-height: 1.6;
    display: inline-block;
    max-width: 100%;
    word-break: break-word;
    white-space: pre-wrap;
    
    .preview-emoji {
      width: 20px;
      height: 20px;
      vertical-align: middle;
      margin: 0 1px;
    }
    
    .empty-text {
      opacity: 0.6;
      font-style: italic;
    }
  }
  
  .quick-reply-preview {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed #e4e7ed;
    
    .quick-reply-btn-preview {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      background: #fff;
      border: 1px solid #06c755;
      border-radius: 20px;
      font-size: 13px;
      color: #06c755;
      white-space: nowrap;
      
      .quick-reply-icon {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        object-fit: cover;
      }
    }
  }
}

.emoji-list-preview {
  margin: 12px 0;
  padding: 12px;
  background: #f0f9eb;
  border-radius: 8px;
  border: 1px solid #e1f3d8;

  .emoji-list-label {
    font-size: 13px;
    font-weight: 500;
    color: #67c23a;
    margin-bottom: 8px;
  }

  .emoji-list-items {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;
  }

  .emoji-list-item {
    display: flex;
    align-items: center;
    gap: 6px;
    background: #fff;
    padding: 4px 8px;
    border-radius: 6px;
    border: 1px solid #e4e7ed;

    .emoji-index {
      width: 20px;
      height: 20px;
      background: #67c23a;
      color: #fff;
      border-radius: 50%;
      font-size: 11px;
      font-weight: bold;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .emoji-thumbnail {
      width: 24px;
      height: 24px;
      object-fit: contain;
    }
  }
}

.text-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  
  .action-tip {
    font-size: 12px;
    color: #909399;
  }
}

.flex-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.code-textarea :deep(textarea) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
}

.flex-preview-panel {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  position: sticky;
  top: 20px;
  align-self: flex-start;

  .preview-title {
    font-weight: 500;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }
}

.mb-3 {
  margin-bottom: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.preset-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 4px 0;

  .preset-label {
    font-weight: 500;
    color: #303133;
  }

  .preset-desc {
    font-size: 12px;
    color: #909399;
    max-width: 280px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.preset-option {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .preset-label {
    font-weight: 500;
  }

  .preset-desc {
    font-size: 12px;
    color: #909399;
  }
}

.editable-fields-section {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;

  .section-subtitle {
    font-weight: 500;
    font-size: 14px;
    color: #303133;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }
}

.line-user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.flex-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.form-tip code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #e6a23c;
}

.color-input-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
}

.import-error {
  margin-top: 12px;

  :deep(.el-alert__description) {
    white-space: pre-wrap;
    font-family: monospace;
  }
}

// Quick Reply 樣式
.quick-reply-section {
  margin-top: 12px;
  
  .quick-reply-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
    
    .section-label {
      font-weight: 500;
      font-size: 14px;
      color: #303133;
    }
  }
  
  .quick-reply-layout {
    margin-top: 12px;
    min-height: 200px;
  }
  
  .quick-reply-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 12px;
    max-height: 400px;
    overflow-y: auto;
  }
  
  .quick-reply-list-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    background: #f5f7fa;
    border: 2px solid transparent;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #ecf5ff;
      border-color: #b3d8ff;
    }
    
    &.active {
      background: #ecf5ff;
      border-color: #409eff;
    }
    
    .item-info {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
      min-width: 0;
      
      .item-index {
        width: 24px;
        height: 24px;
        background: #409eff;
        color: #fff;
        border-radius: 50%;
        font-size: 12px;
        font-weight: bold;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }
      
      .item-label {
        font-size: 13px;
        color: #606266;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
  
  .quick-reply-editor {
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 16px;
    
    .editor-title {
      font-weight: 500;
      font-size: 14px;
      color: #303133;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #ebeef5;
    }
  }
}

.flex-template-selectors {
  display: flex;
  gap: 8px;
  width: 100%;
  
  .flex-template-select {
    flex: 1;
    min-width: 0;
  }
}

.preset-option-with-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  
  .preset-info {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-width: 0;
    
    .preset-label {
      font-size: 13px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .preset-desc {
      font-size: 11px;
      color: #909399;
      margin-top: 2px;
    }
  }
  
  .el-button {
    margin-left: 8px;
    opacity: 0;
    display: inline-flex;
    align-items: center;
    height: 22px;
    padding: 0 6px;
    transition: opacity 0.2s;
  }
}

:deep(.el-select-dropdown__item:hover) {
  .preset-option-with-action .el-button {
    opacity: 1;
  }
}

:global(.flex-template-dropdown .el-select-dropdown__item) {
  height: auto;
  line-height: 18px;
  padding-top: 6px;
  padding-bottom: 6px;
  overflow: visible;
}

:global(.flex-template-dropdown .preset-option-with-action .el-button) {
  opacity: 0;
}

:global(.flex-template-dropdown .el-select-dropdown__item:hover .preset-option-with-action .el-button) {
  opacity: 1 !important;
}
</style>
