package com.cheng.system.service;

import com.cheng.common.core.domain.entity.SysDictData;
import com.cheng.common.core.domain.entity.SysDictType;

import java.util.List;

/**
 * 字典 業務層
 *
 * @author cheng
 */
public interface ISysDictTypeService
{
    /**
     * 根據條件分頁查詢字典類型
     *
     * @param dictType 字典類型訊息
     * @return 字典類型集合訊息
     */
    public List<SysDictType> selectDictTypeList(SysDictType dictType);

    /**
     * 根據所有字典類型
     *
     * @return 字典類型集合訊息
     */
    public List<SysDictType> selectDictTypeAll();

    /**
     * 根據字典類型查詢字典數據
     *
     * @param dictType 字典類型
     * @return 字典數據集合訊息
     */
    public List<SysDictData> selectDictDataByType(String dictType);

    /**
     * 根據字典類型ID查詢訊息
     *
     * @param dictId 字典類型ID
     * @return 字典類型
     */
    public SysDictType selectDictTypeById(Long dictId);

    /**
     * 根據字典類型查詢訊息
     *
     * @param dictType 字典類型
     * @return 字典類型
     */
    public SysDictType selectDictTypeByType(String dictType);

    /**
     * 批次刪除字典訊息
     *
     * @param dictIds 需要刪除的字典ID
     */
    public void deleteDictTypeByIds(Long[] dictIds);

    /**
     * 載入字典暫存數據
     */
    public void loadingDictCache();

    /**
     * 清除字典暫存數據
     */
    public void clearDictCache();

    /**
     * 重置字典暫存數據
     */
    public void resetDictCache();

    /**
     * 新增儲存字典類型訊息
     *
     * @param dictType 字典類型訊息
     * @return 結果
     */
    public int insertDictType(SysDictType dictType);

    /**
     * 修改儲存字典類型訊息
     *
     * @param dictType 字典類型訊息
     * @return 結果
     */
    public int updateDictType(SysDictType dictType);

    /**
     * 校驗字典類型稱是否唯一
     *
     * @param dictType 字典類型
     * @return 結果
     */
    public boolean checkDictTypeUnique(SysDictType dictType);
}
