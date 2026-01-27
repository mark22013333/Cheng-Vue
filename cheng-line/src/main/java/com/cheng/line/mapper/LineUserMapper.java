package com.cheng.line.mapper;

import com.cheng.line.domain.LineUser;
import com.cheng.line.enums.BindStatus;
import com.cheng.line.enums.FollowStatus;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * LINE 使用者 Mapper 介面
 *
 * @author cheng
 */
public interface LineUserMapper {

    /**
     * 查詢 LINE 使用者
     *
     * @param id 主鍵ID
     * @return LINE 使用者
     */
    LineUser selectLineUserById(Long id);

    /**
     * 根據 LINE 使用者 ID 查詢
     *
     * @param lineUserId LINE 使用者ID
     * @return LINE 使用者
     */
    LineUser selectLineUserByLineUserId(String lineUserId);

    /**
     * 查詢 LINE 使用者列表
     *
     * @param lineUser LINE 使用者
     * @return LINE 使用者集合
     */
    List<LineUser> selectLineUserList(LineUser lineUser);

    /**
     * 查詢已綁定的 LINE 使用者列表
     *
     * @return LINE 使用者集合
     */
    List<LineUser> selectBoundLineUsers();

    /**
     * 查詢關注中的 LINE 使用者列表
     *
     * @return LINE 使用者集合
     */
    List<LineUser> selectFollowingLineUsers();

    /**
     * 查詢所有關注中的 LINE 使用者 ID
     *
     * @return LINE 使用者 ID 集合
     */
    List<String> selectAllFollowingLineUserIds();

    /**
     * 根據系統使用者 ID 查詢
     *
     * @param sysUserId 系統使用者ID
     * @return LINE 使用者
     */
    LineUser selectLineUserBySysUserId(Long sysUserId);

    /**
     * 新增 LINE 使用者
     *
     * @param lineUser LINE 使用者
     * @return 結果
     */
    int insertLineUser(LineUser lineUser);

    /**
     * 修改 LINE 使用者
     *
     * @param lineUser LINE 使用者
     * @return 結果
     */
    int updateLineUser(LineUser lineUser);

    /**
     * 更新綁定狀態
     *
     * @param lineUserId LINE 使用者ID
     * @param bindStatus 綁定狀態
     * @param sysUserId  系統使用者ID
     * @return 結果
     */
    int updateBindStatus(String lineUserId, BindStatus bindStatus, Long sysUserId);

    /**
     * 更新關注狀態
     *
     * @param lineUserId   LINE 使用者ID
     * @param followStatus 關注狀態
     * @return 結果
     */
    int updateFollowStatus(String lineUserId, FollowStatus followStatus);

    /**
     * 刪除 LINE 使用者
     *
     * @param id 主鍵ID
     * @return 結果
     */
    int deleteLineUserById(Long id);

    /**
     * 批次刪除 LINE 使用者
     *
     * @param ids 需要刪除的資料ID
     * @return 結果
     */
    int deleteLineUserByIds(Long[] ids);

    /**
     * 統計關注中的使用者數量
     *
     * @return 數量
     */
    int countFollowingUsers();

    /**
     * 統計已綁定的使用者數量
     *
     * @return 數量
     */
    int countBoundUsers();

    /**
     * 增加發送訊息計數
     *
     * @param lineUserId LINE 使用者ID
     * @return 結果
     */
    int incrementMessagesSent(String lineUserId);

    /**
     * 增加接收訊息計數
     *
     * @param lineUserId LINE 使用者ID
     * @return 結果
     */
    int incrementMessagesReceived(String lineUserId);

    /**
     * 更新最後互動時間
     *
     * @param lineUserId LINE 使用者ID
     * @return 結果
     */
    int updateLastInteractionTime(String lineUserId);

    /**
     * 統計總使用者數
     *
     * @return 數量
     */
    long countTotalUsers();

    /**
     * 統計未關注使用者數量（取消關注或封鎖）
     *
     * @return 數量
     */
    long countUnfollowedUsers();

    /**
     * 統計黑名單使用者數量（管理者設定）
     *
     * @return 數量
     */
    long countBlacklistedUsers();

    /**
     * 統計指定時間範圍內新增的使用者數量
     *
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 數量
     */
    long countNewUsersByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 取得每月加入統計資料
     *
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 每月統計列表（Map格式：{month: "2024-01", count: 10}）
     */
    List<Map<String, Object>> getMonthlyJoinStats(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 批次查詢存在的 LINE 使用者 ID
     *
     * @param lineUserIds LINE 使用者 ID 列表
     * @return 存在的 LINE 使用者 ID 列表
     */
    List<String> selectExistingLineUserIds(@Param("lineUserIds") List<String> lineUserIds);
}
