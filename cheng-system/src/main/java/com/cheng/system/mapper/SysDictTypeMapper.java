package com.cheng.system.mapper;

import com.cheng.common.core.domain.entity.SysDictType;

import java.util.List;

/**
 * 字典表 數據層
 *
 * @author cheng
 */
public interface SysDictTypeMapper {
    /**
     * 根據條件分頁查詢字典類型
     *
     * @param dictType 字典類型訊息
     * @return 字典類型集合訊息
     */
    List<SysDictType> selectDictTypeList(SysDictType dictType);

    /**
     * 根據所有字典類型
     *
     * @return 字典類型集合訊息
     */
    List<SysDictType> selectDictTypeAll();

    /**
     * 根據字典類型ID查詢訊息
     *
     * @param dictId 字典類型ID
     * @return 字典類型
     */
    SysDictType selectDictTypeById(Long dictId);

    /**
     * 根據字典類型查詢訊息
     *
     * @param dictType 字典類型
     * @return 字典類型
     */
    SysDictType selectDictTypeByType(String dictType);

    /**
     * 通過字典ID刪除字典訊息
     *
     * @param dictId 字典ID
     * @return 結果
     */
    int deleteDictTypeById(Long dictId);

    /**
     * 批次刪除字典類型訊息
     *
     * @param dictIds 需要刪除的字典ID
     * @return 結果
     */
    int deleteDictTypeByIds(Long[] dictIds);

    /**
     * 新增字典類型訊息
     *
     * @param dictType 字典類型訊息
     * @return 結果
     */
    int insertDictType(SysDictType dictType);

    /**
     * 修改字典類型訊息
     *
     * @param dictType 字典類型訊息
     * @return 結果
     */
    int updateDictType(SysDictType dictType);

    /**
     * 校驗字典類型稱是否唯一
     *
     * @param dictType 字典類型
     * @return 結果
     */
    SysDictType checkDictTypeUnique(String dictType);
}
