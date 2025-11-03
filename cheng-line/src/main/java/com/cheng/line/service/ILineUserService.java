package com.cheng.line.service;

import com.cheng.line.domain.LineUser;
import com.cheng.line.dto.LineUserImportResultDTO;
import com.cheng.line.dto.LineUserStatsDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * LINE 使用者 服務層
 *
 * @author cheng
 */
public interface ILineUserService {

    /**
     * 查詢 LINE 使用者
     *
     * @param id 主鍵ID
     * @return LINE 使用者
     */
    LineUser selectLineUserById(Long id);

    /**
     * 根據 LINE 使用者 ID 查詢
     *
     * @param lineUserId LINE 使用者 ID
     * @return LINE 使用者
     */
    LineUser selectLineUserByLineUserId(String lineUserId);

    /**
     * 查詢 LINE 使用者列表
     *
     * @param lineUser LINE 使用者
     * @return LINE 使用者集合
     */
    List<LineUser> selectLineUserList(LineUser lineUser);

    /**
     * 查詢所有已關注的使用者
     *
     * @return LINE 使用者集合
     */
    List<LineUser> selectFollowingUsers();

    /**
     * 新增 LINE 使用者
     *
     * @param lineUser LINE 使用者
     * @return 結果
     */
    int insertLineUser(LineUser lineUser);

    /**
     * 修改 LINE 使用者
     *
     * @param lineUser LINE 使用者
     * @return 結果
     */
    int updateLineUser(LineUser lineUser);

    /**
     * 批次刪除 LINE 使用者
     *
     * @param ids 需要刪除的主鍵ID
     * @return 結果
     */
    int deleteLineUserByIds(Long[] ids);

    /**
     * 刪除 LINE 使用者
     *
     * @param id 主鍵ID
     * @return 結果
     */
    int deleteLineUserById(Long id);

    /**
     * 處理使用者關注事件
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    int handleFollowEvent(String lineUserId);

    /**
     * 處理使用者取消關注事件
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    int handleUnfollowEvent(String lineUserId);

    /**
     * 處理使用者封鎖事件
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    int handleBlockEvent(String lineUserId);

    /**
     * 綁定系統使用者
     *
     * @param lineUserId LINE 使用者 ID
     * @param sysUserId  系統使用者 ID
     * @return 結果
     */
    int bindSysUser(String lineUserId, Long sysUserId);

    /**
     * 解除綁定系統使用者
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    int unbindSysUser(String lineUserId);

    /**
     * 更新使用者個人資料（從 LINE API 取得）
     *
     * @param lineUserId LINE 使用者 ID
     * @param configId   頻道設定ID
     * @return 結果
     */
    int updateUserProfile(String lineUserId, Integer configId);

    /**
     * 增加訊息統計計數
     *
     * @param lineUserId LINE 使用者 ID
     * @param sent       是否為發送（true）或接收（false）
     * @return 結果
     */
    int incrementMessageCount(String lineUserId, boolean sent);

    /**
     * 取得使用者統計資料
     *
     * @return 統計資料
     */
    LineUserStatsDTO getUserStats();

    /**
     * 匯入 LINE 使用者（從檔案）
     *
     * @param file     上傳的檔案
     * @param configId 頻道設定ID
     * @return 匯入結果
     */
    LineUserImportResultDTO importLineUsers(MultipartFile file, Integer configId);

    /**
     * 將使用者加入黑名單
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    int addToBlacklist(String lineUserId);

    /**
     * 將使用者從黑名單移除
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    int removeFromBlacklist(String lineUserId);

    /**
     * 批次將使用者加入黑名單
     *
     * @param lineUserIds LINE 使用者 ID 陣列
     * @return 成功加入的數量
     */
    int batchAddToBlacklist(String[] lineUserIds);

    /**
     * 批次將使用者從黑名單移除
     *
     * @param lineUserIds LINE 使用者 ID 陣列
     * @return 成功移除的數量
     */
    int batchRemoveFromBlacklist(String[] lineUserIds);
}
