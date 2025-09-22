package com.cheng.system.mapper;

import com.cheng.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 使用者表 數據層
 *
 * @author cheng
 */
public interface SysUserMapper
{
    /**
     * 根據條件分頁查詢使用者列表
     *
     * @param sysUser 使用者訊息
     * @return 使用者訊息集合訊息
     */
    public List<SysUser> selectUserList(SysUser sysUser);

    /**
     * 根據條件分頁查詢已配使用者角色列表
     *
     * @param user 使用者訊息
     * @return 使用者訊息集合訊息
     */
    public List<SysUser> selectAllocatedList(SysUser user);

    /**
     * 根據條件分頁查詢未分配使用者角色列表
     *
     * @param user 使用者訊息
     * @return 使用者訊息集合訊息
     */
    public List<SysUser> selectUnallocatedList(SysUser user);

    /**
     * 通過使用者名查詢使用者
     *
     * @param userName 使用者名
     * @return 使用者物件訊息
     */
    public SysUser selectUserByUserName(String userName);

    /**
     * 通過使用者ID查詢使用者
     *
     * @param userId 使用者ID
     * @return 使用者物件訊息
     */
    public SysUser selectUserById(Long userId);

    /**
     * 新增使用者訊息
     *
     * @param user 使用者訊息
     * @return 結果
     */
    public int insertUser(SysUser user);

    /**
     * 修改使用者訊息
     *
     * @param user 使用者訊息
     * @return 結果
     */
    public int updateUser(SysUser user);

    /**
     * 修改使用者頭像
     *
     * @param userId 使用者ID
     * @param avatar 頭像地址
     * @return 結果
     */
    public int updateUserAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

    /**
     * 修改使用者狀態
     *
     * @param userId 使用者ID
     * @param status 狀態
     * @return 結果
     */
    public int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 更新使用者登入訊息（IP和登入時間）
     *
     * @param userId 使用者ID
     * @param loginIp 登入IP位置
     * @param loginDate 登入時間
     * @return 結果
     */
    public int updateLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp, @Param("loginDate") Date loginDate);

    /**
     * 重置使用者密碼
     *
     * @param userId 使用者ID
     * @param password 密碼
     * @return 結果
     */
    public int resetUserPwd(@Param("userId") Long userId, @Param("password") String password);

    /**
     * 通過使用者ID刪除使用者
     *
     * @param userId 使用者ID
     * @return 結果
     */
    public int deleteUserById(Long userId);

    /**
     * 批次刪除使用者訊息
     *
     * @param userIds 需要刪除的使用者ID
     * @return 結果
     */
    public int deleteUserByIds(Long[] userIds);

    /**
     * 校驗使用者名稱是否唯一
     *
     * @param userName 使用者名稱
     * @return 結果
     */
    public SysUser checkUserNameUnique(String userName);

    /**
     * 校驗手機號碼是否唯一
     *
     * @param phonenumber 手機號碼
     * @return 結果
     */
    public SysUser checkPhoneUnique(String phonenumber);

    /**
     * 校驗email是否唯一
     *
     * @param email 使用者信箱
     * @return 結果
     */
    public SysUser checkEmailUnique(String email);
}
