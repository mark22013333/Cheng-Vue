package com.cheng.shop.service;

import com.cheng.shop.domain.ShopBrowsingLog;
import com.cheng.shop.domain.dto.BrowseEventRequest;
import com.cheng.shop.domain.dto.SearchEventRequest;
import com.cheng.shop.domain.vo.HotProductVO;
import com.cheng.shop.domain.vo.PopularSearchVO;

import java.util.List;

/**
 * 行銷追蹤 Service 介面
 *
 * @author cheng
 */
public interface IShopTrackingService {

    /**
     * 記錄商品瀏覽事件
     *
     * @param memberId 會員ID（已登入時提供，可為 null）
     * @param guestId  訪客識別碼（未登入時使用，可為 null）
     * @param request  瀏覽事件請求
     */
    void logBrowse(Long memberId, String guestId, BrowseEventRequest request);

    /**
     * 記錄搜尋事件
     *
     * @param memberId 會員ID（已登入時提供，可為 null）
     * @param guestId  訪客識別碼（未登入時使用，可為 null）
     * @param request  搜尋事件請求
     */
    void logSearch(Long memberId, String guestId, SearchEventRequest request);

    /**
     * 將訪客紀錄合併至會員（登入後呼叫）
     *
     * @param guestId  訪客識別碼
     * @param memberId 會員ID
     */
    void mergeGuestRecords(String guestId, Long memberId);

    /**
     * 查詢會員最近瀏覽紀錄
     *
     * @param memberId 會員ID
     * @param limit    筆數上限
     * @return 瀏覽紀錄列表
     */
    List<ShopBrowsingLog> getRecentViews(Long memberId, int limit);

    /**
     * 查詢熱門商品
     *
     * @param days  統計天數
     * @param limit 筆數上限
     * @return 熱門商品列表
     */
    List<HotProductVO> getHotProducts(int days, int limit);

    /**
     * 查詢熱門搜尋關鍵字
     *
     * @param days  統計天數
     * @param limit 筆數上限
     * @return 熱門搜尋列表
     */
    List<PopularSearchVO> getPopularSearches(int days, int limit);
}
