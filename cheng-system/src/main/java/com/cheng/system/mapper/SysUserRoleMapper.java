package com.cheng.system.mapper;

import com.cheng.system.domain.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 使用者與角色關聯表 數據層
 *
 * @author cheng
 */
public interface SysUserRoleMapper {
    /**
     * 通過使用者ID刪除使用者和角色關聯
     *
     * @param userId 使用者ID
     * @return 結果
     */
    int deleteUserRoleByUserId(Long userId);

    /**
     * 批次刪除使用者和角色關聯
     *
     * @param ids 需要刪除的數據ID
     * @return 結果
     */
    int deleteUserRole(Long[] ids);

    /**
     * 通過角色ID查詢角色使用數量
     *
     * @param roleId 角色ID
     * @return 結果
     */
    int countUserRoleByRoleId(Long roleId);

    /**
     * 批次新增使用者角色訊息
     *
     * @param userRoleList 使用者角色列表
     * @return 結果
     */
    int batchUserRole(List<SysUserRole> userRoleList);

    /**
     * 刪除使用者和角色關聯訊息
     *
     * @param userRole 使用者和角色關聯訊息
     * @return 結果
     */
    int deleteUserRoleInfo(SysUserRole userRole);

    /**
     * 批次取消授權使用者角色
     *
     * @param roleId  角色ID
     * @param userIds 需要刪除的使用者數據ID
     * @return 結果
     */
    int deleteUserRoleInfos(@Param("roleId") Long roleId, @Param("userIds") Long[] userIds);
}
