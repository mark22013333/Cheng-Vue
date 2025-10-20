package com.cheng.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 書籍原始資料 DTO
 * 用於批次爬蟲的中間資料傳遞
 * 
 * @author Cheng
 * @since 2025-10-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRawDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 書名（原始）
     */
    private String rawTitle;

    /**
     * 作者（原始）
     */
    private String rawAuthor;

    /**
     * 出版社（原始）
     */
    private String rawPublisher;

    /**
     * 出版日期（原始）
     */
    private String rawPublishDate;

    /**
     * 分類（原始）
     */
    private String rawCategory;

    /**
     * 簡介（原始）
     */
    private String rawIntroduction;

    /**
     * 封面圖片 URL（原始）
     */
    private String rawCoverImageUrl;

    /**
     * 來源網址
     */
    private String sourceUrl;

    /**
     * 爬取時間戳
     */
    private Long crawlTimestamp;
}
