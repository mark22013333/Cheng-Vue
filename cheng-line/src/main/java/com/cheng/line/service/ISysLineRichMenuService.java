package com.cheng.line.service;

import com.cheng.line.domain.SysLineRichMenu;

import java.util.List;

/**
 * LINE Rich Menu Service 介面
 *
 * @author cheng
 */
public interface ISysLineRichMenuService {

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
     * 批次刪除 Rich Menu
     *
     * @param ids Rich Menu ID 陣列
     * @return 影響行數
     */
    int deleteRichMenuByIds(Long[] ids);

    /**
     * 根據 ID 刪除 Rich Menu
     *
     * @param id Rich Menu ID
     * @return 影響行數
     */
    int deleteRichMenuById(Long id);

    /**
     * 發布 Rich Menu 到 LINE 平台
     * <p>
     * 1. 呼叫 LINE API 建立 Rich Menu
     * 2. 上傳圖片
     * 3. 儲存 richMenuId
     * 4. 更新狀態為 ACTIVE
     *
     * @param id Rich Menu ID
     * @return LINE 平台返回的 richMenuId
     */
    String publishRichMenu(Long id);

    /**
     * 上傳 Rich Menu 圖片到 LINE 平台
     *
     * @param id         Rich Menu ID
     * @param imageBytes 圖片二進位資料
     * @return 是否成功
     */
    boolean uploadRichMenuImage(Long id, byte[] imageBytes);

    /**
     * 設定為預設選單
     * <p>
     * 1. 取消該頻道的其他預設選單標記
     * 2. 設定當前選單為預設
     * 3. 呼叫 LINE API 設定預設選單
     *
     * @param id Rich Menu ID
     * @return 是否成功
     */
    boolean setDefaultRichMenu(Long id);

    /**
     * 從 LINE 平台刪除 Rich Menu
     *
     * @param id Rich Menu ID
     * @return 是否成功
     */
    boolean deleteRichMenuFromLine(Long id);

    /**
     * 綁定 Rich Menu 到指定使用者
     *
     * @param userId     LINE 使用者 ID
     * @param richMenuId LINE Rich Menu ID
     * @return 是否成功
     */
    boolean linkRichMenuToUser(String userId, String richMenuId);

    /**
     * 解除使用者的 Rich Menu 綁定
     *
     * @param userId LINE 使用者 ID
     * @return 是否成功
     */
    boolean unlinkRichMenuFromUser(String userId);

    /**
     * 檢查選單名稱是否唯一
     *
     * @param richMenu Rich Menu 物件
     * @return 是否唯一
     */
    boolean checkNameUnique(SysLineRichMenu richMenu);
}
