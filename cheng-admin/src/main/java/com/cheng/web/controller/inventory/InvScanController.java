package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.ServletUtils;
import com.cheng.common.utils.ip.IpUtils;
import com.cheng.crawler.dto.CrawledData;
import com.cheng.crawler.service.ICrawlerService;
import com.cheng.system.domain.InvScanLog;
import com.cheng.system.service.IInvScanLogService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 掃描處理控制器
 */
@RestController
@RequestMapping("/inventory/scan")
@Validated
public class InvScanController extends BaseController {

    @Autowired
    private IInvScanLogService invScanLogService;

    @Autowired
    private ICrawlerService crawlerService;

    /**
     * 接收掃描結果，記錄掃描日誌，並觸發爬蟲取得外部資料。
     */
    @PreAuthorize("@ss.hasPermi('inventory:scan:submit')")
    @Log(title = "掃描提交", businessType = BusinessType.OTHER)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody @Validated ScanRequest request) {
        // 1. 寫入掃描記錄
        InvScanLog log = new InvScanLog();
        log.setScanType(request.getScanType());
        log.setScanCode(request.getScanCode());
        log.setItemId(request.getItemId());
        log.setScanResult("0"); // 預設成功
        log.setOperatorId(getUserId());
        log.setOperatorName(getUsername());
        log.setScanTime(new Date());
        log.setIpAddress(IpUtils.getIpAddr());
        String ua = ServletUtils.getRequest() != null ? ServletUtils.getRequest().getHeader("User-Agent") : null;
        log.setUserAgent(ua);
        try {
            invScanLogService.insertInvScanLog(log);
        } catch (Exception e) {
            // 如果寫入失敗，標註為失敗但仍繼續後續流程
            log.setScanResult("1");
            log.setErrorMsg(e.getMessage());
        }

        // 2. 觸發爬蟲模組
        CrawledData data = crawlerService.crawl(request.getScanCode());

        // 3. 回傳爬蟲結果
        return AjaxResult.success(data);
    }

    /**
     * 掃描提交請求物件
     */
    public static class ScanRequest {
        /** 掃描類型（1條碼 2QR碼） */
        @NotBlank(message = "scanType不能為空")
        private String scanType;
        /** 掃描內容 */
        @NotBlank(message = "scanCode不能為空")
        private String scanCode;
        /** 可選：若前端已解析到 itemId 可帶上 */
        private Long itemId;

        public String getScanType() {
            return scanType;
        }

        public void setScanType(String scanType) {
            this.scanType = scanType;
        }

        public String getScanCode() {
            return scanCode;
        }

        public void setScanCode(String scanCode) {
            this.scanCode = scanCode;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }
    }
}
