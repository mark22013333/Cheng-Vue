package com.cheng.web.controller.system;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.system.domain.SysNotice;
import com.cheng.system.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告 訊息操作處理
 *
 * @author cheng
 */
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {
    @Autowired
    private ISysNoticeService noticeService;

    /**
     * 取得通知公告列表
     */
    @PreAuthorize("@ss.hasPermi('system:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice) {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 根據通知公告編號取得詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId) {
        return success(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice) {
        notice.setCreateBy(getUsername());
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice) {
        notice.setUpdateBy(getUsername());
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 刪除通知公告
     */
    @PreAuthorize("@ss.hasPermi('system:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }

    /**
     * 取得首頁公告列表（不需權限）
     */
    @Anonymous
    @GetMapping("/announcements")
    public AjaxResult getAnnouncements() {
        List<SysNotice> list = noticeService.selectAnnouncementList();
        return success(list);
    }

    /**
     * 取得當前使用者未讀通知列表（不需權限）
     */
    @Anonymous
    @GetMapping("/unread")
    public AjaxResult getUnreadNotifications() {
        Long userId = SecurityUtils.getUserId();
        List<SysNotice> list = noticeService.selectUnreadNotifications(userId);
        return success(list);
    }

    /**
     * 取得當前使用者未讀通知數量（不需權限）
     */
    @Anonymous
    @GetMapping("/unread/count")
    public AjaxResult getUnreadCount() {
        Long userId = SecurityUtils.getUserId();
        int count = noticeService.countUnreadNotifications(userId);
        return success(count);
    }

    /**
     * 標記通知為已讀（不需權限）
     */
    @Anonymous
    @PostMapping("/read/{noticeId}")
    public AjaxResult markAsRead(@PathVariable Long noticeId) {
        Long userId = SecurityUtils.getUserId();
        return toAjax(noticeService.markNoticeAsRead(noticeId, userId));
    }
}
