package com.cheng.crawler.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 書籍資訊 DTO
 *
 * @author cheng
 */
@Data
public class BookInfoDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 物品 ID（入庫後產生）
     */
    private Long itemId;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 書名
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版日期
     */
    private String publishDate;

    /**
     * 出版地
     */
    private String publishLocation;

    /**
     * 語言
     */
    private String language;

    /**
     * 版本
     */
    private String edition;

    /**
     * 裝訂
     */
    private String binding;

    /**
     * 分級
     */
    private String classification;

    /**
     * 封面圖片 URL（原始網址）
     */
    private String coverImageUrl;

    /**
     * 封面圖片路徑（下載後的本地路徑）
     */
    private String coverImagePath;

    /**
     * 簡介
     */
    private String introduction;

    /**
     * 來源網址
     */
    private String sourceUrl;

    /**
     * 爬取是否成功
     */
    private Boolean success;

    /**
     * 錯誤訊息（如果失敗）
     */
    private String errorMessage;
}
