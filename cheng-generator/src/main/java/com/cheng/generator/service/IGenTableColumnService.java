package com.cheng.generator.service;

import com.cheng.generator.domain.GenTableColumn;

import java.util.List;

/**
 * 業務欄位 服務層
 *
 * @author cheng
 */
public interface IGenTableColumnService {
    /**
     * 查詢業務欄位列表
     *
     * @param tableId 業務欄位編號
     * @return 業務欄位集合
     */
    List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);

    /**
     * 新增業務欄位
     *
     * @param genTableColumn 業務欄位訊息
     * @return 結果
     */
    int insertGenTableColumn(GenTableColumn genTableColumn);

    /**
     * 修改業務欄位
     *
     * @param genTableColumn 業務欄位訊息
     * @return 結果
     */
    int updateGenTableColumn(GenTableColumn genTableColumn);

    /**
     * 刪除業務欄位訊息
     *
     * @param ids 需要刪除的數據ID
     * @return 結果
     */
    int deleteGenTableColumnByIds(String ids);
}
