package com.cheng.crawler.repository;

import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.crawler.dto.CrawledData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 書籍庫存資料儲存實作（泛型：BookInfoDTO）
 * 處理書籍類型爬蟲的資料儲存，寫入 inv_xxx 相關資料表
 *
 * @author Cheng
 * @since 2025-10-19
 */
@Slf4j
@Repository
public class BookInventoryRepository implements CrawlerDataRepository<BookInfoDTO> {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<BookInfoDTO> dataList, CrawledData crawledData) throws Exception {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("書籍資料列表為空，跳過儲存");
            return 0;
        }
        
        int savedCount = 0;
        for (BookInfoDTO data : dataList) {
            try {
                if (save(data, crawledData)) {
                    savedCount++;
                }
            } catch (Exception e) {
                log.error("儲存書籍資料失敗: {}", data, e);
                // 繼續處理下一筆，不中斷整個批次
            }
        }
        
        log.info("書籍資料批次儲存完成，成功: {}/{}", savedCount, dataList.size());
        return savedCount;
    }
    
    @Override
    public boolean save(BookInfoDTO book, CrawledData crawledData) throws Exception {
        // TODO: 根據實際的書籍資料結構實作儲存邏輯
        // 範例（類型安全，不需要 instanceof 檢查）：
        //
        // // 1. 檢查書籍是否已存在
        // String checkSql = "SELECT COUNT(*) FROM inv_book_info WHERE isbn = ?";
        // Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, book.getIsbn());
        // 
        // if (count != null && count > 0) {
        //     // 更新現有書籍
        //     String updateSql = "UPDATE inv_book_info SET title = ?, author = ?, " +
        //             "publisher = ?, publish_date = ?, update_time = NOW() WHERE isbn = ?";
        //     jdbcTemplate.update(updateSql, book.getTitle(), book.getAuthor(),
        //             book.getPublisher(), book.getPublishDate(), book.getIsbn());
        //     log.debug("更新書籍資訊: {}", book.getIsbn());
        // } else {
        //     // 新增書籍
        //     String insertSql = "INSERT INTO inv_book_info (isbn, title, author, publisher, " +
        //             "publish_date, source_url, create_time, update_time) " +
        //             "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        //     jdbcTemplate.update(insertSql, book.getIsbn(), book.getTitle(), book.getAuthor(),
        //             book.getPublisher(), book.getPublishDate(), book.getSourceUrl());
        //     log.debug("新增書籍資訊: {}", book.getIsbn());
        //     
        //     // 同時建立庫存記錄
        //     String invSql = "INSERT INTO inv_item_stock (item_id, quantity, create_time) " +
        //             "SELECT item_id, 0, NOW() FROM inv_book_item WHERE isbn = ?";
        //     jdbcTemplate.update(invSql, book.getIsbn());
        // }
        // 
        // return true;
        
        log.warn("BookInventoryRepository.save() 方法尚未實作");
        return false;
    }
}
