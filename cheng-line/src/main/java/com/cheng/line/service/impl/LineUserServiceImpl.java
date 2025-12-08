package com.cheng.line.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.StringUtils;
import com.cheng.line.client.LineClientFactory;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.domain.LineUser;
import com.cheng.line.dto.LineUserImportResultDTO;
import com.cheng.line.dto.LineUserStatsDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private @Resource LineClientFactory lineClientFactory;

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
            lineUser.setFollowStatus(FollowStatus.UNFOLLOWED);
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
     * 當使用者封鎖或刪除好友時觸發
     * 
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int handleUnfollowEvent(String lineUserId) {
        LineUser user = lineUserMapper.selectLineUserByLineUserId(lineUserId);
        if (user != null) {
            Date now = new Date();
            
            // 更新關注狀態為未關注（使用者主動取消關注）
            user.setFollowStatus(FollowStatus.UNFOLLOWED);
            user.setUnfollowTime(now);
            // blockTime 只在管理者加入黑名單時才設定，使用者封鎖不設定
            
            // 統一將綁定狀態改為未綁定
            user.setBindStatus(BindStatus.UNBOUND);
            user.setUnbindTime(now);
            user.setSysUserId(null);
            
            log.info("使用者取消關注，已自動解除綁定, lineUserId={}", lineUserId);
            
            return lineUserMapper.updateLineUser(user);
        }
        log.warn("處理取消關注事件失敗，使用者不存在, lineUserId={}", lineUserId);
        return 0;
    }

    /**
     * 處理使用者封鎖事件（LINE 的封鎖與取消關注是同一個 unfollow 事件）
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    @Override
    @Transactional
    public int handleBlockEvent(String lineUserId) {
        LineUser user = lineUserMapper.selectLineUserByLineUserId(lineUserId);
        if (user != null) {
            // LINE 的封鎖與取消關注在系統中視為同一狀態
            user.setFollowStatus(FollowStatus.UNFOLLOWED);
            user.setUnfollowTime(new Date());
            // blockTime 只在管理者加入黑名單時才設定，使用者封鎖不設定
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
            // 取得 LINE Messaging API Client（復用快取）
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

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

    /**
     * 取得使用者統計資料
     *
     * @return 統計資料
     */
    @Override
    public LineUserStatsDTO getUserStats() {
        // 取得當前日期時間
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);

        // 格式化日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String todayStartStr = todayStart.format(formatter);
        String weekStartStr = weekStart.format(formatter);
        String monthStartStr = monthStart.format(formatter);
        String nowStr = now.format(formatter);

        return LineUserStatsDTO.builder()
                .totalUsers(lineUserMapper.countTotalUsers())
                .followingCount((long) lineUserMapper.countFollowingUsers())
                .boundCount((long) lineUserMapper.countBoundUsers())
                .unfollowedCount(lineUserMapper.countUnfollowedUsers())
                .blacklistedCount(lineUserMapper.countBlacklistedUsers())
                .todayNewCount(lineUserMapper.countNewUsersByDateRange(todayStartStr, nowStr))
                .weekNewCount(lineUserMapper.countNewUsersByDateRange(weekStartStr, nowStr))
                .monthNewCount(lineUserMapper.countNewUsersByDateRange(monthStartStr, nowStr))
                .build();
    }

    /**
     * 取得每月加入統計資料
     *
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 每月統計列表
     */
    @Override
    public List<java.util.Map<String, Object>> getMonthlyJoinStats(String startTime, String endTime) {
        return lineUserMapper.getMonthlyJoinStats(startTime, endTime);
    }

    /**
     * 匯入 LINE 使用者（從檔案）
     *
     * @param file     上傳的檔案
     * @param configId 頻道設定ID
     * @return 匯入結果
     */
    @Override
    @Transactional
    public LineUserImportResultDTO importLineUsers(MultipartFile file, Integer configId) {
        LineUserImportResultDTO result = LineUserImportResultDTO.builder()
                .totalCount(0)
                .successCount(0)
                .failCount(0)
                .newCount(0)
                .updateCount(0)
                .failDetails(new ArrayList<>())
                .build();

        try {
            // 讀取檔案內容
            List<String> lineUserIds = readLineUserIdsFromFile(file);
            result.setTotalCount(lineUserIds.size());

            // 取得頻道設定
            if (configId == null) {
                throw new ServiceException("請指定頻道設定");
            }
            
            LineConfig config = lineConfigService.selectLineConfigById(configId);
            if (config == null) {
                throw new ServiceException("頻道設定不存在");
            }
            
            // 取得 LINE Messaging API Client（復用快取）
            MessagingApiClient client = lineClientFactory.getClient(config.getChannelAccessToken());

            // 逐個處理 LINE User ID
            for (int i = 0; i < lineUserIds.size(); i++) {
                String lineUserId = lineUserIds.get(i).trim();
                int rowNumber = i + 1;

                if (StringUtils.isEmpty(lineUserId)) {
                    continue;
                }

                try {
                    // 檢查是否已存在
                    LineUser existUser = lineUserMapper.selectLineUserByLineUserId(lineUserId);
                    boolean isNew = (existUser == null);

                    // 從 LINE API 取得使用者資料
                    UserProfileResponse profile = client.getProfile(lineUserId).get().body();

                    if (isNew) {
                        // 建立新使用者
                        LineUser newUser = new LineUser();
                        newUser.setLineUserId(lineUserId);
                        newUser.setLineDisplayName(profile.displayName());
                        newUser.setLinePictureUrl(profile.pictureUrl() != null ? profile.pictureUrl().toString() : null);
                        newUser.setLineStatusMessage(profile.statusMessage());
                        newUser.setFollowStatus(FollowStatus.FOLLOWING);
                        newUser.setBindStatus(BindStatus.UNBOUND);
                        newUser.setFirstFollowTime(new Date());
                        newUser.setLatestFollowTime(new Date());
                        newUser.setBindCount(0);
                        newUser.setTotalMessagesSent(0);
                        newUser.setTotalMessagesReceived(0);
                        lineUserMapper.insertLineUser(newUser);
                        result.setNewCount(result.getNewCount() + 1);
                    } else {
                        // 更新既有使用者
                        existUser.setLineDisplayName(profile.displayName());
                        existUser.setLinePictureUrl(profile.pictureUrl() != null ? profile.pictureUrl().toString() : null);
                        existUser.setLineStatusMessage(profile.statusMessage());
                        existUser.setFollowStatus(FollowStatus.FOLLOWING);
                        existUser.setLatestFollowTime(new Date());
                        lineUserMapper.updateLineUser(existUser);
                        result.setUpdateCount(result.getUpdateCount() + 1);
                    }

                    result.setSuccessCount(result.getSuccessCount() + 1);

                } catch (Exception e) {
                    log.error("匯入 LINE 使用者失敗：{}", lineUserId, e);
                    result.setFailCount(result.getFailCount() + 1);
                    result.getFailDetails().add(
                            LineUserImportResultDTO.ImportFailDetail.builder()
                                    .lineUserId(lineUserId)
                                    .reason(e.getMessage())
                                    .rowNumber(rowNumber)
                                    .build()
                    );
                }
            }

            return result;

        } catch (Exception e) {
            log.error("匯入 LINE 使用者失敗", e);
            throw new ServiceException("匯入 LINE 使用者失敗：" + e.getMessage());
        }
    }

    /**
     * 從檔案讀取 LINE User ID 列表
     *
     * @param file 上傳的檔案
     * @return LINE User ID 列表
     */
    private List<String> readLineUserIdsFromFile(MultipartFile file) {
        List<String> lineUserIds = new ArrayList<>();
        String fileName = file.getOriginalFilename();

        try {
            if (fileName == null) {
                throw new ServiceException("檔案名稱不能為空");
            }

            // 根據檔案類型讀取
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                // Excel 檔案
                lineUserIds = readFromExcel(file);
            } else if (fileName.endsWith(".csv")) {
                // CSV 檔案
                lineUserIds = readFromCsv(file);
            } else if (fileName.endsWith(".txt")) {
                // TXT 檔案
                lineUserIds = readFromTxt(file);
            } else {
                throw new ServiceException("不支援的檔案格式，請上傳 .xlsx、.xls、.csv 或 .txt 檔案");
            }

            // 移除空白和重複項
            return lineUserIds.stream()
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            log.error("讀取檔案失敗", e);
            throw new ServiceException("讀取檔案失敗：" + e.getMessage());
        }
    }

    /**
     * 從 Excel 讀取
     */
    private List<String> readFromExcel(MultipartFile file) throws Exception {
        List<String> result = new ArrayList<>();
        try (java.io.InputStream is = file.getInputStream();
             org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(is)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 跳過標題行
                org.apache.poi.ss.usermodel.Cell cell = row.getCell(0);
                if (cell != null) {
                    String value = cell.getStringCellValue();
                    if (StringUtils.isNotEmpty(value)) {
                        result.add(value.trim());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 從 CSV 讀取
     */
    private List<String> readFromCsv(MultipartFile file) throws Exception {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳過標題行
                }
                String[] values = line.split(",");
                if (values.length > 0 && StringUtils.isNotEmpty(values[0])) {
                    result.add(values[0].trim());
                }
            }
        }
        return result;
    }

    /**
     * 從 TXT 讀取（換行分隔）
     */
    private List<String> readFromTxt(MultipartFile file) throws Exception {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (StringUtils.isNotEmpty(trimmed)) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    /**
     * 將使用者加入黑名單
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addToBlacklist(String lineUserId) {
        LineUser user = lineUserMapper.selectLineUserByLineUserId(lineUserId);
        if (user == null) {
            throw new ServiceException("使用者不存在");
        }
        
        if (user.getFollowStatus() == FollowStatus.BLACKLISTED) {
            throw new ServiceException("該使用者已在黑名單中");
        }
        
        // 設為黑名單
        user.setFollowStatus(FollowStatus.BLACKLISTED);
        user.setBlockTime(new Date());
        
        // 自動解除綁定
        if (user.getBindStatus() == BindStatus.BOUND) {
            user.setBindStatus(BindStatus.UNBOUND);
            user.setUnbindTime(new Date());
            user.setSysUserId(null);
            log.info("使用者加入黑名單，已自動解除綁定, lineUserId={}", lineUserId);
        }
        
        return lineUserMapper.updateLineUser(user);
    }

    /**
     * 將使用者從黑名單移除
     *
     * @param lineUserId LINE 使用者 ID
     * @return 結果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeFromBlacklist(String lineUserId) {
        LineUser user = lineUserMapper.selectLineUserByLineUserId(lineUserId);
        if (user == null) {
            throw new ServiceException("使用者不存在");
        }
        
        if (user.getFollowStatus() != FollowStatus.BLACKLISTED) {
            throw new ServiceException("該使用者不在黑名單中");
        }
        
        // 移除黑名單，改為未關注狀態（需要使用者重新關注）
        user.setFollowStatus(FollowStatus.UNFOLLOWED);
        user.setBlockTime(null);
        
        log.info("使用者已從黑名單移除, lineUserId={}", lineUserId);
        
        return lineUserMapper.updateLineUser(user);
    }

    /**
     * 批次將使用者加入黑名單
     *
     * @param lineUserIds LINE 使用者 ID 陣列
     * @return 成功加入的數量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddToBlacklist(String[] lineUserIds) {
        if (lineUserIds == null || lineUserIds.length == 0) {
            throw new ServiceException("請選擇要加入黑名單的使用者");
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder failedUsers = new StringBuilder();

        for (String lineUserId : lineUserIds) {
            try {
                addToBlacklist(lineUserId);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failedUsers.append(lineUserId).append(", ");
                log.warn("加入黑名單失敗, lineUserId={}, 原因: {}", lineUserId, e.getMessage());
            }
        }

        if (failCount > 0) {
            log.warn("批次加入黑名單完成，成功: {}, 失敗: {}, 失敗的使用者: {}", 
                successCount, failCount, failedUsers.toString());
        } else {
            log.info("批次加入黑名單完成，成功: {}", successCount);
        }

        return successCount;
    }

    /**
     * 批次將使用者從黑名單移除
     *
     * @param lineUserIds LINE 使用者 ID 陣列
     * @return 成功移除的數量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRemoveFromBlacklist(String[] lineUserIds) {
        if (lineUserIds == null || lineUserIds.length == 0) {
            throw new ServiceException("請選擇要移除黑名單的使用者");
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder failedUsers = new StringBuilder();

        for (String lineUserId : lineUserIds) {
            try {
                removeFromBlacklist(lineUserId);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failedUsers.append(lineUserId).append(", ");
                log.warn("移除黑名單失敗, lineUserId={}, 原因: {}", lineUserId, e.getMessage());
            }
        }

        if (failCount > 0) {
            log.warn("批次移除黑名單完成，成功: {}, 失敗: {}, 失敗的使用者: {}", 
                successCount, failCount, failedUsers.toString());
        } else {
            log.info("批次移除黑名單完成，成功: {}", successCount);
        }

        return successCount;
    }
}
