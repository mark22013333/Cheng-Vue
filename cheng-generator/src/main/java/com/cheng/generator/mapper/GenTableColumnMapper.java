package com.cheng.generator.mapper;

import com.cheng.generator.domain.GenTableColumn;

import java.util.List;

/**
 * 業務欄位 數據層
 *
 * @author cheng
 */
public interface GenTableColumnMapper
{
    /**
     * 根據表名稱查詢列訊息
     *
     * @param tableName 表名稱
     * @return 列訊息
     */
    public List<GenTableColumn> selectDbTableColumnsByName(String tableName);

    /**
     * 查詢業務欄位列表
     *
     * @param tableId 業務欄位編號
     * @return 業務欄位集合
     */
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);

    /**
     * 新增業務欄位
     *
     * @param genTableColumn 業務欄位訊息
     * @return 結果
     */
    public int insertGenTableColumn(GenTableColumn genTableColumn);

    /**
     * 修改業務欄位
     *
     * @param genTableColumn 業務欄位訊息
     * @return 結果
     */
    public int updateGenTableColumn(GenTableColumn genTableColumn);

    /**
     * 刪除業務欄位
     *
     * @param genTableColumns 列數據
     * @return 結果
     */
    public int deleteGenTableColumns(List<GenTableColumn> genTableColumns);

    /**
     * 批次刪除業務欄位
     *
     * @param ids 需要刪除的數據ID
     * @return 結果
     */
    public int deleteGenTableColumnByIds(Long[] ids);
}
