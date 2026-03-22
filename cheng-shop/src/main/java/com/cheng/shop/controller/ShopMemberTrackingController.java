package com.cheng.shop.controller;

import com.cheng.common.annotation.PublicApi;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.shop.domain.ShopBrowsingLog;
import com.cheng.shop.service.IShopTrackingService;
import com.cheng.shop.utils.ShopMemberSecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 會員行銷追蹤 Controller（前台，需登入）
 *
 * @author cheng
 */
@PublicApi
@RestController
@RequestMapping("/shop/member/tracking")
@RequiredArgsConstructor
public class ShopMemberTrackingController extends BaseController {

    private final IShopTrackingService trackingService;

    /**
     * 查詢最近瀏覽紀錄
     */
    @GetMapping("/recent-views")
    public AjaxResult getRecentViews(@RequestParam(defaultValue = "10") int limit) {
        Long memberId = ShopMemberSecurityUtils.getMemberId();
        List<ShopBrowsingLog> list = trackingService.getRecentViews(memberId, limit);
        return success(list);
    }

    /**
     * 將訪客紀錄合併至當前會員（登入後呼叫）
     */
    @PostMapping("/merge")
    public AjaxResult mergeGuestRecords(@RequestBody Map<String, String> body) {
        String guestId = body.get("guestId");
        if (guestId == null || guestId.isEmpty()) {
            return error("缺少 guestId");
        }
        Long memberId = ShopMemberSecurityUtils.getMemberId();
        trackingService.mergeGuestRecords(guestId, memberId);
        return success();
    }
}
