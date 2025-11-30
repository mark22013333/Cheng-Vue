package com.cheng.line.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.JacksonUtil;
import com.cheng.common.utils.OkHttpUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.dto.ApiResponse;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.domain.SysLineRichMenu;
import com.cheng.line.domain.SysLineRichMenuAlias;
import com.cheng.line.enums.LineApiEndpoint;
import com.cheng.line.mapper.LineConfigMapper;
import com.cheng.line.mapper.SysLineRichMenuAliasMapper;
import com.cheng.line.mapper.SysLineRichMenuMapper;
import com.cheng.line.service.ISysLineRichMenuAliasService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LINE Rich Menu Alias Service 實作類別
 * 
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLineRichMenuAliasServiceImpl implements ISysLineRichMenuAliasService {

    private final SysLineRichMenuAliasMapper richMenuAliasMapper;
    private final SysLineRichMenuMapper richMenuMapper;
    private final LineConfigMapper lineConfigMapper;

    @Override
    public SysLineRichMenuAlias selectRichMenuAliasById(Long id) {
        return richMenuAliasMapper.selectRichMenuAliasById(id);
    }

    @Override
    public SysLineRichMenuAlias selectRichMenuAliasByAliasId(String aliasId) {
        return richMenuAliasMapper.selectRichMenuAliasByAliasId(aliasId);
    }

    @Override
    public List<SysLineRichMenuAlias> selectRichMenuAliasList(SysLineRichMenuAlias richMenuAlias) {
        return richMenuAliasMapper.selectRichMenuAliasList(richMenuAlias);
    }

    @Override
    public List<SysLineRichMenuAlias> selectRichMenuAliasByRichMenuId(Long richMenuId) {
        return richMenuAliasMapper.selectRichMenuAliasByRichMenuId(richMenuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRichMenuAlias(SysLineRichMenuAlias richMenuAlias) {
        // 驗證 Alias ID 格式
        if (!validateAliasIdFormat(richMenuAlias.getAliasId())) {
            throw new ServiceException("Alias ID 格式錯誤：只能包含小寫字母、數字和連字號，例如：richmenu-alias-a");
        }
        
        // 檢查 Alias ID 在資料庫中是否唯一
        if (!checkAliasIdUnique(richMenuAlias.getAliasId())) {
            throw new ServiceException("Alias ID 已存在於資料庫");
        }

        // 取得 Rich Menu 的 LINE ID
        SysLineRichMenu richMenu = richMenuMapper.selectRichMenuById(richMenuAlias.getRichMenuId());
        if (richMenu == null) {
            throw new ServiceException("Rich Menu 不存在");
        }

        if (richMenu.getRichMenuId() == null || richMenu.getRichMenuId().isEmpty()) {
            throw new ServiceException("Rich Menu 尚未發布到 LINE 平台，請先發布");
        }

        // 從 LINE 平台取得別名列表，檢查是否已存在
        try {
            List<String> lineAliases = listAliasesFromLine(richMenu.getConfigId());
            if (lineAliases.contains(richMenuAlias.getAliasId())) {
                throw new ServiceException("Alias ID 已存在於 LINE 平台，請使用同步功能將其匯入");
            }
        } catch (ServiceException e) {
            // 如果錯誤訊息是「已存在於 LINE 平台」，直接拋出
            if (e.getMessage().contains("已存在於 LINE 平台")) {
                throw e;
            }
            // 其他錯誤記錄但不阻止建立（可能是網路問題）
            log.warn("無法從 LINE 平台驗證別名唯一性：{}", e.getMessage());
        }

        // 呼叫 LINE API 建立 Alias
        try {
            createAliasOnLine(richMenuAlias.getAliasId(), richMenu.getRichMenuId(), richMenu.getConfigId());
            log.info("===> 在 LINE 平台建立 Rich Menu Alias 成功：{}", richMenuAlias.getAliasId());
        } catch (Exception e) {
            log.error("---> 在 LINE 平台建立 Rich Menu Alias 失敗", e);
            throw new ServiceException("在 LINE 平台建立 Alias 失敗：" + e.getMessage());
        }

        // 儲存到資料庫
        richMenuAlias.setCreateBy(SecurityUtils.getUsername());
        return richMenuAliasMapper.insertRichMenuAlias(richMenuAlias);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRichMenuAlias(SysLineRichMenuAlias richMenuAlias) {
        // 不允許更新別名，要求用戶刪除後重新建立
        throw new ServiceException("不支援更新別名功能，請刪除後重新建立");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRichMenuAliasByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += deleteRichMenuAliasById(id);
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRichMenuAliasById(Long id) {
        SysLineRichMenuAlias alias = richMenuAliasMapper.selectRichMenuAliasById(id);
        if (alias == null) {
            throw new ServiceException("Alias 不存在");
        }

        // 查找使用該別名的選單
        List<SysLineRichMenu> usedMenus = findMenusUsingAlias(alias.getAliasId());
        
        // 取得 Rich Menu 的 Config ID
        SysLineRichMenu richMenu = richMenuMapper.selectRichMenuById(alias.getRichMenuId());
        if (richMenu != null) {
            try {
                // 先從 LINE 平台刪除別名
                deleteAliasFromLine(alias.getAliasId(), richMenu.getConfigId());
                log.info("===> 從 LINE 平台刪除 Rich Menu Alias 成功：{}", alias.getAliasId());
            } catch (Exception e) {
                log.warn("---> 從 LINE 平台刪除 Alias 失敗（可能已被刪除）：{}", e.getMessage());
                // 不拋出異常，繼續處理
            }
        }

        // 清空所有關聯選單中的別名引用
        if (!usedMenus.isEmpty()) {
            log.info("準備清空 {} 個選單中的別名引用：{}", usedMenus.size(), alias.getAliasId());
            for (SysLineRichMenu menu : usedMenus) {
                try {
                    removeAliasFromMenu(menu.getId(), alias.getAliasId());
                } catch (Exception e) {
                    log.error("清空選單 {} 中的別名引用失敗", menu.getName(), e);
                    // 繼續處理其他選單
                }
            }
        }

        // 從資料庫刪除
        return richMenuAliasMapper.deleteRichMenuAliasById(id);
    }

    @Override
    public boolean checkAliasIdUnique(String aliasId) {
        return richMenuAliasMapper.checkAliasIdUnique(aliasId) == 0;
    }

    @Override
    public int syncAliasFromLine() {
        // 此功能較複雜，需要遍歷所有 Rich Menu 並查詢其 Alias
        // 目前先返回 0，後續可擴充
        log.info("同步 Alias 功能尚未實作");
        return 0;
    }

    /**
     * 在 LINE 平台建立 Rich Menu Alias
     * 
     * @param aliasId Rich Menu Alias ID
     * @param richMenuId LINE Rich Menu ID
     * @param configId Config ID（用於取得 Channel Access Token）
     */
    private void createAliasOnLine(String aliasId, String richMenuId, Integer configId) {
        String accessToken = getChannelAccessToken(configId);
        String url = LineApiEndpoint.RICH_MENU_ALIAS_CREATE.getUrl();

        ApiResponse response = OkHttpUtils.builder()
                .addLineAuthHeader(accessToken)
                .addParam("richMenuAliasId", aliasId)
                .addParam("richMenuId", richMenuId)
                .post(url)
                .sync();

        if (response.getHttpStatusCode() != HttpStatus.OK.value()) {
            String errorMsg = response.getResultData() != null ? response.getResultData() : "未知錯誤";
            throw new ServiceException("LINE API 建立 Alias 失敗：" + response.getHttpStatusCode() + " - " + errorMsg);
        }
        
        log.debug("LINE API 建立 Alias 成功");
    }

    /**
     * 從 LINE 平台刪除 Rich Menu Alias
     * 
     * @param aliasId Rich Menu Alias ID
     * @param configId Config ID（用於取得 Channel Access Token）
     */
    private void deleteAliasFromLine(String aliasId, Integer configId) {
        String accessToken = getChannelAccessToken(configId);
        String url = LineApiEndpoint.RICH_MENU_ALIAS_DELETE.getUrl(aliasId);

        ApiResponse response = OkHttpUtils.builder()
                .addLineAuthHeader(accessToken)
                .delete(url)
                .sync();

        if (response.getHttpStatusCode() != HttpStatus.OK.value()) {
            String errorMsg = response.getResultData() != null ? response.getResultData() : "未知錯誤";
            throw new ServiceException("LINE API 刪除 Alias 失敗：" + response.getHttpStatusCode() + " - " + errorMsg);
        }
        
        log.debug("LINE API 刪除 Alias 成功");
    }

    /**
     * 驗證 Alias ID 格式
     * 根據 LINE API 規範，Alias ID 只能包含：
     * - 小寫字母（a-z）
     * - 數字（0-9）
     * - 連字號（-）
     * 
     * @param aliasId Alias ID
     * @return 格式是否正確
     */
    private boolean validateAliasIdFormat(String aliasId) {
        if (aliasId == null || aliasId.trim().isEmpty()) {
            return false;
        }
        // 只允許小寫字母、數字和連字號
        return aliasId.matches("^[a-z0-9-]+$");
    }

    /**
     * 取得 Channel Access Token
     * 
     * @param configId Config ID
     * @return Channel Access Token
     */
    private String getChannelAccessToken(Integer configId) {
        LineConfig config = lineConfigMapper.selectLineConfigById(configId);
        
        if (config == null) {
            throw new ServiceException("找不到對應的 LINE Config");
        }

        String accessToken = config.getChannelAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new ServiceException("Channel Access Token 未設定");
        }

        return accessToken;
    }

    @Override
    public List<SysLineRichMenu> findMenusUsingAlias(String aliasId) {
        if (StringUtils.isEmpty(aliasId)) {
            return new ArrayList<>();
        }

        // 查詢所有 Rich Menu（不限制狀態，因為可能在草稿狀態就設定了 alias）
        SysLineRichMenu query = new SysLineRichMenu();
        List<SysLineRichMenu> allMenus = richMenuMapper.selectRichMenuList(query);

        // 過濾出使用該 aliasId 的選單
        return allMenus.stream()
                .filter(menu -> menuUsesAlias(menu, aliasId))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listAliasesFromLine(Integer configId) {
        String accessToken = getChannelAccessToken(configId);
        String url = LineApiEndpoint.RICH_MENU_ALIAS_LIST.getUrl();

        ApiResponse response = OkHttpUtils.builder()
                .addLineAuthHeader(accessToken)
                .get(url)
                .sync();

        if (response.getHttpStatusCode() != HttpStatus.OK.value()) {
            String errorMsg = response.getResultData() != null ? response.getResultData() : "未知錯誤";
            throw new ServiceException("從 LINE 平台取得別名列表失敗：" + response.getHttpStatusCode() + " - " + errorMsg);
        }

        try {
            // 解析回應：{"aliases": [{"richMenuAliasId": "xxx", "richMenuId": "yyy"}, ...]}
            Map<String, Object> result = JacksonUtil.fromJson(response.getResultData(), new TypeReference<Map<String, Object>>() {});
            List<Map<String, String>> aliases = (List<Map<String, String>>) result.get("aliases");
            
            if (aliases == null) {
                return new ArrayList<>();
            }

            return aliases.stream()
                    .map(alias -> alias.get("richMenuAliasId"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("解析 LINE API 回應失敗", e);
            throw new ServiceException("解析別名列表失敗");
        }
    }

    @Override
    public Map<String, Object> checkAliasUsage(String aliasId) {
        Map<String, Object> result = new HashMap<>();
        
        // 查找使用該別名的選單
        List<SysLineRichMenu> usedMenus = findMenusUsingAlias(aliasId);
        
        // 構建回應資料
        List<Map<String, Object>> menuInfo = usedMenus.stream()
                .map(menu -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", menu.getId());
                    info.put("name", menu.getName());
                    info.put("richMenuId", menu.getRichMenuId());
                    return info;
                })
                .collect(Collectors.toList());

        result.put("usedByMenus", menuInfo);
        result.put("canDelete", true); // 允許刪除，但會清空關聯選單的 aliasId
        result.put("usageCount", usedMenus.size());

        return result;
    }

    /**
     * 檢查 Rich Menu 是否使用了指定的 aliasId
     */
    private boolean menuUsesAlias(SysLineRichMenu menu, String aliasId) {
        if (StringUtils.isEmpty(menu.getAreasJson())) {
            return false;
        }

        try {
            // 解析 areas JSON
            List<Map<String, Object>> areas = JacksonUtil.fromJson(
                    menu.getAreasJson(), 
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            if (areas == null) {
                return false;
            }

            // 檢查每個 area 的 action
            for (Map<String, Object> area : areas) {
                Map<String, Object> action = (Map<String, Object>) area.get("action");
                if (action == null) {
                    continue;
                }

                String type = (String) action.get("type");
                if ("richmenuswitch".equals(type)) {
                    String richMenuAliasId = (String) action.get("richMenuAliasId");
                    if (aliasId.equals(richMenuAliasId)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (Exception e) {
            log.error("解析選單 {} 的 areasJson 失敗", menu.getId(), e);
            return false;
        }
    }

    /**
     * 從選單中移除指定的 aliasId 引用
     * 保留 action 結構，只清空 richMenuAliasId 欄位
     */
    private void removeAliasFromMenu(Long menuId, String aliasId) {
        SysLineRichMenu menu = richMenuMapper.selectRichMenuById(menuId);
        if (menu == null || StringUtils.isEmpty(menu.getAreasJson())) {
            return;
        }

        try {
            // 解析 areas JSON
            List<Map<String, Object>> areas = JacksonUtil.fromJson(
                    menu.getAreasJson(), 
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            if (areas == null) {
                return;
            }

            boolean modified = false;

            // 清空使用該 aliasId 的 action 中的 richMenuAliasId
            for (Map<String, Object> area : areas) {
                Map<String, Object> action = (Map<String, Object>) area.get("action");
                if (action == null) {
                    continue;
                }

                String type = (String) action.get("type");
                if ("richmenuswitch".equals(type)) {
                    String richMenuAliasId = (String) action.get("richMenuAliasId");
                    if (aliasId.equals(richMenuAliasId)) {
                        // 清空 aliasId，保留 action 結構
                        action.put("richMenuAliasId", "");
                        modified = true;
                        log.info("清空選單 {} 中的別名引用：{}", menu.getName(), aliasId);
                    }
                }
            }

            // 如果有修改，更新資料庫
            if (modified) {
                String updatedAreasJson = JacksonUtil.toJsonString(areas);
                menu.setAreasJson(updatedAreasJson);
                richMenuMapper.updateRichMenu(menu);
                log.info("已更新選單 {} 的 areasJson", menu.getName());
            }

        } catch (Exception e) {
            log.error("移除選單 {} 中的別名引用失敗", menuId, e);
            throw new ServiceException("移除別名引用失敗");
        }
    }

    @Override
    public boolean updateAliasRichMenuId(String aliasId, String newRichMenuId, Integer configId) {
        try {
            // 取得 access token
            String accessToken = getChannelAccessToken(configId);
            
            // 呼叫 LINE API 更新 Alias
            String url = LineApiEndpoint.RICH_MENU_ALIAS_UPDATE.getUrl(aliasId);
            
            ApiResponse response = OkHttpUtils.builder()
                    .addLineAuthHeader(accessToken)
                    .addParam("richMenuId", newRichMenuId)
                    .post(url)
                    .sync();
            
            if (response.getHttpStatusCode() != HttpStatus.OK.value()) {
                log.error("LINE API 更新 Alias 失敗：HTTP {}", response.getHttpStatusCode());
                return false;
            }
            
            log.info("已更新 Alias {} 綁定的 Rich Menu ID: {}", aliasId, newRichMenuId);
            
            // 注意：DB 中 Alias 的 richMenuId 欄位是關聯到 sys_line_rich_menu 的 ID，不是 LINE 的 richMenuId
            // 所以這裡不需要更新 DB，只需要確保 LINE API 調用成功即可
            
            return true;
            
        } catch (Exception e) {
            log.error("更新 Alias {} 的 Rich Menu ID 失敗：{}", aliasId, e.getMessage(), e);
            return false;
        }
    }
}
