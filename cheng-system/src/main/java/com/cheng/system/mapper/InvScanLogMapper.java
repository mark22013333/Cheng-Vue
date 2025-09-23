package com.cheng.system.mapper;

import java.util.List;
import com.cheng.system.domain.InvScanLog;

/**
 * 掃描記錄Mapper介面
 * 
 * @author cheng
 * @since 2024-01-01
 */
public interface InvScanLogMapper 
{
    /**
     * 查詢掃描記錄
     * 
     * @param scanId 掃描記錄主鍵
     * @return 掃描記錄
     */
    public InvScanLog selectInvScanLogByScanId(Long scanId);

    /**
     * 查詢掃描記錄列表
     * 
     * @param invScanLog 掃描記錄
     * @return 掃描記錄集合
     */
    public List<InvScanLog> selectInvScanLogList(InvScanLog invScanLog);

    /**
     * 新增掃描記錄
     * 
     * @param invScanLog 掃描記錄
     * @return 結果
     */
    public int insertInvScanLog(InvScanLog invScanLog);

    /**
     * 修改掃描記錄
     * 
     * @param invScanLog 掃描記錄
     * @return 結果
     */
    public int updateInvScanLog(InvScanLog invScanLog);

    /**
     * 刪除掃描記錄
     * 
     * @param scanId 掃描記錄主鍵
     * @return 結果
     */
    public int deleteInvScanLogByScanId(Long scanId);

    /**
     * 批量刪除掃描記錄
     * 
     * @param scanIds 需要刪除的資料主鍵集合
     * @return 結果
     */
    public int deleteInvScanLogByScanIds(Long[] scanIds);

    /**
     * 根據物品ID查詢掃描記錄
     * 
     * @param itemId 物品ID
     * @return 掃描記錄集合
     */
    public List<InvScanLog> selectInvScanLogByItemId(Long itemId);

    /**
     * 根據掃描人員查詢掃描記錄
     * 
     * @param scannerId 掃描人員ID
     * @return 掃描記錄集合
     */
    public List<InvScanLog> selectInvScanLogByScannerId(Long scannerId);

    /**
     * 根據掃描結果查詢掃描記錄
     * 
     * @param scanResult 掃描結果
     * @return 掃描記錄集合
     */
    public List<InvScanLog> selectInvScanLogByScanResult(String scanResult);

    /**
     * 查詢掃描統計資訊
     * 
     * @return 統計資訊
     */
    public List<InvScanLog> selectScanStatistics();

    /**
     * 清除歷史掃描記錄
     * 
     * @param days 保留天數
     * @return 結果
     */
    public int clearHistoryScanLog(int days);
}
