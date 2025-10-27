package com.cheng.line.mapper;

import com.cheng.line.domain.LineMessageLog;

import java.util.List;

/**
 * LINE 推播訊息記錄 Mapper 介面
 *
 * @author cheng
 */
public interface LineMessageLogMapper {

    /**
     * 查詢 LINE 推播訊息記錄
     *
     * @param messageId 訊息ID
     * @return LINE 推播訊息記錄
     */
    LineMessageLog selectLineMessageLogById(Long messageId);

    /**
     * 查詢 LINE 推播訊息記錄列表
     *
     * @param lineMessageLog LINE 推播訊息記錄
     * @return LINE 推播訊息記錄集合
     */
    List<LineMessageLog> selectLineMessageLogList(LineMessageLog lineMessageLog);

    /**
     * 查詢指定使用者的訊息記錄
     *
     * @param lineUserId LINE 使用者ID
     * @return LINE 推播訊息記錄集合
     */
    List<LineMessageLog> selectMessageLogByUserId(String lineUserId);

    /**
     * 查詢指定標籤的訊息記錄
     *
     * @param tagId 標籤ID
     * @return LINE 推播訊息記錄集合
     */
    List<LineMessageLog> selectMessageLogByTagId(Long tagId);

    /**
     * 新增 LINE 推播訊息記錄
     *
     * @param lineMessageLog LINE 推播訊息記錄
     * @return 結果
     */
    int insertLineMessageLog(LineMessageLog lineMessageLog);

    /**
     * 修改 LINE 推播訊息記錄
     *
     * @param lineMessageLog LINE 推播訊息記錄
     * @return 結果
     */
    int updateLineMessageLog(LineMessageLog lineMessageLog);

    /**
     * 刪除 LINE 推播訊息記錄
     *
     * @param messageId 訊息ID
     * @return 結果
     */
    int deleteLineMessageLogById(Long messageId);

    /**
     * 批次刪除 LINE 推播訊息記錄
     *
     * @param messageIds 需要刪除的資料ID
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
