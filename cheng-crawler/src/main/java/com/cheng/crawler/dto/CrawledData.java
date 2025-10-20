package com.cheng.crawler.dto;

import com.cheng.crawler.enums.CrawlerType;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 爬蟲結果資料
 */
@Data
public class CrawledData implements Serializable {
    /**
     * 原始輸入
     */
    private String input;
    /**
     * 爬蟲種類代號
     */
    private CrawlerType crawlerType;
    /**
     * 簡要標題或名稱
     */
    private String title;
    /**
     * 主要內容節選或說明
     */
    private String summary;
    /**
     * 其他鍵值資料（可擴充）
     */
    private Map<String, Object> attributes;
}
