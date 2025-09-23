package com.cheng.system.service;

import com.cheng.system.domain.SysOperLog;

import java.util.List;

/**
 * 操作日誌 服務層
 *
 * @author cheng
 */
public interface ISysOperLogService {
    /**
     * 新增操作日誌
     *
     * @param operLog 操作日誌物件
     */
    void insertOperlog(SysOperLog operLog);

    /**
     * 查詢系統操作日誌集合
     *
     * @param operLog 操作日誌物件
     * @return 操作日誌集合
     */
    List<SysOperLog> selectOperLogList(SysOperLog operLog);

    /**
     * 批次刪除系統操作日誌
     *
     * @param operIds 需要刪除的操作日誌ID
     * @return 結果
     */
    int deleteOperLogByIds(Long[] operIds);

    /**
     * 查詢操作日誌詳細
     *
     * @param operId 操作ID
     * @return 操作日誌物件
     */
    SysOperLog selectOperLogById(Long operId);

    /**
     * 清除操作日誌
     */
    void cleanOperLog();
}
