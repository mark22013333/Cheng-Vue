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
            log.error("綁定系統使用者失敗", e);
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
            log.error("解除綁定系統使用者失敗", e);
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
            log.error("更新使用者個人資料失敗", e);
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

    /**
     * 取得使用者統計資料
     */
    @PreAuthorize("@ss.hasPermi('line:user:list')")
    @GetMapping("/stats")
    public AjaxResult getStats() {
        return success(lineUserService.getUserStats());
    }

    /**
     * 匯入 LINE 使用者
     */
    @PreAuthorize("@ss.hasPermi('line:user:import')")
    @Log(title = "匯入LINE使用者", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importUsers(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                   @RequestParam("configId") Integer configId) {
        try {
            if (file.isEmpty()) {
                return error("請選擇要上傳的檔案");
            }
            
            com.cheng.line.dto.LineUserImportResultDTO result = lineUserService.importLineUsers(file, configId);
            return success(result);
        } catch (Exception e) {
            log.error("匯入 LINE 使用者失敗", e);
            return error("匯入 LINE 使用者失敗：" + e.getMessage());
        }
    }

    /**
     * 下載匯入範本
     */
    @PreAuthorize("@ss.hasPermi('line:user:import')")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        com.cheng.common.utils.poi.ExcelUtil<com.cheng.line.domain.LineUser> util = 
                new com.cheng.common.utils.poi.ExcelUtil<>(com.cheng.line.domain.LineUser.class);
        util.importTemplateExcel(response, "LINE使用者");
    }

    /**
     * 將使用者加入黑名單
     */
    @PreAuthorize("@ss.hasPermi('line:user:edit')")
    @Log(title = "加入黑名單", businessType = BusinessType.UPDATE)
    @PutMapping("/blacklist/add/{lineUserId}")
    public AjaxResult addToBlacklist(@PathVariable("lineUserId") String lineUserId) {
        try {
            return toAjax(lineUserService.addToBlacklist(lineUserId));
        } catch (Exception e) {
            log.error("加入黑名單失敗", e);
            return error(e.getMessage());
        }
    }

    /**
     * 將使用者從黑名單移除
     */
    @PreAuthorize("@ss.hasPermi('line:user:edit')")
    @Log(title = "移除黑名單", businessType = BusinessType.UPDATE)
    @PutMapping("/blacklist/remove/{lineUserId}")
    public AjaxResult removeFromBlacklist(@PathVariable("lineUserId") String lineUserId) {
        try {
            return toAjax(lineUserService.removeFromBlacklist(lineUserId));
        } catch (Exception e) {
            log.error("移除黑名單失敗", e);
            return error(e.getMessage());
        }
    }

    /**
     * 批次將使用者加入黑名單
     */
    @PreAuthorize("@ss.hasPermi('line:user:edit')")
    @Log(title = "批次加入黑名單", businessType = BusinessType.UPDATE)
    @PutMapping("/blacklist/batchAdd")
    public AjaxResult batchAddToBlacklist(@RequestBody String[] lineUserIds) {
        try {
            int count = lineUserService.batchAddToBlacklist(lineUserIds);
            return success("成功加入 " + count + " 位使用者到黑名單");
        } catch (Exception e) {
            log.error("批次加入黑名單失敗", e);
            return error(e.getMessage());
        }
    }

    /**
     * 批次將使用者從黑名單移除
     */
    @PreAuthorize("@ss.hasPermi('line:user:edit')")
    @Log(title = "批次移除黑名單", businessType = BusinessType.UPDATE)
    @PutMapping("/blacklist/batchRemove")
    public AjaxResult batchRemoveFromBlacklist(@RequestBody String[] lineUserIds) {
        try {
            int count = lineUserService.batchRemoveFromBlacklist(lineUserIds);
            return success("成功移除 " + count + " 位使用者的黑名單");
        } catch (Exception e) {
            log.error("批次移除黑名單失敗", e);
            return error(e.getMessage());
        }
    }
}
