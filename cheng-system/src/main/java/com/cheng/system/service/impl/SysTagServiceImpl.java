package com.cheng.system.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.uuid.IdUtils;
import com.cheng.system.domain.SysTag;
import com.cheng.system.mapper.InvItemTagRelationMapper;
import com.cheng.system.mapper.SysTagMapper;
import com.cheng.system.service.ISysTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系統標籤 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTagServiceImpl implements ISysTagService {

    private final SysTagMapper sysTagMapper;
    private final InvItemTagRelationMapper invItemTagRelationMapper;

    @Override
    public SysTag selectSysTagByTagId(Long tagId) {
        return sysTagMapper.selectSysTagByTagId(tagId);
    }

    @Override
    public SysTag selectSysTagByTagCode(String tagCode) {
        return sysTagMapper.selectSysTagByTagCode(tagCode);
    }

    @Override
    public List<SysTag> selectSysTagList(SysTag sysTag) {
        return sysTagMapper.selectSysTagList(sysTag);
    }

    @Override
    public List<SysTag> selectLineTagList(Integer status) {
        return sysTagMapper.selectSysTagListByPlatform(SysTag.SCOPE_LINE, status);
    }

    @Override
    public List<SysTag> selectInventoryTagList(Integer status) {
        return sysTagMapper.selectSysTagListByPlatform(SysTag.SCOPE_INVENTORY, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSysTag(SysTag sysTag) {
        // 設定預設平台範圍
        if (StringUtils.isEmpty(sysTag.getPlatformScope())) {
            sysTag.setPlatformScope(SysTag.SCOPE_ALL);
        }
        // 檢查同平台標籤名稱是否重複
        SysTag existingTag = sysTagMapper.selectByNameAndPlatform(sysTag.getTagName(), sysTag.getPlatformScope());
        if (existingTag != null) {
            throw new ServiceException(String.format("標籤名稱「%s」在此平台已存在", sysTag.getTagName()));
        }
        // 自動產生標籤代碼
        if (StringUtils.isEmpty(sysTag.getTagCode())) {
            sysTag.setTagCode(generateTagCode(sysTag.getPlatformScope()));
        }
        // 設定預設值
        if (sysTag.getUserCount() == null) {
            sysTag.setUserCount(0);
        }
        if (sysTag.getItemCount() == null) {
            sysTag.setItemCount(0);
        }
        if (sysTag.getStatus() == null) {
            sysTag.setStatus(1);
        }
        if (sysTag.getSortOrder() == null) {
            sysTag.setSortOrder(0);
        }
        return sysTagMapper.insertSysTag(sysTag);
    }

    /**
     * 產生標籤代碼
     * LINE 平台：line-xxxxxx
     * 庫存平台：inv-xxxxxx
     * 其他：tag-xxxxxx
     *
     * @param platformScope 平台範圍
     * @return 標籤代碼
     */
    private String generateTagCode(String platformScope) {
        String prefix;
        if (SysTag.SCOPE_LINE.equals(platformScope)) {
            prefix = "line-";
        } else if (SysTag.SCOPE_INVENTORY.equals(platformScope)) {
            prefix = "inv-";
        } else {
            prefix = "tag-";
        }

        // 產生 7 位亂碼（小寫字母和數字）
        String randomCode = IdUtils.simpleUUID().substring(0, 7).toLowerCase();
        String tagCode = prefix + randomCode;

        // 確保唯一性
        while (sysTagMapper.checkTagCodeUnique(tagCode) != null) {
            randomCode = IdUtils.simpleUUID().substring(0, 7).toLowerCase();
            tagCode = prefix + randomCode;
        }

        return tagCode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSysTag(SysTag sysTag) {
        return sysTagMapper.updateSysTag(sysTag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSysTagByTagId(Long tagId) {
        // 刪除標籤前，先刪除相關的關聯資料
        invItemTagRelationMapper.deleteByTagId(tagId);
        // 注意：LINE 使用者標籤關聯由 cheng-line 模組負責刪除（需透過事件或直接呼叫）
        return sysTagMapper.deleteSysTagByTagId(tagId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSysTagByTagIds(Long[] tagIds) {
        for (Long tagId : tagIds) {
            invItemTagRelationMapper.deleteByTagId(tagId);
        }
        return sysTagMapper.deleteSysTagByTagIds(tagIds);
    }

    @Override
    public boolean checkTagCodeUnique(SysTag sysTag) {
        Long tagId = sysTag.getTagId() == null ? -1L : sysTag.getTagId();
        SysTag info = sysTagMapper.checkTagCodeUnique(sysTag.getTagCode());
        if (info != null && !info.getTagId().equals(tagId)) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateAllCounts() {
        log.info("開始重算所有標籤的使用者和物品計數");
        sysTagMapper.recalculateAllUserCounts();
        sysTagMapper.recalculateAllItemCounts();
        log.info("完成重算所有標籤的使用者和物品計數");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserCount(Long tagId) {
        // 這裡需要從 LINE 模組取得計數，暫時使用直接查詢
        // 實際應用中應該透過 Mapper 或 Service 取得
        log.debug("更新標籤 {} 的 LINE 使用者計數", tagId);
        sysTagMapper.recalculateAllUserCounts();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItemCount(Long tagId) {
        int count = invItemTagRelationMapper.countItemsByTagId(tagId);
        sysTagMapper.updateItemCount(tagId, count);
        log.debug("更新標籤 {} 的物品計數為 {}", tagId, count);
    }
}
