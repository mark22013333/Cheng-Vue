package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopOrder;
import com.cheng.shop.service.IShopOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 訂單 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/order")
@RequiredArgsConstructor
public class ShopOrderController extends BaseController {

    private final IShopOrderService orderService;

    /**
     * 查詢訂單列表
     */
    @PreAuthorize("@ss.hasPermi('shop:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(ShopOrder order) {
        startPage();
        List<ShopOrder> list = orderService.selectOrderList(order);
        return getDataTable(list);
    }

    /**
     * 查詢訂單詳情
     */
    @PreAuthorize("@ss.hasPermi('shop:order:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId) {
        return success(orderService.selectOrderById(orderId));
    }

    /**
     * 根據訂單編號查詢
     */
    @PreAuthorize("@ss.hasPermi('shop:order:query')")
    @GetMapping("/no/{orderNo}")
    public AjaxResult getByOrderNo(@PathVariable String orderNo) {
        return success(orderService.selectOrderByOrderNo(orderNo));
    }

    /**
     * 出貨
     */
    @PreAuthorize("@ss.hasPermi('shop:order:ship')")
    @Log(title = "訂單管理", businessType = BusinessType.UPDATE)
    @PutMapping("/ship")
    public AjaxResult ship(@RequestBody ShopOrder order) {
        ShopOrder existingOrder = orderService.selectOrderById(order.getOrderId());
        int result = orderService.shipOrder(order.getOrderId(), order.getShippingNo());
        AjaxResult ajaxResult = toAjax(result);
        if (existingOrder != null) {
            ajaxResult.put("orderNo", existingOrder.getOrderNo());
            ajaxResult.put("shippingNo", order.getShippingNo());
            ajaxResult.put("action", "出貨");
        }
        return ajaxResult;
    }

    /**
     * 取消訂單
     */
    @PreAuthorize("@ss.hasPermi('shop:order:cancel')")
    @Log(title = "訂單管理", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel")
    public AjaxResult cancel(@RequestBody ShopOrder order) {
        ShopOrder existingOrder = orderService.selectOrderById(order.getOrderId());
        int result = orderService.cancelOrder(order.getOrderId(), order.getCancelReason(), SecurityUtils.getUserId());
        AjaxResult ajaxResult = toAjax(result);
        if (existingOrder != null) {
            ajaxResult.put("orderNo", existingOrder.getOrderNo());
            ajaxResult.put("reason", order.getCancelReason());
            ajaxResult.put("action", "取消");
        }
        return ajaxResult;
    }

    /**
     * 確認收貨
     */
    @PreAuthorize("@ss.hasPermi('shop:order:deliver')")
    @Log(title = "訂單管理", businessType = BusinessType.UPDATE)
    @PutMapping("/deliver/{orderId}")
    public AjaxResult deliver(@PathVariable Long orderId) {
        ShopOrder existingOrder = orderService.selectOrderById(orderId);
        int result = orderService.deliverOrder(orderId);
        AjaxResult ajaxResult = toAjax(result);
        if (existingOrder != null) {
            ajaxResult.put("orderNo", existingOrder.getOrderNo());
            ajaxResult.put("action", "確認收貨");
        }
        return ajaxResult;
    }

    /**
     * 完成訂單
     */
    @PreAuthorize("@ss.hasPermi('shop:order:complete')")
    @Log(title = "訂單管理", businessType = BusinessType.UPDATE)
    @PutMapping("/complete/{orderId}")
    public AjaxResult complete(@PathVariable Long orderId) {
        ShopOrder existingOrder = orderService.selectOrderById(orderId);
        int result = orderService.completeOrder(orderId);
        AjaxResult ajaxResult = toAjax(result);
        if (existingOrder != null) {
            ajaxResult.put("orderNo", existingOrder.getOrderNo());
            ajaxResult.put("action", "完成");
        }
        return ajaxResult;
    }

    /**
     * 更新訂單備註
     */
    @PreAuthorize("@ss.hasPermi('shop:order:edit')")
    @Log(title = "訂單管理", businessType = BusinessType.UPDATE)
    @PutMapping("/remark")
    public AjaxResult updateRemark(@RequestBody ShopOrder order) {
        int result = orderService.updateOrderRemark(order.getOrderId(), order.getSellerRemark());
        return toAjax(result);
    }

    /**
     * 手動更新付款狀態
     */
    @PreAuthorize("@ss.hasPermi('shop:order:updatePayStatus')")
    @Log(title = "訂單管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderId}/payStatus")
    public AjaxResult updatePayStatus(@PathVariable Long orderId, @RequestParam String payStatus) {
        ShopOrder existingOrder = orderService.selectOrderById(orderId);
        int result = orderService.updatePayStatus(orderId, payStatus);
        AjaxResult ajaxResult = toAjax(result);
        if (existingOrder != null) {
            ajaxResult.put("orderNo", existingOrder.getOrderNo());
            ajaxResult.put("payStatus", payStatus);
            ajaxResult.put("action", "更新付款狀態");
        }
        return ajaxResult;
    }

    /**
     * 手動更新物流狀態
     */
    @PreAuthorize("@ss.hasPermi('shop:order:updateShipStatus')")
    @Log(title = "訂單管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderId}/shipStatus")
    public AjaxResult updateShipStatus(@PathVariable Long orderId, @RequestParam String shipStatus) {
        ShopOrder existingOrder = orderService.selectOrderById(orderId);
        int result = orderService.updateShipStatus(orderId, shipStatus);
        AjaxResult ajaxResult = toAjax(result);
        if (existingOrder != null) {
            ajaxResult.put("orderNo", existingOrder.getOrderNo());
            ajaxResult.put("shipStatus", shipStatus);
            ajaxResult.put("action", "更新物流狀態");
        }
        return ajaxResult;
    }

    /**
     * 重新建立物流訂單
     * <p>
     * 用於超商取貨訂單物流單號產生失敗時，手動觸發重新建立。
     */
    @PreAuthorize("@ss.hasPermi('shop:order:ship')")
    @Log(title = "訂單管理", businessType = BusinessType.UPDATE)
    @PostMapping("/{orderId}/recreate-logistics")
    public AjaxResult recreateLogistics(@PathVariable Long orderId) {
        ShopOrder existingOrder = orderService.selectOrderById(orderId);
        String shippingNo = orderService.recreateLogistics(orderId);
        AjaxResult ajaxResult = success("物流訂單建立成功");
        ajaxResult.put("shippingNo", shippingNo);
        if (existingOrder != null) {
            ajaxResult.put("orderNo", existingOrder.getOrderNo());
        }
        return ajaxResult;
    }
}
