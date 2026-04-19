package com.cheng.crawler.service;

import com.cheng.crawler.dto.KtunivalProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ktunival.com.tw 爬蟲服務
 * 搜尋條碼 → 取得商品詳情（圖片、分類、介紹）
 *
 * @author cheng
 */
@Slf4j
@Service
public class KtunivalCrawlerService {

    private static final String BASE_URL = "http://www.ktunival.com.tw/shopCart";
    private static final String SEARCH_URL = BASE_URL + "/show_Prdouct_3_1.asp?skeyword=%s";
    private static final String DETAIL_URL = BASE_URL + "/product.asp?productid=%s";
    private static final Charset BIG5 = Charset.forName("Big5");

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 15000;

    private static final Pattern RESULT_COUNT_PATTERN = Pattern.compile("符合的筆數[^\\d]*(\\d+)");
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("product\\.asp\\?productid=(\\d+)");

    /**
     * 用條碼搜尋商品，若找到 1 筆則自動爬取詳情頁
     *
     * @param barcode 國際條碼
     * @return 商品資訊，若找不到或多筆則 resultCount 反映實際數量
     */
    public KtunivalProductDTO searchByBarcode(String barcode) throws IOException {
        String searchUrl = String.format(SEARCH_URL, barcode);
        log.info("搜尋條碼: {} → {}", barcode, searchUrl);

        Document searchDoc = fetchWithRetry(searchUrl);
        KtunivalProductDTO dto = new KtunivalProductDTO();

        // 解析結果數量
        String pageText = searchDoc.text();
        Matcher countMatcher = RESULT_COUNT_PATTERN.matcher(pageText);
        int resultCount = 0;
        if (countMatcher.find()) {
            resultCount = Integer.parseInt(countMatcher.group(1));
        }
        dto.setResultCount(resultCount);

        if (resultCount != 1) {
            log.info("條碼 {} 搜尋結果: {} 筆", barcode, resultCount);
            return dto;
        }

        // 取得 productid
        Elements links = searchDoc.select("a[href*=product.asp?productid=]");
        if (links.isEmpty()) {
            log.warn("條碼 {} 搜尋到 1 筆但找不到詳情連結", barcode);
            dto.setResultCount(0);
            return dto;
        }

        String href = links.first().attr("href");
        Matcher idMatcher = PRODUCT_ID_PATTERN.matcher(href);
        if (!idMatcher.find()) {
            log.warn("條碼 {} 無法解析 productid: {}", barcode, href);
            dto.setResultCount(0);
            return dto;
        }

        String productId = idMatcher.group(1);
        String detailUrl = String.format(DETAIL_URL, productId);
        dto.setDetailUrl(detailUrl);

        // 請求間隔
        sleep(1000);

        // 爬取詳情頁
        fetchDetailPage(detailUrl, dto);
        return dto;
    }

    /**
     * 解析商品詳情頁
     */
    private void fetchDetailPage(String detailUrl, KtunivalProductDTO dto) throws IOException {
        log.info("爬取詳情頁: {}", detailUrl);
        Document doc = fetchWithRetry(detailUrl);

        // 產品名稱：style16 class 中的文字，位於「產品名稱:」之後
        extractField(doc, "產品名稱", text -> dto.setProductName(text));

        // 產品類別
        extractField(doc, "產品類別", text -> dto.setCategory(text));

        // 產品介紹
        extractField(doc, "產品介紹", text -> dto.setDescription(text));

        // 商品圖片
        Element img = doc.selectFirst("td[align=center] img[src*=product_images]");
        if (img != null) {
            String imgSrc = img.attr("src");
            // 反斜線轉正斜線，組合完整 URL
            imgSrc = imgSrc.replace("\\", "/");
            if (!imgSrc.startsWith("http")) {
                imgSrc = BASE_URL + "/" + imgSrc;
            }
            // URL encode 中文路徑部分
            dto.setImageUrl(encodeChineseInUrl(imgSrc));
        }

        log.info("詳情頁解析完成: 名稱={}, 類別={}, 有圖片={}",
                dto.getProductName(), dto.getCategory(), dto.getImageUrl() != null);
    }

    /**
     * 從詳情頁提取指定欄位的值
     */
    private void extractField(Document doc, String fieldName, java.util.function.Consumer<String> setter) {
        Elements paragraphs = doc.select("p.style10");
        for (Element p : paragraphs) {
            if (p.text().contains(fieldName + ":") || p.text().contains(fieldName + "：")) {
                Elements valueSpans = p.select("span.style16, span.style9");
                if (!valueSpans.isEmpty()) {
                    String text = valueSpans.last().text().trim();
                    if (!text.isEmpty()) {
                        setter.accept(text);
                        return;
                    }
                }
                // 備用：找 font size=4 中的值
                Elements fonts = p.select("font[size=4]");
                for (Element font : fonts) {
                    String text = font.text().trim();
                    if (!text.contains(fieldName) && !text.contains(":") && !text.contains("：") && !text.isEmpty()) {
                        setter.accept(text);
                        return;
                    }
                }
            }
        }
    }

    /**
     * 帶重試的 HTTP 請求
     */
    private Document fetchWithRetry(String url) throws IOException {
        IOException lastException = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return Jsoup.connect(url)
                        .timeout(CONNECT_TIMEOUT_MS)
                        .maxBodySize(0)
                        .followRedirects(true)
                        .header("User-Agent", "Mozilla/5.0")
                        .postDataCharset("Big5")
                        .get();
            } catch (IOException e) {
                lastException = e;
                log.warn("請求失敗（第 {}/{} 次）: {} - {}", i + 1, MAX_RETRIES, url, e.getMessage());
                if (i < MAX_RETRIES - 1) {
                    sleep(RETRY_DELAY_MS);
                }
            }
        }
        throw lastException;
    }

    /**
     * URL 中的中文部分進行 encode
     */
    private String encodeChineseInUrl(String url) {
        StringBuilder result = new StringBuilder();
        for (char c : url.toCharArray()) {
            if (c > 127) {
                result.append(URLEncoder.encode(String.valueOf(c), BIG5));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
