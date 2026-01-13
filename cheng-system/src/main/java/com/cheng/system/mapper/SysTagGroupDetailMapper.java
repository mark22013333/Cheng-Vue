package com.cheng.system.mapper;

import com.cheng.system.domain.SysTagGroupDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 標籤群組明細 Mapper 介面
 *
 * @author cheng
 */
public interface SysTagGroupDetailMapper {

    /**
     * 根據群組ID查詢明細列表
     *
     * @param groupId 群組ID
     * @return 明細集合
     */
    List<SysTagGroupDetail> selectByGroupId(Long groupId);

    /**
     * 批次新增群組明細
     *
     * @param details 明細列表
     * @return 影響行數
     */
    int batchInsert(@Param("details") List<SysTagGroupDetail> details);

    /**
     * 刪除群組的所有明細
     *
     * @param groupId 群組ID
     * @return 影響行數
     */
    int deleteByGroupId(Long groupId);

    /**
     * 批次刪除群組的所有明細
     *
     * @param groupIds 群組ID陣列
     * @return 影響行數
     */
    int deleteByGroupIds(Long[] groupIds);

    /**
     * 查詢所有群組的明細（用於批次運算）
     *
     * @param platformScope 平台範圍
     * @return 明細集合
     */
    List<SysTagGroupDetail> selectAllByPlatform(@Param("platformScope") String platformScope);
}
