package com.cheng.crawler.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.crawler.config.CrawlerProperties;
import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.crawler.service.IIsbnCrawlerService;
import com.cheng.crawler.utils.FlareSolverUtil;
import com.cheng.crawler.utils.ImageDownloadUtil;
import com.cheng.crawler.utils.SeleniumUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ISBN 書籍資訊爬蟲服務實現
 *
 * @author cheng
 */
@Service
public class IsbnCrawlerServiceImpl implements IIsbnCrawlerService {
    private static final Logger log = LoggerFactory.getLogger(IsbnCrawlerServiceImpl.class);
    private static final String ISBN_TW_URL = "https://isbn.tw/";
    private static final String ISBN_NCL_URL = "https://isbn.ncl.edu.tw/NEW_ISBNNet/main_DisplayResults.php?&Pact=DisplayAll4Simple";
    private static final String ISBN_US_URL = "https://us.nicebooks.com/search/isbn?isbn=";
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private static final int HTTP_TIMEOUT = 10000; // HTTP 請求超時（毫秒）

    @Value("${cheng.profile:/tmp/uploadPath}")
    private String uploadPath;

    private final CrawlerProperties crawlerProperties;

    public IsbnCrawlerServiceImpl(CrawlerProperties crawlerProperties) {
        this.crawlerProperties = crawlerProperties;
        // 初始化 FlareSolver URL
        FlareSolverUtil.setFlareSolverUrl(crawlerProperties.getFlareSolverUrl());
    }

    @Override
    public BookInfoDTO crawlByIsbn(String isbn) {
        // 重試機制：3 次重試，等待時間遞增（第 N 次失敗後等待 N 秒）
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("[第{}次嘗試] 開始爬取 ISBN: {}", attempt, isbn);

                // 第一層：嘗試從台灣主站搜尋
                log.info("[第1層] 嘗試從台灣主站搜尋 ISBN: {}", isbn);
                BookInfoDTO bookInfo = crawlByIsbnFromTw(isbn);

                // 第二層：如果主站搜尋失敗或未找到書籍，則使用國家圖書館
                if (bookInfo == null || !bookInfo.getSuccess() || isBookInfoIncomplete(bookInfo)) {
                    log.info("[第2層] 台灣主站搜尋失敗或資訊不足，切換到國家圖書館搜尋 ISBN: {}", isbn);
                    bookInfo = crawlByIsbnFromNcl(isbn);
                }

                // 第三層：如果國家圖書館也失敗，則使用美國站備援
                if (bookInfo == null || !bookInfo.getSuccess() || isBookInfoIncomplete(bookInfo)) {
                    log.info("[第3層] 國家圖書館搜尋失敗或資訊不足，切換到美國站備援搜尋 ISBN: {}", isbn);
                    bookInfo = crawlByIsbnFromUs(isbn);
                }

                // 第四層：如果美國站也失敗或資訊不足，則使用 Google Books API
                if (bookInfo == null || !bookInfo.getSuccess() || isBookInfoIncomplete(bookInfo)) {
                    log.info("[第4層] 美國站搜尋失敗或資訊不足，切換到 Google Books API 搜尋 ISBN: {}", isbn);
                    bookInfo = crawlByIsbnFromGoogle(isbn);
                }

                // 如果成功，返回結果
                if (bookInfo != null && bookInfo.getSuccess()) {
                    log.info("第{}次嘗試成功爬取 ISBN: {}", attempt, isbn);
                    return bookInfo;
                }

                // 如果是最後一次嘗試，返回結果（即使失敗）
                if (attempt == maxRetries) {
                    log.warn("✗ 已達最大重試次數({}次)，返回最後結果 ISBN: {}", maxRetries, isbn);
                    return bookInfo != null ? bookInfo : createErrorBookInfo(isbn, "達到最大重試次數，爬取失敗");
                }

                // 如果失敗，等待後重試（遞增等待時間：第 N 次失敗後等待 N 秒）
                log.warn("✗ 第{}次嘗試失敗，等待 {} 秒後重試... ISBN: {}", attempt, attempt, isbn);
                TimeUnit.SECONDS.sleep(attempt);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("重試過程被中斷 ISBN: {}", isbn, e);
                return createErrorBookInfo(isbn, "爬取過程被中斷");
            } catch (Exception e) {
                log.error("第{}次嘗試發生異常 ISBN: {}", attempt, isbn, e);

                // 如果是最後一次嘗試，返回錯誤
                if (attempt == maxRetries) {
                    return createErrorBookInfo(isbn, "爬取失敗: " + e.getMessage());
                }

                // 等待後重試（遞增等待時間：第 N 次失敗後等待 N 秒）
                try {
                    log.warn("等待 {} 秒後重試... ISBN: {}", attempt, isbn);
                    TimeUnit.SECONDS.sleep(attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return createErrorBookInfo(isbn, "爬取過程被中斷");
                }
            }
        }

        // 理論上不會到這裡，但為了安全返回錯誤
        return createErrorBookInfo(isbn, "未知錯誤");
    }

    /**
     * 判斷書籍資訊是否不完整（沒有書名）
     */
    private boolean isBookInfoIncomplete(BookInfoDTO bookInfo) {
        return bookInfo.getTitle() == null || bookInfo.getTitle().trim().isEmpty();
    }

    /**
     * 從 isbn.tw 台灣站爬取書籍資訊
     * 使用 FlareSolver 處理 Cloudflare 驗證
     *
     * @param isbn ISBN 編號
     * @return 書籍資訊
     */
    @Override
    public BookInfoDTO crawlByIsbnFromTw(String isbn) {
        // 參數驗證
        if (isbn == null || isbn.trim().isEmpty()) {
            return createErrorBookInfo(isbn, "ISBN 不能為空");
        }

        String targetUrl = ISBN_TW_URL + isbn.trim();

        // 檢查 FlareSolver 是否啟用
        if (!crawlerProperties.isFlareSolverEnabled()) {
            log.warn("FlareSolver 未啟用，無法從 isbn.tw 爬取資訊");
            return createErrorBookInfo(isbn, targetUrl, "FlareSolver 未啟用");
        }

        log.info("[台灣站] 開始使用 FlareSolver 爬取 ISBN: {}", isbn);
        return crawlWithFlareSolver(targetUrl, isbn);
    }

    /**
     * 從國家圖書館 ISBN 查詢系統爬取書籍資訊
     *
     * @param isbn ISBN
     * @return 書籍資訊
     */
    @Override
    public BookInfoDTO crawlByIsbnFromNcl(String isbn) {
        // 參數驗證
        if (isbn == null || isbn.trim().isEmpty()) {
            return createErrorBookInfo(isbn, "ISBN 不能為空");
        }

        BookInfoDTO bookInfo = createErrorBookInfo(isbn, null);

        WebDriver driver = null;
        try {
            log.info("開始從國家圖書館爬取 ISBN 資訊: {}", isbn);

            // 初始化 WebDriver
            driver = SeleniumUtil.createWebDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 1. 訪問查詢頁面
            driver.get(ISBN_NCL_URL);
            TimeUnit.SECONDS.sleep(2);

            // 2. 選擇查詢欄位為 "ISBN"
            WebElement searchFieldSelect = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("FO_SearchField0")));
            Select select = new Select(searchFieldSelect);
            select.selectByValue("ISBN");
            log.info("已選擇查詢欄位: ISBN");

            // 3. 輸入 ISBN
            WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("FO_SearchValue0")));
            searchInput.clear();
            searchInput.sendKeys(isbn.trim());
            log.info("已輸入 ISBN: {}", isbn);

            // 4. 點擊查詢按鈕（重試機制：最多 3 次）
            WebElement bookLinkElement = null;
            int maxRetries = 3;

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                // 每次都重新取得按鈕元素，避免 StaleElementReferenceException
                WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(@onclick, 'GoSearch')]")));

                searchButton.click();
                log.info("第 {} 次點擊查詢按鈕", attempt);

                // 等待時間遞增：1秒、2秒、3秒
                int waitSeconds = attempt;
                TimeUnit.MILLISECONDS.sleep(waitSeconds * 1000L);

                // 檢查是否有查詢結果
                try {
                    bookLinkElement = driver.findElement(By.xpath("//td[@aria-label='書名']/a"));
                    if (bookLinkElement != null && bookLinkElement.isDisplayed()) {
                        log.info("第 {} 次查詢成功找到結果", attempt);
                        break;
                    }
                } catch (Exception e) {
                    log.warn("第 {} 次查詢未找到結果", attempt);
                    if (attempt == maxRetries) {
                        log.error("已嘗試 {} 次查詢，仍未找到結果", maxRetries);
                        throw new Exception("國家圖書館查詢失敗：多次嘗試後仍未找到結果");
                    }
                }
            }

            // 5. 檢查是否有查詢結果
            try {
                if (bookLinkElement == null) {
                    throw new Exception("未找到書籍連結元素");
                }

                String bookDetailUrl = bookLinkElement.getAttribute("href");
                String bookTitle = bookLinkElement.getText();
                log.info("找到書籍: {}，準備進入詳細頁面", bookTitle);

                // 6. 進入書籍詳細頁面
                // 判斷是否為完整 URL，如果是相對路徑才需要拼接
                String fullUrl;
                if (bookDetailUrl.startsWith("http")) {
                    // 已經是完整 URL，直接使用
                    fullUrl = bookDetailUrl;
                } else {
                    // 相對路徑，需要拼接基礎 URL
                    String currentUrl = driver.getCurrentUrl();
                    String baseUrl = currentUrl.substring(0, currentUrl.lastIndexOf("/") + 1);
                    fullUrl = baseUrl + bookDetailUrl;
                }

                log.info("詳細頁面 URL: {}", fullUrl);
                driver.get(fullUrl);
                TimeUnit.SECONDS.sleep(2);

                // 7. 解析書籍詳細資訊
                WebElement detailTable = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.className("table-bookinforight")));

                // 解析表格中的資料
                Elements rows = Jsoup.parse(driver.getPageSource()).select("table.table-bookinforight tr");

                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() >= 2) {
                        String label = cells.get(0).text().trim();
                        String value = cells.get(1).text().trim().replace("&nbsp;", "").trim();

                        if (value.isEmpty()) continue;

                        // 使用 NclField Enum 處理欄位映射
                        NclField.setFieldValue(bookInfo, label, value);
                    }
                }

                // 設定來源 URL
                bookInfo.setSourceUrl(driver.getCurrentUrl());

                // 檢查是否成功取得書名
                if (bookInfo.getTitle() != null && !bookInfo.getTitle().trim().isEmpty()) {
                    bookInfo.setSuccess(true);
                    log.info("國家圖書館爬取成功: {}", bookInfo.getTitle());
                } else {
                    bookInfo.setErrorMessage("未能從國家圖書館取得書籍資訊");
                    log.warn("國家圖書館未能取得書籍資訊");
                }

            } catch (Exception e) {
                log.warn("國家圖書館查詢結果中未找到書籍: {}", e.getMessage());
                bookInfo.setErrorMessage("國家圖書館查無此書");
            }

        } catch (Exception e) {
            log.error("從國家圖書館爬取 ISBN 資訊時發生錯誤: {}", e.getMessage(), e);
            bookInfo.setErrorMessage("國家圖書館爬取失敗: " + e.getMessage());
        } finally {
            SeleniumUtil.safelyQuitWebDriver(driver);
        }

        return bookInfo;
    }

    /**
     * 從美國站爬取書籍資訊
     *
     * @param isbn ISBN
     * @return 書籍資訊
     */
    @Override
    public BookInfoDTO crawlByIsbnFromUs(String isbn) {
        // 參數驗證
        if (isbn == null || isbn.trim().isEmpty()) {
            return createErrorBookInfo(isbn, "ISBN 不能為空");
        }

        String targetUrl = ISBN_US_URL + isbn.trim();
        BookInfoDTO bookInfo = createErrorBookInfo(isbn, targetUrl, null);

        WebDriver driver = null;
        try {
            log.info("開始從美國站爬取 ISBN 資訊（使用 Selenium {}模式）: {}",
                    crawlerProperties.isRemoteMode() ? "遠端" : "本地", targetUrl);

            // 使用配置檔案中的設定建立 WebDriver（支援 Remote/Local 模式）
            crawlerProperties.setHeadless(true);
            driver = SeleniumUtil.createWebDriver(crawlerProperties);
            driver.get(targetUrl);

            // 等待搜尋結果出現
            WebDriverWait wait = SeleniumUtil.createWebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.search-result-line")));

            // 檢查是否找到書籍資訊
            if (driver.findElements(By.cssSelector("div.search-result-line")).isEmpty()) {
                bookInfo.setErrorMessage("在美國站找不到此 ISBN 的書籍資訊");
                return bookInfo;
            }

            WebElement searchResult = driver.findElement(By.cssSelector("div.search-result-line"));

            // 解析書名
            try {
                WebElement titleElement = searchResult.findElement(By.cssSelector("a.title"));
                bookInfo.setTitle(titleElement.getText().trim());
            } catch (Exception e) {
                log.warn("美國站未找到書名");
            }

            // 解析作者
            try {
                List<WebElement> contentDivs = searchResult.findElements(
                        By.cssSelector("div.medium-10.small-margin-top.columns > div"));
                for (WebElement div : contentDivs) {
                    String text = div.getText();
                    if (text.startsWith("by ")) {
                        WebElement authorLink = div.findElement(By.cssSelector("a.sublinks"));
                        bookInfo.setAuthor(authorLink.getText().trim());
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("美國站未找到作者");
            }

            // 解析出版社、出版日期、裝訂
            try {
                List<WebElement> contentDivs = searchResult.findElements(
                        By.cssSelector("div.medium-10.small-margin-top.columns > div"));
                for (int i = 0; i < contentDivs.size(); i++) {
                    WebElement div = contentDivs.get(i);
                    try {
                        WebElement publisherLink = div.findElement(By.cssSelector("a.sublinks"));
                        if (!div.getText().startsWith("by ")) {
                            bookInfo.setPublisher(publisherLink.getText().trim());

                            // 出版日期和裝訂在下一個 div
                            if (i + 1 < contentDivs.size()) {
                                WebElement nextDiv = contentDivs.get(i + 1);
                                String publishInfo = nextDiv.getText().trim();

                                if (publishInfo.contains(",")) {
                                    String[] parts = publishInfo.split(",", 2);
                                    bookInfo.setPublishDate(parts[0].trim());

                                    try {
                                        WebElement bindingElement = nextDiv.findElement(By.tagName("em"));
                                        bookInfo.setBinding(bindingElement.getText().trim());
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                log.warn("美國站未找到出版資訊");
            }

            // 解析封面圖片 - 使用 Selenium 等待圖片載入
            try {
                // 等待封面圖片容器出現
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div.cover-background a.has-image img")));

                WebElement coverImg = searchResult.findElement(
                        By.cssSelector("div.cover-background a.has-image img"));

                String imgSrc = coverImg.getAttribute("src");
                if (imgSrc != null && !imgSrc.isEmpty()) {
                    log.info("美國站找到封面圖片 URL: {}", imgSrc);

                    // 調整圖片大小為 width=500
                    imgSrc = imgSrc.replaceAll("width=\\d+", "width=500");
                    bookInfo.setCoverImageUrl(imgSrc);

                    // 下載圖片
                    String savedPath = downloadCoverImage(imgSrc, isbn);
                    if (savedPath != null) {
                        bookInfo.setCoverImagePath(savedPath);
                        log.info("美國站封面圖片下載成功: {}", savedPath);
                    } else {
                        log.warn("美國站封面圖片下載失敗: {}", imgSrc);
                    }
                } else {
                    log.warn("美國站找到圖片元素但 src 屬性為空");
                }
            } catch (Exception e) {
                log.info("美國站此書籍沒有封面圖片或載入失敗: {}", e.getMessage());
            }

            bookInfo.setSuccess(true);
            log.info("美國站 ISBN 資訊爬取成功: {}", bookInfo.getTitle());

        } catch (Exception e) {
            log.error("美國站爬取 ISBN 資訊時發生錯誤: {}", e.getMessage(), e);
            bookInfo.setErrorMessage("美國站爬取失敗: " + e.getMessage());
        } finally {
            // 使用 SeleniumUtil 安全關閉 WebDriver
            SeleniumUtil.safelyQuitWebDriver(driver);
        }

        return bookInfo;
    }

    @Override
    public BookInfoDTO crawlByIsbnFromGoogle(String isbn) {
        BookInfoDTO bookInfo = new BookInfoDTO();
        bookInfo.setIsbn(isbn);
        bookInfo.setSuccess(false);

        if (isbn == null || isbn.trim().isEmpty()) {
            bookInfo.setErrorMessage("ISBN 不能為空");
            return bookInfo;
        }

        String targetUrl = GOOGLE_BOOKS_API_URL + isbn.trim();
        bookInfo.setSourceUrl(targetUrl);

        try {
            log.info("開始從 Google Books API 搜尋 ISBN 資訊: {}", targetUrl);

            // 使用 Jsoup 取得 JSON 回應
            String jsonResponse = Jsoup.connect(targetUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .ignoreContentType(true)
                    .timeout(HTTP_TIMEOUT)
                    .execute()
                    .body();

            // 解析 JSON
            JSONObject root = JSON.parseObject(jsonResponse);
            int totalItems = root.getIntValue("totalItems");

            if (totalItems == 0) {
                bookInfo.setErrorMessage("在 Google Books 找不到此 ISBN 的書籍資訊");
                return bookInfo;
            }

            // 取得第一本書的資訊
            JSONArray items = root.getJSONArray("items");
            JSONObject firstItem = items.getJSONObject(0);
            JSONObject volumeInfo = firstItem.getJSONObject("volumeInfo");

            // 解析書名
            if (volumeInfo.containsKey("title")) {
                bookInfo.setTitle(volumeInfo.getString("title"));
            }

            // 解析作者（可能是陣列）
            if (volumeInfo.containsKey("authors")) {
                JSONArray authors = volumeInfo.getJSONArray("authors");
                if (authors != null && !authors.isEmpty()) {
                    StringBuilder authorStr = new StringBuilder();
                    for (int i = 0; i < authors.size(); i++) {
                        if (i > 0) authorStr.append(", ");
                        authorStr.append(authors.getString(i));
                    }
                    bookInfo.setAuthor(authorStr.toString());
                }
            }

            // 解析出版社
            if (volumeInfo.containsKey("publisher")) {
                bookInfo.setPublisher(volumeInfo.getString("publisher"));
            }

            // 解析出版日期
            if (volumeInfo.containsKey("publishedDate")) {
                bookInfo.setPublishDate(volumeInfo.getString("publishedDate"));
            }

            // 解析語言
            if (volumeInfo.containsKey("language")) {
                String lang = volumeInfo.getString("language");
                // 轉換語言代碼為中文
                bookInfo.setLanguage(convertLanguageCode(lang));
            }

            // 解析簡介
            if (volumeInfo.containsKey("description")) {
                bookInfo.setIntroduction(volumeInfo.getString("description"));
            }

            // 解析封面圖片
            if (volumeInfo.containsKey("imageLinks")) {
                JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                String imageUrl = null;

                // 優先使用較大的圖片
                if (imageLinks.containsKey("large")) {
                    imageUrl = imageLinks.getString("large");
                } else if (imageLinks.containsKey("medium")) {
                    imageUrl = imageLinks.getString("medium");
                } else if (imageLinks.containsKey("thumbnail")) {
                    imageUrl = imageLinks.getString("thumbnail");
                }

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Google Books 圖片 URL 可能是 http，改為 https
                    imageUrl = imageUrl.replace("http://", "https://");
                    bookInfo.setCoverImageUrl(imageUrl);

                    // 下載圖片
                    String savedPath = downloadCoverImage(imageUrl, isbn);
                    if (savedPath != null) {
                        bookInfo.setCoverImagePath(savedPath);
                    }
                }
            }

            bookInfo.setSuccess(true);
            log.info("Google Books API 搜尋成功: {}", bookInfo.getTitle());

        } catch (IOException e) {
            log.error("Google Books API 搜尋時發生錯誤: {}", e.getMessage(), e);
            bookInfo.setErrorMessage("Google Books API 搜尋失敗: " + e.getMessage());
        } catch (Exception e) {
            log.error("處理 Google Books API 回應時發生未預期錯誤: {}", e.getMessage(), e);
            bookInfo.setErrorMessage("處理 Google Books 資料失敗: " + e.getMessage());
        }

        return bookInfo;
    }

    /**
     * 轉換語言代碼為中文說明
     */
    private String convertLanguageCode(String langCode) {
        if (langCode == null) return "";

        return switch (langCode.toLowerCase()) {
            case "zh-cn" -> "簡體中文";
            case "zh-tw" -> "繁體中文";
            case "en" -> "英文";
            case "ja" -> "日文";
            case "ko" -> "韓文";
            case "fr" -> "法文";
            case "de" -> "德文";
            case "es" -> "西班牙文";
            default -> langCode.toUpperCase();
        };
    }

    /**
     * 下載封面圖片
     *
     * @param imageUrl 圖片 URL
     * @param isbn     ISBN
     * @return 儲存路徑（相對於 /profile 的路徑）
     */
    private String downloadCoverImage(String imageUrl, String isbn) {
        try {
            // 建立書籍封面儲存目錄
            String bookCoverPath = uploadPath + "/book-covers";
            String fileName = "isbn_" + isbn;

            String savedPath = ImageDownloadUtil.downloadImage(imageUrl, bookCoverPath, fileName);
            if (savedPath != null) {
                log.info("封面圖片下載成功: {}", savedPath);

                // 轉換為相對路徑（相對於 /profile）
                // 例如: /Users/cheng/uploadPath/book-covers/isbn_xxx.jpg -> /profile/book-covers/isbn_xxx.jpg
                if (savedPath.contains("/book-covers/")) {
                    String relativePath = "/profile/book-covers/" + savedPath.substring(savedPath.lastIndexOf("/") + 1);
                    log.info("圖片相對路徑: {}", relativePath);
                    return relativePath;
                }
                return savedPath;
            }
        } catch (Exception e) {
            log.error("下載封面圖片失敗: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 建立錯誤狀態的 BookInfoDTO（不含 URL）
     *
     * @param isbn         ISBN 編號
     * @param errorMessage 錯誤訊息
     * @return 錯誤狀態的 BookInfoDTO
     */
    private BookInfoDTO createErrorBookInfo(String isbn, String errorMessage) {
        BookInfoDTO bookInfo = new BookInfoDTO();
        bookInfo.setIsbn(isbn);
        bookInfo.setSuccess(false);
        bookInfo.setErrorMessage(errorMessage);
        return bookInfo;
    }

    /**
     * 建立錯誤狀態的 BookInfoDTO（含 URL）
     *
     * @param isbn         ISBN 編號
     * @param sourceUrl    來源 URL
     * @param errorMessage 錯誤訊息
     * @return 錯誤狀態的 BookInfoDTO
     */
    private BookInfoDTO createErrorBookInfo(String isbn, String sourceUrl, String errorMessage) {
        BookInfoDTO bookInfo = createErrorBookInfo(isbn, errorMessage);
        bookInfo.setSourceUrl(sourceUrl);
        return bookInfo;
    }

    /**
     * 使用 FlareSolver 爬取書籍資訊
     *
     * @param targetUrl 目標 URL
     * @param isbn      ISBN
     * @return 書籍資訊
     */
    private BookInfoDTO crawlWithFlareSolver(String targetUrl, String isbn) {
        BookInfoDTO bookInfo = createErrorBookInfo(isbn, targetUrl, null);

        try {
            // 檢查 FlareSolver 服務可用性
            if (!FlareSolverUtil.isServiceAvailable()) {
                String error = "FlareSolver 服務不可用";
                log.error(error);
                bookInfo.setErrorMessage(error);
                return bookInfo;
            }

            log.info("使用 FlareSolver 取得頁面: {}", targetUrl);

            // 呼叫 FlareSolver 處理 Cloudflare
            FlareSolverUtil.FlareSolverResponse response = FlareSolverUtil.getPage(
                    targetUrl, null, crawlerProperties.getFlareSolverMaxTimeout()
            );

            // 驗證響應
            String validationError = validateFlareSolverResponse(response);
            if (validationError != null) {
                bookInfo.setErrorMessage(validationError);
                return bookInfo;
            }

            log.info("FlareSolver 成功（HTML 長度: {} 字元）", response.getHtml().length());

            // 解析 HTML 內容
            Document doc = Jsoup.parse(response.getHtml());
            parseBookInfoFromDocument(doc, bookInfo, isbn, response.getCookies(), response.getUserAgent());

            // 驗證解析結果
            if (isBookInfoIncomplete(bookInfo)) {
                log.warn("未能解析書名，頁面可能異常");
                bookInfo.setErrorMessage("未能解析書籍資訊");
                return bookInfo;
            }

            bookInfo.setSuccess(true);
            log.info("爬取成功: {}", bookInfo.getTitle());

        } catch (Exception e) {
            log.error("FlareSolver 爬取錯誤: {}", e.getMessage(), e);
            bookInfo.setErrorMessage("爬取失敗: " + e.getMessage());
        }

        return bookInfo;
    }

    /**
     * 驗證 FlareSolver 響應
     *
     * @param response FlareSolver 響應
     * @return 錯誤訊息（null 表示無錯誤）
     */
    private String validateFlareSolverResponse(FlareSolverUtil.FlareSolverResponse response) {
        if (!response.isSuccess()) {
            log.error("FlareSolver 請求失敗: {}", response.getMessage());
            return "FlareSolver 請求失敗: " + response.getMessage();
        }

        if (response.getStatus() != 200) {
            log.error("HTTP 狀態碼異常: {}", response.getStatus());
            return "HTTP 狀態碼異常: " + response.getStatus();
        }

        if (response.getHtml() == null || response.getHtml().trim().isEmpty()) {
            log.error("HTML 內容為空");
            return "HTML 內容為空";
        }

        return null;
    }

    /**
     * 從 Jsoup Document 解析書籍資訊（共用方法）
     *
     * @param doc       Jsoup Document
     * @param bookInfo  書籍資訊 DTO
     * @param isbn      ISBN
     * @param cookies   Cloudflare cookies（可為 null）
     * @param userAgent User-Agent（可為 null）
     */
    private void parseBookInfoFromDocument(Document doc, BookInfoDTO bookInfo, String isbn,
                                           String cookies, String userAgent) {
        // 解析書名
        parseTitle(doc, bookInfo);

        // 解析欄位資訊（作者、出版社等）
        parseBookFields(doc, bookInfo);

        // 解析簡介
        parseIntroduction(doc, bookInfo);

        // 解析並下載封面圖片
        parseCoverImage(doc, bookInfo, isbn, cookies, userAgent);
    }

    /**
     * 解析書名
     */
    private void parseTitle(Document doc, BookInfoDTO bookInfo) {
        Element titleElement = doc.selectFirst("h1.h3.mb-2.font-weight-bold");
        if (titleElement != null) {
            bookInfo.setTitle(titleElement.text().trim());
        }
    }

    /**
     * 解析書籍欄位資訊（使用 Enum 管理欄位映射）
     */
    private void parseBookFields(Document doc, BookInfoDTO bookInfo) {
        Elements dlElements = doc.select("dl.row.mb-0");
        for (Element dl : dlElements) {
            Element dt = dl.selectFirst("dt");
            Element dd = dl.selectFirst("dd");

            if (dt != null && dd != null) {
                String label = dt.text().trim();
                String value = dd.text().trim();
                BookField.setFieldValue(bookInfo, label, value);
            }
        }
    }

    /**
     * 解析簡介
     */
    private void parseIntroduction(Document doc, BookInfoDTO bookInfo) {
        Element introCard = doc.selectFirst("div.card.mb-4");
        if (introCard == null) return;

        Element introHeader = introCard.selectFirst("h2.h6.card-header");
        if (introHeader == null || !"簡介".equals(introHeader.text().trim())) return;

        Element introBody = introCard.selectFirst("div.card-body p");
        if (introBody != null) {
            String introHtml = introBody.html();
            String introText = introHtml.replaceAll("<br\\s*/?>", "\n");
            introText = Jsoup.parse(introText).text();
            bookInfo.setIntroduction(introText.trim());
        }
    }

    /**
     * 解析封面圖片（含重試機制）
     */
    private void parseCoverImage(Document doc, BookInfoDTO bookInfo, String isbn,
                                 String cookies, String userAgent) {
        Element coverImg = doc.selectFirst("div.embed-responsive-item img");
        if (coverImg == null) return;

        String imgSrc = coverImg.attr("src");
        if (imgSrc.isEmpty()) return;

        // 處理相對路徑
        if (imgSrc.startsWith("/")) {
            imgSrc = "https://isbn.tw" + imgSrc;
        }
        bookInfo.setCoverImageUrl(imgSrc);

        // 下載圖片（重試機制：最多 3 次）
        String savedPath = null;
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("第 {} 次嘗試下載封面圖片: {}", attempt, imgSrc);

            savedPath = (cookies != null && !cookies.isEmpty())
                    ? downloadCoverImageWithCookies(imgSrc, isbn, cookies, userAgent, bookInfo.getTitle())
                    : downloadCoverImage(imgSrc, isbn);

            if (savedPath != null) {
                log.info("第 {} 次下載封面圖片成功", attempt);
                break;
            }

            // 下載失敗，等待後重試
            if (attempt < maxRetries) {
                int waitSeconds = attempt;
                log.warn("第 {} 次下載封面圖片失敗，{}秒後重試", attempt, waitSeconds);
                try {
                    Thread.sleep(waitSeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("等待重試時被中斷", e);
                    break;
                }
            } else {
                log.error("已嘗試 {} 次下載封面圖片，全部失敗", maxRetries);
            }
        }

        if (savedPath != null) {
            bookInfo.setCoverImagePath(savedPath);
        }
    }

    /**
     * 書籍欄位 Enum（管理欄位名稱與 setter 映射）
     */
    @RequiredArgsConstructor
    private enum BookField {
        AUTHOR("作者"),
        PUBLISHER("出版社"),
        PUBLISH_DATE("出版日期"),
        PUBLISH_LOCATION("出版地"),
        LANGUAGE("語言"),
        EDITION("版本"),
        BINDING("裝訂"),
        CLASSIFICATION("分級");

        private final String label;

        /**
         * 根據標籤設定 BookInfoDTO 欄位值
         *
         * @param bookInfo BookInfoDTO 物件
         * @param label    欄位標籤
         * @param value    欄位值
         */
        public static void setFieldValue(BookInfoDTO bookInfo, String label, String value) {
            for (BookField field : values()) {
                if (field.label.equals(label)) {
                    field.setValue(bookInfo, value);
                    return;
                }
            }
        }

        /**
         * 設定對應欄位值
         */
        private void setValue(BookInfoDTO bookInfo, String value) {
            switch (this) {
                case AUTHOR:
                    bookInfo.setAuthor(value);
                    break;
                case PUBLISHER:
                    bookInfo.setPublisher(value);
                    break;
                case PUBLISH_DATE:
                    bookInfo.setPublishDate(value);
                    break;
                case PUBLISH_LOCATION:
                    bookInfo.setPublishLocation(value);
                    break;
                case LANGUAGE:
                    bookInfo.setLanguage(value);
                    break;
                case EDITION:
                    bookInfo.setEdition(value);
                    break;
                case BINDING:
                    bookInfo.setBinding(value);
                    break;
                case CLASSIFICATION:
                    bookInfo.setClassification(value);
                    break;
            }
        }
    }

    /**
     * 國家圖書館欄位 Enum（管理欄位名稱與 setter 映射）
     */
    private enum NclField {
        TITLE("書名"),
        AUTHOR("作者"),
        PUBLISHER("出版機構"),
        EDITION("出版版次"),
        CLASSIFICATION("圖書類號"),
        SUBJECT("主題標題"),
        TARGET_AUDIENCE("適讀對象"),
        CATEGORY_SUGGESTION("建議上架分類"),
        RATING("分級註記");

        private final String label;

        NclField(String label) {
            this.label = label;
        }

        /**
         * 根據標籤設定 BookInfoDTO 欄位值
         *
         * @param bookInfo BookInfoDTO 物件
         * @param label    欄位標籤
         * @param value    欄位值
         */
        public static void setFieldValue(BookInfoDTO bookInfo, String label, String value) {
            for (NclField field : values()) {
                if (field.label.equals(label)) {
                    field.setValue(bookInfo, value);
                    return;
                }
            }
        }

        /**
         * 設定對應欄位值
         */
        private void setValue(BookInfoDTO bookInfo, String value) {
            switch (this) {
                case TITLE:
                    bookInfo.setTitle(value);
                    break;
                case AUTHOR:
                    bookInfo.setAuthor(value);
                    break;
                case PUBLISHER:
                    bookInfo.setPublisher(value);
                    break;
                case EDITION:
                    bookInfo.setEdition(value);
                    break;
                case CLASSIFICATION:
                    bookInfo.setClassification(value);
                    break;
                case SUBJECT:
                case TARGET_AUDIENCE:
                case CATEGORY_SUGGESTION:
                case RATING:
                    // 這些欄位附加到簡介
                    appendToIntroduction(bookInfo, this.label, value);
                    break;
            }
        }

        /**
         * 將資訊附加到簡介欄位
         */
        private void appendToIntroduction(BookInfoDTO bookInfo, String label, String value) {
            String newLine = label + ": " + value;
            if (bookInfo.getIntroduction() == null) {
                bookInfo.setIntroduction(newLine);
            } else {
                bookInfo.setIntroduction(bookInfo.getIntroduction() + "\n" + newLine);
            }
        }
    }

    /**
     * 下載封面圖片（使用 FlareSolver 處理重定向和 Cloudflare）
     *
     * @param imageUrl  圖片 URL
     * @param isbn      ISBN
     * @param cookies   Cloudflare cookies（用於 session 重用）
     * @param userAgent User-Agent
     * @param bookTitle 書名（用於降級搜尋）
     * @return 儲存路徑（相對於 /profile 的路徑）
     */
    private String downloadCoverImageWithCookies(String imageUrl, String isbn,
                                                 String cookies, String userAgent, String bookTitle) {
        try {
            log.info("使用 FlareSolver 處理圖片下載（支援重定向和 Cloudflare）: {}", imageUrl);

            // 使用 FlareSolver 訪問圖片 URL，獲取重定向後的 cookies
            // 圖片 URL 可能重定向到 books.com.tw，需要新的 cookies
            FlareSolverUtil.FlareSolverResponse imageResponse = FlareSolverUtil.getPage(
                    imageUrl,
                    null,  // 不重用 session，因為圖片可能在不同域名
                    30000  // 圖片下載超時 30 秒（配合重試機制）
            );

            if (!imageResponse.isSuccess()) {
                log.warn("FlareSolver 訪問圖片 URL 失敗，嘗試從 books.com.tw 搜尋: {}", imageResponse.getMessage());
                // 降級到 books.com.tw 搜尋
                return searchAndDownloadFromBooksComTw(bookTitle, isbn);
            }

            // 獲取 FlareSolver 處理後的資訊
            String finalCookies = imageResponse.getCookies();
            String finalUserAgent = imageResponse.getUserAgent();
            String finalUrl = imageResponse.getUrl();  // 重定向後的最終 URL

            if (finalCookies == null || finalCookies.isEmpty()) {
                log.warn("FlareSolver 未返回 cookies，使用原始 cookies");
                finalCookies = cookies;
            }

            if (finalUserAgent == null || finalUserAgent.isEmpty()) {
                finalUserAgent = userAgent;
            }

            // 使用重定向後的最終 URL（如果有的話）
            String downloadUrl = (finalUrl != null && !finalUrl.isEmpty()) ? finalUrl : imageUrl;

            log.info("FlareSolver 成功處理圖片 URL");
            log.info("  原始 URL: {}", imageUrl);
            if (!imageUrl.equals(downloadUrl)) {
                log.info("  最終 URL: {} （已重定向）", downloadUrl);
            }

            // 建立書籍封面儲存目錄
            String bookCoverPath = uploadPath + "/book-covers";
            String fileName = "isbn_" + isbn;

            // 如果發生重定向，使用原始 URL 作為 Referer（重要！防盜鏈保護）
            String referer = imageUrl.equals(downloadUrl) ? null : imageUrl;
            if (referer != null) {
                log.info("圖片發生重定向，使用原始 URL 作為 Referer: {}", referer);
            }

            String savedPath = ImageDownloadUtil.downloadImageWithCookies(
                    downloadUrl, bookCoverPath, fileName, finalCookies, finalUserAgent, referer
            );

            if (savedPath != null) {
                // 驗證下載的檔案大小（雙重檢查）
                File imageFile = new File(savedPath);
                if (imageFile.exists() && imageFile.length() < 2048) {
                    log.warn("下載的圖片檔案過小（{} bytes），可能是錯誤頁面，改用 books.com.tw 搜尋",
                            imageFile.length());
                    try {
                        Files.deleteIfExists(imageFile.toPath());
                        log.info("已刪除無效圖片檔案: {}", savedPath);
                    } catch (IOException e) {
                        log.warn("刪除無效圖片檔案失敗: {}, 錯誤: {}", savedPath, e.getMessage());
                    }
                    return searchAndDownloadFromBooksComTw(bookTitle, isbn);
                }

                log.info("封面圖片下載成功（使用 FlareSolver cookies）: {}，檔案大小: {} bytes",
                        savedPath, imageFile.length());

                // 轉換為相對路徑（相對於 /profile）
                if (savedPath.contains("/book-covers/")) {
                    String relativePath = "/profile/book-covers/" +
                            savedPath.substring(savedPath.lastIndexOf("/") + 1);
                    log.info("圖片相對路徑: {}", relativePath);
                    return relativePath;
                }
                return savedPath;
            } else {
                // 下載失敗或檔案無效，嘗試降級方案
                log.warn("使用 FlareSolver 下載圖片失敗或檔案無效，改用 books.com.tw 搜尋");
                return searchAndDownloadFromBooksComTw(bookTitle, isbn);
            }
        } catch (Exception e) {
            log.error("下載封面圖片失敗（使用 FlareSolver）: {}", e.getMessage(), e);
            // 降級到 books.com.tw 搜尋
            return searchAndDownloadFromBooksComTw(bookTitle, isbn);
        }
    }

    /**
     * 從 books.com.tw 搜尋書名並下載封面圖片（降級方案）
     *
     * @param bookTitle 書名
     * @param isbn      ISBN
     * @return 儲存路徑（相對於 /profile 的路徑）
     */
    private String searchAndDownloadFromBooksComTw(String bookTitle, String isbn) {
        if (bookTitle == null || bookTitle.trim().isEmpty()) {
            log.warn("書名為空，無法從 books.com.tw 搜尋圖片");
            return null;
        }

        try {
            // 建立搜尋 URL
            String encodedTitle = URLEncoder.encode(bookTitle.trim(), StandardCharsets.UTF_8);
            String searchUrl = String.format(
                    "https://search.books.com.tw/search/query/cat/1/sort/1/v/1/page/1/spell/3/ms2/ms2_1/key/%s",
                    encodedTitle
            );
            log.info("從 books.com.tw 搜尋書籍封面: {} ({})", bookTitle, searchUrl);

            // 使用 Jsoup 取得搜尋結果頁面
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            // 找到所有圖片元素（包含 b-lazy 或已載入的 b-loaded）
            Elements images = doc.select("img.b-lazy, img[src*='book.com.tw']");
            if (images.isEmpty()) {
                log.warn("在 books.com.tw 搜尋結果中未找到圖片元素");
                return null;
            }

            log.info("找到 {} 個圖片元素", images.size());

            // 優先尋找 alt 屬性包含書名的圖片
            Element targetImage = null;
            for (Element img : images) {
                String alt = img.attr("alt");
                if (alt != null && alt.contains(bookTitle)) {
                    targetImage = img;
                    log.info("找到匹配書名的圖片: alt='{}'", alt);
                    break;
                }
            }

            // 如果沒有找到匹配的，使用第一個
            if (targetImage == null) {
                targetImage = images.first();
                log.info("使用第一個圖片元素: alt='{}'", targetImage.attr("alt"));
            }

            // 從圖片元素中提取真實圖片 URL
            // 優先檢查 src，如果是懶加載占位符，則檢查 data-src 或 srcset
            String src = targetImage.attr("src");
            String dataSrc = targetImage.attr("data-src");
            String srcset = targetImage.attr("srcset");
            
            log.debug("圖片屬性 - src: {}, data-src: {}, srcset: {}", src, dataSrc, srcset);
            
            String realImageUrl = null;
            
            // 如果 src 是 base64 占位符（懶加載），使用 srcset 或 data-src
            if (src != null && src.startsWith("data:image")) {
                log.info("偵測到懶加載圖片（base64 占位符），嘗試從 srcset 或 data-src 取得真實 URL");
                
                // 優先使用 srcset（通常包含高解析度圖片）
                if (srcset != null && !srcset.isEmpty()) {
                    realImageUrl = extractRealImageUrl(srcset);
                    if (realImageUrl != null) {
                        log.info("從 srcset 提取到圖片 URL: {}", realImageUrl);
                    }
                }
                
                // 其次使用 data-src
                if (realImageUrl == null && dataSrc != null && !dataSrc.isEmpty()) {
                    realImageUrl = extractRealImageUrl(dataSrc);
                    if (realImageUrl != null) {
                        log.info("從 data-src 提取到圖片 URL: {}", realImageUrl);
                    }
                }
            } else {
                // 正常情況，從 src 提取
                realImageUrl = extractRealImageUrl(src);
            }

            if (realImageUrl == null) {
                log.warn("無法從圖片元素中提取真實 URL - src: {}, data-src: {}, srcset: {}", 
                        src, dataSrc, srcset);
                return null;
            }

            log.info("成功提取圖片 URL: {}", realImageUrl);

            // 下載圖片
            String bookCoverPath = uploadPath + "/book-covers";
            String fileName = "isbn_" + isbn;

            String savedPath = ImageDownloadUtil.downloadImage(realImageUrl, bookCoverPath, fileName);

            if (savedPath != null && savedPath.contains("/book-covers/")) {
                String relativePath = "/profile/book-covers/" +
                        savedPath.substring(savedPath.lastIndexOf("/") + 1);
                log.info("從 books.com.tw 下載封面圖片成功: {}", relativePath);
                return relativePath;
            }
            return savedPath;
        } catch (Exception e) {
            log.error("從 books.com.tw 搜尋並下載圖片失敗: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 從 books.com.tw 的圖片 URL 中提取真實圖片連結
     * <p>
     * 範例輸入:
     * <a href="https://im1.book.com.tw/image/getImage?i=https://www.books.com.tw/img/001/070/58/0010705830.jpg&w=187&h=187&v=56ab3f58">
     * https://im1.book.com.tw/image/getImage?i=https://www.books.com.tw/img/001/070/58/0010705830.jpg&w=187&h=187&v=56ab3f58
     * </a>
     * 範例輸出:
     * <a href="https://www.books.com.tw/img/001/070/58/0010705830.jpg">
     * https://www.books.com.tw/img/001/070/58/0010705830.jpg
     * </a>
     *
     * @param src 圖片 src 屬性值
     * @return 真實圖片 URL
     */
    private String extractRealImageUrl(String src) {
        if (src == null || src.isEmpty()) {
            return null;
        }

        try {
            // 找到 i= 參數
            int startIndex = src.indexOf("i=");
            if (startIndex == -1) {
                log.warn("src 中未找到 i= 參數: {}", src);
                return null;
            }

            // 從 i= 後面開始提取
            startIndex += 2; // 跳過 "i="

            // 找到 .jpg 的位置（也支援其他圖片格式）
            int endIndex = -1;
            String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
            for (String ext : imageExtensions) {
                int idx = src.indexOf(ext, startIndex);
                if (idx != -1) {
                    endIndex = idx + ext.length();
                    break;
                }
            }

            if (endIndex == -1) {
                log.warn("src 中未找到圖片副檔名: {}", src);
                return null;
            }

            // 提取真實 URL
            String realUrl = src.substring(startIndex, endIndex);

            // 處理 HTML 實體編碼（&amp; -> &）
            realUrl = realUrl.replace("&amp;", "&");

            return realUrl;
        } catch (Exception e) {
            log.error("提取真實圖片 URL 失敗: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 直接下載圖片（不使用 FlareSolver，作為降級方案）
     *
     * @param imageUrl  圖片 URL
     * @param isbn      ISBN
     * @param cookies   Cookies
     * @param userAgent User-Agent
     * @return 儲存路徑（相對於 /profile 的路徑）
     */
    private String downloadImageDirectly(String imageUrl, String isbn,
                                         String cookies, String userAgent) {
        try {
            log.info("嘗試直接下載圖片（降級方案）: {}", imageUrl);

            String bookCoverPath = uploadPath + "/book-covers";
            String fileName = "isbn_" + isbn;

            String savedPath = ImageDownloadUtil.downloadImageWithCookies(
                    imageUrl, bookCoverPath, fileName, cookies, userAgent
            );

            if (savedPath != null && savedPath.contains("/book-covers/")) {
                return "/profile/book-covers/" +
                        savedPath.substring(savedPath.lastIndexOf("/") + 1);
            }
            return savedPath;
        } catch (Exception e) {
            log.error("直接下載圖片失敗: {}", e.getMessage(), e);
            return null;
        }
    }
}
