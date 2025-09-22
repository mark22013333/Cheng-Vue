package com.cheng.system.mapper;

import com.cheng.common.core.domain.entity.SysDictData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典表 數據層
 *
 * @author cheng
 */
public interface SysDictDataMapper
{
    /**
     * 根據條件分頁查詢字典數據
     *
     * @param dictData 字典數據訊息
     * @return 字典數據集合訊息
     */
    public List<SysDictData> selectDictDataList(SysDictData dictData);

    /**
     * 根據字典類型查詢字典數據
     *
     * @param dictType 字典類型
     * @return 字典數據集合訊息
     */
    public List<SysDictData> selectDictDataByType(String dictType);

    /**
     * 根據字典類型和字典鍵值查詢字典數據訊息
     *
     * @param dictType 字典類型
     * @param dictValue 字典鍵值
     * @return 字典標籤
     */
    public String selectDictLabel(@Param("dictType") String dictType, @Param("dictValue") String dictValue);

    /**
     * 根據字典數據ID查詢訊息
     *
     * @param dictCode 字典數據ID
     * @return 字典數據
     */
    public SysDictData selectDictDataById(Long dictCode);

    /**
     * 查詢字典數據
     *
     * @param dictType 字典類型
     * @return 字典數據
     */
    public int countDictDataByType(String dictType);

    /**
     * 通過字典ID刪除字典數據訊息
     *
     * @param dictCode 字典數據ID
     * @return 結果
     */
    public int deleteDictDataById(Long dictCode);

    /**
     * 批次刪除字典數據訊息
     *
     * @param dictCodes 需要刪除的字典數據ID
     * @return 結果
     */
    public int deleteDictDataByIds(Long[] dictCodes);

    /**
     * 新增字典數據訊息
     *
     * @param dictData 字典數據訊息
     * @return 結果
     */
    public int insertDictData(SysDictData dictData);

    /**
     * 修改字典數據訊息
     *
     * @param dictData 字典數據訊息
     * @return 結果
     */
    public int updateDictData(SysDictData dictData);

    /**
     * 同步修改字典類型
     *
     * @param oldDictType 舊字典類型
     * @param newDictType 新舊字典類型
     * @return 結果
     */
    public int updateDictDataType(@Param("oldDictType") String oldDictType, @Param("newDictType") String newDictType);
}
