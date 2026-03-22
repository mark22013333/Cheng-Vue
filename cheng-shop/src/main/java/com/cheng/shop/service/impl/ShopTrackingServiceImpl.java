package com.cheng.shop.service.impl;

import com.cheng.shop.domain.ShopBrowsingLog;
import com.cheng.shop.domain.ShopSearchLog;
import com.cheng.shop.domain.dto.BrowseEventRequest;
import com.cheng.shop.domain.dto.SearchEventRequest;
import com.cheng.shop.domain.vo.HotProductVO;
import com.cheng.shop.domain.vo.PopularSearchVO;
import com.cheng.shop.mapper.ShopBrowsingLogMapper;
import com.cheng.shop.mapper.ShopSearchLogMapper;
import com.cheng.shop.service.IShopTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 行銷追蹤 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopTrackingServiceImpl implements IShopTrackingService {

    /** 最近瀏覽紀錄的最大回傳筆數 */
    private static final int MAX_RECENT_VIEWS_LIMIT = 50;

    private final ShopBrowsingLogMapper browsingLogMapper;
    private final ShopSearchLogMapper searchLogMapper;

    @Async
    @Override
    public void logBrowse(Long memberId, String guestId, BrowseEventRequest request) {
        try {
            ShopBrowsingLog browsingLog = new ShopBrowsingLog();
            browsingLog.setMemberId(memberId);
            browsingLog.setGuestId(guestId);
            browsingLog.setProductId(request.getProductId());
            browsingLog.setProductName(request.getProductName());
            browsingLog.setCategoryId(request.getCategoryId());
            browsingLog.setSource(request.getSource());
            browsingLog.setCreateTime(LocalDateTime.now());
            browsingLogMapper.insert(browsingLog);
        } catch (Exception e) {
            log.error("記錄瀏覽事件失敗: memberId={}, guestId={}, productId={}", memberId, guestId, request.getProductId(), e);
        }
    }

    @Async
    @Override
    public void logSearch(Long memberId, String guestId, SearchEventRequest request) {
        try {
            ShopSearchLog searchLog = new ShopSearchLog();
            searchLog.setMemberId(memberId);
            searchLog.setGuestId(guestId);
            searchLog.setKeyword(request.getKeyword());
            searchLog.setResultCount(request.getResultCount());
            searchLog.setCreateTime(LocalDateTime.now());
            searchLogMapper.insert(searchLog);
        } catch (Exception e) {
            log.error("記錄搜尋事件失敗: memberId={}, guestId={}, keyword={}", memberId, guestId, request.getKeyword(), e);
        }
    }

    @Async
    @Override
    public void mergeGuestRecords(String guestId, Long memberId) {
        try {
            browsingLogMapper.mergeGuestToMember(guestId, memberId);
            searchLogMapper.mergeGuestToMember(guestId, memberId);
            log.info("訪客紀錄合併完成: guestId={}, memberId={}", guestId, memberId);
        } catch (Exception e) {
            log.error("訪客紀錄合併失敗: guestId={}, memberId={}", guestId, memberId, e);
        }
    }

    @Override
    public List<ShopBrowsingLog> getRecentViews(Long memberId, int limit) {
        int effectiveLimit = Math.min(limit, MAX_RECENT_VIEWS_LIMIT);
        return browsingLogMapper.selectRecentByMemberId(memberId, effectiveLimit);
    }

    @Override
    public List<HotProductVO> getHotProducts(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return browsingLogMapper.selectHotProducts(since, limit);
    }

    @Override
    public List<PopularSearchVO> getPopularSearches(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return searchLogMapper.selectPopularKeywords(since, limit);
    }
}
