package com.cheng.web.controller.inventory;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.ServletUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.ip.IpUtils;
import com.cheng.crawler.handler.CrawlerHandler;
import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.system.domain.InvItem;
import com.cheng.system.domain.InvScanLog;
import com.cheng.system.dto.InvItemWithStockDTO;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.service.IBookItemService;
import com.cheng.system.service.IInvScanLogService;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
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
@Validated
@RestController
@RequestMapping("/inventory/scan")
@RequiredArgsConstructor
public class InvScanController extends BaseController {

    private final IInvScanLogService invScanLogService;
    private final IBookItemService bookItemService;
    private final InvItemMapper invItemMapper;

    /**
     * ISBN 掃描處理（專門用於書籍）
     * 1. 驗證 ISBN 格式
     * 2. 透過 CA101WHandler 爬取書籍資訊
     * 3. 透過 BookItemService 儲存到資料庫
     * 4. 回傳書籍物品資訊
     */
    @PreAuthorize("@ss.hasPermi('inventory:scan:use')")
    @Log(title = "ISBN掃描", businessType = BusinessType.INSERT)
    @PostMapping("/isbn")
    public AjaxResult scanIsbn(@RequestBody @Validated IsbnScanRequest request) {
        String isbn = request.getIsbn().trim();

        try {
            // 1. 驗證 ISBN 格式
            if (!isValidIsbn(isbn)) {
                return AjaxResult.error("ISBN 格式不正確，請輸入10位或13位數字");
            }

            // 2. 寫入掃描記錄
            saveScanLog(isbn);

            // 3. 透過 CA101 Handler 爬取書籍資訊
            CrawlerHandler<?, ?> handler = CrawlerHandler.getHandler(CrawlerType.CA101);
            if (handler == null) {
                return AjaxResult.error("CA101 爬蟲服務未初始化");
            }

            BookInfoDTO bookInfo = handler.crawlSingleAndSave(isbn, BookInfoDTO.class);

            // 4. 透過 BookItemService 儲存到資料庫
            InvItem bookItem = bookItemService.createOrGetBookItem(isbn, bookInfo);

            // 5. 查詢包含庫存資訊的完整資料
            InvItemWithStockDTO itemWithStock = invItemMapper.selectItemWithStockByItemId(bookItem.getItemId());

            // 6. 計算庫存狀態和價值
            if (itemWithStock != null) {
                itemWithStock.calculateStockStatus();
                itemWithStock.calculateStockValue();
            }

            // 7. 判斷是新建還是已存在
            boolean isNewItem = bookItem.getCreateTime() != null &&
                    bookItem.getCreateTime().getTime() > System.currentTimeMillis() - 5000;

            // 8. 組裝回傳資料
            Map<String, Object> result = new HashMap<>();
            result.put("item", itemWithStock != null ? itemWithStock : bookItem);
            result.put("message", isNewItem ? "書籍建立成功" : "書籍已存在");
            result.put("isNew", isNewItem);

            return AjaxResult.success(result);

        } catch (Exception e) {
            logger.error("ISBN 掃描處理失敗 - ISBN: {}, 錯誤: {}", isbn, e.getMessage(), e);
            return AjaxResult.error("處理失敗: " + e.getMessage());
        }
    }

    /**
     * 儲存掃描記錄
     */
    private void saveScanLog(String isbn) {
        try {
            InvScanLog log = new InvScanLog();
            log.setScanType("1"); // 條碼類型
            log.setScanCode(isbn);
            log.setScanResult("0"); // 成功
            log.setOperatorId(getUserId());
            log.setOperatorName(getUsername());
            log.setScanTime(new Date());
            log.setIpAddress(IpUtils.getIpAddr());
            log.setUserAgent(ServletUtils.getRequest().getHeader(HttpHeaders.USER_AGENT));

            invScanLogService.insertInvScanLog(log);
        } catch (Exception e) {
            logger.warn("寫入掃描記錄失敗: {}", e.getMessage());
            // 不中斷主流程
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
    @Setter
    @Getter
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
    }

    /**
     * ISBN 掃描請求物件
     */
    @Setter
    @Getter
    public static class IsbnScanRequest {
        /**
         * ISBN 編號
         */
        @NotBlank(message = "ISBN不能為空")
        private String isbn;
    }
}
