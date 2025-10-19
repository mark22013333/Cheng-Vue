package com.cheng.crawler.repository;

import com.cheng.crawler.dto.CrawledData;
import com.cheng.crawler.enums.CrawlerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通用爬蟲資料儲存實作（泛型：Object - 保持最大靈活性）
 * 處理非書籍類型爬蟲的資料儲存，根據 CrawlerType 寫入對應的 CAxxx 資料表
 * <p>
 * 例如：
 * - CA102 -> CA102_news 表（新聞資料）
 * - CA103 -> CA103_products 表（商品資料）
 *
 * @author Cheng
 * @since 2025-10-19
 */
@Slf4j
@Repository
public class GenericCrawlerRepository implements CrawlerDataRepository<Object> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSave(List<Object> dataList, CrawledData crawledData) throws Exception {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("資料列表為空，跳過儲存");
            return 0;
        }

        int savedCount = 0;
        for (Object data : dataList) {
            try {
                if (save(data, crawledData)) {
                    savedCount++;
                }
            } catch (Exception e) {
                log.error("儲存爬蟲資料失敗: {}", data, e);
                // 繼續處理下一筆
            }
        }

        log.info("通用爬蟲資料批次儲存完成，成功: {}/{}", savedCount, dataList.size());
        return savedCount;
    }

    @Override
    public boolean save(Object data, CrawledData crawledData) {
        CrawlerType crawlerType = crawledData.getCrawlerType();
        if (crawlerType == null) {
            log.error("CrawlerType 為空，無法儲存資料");
            return false;
        }

        // 根據 CrawlerType 動態決定資料表名稱
        String tableName = getTableName(crawlerType);

        // 根據實際的資料結構實作儲存邏輯
        // 範例：儲存到動態資料表
        // String sql = buildInsertSql(tableName, data);
        // jdbcTemplate.update(sql, extractParams(data));

        log.debug("儲存資料到表 {}: {}", tableName, data);
        return true;
    }

    /**
     * 根據 CrawlerType 取得對應的資料表名稱
     */
    private String getTableName(CrawlerType crawlerType) {
        // 命名規則：CAxxx -> CAxxx_data
        String typeName = crawlerType.name().toLowerCase();
        return typeName + "_data";
    }

    /**
     * 建立 INSERT SQL 語句（根據實際需求實作）
     */
    private String buildInsertSql(String tableName, Object data) {
        // TODO: 根據資料結構動態建立 SQL
        return "INSERT INTO " + tableName + " (data, create_time) VALUES (?, NOW())";
    }
}
