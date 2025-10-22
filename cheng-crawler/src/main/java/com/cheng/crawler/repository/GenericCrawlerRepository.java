package com.cheng.crawler.repository;

import com.cheng.crawler.enums.CrawlerType;
import com.cheng.crawler.utils.JdbcSqlTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用爬蟲資料儲存實作（泛型：Object - 保持最大靈活性）
 * 處理非書籍類型爬蟲的資料儲存，根據 CrawlerType 寫入對應的 CAxxx 資料表
 * <p>
 * 核心功能：
 * 1. 支援 JDBC 批次寫入（高效能）
 * 2. 支援動態 SQL 註冊（每個爬蟲註冊自己的 SQL）
 * 3. 支援資料轉換（業務物件 -> String[]）
 * <p>
 * 使用方式：
 * <pre>
 * // 在 Handler 的 init() 中註冊 SQL
 * genericCrawlerRepository.registerSql(
 *     CrawlerType.CA102,
 *     sql,
 *     dto -> new String[]{dto.getField1(), dto.getField2()}
 * );
 * </pre>
 *
 * @author Cheng
 * @since 2025-10-19
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class GenericCrawlerRepository implements CrawlerDataRepository<Object> {

    private final JdbcSqlTemplate jdbcSqlTemplate;

    /**
     * 儲存每個 CrawlerType 的 SQL 語句
     * Key: CrawlerType, Value: SQL 語句
     */
    private final Map<CrawlerType, String> sqlRegistry = new ConcurrentHashMap<>();

    /**
     * 儲存每個 CrawlerType 的資料轉換器
     * Key: CrawlerType, Value: 資料轉換函數
     */
    private final Map<CrawlerType, DataConverter<Object>> converterRegistry = new ConcurrentHashMap<>();

    /**
     * 資料轉換器介面
     * 將業務物件轉換為 String[] 以便 JDBC 批次寫入
     */
    @FunctionalInterface
    public interface DataConverter<T> {
        String[] convert(T data);
    }

    /**
     * 註冊爬蟲的 SQL 語句和資料轉換器
     *
     * @param crawlerType 爬蟲類型
     * @param sql         INSERT SQL 語句
     * @param converter   資料轉換器（業務物件 -> String[]）
     */
    public void registerSql(CrawlerType crawlerType, String sql, DataConverter<Object> converter) {
        sqlRegistry.put(crawlerType, sql);
        converterRegistry.put(crawlerType, converter);
        log.info("已註冊 {} 的 SQL 和資料轉換器", crawlerType.name());
    }

    /**
     * 僅註冊 SQL（不註冊轉換器，適用於原始資料已經是 String[] 的情況）
     */
    public void registerSql(CrawlerType crawlerType, String sql) {
        sqlRegistry.put(crawlerType, sql);
        log.info("已註冊 {} 的 SQL", crawlerType.name());
    }

    @Override
    public int batchSave(List<Object> dataList, CrawlerType crawlerType) throws Exception {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("資料列表為空，跳過儲存");
            return 0;
        }

        if (crawlerType == null) {
            log.error("CrawlerType 為空，無法儲存資料");
            return 0;
        }

        // 檢查是否已註冊 SQL
        String sql = sqlRegistry.get(crawlerType);
        if (sql == null) {
            log.error("[{}] 未註冊 SQL 語句，無法儲存。請在 Handler 的 init() 方法中呼叫 registerSql()",
                    crawlerType.name());
            return 0;
        }

        // 轉換資料
        List<String[]> convertedData = convertData(crawlerType, dataList);
        if (convertedData.isEmpty()) {
            log.warn("[{}] 資料轉換後為空，跳過儲存", crawlerType.name());
            return 0;
        }

        // 使用 JDBC 批次寫入
        boolean success = batchSaveToDatabase(crawlerType, sql, convertedData);
        if (success) {
            log.info("[{}] 批次儲存成功，共 {} 筆", crawlerType.name(), convertedData.size());
            return convertedData.size();
        } else {
            log.error("[{}] 批次儲存失敗", crawlerType.name());
            return 0;
        }
    }

    /**
     * 轉換資料列表
     * 將業務物件轉換為 String[] 格式
     */
    private List<String[]> convertData(CrawlerType crawlerType, List<Object> dataList) {
        List<String[]> result = new ArrayList<>();
        DataConverter<Object> converter = converterRegistry.get(crawlerType);

        for (Object data : dataList) {
            try {
                String[] converted;
                if (converter != null) {
                    // 使用註冊的轉換器
                    converted = converter.convert(data);
                } else if (data instanceof String[]) {
                    // 資料本身就是 String[]
                    converted = (String[]) data;
                } else {
                    log.warn("[{}] 資料類型不支援且未註冊轉換器: {}",
                            crawlerType.name(), data.getClass().getName());
                    continue;
                }

                if (converted != null && converted.length > 0) {
                    result.add(converted);
                }
            } catch (Exception e) {
                log.error("[{}] 資料轉換失敗: {}", crawlerType.name(), data, e);
                // 繼續處理下一筆
            }
        }

        return result;
    }

    /**
     * 批次寫入資料庫
     * 使用 JdbcSqlTemplate 提供的高效能批次寫入功能
     */
    private boolean batchSaveToDatabase(CrawlerType crawlerType, String sql, List<String[]> data) {
        try {
            return jdbcSqlTemplate.insertBatchSql(sql, data);
        } catch (Exception e) {
            log.error("[{}] JDBC 批次寫入失敗", crawlerType.name(), e);
            return false;
        }
    }

    @Override
    public boolean save(Object data, CrawlerType crawlerType) {
        // 單筆儲存：轉換為列表後呼叫批次儲存
        try {
            List<Object> dataList = new ArrayList<>();
            dataList.add(data);
            return batchSave(dataList, crawlerType) > 0;
        } catch (Exception e) {
            log.error("單筆儲存失敗", e);
            return false;
        }
    }

    /**
     * 取得已註冊的 SQL 語句（僅供查詢用）
     */
    public String getRegisteredSql(CrawlerType crawlerType) {
        return sqlRegistry.get(crawlerType);
    }

    /**
     * 檢查是否已註冊 SQL
     */
    public boolean isSqlRegistered(CrawlerType crawlerType) {
        return sqlRegistry.containsKey(crawlerType);
    }
}
