package com.cheng.system.service;

import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.system.domain.InvItem;

/**
 * 書籍物品整合服務介面
 * 處理 ISBN 掃描後的書籍資訊儲存與物品建立
 *
 * @author cheng
 */
public interface IBookItemService {

    /**
     * 根據 ISBN 建立或更新書籍物品（會自動爬取書籍資訊）
     * 1. 檢查 ISBN 是否已存在
     * 2. 不存在則爬取書籍資訊並建立物品
     * 3. 存在則回傳既有物品資訊
     *
     * @param isbn ISBN 編號
     * @return 物品資訊（包含書籍資訊）
     */
    InvItem createOrGetBookItem(String isbn);

    /**
     * 根據 ISBN 和已爬取的書籍資訊建立或取得物品
     * 1. 檢查 ISBN 是否已存在
     * 2. 不存在則使用提供的書籍資訊建立物品
     * 3. 存在則更新書籍資訊並回傳既有物品
     *
     * @param isbn        ISBN 編號
     * @param bookInfoDTO 已爬取的書籍資訊
     * @return 物品資訊
     */
    InvItem createOrGetBookItem(String isbn, BookInfoDTO bookInfoDTO);

    /**
     * 根據爬取的書籍資訊建立物品
     * 1. 建立物品記錄（inv_item）
     * 2. 建立書籍資訊記錄（inv_book_info）
     * 3. 初始化庫存記錄（inv_stock）
     * 4. 下載並儲存封面圖片
     *
     * @param bookInfoDTO 爬取的書籍資訊
     * @return 建立的物品資訊
     */
    InvItem createBookItemFromDTO(BookInfoDTO bookInfoDTO);

    /**
     * 更新物品的書籍資訊
     *
     * @param itemId      物品ID
     * @param bookInfoDTO 書籍資訊
     * @return 更新結果
     */
    int updateBookInfo(Long itemId, BookInfoDTO bookInfoDTO);
}
