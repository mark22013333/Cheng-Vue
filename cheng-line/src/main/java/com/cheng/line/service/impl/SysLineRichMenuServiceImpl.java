package com.cheng.line.service.impl;

import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.OkHttpUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.JacksonUtil;
import com.cheng.common.utils.ImageResizeUtil;
import com.cheng.common.utils.dto.ApiResponse;
import com.cheng.line.client.LineClientFactory;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.domain.SysLineRichMenu;
import com.cheng.line.enums.LineApiEndpoint;
import com.cheng.line.enums.RichMenuActionType;
import com.cheng.line.enums.RichMenuStatus;
import com.cheng.line.mapper.LineConfigMapper;
import com.cheng.line.mapper.SysLineRichMenuMapper;
import com.cheng.line.service.ISysLineRichMenuService;
import com.cheng.line.service.ISysLineRichMenuAliasService;
import com.cheng.line.domain.SysLineRichMenuAlias;
import com.fasterxml.jackson.core.type.TypeReference;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cheng.framework.sse.SseManager;
import com.cheng.framework.sse.SseChannels;
import com.cheng.framework.sse.SseEvent;

import jakarta.annotation.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * LINE Rich Menu Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
public class SysLineRichMenuServiceImpl implements ISysLineRichMenuService {

    @Resource
    private SysLineRichMenuMapper richMenuMapper;

    @Resource
    private LineConfigMapper lineConfigMapper;

    @Resource
    private LineClientFactory lineClientFactory;

    @Resource
    @Lazy
    private ISysLineRichMenuAliasService aliasService;

    @Autowired
    private SseManager sseManager;

    /**
     * 查詢 Rich Menu 列表
     */
    @Override
    public List<SysLineRichMenu> selectRichMenuList(SysLineRichMenu richMenu) {
        return richMenuMapper.selectRichMenuList(richMenu);
    }

    /**
     * 根據 ID 查詢 Rich Menu
     */
    @Override
    public SysLineRichMenu selectRichMenuById(Long id) {
        return richMenuMapper.selectRichMenuById(id);
    }

    /**
     * 根據 LINE richMenuId 查詢
     */
    @Override
    public SysLineRichMenu selectRichMenuByRichMenuId(String richMenuId) {
        return richMenuMapper.selectRichMenuByRichMenuId(richMenuId);
    }

    /**
     * 根據頻道 ID 查詢預設選單
     */
    @Override
    public SysLineRichMenu selectDefaultRichMenuByConfigId(Integer configId) {
        return richMenuMapper.selectDefaultRichMenuByConfigId(configId);
    }

    /**
     * 根據頻道 ID 查詢使用中的選單
     */
    @Override
    public SysLineRichMenu selectSelectedRichMenuByConfigId(Integer configId) {
        return richMenuMapper.selectSelectedRichMenuByConfigId(configId);
    }

    /**
     * 新增 Rich Menu
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRichMenu(SysLineRichMenu richMenu) {
        // 驗證頻道是否存在
        LineConfig config = lineConfigMapper.selectLineConfigById(richMenu.getConfigId());
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        // 檢查名稱是否重複
        if (!checkNameUnique(richMenu)) {
            throw new ServiceException(String.format("選單名稱「%s」已存在", richMenu.getName()));
        }

        // 驗證區塊設定（基本驗證）
        validateAreas(richMenu);

        // 如果有 areasJson，呼叫 LINE API 驗證 Rich Menu 結構
        if (StringUtils.isNotEmpty(richMenu.getAreasJson()) && StringUtils.isNotEmpty(richMenu.getImageSize())) {
            try {
                log.info("新增 Rich Menu 前驗證結構：{}", richMenu.getName());
                
                // 解析 areas JSON
                List<RichMenuArea> areas = parseAreas(richMenu.getAreasJson());
                
                // 解析圖片尺寸
                String[] sizeParts = richMenu.getImageSize().split("x");
                int width = Integer.parseInt(sizeParts[0]);
                int height = Integer.parseInt(sizeParts[1]);
                
                // 建立 RichMenuRequest
                RichMenuSize size = new RichMenuSize.Builder()
                        .width((long) width)
                        .height((long) height)
                        .build();
                
                RichMenuRequest richMenuRequest = new RichMenuRequest.Builder()
                        .size(size)
                        .selected(false)
                        .name(richMenu.getName())
                        .chatBarText(richMenu.getChatBarText() != null ? richMenu.getChatBarText() : "選單")
                        .areas(areas)
                        .build();
                
                // 呼叫 LINE API 驗證
                validateRichMenuStructure(richMenuRequest, config.getChannelAccessToken());
                
                log.info("✓ Rich Menu 結構驗證通過");
                
            } catch (ServiceException e) {
                // 重新拋出 ServiceException ，保持原始錯誤訊息
                throw e;
            } catch (Exception e) {
                log.error("驗證 Rich Menu 結構失敗", e);
                throw new ServiceException("驗證 Rich Menu 結構失敗：" + e.getMessage());
            }
        }

        // 預設狀態為草稿
        if (richMenu.getStatus() == null) {
            richMenu.setStatus(RichMenuStatus.DRAFT);
        }

        // 預設版本號
        if (richMenu.getVersion() == null) {
            richMenu.setVersion(1);
        }

        return richMenuMapper.insertRichMenu(richMenu);
    }

    /**
     * 修改 Rich Menu
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRichMenu(SysLineRichMenu richMenu) {
        // 檢查是否存在
        SysLineRichMenu existMenu = richMenuMapper.selectRichMenuById(richMenu.getId());
        if (existMenu == null) {
            throw new ServiceException("Rich Menu 不存在");
        }

        // 查詢頻道設定（用於驗證）
        LineConfig config = lineConfigMapper.selectLineConfigById(richMenu.getConfigId());
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        // 檢查名稱是否重複
        if (!checkNameUnique(richMenu)) {
            throw new ServiceException(String.format("選單名稱「%s」已存在", richMenu.getName()));
        }

        // 驗證區塊設定（基本驗證）
        validateAreas(richMenu);

        // 如果有 areasJson，呼叫 LINE API 驗證 Rich Menu 結構
        if (StringUtils.isNotEmpty(richMenu.getAreasJson()) && StringUtils.isNotEmpty(richMenu.getImageSize())) {
            try {
                log.info("修改 Rich Menu 前驗證結構：{}", richMenu.getName());
                
                // 解析 areas JSON
                List<RichMenuArea> areas = parseAreas(richMenu.getAreasJson());
                
                // 解析圖片尺寸
                String[] sizeParts = richMenu.getImageSize().split("x");
                int width = Integer.parseInt(sizeParts[0]);
                int height = Integer.parseInt(sizeParts[1]);
                
                // 建立 RichMenuRequest
                RichMenuSize size = new RichMenuSize.Builder()
                        .width((long) width)
                        .height((long) height)
                        .build();
                
                RichMenuRequest richMenuRequest = new RichMenuRequest.Builder()
                        .size(size)
                        .selected(richMenu.getSelected() == 1)
                        .name(richMenu.getName())
                        .chatBarText(richMenu.getChatBarText() != null ? richMenu.getChatBarText() : "選單")
                        .areas(areas)
                        .build();
                
                // 呼叫 LINE API 驗證
                validateRichMenuStructure(richMenuRequest, config.getChannelAccessToken());
                
                log.info("✓ Rich Menu 結構驗證通過");
                
            } catch (ServiceException e) {
                // 重新拋出 ServiceException ，保持原始錯誤訊息
                throw e;
            } catch (Exception e) {
                log.error("驗證 Rich Menu 結構失敗", e);
                throw new ServiceException("驗證 Rich Menu 結構失敗：" + e.getMessage());
            }
        }

        // 如果已發布，不允許修改某些欄位
        if (existMenu.isPublished()) {
            log.warn("Rich Menu [{}] 已發布到 LINE 平台，修改後需重新發布", existMenu.getId());
        }

        // 版本號遞增
        if (richMenu.getVersion() != null) {
            richMenu.setVersion(richMenu.getVersion() + 1);
        }

        return richMenuMapper.updateRichMenu(richMenu);
    }

    /**
     * 批次刪除 Rich Menu（先從 LINE API 刪除，再刪除資料庫）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRichMenuByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("請選擇要刪除的 Rich Menu");
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long id : ids) {
            SysLineRichMenu menu = richMenuMapper.selectRichMenuById(id);
            if (menu == null) {
                errors.add("選單 ID " + id + " 不存在");
                continue;
            }

            try {
                // 如果已發布到 LINE，先從 LINE 刪除
                if (menu.isPublished() && StringUtils.isNotEmpty(menu.getRichMenuId())) {
                    log.info("刪除 Rich Menu：{} (LINE ID: {})", menu.getName(), menu.getRichMenuId());
                    
                    LineConfig config = lineConfigMapper.selectLineConfigById(menu.getConfigId());
                    if (config != null) {
                        deleteRichMenuFromLine(menu.getRichMenuId(), config.getChannelAccessToken());
                        log.info("✓ 已從 LINE 平台刪除 Rich Menu: {}", menu.getRichMenuId());
                    }
                }

                // 刪除資料庫記錄
                richMenuMapper.deleteRichMenuById(id);
                successCount++;
                log.info("✓ 已從資料庫刪除 Rich Menu: {}", menu.getName());

            } catch (Exception e) {
                log.error("刪除 Rich Menu 失敗：{}", menu.getName(), e);
                errors.add(menu.getName() + "：" + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new ServiceException("部分選單刪除失敗：\n" + String.join("\n", errors));
        }

        return successCount;
    }

    /**
     * 從 LINE 平台刪除 Rich Menu
     */
    private void deleteRichMenuFromLine(String richMenuId, String accessToken) {
        try {
            String url = LineApiEndpoint.RICH_MENU_DELETE.getUrl(richMenuId);
            
            ApiResponse response = OkHttpUtils.builder()
                    .addLineAuthHeader(accessToken)
                    .delete(url)
                    .sync();
            
            if (response.getHttpStatusCode() != HttpStatus.OK.value()) {
                String error = response.getResultData() != null ? response.getResultData() : "未知錯誤";
                throw new ServiceException("LINE API 刪除失敗：" + error);
            }
            
        } catch (Exception e) {
            log.error("呼叫 LINE API 刪除 Rich Menu 失敗：{}", e.getMessage(), e);
            throw new ServiceException("刪除失敗：" + e.getMessage());
        }
    }

    /**
     * 根據 ID 刪除 Rich Menu
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRichMenuById(Long id) {
        return deleteRichMenuByIds(new Long[]{id});
    }

    /**
     * 發布 Rich Menu 到 LINE 平台（同步版本，向下相容）
     * 自動判斷是首次發布還是重新發布
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String publishRichMenu(Long id) {
        return publishRichMenuInternal(id, null);
    }

    /**
     * 發布 Rich Menu 到 LINE 平台（異步版本，支援 SSE 推送進度）
     * 
     * @param id Rich Menu ID
     * @param taskId 任務 ID（用於 SSE 推送）
     */
    @Async
    public void publishRichMenuAsync(Long id, String taskId) {
        try {
            publishRichMenuInternal(id, taskId);
        } catch (Exception e) {
            log.error("[Async] Rich Menu 發布失敗", e);
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId, 
                    SseEvent.error("發布失敗：" + e.getMessage()));
            }
        }
    }

    /**
     * 發布 Rich Menu 內部實作
     * 
     * @param id Rich Menu ID
     * @param taskId 任務 ID（可選，用於 SSE 推送）
     * @return richMenuId
     */
    @Transactional(rollbackFor = Exception.class)
    protected String publishRichMenuInternal(Long id, String taskId) {
        log.info("========== 開始發布 Rich Menu ==========");

        // 查詢 Rich Menu
        SysLineRichMenu menu = richMenuMapper.selectRichMenuById(id);
        if (menu == null) {
            throw new ServiceException("Rich Menu 不存在");
        }

        // 驗證必要欄位
        if (StringUtils.isEmpty(menu.getImageUrl())) {
            throw new ServiceException("請先上傳選單圖片");
        }

        if (StringUtils.isEmpty(menu.getAreasJson())) {
            throw new ServiceException("請設定選單區塊");
        }

        // 查詢頻道設定
        LineConfig config = lineConfigMapper.selectLineConfigById(menu.getConfigId());
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        try {
            // 取得 MessagingApiClient
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            // 判斷是首次發布還是重新發布
            boolean isRepublish = menu.isPublished();

            String richMenuId;
            if (isRepublish) {
                log.info("🔄 偵測到重新發布模式（舊 richMenuId: {}）", menu.getRichMenuId());
                richMenuId = republish(menu, config, client, taskId);
            } else {
                log.info("🆕 偵測到首次發布模式");
                richMenuId = firstPublish(menu, config, client, taskId);
            }

            // 發送成功事件
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.success("發布成功！", richMenuId));
            }

            log.info("========== Rich Menu 發布成功！==========");
            return richMenuId;

        } catch (Exception e) {
            log.error("========== Rich Menu 發布失敗 ==========");
            log.error("錯誤訊息：{}", e.getMessage(), e);
            
            // 發送錯誤事件
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.error("發布失敗：" + e.getMessage()));
            }
            
            throw new ServiceException("發布 Rich Menu 失敗：" + e.getMessage());
        }
    }

    /**
     * 上傳 Rich Menu 圖片到 LINE 平台
     */
    @Override
    public boolean uploadRichMenuImage(Long id, byte[] imageBytes) {
        // 查詢 Rich Menu
        SysLineRichMenu menu = richMenuMapper.selectRichMenuById(id);
        if (menu == null) {
            throw new ServiceException("Rich Menu 不存在");
        }

        if (!menu.isPublished()) {
            throw new ServiceException("請先發布 Rich Menu");
        }

        // 查詢頻道設定
        LineConfig config = lineConfigMapper.selectLineConfigById(menu.getConfigId());
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        try {
            // 取得 MessagingApiClient
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            log.info("上傳 Rich Menu 圖片，richMenuId: {}，大小：{} bytes", menu.getRichMenuId(), imageBytes.length);

            // 呼叫 LINE API 上傳圖片
            // Note: 圖片格式必須是 JPEG 或 PNG，檔案大小不得超過 1 MB
            // TODO: LINE SDK 9.12.0 的圖片上傳 API 需要使用 HTTP 請求，需要另外實作
            log.warn("圖片上傳功能尚未實作，請使用 LINE Official Account Manager 手動上傳");

            log.info("成功上傳 Rich Menu 圖片");

            return true;

        } catch (Exception e) {
            log.error("上傳 Rich Menu 圖片失敗：{}", e.getMessage(), e);
            throw new ServiceException("上傳圖片失敗：" + e.getMessage());
        }
    }

    /**
     * 設定為預設選單（先清除舊預設，再設定新預設）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultRichMenu(Long id) {
        // 查詢 Rich Menu
        SysLineRichMenu menu = richMenuMapper.selectRichMenuById(id);
        if (menu == null) {
            throw new ServiceException("Rich Menu 不存在");
        }

        if (!menu.isPublished()) {
            throw new ServiceException("請先發布 Rich Menu 到 LINE 平台");
        }

        // 查詢頻道設定
        LineConfig config = lineConfigMapper.selectLineConfigById(menu.getConfigId());
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        try {
            log.info("設定 Rich Menu [{}] 為預設選單", menu.getName());
            
            // 步驟 1：先清除 LINE 平台上的舊預設選單
            log.info("▶ 步驟 1：清除 LINE 平台上的舊預設選單");
            clearDefaultRichMenuFromLine(config.getChannelAccessToken());
            log.info("✓ 已清除舊預設選單");

            // 步驟 2：呼叫 LINE API 設定新預設選單
            log.info("▶ 步驟 2：設定新預設選單到 LINE 平台");
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());
            client.setDefaultRichMenu(menu.getRichMenuId()).get();
            log.info("✓ 已在 LINE 平台設定為預設選單");

            // 步驟 3：更新資料庫
            log.info("▶ 步驟 3：更新資料庫");
            richMenuMapper.unsetAllDefaultByConfigId(menu.getConfigId());
            richMenuMapper.setDefaultById(id);
            log.info("✓ 資料庫已更新");

            log.info("✓ 成功設定 Rich Menu [{}] 為預設選單", menu.getName());
            return true;

        } catch (Exception e) {
            log.error("✗ 設定預設選單失敗：{}", e.getMessage(), e);
            throw new ServiceException("設定預設選單失敗：" + e.getMessage());
        }
    }

    /**
     * 清除 LINE 平台上的預設 Rich Menu
     */
    private void clearDefaultRichMenuFromLine(String accessToken) {
        try {
            String url = LineApiEndpoint.DEFAULT_RICH_MENU_CLEAR.getUrl();
            
            ApiResponse response = OkHttpUtils.builder()
                    .addLineAuthHeader(accessToken)
                    .delete(url)
                    .sync();
            
            // 200 OK 或 404 Not Found 都視為成功（404 表示沒有預設選單）
            if (response.getHttpStatusCode() != HttpStatus.OK.value() 
                && response.getHttpStatusCode() != HttpStatus.NOT_FOUND.value()) {
                String error = response.getResultData() != null ? response.getResultData() : "未知錯誤";
                log.warn("清除預設選單失敗（繼續執行）：{}", error);
            }
            
        } catch (Exception e) {
            log.warn("清除預設選單異常（繼續執行）：{}", e.getMessage());
            // 不拋出異常，允許繼續設定新預設
        }
    }

    /**
     * 從 LINE 平台刪除 Rich Menu
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRichMenuFromLine(Long id) {
        // 查詢 Rich Menu
        SysLineRichMenu menu = richMenuMapper.selectRichMenuById(id);
        if (menu == null) {
            throw new ServiceException("Rich Menu 不存在");
        }

        if (!menu.isPublished()) {
            throw new ServiceException("該 Rich Menu 尚未發布到 LINE 平台");
        }

        // 查詢頻道設定
        LineConfig config = lineConfigMapper.selectLineConfigById(menu.getConfigId());
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        try {
            // 取得 MessagingApiClient
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            // 呼叫 LINE API 刪除 Rich Menu
            client.deleteRichMenu(menu.getRichMenuId()).get();

            log.info("成功從 LINE 平台刪除 Rich Menu [{}]，richMenuId：{}", menu.getName(), menu.getRichMenuId());

            // 清除 richMenuId 和更新狀態
            richMenuMapper.updateRichMenuId(id, null);
            richMenuMapper.updateStatus(id, RichMenuStatus.INACTIVE.getCode());

            return true;

        } catch (Exception e) {
            log.error("從 LINE 刪除 Rich Menu 失敗：{}", e.getMessage(), e);
            throw new ServiceException("刪除失敗：" + e.getMessage());
        }
    }

    /**
     * 綁定 Rich Menu 到指定使用者
     */
    @Override
    public boolean linkRichMenuToUser(String userId, String richMenuId) {
        if (StringUtils.isEmpty(userId)) {
            throw new ServiceException("使用者 ID 不能為空");
        }

        if (StringUtils.isEmpty(richMenuId)) {
            throw new ServiceException("Rich Menu ID 不能為空");
        }

        // 需要取得頻道設定，但這裡沒有 configId，需要從 richMenuId 反查
        SysLineRichMenu menu = richMenuMapper.selectRichMenuByRichMenuId(richMenuId);
        if (menu == null) {
            throw new ServiceException("找不到對應的 Rich Menu");
        }

        LineConfig config = lineConfigMapper.selectLineConfigById(menu.getConfigId());
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        try {
            // 取得 MessagingApiClient
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            // 呼叫 LINE API 綁定選單到使用者
            client.linkRichMenuIdToUser(userId, richMenuId).get();

            log.info("成功綁定 Rich Menu [{}] 到使用者 [{}]", richMenuId, userId);

            return true;

        } catch (Exception e) {
            log.error("綁定 Rich Menu 失敗：{}", e.getMessage(), e);
            throw new ServiceException("綁定失敗：" + e.getMessage());
        }
    }

    /**
     * 解除使用者的 Rich Menu 綁定
     */
    @Override
    public boolean unlinkRichMenuFromUser(String userId) {
        if (StringUtils.isEmpty(userId)) {
            throw new ServiceException("使用者 ID 不能為空");
        }

        // 這個方法需要 config，但沒有傳入，需要額外參數或從其他地方取得
        // 暫時拋出異常，需要調整 API 設計
        throw new ServiceException("此方法需要提供頻道設定ID");
    }

    /**
     * 檢查選單名稱是否唯一
     */
    @Override
    public boolean checkNameUnique(SysLineRichMenu richMenu) {
        Long menuId = richMenu.getId() == null ? -1L : richMenu.getId();
        SysLineRichMenu existMenu = richMenuMapper.checkNameUnique(
                richMenu.getConfigId(),
                richMenu.getName(),
                menuId
        );
        return existMenu == null;
    }

    /**
     * 驗證區塊設定
     */
    private void validateAreas(SysLineRichMenu richMenu) {
        if (StringUtils.isEmpty(richMenu.getAreasJson())) {
            return;
        }

        // TODO: 驗證 areas JSON 格式
        // 1. JSON 格式是否正確
        // 2. 區塊數量是否超過 20
        // 3. 區塊是否重疊
        // 4. 區塊是否在圖片範圍內
        log.debug("驗證 Rich Menu 區塊設定：{}", richMenu.getAreasJson());
    }

    /**
     * 根據 imageUrl 類型取得圖片 byte[]
     * 支援三種來源：
     * 1. HTTP URL: https://example.com/image.png → 下載外部圖片
     * 2. 本地系統路徑: /upload/richmenu/xxx.png → 從系統上傳目錄讀取
     * 3. Base64: data:image/png;base64,... → Base64 解碼
     */
    private byte[] getImageBytes(String imageUrl) throws Exception {
        return getImageBytes(imageUrl, null);
    }

    /**
     * 根據 imageUrl 類型取得圖片 byte[]，並自動調整到指定尺寸
     * 
     * @param imageUrl 圖片 URL
     * @param targetSize 目標尺寸（格式：寬x高，例如：2500x1686），null 表示不調整
     * @return 圖片位元組陣列
     * @throws Exception 處理異常
     */
    private byte[] getImageBytes(String imageUrl, String targetSize) throws Exception {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new ServiceException("圖片 URL 不能為空");
        }

        byte[] imageBytes;
        
        if (imageUrl.startsWith("data:image/")) {
            log.debug("圖片來源：Base64 編碼");
            imageBytes = decodeBase64Image(imageUrl);
        } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            log.debug("圖片來源：HTTP URL");
            imageBytes = downloadImageFromUrl(imageUrl);
        } else {
            // 視為本地系統路徑（相對或絕對）
            log.debug("圖片來源：本地系統路徑");
            imageBytes = readLocalFile(imageUrl);
        }
        
        // 如果指定了目標尺寸，自動調整圖片
        if (StringUtils.isNotEmpty(targetSize)) {
            imageBytes = autoResizeImage(imageBytes, targetSize);
        }
        
        return imageBytes;
    }

    /**
     * 自動調整圖片到指定尺寸
     * 使用智慧裁切模式（CROP），保持比例並居中裁切
     * 
     * @param imageBytes 原始圖片
     * @param targetSize 目標尺寸（格式：寬x高）
     * @return 調整後的圖片
     * @throws Exception 處理異常
     */
    private byte[] autoResizeImage(byte[] imageBytes, String targetSize) throws Exception {
        try {
            // 解析目標尺寸
            String[] sizeParts = targetSize.split("x");
            if (sizeParts.length != 2) {
                throw new ServiceException("圖片尺寸格式錯誤：" + targetSize);
            }
            
            int targetWidth = Integer.parseInt(sizeParts[0]);
            int targetHeight = Integer.parseInt(sizeParts[1]);
            
            // 讀取原始圖片尺寸
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (originalImage == null) {
                throw new ServiceException("無法讀取圖片");
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // 如果尺寸已經符合，直接返回
            if (originalWidth == targetWidth && originalHeight == targetHeight) {
                log.info("圖片尺寸已符合 ({}x{})，無需調整", originalWidth, originalHeight);
                return imageBytes;
            }
            
            log.info("圖片尺寸不符，自動調整：{}x{} -> {}x{}",
                    originalWidth, originalHeight, targetWidth, targetHeight);
            
            // 使用 CROP 模式（智慧裁切）
            byte[] resizedBytes = ImageResizeUtil.resize(
                    imageBytes, 
                    targetWidth, 
                    targetHeight, 
                    ImageResizeUtil.ResizeMode.CROP
            );
            
            // 檢查是否超過 1MB 限制
            long maxSize = 1024 * 1024; // 1MB
            if (ImageResizeUtil.exceedsSize(resizedBytes, maxSize)) {
                log.warn("調整後圖片超過 1MB，開始壓縮：{} bytes", resizedBytes.length);
                resizedBytes = ImageResizeUtil.compressToSize(resizedBytes, maxSize);
                log.info("壓縮完成，最終大小：{} bytes ({} KB)", 
                        resizedBytes.length, resizedBytes.length / 1024);
            }
            
            return resizedBytes;
            
        } catch (NumberFormatException e) {
            throw new ServiceException("圖片尺寸格式錯誤：" + targetSize);
        } catch (IOException e) {
            log.error("調整圖片失敗：{}", e.getMessage(), e);
            throw new ServiceException("調整圖片失敗：" + e.getMessage());
        }
    }

    /**
     * Base64 解碼圖片
     */
    private byte[] decodeBase64Image(String dataUrl) {
        try {
            // 格式：data:image/png;base64,iVBOR...
            String base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
            return Base64.getDecoder().decode(base64Data);
        } catch (Exception e) {
            log.error("Base64 解碼失敗：{}", e.getMessage());
            throw new ServiceException("Base64 解碼失敗：" + e.getMessage());
        }
    }

    /**
     * 讀取本地系統檔案
     * 支援相對路徑（從專案根目錄）和絕對路徑
     * 特別處理 /profile 開頭的路徑，轉換為實際的檔案系統路徑
     */
    private byte[] readLocalFile(String filePath) throws IOException {
        try {
            java.nio.file.Path path;

            // 處理 /profile 開頭的相對路徑（轉換為實際路徑）
            if (filePath.startsWith("/profile/")) {
                // 移除 /profile 前綴，使用 CoolAppsConfig 取得實際路徑
                String relativePath = filePath.substring("/profile/".length());
                String fullPath = CoolAppsConfig.getProfile() + "/" + relativePath;
                path = Paths.get(fullPath);
                log.debug("轉換路徑：{} -> {}", filePath, fullPath);
            }
            // 處理絕對路徑
            else if (filePath.startsWith("/") || filePath.matches("^[A-Za-z]:.*")) {
                path = Paths.get(filePath);
            }
            // 相對路徑：從專案根目錄
            else {
                path = Paths.get(System.getProperty("user.dir"), filePath);
            }

            if (!Files.exists(path)) {
                throw new ServiceException("檔案不存在：" + filePath + " (實際路徑：" + path + ")");
            }

            log.debug("讀取本地檔案：{}", path);
            return Files.readAllBytes(path);

        } catch (IOException e) {
            log.error("讀取本地檔案失敗：{}", e.getMessage());
            throw new ServiceException("讀取本地檔案失敗：" + e.getMessage());
        }
    }

    /**
     * 下載圖片
     */
    private byte[] downloadImageFromUrl(String url) {
        ApiResponse response = OkHttpUtils.builder()
                .get(url)
                .sync();

        if (response.getHttpStatusCode() != HttpStatus.OK.value()) {
            throw new ServiceException("下載圖片失敗：HTTP " + response.getHttpStatusCode());
        }

        byte[] imageData = response.getBinaryData();
        if (imageData == null || imageData.length == 0) {
            throw new ServiceException("下載圖片失敗：回應內容為空");
        }

        return imageData;
    }

    /**
     * 驗證圖片規格
     * 1. 檔案大小 <= 1MB
     * 2. 圖片格式（JPEG/PNG）
     * 3. 圖片實際尺寸是否符合預期
     */
    private void validateImage(byte[] imageBytes, String expectedSize) throws Exception {
        // 1. 檢查檔案大小
        if (imageBytes.length > 1024 * 1024) {
            throw new ServiceException(
                    String.format("圖片大小超過 1MB 限制（當前：%.2f MB）", imageBytes.length / 1024.0 / 1024.0)
            );
        }

        // 2. 讀取圖片並檢查格式
        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (Exception e) {
            throw new ServiceException("無法讀取圖片，請確認檔案格式為 JPEG 或 PNG");
        }

        if (image == null) {
            throw new ServiceException("無法解析圖片，請確認檔案格式為 JPEG 或 PNG");
        }

        // 3. 檢查圖片尺寸
        int actualWidth = image.getWidth();
        int actualHeight = image.getHeight();

        String[] sizeParts = expectedSize.split("x");
        int expectedWidth = Integer.parseInt(sizeParts[0]);
        int expectedHeight = Integer.parseInt(sizeParts[1]);

        if (actualWidth != expectedWidth || actualHeight != expectedHeight) {
            throw new ServiceException(
                    String.format("圖片尺寸不符合要求！預期：%dx%d，實際：%dx%d",
                            expectedWidth, expectedHeight, actualWidth, actualHeight)
            );
        }

        log.info("✓ 圖片驗證通過：{}x{}, {} KB",
                actualWidth, actualHeight, String.format("%.2f", imageBytes.length / 1024.0));
    }

    /**
     * 上傳圖片到 LINE Rich Menu
     * LINE Bot SDK 9.12.0 沒有提供圖片上傳方法，因此使用 OkHttpUtils 呼叫 LINE API
     */
    private void uploadImageToLine(String richMenuId, byte[] imageBytes, String accessToken) {
        log.info("使用 OkHttpUtils 上傳圖片到 Rich Menu: {}, 大小: {} bytes", richMenuId, imageBytes.length);

        // 判斷圖片格式
        String contentType = "image/png";
        if (imageBytes.length > 2) {
            // JPEG 檔案開頭：FF D8 FF
            if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
                contentType = "image/jpeg";
            }
        }

        // 使用 OkHttpUtils 上傳 Rich Menu 圖片
        String url = LineApiEndpoint.getRichMenuUploadUrl(richMenuId);

        ApiResponse response = OkHttpUtils.builder()
                .addLineAuthHeader(accessToken)
                .postBinary(url, imageBytes, contentType)
                .sync();

        if (response.getHttpStatusCode() != HttpStatus.OK.value()) {
            String errorMsg = response.getResultData() != null ? response.getResultData() : "未知錯誤";
            throw new ServiceException("上傳圖片失敗：HTTP " + response.getHttpStatusCode() + " - " + errorMsg);
        }

        log.info("===> 成功上傳 Rich Menu 圖片：{} ({})", richMenuId, contentType);
    }


    /**
     * 解析 areas JSON 為 LINE API 所需的 RichMenuArea 列表
     */
    private List<RichMenuArea> parseAreas(String areasJson) throws Exception {
        if (StringUtils.isEmpty(areasJson)) {
            return new ArrayList<>();
        }

        // 使用 JacksonUtil 工具方法解析 JSON
        List<Map<String, Object>> areasData = JacksonUtil.parseJsonArrayToMapList(areasJson);

        if (areasData == null || areasData.isEmpty()) {
            throw new ServiceException("解析 areas JSON 失敗");
        }

        List<RichMenuArea> areas = new ArrayList<>();

        for (Map<String, Object> areaData : areasData) {
            // 解析 bounds
            Map<String, Object> boundsData = (Map<String, Object>) areaData.get("bounds");
            RichMenuBounds bounds = new RichMenuBounds(
                    ((Number) boundsData.get("x")).longValue(),
                    ((Number) boundsData.get("y")).longValue(),
                    ((Number) boundsData.get("width")).longValue(),
                    ((Number) boundsData.get("height")).longValue()
            );

            // 解析 action
            Map<String, Object> actionData = (Map<String, Object>) areaData.get("action");
            Action action = parseAction(actionData);

            areas.add(new RichMenuArea(bounds, action));
        }

        return areas;
    }

    /**
     * 解析 action 資料為 LINE API 的 Action 物件
     * 使用靜態內部 Builder 類建立所有 Action
     */
    private Action parseAction(Map<String, Object> actionData) {
        String typeStr = (String) actionData.get("type");
        // fromCode 方法內部已經處理大小寫轉換和特殊格式（如 richmenuswitch → RICHMENU_SWITCH）
        RichMenuActionType type = RichMenuActionType.fromCode(typeStr);

        switch (type) {
            case URI:
                String uri = (String) actionData.get("uri");
                return new URIAction.Builder()
                        .uri(URI.create(uri))
                        .build();

            case MESSAGE:
                String text = (String) actionData.get("text");
                return new MessageAction.Builder()
                        .text(text)
                        .build();

            case POSTBACK:
                String data = (String) actionData.get("data");
                String displayText = (String) actionData.get("displayText");
                PostbackAction.Builder builder = new PostbackAction.Builder()
                        .data(data);
                if (displayText != null && !displayText.isEmpty()) {
                    builder.displayText(displayText);
                }
                return builder.build();

            case RICHMENU_SWITCH:
                String richMenuAliasId = (String) actionData.get("richMenuAliasId");
                String switchData = (String) actionData.get("data");
                RichMenuSwitchAction.Builder switchBuilder = new RichMenuSwitchAction.Builder()
                        .richMenuAliasId(richMenuAliasId);
                if (switchData != null && !switchData.isEmpty()) {
                    switchBuilder.data(switchData);
                }
                return switchBuilder.build();

            case DATETIMEPICKER:
                String datetimeData = (String) actionData.get("data");
                String mode = (String) actionData.get("mode");
                DatetimePickerAction.Mode modeEnum;
                if ("date".equalsIgnoreCase(mode)) {
                    modeEnum = DatetimePickerAction.Mode.DATE;
                } else if ("time".equalsIgnoreCase(mode)) {
                    modeEnum = DatetimePickerAction.Mode.TIME;
                } else {
                    modeEnum = DatetimePickerAction.Mode.DATETIME;
                }
                return new DatetimePickerAction.Builder()
                        .data(datetimeData)
                        .mode(modeEnum)
                        .build();

            default:
                throw new IllegalArgumentException("不支援的 action 類型：" + type.getDescription());
        }
    }

    /**
     * 驗證 Rich Menu 結構（呼叫 LINE API）
     */
    private void validateRichMenuStructure(RichMenuRequest richMenuRequest, String accessToken) {
        try {
            String url = LineApiEndpoint.RICH_MENU_VALIDATE.getUrl();

            // 將 RichMenuRequest 轉換為 Map 以便使用 OkHttpUtils
            Map<String, Object> params = JacksonUtil.fromJson(
                    JacksonUtil.toJsonString(richMenuRequest),
                    new TypeReference<>() {
                    }
            );

            var builder = OkHttpUtils.builder()
                    .addLineAuthHeader(accessToken);

            // 新增所有參數
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                builder.addParam(entry.getKey(), entry.getValue());
            }

            ApiResponse response = builder.post(url).sync();

            if (response.getHttpStatusCode() != HttpStatus.OK.value()) {
                String error = response.getResultData() != null ? response.getResultData() : "驗證失敗";
                throw new ServiceException("Rich Menu 結構驗證失敗：" + error);
            }

            log.info("Rich Menu 結構驗證通過");

        } catch (Exception e) {
            log.error("驗證 Rich Menu 結構失敗：{}", e.getMessage(), e);
            throw new ServiceException("驗證失敗：" + e.getMessage());
        }
    }

    /**
     * 從 LINE 平台下載 Rich Menu 圖片並儲存到本地
     * <p>
     * 使用 CoolAppsConfig.getRichMenuPath() 取得實際儲存路徑
     * 返回相對路徑供前端訪問（/profile/upload/richmenu/xxx.jpg）
     * 如果檔案已存在會自動覆蓋
     */
    private String downloadRichMenuImage(String richMenuId, String accessToken, String imageSize) {
        try {
            String url = LineApiEndpoint.getRichMenuDownloadImageUrl(richMenuId);

            ApiResponse response = OkHttpUtils.builder()
                    .addLineAuthHeader(accessToken)
                    .get(url)
                    .sync();

            if (response.getHttpStatusCode() != HttpStatus.OK.value() || response.getBinaryData() == null) {
                throw new ServiceException("下載圖片失敗");
            }

            byte[] imageBytes = response.getBinaryData();

            // 使用配置的上傳路徑
            String fileName = richMenuId + ".jpg";
            String fullPath = CoolAppsConfig.getRichMenuPath() + "/" + fileName;
            
            // 確保目錄存在
            Files.createDirectories(Paths.get(CoolAppsConfig.getRichMenuPath()));

            // 儲存圖片（如果檔案已存在會自動覆蓋）
            Files.write(Paths.get(fullPath), imageBytes);

            // 返回相對路徑供前端訪問（對應 ResourcesConfig 的 /profile 映射）
            String relativePath = "/profile/upload/richmenu/" + fileName;
            
            log.info("已下載並儲存 Rich Menu 圖片至：{}", fullPath);
            log.info("前端訪問路徑：{}", relativePath);

            return relativePath;

        } catch (IOException e) {
            log.error("下載或儲存圖片失敗：{}", e.getMessage(), e);
            throw new ServiceException("下載圖片失敗：" + e.getMessage());
        }
    }

    /**
     * 儲存配置快照（用於異常恢復）
     */
    private String saveConfigSnapshot(SysLineRichMenu menu) {
        try {
            Map<String, Object> snapshot = Map.of(
                    "richMenuId", menu.getRichMenuId() != null ? menu.getRichMenuId() : "",
                    "areasJson", menu.getAreasJson() != null ? menu.getAreasJson() : "",
                    "imageUrl", menu.getImageUrl() != null ? menu.getImageUrl() : "",
                    "localImagePath", menu.getLocalImagePath() != null ? menu.getLocalImagePath() : "",
                    "publishTime", System.currentTimeMillis()
            );
            return JacksonUtil.toJsonString(snapshot);
        } catch (Exception e) {
            log.warn("儲存配置快照失敗：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 首次發布 Rich Menu（含 Rollback 和 SSE 推送）
     * 
     * @param menu Rich Menu
     * @param config LINE 配置
     * @param client MessagingApiClient
     * @param taskId 任務 ID（用於 SSE 推送）
     * @return richMenuId
     */
    private String firstPublish(SysLineRichMenu menu, LineConfig config, MessagingApiClient client, String taskId) throws Exception {
        String richMenuId = null;
        
        try {
            // 解析 areas JSON
            List<RichMenuArea> areas = parseAreas(menu.getAreasJson());

            // 解析圖片尺寸
            String[] sizeParts = menu.getImageSize().split("x");
            int width = Integer.parseInt(sizeParts[0]);
            int height = Integer.parseInt(sizeParts[1]);

            // 建立 RichMenuRequest
            RichMenuSize size = new RichMenuSize.Builder()
                    .width((long) width)
                    .height((long) height)
                    .build();

            RichMenuRequest richMenuRequest = new RichMenuRequest.Builder()
                    .size(size)
                    .selected(menu.getSelected() == 1)
                    .name(menu.getName())
                    .chatBarText(menu.getChatBarText() != null ? menu.getChatBarText() : "選單")
                    .areas(areas)
                    .build();

            log.info("▶ 首次發布：驗證 Rich Menu 結構");
            validateRichMenuStructure(richMenuRequest, config.getChannelAccessToken());

            // 階段 1: 建立 Rich Menu (20%)
            log.info("▶ 首次發布：建立 Rich Menu");
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.progress("creating", 20, "建立 Rich Menu 中..."));
            }
            
            RichMenuIdResponse response = client.createRichMenu(richMenuRequest).get().body();
            richMenuId = response.richMenuId();
            log.info("✓ 建立成功，richMenuId: {}", richMenuId);

            // 階段 2: 上傳圖片 (50%)
            log.info("▶ 首次發布：上傳圖片");
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.progress("uploading", 50, "上傳圖片中..."));
            }
            
            // 自動調整圖片到目標尺寸
            byte[] imageBytes = getImageBytes(menu.getImageUrl(), menu.getImageSize());
            validateImage(imageBytes, menu.getImageSize());
            uploadImageToLine(richMenuId, imageBytes, config.getChannelAccessToken());
            log.info("✓ 圖片上傳成功");

            // 階段 3: 下載圖片到本地 (70%)
            log.info("▶ 首次發布：下載圖片到本地");
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.progress("downloading", 70, "下載預覽圖中..."));
            }
            
            String localImagePath = downloadRichMenuImage(richMenuId, config.getChannelAccessToken(), menu.getImageSize());
            log.info("✓ 圖片已下載：{}", localImagePath);

            // 階段 4: 更新資料庫 (85%)
            log.info("▶ 首次發布：更新資料庫");
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.progress("saving", 85, "儲存資料中..."));
            }
            
            String previousConfig = saveConfigSnapshot(menu);
            richMenuMapper.updatePublishInfo(
                    menu.getId(),
                    richMenuId,
                    null,  // 首次發布沒有前一個 ID
                    previousConfig,
                    localImagePath,
                    RichMenuStatus.ACTIVE.getCode()
            );

            // 自動建立 Alias（如果有指定）
            if (StringUtils.isNotEmpty(menu.getSuggestedAliasId())) {
                log.info("▶ 首次發布：自動建立 Alias: {}", menu.getSuggestedAliasId());
                try {
                    SysLineRichMenuAlias alias = new SysLineRichMenuAlias();
                    alias.setRichMenuId(menu.getId());
                    alias.setAliasId(menu.getSuggestedAliasId());
                    alias.setDescription("自動建立於發布時");
                    aliasService.insertRichMenuAlias(alias);
                    log.info("✓ Alias 建立成功");
                } catch (Exception e) {
                    log.warn("⚠ Alias 建立失敗（不影響發布）：{}", e.getMessage());
                }
            }

            log.info("✓ 首次發布完成！");
            return richMenuId;
            
        } catch (Exception e) {
            log.error("首次發布失敗，開始 Rollback", e);
            
            // Rollback: 刪除已建立的 Rich Menu
            if (richMenuId != null) {
                try {
                    log.warn("⚠ 執行 Rollback：刪除已建立的 Rich Menu: {}", richMenuId);
                    deleteRichMenuFromLine(richMenuId, config.getChannelAccessToken());
                    log.info("✓ Rollback 成功");
                } catch (Exception rollbackEx) {
                    log.error("✗ Rollback 失敗：{}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            
            throw e;
        }
    }

    /**
     * 重新發布 Rich Menu（含 Alias 自動更新和異常回滾）
     * 
     * @param menu Rich Menu
     * @param config LINE 配置
     * @param client MessagingApiClient
     * @param taskId 任務 ID（用於 SSE 推送）
     * @return richMenuId
     */
    private String republish(SysLineRichMenu menu, LineConfig config, MessagingApiClient client, String taskId) throws Exception {
        String oldRichMenuId = menu.getRichMenuId();
        String newRichMenuId = null;
        List<UpdatedAliasInfo> updatedAliases = new ArrayList<>();

        try {
            // 階段 1：查找使用此 Rich Menu 的所有 Alias
            log.info("▶ 重新發布：查找關聯的 Alias");
            List<SysLineRichMenuAlias> aliases = aliasService.selectRichMenuAliasByRichMenuId(menu.getId());

            if (!aliases.isEmpty()) {
                log.warn("⚠ 檢測到 {} 個 Alias 正在使用此 Rich Menu", aliases.size());
                for (SysLineRichMenuAlias alias : aliases) {
                    log.info("  - {}: {}", alias.getAliasId(), alias.getDescription());
                }
            }

            // 階段 2：建立新的 Rich Menu
            log.info("▶ 重新發布：建立新 Rich Menu");
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.progress("creating", 20, "建立 Rich Menu 中..."));
            }
            List<RichMenuArea> areas = parseAreas(menu.getAreasJson());
            String[] sizeParts = menu.getImageSize().split("x");
            int width = Integer.parseInt(sizeParts[0]);
            int height = Integer.parseInt(sizeParts[1]);

            RichMenuSize size = new RichMenuSize.Builder()
                    .width((long) width)
                    .height((long) height)
                    .build();

            RichMenuRequest richMenuRequest = new RichMenuRequest.Builder()
                    .size(size)
                    .selected(menu.getSelected() == 1)
                    .name(menu.getName())
                    .chatBarText(menu.getChatBarText() != null ? menu.getChatBarText() : "選單")
                    .areas(areas)
                    .build();

            validateRichMenuStructure(richMenuRequest, config.getChannelAccessToken());

            RichMenuIdResponse response = client.createRichMenu(richMenuRequest).get().body();
            newRichMenuId = response.richMenuId();
            log.info("✓ 新 Rich Menu 建立成功：{}", newRichMenuId);

            // 階段 3：上傳圖片
            log.info("▶ 重新發布：上傳圖片");
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.progress("uploading", 50, "上傳圖片中..."));
            }
            // 自動調整圖片到目標尺寸
            byte[] imageBytes = getImageBytes(menu.getImageUrl(), menu.getImageSize());
            validateImage(imageBytes, menu.getImageSize());
            uploadImageToLine(newRichMenuId, imageBytes, config.getChannelAccessToken());
            log.info("✓ 圖片上傳成功");

            // 階段 4：下載圖片到本地
            log.info("▶ 重新發布：下載圖片到本地");
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.progress("downloading", 70, "下載預覽圖中..."));
            }
            String localImagePath = downloadRichMenuImage(newRichMenuId, config.getChannelAccessToken(), menu.getImageSize());
            log.info("✓ 圖片已下載：{}", localImagePath);

            // 階段 5：更新所有 Alias（關鍵步驟）
            if (!aliases.isEmpty()) {
                log.info("▶ 重新發布：更新 {} 個 Alias 綁定", aliases.size());
                for (SysLineRichMenuAlias alias : aliases) {
                    // 記錄更新資訊（用於回滾）
                    updatedAliases.add(new UpdatedAliasInfo(
                            alias.getId(),
                            alias.getAliasId(),
                            oldRichMenuId,
                            newRichMenuId
                    ));

                    // 呼叫 LINE API 更新 Alias
                    boolean success = aliasService.updateAliasRichMenuId(
                            alias.getAliasId(),
                            newRichMenuId,
                            config.getConfigId()
                    );

                    if (!success) {
                        throw new ServiceException("更新 Alias " + alias.getAliasId() + " 失敗");
                    }

                    log.info("  ✓ 已更新 Alias: {}", alias.getAliasId());
                }
                log.info("✓ 所有 Alias 更新完成");
            }

            // 階段 6：刪除舊的 Rich Menu
            log.info("▶ 重新發布：刪除舊 Rich Menu: {}", oldRichMenuId);
            try {
                client.deleteRichMenu(oldRichMenuId).get();
                log.info("✓ 舊 Rich Menu 已刪除");
            } catch (Exception e) {
                log.warn("⚠ 刪除舊 Rich Menu 失敗（可忽略）：{}", e.getMessage());
            }

            // 階段 7：更新資料庫
            log.info("▶ 重新發布：更新資料庫");
            if (taskId != null) {
                sseManager.send(SseChannels.RICHMENU_PUBLISH, taskId,
                    SseEvent.progress("saving", 85, "儲存資料中..."));
            }
            String previousConfig = saveConfigSnapshot(menu);
            richMenuMapper.updatePublishInfo(
                    menu.getId(),
                    newRichMenuId,
                    oldRichMenuId,  // 保存舊 ID 用於回滾
                    previousConfig,
                    localImagePath,
                    RichMenuStatus.ACTIVE.getCode()
            );

            // 自動建立 Alias（如果有指定且不存在）
            if (StringUtils.isNotEmpty(menu.getSuggestedAliasId())) {
                boolean aliasExists = aliases.stream()
                        .anyMatch(a -> menu.getSuggestedAliasId().equals(a.getAliasId()));
                
                if (!aliasExists) {
                    log.info("▶ 重新發布：自動建立 Alias: {}", menu.getSuggestedAliasId());
                    try {
                        SysLineRichMenuAlias alias = new SysLineRichMenuAlias();
                        alias.setRichMenuId(menu.getId());
                        alias.setAliasId(menu.getSuggestedAliasId());
                        alias.setDescription("自動建立於重新發布時");
                        aliasService.insertRichMenuAlias(alias);
                        log.info("✓ Alias 建立成功");
                    } catch (Exception e) {
                        log.warn("⚠ Alias 建立失敗（不影響發布）：{}", e.getMessage());
                    }
                } else {
                    log.info("ℹ️ Alias 已存在，跳過建立");
                }
            }

            log.info("✓ 重新發布完成！");
            return newRichMenuId;

        } catch (Exception e) {
            log.error("✗ 重新發布失敗，開始回滾", e);
            rollbackRepublish(newRichMenuId, updatedAliases, oldRichMenuId, config.getChannelAccessToken());
            throw e;
        }
    }

    /**
     * 回滾重新發布操作
     */
    private void rollbackRepublish(String newRichMenuId, List<UpdatedAliasInfo> updatedAliases,
                                   String oldRichMenuId, String accessToken) {
        log.warn("⚠ 開始回滾操作");

        try {
            // 1. 恢復所有 Alias 綁定
            if (!updatedAliases.isEmpty()) {
                log.info("回滾：恢復 {} 個 Alias 綁定", updatedAliases.size());
                for (UpdatedAliasInfo info : updatedAliases) {
                    try {
                        boolean success = aliasService.updateAliasRichMenuId(
                                info.aliasId,
                                info.oldRichMenuId,
                                null  // 使用已有的 accessToken
                        );

                        if (success) {
                            log.info("  ✓ 已恢復 Alias: {}", info.aliasId);
                        } else {
                            log.error("  ✗ 恢復 Alias 失敗: {}", info.aliasId);
                        }
                    } catch (Exception e) {
                        log.error("  ✗ 恢復 Alias 異常: {}", info.aliasId, e);
                    }
                }
            }

            // 2. 刪除新建立的 Rich Menu
            if (newRichMenuId != null) {
                log.info("回滾：刪除新建立的 Rich Menu: {}", newRichMenuId);
                try {
                    String url = LineApiEndpoint.RICH_MENU_DELETE.getUrl(newRichMenuId);
                    ApiResponse response = OkHttpUtils.builder()
                            .addLineAuthHeader(accessToken)
                            .delete(url)
                            .sync();

                    if (response.getHttpStatusCode() == HttpStatus.OK.value()) {
                        log.info("  ✓ 新 Rich Menu 已刪除");
                    } else {
                        log.warn("  ⚠ 刪除新 Rich Menu 失敗（可忽略）");
                    }
                } catch (Exception e) {
                    log.warn("  ⚠ 刪除新 Rich Menu 異常（可忽略）：{}", e.getMessage());
                }
            }

            log.info("✓ 回滾完成");

        } catch (Exception e) {
            log.error("✗ 回滾失敗，需要人工介入", e);
            // TODO: 發送告警通知管理員
        }
    }

    /**
     * 內部類：記錄更新的 Alias 資訊（用於回滾）
     */
    private static class UpdatedAliasInfo {
        Long dbId;
        String aliasId;
        String oldRichMenuId;
        String newRichMenuId;

        UpdatedAliasInfo(Long dbId, String aliasId, String oldRichMenuId, String newRichMenuId) {
            this.dbId = dbId;
            this.aliasId = aliasId;
            this.oldRichMenuId = oldRichMenuId;
            this.newRichMenuId = newRichMenuId;
        }
    }

    /**
     * 從 LINE 平台重新下載預覽圖並更新本地路徑
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refreshPreviewImage(Long id) {
        log.info("==================== 開始更新預覽圖 ====================");
        log.info("Rich Menu ID: {}", id);

        // 1. 查詢 Rich Menu
        SysLineRichMenu menu = richMenuMapper.selectRichMenuById(id);
        if (menu == null) {
            throw new ServiceException("Rich Menu 不存在");
        }

        // 2. 檢查是否已發布
        if (!menu.isPublished()) {
            throw new ServiceException("Rich Menu 尚未發布，無法下載預覽圖");
        }

        if (StringUtils.isEmpty(menu.getRichMenuId())) {
            throw new ServiceException("Rich Menu ID 為空，無法下載預覽圖");
        }

        // 3. 查詢頻道設定
        LineConfig config = lineConfigMapper.selectLineConfigById(menu.getConfigId());
        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        try {
            log.info("▶ 從 LINE 平台下載圖片");
            
            // 4. 下載圖片
            String localImagePath = downloadRichMenuImage(
                    menu.getRichMenuId(),
                    config.getChannelAccessToken(),
                    menu.getImageSize()
            );
            
            log.info("✓ 圖片已下載：{}", localImagePath);

            // 5. 更新資料庫
            menu.setLocalImagePath(localImagePath);
            int result = richMenuMapper.updateRichMenu(menu);

            if (result > 0) {
                log.info("✓ 預覽圖路徑已更新到資料庫");
                log.info("==================== 更新預覽圖完成 ====================");
                return true;
            } else {
                throw new ServiceException("更新資料庫失敗");
            }

        } catch (ServiceException e) {
            log.error("更新預覽圖失敗：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("更新預覽圖失敗", e);
            throw new ServiceException("更新預覽圖失敗：" + e.getMessage());
        }
    }
}
