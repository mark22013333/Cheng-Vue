package com.cheng.system.service;

import com.cheng.common.core.domain.entity.SysDictData;

import java.util.List;

/**
 * 字典 業務層
 *
 * @author cheng
 */
public interface ISysDictDataService
{
    /**
     * 根據條件分頁查詢字典數據
     *
     * @param dictData 字典數據訊息
     * @return 字典數據集合訊息
     */
    public List<SysDictData> selectDictDataList(SysDictData dictData);

    /**
     * 根據字典類型和字典鍵值查詢字典數據訊息
     *
     * @param dictType 字典類型
     * @param dictValue 字典鍵值
     * @return 字典標籤
     */
    public String selectDictLabel(String dictType, String dictValue);

    /**
     * 根據字典數據ID查詢訊息
     *
     * @param dictCode 字典數據ID
     * @return 字典數據
     */
    public SysDictData selectDictDataById(Long dictCode);

    /**
     * 批次刪除字典數據訊息
     *
     * @param dictCodes 需要刪除的字典數據ID
     */
    public void deleteDictDataByIds(Long[] dictCodes);

    /**
     * 新增儲存字典數據訊息
     *
     * @param dictData 字典數據訊息
     * @return 結果
     */
    public int insertDictData(SysDictData dictData);

    /**
     * 修改儲存字典數據訊息
     *
     * @param dictData 字典數據訊息
     * @return 結果
     */
    public int updateDictData(SysDictData dictData);
}
