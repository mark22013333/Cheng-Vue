package com.cheng.system.mapper;

import com.cheng.common.core.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 選單表 數據層
 *
 * @author cheng
 */
public interface SysMenuMapper {
    /**
     * 查詢系統選單列表
     *
     * @param menu 選單訊息
     * @return 選單列表
     */
    List<SysMenu> selectMenuList(SysMenu menu);

    /**
     * 根據使用者所有權限
     *
     * @return 權限列表
     */
    List<String> selectMenuPerms();

    /**
     * 根據使用者查詢系統選單列表
     *
     * @param menu 選單訊息
     * @return 選單列表
     */
    List<SysMenu> selectMenuListByUserId(SysMenu menu);

    /**
     * 根據角色ID查詢權限
     *
     * @param roleId 角色ID
     * @return 權限列表
     */
    List<String> selectMenuPermsByRoleId(Long roleId);

    /**
     * 根據使用者ID查詢權限
     *
     * @param userId 使用者ID
     * @return 權限列表
     */
    List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根據使用者ID查詢選單
     *
     * @return 選單列表
     */
    List<SysMenu> selectMenuTreeAll();

    /**
     * 根據使用者ID查詢選單
     *
     * @param userId 使用者ID
     * @return 選單列表
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 根據角色ID查詢選單樹訊息
     *
     * @param roleId            角色ID
     * @param menuCheckStrictly 選單樹選擇項是否關聯顯示
     * @return 選中選單列表
     */
    List<Long> selectMenuListByRoleId(@Param("roleId") Long roleId, @Param("menuCheckStrictly") boolean menuCheckStrictly);

    /**
     * 根據選單ID查詢訊息
     *
     * @param menuId 選單ID
     * @return 選單訊息
     */
    SysMenu selectMenuById(Long menuId);

    /**
     * 是否存在選單子節點
     *
     * @param menuId 選單ID
     * @return 結果
     */
    int hasChildByMenuId(Long menuId);

    /**
     * 新增選單訊息
     *
     * @param menu 選單訊息
     * @return 結果
     */
    int insertMenu(SysMenu menu);

    /**
     * 修改選單訊息
     *
     * @param menu 選單訊息
     * @return 結果
     */
    int updateMenu(SysMenu menu);

    /**
     * 刪除選單管理訊息
     *
     * @param menuId 選單ID
     * @return 結果
     */
    int deleteMenuById(Long menuId);

    /**
     * 校驗選單名稱是否唯一
     *
     * @param menuName 選單名稱
     * @param parentId 父選單ID
     * @return 結果
     */
    SysMenu checkMenuNameUnique(@Param("menuName") String menuName, @Param("parentId") Long parentId);
}
