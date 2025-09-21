package com.cheng.system.mapper;

import com.cheng.system.domain.SysOperLog;

import java.util.List;

/**
 * 操作日誌 數據層
 *
 * @author cheng
 */
public interface SysOperLogMapper
{
    /**
     * 新增操作日誌
     *
     * @param operLog 操作日誌物件
     */
    public void insertOperlog(SysOperLog operLog);

    /**
     * 查詢系統操作日誌集合
     *
     * @param operLog 操作日誌物件
     * @return 操作日誌集合
     */
    public List<SysOperLog> selectOperLogList(SysOperLog operLog);

    /**
     * 批量刪除系統操作日誌
     *
     * @param operIds 需要刪除的操作日誌ID
     * @return 結果
     */
    public int deleteOperLogByIds(Long[] operIds);

    /**
     * 查詢操作日誌詳細
     * 
     * @param operId 操作ID
     * @return 操作日誌物件
     */
    public SysOperLog selectOperLogById(Long operId);

    /**
     * 清除操作日誌
     */
    public void cleanOperLog();
}
