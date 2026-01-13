package com.cheng.system.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.system.domain.SysTagGroup;
import com.cheng.system.domain.SysTagGroupDetail;
import com.cheng.system.mapper.SysTagGroupDetailMapper;
import com.cheng.system.mapper.SysTagGroupMapper;
import com.cheng.system.service.ISysTagGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 標籤群組 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTagGroupServiceImpl implements ISysTagGroupService {

    private final SysTagGroupMapper sysTagGroupMapper;
    private final SysTagGroupDetailMapper sysTagGroupDetailMapper;
    private final TagGroupCalcService tagGroupCalcService;

    @Override
    public List<SysTagGroup> selectSysTagGroupList(SysTagGroup sysTagGroup) {
        return sysTagGroupMapper.selectSysTagGroupList(sysTagGroup);
    }

    @Override
    public SysTagGroup selectSysTagGroupById(Long groupId) {
        SysTagGroup group = sysTagGroupMapper.selectSysTagGroupById(groupId);
        if (group != null) {
            group.setDetails(sysTagGroupDetailMapper.selectByGroupId(groupId));
        }
        return group;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSysTagGroup(SysTagGroup sysTagGroup) {
        // 檢查代碼唯一性
        if (!checkGroupCodeUnique(sysTagGroup)) {
            throw new ServiceException("群組代碼「" + sysTagGroup.getGroupCode() + "」已存在");
        }

        // 檢查明細數量
        validateDetails(sysTagGroup.getDetails());

        // 新增群組主表
        int result = sysTagGroupMapper.insertSysTagGroup(sysTagGroup);

        // 新增明細
        if (sysTagGroup.getDetails() != null && !sysTagGroup.getDetails().isEmpty()) {
            for (SysTagGroupDetail detail : sysTagGroup.getDetails()) {
                detail.setGroupId(sysTagGroup.getGroupId());
                detail.setCreateBy(sysTagGroup.getCreateBy());
            }
            sysTagGroupDetailMapper.batchInsert(sysTagGroup.getDetails());
        }

        log.info("新增標籤群組：{}，明細數：{}", sysTagGroup.getGroupName(),
                sysTagGroup.getDetails() != null ? sysTagGroup.getDetails().size() : 0);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSysTagGroup(SysTagGroup sysTagGroup) {
        // 檢查代碼唯一性
        if (!checkGroupCodeUnique(sysTagGroup)) {
            throw new ServiceException("群組代碼「" + sysTagGroup.getGroupCode() + "」已存在");
        }

        // 檢查明細數量
        validateDetails(sysTagGroup.getDetails());

        // 更新群組主表
        int result = sysTagGroupMapper.updateSysTagGroup(sysTagGroup);

        // 刪除舊明細並新增新明細
        sysTagGroupDetailMapper.deleteByGroupId(sysTagGroup.getGroupId());
        if (sysTagGroup.getDetails() != null && !sysTagGroup.getDetails().isEmpty()) {
            for (SysTagGroupDetail detail : sysTagGroup.getDetails()) {
                detail.setGroupId(sysTagGroup.getGroupId());
                detail.setCreateBy(sysTagGroup.getUpdateBy());
            }
            sysTagGroupDetailMapper.batchInsert(sysTagGroup.getDetails());
        }

        log.info("更新標籤群組：{}，明細數：{}", sysTagGroup.getGroupName(),
                sysTagGroup.getDetails() != null ? sysTagGroup.getDetails().size() : 0);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSysTagGroupById(Long groupId) {
        sysTagGroupDetailMapper.deleteByGroupId(groupId);
        return sysTagGroupMapper.deleteSysTagGroupById(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSysTagGroupByIds(Long[] groupIds) {
        sysTagGroupDetailMapper.deleteByGroupIds(groupIds);
        return sysTagGroupMapper.deleteSysTagGroupByIds(groupIds);
    }

    @Override
    public boolean checkGroupCodeUnique(SysTagGroup sysTagGroup) {
        Long groupId = sysTagGroup.getGroupId() == null ? -1L : sysTagGroup.getGroupId();
        int count = sysTagGroupMapper.checkGroupCodeUnique(sysTagGroup.getGroupCode(), groupId);
        return count == 0;
    }

    @Override
    public Map<String, Object> executeLineGroupCalc(Long groupId) {
        SysTagGroup group = selectSysTagGroupById(groupId);
        if (group == null) {
            throw new ServiceException("群組不存在");
        }
        if (!SysTagGroup.SCOPE_LINE.equals(group.getPlatformScope())) {
            throw new ServiceException("此群組不適用於 LINE 平台");
        }
        return tagGroupCalcService.executeLineGroupCalc(group);
    }

    @Override
    public Map<String, Object> executeInventoryGroupCalc(Long groupId) {
        SysTagGroup group = selectSysTagGroupById(groupId);
        if (group == null) {
            throw new ServiceException("群組不存在");
        }
        if (!SysTagGroup.SCOPE_INVENTORY.equals(group.getPlatformScope())) {
            throw new ServiceException("此群組不適用於庫存平台");
        }
        return tagGroupCalcService.executeInventoryGroupCalc(group);
    }

    @Override
    public Map<String, Object> previewGroupCalc(Long groupId, int limit) {
        SysTagGroup group = selectSysTagGroupById(groupId);
        if (group == null) {
            throw new ServiceException("群組不存在");
        }
        return tagGroupCalcService.previewGroupCalc(group, limit);
    }

    /**
     * 驗證明細數量（最多 5 個）
     */
    private void validateDetails(List<SysTagGroupDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new ServiceException("至少需要設定一個標籤");
        }
        if (details.size() > 5) {
            throw new ServiceException("最多只能設定 5 個標籤");
        }

        // 驗證 groupIndex 連續性
        Set<Integer> indexes = details.stream()
                .map(SysTagGroupDetail::getGroupIndex)
                .collect(Collectors.toSet());
        for (int i = 1; i <= details.size(); i++) {
            if (!indexes.contains(i)) {
                throw new ServiceException("標籤順序必須連續，缺少第 " + i + " 個");
            }
        }

        // 驗證第一個標籤不應有運算子
        details.stream()
                .filter(d -> d.getGroupIndex() == 1 && d.getOperator() != null && !d.getOperator().isEmpty())
                .findFirst()
                .ifPresent(d -> {
                    throw new ServiceException("第一個標籤不應設定運算子");
                });

        // 驗證其他標籤必須有運算子
        details.stream()
                .filter(d -> d.getGroupIndex() > 1 && (d.getOperator() == null || d.getOperator().isEmpty()))
                .findFirst()
                .ifPresent(d -> {
                    throw new ServiceException("第 " + d.getGroupIndex() + " 個標籤必須設定運算子（AND/OR）");
                });
    }
}
