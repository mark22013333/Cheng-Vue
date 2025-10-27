package com.cheng.line.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.StringUtils;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.domain.LineUser;
import com.cheng.line.enums.BindStatus;
import com.cheng.line.enums.FollowStatus;
import com.cheng.line.mapper.LineUserMapper;
import com.cheng.line.service.ILineConfigService;
import com.cheng.line.service.ILineUserService;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.UserProfileResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * LINE 使用者 服務層實作
 *
 * @author cheng
 */
@Slf4j
@Service
public class LineUserServiceImpl implements ILineUserService {

    private @Resource LineUserMapper lineUserMapper;
    private @Resource ILineConfigService lineConfigService;

    /**
     * 查詢 LINE 使用者
     *
     * @param id 主鍵ID
     * @return LINE 使用者
     */
    @Override
    public LineUser selectLineUserById(Long id) {
        return lineUserMapper.selectLineUserById(id);
    }

    /**
     * 根據 LINE 使用者 ID 查詢
     *
     * @param lineUserId LINE 使用者 ID
     * @return LINE 使用者
     */
    @Override
    public LineUser selectLineUserByLineUserId(String lineUserId) {
        return lineUserMapper.selectLineUserByLineUserId(lineUserId);
    }

    /**
     * 查詢 LINE 使用者列表
     *
     * @param lineUser LINE 使用者
     * @return LINE 使用者集合
     */
    @Override
    public List<LineUser> selectLineUserList(LineUser lineUser) {
        return lineUserMapper.selectLineUserList(lineUser);
    }

    /**
     * 查詢所有已關注的使用者
     *
     * @return LINE 使用者集合
     */
    @Override
    public List<LineUser> selectFollowingUsers() {
        return lineUserMapper.selectFollowingLineUsers();
    }

    /**
     * 新增 LINE 使用者
     *
     * @param lineUser LINE 使用者
     * @return 結果
     */
    @Override
    public int insertLineUser(LineUser lineUser) {
        // 設定預設值
        if (lineUser.getBindStatus() == null) {
            lineUser.setBindStatus(BindStatus.UNBOUND);
        }
        if (lineUser.getFollowStatus() == null) {
            lineUser.setFollowStatus(FollowStatus.BLOCKED);
        }
        if (lineUser.getBindCount() == null) {
            lineUser.setBindCount(0);
        }
        if (lineUser.getTotalMessagesSent() == null) {
            lineUser.setTotalMessagesSent(0);
        }
        if (lineUser.getTotalMessagesReceived() == null) {
            lineUser.setTotalMessagesReceived(0);
        }
        return lineUserMapper.insertLineUser(lineUser);
    }

    /**
     * 修改 LINE 使用者
     *
     * @param lineUser LINE 使用者
     * @return 結果
     */
    @Override
    public int updateLineUser(LineUser lineUser) {
        return lineUserMapper.updateLineUser(lineUser);
    }

    /**
     * 批次刪除 LINE 使用者
     *
     * @param ids 需要刪除的主鍵ID
     * @return 結果
     */
    @Override
    @Transactional
    public int deleteLineUserByIds(Long[] ids) {
        return lineUserMapper.deleteLineUserByIds(ids);
    }

    /**
     * 刪除 LINE 使用者
     *
     * @param id 主鍵ID
     * @return 結果
     */
    @Override
    public int deleteLineUserById(Long id) {
        return lineUserMapper.deleteLineUserById(id);
    }

    /**
     * 處理使用者關注事件
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    @Override
    @Transactional
    public int handleFollowEvent(String lineUserId) {
        LineUser existUser = lineUserMapper.selectLineUserByLineUserId(lineUserId);
        Date now = new Date();

        if (existUser == null) {
            // 新使用者，建立記錄
            LineUser newUser = new LineUser();
            newUser.setLineUserId(lineUserId);
            newUser.setFollowStatus(FollowStatus.FOLLOWING);
            newUser.setBindStatus(BindStatus.UNBOUND);
            newUser.setFirstFollowTime(now);
            newUser.setLatestFollowTime(now);
            newUser.setLastInteractionTime(now);
            newUser.setBindCount(0);
            newUser.setTotalMessagesSent(0);
            newUser.setTotalMessagesReceived(0);

            return lineUserMapper.insertLineUser(newUser);
        } else {
            // 既有使用者重新關注
            existUser.setFollowStatus(FollowStatus.FOLLOWING);
            existUser.setLatestFollowTime(now);
            existUser.setUnfollowTime(null);
            existUser.setBlockTime(null);
            existUser.setLastInteractionTime(now);

            return lineUserMapper.updateLineUser(existUser);
        }
    }

    /**
     * 處理使用者取消關注事件
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    @Override
    @Transactional
    public int handleUnfollowEvent(String lineUserId) {
        LineUser user = lineUserMapper.selectLineUserByLineUserId(lineUserId);
        if (user != null) {
            user.setFollowStatus(FollowStatus.BLOCKED);
            user.setUnfollowTime(new Date());
            return lineUserMapper.updateLineUser(user);
        }
        return 0;
    }

    /**
     * 處理使用者封鎖事件
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    @Override
    @Transactional
    public int handleBlockEvent(String lineUserId) {
        LineUser user = lineUserMapper.selectLineUserByLineUserId(lineUserId);
        if (user != null) {
            user.setFollowStatus(FollowStatus.BLOCKED);
            user.setBlockTime(new Date());
            return lineUserMapper.updateLineUser(user);
        }
        return 0;
    }

    /**
     * 綁定系統使用者
     *
     * @param lineUserId LINE 使用者 ID
     * @param sysUserId  系統使用者 ID
     * @return 結果
     */
    @Override
    @Transactional
    public int bindSysUser(String lineUserId, Long sysUserId) {
        LineUser user = lineUserMapper.selectLineUserByLineUserId(lineUserId);
        if (user == null) {
            throw new ServiceException("LINE 使用者不存在");
        }

        Date now = new Date();
        user.setSysUserId(sysUserId);
        user.setBindStatus(BindStatus.BOUND);

        // 更新綁定時間
        if (user.getFirstBindTime() == null) {
            user.setFirstBindTime(now);
        }
        user.setLatestBindTime(now);
        user.setUnbindTime(null);

        // 增加綁定次數
        if (user.getBindCount() == null) {
            user.setBindCount(1);
        } else {
            user.setBindCount(user.getBindCount() + 1);
        }

        return lineUserMapper.updateLineUser(user);
    }

    /**
     * 解除綁定系統使用者
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    @Override
    @Transactional
    public int unbindSysUser(String lineUserId) {
        LineUser user = lineUserMapper.selectLineUserByLineUserId(lineUserId);
        if (user == null) {
            throw new ServiceException("LINE 使用者不存在");
        }

        user.setSysUserId(null);
        user.setBindStatus(BindStatus.UNBOUND);
        user.setUnbindTime(new Date());

        return lineUserMapper.updateLineUser(user);
    }

    /**
     * 更新使用者個人資料（從 LINE API 取得）
     *
     * @param lineUserId LINE 使用者 ID
     * @param configId   頻道設定ID
     * @return 結果
     */
    @Override
    @Transactional
    public int updateUserProfile(String lineUserId, Integer configId) {
        // 取得頻道設定
        LineConfig config;
        if (configId != null) {
            config = lineConfigService.selectLineConfigById(configId);
        } else {
            config = lineConfigService.selectDefaultLineConfig();
        }

        if (config == null) {
            throw new ServiceException("頻道設定不存在");
        }

        try {
            // 建立 LINE Messaging API Client (SDK 9.x)
            MessagingApiClient client = MessagingApiClient.builder(config.getChannelAccessToken()).build();

            // 取得使用者個人資料
            UserProfileResponse profile = client.getProfile(lineUserId).get().body();

            // 更新資料庫
            LineUser user = lineUserMapper.selectLineUserByLineUserId(lineUserId);
            if (user == null) {
                // 建立新使用者
                user = new LineUser();
                user.setLineUserId(lineUserId);
                user.setLineDisplayName(profile.displayName());
                user.setLinePictureUrl(profile.pictureUrl() != null ? profile.pictureUrl().toString() : null);
                user.setLineStatusMessage(profile.statusMessage());
                user.setFollowStatus(FollowStatus.FOLLOWING);
                user.setBindStatus(BindStatus.UNBOUND);
                user.setBindCount(0);
                user.setTotalMessagesSent(0);
                user.setTotalMessagesReceived(0);
                return lineUserMapper.insertLineUser(user);
            } else {
                // 更新既有使用者
                user.setLineDisplayName(profile.displayName());
                user.setLinePictureUrl(profile.pictureUrl() != null ? profile.pictureUrl().toString() : null);
                user.setLineStatusMessage(profile.statusMessage());
                return lineUserMapper.updateLineUser(user);
            }

        } catch (InterruptedException | ExecutionException e) {
            log.error("更新使用者個人資料失敗", e);
            throw new ServiceException("更新使用者個人資料失敗：" + e.getMessage());
        }
    }

    /**
     * 增加訊息統計計數
     *
     * @param lineUserId LINE 使用者 ID
     * @param sent       是否為發送（true）或接收（false）
     * @return 結果
     */
    @Override
    @Transactional
    public int incrementMessageCount(String lineUserId, boolean sent) {
        // 更新最後互動時間
        lineUserMapper.updateLastInteractionTime(lineUserId);

        // 增加訊息計數
        if (sent) {
            return lineUserMapper.incrementMessagesSent(lineUserId);
        } else {
            return lineUserMapper.incrementMessagesReceived(lineUserId);
        }
    }
}
