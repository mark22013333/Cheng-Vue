package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.ServletUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.ip.IpUtils;
import com.cheng.crawler.dto.CrawledData;
import com.cheng.crawler.service.ICrawlerService;
import com.cheng.system.domain.InvItem;
import com.cheng.system.domain.InvScanLog;
import com.cheng.system.service.IBookItemService;
import com.cheng.system.service.IInvScanLogService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 掃描處理控制器
 *
 * @author cheng
 */
@RestController
@RequestMapping("/inventory/scan")
@Validated
public class InvScanController extends BaseController {

    @Autowired
    private IInvScanLogService invScanLogService;

    @Autowired
    private ICrawlerService crawlerService;

    @Autowired
    private IBookItemService bookItemService;

    /**
     * 接收掃描結果，記錄掃描日誌，並觸發爬蟲取得外部資料。
     */
    @PreAuthorize("@ss.hasPermi('inventory:scan:use')")
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
     * ISBN 掃描處理（專門用於書籍）
     * 1. 驗證 ISBN 格式
     * 2. 檢查書籍是否已存在
     * 3. 不存在則爬取並建立書籍物品
     * 4. 回傳書籍物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:scan:use')")
    @Log(title = "ISBN掃描", businessType = BusinessType.INSERT)
    @PostMapping("/isbn")
    public AjaxResult scanIsbn(@RequestBody @Validated IsbnScanRequest request) {
        try {
            String isbn = request.getIsbn().trim();

            // 1. 驗證 ISBN 格式（簡單驗證：10位或13位數字）
            if (!isValidIsbn(isbn)) {
                return AjaxResult.error("ISBN 格式不正確，請輸入10位或13位數字");
            }

            // 2. 寫入掃描記錄
            InvScanLog log = new InvScanLog();
            log.setScanType("1"); // 條碼類型
            log.setScanCode(isbn);
            log.setScanResult("0");
            log.setOperatorId(getUserId());
            log.setOperatorName(getUsername());
            log.setScanTime(new Date());
            log.setIpAddress(IpUtils.getIpAddr());
            String ua = ServletUtils.getRequest() != null ? ServletUtils.getRequest().getHeader("User-Agent") : null;
            log.setUserAgent(ua);

            try {
                invScanLogService.insertInvScanLog(log);
            } catch (Exception e) {
                logger.warn("寫入掃描記錄失敗: {}", e.getMessage());
            }

            // 3. 建立或取得書籍物品
            InvItem bookItem = bookItemService.createOrGetBookItem(isbn);

            // 4. 組裝回傳資料
            Map<String, Object> result = new HashMap<>();
            result.put("item", bookItem);
            result.put("message", bookItem.getCreateTime().getTime() > System.currentTimeMillis() - 5000
                    ? "書籍建立成功" : "書籍已存在");

            return AjaxResult.success(result);

        } catch (Exception e) {
            logger.error("ISBN 掃描處理失敗: {}", e.getMessage(), e);
            return AjaxResult.error("處理失敗: " + e.getMessage());
        }
    }

    /**
     * 驗證 ISBN 格式
     */
    private boolean isValidIsbn(String isbn) {
        if (StringUtils.isEmpty(isbn)) {
            return false;
        }
        // 移除可能的連字號或空格
        String cleanIsbn = isbn.replaceAll("[-\\s]", "");
        // 檢查是否為10位或13位數字
        return cleanIsbn.matches("^\\d{10}$") || cleanIsbn.matches("^\\d{13}$");
    }

    /**
     * 掃描提交請求物件
     */
    public static class ScanRequest {
        /**
         * 掃描類型（1條碼 2QR碼）
         */
        @NotBlank(message = "scanType不能為空")
        private String scanType;
        /**
         * 掃描內容
         */
        @NotBlank(message = "scanCode不能為空")
        private String scanCode;
        /**
         * 可選：若前端已解析到 itemId 可帶上
         */
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

    /**
     * ISBN 掃描請求物件
     */
    public static class IsbnScanRequest {
        /**
         * ISBN 編號
         */
        @NotBlank(message = "ISBN不能為空")
        private String isbn;

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }
    }
}
