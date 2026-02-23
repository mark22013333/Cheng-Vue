package com.cheng.web.controller.line;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.line.domain.LineMessageLog;
import com.cheng.line.dto.BroadcastMessageDTO;
import com.cheng.line.dto.FlexMessageDTO;
import com.cheng.line.dto.MulticastMessageDTO;
import com.cheng.line.dto.PushMessageDTO;
import com.cheng.line.service.ILineMessageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LINE 推播訊息 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/line/message")
public class LineMessageController extends BaseController {

    @Autowired
    private ILineMessageService lineMessageService;

    /**
     * 查詢推播訊息記錄列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(LineMessageLog lineMessageLog) {
        startPage();
        List<LineMessageLog> list = lineMessageService.selectLineMessageLogList(lineMessageLog);
        return getDataTable(list);
    }

    /**
     * 匯出推播訊息記錄列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.EXPORT + "')")
    @Log(title = "LINE推播訊息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LineMessageLog lineMessageLog) {
        List<LineMessageLog> list = lineMessageService.selectLineMessageLogList(lineMessageLog);
        ExcelUtil<LineMessageLog> util = new ExcelUtil<>(LineMessageLog.class);
        util.exportExcel(response, list, "推播訊息記錄");
    }

    /**
     * 取得推播訊息記錄詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.QUERY + "')")
    @GetMapping(value = "/{messageId}")
    public AjaxResult getInfo(@PathVariable("messageId") Long messageId) {
        return success(lineMessageService.selectLineMessageLogById(messageId));
    }

    /**
     * 發送推播訊息（單人）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "發送推播訊息", businessType = BusinessType.INSERT)
    @PostMapping("/push")
    public AjaxResult push(@Validated @RequestBody PushMessageDTO pushMessageDTO) {
        try {
            Long messageId = lineMessageService.sendPushMessage(pushMessageDTO);
            return AjaxResult.success("推播訊息發送成功", messageId);
        } catch (Exception e) {
            log.error("發送推播訊息失敗", e);
            return error("發送推播訊息失敗：" + e.getMessage());
        }
    }

    /**
     * 發送推播訊息（多人）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "發送多人推播訊息", businessType = BusinessType.INSERT)
    @PostMapping("/multicast")
    public AjaxResult multicast(@Validated @RequestBody MulticastMessageDTO multicastMessageDTO) {
        try {
            Long messageId = lineMessageService.sendMulticastMessage(multicastMessageDTO);
            return AjaxResult.success("多人推播訊息發送成功", messageId);
        } catch (Exception e) {
            log.error("發送多人推播訊息失敗", e);
            return error("發送多人推播訊息失敗：" + e.getMessage());
        }
    }

    /**
     * 發送廣播訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "發送廣播訊息", businessType = BusinessType.INSERT)
    @PostMapping("/broadcast")
    public AjaxResult broadcast(@Validated @RequestBody BroadcastMessageDTO broadcastMessageDTO) {
        try {
            Long messageId = lineMessageService.sendBroadcastMessage(broadcastMessageDTO);
            return AjaxResult.success("廣播訊息發送成功", messageId);
        } catch (Exception e) {
            log.error("發送廣播訊息失敗", e);
            return error("發送廣播訊息失敗：" + e.getMessage());
        }
    }

    /**
     * 發送 Flex Message（彈性訊息）
     * 支援單人、多人、廣播
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "發送Flex Message", businessType = BusinessType.INSERT)
    @PostMapping("/flex")
    public AjaxResult sendFlexMessage(@Validated @RequestBody FlexMessageDTO flexMessageDTO) {
        try {
            Long messageId = lineMessageService.sendFlexMessage(flexMessageDTO);
            return AjaxResult.success("Flex Message 發送成功", messageId);
        } catch (Exception e) {
            log.error("發送 Flex Message 失敗", e);
            return error("發送 Flex Message 失敗：" + e.getMessage());
        }
    }

    /**
     * 刪除推播訊息記錄
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.REMOVE + "')")
    @Log(title = "LINE推播訊息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds) {
        return toAjax(lineMessageService.deleteLineMessageLogByIds(messageIds));
    }
}
