package com.cheng.crawler.service.impl;

import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.crawler.service.IIsbnCrawlerService;
import com.cheng.crawler.util.ImageDownloadUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ISBN 書籍資訊爬蟲服務實現
 *
 * @author cheng
 */
@Service
public class IsbnCrawlerServiceImpl implements IIsbnCrawlerService {
    private static final Logger log = LoggerFactory.getLogger(IsbnCrawlerServiceImpl.class);
    private static final String ISBN_TW_URL = "https://isbn.tw/";
    private static final int TIMEOUT = 10000; // 10秒

    @Value("${cheng.profile:/tmp/uploadPath}")
    private String uploadPath;

    @Override
    public BookInfoDTO crawlByIsbn(String isbn) {
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
            log.info("開始爬取 ISBN 資訊: {}", targetUrl);

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
            log.info("ISBN 資訊爬取成功: {}", bookInfo.getTitle());

        } catch (IOException e) {
            log.error("爬取 ISBN 資訊時發生錯誤: {}", e.getMessage(), e);
            bookInfo.setErrorMessage("爬取失敗: " + e.getMessage());
        } catch (Exception e) {
            log.error("處理 ISBN 資訊時發生未預期錯誤: {}", e.getMessage(), e);
            bookInfo.setErrorMessage("處理失敗: " + e.getMessage());
        }

        return bookInfo;
    }

    /**
     * 下載封面圖片
     *
     * @param imageUrl 圖片 URL
     * @param isbn     ISBN
     * @return 儲存路徑
     */
    private String downloadCoverImage(String imageUrl, String isbn) {
        try {
            // 建立書籍封面儲存目錄
            String bookCoverPath = uploadPath + "/book-covers";
            String fileName = "isbn_" + isbn;

            String savedPath = ImageDownloadUtil.downloadImage(imageUrl, bookCoverPath, fileName);
            if (savedPath != null) {
                log.info("封面圖片下載成功: {}", savedPath);
                return savedPath;
            }
        } catch (Exception e) {
            log.error("下載封面圖片失敗: {}", e.getMessage(), e);
        }
        return null;
    }
}
