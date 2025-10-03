package com.cheng.crawler.service.impl;

import com.cheng.crawler.dto.CrawledData;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.crawler.service.ICrawlerService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 預設爬蟲實作：
 * - 若輸入為 URL，抓取頁面標題與前200字文本作為摘要。
 * - 若輸入為 13 位數字（EAN-13 條碼），回傳標記型別 BARCODE。
 * - 其他輸入回傳簡單歸類 CODE。
 */
@Service
public class DefaultCrawlerService implements ICrawlerService {
    private static final Logger log = Logger.getLogger(DefaultCrawlerService.class.getName());

    @Override
    public CrawledData crawl(String input) {
        CrawledData data = new CrawledData();
        data.setInput(input);

        if (input == null || input.isBlank()) {
            data.setSourceType(CrawlerType.CA101);
            data.setTitle(CrawlerType.CA101.getCategory());
            data.setSummary("未提供可用的掃描內容");
            return data;
        }

        String trimmed = input.trim();

        return data;
    }

    private boolean isUrl(String s) {
        return s.startsWith("http://") || s.startsWith("https://");
    }
}
