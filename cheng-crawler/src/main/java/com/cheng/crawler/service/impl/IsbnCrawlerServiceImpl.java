package com.cheng.crawler.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.crawler.service.IIsbnCrawlerService;
import com.cheng.crawler.utils.ImageDownloadUtil;
import com.cheng.crawler.utils.SeleniumUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * ISBN 書籍資訊爬蟲服務實現
 *
 * @author cheng
 */
@Service
public class IsbnCrawlerServiceImpl implements IIsbnCrawlerService {
    private static final Logger log = LoggerFactory.getLogger(IsbnCrawlerServiceImpl.class);
    private static final String ISBN_TW_URL = "https://isbn.tw/";
    private static final String ISBN_US_URL = "https://us.nicebooks.com/search/isbn?isbn=";
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private static final int TIMEOUT = 10000; // 10秒

    @Value("${cheng.profile:/tmp/uploadPath}")
    private String uploadPath;

    @Override
    public BookInfoDTO crawlByIsbn(String isbn) {
        // 第一層：嘗試從台灣主站搜尋
        log.info("[第1層] 嘗試從台灣主站搜尋 ISBN: {}", isbn);
        BookInfoDTO bookInfo = crawlByIsbnFromTw(isbn);
        
        // 第二層：如果主站搜尋失敗或未找到書籍，則使用美國站備援
        if (bookInfo == null || !bookInfo.getSuccess() || isBookInfoIncomplete(bookInfo)) {
            log.info("[第2層] 台灣主站搜尋失敗或資訊不足，切換到美國站備援搜尋 ISBN: {}", isbn);
            bookInfo = crawlByIsbnFromUs(isbn);
        }
        
        // 第三層：如果美國站也失敗或資訊不足，則使用 Google Books API
        if (bookInfo == null || !bookInfo.getSuccess() || isBookInfoIncomplete(bookInfo)) {
            log.info("[第3層] 美國站搜尋失敗或資訊不足，切換到 Google Books API 搜尋 ISBN: {}", isbn);
            bookInfo = crawlByIsbnFromGoogle(isbn);
        }
        
        return bookInfo;
    }

    /**
     * 判斷書籍資訊是否不完整（沒有書名）
     */
    private boolean isBookInfoIncomplete(BookInfoDTO bookInfo) {
        return bookInfo.getTitle() == null || bookInfo.getTitle().trim().isEmpty();
    }

    @Override
    public BookInfoDTO crawlByIsbnFromTw(String isbn) {
        BookInfoDTO bookInfo = new BookInfoDTO();
        bookInfo.setIsbn(isbn);
        bookInfo.setSuccess(false);

        if (isbn == null || isbn.trim().isEmpty()) {
            bookInfo.setErrorMessage("ISBN 不能為空");
            return bookInfo;
        }

        String targetUrl = ISBN_TW_URL + isbn.trim();
        bookInfo.setSourceUrl(targetUrl);

        try {
            log.info("開始從台灣站爬取 ISBN 資訊: {}", targetUrl);

            // 連接並取得頁面
            Document doc = Jsoup.connect(targetUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .referrer("https://isbn.tw/")
                    .timeout(TIMEOUT)
                    .get();

            // 解析書名
            Element titleElement = doc.selectFirst("h1.h3.mb-2.font-weight-bold");
            if (titleElement != null) {
                bookInfo.setTitle(titleElement.text().trim());
            }

            // 解析定義列表（dl）中的各項資訊
            Elements dlElements = doc.select("dl.row.mb-0");
            for (Element dl : dlElements) {
                Element dt = dl.selectFirst("dt");
                Element dd = dl.selectFirst("dd");

                if (dt != null && dd != null) {
                    String label = dt.text().trim();
                    String value = dd.text().trim();

                    switch (label) {
                        case "作者":
                            bookInfo.setAuthor(value);
                            break;
                        case "出版社":
                            bookInfo.setPublisher(value);
                            break;
                        case "出版日期":
                            bookInfo.setPublishDate(value);
                            break;
                        case "出版地":
                            bookInfo.setPublishLocation(value);
                            break;
                        case "語言":
                            bookInfo.setLanguage(value);
                            break;
                        case "版本":
                            bookInfo.setEdition(value);
                            break;
                        case "裝訂":
                            bookInfo.setBinding(value);
                            break;
                        case "分級":
                            bookInfo.setClassification(value);
                            break;
                    }
                }
            }

            // 解析簡介
            Element introCard = doc.selectFirst("div.card.mb-4");
            if (introCard != null) {
                Element introHeader = introCard.selectFirst("h2.h6.card-header");
                if (introHeader != null && "簡介".equals(introHeader.text().trim())) {
                    Element introBody = introCard.selectFirst("div.card-body p");
                    if (introBody != null) {
                        // 取得 HTML 內容並轉換 <br> 為換行
                        String introHtml = introBody.html();
                        String introText = introHtml.replaceAll("<br\\s*/?>", "\n");
                        // 移除其他 HTML 標籤
                        introText = Jsoup.parse(introText).text();
                        bookInfo.setIntroduction(introText.trim());
                    }
                }
            }

            // 解析封面圖片
            Element coverImg = doc.selectFirst("div.embed-responsive-item img");
            if (coverImg != null) {
                String imgSrc = coverImg.attr("src");
                if (!imgSrc.isEmpty()) {
                    // 處理相對路徑
                    if (imgSrc.startsWith("/")) {
                        imgSrc = "https://isbn.tw" + imgSrc;
                    }
                    bookInfo.setCoverImageUrl(imgSrc);

                    // 下載圖片
                    String savedPath = downloadCoverImage(imgSrc, isbn);
                    if (savedPath != null) {
                        bookInfo.setCoverImagePath(savedPath);
                    }
                }
            }

            bookInfo.setSuccess(true);
            log.info("台灣站 ISBN 資訊爬取成功: {}", bookInfo.getTitle());

        } catch (IOException e) {
            log.error("台灣站爬取 ISBN 資訊時發生錯誤: {}", e.getMessage(), e);
            bookInfo.setErrorMessage("台灣站爬取失敗: " + e.getMessage());
        } catch (Exception e) {
            log.error("台灣站處理 ISBN 資訊時發生未預期錯誤: {}", e.getMessage(), e);
            bookInfo.setErrorMessage("台灣站處理失敗: " + e.getMessage());
        }

        return bookInfo;
    }

    @Override
    public BookInfoDTO crawlByIsbnFromUs(String isbn) {
        BookInfoDTO bookInfo = new BookInfoDTO();
        bookInfo.setIsbn(isbn);
        bookInfo.setSuccess(false);

        if (isbn == null || isbn.trim().isEmpty()) {
            bookInfo.setErrorMessage("ISBN 不能為空");
            return bookInfo;
        }

        String targetUrl = ISBN_US_URL + isbn.trim();
        bookInfo.setSourceUrl(targetUrl);

        WebDriver driver = null;
        try {
            log.info("開始從美國站爬取 ISBN 資訊（使用 Selenium）: {}", targetUrl);

            // 使用 SeleniumUtil 建立 WebDriver（包含自動設定 ChromeDriver 路徑和反爬蟲配置）
            driver = SeleniumUtil.createWebDriver();
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
                                    } catch (Exception ignored) {}
                                }
                            }
                            break;
                        }
                    } catch (Exception ignored) {}
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
                    .timeout(TIMEOUT)
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
        
        switch (langCode.toLowerCase()) {
            case "zh-cn":
                return "簡體中文";
            case "zh-tw":
                return "繁體中文";
            case "en":
                return "英文";
            case "ja":
                return "日文";
            case "ko":
                return "韓文";
            case "fr":
                return "法文";
            case "de":
                return "德文";
            case "es":
                return "西班牙文";
            default:
                return langCode.toUpperCase();
        }
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
}
