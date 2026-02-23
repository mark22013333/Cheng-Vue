package com.cheng.web.controller.monitor;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.framework.web.service.SysPasswordService;
import com.cheng.system.domain.SysLogininfor;
import com.cheng.system.service.ISysUninformatively;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系統訪問記錄
 *
 * @author cheng
 */
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController {
    @Autowired
    private ISysUninformatively uninformatively;

    @Autowired
    private SysPasswordService passwordService;

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Logininfor.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(SysLogininfor logininfor) {
        startPage();
        List<SysLogininfor> list = uninformatively.selectLogininforList(logininfor);
        return getDataTable(list);
    }

    @Log(title = "登入日誌", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Logininfor.EXPORT + "')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLogininfor logininfor) {
        List<SysLogininfor> list = uninformatively.selectLogininforList(logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
        util.exportExcel(response, list, "登入日誌");
    }

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Logininfor.REMOVE + "')")
    @Log(title = "登入日誌", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds) {
        return toAjax(uninformatively.deleteLogininforByIds(infoIds));
    }

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Logininfor.REMOVE + "')")
    @Log(title = "登入日誌", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        uninformatively.cleanLogininfor();
        return success();
    }

    @PreAuthorize("@ss.hasPermi('" + PermConstants.Monitor.Logininfor.UNLOCK + "')")
    @Log(title = "帳號解鎖", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public AjaxResult unlock(@PathVariable("userName") String userName) {
        passwordService.clearLoginRecordCache(userName);
        return success();
    }
}
