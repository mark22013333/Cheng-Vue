package com.cheng.line.service;

import com.cheng.line.domain.LineMessageLog;

import java.util.List;

/**
 * LINE 推播訊息記錄 服務層
 *
 * @author cheng
 */
public interface ILineMessageLogService {

    /**
     * 查詢推播訊息記錄
     *
     * @param messageId 訊息ID
     * @return 推播訊息記錄
     */
    LineMessageLog selectLineMessageLogById(Long messageId);

    /**
     * 查詢推播訊息記錄列表
     *
     * @param lineMessageLog 查詢條件
     * @return 推播訊息記錄集合
     */
    List<LineMessageLog> selectLineMessageLogList(LineMessageLog lineMessageLog);

    /**
     * 查詢指定使用者的訊息記錄
     *
     * @param lineUserId LINE 使用者ID
     * @return 推播訊息記錄集合
     */
    List<LineMessageLog> selectMessageLogByUserId(String lineUserId);

    /**
     * 查詢指定標籤的訊息記錄
     *
     * @param tagId 標籤ID
     * @return 推播訊息記錄集合
     */
    List<LineMessageLog> selectMessageLogByTagId(Long tagId);

    /**
     * 新增推播訊息記錄
     *
     * @param lineMessageLog 推播訊息記錄
     * @return 結果
     */
    int insertLineMessageLog(LineMessageLog lineMessageLog);

    /**
     * 修改推播訊息記錄
     *
     * @param lineMessageLog 推播訊息記錄
     * @return 結果
     */
    int updateLineMessageLog(LineMessageLog lineMessageLog);

    /**
     * 刪除推播訊息記錄
     *
     * @param messageId 訊息ID
     * @return 結果
     */
    int deleteLineMessageLogById(Long messageId);

    /**
     * 批次刪除推播訊息記錄
     *
     * @param messageIds 訊息ID陣列
     * @return 結果
     */
    int deleteLineMessageLogByIds(Long[] messageIds);

    /**
     * 統計發送成功的訊息數（依頻道）
     *
     * @param configId 頻道設定ID
     * @return 成功數量
     */
    int countSuccessMessagesByConfigId(Integer configId);

    /**
     * 統計發送失敗的訊息數（依頻道）
     *
     * @param configId 頻道設定ID
     * @return 失敗數量
     */
    int countFailedMessagesByConfigId(Integer configId);
}
