package com.cheng.generator.mapper;

import com.cheng.generator.domain.GenTable;

import java.util.List;

/**
 * 業務 數據層
 *
 * @author cheng
 */
public interface GenTableMapper {
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
     * 查詢表ID業務訊息
     *
     * @param id 業務ID
     * @return 業務訊息
     */
    GenTable selectGenTableById(Long id);

    /**
     * 查詢表名稱業務訊息
     *
     * @param tableName 表名稱
     * @return 業務訊息
     */
    GenTable selectGenTableByName(String tableName);

    /**
     * 新增業務
     *
     * @param genTable 業務訊息
     * @return 結果
     */
    int insertGenTable(GenTable genTable);

    /**
     * 修改業務
     *
     * @param genTable 業務訊息
     * @return 結果
     */
    int updateGenTable(GenTable genTable);

    /**
     * 批次刪除業務
     *
     * @param ids 需要刪除的數據ID
     * @return 結果
     */
    int deleteGenTableByIds(Long[] ids);

    /**
     * 建立表
     *
     * @param sql 表結構
     * @return 結果
     */
    int createTable(String sql);
}
