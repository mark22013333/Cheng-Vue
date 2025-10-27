package com.cheng.web.controller.line;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.line.domain.LineUser;
import com.cheng.line.service.ILineUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LINE 使用者 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/line/user")
public class LineUserController extends BaseController {

    @Autowired
    private ILineUserService lineUserService;

    /**
     * 查詢 LINE 使用者列表
     */
    @PreAuthorize("@ss.hasPermi('line:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(LineUser lineUser) {
        startPage();
        List<LineUser> list = lineUserService.selectLineUserList(lineUser);
        return getDataTable(list);
    }

    /**
     * 查詢所有關注中的使用者
     */
    @PreAuthorize("@ss.hasPermi('line:user:list')")
    @GetMapping("/following")
    public AjaxResult getFollowingUsers() {
        List<LineUser> list = lineUserService.selectFollowingUsers();
        return success(list);
    }

    /**
     * 匯出 LINE 使用者列表
     */
    @PreAuthorize("@ss.hasPermi('line:user:export')")
    @Log(title = "LINE使用者", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LineUser lineUser) {
        List<LineUser> list = lineUserService.selectLineUserList(lineUser);
        ExcelUtil<LineUser> util = new ExcelUtil<>(LineUser.class);
        util.exportExcel(response, list, "LINE使用者");
    }

    /**
     * 取得 LINE 使用者詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('line:user:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(lineUserService.selectLineUserById(id));
    }

    /**
     * 根據 LINE 使用者 ID 查詢
     */
    @PreAuthorize("@ss.hasPermi('line:user:query')")
    @GetMapping("/lineUserId/{lineUserId}")
    public AjaxResult getByLineUserId(@PathVariable("lineUserId") String lineUserId) {
        return success(lineUserService.selectLineUserByLineUserId(lineUserId));
    }

    /**
     * 綁定系統使用者
     */
    @PreAuthorize("@ss.hasPermi('line:user:bind')")
    @Log(title = "綁定LINE使用者", businessType = BusinessType.UPDATE)
    @PutMapping("/bind/{lineUserId}/{sysUserId}")
    public AjaxResult bind(@PathVariable("lineUserId") String lineUserId, @PathVariable("sysUserId") Long sysUserId) {
        try {
            return toAjax(lineUserService.bindSysUser(lineUserId, sysUserId));
        } catch (Exception e) {
            logger.error("綁定系統使用者失敗", e);
            return error("綁定系統使用者失敗：" + e.getMessage());
        }
    }

    /**
     * 解除綁定系統使用者
     */
    @PreAuthorize("@ss.hasPermi('line:user:bind')")
    @Log(title = "解除綁定LINE使用者", businessType = BusinessType.UPDATE)
    @PutMapping("/unbind/{lineUserId}")
    public AjaxResult unbind(@PathVariable("lineUserId") String lineUserId) {
        try {
            return toAjax(lineUserService.unbindSysUser(lineUserId));
        } catch (Exception e) {
            logger.error("解除綁定系統使用者失敗", e);
            return error("解除綁定系統使用者失敗：" + e.getMessage());
        }
    }

    /**
     * 更新使用者個人資料（從 LINE API 取得）
     */
    @PreAuthorize("@ss.hasPermi('line:user:edit')")
    @Log(title = "更新LINE使用者資料", businessType = BusinessType.UPDATE)
    @PutMapping("/updateProfile/{lineUserId}")
    public AjaxResult updateProfile(@PathVariable("lineUserId") String lineUserId, @RequestParam(required = false) Integer configId) {
        try {
            return toAjax(lineUserService.updateUserProfile(lineUserId, configId));
        } catch (Exception e) {
            logger.error("更新使用者個人資料失敗", e);
            return error("更新使用者個人資料失敗：" + e.getMessage());
        }
    }

    /**
     * 刪除 LINE 使用者
     */
    @PreAuthorize("@ss.hasPermi('line:user:remove')")
    @Log(title = "LINE使用者", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(lineUserService.deleteLineUserByIds(ids));
    }
}
