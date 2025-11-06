package com.cheng.crawler.service.impl;

import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.crawler.dto.CrawlTaskDTO;
import com.cheng.crawler.service.IIsbnCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 爬取任務執行器（獨立類別以支援 @Async）
 *
 * @author cheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlTaskExecutor {
    
    private final IIsbnCrawlerService isbnCrawlerService;
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 非同步執行爬取任務
     *
     * @param taskId 任務 ID
     * @param taskMap 任務 Map
     */
    @Async("taskExecutor")
    public void executeTask(String taskId, Map<String, CrawlTaskDTO> taskMap) {
        CrawlTaskDTO task = taskMap.get(taskId);
        if (task == null) {
            log.error("任務不存在: taskId={}", taskId);
            return;
        }
        
        try {
            // 更新狀態為處理中
            task.setStatus(CrawlTaskDTO.TaskStatus.PROCESSING);
            log.info("開始執行爬取任務: taskId={}, isbn={}", taskId, task.getIsbn());
            
            // 執行爬取
            BookInfoDTO bookInfo = isbnCrawlerService.crawlByIsbn(task.getIsbn());
            
            if (bookInfo != null && bookInfo.getSuccess()) {
                // 成功
                task.setStatus(CrawlTaskDTO.TaskStatus.COMPLETED);
                task.setBookInfo(bookInfo);
                log.info("爬取任務完成: taskId={}, isbn={}, 書名={}", 
                        taskId, task.getIsbn(), bookInfo.getTitle());
                
                // 自動入庫（不影響任務狀態）
                try {
                    saveBookToDatabase(bookInfo);
                } catch (Exception e) {
                    log.error("自動入庫失敗: isbn={}, 錯誤={}", task.getIsbn(), e.getMessage(), e);
                    // 入庫失敗不影響爬取成功的狀態
                }
            } else {
                // 失敗
                task.setStatus(CrawlTaskDTO.TaskStatus.FAILED);
                task.setErrorMessage(bookInfo != null ? bookInfo.getErrorMessage() : "爬取失敗");
                log.warn("爬取任務失敗: taskId={}, isbn={}, 錯誤={}", 
                        taskId, task.getIsbn(), task.getErrorMessage());
            }
            
        } catch (Exception e) {
            // 異常
            task.setStatus(CrawlTaskDTO.TaskStatus.FAILED);
            task.setErrorMessage("系統錯誤: " + e.getMessage());
            log.error("爬取任務異常: taskId={}, isbn={}", taskId, task.getIsbn(), e);
        } finally {
            task.setCompleteTime(LocalDateTime.now());
        }
    }
    
    /**
     * 使用 JDBC 將書籍資料直接寫入資料庫
     *
     * @param bookInfo 書籍資訊
     */
    private void saveBookToDatabase(BookInfoDTO bookInfo) {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate 未注入，跳過自動入庫");
            return;
        }
        
        if (bookInfo == null || bookInfo.getIsbn() == null || bookInfo.getTitle() == null) {
            log.warn("書籍資訊不完整，跳過自動入庫");
            return;
        }
        
        String isbn = bookInfo.getIsbn();
        log.info("開始自動入庫: isbn={}, 書名={}", isbn, bookInfo.getTitle());
        
        try {
            // 檢查 ISBN 是否已存在
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inv_item WHERE barcode = ?", 
                Integer.class, 
                isbn
            );
            
            if (count != null && count > 0) {
                log.info("ISBN 已存在，檢查是否需要更新資料: isbn={}", isbn);
                
                // 先取得 inv_item 的 item_id
                Long itemId = jdbcTemplate.queryForObject(
                    "SELECT item_id FROM inv_item WHERE barcode = ?",
                    Long.class,
                    isbn
                );
                
                // 檢查 inv_book_info 是否存在
                Integer bookInfoCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM inv_book_info WHERE item_id = ?",
                    Integer.class,
                    itemId
                );
                
                if (bookInfoCount != null && bookInfoCount > 0) {
                    // inv_book_info 已存在，檢查是否需要更新
                    Map<String, Object> existingBook = jdbcTemplate.queryForMap(
                        "SELECT * FROM inv_book_info WHERE item_id = ?",
                        itemId
                    );
                    
                    // 比對資料完整性
                    if (shouldUpdateBookInfo(existingBook, bookInfo)) {
                        updateBookInfo(itemId, bookInfo);
                        bookInfo.setItemId(itemId);
                        log.info("書籍資料已更新（新資料更完整）: isbn={}, itemId={}", isbn, itemId);
                    } else {
                        bookInfo.setItemId(itemId);
                        log.info("現有資料已足夠完整，跳過更新: isbn={}, itemId={}", isbn, itemId);
                    }
                } else {
                    // inv_item 存在但 inv_book_info 不存在，補新增
                    log.info("inv_item 存在但 inv_book_info 不存在，補新增書籍詳細資訊: isbn={}, itemId={}", isbn, itemId);
                    insertBookInfo(itemId, bookInfo);
                    bookInfo.setItemId(itemId);
                }
                return;
            }
            
            // 新增物品資料
            String itemCode = "BOOK-" + isbn;
            String insertItemSql = "INSERT INTO inv_item " +
                "(item_code, item_name, category_id, barcode, specification, unit, brand, supplier, " +
                "description, image_url, status, remark, create_time, create_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 'system')";
            
            String specification = bookInfo.getAuthor() != null && !bookInfo.getAuthor().trim().isEmpty()
                ? "作者：" + bookInfo.getAuthor()
                : null;
            
            String publisher = bookInfo.getPublisher() != null && !bookInfo.getPublisher().trim().isEmpty()
                ? bookInfo.getPublisher()
                : null;
            
            jdbcTemplate.update(
                insertItemSql,
                itemCode,                           // item_code
                bookInfo.getTitle(),                // item_name
                1L,                                 // category_id (書籍分類)
                isbn,                               // barcode
                specification,                      // specification
                "本",                               // unit
                publisher,                          // brand
                publisher,                          // supplier
                bookInfo.getIntroduction(),         // description
                bookInfo.getCoverImagePath(),       // image_url
                "0",                                // status (正常)
                "ISBN 掃描自動建立"                  // remark
            );
            
            log.info("物品資料新增成功: isbn={}, itemCode={}", isbn, itemCode);
            
            // 取得剛新增的 item_id
            Long itemId = jdbcTemplate.queryForObject(
                "SELECT item_id FROM inv_item WHERE item_code = ?",
                Long.class,
                itemCode
            );
            
            if (itemId != null) {
                // 新增庫存資料（初始數量為 0）
                String insertStockSql = "INSERT INTO inv_stock " +
                    "(item_id, total_quantity, available_qty, borrowed_qty, reserved_qty, damaged_qty) " +
                    "VALUES (?, 0, 0, 0, 0, 0)";
                
                jdbcTemplate.update(insertStockSql, itemId);
                log.info("庫存資料新增成功: itemId={}, 初始數量=0", itemId);
                
                // 新增書籍詳細資訊
                String insertBookInfoSql = "INSERT INTO inv_book_info " +
                    "(item_id, isbn, title, author, publisher, publish_date, language, " +
                    "cover_image_path, introduction, source_url, crawl_time, status, create_by, create_time, remark) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), '0', 'system', NOW(), 'ISBN 掃描自動建立')";
                
                jdbcTemplate.update(
                    insertBookInfoSql,
                    itemId,                             // item_id
                    isbn,                               // isbn
                    bookInfo.getTitle(),                // title
                    bookInfo.getAuthor(),               // author
                    bookInfo.getPublisher(),            // publisher
                    bookInfo.getPublishDate(),          // publish_date
                    bookInfo.getLanguage(),             // language
                    bookInfo.getCoverImagePath(),       // cover_image_path
                    bookInfo.getIntroduction(),         // introduction
                    bookInfo.getSourceUrl()             // source_url
                );
                
                log.info("書籍資訊新增成功: itemId={}", itemId);
                log.info("✓✓自動入庫完成: isbn={}, itemId={}", isbn, itemId);
                
                // 回寫 itemId 到 BookInfoDTO
                bookInfo.setItemId(itemId);
            }
            
        } catch (DuplicateKeyException e) {
            log.warn("ISBN 已存在（並發新增）: isbn={}", isbn);
        } catch (Exception e) {
            log.error("自動入庫失敗: isbn={}", isbn, e);
            throw e;
        }
    }
    
    /**
     * 判斷是否應該更新書籍資訊
     * 比對新舊資料的完整性，只在新資料更完整時才返回 true
     *
     * @param existingBook 現有書籍資料
     * @param newBookInfo 新書籍資料
     * @return 是否應該更新
     */
    private boolean shouldUpdateBookInfo(Map<String, Object> existingBook, BookInfoDTO newBookInfo) {
        int existingScore = 0;
        int newScore = 0;
        
        // 計分規則：每個非空欄位 +1 分，重要欄位 +2 分
        
        // 作者
        if (isNotEmpty(existingBook.get("author"))) existingScore += 2;
        if (isNotEmpty(newBookInfo.getAuthor())) newScore += 2;
        
        // 出版社
        if (isNotEmpty(existingBook.get("publisher"))) existingScore += 2;
        if (isNotEmpty(newBookInfo.getPublisher())) newScore += 2;
        
        // 出版日期
        if (isNotEmpty(existingBook.get("publish_date"))) existingScore += 1;
        if (isNotEmpty(newBookInfo.getPublishDate())) newScore += 1;
        
        // 語言
        if (isNotEmpty(existingBook.get("language"))) existingScore += 1;
        if (isNotEmpty(newBookInfo.getLanguage())) newScore += 1;
        
        // 封面圖片
        if (isNotEmpty(existingBook.get("cover_image_path"))) existingScore += 2;
        if (isNotEmpty(newBookInfo.getCoverImagePath())) newScore += 2;
        
        // 簡介
        String existingIntro = (String) existingBook.get("introduction");
        String newIntro = newBookInfo.getIntroduction();
        if (isNotEmpty(existingIntro)) {
            existingScore += Math.min(existingIntro.length() / 50, 3); // 簡介長度加分，最多 3 分
        }
        if (isNotEmpty(newIntro)) {
            newScore += Math.min(newIntro.length() / 50, 3);
        }
        
        log.info("資料完整性比對: 現有={}, 新資料={}", existingScore, newScore);
        
        // 新資料分數更高才更新
        return newScore > existingScore;
    }
    
    /**
     * 檢查值是否非空
     */
    private boolean isNotEmpty(Object value) {
        if (value == null) return false;
        if (value instanceof String) {
            String str = ((String) value).trim();
            return !str.isEmpty() && !"null".equalsIgnoreCase(str);
        }
        return true;
    }
    
    /**
     * 更新書籍資訊
     *
     * @param itemId 物品 ID
     * @param bookInfo 新書籍資訊
     */
    private void updateBookInfo(Long itemId, BookInfoDTO bookInfo) {
        try {
            // 更新 inv_item 表
            String updateItemSql = "UPDATE inv_item SET " +
                "item_name = ?, " +
                "specification = ?, " +
                "brand = ?, " +
                "supplier = ?, " +
                "description = ?, " +
                "image_url = ?, " +
                "update_time = NOW(), " +
                "update_by = 'system' " +
                "WHERE item_id = ?";
            
            String specification = bookInfo.getAuthor() != null && !bookInfo.getAuthor().trim().isEmpty()
                ? "作者：" + bookInfo.getAuthor()
                : null;
            
            String publisher = bookInfo.getPublisher() != null && !bookInfo.getPublisher().trim().isEmpty()
                ? bookInfo.getPublisher()
                : null;
            
            jdbcTemplate.update(
                updateItemSql,
                bookInfo.getTitle(),
                specification,
                publisher,
                publisher,
                bookInfo.getIntroduction(),
                bookInfo.getCoverImagePath(),
                itemId
            );
            
            // 更新 inv_book_info 表
            String updateBookInfoSql = "UPDATE inv_book_info SET " +
                "title = ?, " +
                "author = ?, " +
                "publisher = ?, " +
                "publish_date = ?, " +
                "language = ?, " +
                "cover_image_path = ?, " +
                "introduction = ?, " +
                "source_url = ?, " +
                "crawl_time = NOW(), " +
                "update_time = NOW(), " +
                "update_by = 'system' " +
                "WHERE item_id = ?";
            
            jdbcTemplate.update(
                updateBookInfoSql,
                bookInfo.getTitle(),
                bookInfo.getAuthor(),
                bookInfo.getPublisher(),
                bookInfo.getPublishDate(),
                bookInfo.getLanguage(),
                bookInfo.getCoverImagePath(),
                bookInfo.getIntroduction(),
                bookInfo.getSourceUrl(),
                itemId
            );
            
            log.info("書籍資訊更新成功: itemId={}", itemId);
            
        } catch (Exception e) {
            log.error("更新書籍資訊失敗: itemId={}", itemId, e);
            throw e;
        }
    }
    
    /**
     * 新增書籍詳細資訊到 inv_book_info
     *
     * @param itemId 物品 ID
     * @param bookInfo 書籍資訊
     */
    private void insertBookInfo(Long itemId, BookInfoDTO bookInfo) {
        try {
            String insertBookInfoSql = "INSERT INTO inv_book_info " +
                "(item_id, isbn, title, author, publisher, publish_date, language, " +
                "cover_image_path, introduction, source_url, crawl_time, status, create_by, create_time, remark) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), '0', 'system', NOW(), 'ISBN 掃描自動建立')";
            
            jdbcTemplate.update(
                insertBookInfoSql,
                itemId,
                bookInfo.getIsbn(),
                bookInfo.getTitle(),
                bookInfo.getAuthor(),
                bookInfo.getPublisher(),
                bookInfo.getPublishDate(),
                bookInfo.getLanguage(),
                bookInfo.getCoverImagePath(),
                bookInfo.getIntroduction(),
                bookInfo.getSourceUrl()
            );
            
            log.info("書籍詳細資訊新增成功: itemId={}", itemId);
            
        } catch (Exception e) {
            log.error("新增書籍詳細資訊失敗: itemId={}", itemId, e);
            throw e;
        }
    }
}
