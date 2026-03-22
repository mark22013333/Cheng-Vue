package com.cheng.shop.controller;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.annotation.PublicApi;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.shop.domain.dto.BrowseEventRequest;
import com.cheng.shop.domain.dto.SearchEventRequest;
import com.cheng.shop.domain.vo.HotProductVO;
import com.cheng.shop.domain.vo.PopularSearchVO;
import com.cheng.shop.service.IShopTrackingService;
import com.cheng.shop.utils.ShopMemberSecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 行銷追蹤公開 Controller（前台，免登入）
 *
 * @author cheng
 */
@Slf4j
@Anonymous
@PublicApi
@RestController
@RequestMapping("/shop/front/tracking")
@RequiredArgsConstructor
public class ShopFrontTrackingController extends BaseController {

    private final IShopTrackingService trackingService;

    /**
     * 記錄商品瀏覽事件（支援訪客與會員）
     */
    @PostMapping("/browse")
    public AjaxResult logBrowse(@Valid @RequestBody BrowseEventRequest request) {
        Long memberId = tryGetMemberId();
        String guestId = request.getGuestId();
        if (memberId == null && (guestId == null || guestId.isEmpty())) {
            return error("缺少身份識別：請登入或提供 guestId");
        }
        trackingService.logBrowse(memberId, guestId, request);
        return success();
    }

    /**
     * 記錄搜尋事件（支援訪客與會員）
     */
    @PostMapping("/search")
    public AjaxResult logSearch(@Valid @RequestBody SearchEventRequest request) {
        Long memberId = tryGetMemberId();
        String guestId = request.getGuestId();
        if (memberId == null && (guestId == null || guestId.isEmpty())) {
            return error("缺少身份識別：請登入或提供 guestId");
        }
        trackingService.logSearch(memberId, guestId, request);
        return success();
    }

    /**
     * 嘗試從 SecurityContext 取得會員ID（未登入時回傳 null）
     */
    private Long tryGetMemberId() {
        try {
            return ShopMemberSecurityUtils.getMemberId();
        } catch (Exception e) {
            // 未登入，使用 guestId
            return null;
        }
    }

    /**
     * 查詢熱門商品
     */
    @GetMapping("/hot-products")
    public AjaxResult getHotProducts(@RequestParam(defaultValue = "7") int days,
                                     @RequestParam(defaultValue = "10") int limit) {
        List<HotProductVO> list = trackingService.getHotProducts(days, limit);
        return success(list);
    }

    /**
     * 查詢熱門搜尋關鍵字
     */
    @GetMapping("/popular-searches")
    public AjaxResult getPopularSearches(@RequestParam(defaultValue = "7") int days,
                                         @RequestParam(defaultValue = "10") int limit) {
        List<PopularSearchVO> list = trackingService.getPopularSearches(days, limit);
        return success(list);
    }
}
