package com.cheng.system.service.impl;

import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.domain.entity.SysDictData;
import com.cheng.common.core.domain.entity.SysDictType;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.DictUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.system.mapper.SysDictDataMapper;
import com.cheng.system.mapper.SysDictTypeMapper;
import com.cheng.system.service.ISysDictTypeService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典 業務層處理
 *
 * @author cheng
 */
@Service
public class SysDictTypeServiceImpl implements ISysDictTypeService
{
    @Autowired
    private SysDictTypeMapper dictTypeMapper;

    @Autowired
    private SysDictDataMapper dictDataMapper;

    /**
     * 專案啟動時，初始化字典到暫存
     */
    @PostConstruct
    public void init()
    {
        loadingDictCache();
    }

    /**
     * 根據條件分頁查詢字典類型
     *
     * @param dictType 字典類型訊息
     * @return 字典類型集合訊息
     */
    @Override
    public List<SysDictType> selectDictTypeList(SysDictType dictType)
    {
        return dictTypeMapper.selectDictTypeList(dictType);
    }

    /**
     * 根據所有字典類型
     *
     * @return 字典類型集合訊息
     */
    @Override
    public List<SysDictType> selectDictTypeAll()
    {
        return dictTypeMapper.selectDictTypeAll();
    }

    /**
     * 根據字典類型查詢字典數據
     *
     * @param dictType 字典類型
     * @return 字典數據集合訊息
     */
    @Override
    public List<SysDictData> selectDictDataByType(String dictType)
    {
        List<SysDictData> dictDatas = DictUtils.getDictCache(dictType);
        if (StringUtils.isNotEmpty(dictDatas))
        {
            return dictDatas;
        }
        dictDatas = dictDataMapper.selectDictDataByType(dictType);
        if (StringUtils.isNotEmpty(dictDatas))
        {
            DictUtils.setDictCache(dictType, dictDatas);
            return dictDatas;
        }
        return null;
    }

    /**
     * 根據字典類型ID查詢訊息
     *
     * @param dictId 字典類型ID
     * @return 字典類型
     */
    @Override
    public SysDictType selectDictTypeById(Long dictId)
    {
        return dictTypeMapper.selectDictTypeById(dictId);
    }

    /**
     * 根據字典類型查詢訊息
     *
     * @param dictType 字典類型
     * @return 字典類型
     */
    @Override
    public SysDictType selectDictTypeByType(String dictType)
    {
        return dictTypeMapper.selectDictTypeByType(dictType);
    }

    /**
     * 批量刪除字典類型訊息
     *
     * @param dictIds 需要刪除的字典ID
     */
    @Override
    public void deleteDictTypeByIds(Long[] dictIds)
    {
        for (Long dictId : dictIds)
        {
            SysDictType dictType = selectDictTypeById(dictId);
            if (dictDataMapper.countDictDataByType(dictType.getDictType()) > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能刪除", dictType.getDictName()));
            }
            dictTypeMapper.deleteDictTypeById(dictId);
            DictUtils.removeDictCache(dictType.getDictType());
        }
    }

    /**
     * 載入字典暫存數據
     */
    @Override
    public void loadingDictCache()
    {
        SysDictData dictData = new SysDictData();
        dictData.setStatus("0");
        Map<String, List<SysDictData>> dictDataMap = dictDataMapper.selectDictDataList(dictData).stream().collect(Collectors.groupingBy(SysDictData::getDictType));
        for (Map.Entry<String, List<SysDictData>> entry : dictDataMap.entrySet())
        {
            DictUtils.setDictCache(entry.getKey(), entry.getValue().stream().sorted(Comparator.comparing(SysDictData::getDictSort)).collect(Collectors.toList()));
        }
    }

    /**
     * 清除字典暫存數據
     */
    @Override
    public void clearDictCache()
    {
        DictUtils.clearDictCache();
    }

    /**
     * 重置字典暫存數據
     */
    @Override
    public void resetDictCache()
    {
        clearDictCache();
        loadingDictCache();
    }

    /**
     * 新增儲存字典類型訊息
     *
     * @param dict 字典類型訊息
     * @return 結果
     */
    @Override
    public int insertDictType(SysDictType dict)
    {
        int row = dictTypeMapper.insertDictType(dict);
        if (row > 0)
        {
            DictUtils.setDictCache(dict.getDictType(), null);
        }
        return row;
    }

    /**
     * 修改儲存字典類型訊息
     *
     * @param dict 字典類型訊息
     * @return 結果
     */
    @Override
    @Transactional
    public int updateDictType(SysDictType dict)
    {
        SysDictType oldDict = dictTypeMapper.selectDictTypeById(dict.getDictId());
        dictDataMapper.updateDictDataType(oldDict.getDictType(), dict.getDictType());
        int row = dictTypeMapper.updateDictType(dict);
        if (row > 0)
        {
            List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(dict.getDictType());
            DictUtils.setDictCache(dict.getDictType(), dictDatas);
        }
        return row;
    }

    /**
     * 校驗字典類型稱是否唯一
     *
     * @param dict 字典類型
     * @return 結果
     */
    @Override
    public boolean checkDictTypeUnique(SysDictType dict)
    {
        Long dictId = StringUtils.isNull(dict.getDictId()) ? -1L : dict.getDictId();
        SysDictType dictType = dictTypeMapper.checkDictTypeUnique(dict.getDictType());
        if (StringUtils.isNotNull(dictType) && dictType.getDictId().longValue() != dictId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
