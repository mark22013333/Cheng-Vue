package com.cheng.crawler.handler;

import com.cheng.crawler.CrawlerHandler;
import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.crawler.dto.BookRawDTO;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.crawler.repository.CrawlerDataRepository;
import com.cheng.crawler.service.IIsbnCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CA101 書籍爬蟲處理器（泛型：BookRawDTO -> BookInfoDTO）
 * 負責爬取 ISBN 書籍資訊（不涉及資料庫操作）
 * <p>
 * 職責：
 * - 呼叫三層備援爬蟲服務取得書籍資料
 * - 回傳 BookInfoDTO 給呼叫方
 * - 資料儲存由 system 模組的 Service 層處理
 *
 * @author Cheng
 * @since 2025-10-02 00:21
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class CA101WHandler extends CrawlerHandler<BookRawDTO, BookInfoDTO> {

    private final IIsbnCrawlerService isbnCrawlerService;

    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA101;
    }

    @Override
    protected CrawlerDataRepository<BookInfoDTO> getRepository() {
        // CA101 只負責爬取，不直接操作資料庫
        return null;
    }

    /**
     * 單一 ISBN 查詢核心邏輯（只爬取，不儲存）
     * 回傳 BookInfoDTO（類型安全，不需要轉換）
     *
     * @param isbn ISBN 編號
     * @return 爬取的書籍資訊（BookInfoDTO）
     */
    @Override
    protected BookInfoDTO crawlSingle(String isbn) {
        log.info("[CA101] 開始爬取 ISBN: {}", isbn);

        // 爬取書籍資訊
        BookInfoDTO bookInfo = isbnCrawlerService.crawlByIsbn(isbn);

        if (bookInfo == null || !bookInfo.getSuccess()) {
            String errorMsg = bookInfo != null ? bookInfo.getErrorMessage() : "爬取失敗";
            throw new RuntimeException("無法取得 ISBN 書籍資訊: " + errorMsg);
        }

        // 直接回傳 BookInfoDTO，類型轉換由父類統一處理
        return bookInfo;
    }

    // ==================== 批次處理方法（未實作）====================
    // CA101 目前僅支援單一查詢模式（crawlSingleAndSave）
    // 如果未來需要批次處理，可以實作以下方法：
    // - crawlWebsiteFetchData()
    // - processData()
}
