package com.cheng.line.service;

import com.cheng.line.domain.SysLineRichMenu;
import com.cheng.line.domain.SysLineRichMenuAlias;

import java.util.List;
import java.util.Map;

/**
 * LINE Rich Menu Alias Service 介面
 * 
 * @author cheng
 */
public interface ISysLineRichMenuAliasService {
    
    /**
     * 查詢 Rich Menu Alias
     * 
     * @param id Rich Menu Alias 主鍵
     * @return Rich Menu Alias
     */
    SysLineRichMenuAlias selectRichMenuAliasById(Long id);

    /**
     * 根據 Alias ID 查詢
     * 
     * @param aliasId Alias ID
     * @return Rich Menu Alias
     */
    SysLineRichMenuAlias selectRichMenuAliasByAliasId(String aliasId);

    /**
     * 查詢 Rich Menu Alias 列表
     * 
     * @param richMenuAlias Rich Menu Alias
     * @return Rich Menu Alias 集合
     */
    List<SysLineRichMenuAlias> selectRichMenuAliasList(SysLineRichMenuAlias richMenuAlias);

    /**
     * 根據 Rich Menu ID 查詢所有 Alias
     * 
     * @param richMenuId Rich Menu ID
     * @return Rich Menu Alias 集合
     */
    List<SysLineRichMenuAlias> selectRichMenuAliasByRichMenuId(Long richMenuId);

    /**
     * 新增 Rich Menu Alias 並同步到 LINE 平台
     * 
     * @param richMenuAlias Rich Menu Alias
     * @return 結果
     */
    int insertRichMenuAlias(SysLineRichMenuAlias richMenuAlias);

    /**
     * 修改 Rich Menu Alias 並同步到 LINE 平台
     * 
     * @param richMenuAlias Rich Menu Alias
     * @return 結果
     */
    int updateRichMenuAlias(SysLineRichMenuAlias richMenuAlias);

    /**
     * 批次刪除 Rich Menu Alias 並從 LINE 平台刪除
     * 
     * @param ids 需要刪除的 Rich Menu Alias 主鍵集合
     * @return 結果
     */
    int deleteRichMenuAliasByIds(Long[] ids);

    /**
     * 刪除 Rich Menu Alias 並從 LINE 平台刪除
     * 
     * @param id Rich Menu Alias 主鍵
     * @return 結果
     */
    int deleteRichMenuAliasById(Long id);

    /**
     * 檢查 Alias ID 是否唯一
     * 
     * @param aliasId Alias ID
     * @return 結果
     */
    boolean checkAliasIdUnique(String aliasId);

    /**
     * 從 LINE 平台同步 Alias 列表
     * 
     * @return 結果
     */
    int syncAliasFromLine();

    /**
     * 查找使用指定別名的 Rich Menu 列表
     * 
     * @param aliasId Alias ID
     * @return 使用該別名的 Rich Menu 列表
     */
    List<SysLineRichMenu> findMenusUsingAlias(String aliasId);

    /**
     * 從 LINE 平台獲取別名列表（用於驗證）
     * 
     * @param configId Config ID
     * @return LINE 平台上的別名 ID 列表
     */
    List<String> listAliasesFromLine(Integer configId);

    /**
     * 檢查別名使用情況
     * 返回使用該別名的選單資訊，用於刪除前確認
     * 
     * @param aliasId Alias ID
     * @return Map 包含 usedByMenus (List<Map>) 和 canDelete (boolean)
     */
    Map<String, Object> checkAliasUsage(String aliasId);

    /**
     * 更新 Alias 綁定的 Rich Menu ID（用於重新發布時同步更新）
     * 
     * @param aliasId        Alias ID
     * @param newRichMenuId  新的 Rich Menu ID
     * @param configId       Config ID（用於獲取 access token）
     * @return 是否成功
     */
    boolean updateAliasRichMenuId(String aliasId, String newRichMenuId, Integer configId);
}
