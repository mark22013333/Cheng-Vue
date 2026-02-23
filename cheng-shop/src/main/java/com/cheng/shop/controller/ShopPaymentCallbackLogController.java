package com.cheng.shop.controller;

import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.shop.domain.ShopPaymentCallbackLog;
import com.cheng.shop.service.IShopPaymentCallbackLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 金流回調紀錄 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/payment/callback/log")
@RequiredArgsConstructor
public class ShopPaymentCallbackLogController extends BaseController {

    private final IShopPaymentCallbackLogService logService;

    /**
     * 查詢金流回調紀錄列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.PaymentCallback.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(ShopPaymentCallbackLog log) {
        startPage();
        List<ShopPaymentCallbackLog> list = logService.selectLogList(log);
        return getDataTable(list);
    }

    /**
     * 查詢金流回調紀錄詳情
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Shop.PaymentCallback.QUERY + "')")
    @GetMapping("/{logId}")
    public AjaxResult getInfo(@PathVariable Long logId) {
        return success(logService.selectLogById(logId));
    }
}
