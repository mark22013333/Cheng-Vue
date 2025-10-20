package com.cheng.system.service.impl;

import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.crawler.dto.BookInfoDTO;
import com.cheng.crawler.service.IIsbnCrawlerService;
import com.cheng.system.domain.InvBookInfo;
import com.cheng.system.domain.InvItem;
import com.cheng.system.domain.InvStock;
import com.cheng.system.mapper.InvItemMapper;
import com.cheng.system.mapper.InvStockMapper;
import com.cheng.system.service.IBookItemService;
import com.cheng.system.service.IInvBookInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 書籍物品整合服務實現
 *
 * @author cheng
 */
@Slf4j
@Service
public class BookItemServiceImpl implements IBookItemService {

    /**
     * 書籍分類 ID（對應資料庫 inv_category 表中的「書籍」分類）
     */
    private static final Long BOOK_CATEGORY_ID = 1000L;

    @Autowired
    private IIsbnCrawlerService isbnCrawlerService;

    @Autowired
    private IInvBookInfoService invBookInfoService;

    @Autowired
    private InvItemMapper invItemMapper;

    @Autowired
    private InvStockMapper invStockMapper;

    /**
     * 根據 ISBN 建立或取得書籍物品
     */
    @Override
    @Transactional
    public InvItem createOrGetBookItem(String isbn) {
        if (StringUtils.isEmpty(isbn)) {
            throw new ServiceException("ISBN 不能為空");
        }

        // 1. 檢查 ISBN 是否已存在
        InvBookInfo existingBook = invBookInfoService.selectInvBookInfoByIsbn(isbn);
        if (existingBook != null) {
            if (existingBook.getItemId() != null) {
                log.info("書籍已存在，ISBN: {}, ItemId: {}", isbn, existingBook.getItemId());
                InvItem existingItem = invItemMapper.selectInvItemByItemId(existingBook.getItemId());
                if (existingItem != null) {
                    return existingItem;
                }
                log.warn("書籍資訊存在但物品不存在，ItemId: {}，將重新建立物品並關聯", existingBook.getItemId());
            }
            
            // 如果書籍資訊存在但物品不存在或已刪除，重新爬取並建立物品
            log.info("開始爬取書籍資訊以重建物品，ISBN: {}", isbn);
            BookInfoDTO bookInfoDTO = isbnCrawlerService.crawlByIsbn(isbn);
            
            if (!bookInfoDTO.getSuccess()) {
                throw new ServiceException("爬取書籍資訊失敗: " + bookInfoDTO.getErrorMessage());
            }
            
            // 建立新物品並更新現有書籍資訊的關聯
            return recreateItemForExistingBook(existingBook, bookInfoDTO);
        }

        // 2. 書籍資訊不存在，爬取新書籍資訊
        log.info("開始爬取新書籍資訊，ISBN: {}", isbn);
        BookInfoDTO bookInfoDTO = isbnCrawlerService.crawlByIsbn(isbn);

        if (!bookInfoDTO.getSuccess()) {
            throw new ServiceException("爬取書籍資訊失敗: " + bookInfoDTO.getErrorMessage());
        }

        // 3. 建立物品和書籍資訊
        return createBookItemFromDTO(bookInfoDTO);
    }

    /**
     * 根據 ISBN 和已爬取的書籍資訊建立或取得物品
     * 使用已爬取的 BookInfoDTO，避免重複爬取
     */
    @Override
    @Transactional
    public InvItem createOrGetBookItem(String isbn, BookInfoDTO bookInfoDTO) {
        if (StringUtils.isEmpty(isbn)) {
            throw new ServiceException("ISBN 不能為空");
        }
        
        if (bookInfoDTO == null || !bookInfoDTO.getSuccess()) {
            throw new ServiceException("書籍資訊無效");
        }

        // 1. 檢查 ISBN 是否已存在
        InvBookInfo existingBook = invBookInfoService.selectInvBookInfoByIsbn(isbn);
        if (existingBook != null) {
            if (existingBook.getItemId() != null) {
                log.info("書籍已存在，ISBN: {}, ItemId: {}", isbn, existingBook.getItemId());
                InvItem existingItem = invItemMapper.selectInvItemByItemId(existingBook.getItemId());
                if (existingItem != null) {
                    // 更新書籍資訊（可能有更新）
                    updateExistingBookInfo(existingBook, bookInfoDTO);
                    return existingItem;
                }
                log.warn("書籍資訊存在但物品不存在，ItemId: {}，將重新建立物品並關聯", existingBook.getItemId());
            }
            
            // 如果書籍資訊存在但物品不存在或已刪除，重新建立物品
            return recreateItemForExistingBook(existingBook, bookInfoDTO);
        }

        // 2. 書籍資訊不存在，使用提供的 DTO 建立新物品
        log.info("使用提供的書籍資訊建立新物品，ISBN: {}", isbn);
        return createBookItemFromDTO(bookInfoDTO);
    }

    /**
     * 更新現有書籍資訊
     */
    private void updateExistingBookInfo(InvBookInfo existingBook, BookInfoDTO bookInfoDTO) {
        existingBook.setTitle(bookInfoDTO.getTitle());
        existingBook.setAuthor(bookInfoDTO.getAuthor());
        existingBook.setPublisher(bookInfoDTO.getPublisher());
        existingBook.setPublishDate(bookInfoDTO.getPublishDate());
        existingBook.setPublishLocation(bookInfoDTO.getPublishLocation());
        existingBook.setLanguage(bookInfoDTO.getLanguage());
        existingBook.setEdition(bookInfoDTO.getEdition());
        existingBook.setBinding(bookInfoDTO.getBinding());
        existingBook.setClassification(bookInfoDTO.getClassification());
        existingBook.setCoverImagePath(bookInfoDTO.getCoverImagePath());
        existingBook.setIntroduction(bookInfoDTO.getIntroduction());
        existingBook.setSourceUrl(bookInfoDTO.getSourceUrl());
        existingBook.setCrawlTime(new Date());
        invBookInfoService.updateInvBookInfo(existingBook);
        log.info("更新書籍資訊成功，ISBN: {}", existingBook.getIsbn());
    }

    /**
     * 為已存在的書籍資訊重新建立物品
     * 用於處理書籍資訊存在但物品已被刪除的髒資料情況
     */
    @Transactional
    protected InvItem recreateItemForExistingBook(InvBookInfo existingBook, BookInfoDTO bookInfoDTO) {
        try {
            // 1. 建立新物品記錄
            InvItem item = new InvItem();
            item.setItemCode("BOOK-" + bookInfoDTO.getIsbn());
            item.setItemName(bookInfoDTO.getTitle());
            item.setCategoryId(BOOK_CATEGORY_ID);
            item.setBarcode(bookInfoDTO.getIsbn());
            item.setSpecification(buildSpecification(bookInfoDTO));
            item.setUnit("本");
            item.setBrand(bookInfoDTO.getPublisher());
            item.setModel(bookInfoDTO.getEdition());
            item.setDescription(truncateDescription(bookInfoDTO.getIntroduction()));
            item.setImageUrl(bookInfoDTO.getCoverImagePath());
            item.setStatus("0");
            item.setDelFlag("0");
            item.setCreateBy(SecurityUtils.getUsername());
            item.setCreateTime(new Date());
            item.setRemark("重新建立（原物品已刪除）");

            int itemResult = invItemMapper.insertInvItem(item);
            if (itemResult <= 0) {
                throw new ServiceException("建立物品記錄失敗");
            }
            log.info("重新建立物品成功，ItemId: {}, ISBN: {}", item.getItemId(), bookInfoDTO.getIsbn());

            // 2. 更新現有書籍資訊的 item_id 關聯
            existingBook.setItemId(item.getItemId());
            existingBook.setTitle(bookInfoDTO.getTitle());
            existingBook.setAuthor(bookInfoDTO.getAuthor());
            existingBook.setPublisher(bookInfoDTO.getPublisher());
            existingBook.setPublishDate(bookInfoDTO.getPublishDate());
            existingBook.setPublishLocation(bookInfoDTO.getPublishLocation());
            existingBook.setLanguage(bookInfoDTO.getLanguage());
            existingBook.setEdition(bookInfoDTO.getEdition());
            existingBook.setBinding(bookInfoDTO.getBinding());
            existingBook.setClassification(bookInfoDTO.getClassification());
            existingBook.setCoverImagePath(bookInfoDTO.getCoverImagePath());
            existingBook.setIntroduction(bookInfoDTO.getIntroduction());
            existingBook.setSourceUrl(bookInfoDTO.getSourceUrl());
            existingBook.setCrawlTime(new Date());
            existingBook.setUpdateBy(SecurityUtils.getUsername());
            existingBook.setUpdateTime(new Date());

            int bookInfoResult = invBookInfoService.updateInvBookInfo(existingBook);
            if (bookInfoResult <= 0) {
                throw new ServiceException("更新書籍資訊失敗");
            }
            log.info("更新書籍資訊關聯成功，BookInfoId: {}, 新 ItemId: {}", 
                existingBook.getBookInfoId(), item.getItemId());

            // 3. 初始化或更新庫存記錄
            // 先檢查是否已存在庫存記錄（可能是舊物品的殘留）
            InvStock existingStock = invStockMapper.selectInvStockByItemId(item.getItemId());
            
            if (existingStock != null) {
                // 更新現有庫存記錄
                log.info("發現舊庫存記錄，StockId: {}，將重置為初始狀態", existingStock.getStockId());
                existingStock.setTotalQuantity(0);
                existingStock.setAvailableQty(0);
                existingStock.setBorrowedQty(0);
                existingStock.setReservedQty(0);
                existingStock.setDamagedQty(0);
                existingStock.setLostQty(0);
                existingStock.setUpdateTime(new Date());
                
                int stockResult = invStockMapper.updateInvStock(existingStock);
                if (stockResult <= 0) {
                    throw new ServiceException("更新庫存記錄失敗");
                }
                log.info("庫存記錄已重置，StockId: {}", existingStock.getStockId());
            } else {
                // 建立新庫存記錄
                InvStock stock = new InvStock();
                stock.setItemId(item.getItemId());
                stock.setTotalQuantity(0);
                stock.setAvailableQty(0);
                stock.setBorrowedQty(0);
                stock.setReservedQty(0);
                stock.setDamagedQty(0);
                stock.setLostQty(0);
                stock.setUpdateTime(new Date());

                int stockResult = invStockMapper.insertInvStock(stock);
                if (stockResult <= 0) {
                    throw new ServiceException("初始化庫存記錄失敗");
                }
                log.info("初始化庫存成功，StockId: {}", stock.getStockId());
            }

            return item;

        } catch (Exception e) {
            log.error("重新建立書籍物品時發生錯誤: {}", e.getMessage(), e);
            throw new ServiceException("重新建立書籍物品失敗: " + e.getMessage());
        }
    }

    /**
     * 根據爬取的書籍資訊建立物品
     */
    @Override
    @Transactional
    public InvItem createBookItemFromDTO(BookInfoDTO bookInfoDTO) {
        if (bookInfoDTO == null || StringUtils.isEmpty(bookInfoDTO.getIsbn())) {
            throw new ServiceException("書籍資訊不完整");
        }

        try {
            // 1. 建立物品記錄
            InvItem item = new InvItem();
            item.setItemCode("BOOK-" + bookInfoDTO.getIsbn());
            item.setItemName(bookInfoDTO.getTitle());
            item.setCategoryId(BOOK_CATEGORY_ID);
            item.setBarcode(bookInfoDTO.getIsbn());
            item.setSpecification(buildSpecification(bookInfoDTO));
            item.setUnit("本");
            item.setBrand(bookInfoDTO.getPublisher());
            item.setModel(bookInfoDTO.getEdition());
            item.setDescription(truncateDescription(bookInfoDTO.getIntroduction()));
            item.setImageUrl(bookInfoDTO.getCoverImagePath());
            item.setStatus("0");
            item.setDelFlag("0");
            item.setCreateBy(SecurityUtils.getUsername());
            item.setCreateTime(new Date());
            item.setRemark("由 ISBN 自動建立");

            int itemResult = invItemMapper.insertInvItem(item);
            if (itemResult <= 0) {
                throw new ServiceException("建立物品記錄失敗");
            }
            log.info("建立物品成功，ItemId: {}, ISBN: {}", item.getItemId(), bookInfoDTO.getIsbn());

            // 2. 建立書籍資訊記錄
            InvBookInfo bookInfo = new InvBookInfo();
            bookInfo.setItemId(item.getItemId());
            bookInfo.setIsbn(bookInfoDTO.getIsbn());
            bookInfo.setTitle(bookInfoDTO.getTitle());
            bookInfo.setAuthor(bookInfoDTO.getAuthor());
            bookInfo.setPublisher(bookInfoDTO.getPublisher());
            bookInfo.setPublishDate(bookInfoDTO.getPublishDate());
            bookInfo.setPublishLocation(bookInfoDTO.getPublishLocation());
            bookInfo.setLanguage(bookInfoDTO.getLanguage());
            bookInfo.setEdition(bookInfoDTO.getEdition());
            bookInfo.setBinding(bookInfoDTO.getBinding());
            bookInfo.setClassification(bookInfoDTO.getClassification());
            bookInfo.setCoverImagePath(bookInfoDTO.getCoverImagePath());
            bookInfo.setIntroduction(bookInfoDTO.getIntroduction());
            bookInfo.setSourceUrl(bookInfoDTO.getSourceUrl());
            bookInfo.setCrawlTime(new Date());
            bookInfo.setStatus("0");
            bookInfo.setCreateBy(SecurityUtils.getUsername());
            bookInfo.setCreateTime(new Date());

            int bookInfoResult = invBookInfoService.insertInvBookInfo(bookInfo);
            if (bookInfoResult <= 0) {
                throw new ServiceException("建立書籍資訊失敗");
            }
            log.info("建立書籍資訊成功，BookInfoId: {}", bookInfo.getBookInfoId());

            // 3. 初始化庫存記錄
            InvStock stock = new InvStock();
            stock.setItemId(item.getItemId());
            stock.setTotalQuantity(0);
            stock.setAvailableQty(0);
            stock.setBorrowedQty(0);
            stock.setReservedQty(0);
            stock.setDamagedQty(0);
            stock.setLostQty(0);
            stock.setUpdateTime(new Date());

            int stockResult = invStockMapper.insertInvStock(stock);
            if (stockResult <= 0) {
                throw new ServiceException("初始化庫存記錄失敗");
            }
            log.info("初始化庫存成功，StockId: {}", stock.getStockId());

            return item;

        } catch (Exception e) {
            log.error("建立書籍物品時發生錯誤: {}", e.getMessage(), e);
            throw new ServiceException("建立書籍物品失敗: " + e.getMessage());
        }
    }

    /**
     * 更新物品的書籍資訊
     */
    @Override
    @Transactional
    public int updateBookInfo(Long itemId, BookInfoDTO bookInfoDTO) {
        if (itemId == null || bookInfoDTO == null) {
            throw new ServiceException("參數不能為空");
        }

        // 檢查物品是否存在
        InvItem item = invItemMapper.selectInvItemByItemId(itemId);
        if (item == null) {
            throw new ServiceException("物品不存在");
        }

        // 更新書籍資訊
        InvBookInfo bookInfo = invBookInfoService.selectInvBookInfoByItemId(itemId);
        if (bookInfo == null) {
            throw new ServiceException("找不到對應的書籍資訊");
        }

        bookInfo.setTitle(bookInfoDTO.getTitle());
        bookInfo.setAuthor(bookInfoDTO.getAuthor());
        bookInfo.setPublisher(bookInfoDTO.getPublisher());
        bookInfo.setPublishDate(bookInfoDTO.getPublishDate());
        bookInfo.setPublishLocation(bookInfoDTO.getPublishLocation());
        bookInfo.setLanguage(bookInfoDTO.getLanguage());
        bookInfo.setEdition(bookInfoDTO.getEdition());
        bookInfo.setBinding(bookInfoDTO.getBinding());
        bookInfo.setClassification(bookInfoDTO.getClassification());
        bookInfo.setIntroduction(bookInfoDTO.getIntroduction());
        bookInfo.setUpdateBy(SecurityUtils.getUsername());
        bookInfo.setUpdateTime(new Date());

        if (StringUtils.isNotEmpty(bookInfoDTO.getCoverImagePath())) {
            bookInfo.setCoverImagePath(bookInfoDTO.getCoverImagePath());
        }

        return invBookInfoService.updateInvBookInfo(bookInfo);
    }

    /**
     * 建立規格描述
     */
    private String buildSpecification(BookInfoDTO bookInfoDTO) {
        StringBuilder spec = new StringBuilder();
        if (StringUtils.isNotEmpty(bookInfoDTO.getLanguage())) {
            spec.append(bookInfoDTO.getLanguage());
        }
        if (StringUtils.isNotEmpty(bookInfoDTO.getBinding())) {
            if (!spec.isEmpty()) spec.append(" / ");
            spec.append(bookInfoDTO.getBinding());
        }
        if (StringUtils.isNotEmpty(bookInfoDTO.getPublishDate())) {
            if (!spec.isEmpty()) spec.append(" / ");
            spec.append(bookInfoDTO.getPublishDate());
        }
        return spec.toString();
    }

    /**
     * 截斷簡介文字（資料庫 description 為 TEXT 類型，但為了效能考量可以截斷）
     */
    private String truncateDescription(String description) {
        if (StringUtils.isEmpty(description)) {
            return "";
        }
        // 保留前 2000 字元
        return description.length() > 2000 ? description.substring(0, 2000) + "..." : description;
    }
}
