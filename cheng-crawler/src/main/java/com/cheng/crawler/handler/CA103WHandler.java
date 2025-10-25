package com.cheng.crawler.handler;

import com.cheng.crawler.dto.CrawlerParams;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.crawler.repository.CrawlerDataRepository;
import com.cheng.crawler.repository.GenericCrawlerRepository;
import com.cheng.crawler.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 臺灣證券交易所上市公司每日收盤價爬蟲處理器
 * 目標網址: <a href="https://www.twse.com.tw/zh/trading/historical/mi-index.html">臺灣證券交易所每日收盤行情</a>
 *
 * @author Cheng
 * @since 2025-10-25 00:33
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class CA103WHandler extends CrawlerHandler<String[], String[]> {

    private static final String TARGET_URL = "https://www.twse.com.tw/zh/trading/historical/mi-index.html";

    // 爬蟲設定
    private static final int PAGE_DELAY_SECONDS = 3; // 每頁間隔3秒
    private static final int QUERY_DELAY_SECONDS = 5; // 每次查詢間隔5秒

    // 查詢設定
    private static final String SECURITY_TYPE = "ALLBUT0999"; // 全部(不含權證、牛熊證、可展延牛熊證)

    private final GenericCrawlerRepository genericCrawlerRepository;

    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA103;
    }

    @Override
    protected void init() {
        // 初始化時建立動態 SQL 語句
        String timestampFunc = jdbcSqlTemplate.getCurrentTimestampFunction();
        registeredSql = String.format(
                "INSERT INTO CAT103 (COMPANY_NAME, COMPANY_NO, PRICE, PUBLISH_DATE, EXTRACT_DATE) " +
                        "VALUES (?, ?, ?, ?, %s)",
                timestampFunc
        );

        // 註冊 SQL 到 Repository（資料已經是 String[] 格式，直接返回即可）
        genericCrawlerRepository.registerSql(
                CrawlerType.CA103,
                registeredSql,
                data -> (String[]) data
        );

        log.info("初始化 CA103WHandler，已註冊 SQL 到 Repository");
        log.info("SQL 語句: {}", registeredSql);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected CrawlerDataRepository<String[]> getRepository() {
        return (CrawlerDataRepository<String[]>) (CrawlerDataRepository<?>) genericCrawlerRepository;
    }

    // 批次寫入設定
    private static final int BATCH_WRITE_SIZE = 100; // 每 100 筆寫入一次資料庫
    private final List<String[]> pendingBatch = new ArrayList<>(); // 待寫入的批次資料
    private int totalWritten = 0; // 已寫入的總筆數
    private String registeredSql; // 註冊的 SQL 語句

    @Override
    protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawlerParams crawlerParams) throws Exception {
        List<String[]> resultList = new ArrayList<>();
        pendingBatch.clear();
        totalWritten = 0;

        // 從參數讀取執行模式
        String mode = crawlerParams != null ? crawlerParams.getMode() : null;
        boolean todayOnly = "today-only".equalsIgnoreCase(mode);
        
        log.info("開始執行證券交易所收盤價爬蟲");
        log.info("參數資訊 - mode: {}, 模式: {}", mode, todayOnly ? "僅爬取今日資料" : "從歷史資料開始爬取");
        log.info("批次寫入設定：每 {} 筆資料立即寫入資料庫", BATCH_WRITE_SIZE);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        try {
            // 設定爬取的日期區間
            LocalDate currentDate = LocalDate.now();

            // 根據模式設定起始日期
            LocalDate startDate;
            if (todayOnly) {
                // 如果是只爬取今天，則起始日期就是今天
                startDate = currentDate;
                log.info("僅爬取今日 ({}) 的收盤行情資料", currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            } else if ("date-range".equalsIgnoreCase(mode) && crawlerParams != null) {
                // 自訂日期範圍模式
                String startDateStr = crawlerParams.getStartDate();
                if (startDateStr != null && !startDateStr.isEmpty()) {
                    startDate = LocalDate.parse(startDateStr);
                    log.info("使用自訂起始日期: {}", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                } else {
                    startDate = LocalDate.of(2016, 8, 1);
                    log.warn("未指定起始日期，使用預設值: {}", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
            } else {
                // 完整同步模式
                startDate = LocalDate.of(2016, 8, 1); // 105年8月1日
                log.info("從 {} 開始爬取到今日 ({}) 的收盤行情資料",
                        startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }

            YearMonth currentYearMonth = YearMonth.of(startDate.getYear(), startDate.getMonthValue());
            YearMonth endYearMonth = YearMonth.of(currentDate.getYear(), currentDate.getMonthValue());

            Set<String> processedStocks = new HashSet<>(); // 用於追蹤已處理的股票和日期組合
            int totalProcessed = 0;

            while (!currentYearMonth.isAfter(endYearMonth)) {
                String year = String.valueOf(currentYearMonth.getYear());
                String month = String.valueOf(currentYearMonth.getMonthValue());

                int startDay;
                int endDay;

                // 如果是僅爬取今日資料且月份是當前月份，或是第一個月的情況
                if ((todayOnly && currentYearMonth.equals(YearMonth.of(currentDate.getYear(), currentDate.getMonthValue()))) ||
                        currentYearMonth.equals(YearMonth.of(startDate.getYear(), startDate.getMonthValue()))) {
                    // 如果是今日模式則使用今天的日期，否則使用起始日
                    startDay = todayOnly ? currentDate.getDayOfMonth() : startDate.getDayOfMonth();
                } else {
                    // 其他情況，從月初開始
                    startDay = 1;
                }

                // 對於今日模式或當前月份，結束日就是今天
                if (todayOnly || currentYearMonth.equals(YearMonth.of(currentDate.getYear(), currentDate.getMonthValue()))) {
                    endDay = currentDate.getDayOfMonth();
                } else {
                    // 其他情況，爬取整個月
                    endDay = currentYearMonth.lengthOfMonth();
                }

                // 如果是今日模式，則只處理一天
                if (todayOnly) {
                    log.info("爬取 {} 年 {} 月 {} 日的收盤行情資料", year, month, startDay);
                    processDateData(driver, wait, jsExecutor, year, month, String.valueOf(startDay),
                            processedStocks, resultList);
                    break; // 只處理今天，處理完就跳出循環
                } else {
                    // 否則逐日爬取
                    for (int day = startDay; day <= endDay; day++) {
                        log.info("爬取 {} 年 {} 月 {} 日的收盤行情資料", year, month, day);
                        List<String[]> dayData = processDateData(driver, wait, jsExecutor, year, month,
                                String.valueOf(day), processedStocks, resultList);

                        totalProcessed += dayData.size();

                        // 每天處理完後等待一下，避免頻繁請求
                        if (day < endDay) {
                            TimeUnit.SECONDS.sleep(QUERY_DELAY_SECONDS);
                            log.info("處理完 {} 年 {} 月 {} 日的資料，等待 {} 秒繼續下一天...",
                                    year, month, day, QUERY_DELAY_SECONDS);
                        }
                    }

                    // 處理完當前月份，移動到下一個月份
                    currentYearMonth = currentYearMonth.plusMonths(1);

                    // 處理完一個月之後暫停一下，避免頻繁請求
                    TimeUnit.SECONDS.sleep(QUERY_DELAY_SECONDS);
                    log.info("處理完 {} 年 {} 月的資料，等待 {} 秒後繼續下一個月份...",
                            year, month, QUERY_DELAY_SECONDS);
                }
            }

            String message = todayOnly
                    ? String.format("爬蟲完成，爬取了 %s 總共 %d 筆股票收盤行情資料",
                    currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE), totalProcessed)
                    : String.format("爬蟲完成，從 %s 年 %s 月 %s 日 至今，總共處理 %d 筆股票收盤行情資料",
                    startDate.getYear(), startDate.getMonthValue(), startDate.getDayOfMonth(), totalProcessed);

            log.info(message);
            
            // 爬取完成後，寫入剩餘的資料
            if (!pendingBatch.isEmpty()) {
                log.info("爬取完成，寫入剩餘的 {} 筆資料到資料庫", pendingBatch.size());
                writeBatchToDatabase();
            }
            
            log.info("資料庫寫入統計：總共成功寫入 {} 筆資料", totalWritten);

        } catch (Exception e) {
            log.error("=>爬取臺灣證券交易所每日收盤行情資訊時發生錯誤", e);
            
            // 發生異常時，嘗試寫入剩餘資料
            if (!pendingBatch.isEmpty()) {
                log.warn("發生異常，嘗試寫入剩餘的 {} 筆資料", pendingBatch.size());
                try {
                    writeBatchToDatabase();
                } catch (Exception writeException) {
                    log.error("寫入剩餘資料時發生錯誤", writeException);
                }
            }
            
            throw e;
        }

        return resultList;
    }
    
    /**
     * 將待處理批次寫入資料庫
     */
    private void writeBatchToDatabase() {
        if (pendingBatch.isEmpty()) {
            return;
        }
        
        int batchSize = pendingBatch.size();
        log.info("========================================");
        log.info("準備寫入第 {} 批資料到資料庫，本批筆數: {}", (totalWritten / BATCH_WRITE_SIZE) + 1, batchSize);
        log.info("SQL: {}", registeredSql);
        log.info("========================================");
        
        try {
            // 記錄第一筆資料的內容（用於除錯）
            if (!pendingBatch.isEmpty()) {
                String[] firstRow = pendingBatch.get(0);
                log.info("第一筆資料內容: 公司名稱={}, 公司代碼={}, 價格={}, 發布日期={}",
                        firstRow[0], firstRow[1], firstRow[2], firstRow[3]);
            }
            
            // 使用父類的批次寫入方法（不分批，一次全寫）
            boolean success = batchSaveToDatabase(registeredSql, new ArrayList<>(pendingBatch), batchSize, 0);
            
            if (success) {
                totalWritten += batchSize;
                log.info("✅ 批次寫入成功！本批: {} 筆，累計已寫入: {} 筆", batchSize, totalWritten);
                pendingBatch.clear();
            } else {
                log.error("❌ 批次寫入失敗！本批: {} 筆", batchSize);
                log.error("失敗的資料將保留在 pendingBatch 中，筆數: {}", pendingBatch.size());
                
                // 記錄所有失敗的資料（前 5 筆）
                int displayCount = Math.min(5, pendingBatch.size());
                for (int i = 0; i < displayCount; i++) {
                    String[] row = pendingBatch.get(i);
                    log.error("失敗資料 #{}: {}", i + 1, String.join(", ", row));
                }
            }
        } catch (Exception e) {
            log.error("❌ 寫入資料庫時發生異常！本批筆數: {}", batchSize, e);
            log.error("異常類型: {}", e.getClass().getName());
            log.error("異常訊息: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("根本原因: {}", e.getCause().getMessage());
            }
            
            // 記錄堆疊追蹤的前 10 行
            StackTraceElement[] stackTrace = e.getStackTrace();
            log.error("堆疊追蹤:");
            for (int i = 0; i < Math.min(10, stackTrace.length); i++) {
                log.error("  at {}", stackTrace[i]);
            }
        }
        
        log.info("========================================\n");
    }

    /**
     * 處理特定日期的資料
     *
     * @param driver          WebDriver實例
     * @param wait            WebDriverWait實例
     * @param jsExecutor      JavascriptExecutor實例
     * @param year            年份
     * @param month           月份
     * @param day             日期
     * @param processedStocks 已處理的股票集合
     * @param resultList      結果列表
     * @return 此日期處理的資料列表
     */
    private List<String[]> processDateData(WebDriver driver, WebDriverWait wait, JavascriptExecutor jsExecutor,
                                           String year, String month, String day,
                                           Set<String> processedStocks, List<String[]> resultList)
            throws InterruptedException {

        List<String[]> dayResultList = new ArrayList<>();

        // 每次查詢前重新導航到目標頁面，確保頁面乾淨
        driver.get(TARGET_URL);

        // 等待日期選擇框載入
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form select[name='yy']")));

        // 設定日期
        Select yearSelect = new Select(driver.findElement(By.cssSelector("select[name='yy']")));
        yearSelect.selectByValue(year);

        Select monthSelect = new Select(driver.findElement(By.cssSelector("select[name='mm']")));
        monthSelect.selectByValue(month);

        Select daySelect = new Select(driver.findElement(By.cssSelector("select[name='dd']")));
        daySelect.selectByValue(day);

        // 選擇分類
        Select typeSelect = new Select(driver.findElement(By.cssSelector("select[name='type']")));
        typeSelect.selectByValue(SECURITY_TYPE);

        // 點擊查詢按鈕
        WebElement searchButton = driver.findElement(By.cssSelector("button.search"));
        searchButton.click();

        // 等待查詢結果載入
        TimeUnit.SECONDS.sleep(QUERY_DELAY_SECONDS);
        log.info("等待查詢結果載入，等待 {} 秒...", QUERY_DELAY_SECONDS);

        // 向下捲動頁面，直到找到目標表格
        scrollToTable8(driver, jsExecutor, wait);

        // 如果找不到表格，可能是當天無資料，直接返回空列表
        if (driver.findElements(By.cssSelector("div.table.active#table8")).isEmpty()) {
            log.warn("找不到資料表格 table8，可能當天無資料: {} 年 {} 月 {} 日", year, month, day);
            return dayResultList;
        }

        // 等待並定位目標表格 (id="table8")
        WebElement table8 = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.table.active#table8")));

        // 從表格標題中取得日期
        WebElement titleElement = table8.findElement(By.cssSelector("h2"));
        String title = titleElement.getText().trim();
        log.info("成功找到標題: {}", title);

        // 使用正則表達式從標題中提取日期
        String datePattern = "(\\d+)年(\\d+)月(\\d+)日";
        Pattern pattern = Pattern.compile(datePattern);
        Matcher matcher = pattern.matcher(title);

        String publishDate;
        if (matcher.find()) {
            // 從標題中提取日期部分
            String dateText = matcher.group(0); // 例如 "114年03月26日"
            publishDate = TimeUtil.convertTaiwanDate(dateText);
            log.info("成功解析發布日期: {}", publishDate);
        } else {
            // 如果無法從標題解析日期，則使用傳入的日期
            LocalDate date = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
            publishDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            log.warn("無法從標題解析日期，使用指定日期: {}", publishDate);
        }

        // 處理分頁資料
        boolean hasNextPage = true;
        int currentPage = 1;

        while (hasNextPage) {
            log.info("正在處理 {} 的第 {} 頁收盤行情資料", publishDate, currentPage);
            List<String[]> pageData = new ArrayList<>();

            // 等待資料表格內容載入
            WebElement tableElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.table.active#table8 div.rwd-table table")));

            // 再等一下以確保資料完全載入
            TimeUnit.SECONDS.sleep(2);

            // 取得表格中所有股票行
            List<WebElement> rows = tableElement.findElements(By.cssSelector("tbody tr"));
            if (rows.isEmpty()) {
                log.info("當前頁面無股票資料");
                hasNextPage = false;
                continue;
            }

            log.info("本頁共有 {} 筆股票資料", rows.size());

            for (WebElement row : rows) {
                try {
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.size() < 9) { // 確保至少有足夠的欄位
                        continue;
                    }

                    // 取得股票資訊（只取證券代號、證券名稱、收盤價）
                    String stockCode = cells.get(0).getText().trim(); // 證券代號
                    String stockName = cells.get(1).getText().trim(); // 證券名稱
                    String closingPrice = cells.get(8).getText().trim(); // 收盤價

                    // 使用股票代碼+日期作為唯一識別
                    String uniqueKey = stockCode + "_" + publishDate;

                    // 跳過已處理過的股票代碼+日期組合
                    if (processedStocks.contains(uniqueKey)) {
                        continue;
                    }

                    // 跳過收盤價為空或非數字的資料
                    if (closingPrice.isEmpty() || !isValidPrice(closingPrice)) {
                        log.warn("股票 {} ({}) 的收盤價 '{}' 無效，跳過", stockCode, stockName, closingPrice);
                        continue;
                    }

                    // 將收盤價轉換為浮點數
                    double price = parsePrice(closingPrice);

                    // 準備資料庫資料
                    String[] data = new String[]{
                            stockName,     // COMPANY_NAME
                            stockCode,     // COMPANY_NO
                            String.valueOf(price), // PRICE
                            publishDate    // PUBLISH_DATE
                    };

                    pageData.add(data);
                    processedStocks.add(uniqueKey); // 記錄已處理的股票代碼+日期組合

                    log.debug("已處理股票: 代碼={}, 名稱={}, 收盤價={}, 日期={}",
                            stockCode, stockName, price, publishDate);
                } catch (Exception e) {
                    log.error("處理股票行資料時出錯", e);
                }
            }

            // 將此頁資料加入結果列表和待寫入批次
            if (!pageData.isEmpty()) {
                log.info("正在將 {} 的第 {} 頁的 {} 筆資料加入結果列表",
                        publishDate, currentPage, pageData.size());
                dayResultList.addAll(pageData);
                resultList.addAll(pageData);
                pendingBatch.addAll(pageData);
                
                // 檢查是否達到批次寫入條件
                if (pendingBatch.size() >= BATCH_WRITE_SIZE) {
                    writeBatchToDatabase();
                }
            }

            // 檢查是否有下一頁
            try {
                // 在翻頁前停留，避免頻繁請求
                TimeUnit.SECONDS.sleep(PAGE_DELAY_SECONDS);
                log.info("等待一段時間再進行翻頁，等待 {} 秒...", PAGE_DELAY_SECONDS);

                // 尋找下一頁按鈕（在 div.table.active#table8 內）
                List<WebElement> nextPageLinks = driver.findElements(
                        By.cssSelector("div.table.active#table8 div.pagination a.page-link.next"));

                if (!nextPageLinks.isEmpty() && nextPageLinks.get(0).isDisplayed() && nextPageLinks.get(0).isEnabled()) {
                    // 檢查是否真的可以點擊（有時按鈕雖然存在但已被禁用）
                    String nextBtnClass = nextPageLinks.get(0).getAttribute("class");
                    if (nextBtnClass != null && nextBtnClass.contains("disabled")) {
                        log.info("下一頁按鈕已被禁用，無法繼續翻頁");
                        hasNextPage = false;
                    } else {
                        // 點擊下一頁（使用 JavaScript 確保可以點擊到）
                        jsExecutor.executeScript("arguments[0].click();", nextPageLinks.get(0));

                        // 等待新頁面載入
                        TimeUnit.SECONDS.sleep(3);
                        log.info("等待新頁面載入，等待 3 秒...");
                        currentPage++;
                    }
                } else {
                    log.info("沒有找到下一頁按鈕或按鈕不可用，結束分頁處理");
                    hasNextPage = false;
                }
            } catch (Exception e) {
                log.error("處理下一頁時發生錯誤，結束分頁處理: {}", e.getMessage());
                hasNextPage = false;
            }
        }

        return dayResultList;
    }

    /**
     * 向下捲動頁面直到找到目標表格 table8
     *
     * @param driver     WebDriver 實例
     * @param jsExecutor JavascriptExecutor 實例
     * @param wait       WebDriverWait 實例
     * @throws InterruptedException 如果等待過程中被中斷
     */
    private void scrollToTable8(WebDriver driver, JavascriptExecutor jsExecutor, WebDriverWait wait) throws InterruptedException {
        log.info("開始向下捲動頁面尋找目標表格 table8");

        // 嘗試尋找 table8 元素
        List<WebElement> table8Elements = driver.findElements(By.cssSelector("div.table#table8"));

        // 如果找不到 table8，則執行捲動操作
        if (table8Elements.isEmpty()) {
            int maxScrollAttempts = 10;
            for (int i = 0; i < maxScrollAttempts; i++) {
                // 向下捲動頁面
                jsExecutor.executeScript("window.scrollBy(0, 500);");
                log.info("向下捲動頁面 500 像素，第 {} 次嘗試", i + 1);

                // 等待 1s 讓頁面反應
                TimeUnit.SECONDS.sleep(1);

                // 再次檢查是否已經找到 table8
                table8Elements = driver.findElements(By.cssSelector("div.table#table8"));
                if (!table8Elements.isEmpty()) {
                    log.info("已找到 table8 元素，停止捲動");
                    break;
                }
            }

            // 如果經過多次捲動仍找不到 table8，嘗試直接捲動到頁面底部
            if (table8Elements.isEmpty()) {
                log.info("多次捲動後仍未找到 table8，嘗試直接捲動到頁面底部");
                jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                TimeUnit.SECONDS.sleep(2);
            }
        }

        // 檢查是否找到 table8，如未找到則使用等待方式再次嘗試
        if (driver.findElements(By.cssSelector("div.table#table8")).isEmpty()) {
            log.info("無法通過捲動找到 table8，嘗試等待元素出現");
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.table#table8")));
                log.info("成功等待到 table8 元素出現");
            } catch (Exception e) {
                log.warn("等待 table8 元素超時，將嘗試繼續處理", e);
            }
        }
    }

    /**
     * 判斷價格是否有效
     */
    private boolean isValidPrice(String priceStr) {
        try {
            // 移除可能的千位分隔符號
            String cleanPrice = priceStr.replace(",", "");
            Double.parseDouble(cleanPrice);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 解析價格字串為浮點數
     */
    private double parsePrice(String priceStr) {
        try {
            // 移除可能的千位分隔符號
            String cleanPrice = priceStr.replace(",", "");
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            log.error("解析價格 '{}' 出錯: {}", priceStr, e.getMessage());
            return 0.0;
        }
    }

    @Override
    protected List<String[]> processData(List<String[]> rawData, CrawlerParams params) throws Exception {
        // 資料已經是 String[] 格式，直接返回
        return rawData;
    }
}
