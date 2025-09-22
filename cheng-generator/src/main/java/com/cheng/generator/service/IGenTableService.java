package com.cheng.generator.service;

import com.cheng.generator.domain.GenTable;

import java.util.List;
import java.util.Map;

/**
 * 業務 服務層
 *
 * @author cheng
 */
public interface IGenTableService {
    /**
     * 查詢業務列表
     *
     * @param genTable 業務訊息
     * @return 業務集合
     */
    List<GenTable> selectGenTableList(GenTable genTable);

    /**
     * 查詢據庫列表
     *
     * @param genTable 業務訊息
     * @return 資料庫表集合
     */
    List<GenTable> selectDbTableList(GenTable genTable);

    /**
     * 查詢據庫列表
     *
     * @param tableNames 表名稱組
     * @return 資料庫表集合
     */
    List<GenTable> selectDbTableListByNames(String[] tableNames);

    /**
     * 查詢所有表訊息
     *
     * @return 表訊息集合
     */
    List<GenTable> selectGenTableAll();

    /**
     * 查詢業務訊息
     *
     * @param id 業務ID
     * @return 業務訊息
     */
    GenTable selectGenTableById(Long id);

    /**
     * 修改業務
     *
     * @param genTable 業務訊息
     * @return 結果
     */
    void updateGenTable(GenTable genTable);

    /**
     * 刪除業務訊息
     *
     * @param tableIds 需要刪除的表數據ID
     * @return 結果
     */
    void deleteGenTableByIds(Long[] tableIds);

    /**
     * 建立表
     *
     * @param sql 建立表語句
     * @return 結果
     */
    boolean createTable(String sql);

    /**
     * 匯入表結構
     *
     * @param tableList 匯入表列表
     * @param operName  操作人員
     */
    void importGenTable(List<GenTable> tableList, String operName);

    /**
     * 預覽程式碼
     *
     * @param tableId 表編號
     * @return 預覽數據列表
     */
    Map<String, String> previewCode(Long tableId);

    /**
     * 產生程式碼（下載方式）
     *
     * @param tableName 表名稱
     * @return 數據
     */
    byte[] downloadCode(String tableName);

    /**
     * 產生程式碼（自定義路徑）
     *
     * @param tableName 表名稱
     * @return 數據
     */
    void generatorCode(String tableName);

    /**
     * 同步資料庫
     *
     * @param tableName 表名稱
     */
    void synchDb(String tableName);

    /**
     * 批次產生程式碼（下載方式）
     *
     * @param tableNames 表陣列
     * @return 數據
     */
    byte[] downloadCode(String[] tableNames);

    /**
     * 修改儲存參數校驗
     *
     * @param genTable 業務訊息
     */
    void validateEdit(GenTable genTable);
}
