package com.cheng.web.controller.shop;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.service.IShopOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 會員訂單 Controller（前台，只需登入不需特定權限）
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/order/my")
@RequiredArgsConstructor
public class ShopMemberOrderController extends BaseController {

    private final IShopOrderService orderService;

    /**
     * 取得當前會員的訂單列表
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String status) {
        Long memberId = SecurityUtils.getUserId();
        List<ShopOrder> orders = orderService.selectOrdersByMemberId(memberId, status);
        return success(orders);
    }

    /**
     * 取得訂單詳情
     */
    @GetMapping("/{orderNo}")
    public AjaxResult getInfo(@PathVariable String orderNo) {
        Long memberId = SecurityUtils.getUserId();
        ShopOrder order = orderService.selectOrderByOrderNo(orderNo);

        // 檢查是否屬於當前會員
        if (order == null || !order.getMemberId().equals(memberId)) {
            return error("訂單不存在或無權查看");
        }

        return success(order);
    }

    /**
     * 取得訂單狀態統計
     */
    @GetMapping("/stats")
    public AjaxResult getStats() {
        Long memberId = SecurityUtils.getUserId();
        Map<String, Integer> stats = orderService.countOrderByStatus(memberId);
        return success(stats);
    }

    /**
     * 取消訂單
     */
    @Log(title = "會員取消訂單", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{orderId}")
    public AjaxResult cancel(@PathVariable Long orderId,
                             @RequestParam(required = false, defaultValue = "會員主動取消") String reason) {
        Long memberId = SecurityUtils.getUserId();

        // 檢查訂單歸屬
        ShopOrder order = orderService.selectOrderById(orderId);
        if (order == null || !order.getMemberId().equals(memberId)) {
            return error("訂單不存在或無權操作");
        }

        return toAjax(orderService.cancelOrder(orderId, reason));
    }

    /**
     * 確認收貨
     */
    @Log(title = "會員確認收貨", businessType = BusinessType.UPDATE)
    @PostMapping("/confirm/{orderId}")
    public AjaxResult confirmReceipt(@PathVariable Long orderId) {
        Long memberId = SecurityUtils.getUserId();

        // 檢查訂單歸屬
        ShopOrder order = orderService.selectOrderById(orderId);
        if (order == null || !order.getMemberId().equals(memberId)) {
            return error("訂單不存在或無權操作");
        }

        return toAjax(orderService.deliverOrder(orderId));
    }
}
