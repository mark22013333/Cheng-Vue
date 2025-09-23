package com.cheng.system.mapper;

import com.cheng.common.core.domain.entity.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部門管理 數據層
 *
 * @author cheng
 */
public interface SysDeptMapper {
    /**
     * 查詢部門管理數據
     *
     * @param dept 部門訊息
     * @return 部門訊息集合
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 根據角色ID查詢部門樹訊息
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部門樹選擇項是否關聯顯示
     * @return 選中部門列表
     */
    List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

    /**
     * 根據部門ID查詢訊息
     *
     * @param deptId 部門ID
     * @return 部門訊息
     */
    SysDept selectDeptById(Long deptId);

    /**
     * 根據ID查詢所有子部門
     *
     * @param deptId 部門ID
     * @return 部門列表
     */
    List<SysDept> selectChildrenDeptById(Long deptId);

    /**
     * 根據ID查詢所有子部門（正常狀態）
     *
     * @param deptId 部門ID
     * @return 子部門數
     */
    int selectNormalChildrenDeptById(Long deptId);

    /**
     * 是否存在子節點
     *
     * @param deptId 部門ID
     * @return 結果
     */
    int hasChildByDeptId(Long deptId);

    /**
     * 查詢部門是否存在使用者
     *
     * @param deptId 部門ID
     * @return 結果
     */
    int checkDeptExistUser(Long deptId);

    /**
     * 校驗部門名稱是否唯一
     *
     * @param deptName 部門名稱
     * @param parentId 父部門ID
     * @return 結果
     */
    SysDept checkDeptNameUnique(@Param("deptName") String deptName, @Param("parentId") Long parentId);

    /**
     * 新增部門訊息
     *
     * @param dept 部門訊息
     * @return 結果
     */
    int insertDept(SysDept dept);

    /**
     * 修改部門訊息
     *
     * @param dept 部門訊息
     * @return 結果
     */
    int updateDept(SysDept dept);

    /**
     * 修改所在部門正常狀態
     *
     * @param deptIds 部門ID組
     */
    void updateDeptStatusNormal(Long[] deptIds);

    /**
     * 修改子元素關系
     *
     * @param depts 子元素
     * @return 結果
     */
    int updateDeptChildren(@Param("depts") List<SysDept> depts);

    /**
     * 刪除部門管理訊息
     *
     * @param deptId 部門ID
     * @return 結果
     */
    int deleteDeptById(Long deptId);
}
