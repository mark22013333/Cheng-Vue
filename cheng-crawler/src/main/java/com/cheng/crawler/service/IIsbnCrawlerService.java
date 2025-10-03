package com.cheng.crawler.service;

import com.cheng.crawler.dto.BookInfoDTO;

/**
 * ISBN 書籍資訊爬蟲服務介面
 *
 * @author cheng
 */
public interface IIsbnCrawlerService {

    /**
     * 根據 ISBN 從 isbn.tw 爬取書籍資訊
     *
     * @param isbn ISBN 編號
     * @return 書籍資訊 DTO
     */
    BookInfoDTO crawlByIsbn(String isbn);
}
