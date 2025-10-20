package com.cheng.crawler.service;

import com.cheng.crawler.dto.BookInfoDTO;

/**
 * ISBN 書籍資訊爬蟲服務介面
 *
 * @author cheng
 */
public interface IIsbnCrawlerService {

    /**
     * 根據 ISBN 爬取書籍資訊
     * 先從 isbn.tw 搜尋，若失敗則自動 fallback 到 NiceBooks 美國站
     *
     * @param isbn ISBN 編號
     * @return 書籍資訊 DTO
     */
    BookInfoDTO crawlByIsbn(String isbn);

    /**
     * 根據 ISBN 從 isbn.tw 爬取書籍資訊（台灣主站）
     *
     * @param isbn ISBN 編號
     * @return 書籍資訊 DTO
     */
    BookInfoDTO crawlByIsbnFromTw(String isbn);

    /**
     * 根據 ISBN 從 NiceBooks 美國站爬取書籍資訊（備援站）
     *
     * @param isbn ISBN 編號
     * @return 書籍資訊 DTO
     */
    BookInfoDTO crawlByIsbnFromUs(String isbn);

    /**
     * 根據 ISBN 從 Google Books API 爬取書籍資訊（第三層備援）
     *
     * @param isbn ISBN 編號
     * @return 書籍資訊 DTO
     */
    BookInfoDTO crawlByIsbnFromGoogle(String isbn);
}
