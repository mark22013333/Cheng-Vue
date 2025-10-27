package com.cheng.web.controller.line;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.line.domain.LineConfig;
import com.cheng.line.dto.WebhookTestDTO;
import com.cheng.line.service.ILineConfigService;
import com.cheng.line.vo.ConnectionTestVO;
import com.cheng.line.vo.WebhookTestVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * LINE 頻道設定 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/line/config")
public class LineConfigController extends BaseController {

    @Autowired
    private ILineConfigService lineConfigService;

    /**
     * 查詢 LINE 頻道設定列表
     */
    @PreAuthorize("@ss.hasPermi('line:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(LineConfig lineConfig) {
        startPage();
        List<LineConfig> list = lineConfigService.selectLineConfigList(lineConfig);
        return getDataTable(list);
    }

    /**
     * 匯出 LINE 頻道設定列表
     */
    @PreAuthorize("@ss.hasPermi('line:config:export')")
    @Log(title = "LINE頻道設定", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LineConfig lineConfig) {
        List<LineConfig> list = lineConfigService.selectLineConfigList(lineConfig);
        ExcelUtil<LineConfig> util = new ExcelUtil<>(LineConfig.class);
        util.exportExcel(response, list, "LINE頻道設定");
    }

    /**
     * 取得 LINE 頻道設定詳細資訊
     */
    @PreAuthorize("@ss.hasPermi('line:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Integer configId) {
        return success(lineConfigService.selectLineConfigById(configId));
    }

    /**
     * 取得預設頻道設定
     */
    @PreAuthorize("@ss.hasPermi('line:config:query')")
    @GetMapping("/default")
    public AjaxResult getDefaultConfig() {
        return success(lineConfigService.selectDefaultLineConfig());
    }

    /**
     * 新增 LINE 頻道設定
     */
    @PreAuthorize("@ss.hasPermi('line:config:add')")
    @Log(title = "LINE頻道設定", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody LineConfig lineConfig) {
        if (!lineConfigService.checkChannelTypeUnique(lineConfig)) {
            return error("新增 LINE 頻道設定失敗，頻道類型已存在");
        }
        lineConfig.setCreateBy(getUsername());
        return toAjax(lineConfigService.insertLineConfig(lineConfig));
    }

    /**
     * 修改 LINE 頻道設定
     */
    @PreAuthorize("@ss.hasPermi('line:config:edit')")
    @Log(title = "LINE頻道設定", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody LineConfig lineConfig) {
        lineConfig.setUpdateBy(getUsername());
        return toAjax(lineConfigService.updateLineConfig(lineConfig));
    }

    /**
     * 刪除 LINE 頻道設定
     */
    @PreAuthorize("@ss.hasPermi('line:config:remove')")
    @Log(title = "LINE頻道設定", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Integer[] configIds) {
        return toAjax(lineConfigService.deleteLineConfigByIds(configIds));
    }

    /**
     * 測試 LINE 頻道連線
     */
    @PreAuthorize("@ss.hasPermi('line:config:test')")
    @Log(title = "LINE連線測試", businessType = BusinessType.OTHER)
    @PostMapping("/testConnection/{configId}")
    public AjaxResult testConnection(@PathVariable Integer configId) {
        try {
            long startTime = System.currentTimeMillis();
            String message = lineConfigService.testLineConnection(configId);
            long responseTime = System.currentTimeMillis() - startTime;

            ConnectionTestVO result = ConnectionTestVO.builder()
                    .success(true)
                    .message(message)
                    .build();

            return success(result);
        } catch (Exception e) {
            logger.error("LINE 連線測試失敗", e);
            ConnectionTestVO result = ConnectionTestVO.builder()
                    .success(false)
                    .message("連線測試失敗")
                    .errorDetails(e.getMessage())
                    .build();
            return success(result);
        }
    }

    /**
     * 測試 Webhook 端點
     */
    @PreAuthorize("@ss.hasPermi('line:config:test')")
    @Log(title = "Webhook測試", businessType = BusinessType.OTHER)
    @PostMapping("/testWebhook")
    public AjaxResult testWebhook(@Validated @RequestBody WebhookTestDTO webhookTestDTO) {
        try {
            long startTime = System.currentTimeMillis();
            String message = lineConfigService.testWebhook(webhookTestDTO.getConfigId());
            long responseTime = System.currentTimeMillis() - startTime;

            WebhookTestVO result = WebhookTestVO.builder()
                    .success(true)
                    .message(message)
                    .statusCode(200)
                    .responseTime(responseTime)
                    .testTime(LocalDateTime.now())
                    .build();

            return success(result);
        } catch (Exception e) {
            logger.error("Webhook 測試失敗", e);
            WebhookTestVO result = WebhookTestVO.builder()
                    .success(false)
                    .message("Webhook 測試失敗")
                    .errorDetails(e.getMessage())
                    .testTime(LocalDateTime.now())
                    .build();
            return success(result);
        }
    }

    /**
     * 更新 Webhook URL
     */
    @PreAuthorize("@ss.hasPermi('line:config:edit')")
    @Log(title = "更新Webhook URL", businessType = BusinessType.UPDATE)
    @PutMapping("/webhook/{configId}")
    public AjaxResult updateWebhookUrl(@PathVariable Integer configId, @RequestParam String webhookUrl) {
        return toAjax(lineConfigService.updateWebhookUrl(configId, webhookUrl));
    }

    /**
     * 設定為預設頻道
     */
    @PreAuthorize("@ss.hasPermi('line:config:edit')")
    @Log(title = "設定預設頻道", businessType = BusinessType.UPDATE)
    @PutMapping("/setDefault/{configId}")
    public AjaxResult setAsDefault(@PathVariable Integer configId) {
        return toAjax(lineConfigService.setAsDefaultChannel(configId));
    }
}
