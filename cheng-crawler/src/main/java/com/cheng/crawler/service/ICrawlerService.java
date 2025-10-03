package com.cheng.crawler.service;

import com.cheng.crawler.dto.CrawledData;

/**
 * 爬蟲服務介面：根據掃描內容抓取外部資料。
 */
public interface ICrawlerService {

    /**
     * 根據輸入內容自動判斷來源並爬取資料。
     * 支援：URL（http/https）、一般商品代碼/條碼等。
     *
     * @param input 掃描內容（如商品代碼、網址、編號等）
     * @return 爬取與整理後的資料
     */
    CrawledData crawl(String input);
}
