package com.cheng.web.controller.line;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.line.domain.LineMessageLog;
import com.cheng.line.service.ILineMessageLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LINE 訊息發送記錄 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/line/message/log")
public class LineMessageLogController extends BaseController {

    @Autowired
    private ILineMessageLogService lineMessageLogService;

    /**
     * 查詢訊息發送記錄列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(LineMessageLog lineMessageLog) {
        startPage();
        List<LineMessageLog> list = lineMessageLogService.selectLineMessageLogList(lineMessageLog);
        return getDataTable(list);
    }

    /**
     * 匯出訊息發送記錄列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.EXPORT + "')")
    @Log(title = "LINE訊息發送記錄", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LineMessageLog lineMessageLog) {
        List<LineMessageLog> list = lineMessageLogService.selectLineMessageLogList(lineMessageLog);
        ExcelUtil<LineMessageLog> util = new ExcelUtil<>(LineMessageLog.class);
        util.exportExcel(response, list, "訊息發送記錄");
    }

    /**
     * 取得訊息發送記錄詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.QUERY + "')")
    @GetMapping(value = "/{messageId}")
    public AjaxResult getInfo(@PathVariable("messageId") Long messageId) {
        return success(lineMessageLogService.selectLineMessageLogById(messageId));
    }

    /**
     * 新增訊息發送記錄
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE訊息發送記錄", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LineMessageLog lineMessageLog) {
        return toAjax(lineMessageLogService.insertLineMessageLog(lineMessageLog));
    }

    /**
     * 刪除訊息發送記錄
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.REMOVE + "')")
    @Log(title = "LINE訊息發送記錄", businessType = BusinessType.DELETE)
    @DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds) {
        return toAjax(lineMessageLogService.deleteLineMessageLogByIds(messageIds));
    }

    /**
     * 查詢指定使用者的訊息記錄
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.QUERY + "')")
    @GetMapping("/user/{lineUserId}")
    public AjaxResult getByUserId(@PathVariable("lineUserId") String lineUserId) {
        return success(lineMessageLogService.selectMessageLogByUserId(lineUserId));
    }

    /**
     * 統計頻道訊息數量
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.QUERY + "')")
    @GetMapping("/stats/{configId}")
    public AjaxResult getStats(@PathVariable("configId") Integer configId) {
        int successCount = lineMessageLogService.countSuccessMessagesByConfigId(configId);
        int failedCount = lineMessageLogService.countFailedMessagesByConfigId(configId);
        AjaxResult result = AjaxResult.success();
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("totalCount", successCount + failedCount);
        return result;
    }
}
