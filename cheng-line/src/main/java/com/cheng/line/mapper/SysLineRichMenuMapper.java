package com.cheng.line.mapper;

import com.cheng.line.domain.SysLineRichMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * LINE Rich Menu Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface SysLineRichMenuMapper {

    /**
     * 查詢 Rich Menu 列表
     *
     * @param richMenu Rich Menu 查詢條件
     * @return Rich Menu 列表
     */
    List<SysLineRichMenu> selectRichMenuList(SysLineRichMenu richMenu);

    /**
     * 根據 ID 查詢 Rich Menu
     *
     * @param id Rich Menu ID
     * @return Rich Menu 物件
     */
    SysLineRichMenu selectRichMenuById(Long id);

    /**
     * 根據 LINE richMenuId 查詢
     *
     * @param richMenuId LINE 平台的 richMenuId
     * @return Rich Menu 物件
     */
    SysLineRichMenu selectRichMenuByRichMenuId(String richMenuId);

    /**
     * 根據頻道 ID 查詢預設選單
     *
     * @param configId 頻道設定 ID
     * @return Rich Menu 物件
     */
    SysLineRichMenu selectDefaultRichMenuByConfigId(Integer configId);

    /**
     * 根據頻道 ID 查詢使用中的選單
     *
     * @param configId 頻道設定 ID
     * @return Rich Menu 物件
     */
    SysLineRichMenu selectSelectedRichMenuByConfigId(Integer configId);

    /**
     * 新增 Rich Menu
     *
     * @param richMenu Rich Menu 物件
     * @return 影響行數
     */
    int insertRichMenu(SysLineRichMenu richMenu);

    /**
     * 修改 Rich Menu
     *
     * @param richMenu Rich Menu 物件
     * @return 影響行數
     */
    int updateRichMenu(SysLineRichMenu richMenu);

    /**
     * 根據 ID 刪除 Rich Menu
     *
     * @param id Rich Menu ID
     * @return 影響行數
     */
    int deleteRichMenuById(Long id);

    /**
     * 批次刪除 Rich Menu
     *
     * @param ids Rich Menu ID 陣列
     * @return 影響行數
     */
    int deleteRichMenuByIds(Long[] ids);

    /**
     * 取消指定頻道的所有預設選單標記
     *
     * @param configId 頻道設定 ID
     * @return 影響行數
     */
    int unsetAllDefaultByConfigId(Integer configId);

    /**
     * 取消指定頻道的所有使用中選單標記
     *
     * @param configId 頻道設定 ID
     * @return 影響行數
     */
    int unsetAllSelectedByConfigId(Integer configId);

    /**
     * 設定指定 Rich Menu 為預設選單
     *
     * @param id Rich Menu ID
     * @return 影響行數
     */
    int setDefaultById(Long id);

    /**
     * 設定指定 Rich Menu 為使用中選單
     *
     * @param id Rich Menu ID
     * @return 影響行數
     */
    int setSelectedById(Long id);

    /**
     * 更新 Rich Menu 的 richMenuId（發布後）
     *
     * @param id         Rich Menu ID
     * @param richMenuId LINE 平台返回的 richMenuId
     * @return 影響行數
     */
    int updateRichMenuId(@Param("id") Long id, @Param("richMenuId") String richMenuId);

    /**
     * 更新 Rich Menu 狀態
     *
     * @param id     Rich Menu ID
     * @param status 狀態代碼
     * @return 影響行數
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 檢查選單名稱是否重複
     *
     * @param configId 頻道設定 ID
     * @param name     選單名稱
     * @param excludeId 排除的 ID（更新時使用）
     * @return Rich Menu 物件
     */
    SysLineRichMenu checkNameUnique(@Param("configId") Integer configId, @Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 更新 Rich Menu 發布資訊（含歷史記錄）
     *
     * @param id                  Rich Menu ID
     * @param richMenuId          新的 LINE Rich Menu ID
     * @param previousRichMenuId  舊的 Rich Menu ID
     * @param previousConfig      前一版本配置快照
     * @param localImagePath      本地圖片路徑
     * @param status              狀態
     * @return 影響行數
     */
    int updatePublishInfo(@Param("id") Long id,
                         @Param("richMenuId") String richMenuId,
                         @Param("previousRichMenuId") String previousRichMenuId,
                         @Param("previousConfig") String previousConfig,
                         @Param("localImagePath") String localImagePath,
                         @Param("status") String status);
}
