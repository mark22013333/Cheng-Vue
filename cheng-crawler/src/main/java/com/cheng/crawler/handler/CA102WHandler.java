package com.cheng.crawler.handler;

import com.cheng.crawler.CrawlerHandler;
import com.cheng.crawler.dto.CrawledData;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.crawler.repository.CrawlerDataRepository;
import com.cheng.crawler.repository.GenericCrawlerRepository;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CA102 範例爬蟲處理器（泛型：String[] -> Map<String, String>）
 * 示範如何建立其他類型的爬蟲，資料會儲存到 ca102_data 資料表
 * <p>
 * 這是一個範例實作，展示如何擴展爬蟲系統
 *
 * @author Cheng
 * @since 2025-10-19
 **/
@Slf4j
@Component
public class CA102WHandler extends CrawlerHandler<String[], Map<String, String>> {

    @Autowired
    private GenericCrawlerRepository genericCrawlerRepository;

    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA102;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected CrawlerDataRepository<Map<String, String>> getRepository() {
        // GenericCrawlerRepository 使用 Object 泛型，需要強制轉換
        return (CrawlerDataRepository<Map<String, String>>) (CrawlerDataRepository<?>) genericCrawlerRepository;
    }

    @Override
    protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawledData crawledData) throws Exception {
        // 範例：爬取某個網站的資料
        String targetUrl = crawledData.getInput();
        if (targetUrl == null || targetUrl.isEmpty()) {
            log.warn("[CA102] 目標 URL 為空");
            return new ArrayList<>();
        }

        log.info("[CA102] 開始爬取網站: {}", targetUrl);
        driver.get(targetUrl);

        // 等待頁面載入
        Thread.sleep(2000);

        // 範例：爬取頁面中的資料
        List<String[]> rawData = new ArrayList<>();

        // 假設爬取某些元素
        // List<WebElement> elements = driver.findElements(By.cssSelector(".data-item"));
        // for (WebElement element : elements) {
        //     String title = element.findElement(By.cssSelector(".title")).getText();
        //     String content = element.findElement(By.cssSelector(".content")).getText();
        //     rawData.add(new String[]{title, content});
        // }

        log.info("[CA102] 爬取完成，共 {} 筆原始資料", rawData.size());
        return rawData;
    }

    @Override
    protected List<Map<String, String>> processData(List<String[]> rawData, CrawledData crawledData) throws Exception {
        List<Map<String, String>> processedData = new ArrayList<>();

        for (String[] data : rawData) {
            if (data != null && data.length >= 2) {
                // 將原始資料轉換為業務物件（類型安全）
                Map<String, String> item = new HashMap<>();
                item.put("title", data[0]);
                item.put("content", data[1]);
                item.put("crawl_time", String.valueOf(System.currentTimeMillis()));

                processedData.add(item);
            }
        }

        log.info("[CA102] 資料處理完成，共 {} 筆", processedData.size());
        return processedData;
    }

    @Override
    protected void beforeCrawl(CrawledData crawledData) throws Exception {
        log.info("[CA102] 初始化爬蟲環境");
        // 可以在這裡進行一些初始化工作，例如：
        // - 檢查參數有效性
        // - 準備資料庫連線
        // - 載入配置
    }

    @Override
    protected void afterCrawl(CrawledData crawledData, boolean success) {
        if (success) {
            log.info("[CA102] 爬蟲任務成功完成");
            // 可以在這裡進行一些清理工作，例如：
            // - 發送通知
            // - 更新統計資料
            // - 清理暫存檔案
        } else {
            log.error("[CA102] 爬蟲任務失敗，需要檢查");
            // 可以在這裡進行錯誤處理，例如：
            // - 發送告警
            // - 記錄失敗原因
            // - 回滾部分操作
        }
    }
}
