package com.cheng.system.service.impl;

import com.cheng.common.annotation.DataScope;
import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.domain.entity.SysRole;
import com.cheng.common.core.domain.entity.SysUser;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.bean.BeanValidators;
import com.cheng.common.utils.spring.SpringUtils;
import com.cheng.system.domain.SysPost;
import com.cheng.system.domain.SysUserPost;
import com.cheng.system.domain.SysUserRole;
import com.cheng.system.mapper.*;
import com.cheng.system.service.ISysConfigService;
import com.cheng.system.service.ISysDeptService;
import com.cheng.system.service.ISysUserService;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 使用者 業務層處理
 *
 * @author cheng
 */
@Service
public class SysUserServiceImpl implements ISysUserService {
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysUserPostMapper userPostMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    protected Validator validator;

    /**
     * 根據條件分頁查詢使用者列表
     *
     * @param user 使用者訊息
     * @return 使用者訊息集合訊息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user) {
        return userMapper.selectUserList(user);
    }

    /**
     * 根據條件分頁查詢已分配使用者角色列表
     *
     * @param user 使用者訊息
     * @return 使用者訊息集合訊息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectAllocatedList(SysUser user) {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * 根據條件分頁查詢未分配使用者角色列表
     *
     * @param user 使用者訊息
     * @return 使用者訊息集合訊息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUnallocatedList(SysUser user) {
        return userMapper.selectUnallocatedList(user);
    }

    /**
     * 通過使用者名查詢使用者
     *
     * @param userName 使用者名
     * @return 使用者物件訊息
     */
    @Override
    public SysUser selectUserByUserName(String userName) {
        return userMapper.selectUserByUserName(userName);
    }

    /**
     * 通過使用者ID查詢使用者
     *
     * @param userId 使用者ID
     * @return 使用者物件訊息
     */
    @Override
    public SysUser selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    /**
     * 查詢使用者所屬角色組
     *
     * @param userName 使用者名
     * @return 結果
     */
    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    /**
     * 查詢使用者所屬職位組
     *
     * @param userName 使用者名
     * @return 結果
     */
    @Override
    public String selectUserPostGroup(String userName) {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollectionUtils.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

    /**
     * 校驗使用者名稱是否唯一
     *
     * @param user 使用者訊息
     * @return 結果
     */
    @Override
    public boolean checkUserNameUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkUserNameUnique(user.getUserName());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校驗手機號碼是否唯一
     *
     * @param user 使用者訊息
     * @return
     */
    @Override
    public boolean checkPhoneUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校驗email是否唯一
     *
     * @param user 使用者訊息
     * @return
     */
    @Override
    public boolean checkEmailUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校驗使用者是否允許操作
     *
     * @param user 使用者訊息
     */
    @Override
    public void checkUserAllowed(SysUser user) {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin()) {
            throw new ServiceException("不允許操作超級管理員使用者");
        }
    }

    /**
     * 校驗使用者是否有數據權限
     *
     * @param userId 使用者id
     */
    @Override
    public void checkUserDataScope(Long userId) {
        if (!SysUser.isAdmin(SecurityUtils.getUserId())) {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = SpringUtils.getAopProxy(this).selectUserList(user);
            if (StringUtils.isEmpty(users)) {
                throw new ServiceException("沒有權限訪問使用者數據！");
            }
        }
    }

    /**
     * 新增儲存使用者訊息
     *
     * @param user 使用者訊息
     * @return 結果
     */
    @Override
    @Transactional
    public int insertUser(SysUser user) {
        // 新增使用者訊息
        int rows = userMapper.insertUser(user);
        // 新增使用者職位關聯
        insertUserPost(user);
        // 新增使用者與角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 註冊使用者訊息
     *
     * @param user 使用者訊息
     * @return 結果
     */
    @Override
    public boolean registerUser(SysUser user) {
        return userMapper.insertUser(user) > 0;
    }

    /**
     * 修改儲存使用者訊息
     *
     * @param user 使用者訊息
     * @return 結果
     */
    @Override
    @Transactional
    public int updateUser(SysUser user) {
        Long userId = user.getUserId();
        // 刪除使用者與角色關聯
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增使用者與角色管理
        insertUserRole(user);
        // 刪除使用者與職位關聯
        userPostMapper.deleteUserPostByUserId(userId);
        // 新增使用者與職位管理
        insertUserPost(user);
        return userMapper.updateUser(user);
    }

    /**
     * 使用者授權角色
     *
     * @param userId  使用者ID
     * @param roleIds 角色組
     */
    @Override
    @Transactional
    public void insertUserAuth(Long userId, Long[] roleIds) {
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改使用者狀態
     *
     * @param user 使用者訊息
     * @return 結果
     */
    @Override
    public int updateUserStatus(SysUser user) {
        return userMapper.updateUserStatus(user.getUserId(), user.getStatus());
    }

    /**
     * 修改使用者基本訊息
     *
     * @param user 使用者訊息
     * @return 結果
     */
    @Override
    public int updateUserProfile(SysUser user) {
        return userMapper.updateUser(user);
    }

    /**
     * 修改使用者頭像
     *
     * @param userId 使用者ID
     * @param avatar 頭像位置
     * @return 結果
     */
    @Override
    public boolean updateUserAvatar(Long userId, String avatar) {
        return userMapper.updateUserAvatar(userId, avatar) > 0;
    }

    /**
     * 更新使用者登入訊息（IP和登入時間）
     *
     * @param userId    使用者ID
     * @param loginIp   登入IP位置
     * @param loginDate 登入時間
     * @return 結果
     */
    public void updateLoginInfo(Long userId, String loginIp, Date loginDate) {
        userMapper.updateLoginInfo(userId, loginIp, loginDate);
    }

    /**
     * 重置使用者密碼
     *
     * @param user 使用者訊息
     * @return 結果
     */
    @Override
    public int resetPwd(SysUser user) {
        return userMapper.resetUserPwd(user.getUserId(), user.getPassword());
    }

    /**
     * 重置使用者密碼
     *
     * @param userId   使用者ID
     * @param password 密碼
     * @return 結果
     */
    @Override
    public int resetUserPwd(Long userId, String password) {
        return userMapper.resetUserPwd(userId, password);
    }

    /**
     * 新增使用者角色訊息
     *
     * @param user 使用者物件
     */
    public void insertUserRole(SysUser user) {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    /**
     * 新增使用者職位訊息
     *
     * @param user 使用者物件
     */
    public void insertUserPost(SysUser user) {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotEmpty(posts)) {
            // 新增使用者與職位管理
            List<SysUserPost> list = new ArrayList<SysUserPost>(posts.length);
            for (Long postId : posts) {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            userPostMapper.batchUserPost(list);
        }
    }

    /**
     * 新增使用者角色訊息
     *
     * @param userId  使用者ID
     * @param roleIds 角色組
     */
    public void insertUserRole(Long userId, Long[] roleIds) {
        if (StringUtils.isNotEmpty(roleIds)) {
            // 新增使用者與角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>(roleIds.length);
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            userRoleMapper.batchUserRole(list);
        }
    }

    /**
     * 通過使用者ID刪除使用者
     *
     * @param userId 使用者ID
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteUserById(Long userId) {
        // 刪除使用者與角色關聯
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 刪除使用者與職位表
        userPostMapper.deleteUserPostByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * 批次刪除使用者訊息
     *
     * @param userIds 需要刪除的使用者ID
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        // 刪除使用者與角色關聯
        userRoleMapper.deleteUserRole(userIds);
        // 刪除使用者與職位關聯
        userPostMapper.deleteUserPost(userIds);
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     * 匯入使用者數據
     *
     * @param userList        使用者數據列表
     * @param isUpdateSupport 是否更新支援，如果已存在，則進行更新數據
     * @param operName        操作使用者
     * @return 結果
     */
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName) {
        if (StringUtils.isNull(userList) || userList.isEmpty()) {
            throw new ServiceException("匯入使用者數據不能為空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysUser user : userList) {
            try {
                // 驗證是否存在這個使用者
                SysUser u = userMapper.selectUserByUserName(user.getUserName());
                if (StringUtils.isNull(u)) {
                    BeanValidators.validateWithException(validator, user);
                    deptService.checkDeptDataScope(user.getDeptId());
                    String password = configService.selectConfigByKey("sys.user.initPassword");
                    user.setPassword(SecurityUtils.encryptPassword(password));
                    user.setCreateBy(operName);
                    userMapper.insertUser(user);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、帳號 ").append(user.getUserName()).append(" 匯入成功");
                } else if (isUpdateSupport) {
                    BeanValidators.validateWithException(validator, user);
                    checkUserAllowed(u);
                    checkUserDataScope(u.getUserId());
                    deptService.checkDeptDataScope(user.getDeptId());
                    user.setUserId(u.getUserId());
                    user.setDeptId(u.getDeptId());
                    user.setUpdateBy(operName);
                    userMapper.updateUser(user);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、帳號 ").append(user.getUserName()).append(" 更新成功");
                } else {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、帳號 ").append(user.getUserName()).append(" 已存在");
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、帳號 " + user.getUserName() + " 匯入失敗：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，匯入失敗！共 " + failureNum + " 則數據格式不正確，錯誤如下：");
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，數據已全部匯入成功！共 " + successNum + " 條，數據如下：");
        }
        return successMsg.toString();
    }
}
