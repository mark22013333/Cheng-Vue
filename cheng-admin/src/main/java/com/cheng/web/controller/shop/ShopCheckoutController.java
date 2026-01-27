package com.cheng.web.controller.shop;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.dto.CheckoutSubmitRequest;
import com.cheng.shop.domain.vo.CheckoutPreviewVO;
import com.cheng.shop.domain.vo.CheckoutResultVO;
import com.cheng.shop.service.IShopCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 結帳 Controller（會員前台，只需登入不需特定權限）
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/checkout")
@RequiredArgsConstructor
public class ShopCheckoutController extends BaseController {

    private final IShopCheckoutService checkoutService;

    /**
     * 結帳預覽
     */
    @GetMapping("/preview")
    public AjaxResult preview(@RequestParam(required = false) Long addressId) {
        Long memberId = SecurityUtils.getUserId();
        CheckoutPreviewVO preview = checkoutService.preview(memberId, addressId);
        return success(preview);
    }

    /**
     * 提交訂單
     */
    @Log(title = "結帳", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@Valid @RequestBody CheckoutSubmitRequest request) {
        Long memberId = SecurityUtils.getUserId();
        CheckoutResultVO result = checkoutService.submit(memberId, request);
        return success(result);
    }
}
