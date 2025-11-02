package com.cheng.line.service;

import com.cheng.line.domain.LineConversationLog;

import java.util.List;

/**
 * LINE 對話記錄 服務層
 *
 * @author cheng
 */
public interface ILineConversationLogService {

    /**
     * 查詢 LINE 對話記錄
     *
     * @param id 主鍵ID
     * @return LINE 對話記錄
     */
    LineConversationLog selectLineConversationLogById(Long id);

    /**
     * 查詢 LINE 對話記錄列表
     *
     * @param lineConversationLog LINE 對話記錄
     * @return LINE 對話記錄集合
     */
    List<LineConversationLog> selectLineConversationLogList(LineConversationLog lineConversationLog);

    /**
     * 根據 LINE 使用者 ID 查詢最近的對話記錄
     *
     * @param lineUserId LINE 使用者ID
     * @param limit      限制數量
     * @return LINE 對話記錄集合
     */
    List<LineConversationLog> selectRecentConversations(String lineUserId, int limit);

    /**
     * 新增 LINE 對話記錄
     *
     * @param lineConversationLog LINE 對話記錄
     * @return 結果
     */
    int insertLineConversationLog(LineConversationLog lineConversationLog);

    /**
     * 批次新增 LINE 對話記錄
     *
     * @param logs LINE 對話記錄集合
     * @return 結果
     */
    int batchInsertLineConversationLog(List<LineConversationLog> logs);

    /**
     * 刪除 LINE 對話記錄
     *
     * @param id 主鍵ID
     * @return 結果
     */
    int deleteLineConversationLogById(Long id);

    /**
     * 批次刪除 LINE 對話記錄
     *
     * @param ids 需要刪除的主鍵ID
     * @return 結果
     */
    int deleteLineConversationLogByIds(Long[] ids);

    /**
     * 根據 LINE 使用者 ID 刪除對話記錄
     *
     * @param lineUserId LINE 使用者ID
     * @return 結果
     */
    int deleteLineConversationLogByLineUserId(String lineUserId);

    /**
     * 刪除指定天數之前的對話記錄
     *
     * @param days 天數
     * @return 結果
     */
    int deleteLineConversationLogBeforeDays(int days);
}
